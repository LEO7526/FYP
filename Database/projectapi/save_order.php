<?php
header('Content-Type: application/json');

// Connect to MySQL
$conn = new mysqli("localhost", "root", "", "ProjectDB");
if ($conn->connect_error) {
    error_log("DB Connection failed: " . $conn->connect_error);
    echo json_encode(["error" => "Connection failed"]);
    exit;
}

// Read JSON input
$input = json_decode(file_get_contents("php://input"), true);
error_log("Received input: " . json_encode($input));

// Extract and validate fields
$ocost = $input['ocost'] ?? null;
$cid = $input['cid'] ?? null;
$odeliverdate = $input['odeliverdate'] ?? null;
$ostatus = $input['ostatus'] ?? 0;
$items = $input['items'] ?? [];
$odate = date('Y-m-d H:i:s'); // Current timestamp

if (!$ocost || $cid === null || empty($items)) {
    error_log("Missing required fields: ocost=$ocost, cid=$cid, items=" . json_encode($items));
    echo json_encode(["error" => "Missing required fields"]);
    exit;
}

// Insert into orders table
$stmt = $conn->prepare("
    INSERT INTO orders (odate, ocost, cid, odeliverdate, ostatus)
    VALUES (?, ?, ?, ?, ?)
");
if (!$stmt) {
    error_log("Prepare failed for orders: " . $conn->error);
    echo json_encode(["error" => "Failed to prepare order insert"]);
    exit;
}

$stmt->bind_param("sdssi", $odate, $ocost, $cid, $odeliverdate, $ostatus);

if (!$stmt->execute()) {
    error_log("Execute failed for orders: " . $stmt->error);
    echo json_encode(["error" => "Failed to save order header"]);
    $stmt->close();
    $conn->close();
    exit;
}

$order_id = $stmt->insert_id;
error_log("Order header saved with ID: $order_id");
$stmt->close();

// Insert each item into order_items
foreach ($items as $item) {
    $pid = $item['pid'] ?? null;
    $oqty = $item['oqty'] ?? null;
    $item_cost = $item['item_cost'] ?? null;

    if (!$pid || !$oqty || !$item_cost) {
        error_log("Skipping invalid item: " . json_encode($item));
        continue;
    }

    $itemStmt = $conn->prepare("
        INSERT INTO order_items (oid, pid, oqty, item_cost)
        VALUES (?, ?, ?, ?)
    ");
    if (!$itemStmt) {
        error_log("Prepare failed for item: " . $conn->error);
        continue;
    }

    $itemStmt->bind_param("iiid", $order_id, $pid, $oqty, $item_cost);

    if (!$itemStmt->execute()) {
        error_log("Execute failed for item: pid=$pid, qty=$oqty, cost=$item_cost — " . $itemStmt->error);
    } else {
        error_log("Item saved: order_id=$order_id, pid=$pid, qty=$oqty, cost=$item_cost");
    }

    $itemStmt->close();
}

// Staff-specific logic
$sid = $input['sid'] ?? null;
$table_number = $input['table_number'] ?? null;
$table_order_id = null;

if ($sid && $table_number) {
    $tableStmt = $conn->prepare("
        INSERT INTO table_orders (table_number, oid, staff_id, status)
        VALUES (?, ?, ?, 'ordering')
    ");
    if (!$tableStmt) {
        error_log("Prepare failed for table_orders: " . $conn->error);
        echo json_encode(["error" => "Failed to prepare table order insert"]);
        $conn->close();
        exit;
    }

    $tableStmt->bind_param("iii", $table_number, $order_id, $sid);

    if (!$tableStmt->execute()) {
        error_log("Execute failed for table_orders: " . $tableStmt->error);
        echo json_encode(["error" => "Failed to save table order"]);
        $tableStmt->close();
        $conn->close();
        exit;
    }

    $table_order_id = $tableStmt->insert_id;
    error_log("Table order saved with ID: $table_order_id");
    $tableStmt->close();
}

// Final response
$response = ["success" => true, "order_id" => $order_id];
if ($table_order_id !== null) {
    $response["table_order_id"] = $table_order_id;
}
echo json_encode($response);
$conn->close();