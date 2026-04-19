<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

function isValidIpv4($ip)
{
    return filter_var($ip, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4) !== false;
}

function isUsableLanIp($ip)
{
    if (!isValidIpv4($ip)) {
        return false;
    }

    if ($ip === '127.0.0.1' || $ip === '0.0.0.0') {
        return false;
    }

    return true;
}

function extractHostIp($host)
{
    if (!$host) {
        return null;
    }

    // Remove port suffix if present (e.g. 192.168.0.121:80)
    $hostOnly = preg_replace('/:\\d+$/', '', trim($host));

    return isValidIpv4($hostOnly) ? $hostOnly : null;
}

function uniquePush(array &$list, $value)
{
    if ($value !== null && $value !== '' && !in_array($value, $list, true)) {
        $list[] = $value;
    }
}

function getSubnet24($ip)
{
    if (!isValidIpv4($ip)) {
        return null;
    }

    $parts = explode('.', $ip);
    return $parts[0] . '.' . $parts[1] . '.' . $parts[2];
}

try {
    $scheme = 'http';
    if (
        (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off' && $_SERVER['HTTPS'] !== '') ||
        (isset($_SERVER['REQUEST_SCHEME']) && $_SERVER['REQUEST_SCHEME'] === 'https')
    ) {
        $scheme = 'https';
    }

    $hostHeader = $_SERVER['HTTP_HOST'] ?? '';
    $serverName = $_SERVER['SERVER_NAME'] ?? '';
    $serverAddr = $_SERVER['SERVER_ADDR'] ?? '';
    $remoteAddr = $_SERVER['REMOTE_ADDR'] ?? '';

    $hostIp = extractHostIp($hostHeader);

    $candidates = [];
    uniquePush($candidates, $hostIp);

    if (isUsableLanIp($serverAddr)) {
        uniquePush($candidates, $serverAddr);
    }

    $hostname = gethostname();
    if ($hostname) {
        $ips = @gethostbynamel($hostname);
        if (is_array($ips)) {
            foreach ($ips as $ip) {
                if (isUsableLanIp($ip)) {
                    uniquePush($candidates, $ip);
                }
            }
        }

        $single = @gethostbyname($hostname);
        if (isUsableLanIp($single)) {
            uniquePush($candidates, $single);
        }
    }

    $resolvedIp = count($candidates) > 0 ? $candidates[0] : null;

    // If request comes from LAN device (e.g., Android phone), prefer same /24 subnet IP.
    $remoteSubnet = getSubnet24($remoteAddr);
    if ($remoteSubnet !== null && $remoteAddr !== '127.0.0.1') {
        foreach ($candidates as $candidate) {
            if (getSubnet24($candidate) === $remoteSubnet) {
                $resolvedIp = $candidate;
                break;
            }
        }
    }

    if ($resolvedIp === null) {
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'message' => 'Unable to resolve computer address dynamically.',
            'debug' => [
                'http_host' => $hostHeader,
                'server_name' => $serverName,
                'server_addr' => $serverAddr,
                'remote_addr' => $remoteAddr
            ]
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $displayHost = $hostHeader !== '' ? $hostHeader : $resolvedIp;

    echo json_encode([
        'success' => true,
        'computer_ip' => $resolvedIp,
        'request_host' => $displayHost,
        'scheme' => $scheme,
        'remote_addr' => $remoteAddr,
        'projectapi_base_url' => $scheme . '://' . $displayHost . '/newFolder/Database/projectapi/',
        'detected_candidates' => $candidates
    ], JSON_UNESCAPED_UNICODE);
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Unexpected error: ' . $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
}
