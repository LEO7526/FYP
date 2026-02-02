<?php
require_once '../conn.php';
header('Content-Type: application/json');

$method = $_SERVER['REQUEST_METHOD'];

try {
    if ($method === 'GET') {
        $action = $_GET['action'] ?? '';

        if ($action === 'get_all_groups') {
            // Fetch all groups
            $sql = "SELECT * FROM customization_option_group ORDER BY group_name";
            $result = mysqli_query($conn, $sql);
            $groups = mysqli_fetch_all($result, MYSQLI_ASSOC);
            echo json_encode(['success' => true, 'data' => $groups]);
        }
        elseif ($action === 'get_group_values') {
            // Fetch values for a specific group (for display purposes)
            $groupId = intval($_GET['group_id']);
            $sql = "SELECT value_name FROM customization_option_value WHERE group_id = ? ORDER BY display_order";
            $stmt = $conn->prepare($sql);
            $stmt->bind_param("i", $groupId);
            $stmt->execute();
            $result = $stmt->get_result();
            $values = [];
            while ($row = $result->fetch_assoc()) {
                $values[] = $row['value_name'];
            }
            echo json_encode(['success' => true, 'data' => implode(', ', $values)]);
        }
        elseif ($action === 'get_unused_groups') {
            // Fetch groups not used in item_customization_options
            $sql = "SELECT g.group_id, g.group_name, g.group_type 
                    FROM customization_option_group g
                    LEFT JOIN item_customization_options ico ON g.group_id = ico.group_id
                    WHERE ico.group_id IS NULL
                    ORDER BY g.group_type";  // 改為按 group_type 排序
            $result = mysqli_query($conn, $sql);
            $data = mysqli_fetch_all($result, MYSQLI_ASSOC);
            echo json_encode(['success' => true, 'data' => $data]);
        }
    }
    elseif ($method === 'POST') {
        $input = json_decode(file_get_contents('php://input'), true);
        $action = $input['action'] ?? '';

        if ($action === 'create_group') {
            // Create new group with values
            $type = trim($input['type']);
            $name = trim($input['name']);
            $values = $input['values']; // Array of strings

            // Validate required fields
            if (empty($type) || empty($name)) {
                echo json_encode(['success' => false, 'message' => 'Type and Name are required']);
                exit;
            }

            // Sanitize type (allow only letters, numbers, underscore, hyphen)
            $type = preg_replace('/[^a-zA-Z0-9_-]/', '', $type);

            mysqli_begin_transaction($conn);

            try {
                $check_sql = "SELECT group_id FROM customization_option_group WHERE group_type = ?";
                $check_stmt = $conn->prepare($check_sql);
                $check_stmt->bind_param("s", $type);
                $check_stmt->execute();
                $check_result = $check_stmt->get_result();

                if ($check_result->num_rows > 0) {
                    throw new Exception('Group with this type already exists');
                }

                // 1. Insert Group
                $stmt = $conn->prepare("INSERT INTO customization_option_group (group_name, group_type) VALUES (?, ?)");
                $stmt->bind_param("ss", $name, $type);
                $stmt->execute();
                $groupId = $conn->insert_id;

                // 2. Insert Values
                if (!empty($values)) {
                    $stmtVal = $conn->prepare("INSERT INTO customization_option_value (group_id, value_name, display_order) VALUES (?, ?, ?)");
                    $order = 1;
                    foreach ($values as $val) {
                        $val = trim($val);
                        if (!empty($val)) {
                            $stmtVal->bind_param("isi", $groupId, $val, $order);
                            $stmtVal->execute();
                            $order++;
                        }
                    }
                    $stmtVal->close();
                }

                mysqli_commit($conn);
                echo json_encode(['success' => true, 'message' => 'Group created successfully', 'group_id' => $groupId]);

            } catch (Exception $e) {
                mysqli_rollback($conn);
                http_response_code(400);
                echo json_encode(['success' => false, 'message' => 'Failed to create group: ' . $e->getMessage()]);
            }
        }
        elseif ($action === 'delete_groups') {
            // Delete selected groups
            $groupIds = $input['group_ids']; // Array of IDs

            if (!empty($groupIds)) {
                $ids = implode(',', array_map('intval', $groupIds));
                // Values delete automatically via Cascade if foreign keys are set up that way,
                // but explicit delete is safer based on provided schema dump which has ON DELETE CASCADE.
                $sql = "DELETE FROM customization_option_group WHERE group_id IN ($ids)";
                if(mysqli_query($conn, $sql)){
                    echo json_encode(['success' => true, 'message' => 'Groups deleted']);
                } else {
                    throw new Exception(mysqli_error($conn));
                }
            } else {
                echo json_encode(['success' => false, 'message' => 'No groups selected']);
            }
        }
    }
} catch (Exception $e) {
    if (isset($conn) && $conn->in_transaction) mysqli_rollback($conn);
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
?>