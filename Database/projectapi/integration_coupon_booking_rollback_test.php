<?php
// Positive integration test with rollback for coupon + booking management.
// Run:
// php Database/projectapi/integration_coupon_booking_rollback_test.php
// php Database/projectapi/integration_coupon_booking_rollback_test.php --cid=1 --days-ahead=3 --pnum=2 --times=12:00,18:30,20:00

date_default_timezone_set('Asia/Hong_Kong');

function print_usage(): void {
    echo 'Usage: php Database/projectapi/integration_coupon_booking_rollback_test.php [options]' . PHP_EOL;
    echo '  --cid=INT          Customer id (default: 1)' . PHP_EOL;
    echo '  --days-ahead=INT   Booking date offset in days, >= 2 (default: 3)' . PHP_EOL;
    echo '  --pnum=INT         Guest count for booking test (default: 2)' . PHP_EOL;
    echo '  --times=CSV        Preferred booking slots, HH:MM comma list (default: 12:00,18:30,20:00)' . PHP_EOL;
    echo '  --base-url=URL     API base URL (default: http://127.0.0.1/newFolder/Database/projectapi)' . PHP_EOL;
    echo '  --help             Show this help' . PHP_EOL;
}

function normalize_time_slots(string $raw): array {
    $slots = array_filter(array_map('trim', explode(',', $raw)), static function ($v) {
        return $v !== '';
    });

    $normalized = [];
    foreach ($slots as $slot) {
        if (preg_match('/^(?:[01]\d|2[0-3]):[0-5]\d$/', $slot)) {
            $normalized[] = $slot;
        }
    }

    return array_values(array_unique($normalized));
}

$cliOptions = getopt('', ['cid:', 'days-ahead:', 'pnum:', 'times:', 'base-url:', 'help']);
if ($cliOptions === false) {
    $cliOptions = [];
}

if (isset($cliOptions['help'])) {
    print_usage();
    exit(0);
}

$baseUrl = 'http://127.0.0.1/newFolder/Database/projectapi';
$cid = 1;
$daysAhead = 3;
$guestCount = 2;
$preferredSlots = ['12:00', '18:30', '20:00'];

if (isset($cliOptions['cid'])) {
    $cid = intval($cliOptions['cid']);
}

if (isset($cliOptions['days-ahead'])) {
    $daysAhead = intval($cliOptions['days-ahead']);
}

if (isset($cliOptions['pnum'])) {
    $guestCount = intval($cliOptions['pnum']);
}

if (isset($cliOptions['times'])) {
    $parsed = normalize_time_slots((string)$cliOptions['times']);
    if (empty($parsed)) {
        fwrite(STDERR, 'Invalid --times. Use comma-separated HH:MM values, e.g. 12:00,18:30' . PHP_EOL);
        exit(1);
    }
    $preferredSlots = $parsed;
}

if (isset($cliOptions['base-url'])) {
    $candidateUrl = rtrim((string)$cliOptions['base-url'], '/');
    if ($candidateUrl === '') {
        fwrite(STDERR, 'Invalid --base-url. Value cannot be empty.' . PHP_EOL);
        exit(1);
    }
    $baseUrl = $candidateUrl;
}

if ($cid <= 0) {
    fwrite(STDERR, 'Invalid --cid. Must be greater than 0.' . PHP_EOL);
    exit(1);
}

if ($daysAhead < 2) {
    fwrite(STDERR, 'Invalid --days-ahead. Must be at least 2 to satisfy 24-hour booking rule.' . PHP_EOL);
    exit(1);
}

if ($guestCount <= 0) {
    fwrite(STDERR, 'Invalid --pnum. Must be greater than 0.' . PHP_EOL);
    exit(1);
}

$runId = 'AUTO_IT_' . date('Ymd_His') . '_' . random_int(1000, 9999);

$results = [];
$hasFailure = false;

function record_result(array &$results, string $name, bool $pass, string $detail): void {
    $results[] = [
        'test' => $name,
        'pass' => $pass,
        'detail' => $detail
    ];
}

function parse_status_line(string $statusLine): int {
    if (preg_match('/HTTP\/\d+\.\d+\s+(\d{3})/', $statusLine, $m)) {
        return (int)$m[1];
    }
    return 0;
}

function http_request(string $method, string $url, ?string $contentType = null, ?string $body = null): array {
    if (function_exists('curl_init')) {
        $ch = curl_init($url);
        $headers = [];

        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
        curl_setopt($ch, CURLOPT_HEADER, true);

        if ($contentType !== null) {
            $headers[] = 'Content-Type: ' . $contentType;
        }

        if (!empty($headers)) {
            curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        }

        if ($body !== null) {
            curl_setopt($ch, CURLOPT_POSTFIELDS, $body);
        }

        $raw = curl_exec($ch);
        if ($raw === false) {
            $err = curl_error($ch);
            curl_close($ch);
            return [
                'status' => 0,
                'body' => '',
                'error' => $err
            ];
        }

        $headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
        $status = (int)curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $responseBody = substr($raw, $headerSize);
        curl_close($ch);

        return [
            'status' => $status,
            'body' => $responseBody,
            'error' => null
        ];
    }

    $opts = [
        'http' => [
            'method' => $method,
            'ignore_errors' => true
        ]
    ];

    if ($contentType !== null) {
        $opts['http']['header'] = 'Content-Type: ' . $contentType;
    }

    if ($body !== null) {
        $opts['http']['content'] = $body;
    }

    $context = stream_context_create($opts);
    $responseBody = @file_get_contents($url, false, $context);
    $headers = $http_response_header ?? [];
    $status = 0;

    if (!empty($headers)) {
        $status = parse_status_line($headers[0]);
    }

    if ($responseBody === false) {
        return [
            'status' => $status,
            'body' => '',
            'error' => 'HTTP request failed'
        ];
    }

    return [
        'status' => $status,
        'body' => $responseBody,
        'error' => null
    ];
}

function http_get_json(string $url): array {
    $resp = http_request('GET', $url, null, null);
    $data = json_decode($resp['body'], true);
    return [
        'status' => $resp['status'],
        'data' => $data,
        'raw' => $resp['body'],
        'error' => $resp['error']
    ];
}

function http_post_form_json(string $url, array $fields): array {
    $resp = http_request('POST', $url, 'application/x-www-form-urlencoded', http_build_query($fields));
    $data = json_decode($resp['body'], true);
    return [
        'status' => $resp['status'],
        'data' => $data,
        'raw' => $resp['body'],
        'error' => $resp['error']
    ];
}

function http_post_json_json(string $url, array $payload): array {
    $resp = http_request('POST', $url, 'application/json', json_encode($payload));
    $data = json_decode($resp['body'], true);
    return [
        'status' => $resp['status'],
        'data' => $data,
        'raw' => $resp['body'],
        'error' => $resp['error']
    ];
}

function query_one(mysqli $conn, string $sql, string $types = '', array $params = []): ?array {
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new RuntimeException('Prepare failed: ' . $conn->error . ' SQL=' . $sql);
    }

    if ($types !== '') {
        $stmt->bind_param($types, ...$params);
    }

    if (!$stmt->execute()) {
        $err = $stmt->error;
        $stmt->close();
        throw new RuntimeException('Execute failed: ' . $err . ' SQL=' . $sql);
    }

    $result = $stmt->get_result();
    $row = $result ? $result->fetch_assoc() : null;
    $stmt->close();
    return $row;
}

function execute_stmt(mysqli $conn, string $sql, string $types = '', array $params = []): int {
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new RuntimeException('Prepare failed: ' . $conn->error . ' SQL=' . $sql);
    }

    if ($types !== '') {
        $stmt->bind_param($types, ...$params);
    }

    if (!$stmt->execute()) {
        $err = $stmt->error;
        $stmt->close();
        throw new RuntimeException('Execute failed: ' . $err . ' SQL=' . $sql);
    }

    $affected = $stmt->affected_rows;
    $stmt->close();
    return $affected;
}

$conn = new mysqli('localhost', 'root', '', 'ProjectDB');
if ($conn->connect_error) {
    fwrite(STDERR, 'DB connection failed: ' . $conn->connect_error . PHP_EOL);
    exit(1);
}

$originalPoints = null;
$testStartPoints = null;
$couponId = null;
$couponPointsRequired = 0;
$beforeRedemptionMax = 0;
$beforeHistoryMax = 0;
$bookingId = null;
$bookingDate = null;
$bookingTime = null;
$bookingRemark = 'IT_ROLLBACK_' . $runId;

try {
    // ----- Coupon positive flow with rollback -----
    $customer = query_one($conn, 'SELECT coupon_point FROM customer WHERE cid = ?', 'i', [$cid]);
    if (!$customer) {
        throw new RuntimeException('Customer not found for cid=' . $cid);
    }

    $originalPoints = (int)$customer['coupon_point'];

    $coupon = query_one(
        $conn,
        "SELECT c.coupon_id, c.points_required
         FROM coupons c
         LEFT JOIN coupon_rules r ON c.coupon_id = r.coupon_id
         WHERE c.is_active = 1
           AND c.coupon_id <> 4
           AND (r.birthday_only IS NULL OR r.birthday_only = 0)
         ORDER BY c.points_required ASC, c.coupon_id ASC
         LIMIT 1"
    );

    if (!$coupon) {
        throw new RuntimeException('No suitable active coupon found for positive redeem test');
    }

    $couponId = (int)$coupon['coupon_id'];
    $couponPointsRequired = (int)$coupon['points_required'];

    $redemptionRow = query_one(
        $conn,
        'SELECT COALESCE(MAX(redemption_id), 0) AS max_id FROM coupon_redemptions WHERE cid = ? AND coupon_id = ?',
        'ii',
        [$cid, $couponId]
    );
    $beforeRedemptionMax = (int)($redemptionRow['max_id'] ?? 0);

    $historyRow = query_one(
        $conn,
        'SELECT COALESCE(MAX(cph_id), 0) AS max_id FROM coupon_point_history WHERE cid = ? AND coupon_id = ?',
        'ii',
        [$cid, $couponId]
    );
    $beforeHistoryMax = (int)($historyRow['max_id'] ?? 0);

    if ($originalPoints < $couponPointsRequired) {
        $testStartPoints = $couponPointsRequired + 20;
        execute_stmt($conn, 'UPDATE customer SET coupon_point = ? WHERE cid = ?', 'ii', [$testStartPoints, $cid]);
    } else {
        $testStartPoints = $originalPoints;
    }

    $redeemResp = http_post_form_json(
        $baseUrl . '/redeemCoupon.php',
        [
            'cid' => $cid,
            'coupon_id' => $couponId,
            'quantity' => 1
        ]
    );

    $redeemOk = $redeemResp['error'] === null
        && $redeemResp['status'] === 200
        && is_array($redeemResp['data'])
        && (($redeemResp['data']['success'] ?? false) === true);

    record_result(
        $results,
        'Coupon redeem positive API',
        $redeemOk,
        'status=' . $redeemResp['status'] . ', raw=' . substr((string)$redeemResp['raw'], 0, 200)
    );

    if (!$redeemOk) {
        throw new RuntimeException('Coupon positive redeem API test failed');
    }

    $afterPointsRow = query_one($conn, 'SELECT coupon_point FROM customer WHERE cid = ?', 'i', [$cid]);
    $expectedPointsAfter = $testStartPoints - $couponPointsRequired;
    $actualPointsAfter = (int)($afterPointsRow['coupon_point'] ?? -999999);

    record_result(
        $results,
        'Coupon points deducted correctly',
        $actualPointsAfter === $expectedPointsAfter,
        'expected=' . $expectedPointsAfter . ', actual=' . $actualPointsAfter
    );

    $newRedemptionCountRow = query_one(
        $conn,
        'SELECT COUNT(*) AS cnt FROM coupon_redemptions WHERE cid = ? AND coupon_id = ? AND redemption_id > ?',
        'iii',
        [$cid, $couponId, $beforeRedemptionMax]
    );
    $newRedemptionCount = (int)($newRedemptionCountRow['cnt'] ?? 0);

    record_result(
        $results,
        'Coupon redemption row created',
        $newRedemptionCount >= 1,
        'new_rows=' . $newRedemptionCount
    );

    $newHistoryCountRow = query_one(
        $conn,
        "SELECT COUNT(*) AS cnt FROM coupon_point_history
         WHERE cid = ? AND coupon_id = ? AND cph_id > ? AND action = 'redeem'",
        'iii',
        [$cid, $couponId, $beforeHistoryMax]
    );
    $newHistoryCount = (int)($newHistoryCountRow['cnt'] ?? 0);

    record_result(
        $results,
        'Coupon history row created',
        $newHistoryCount >= 1,
        'new_rows=' . $newHistoryCount
    );

    // ----- Booking positive flow with rollback -----
    $slots = $preferredSlots;
    $bookingDate = (new DateTime('now'))->modify('+' . $daysAhead . ' days')->format('Y-m-d');
    $tableId = 0;

    foreach ($slots as $slot) {
        $availableResp = http_get_json(
            $baseUrl . '/get_available_tables.php?date=' . urlencode($bookingDate)
            . '&time=' . urlencode($slot)
            . '&pnum=' . $guestCount
        );

        if ($availableResp['error'] !== null || $availableResp['status'] !== 200 || !is_array($availableResp['data'])) {
            continue;
        }

        if (count($availableResp['data']) > 0 && isset($availableResp['data'][0]['tid'])) {
            $tableId = (int)$availableResp['data'][0]['tid'];
            $bookingTime = $slot;
            break;
        }
    }

    if ($tableId <= 0 || $bookingTime === null) {
        record_result(
            $results,
            'Booking slot available',
            false,
            'No available table found for date=' . $bookingDate . ', pnum=' . $guestCount . ', slots=' . implode(',', $slots)
        );
        throw new RuntimeException('No available table found for booking positive test');
    }

    record_result($results, 'Booking slot available', true, 'date=' . $bookingDate . ', time=' . $bookingTime . ', tid=' . $tableId);

    $createBookingResp = http_post_json_json(
        $baseUrl . '/create_booking.php',
        [
            'cid' => $cid,
            'bkcname' => 'Auto Integration',
            'bktel' => '91234567',
            'tid' => $tableId,
            'bdate' => $bookingDate,
            'btime' => $bookingTime,
            'pnum' => $guestCount,
            'purpose' => 'integration-test',
            'remark' => $bookingRemark
        ]
    );

    $createOk = $createBookingResp['error'] === null
        && $createBookingResp['status'] === 201
        && is_array($createBookingResp['data'])
        && (($createBookingResp['data']['success'] ?? false) === true);

    record_result(
        $results,
        'Booking create positive API',
        $createOk,
        'status=' . $createBookingResp['status'] . ', raw=' . substr((string)$createBookingResp['raw'], 0, 200)
    );

    if (!$createOk) {
        throw new RuntimeException('Booking create positive API test failed');
    }

    $bookingRow = query_one(
        $conn,
        'SELECT bid, status FROM booking WHERE cid = ? AND remark = ? ORDER BY bid DESC LIMIT 1',
        'is',
        [$cid, $bookingRemark]
    );

    if (!$bookingRow) {
        record_result($results, 'Booking row persisted', false, 'No row found by remark=' . $bookingRemark);
        throw new RuntimeException('Booking row was not found after create');
    }

    $bookingId = (int)$bookingRow['bid'];
    record_result($results, 'Booking row persisted', $bookingId > 0, 'bid=' . $bookingId . ', status=' . $bookingRow['status']);

    $cancelResp = http_post_json_json(
        $baseUrl . '/cancel_booking.php',
        [
            'bid' => $bookingId,
            'cid' => $cid
        ]
    );

    $cancelOk = $cancelResp['error'] === null
        && $cancelResp['status'] === 200
        && is_array($cancelResp['data'])
        && (($cancelResp['data']['success'] ?? false) === true);

    record_result(
        $results,
        'Booking cancel positive API',
        $cancelOk,
        'status=' . $cancelResp['status'] . ', raw=' . substr((string)$cancelResp['raw'], 0, 200)
    );

    if (!$cancelOk) {
        throw new RuntimeException('Booking cancel positive API test failed');
    }

    $bookingStatusRow = query_one($conn, 'SELECT status FROM booking WHERE bid = ? AND cid = ?', 'ii', [$bookingId, $cid]);
    $isCancelled = $bookingStatusRow && (int)$bookingStatusRow['status'] === 0;
    record_result($results, 'Booking status updated to cancelled', $isCancelled, 'status=' . ($bookingStatusRow['status'] ?? 'null'));

} catch (Throwable $e) {
    $hasFailure = true;
    record_result($results, 'Test runner exception', false, $e->getMessage());
} finally {
    // Rollback coupon test data.
    try {
        if ($couponId !== null) {
            execute_stmt(
                $conn,
                'DELETE FROM coupon_redemptions WHERE cid = ? AND coupon_id = ? AND redemption_id > ?',
                'iii',
                [$cid, $couponId, $beforeRedemptionMax]
            );

            execute_stmt(
                $conn,
                "DELETE FROM coupon_point_history
                 WHERE cid = ? AND coupon_id = ? AND cph_id > ? AND action = 'redeem'",
                'iii',
                [$cid, $couponId, $beforeHistoryMax]
            );
        }

        if ($originalPoints !== null) {
            execute_stmt($conn, 'UPDATE customer SET coupon_point = ? WHERE cid = ?', 'ii', [$originalPoints, $cid]);

            $restoredRow = query_one($conn, 'SELECT coupon_point FROM customer WHERE cid = ?', 'i', [$cid]);
            $restored = $restoredRow && (int)$restoredRow['coupon_point'] === (int)$originalPoints;
            record_result(
                $results,
                'Coupon rollback restore points',
                $restored,
                'expected=' . $originalPoints . ', actual=' . ($restoredRow['coupon_point'] ?? 'null')
            );
            if (!$restored) {
                $hasFailure = true;
            }
        }
    } catch (Throwable $rollbackErr) {
        $hasFailure = true;
        record_result($results, 'Coupon rollback exception', false, $rollbackErr->getMessage());
    }

    // Rollback booking test data.
    try {
        if ($bookingId !== null) {
            $deleted = execute_stmt($conn, 'DELETE FROM booking WHERE bid = ? AND cid = ?', 'ii', [$bookingId, $cid]);
            record_result($results, 'Booking rollback delete row', $deleted >= 1, 'deleted_rows=' . $deleted . ', bid=' . $bookingId);
            if ($deleted < 1) {
                $hasFailure = true;
            }
        }
    } catch (Throwable $rollbackErr) {
        $hasFailure = true;
        record_result($results, 'Booking rollback exception', false, $rollbackErr->getMessage());
    }

    $conn->close();
}

$passCount = 0;
foreach ($results as $r) {
    if ($r['pass']) {
        $passCount++;
    } else {
        $hasFailure = true;
    }
}

echo '===== Integration Test (Positive + Rollback) =====' . PHP_EOL;
echo 'Config: cid=' . $cid . ', daysAhead=' . $daysAhead . ', pnum=' . $guestCount . ', slots=' . implode(',', $preferredSlots) . ', baseUrl=' . $baseUrl . PHP_EOL;
foreach ($results as $r) {
    $mark = $r['pass'] ? 'PASS' : 'FAIL';
    echo '[' . $mark . '] ' . $r['test'] . ' :: ' . $r['detail'] . PHP_EOL;
}

echo 'Summary: total=' . count($results) . ', passed=' . $passCount . ', failed=' . (count($results) - $passCount) . PHP_EOL;

echo 'RunId: ' . $runId . PHP_EOL;

exit($hasFailure ? 1 : 0);
