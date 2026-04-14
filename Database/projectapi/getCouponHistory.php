<?php
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json; charset=utf-8');

// Connect to database
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "DB connection failed"]);
    exit;
}

// Validate input
$cid = isset($_GET['cid']) ? intval($_GET['cid']) : 0;
$lang = isset($_GET['lang']) ? $_GET['lang'] : 'en';

$allowedLang = ['en', 'zh-TW', 'zh-CN'];
if (!in_array($lang, $allowedLang, true)) {
    $lang = 'en';
}

if ($cid <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "error" => "Missing or invalid cid"]);
    exit;
}

// Backfill missing earn records from paid card orders so coupon history matches order history better.
// Rule aligned with current backend logic: points = SUM(order_items price * qty).
$backfillSql = "INSERT INTO coupon_point_history (cid, coupon_id, delta, resulting_points, action, note, created_at)
                SELECT o.cid,
                       NULL,
                       COALESCE(SUM(mi.item_price * oi.qty), 0) AS delta,
                       0 AS resulting_points,
                       'earn' AS action,
                       CONCAT('Payment for order #', o.oid) AS note,
                       o.odate AS created_at
                FROM orders o
                LEFT JOIN order_items oi ON oi.oid = o.oid
                LEFT JOIN menu_item mi ON mi.item_id = oi.item_id
                WHERE o.cid = ?
                  AND o.payment_method = 'card'
                  AND o.ostatus IN (1, 2)
                  AND NOT EXISTS (
                      SELECT 1
                      FROM coupon_point_history h
                      WHERE h.cid = o.cid
                        AND h.action = 'earn'
                        AND (
                            h.note = CONCAT('Payment for order #', o.oid)
                            OR h.note LIKE CONCAT('Cash payment confirmed for order #', o.oid, '%')
                        )
                  )
                GROUP BY o.oid, o.cid, o.odate";

$backfillStmt = $conn->prepare($backfillSql);
if ($backfillStmt) {
    $backfillStmt->bind_param("i", $cid);
    $backfillStmt->execute();
    $backfillStmt->close();
}

// Build query: localized action text + computed running resulting points from latest customer balance.
$sql = "SELECT h.delta,
               (
                   c.coupon_point
                   - COALESCE(
                       SUM(h.delta) OVER (
                           PARTITION BY h.cid
                           ORDER BY h.created_at DESC, h.cph_id DESC
                           ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING
                       ),
                       0
                   )
               ) AS resulting_points,
               CASE
                   WHEN ? = 'zh-TW' THEN
                       CASE h.action
                           WHEN 'earn' THEN '賺取'
                           WHEN 'redeem' THEN '兌換'
                           ELSE h.action
                       END
                   WHEN ? = 'zh-CN' THEN
                       CASE h.action
                           WHEN 'earn' THEN '赚取'
                           WHEN 'redeem' THEN '兑换'
                           ELSE h.action
                       END
                   ELSE h.action
               END AS action,
               h.note,
               h.created_at,
               ct.title AS coupon_title
        FROM coupon_point_history h
        JOIN customer c ON c.cid = h.cid
        LEFT JOIN coupon_translation ct
               ON h.coupon_id = ct.coupon_id AND ct.language_code = ?
        WHERE h.cid = ?
        ORDER BY h.created_at DESC, h.cph_id DESC";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Prepare failed: " . $conn->error]);
    exit;
}

$stmt->bind_param("sssi", $lang, $lang, $lang, $cid);
$stmt->execute();
$result = $stmt->get_result();

$history = [];
while ($row = $result->fetch_assoc()) {
    $history[] = [
        "delta"            => intval($row['delta']),
        "resulting_points" => intval($row['resulting_points']),
        "action"           => $row['action'],
        "note"             => $row['note'],
        "created_at"       => $row['created_at'],
        "coupon_title"     => $row['coupon_title']
    ];
}

http_response_code(200);
echo json_encode([
    "success" => true,
    "history" => $history
], JSON_UNESCAPED_UNICODE);

$stmt->close();
$conn->close();
?>
