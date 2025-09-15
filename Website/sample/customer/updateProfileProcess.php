<?php
require_once '../auth_check.php';
check_customer_auth();
include '../conn.php';

header('Content-Type: application/json');

$cid = $_SESSION['customer']['cid'];
$currentPassword = isset($_POST['currentPassword']) ? trim($_POST['currentPassword']) : '';
$newPassword = isset($_POST['newPassword']) ? trim($_POST['newPassword']) : '';
$phoneNumber = isset($_POST['phoneNumber']) ? trim($_POST['phoneNumber']) : '';
$deliveryAddress = isset($_POST['deliveryAddress']) ? trim($_POST['deliveryAddress']) : '';

if (empty($phoneNumber) || empty($deliveryAddress)) {
    echo json_encode(['success' => false, 'message' => 'Phone number and delivery address are required']);
    exit();
}

$conn->begin_transaction();

try {
    $stmt = $conn->prepare("SELECT cpassword FROM customer WHERE cid = ?");
    $stmt->bind_param("i", $cid);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        throw new Exception("Customer not found");
    }

    $customer = $result->fetch_assoc();
    $storedPassword = $customer['cpassword']; // 密码以明文形式存储

    if (!empty($currentPassword) || !empty($newPassword)) {
        if (empty($currentPassword) || empty($newPassword)) {
            throw new Exception("Both current and new passwords are required to change password");
        }

        // 明文密码验证
        if ($currentPassword !== $storedPassword) {
            throw new Exception("Current password is incorrect");
        }

        // 更新为新的明文密码
        $updateStmt = $conn->prepare("UPDATE customer SET cpassword = ?, ctel = ?, caddr = ? WHERE cid = ?");
        $updateStmt->bind_param("sssi", $newPassword, $phoneNumber, $deliveryAddress, $cid);
        $updateStmt->execute();
        $updateStmt->close();
    } else {
        $updateStmt = $conn->prepare("UPDATE customer SET ctel = ?, caddr = ? WHERE cid = ?");
        $updateStmt->bind_param("ssi", $phoneNumber, $deliveryAddress, $cid);
        $updateStmt->execute();
        $updateStmt->close();
    }

    $conn->commit();
    echo json_encode(['success' => true, 'message' => 'Profile updated successfully']);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
} finally {
    $conn->close();
}