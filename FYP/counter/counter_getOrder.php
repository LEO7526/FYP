<?php
require_once __DIR__ . '/../conn.php';

header('Content-Type: application/json; charset=utf-8');

try {
    // Check database connection
    if (!$conn) {
        throw new Exception("Database connection failed: " . mysqli_connect_error());
    }

    // Fetch only pending orders (status=1)
    $query = "
        SELECT o.oid, o.odate, o.cid, o.ostatus, o.note, o.orderRef, 
               o.order_type, o.table_number
        FROM orders o
        WHERE o.ostatus = 0  -- Only show pending orders
        ORDER BY o.odate ASC  -- Show oldest orders first
    ";

    $result = $conn->query($query);

    if (!$result) {
        throw new Exception("Query failed: " . $conn->error);
    }

    $orders = [];
    $pendingCount = 0;

    // Prepare statement for individual items
    $itemStmt = $conn->prepare("
        SELECT oi.item_id, oi.qty, 
               COALESCE(mit.item_name, 'Unknown Item') as item_name
        FROM order_items oi
        LEFT JOIN menu_item_translation mit ON oi.item_id = mit.item_id AND mit.language_code = 'zh-TW'
        WHERE oi.oid = ?
    ");

    if (!$itemStmt) {
        throw new Exception("Failed to prepare item query: " . $conn->error);
    }

    // Prepare statement for package items
    $packageStmt = $conn->prepare("
        SELECT op.op_id, op.package_id, op.qty, mp.package_name
        FROM order_packages op
        LEFT JOIN menu_package mp ON op.package_id = mp.package_id
        WHERE op.oid = ?
    ");

    while ($row = $result->fetch_assoc()) {
        $oid = $row['oid'];
        $pendingCount++;

        // Get individual items for this order
        $items = [];
        $itemStmt->bind_param("i", $oid);
        $itemStmt->execute();
        $itemResult = $itemStmt->get_result();

        while ($itemRow = $itemResult->fetch_assoc()) {
            $itemId = $itemRow['item_id'];

            // Get customizations for this item
            $customQuery = $conn->prepare("
                SELECT cog.group_name, 
                       COALESCE(
                           (SELECT GROUP_CONCAT(cov.value_name SEPARATOR ', ') 
                            FROM customization_option_value cov 
                            WHERE FIND_IN_SET(cov.value_id, REPLACE(REPLACE(oic.selected_value_ids, '[', ''), ']', ''))
                           ),
                           oic.text_value
                       ) as value_name
                FROM order_item_customizations oic
                LEFT JOIN customization_option_group cog ON oic.group_id = cog.group_id
                WHERE oic.oid = ? AND oic.item_id = ?
            ");

            $customizations = [];
            if ($customQuery) {
                $customQuery->bind_param("ii", $oid, $itemId);
                $customQuery->execute();
                $customResult = $customQuery->get_result();

                while ($customRow = $customResult->fetch_assoc()) {
                    if ($customRow['group_name']) {
                        $customizations[] = $customRow;
                    }
                }
                $customQuery->close();
            }

            $itemRow['customizations'] = $customizations;
            $items[] = $itemRow;
        }
        $itemStmt->free_result();
        $row['items'] = $items;

        // Get package items for this order
        $packages = [];
        if ($packageStmt) {
            $packageStmt->bind_param("i", $oid);
            $packageStmt->execute();
            $packageResult = $packageStmt->get_result();

            while ($packageRow = $packageResult->fetch_assoc()) {
                $packages[] = $packageRow;
            }
            $packageStmt->free_result();
        }
        $row['packages'] = $packages;

        $orders[] = $row;
    }

    // Close prepared statements
    $itemStmt->close();
    if ($packageStmt) {
        $packageStmt->close();
    }

    // Return JSON response
    echo json_encode([
        'success' => true,
        'orders' => $orders,
        'pendingCount' => $pendingCount
    ]);

} catch (Exception $e) {
    // Error handling
    error_log("Error in get_orders.php: " . $e->getMessage());

    echo json_encode([
        'success' => false,
        'message' => 'Error fetching orders: ' . $e->getMessage()
    ]);
}

// Close database connection
if (isset($conn) && $conn) {
    $conn->close();
}
?>