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
$cid = $input['cid'] ?? null;
$ostatus = $input['ostatus'] ?? 0;
$items = $input['items'] ?? [];
$odate = date('Y-m-d H:i:s'); // Current timestamp

if ($cid === null || empty($items)) {
    error_log("Missing required fields: cid=$cid, items=" . json_encode($items));
    echo json_encode(["error" => "Missing required fields"]);
    exit;
}

// Insert into orders table (removed ocost and odeliverdate as they don't exist in your schema)
$stmt = $conn->prepare("
    INSERT INTO orders (odate, cid, ostatus)
    VALUES (?, ?, ?)
");
if (!$stmt) {
    error_log("Prepare failed for orders: " . $conn->error);
    echo json_encode(["error" => "Failed to prepare order insert"]);
    exit;
}

$stmt->bind_param("sii", $odate, $cid, $ostatus);

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

// Insert each item into order_items (adjusted for your schema)
foreach ($items as $item) {
    $item_id = $item['item_id'] ?? null;
    $qty = $item['qty'] ?? null;

    if (!$item_id || !$qty) {
        error_log("Skipping invalid item: " . json_encode($item));
        continue;
    }

    $itemStmt = $conn->prepare("
        INSERT INTO order_items (oid, item_id, qty)
        VALUES (?, ?, ?)
    ");
    if (!$itemStmt) {
        error_log("Prepare failed for item: " . $conn->error);
        continue;
    }

    $itemStmt->bind_param("iii", $order_id, $item_id, $qty);

    if (!$itemStmt->execute()) {
        error_log("Execute failed for item: item_id=$item_id, qty=$qty — " . $itemStmt->error);
    } else {
        error_log("Item saved: order_id=$order_id, item_id=$item_id, qty=$qty");
    }

    $itemStmt->close();
}

// Staff-specific logic for table orders
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
?>