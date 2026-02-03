<?php
$token = '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';

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://api.github.com/repos/LEO7526/FYP/contents/Image');
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Authorization: token ' . $token,
    'User-Agent: PHP-Script'
]);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

echo "HTTP: $httpCode\n";
echo "response:\n$response\n";

if ($httpCode === 200) {
    echo "\n✅ efficient\n";
} elseif ($httpCode === 401) {
    echo "\n❌ Invalid or expired\n";
} elseif ($httpCode === 403) {
    echo "\n❌ Insufficient permissions or rate limit reached\n";
} elseif ($httpCode === 404) {
    echo "\n❌ Does not exist or path is incorrect\n";
}