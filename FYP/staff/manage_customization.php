<?php
require_once '../conn.php';
header('Content-Type: application/json');

$method = $_SERVER['REQUEST_METHOD'];

try {
    if ($method === 'GET') {
        $action = $_GET['action'] ?? '';

        if ($action === 'get_all_groups') {
            $sql = "SELECT group_id, group_name, group_type FROM customization_option_group ORDER BY group_name";
            $result = mysqli_query($conn, $sql);
            $groups = mysqli_fetch_all($result, MYSQLI_ASSOC);
            echo json_encode(['success' => true, 'data' => $groups]);
        }
        elseif ($action === 'get_group_values') {
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
            $sql = "SELECT g.group_id, g.group_name, g.group_type 
                    FROM customization_option_group g
                    LEFT JOIN item_customization_options ico ON g.group_id = ico.group_id
                    WHERE ico.group_id IS NULL
                    ORDER BY g.group_type";
            $result = mysqli_query($conn, $sql);
            $data = mysqli_fetch_all($result, MYSQLI_ASSOC);
            echo json_encode(['success' => true, 'data' => $data]);
        }
    }
    elseif ($method === 'POST') {
        $input = json_decode(file_get_contents('php://input'), true);
        $action = $input['action'] ?? '';

        if ($action === 'create_group') {
            // 接收三语言 group name
            $group_name_en   = trim($input['group_name_en'] ?? '');
            $group_name_zh_cn = trim($input['group_name_zh_cn'] ?? '');
            $group_name_zh_tw = trim($input['group_name_zh_tw'] ?? '');
            $type = trim($input['type'] ?? '');
            $values = $input['values'] ?? []; // 每个元素包含 en, zh_cn, zh_tw

            // 验证必填
            if (empty($group_name_en) || empty($group_name_zh_cn) || empty($group_name_zh_tw)) {
                echo json_encode(['success' => false, 'message' => 'Group name in all three languages is required']);
                exit;
            }
            if (empty($type)) {
                echo json_encode(['success' => false, 'message' => 'Group type is required']);
                exit;
            }
            if (empty($values)) {
                echo json_encode(['success' => false, 'message' => 'At least one value is required']);
                exit;
            }

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
                $check_stmt->close();

                $stmt = $conn->prepare("INSERT INTO customization_option_group (group_name, group_type) VALUES (?, ?)");
                $stmt->bind_param("ss", $group_name_en, $type);
                $stmt->execute();
                $groupId = $conn->insert_id;
                $stmt->close();

                $stmt_tw = $conn->prepare("INSERT INTO customization_option_group_translation (group_id, language_code, group_name) VALUES (?, 'zh-TW', ?)");
                $stmt_tw->bind_param("is", $groupId, $group_name_zh_tw);
                $stmt_tw->execute();
                $stmt_tw->close();

                $stmt_cn = $conn->prepare("INSERT INTO customization_option_group_translation (group_id, language_code, group_name) VALUES (?, 'zh-CN', ?)");
                $stmt_cn->bind_param("is", $groupId, $group_name_zh_cn);
                $stmt_cn->execute();
                $stmt_cn->close();

                $stmt_val = $conn->prepare("INSERT INTO customization_option_value (group_id, value_name, display_order) VALUES (?, ?, ?)");
                $stmt_val_tw = $conn->prepare("INSERT INTO customization_option_value_translation (value_id, language_code, value_name) VALUES (?, 'zh-TW', ?)");
                $stmt_val_cn = $conn->prepare("INSERT INTO customization_option_value_translation (value_id, language_code, value_name) VALUES (?, 'zh-CN', ?)");

                $order = 1;
                foreach ($values as $val) {
                    $val_en = trim($val['en'] ?? '');
                    $val_zh_cn = trim($val['zh_cn'] ?? '');
                    $val_zh_tw = trim($val['zh_tw'] ?? '');
                    if (empty($val_en) || empty($val_zh_cn) || empty($val_zh_tw)) {
                        throw new Exception('Each value must have all three language names');
                    }

                    // 插入 value 主表（英文）
                    $stmt_val->bind_param("isi", $groupId, $val_en, $order);
                    $stmt_val->execute();
                    $valueId = $conn->insert_id;

                    // 插入繁体翻译
                    $stmt_val_tw->bind_param("is", $valueId, $val_zh_tw);
                    $stmt_val_tw->execute();

                    // 插入简体翻译
                    $stmt_val_cn->bind_param("is", $valueId, $val_zh_cn);
                    $stmt_val_cn->execute();

                    $order++;
                }
                $stmt_val->close();
                $stmt_val_tw->close();
                $stmt_val_cn->close();

                mysqli_commit($conn);
                echo json_encode(['success' => true, 'message' => 'Group created successfully', 'group_id' => $groupId]);

            } catch (Exception $e) {
                mysqli_rollback($conn);
                http_response_code(400);
                echo json_encode(['success' => false, 'message' => 'Failed to create group: ' . $e->getMessage()]);
            }
        }
        elseif ($action === 'delete_groups') {
            $groupIds = $input['group_ids'] ?? [];
            if (!empty($groupIds)) {
                $ids = implode(',', array_map('intval', $groupIds));
                $sql = "DELETE FROM customization_option_group WHERE group_id IN ($ids)";
                if (mysqli_query($conn, $sql)) {
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