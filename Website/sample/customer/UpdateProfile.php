<?php
require_once '../auth_check.php';
check_customer_auth();

include '../conn.php';

// Get current logged-in customer details
$cid = $_SESSION['customer']['cid'];
$sql = "SELECT ctel, caddr FROM customer WHERE cid = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $cid);
$stmt->execute();
$result = $stmt->get_result();
$customer = $result->fetch_assoc();
$stmt->close();
$conn->close();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Update Profile | Smile & Sunshine Toy</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/form.css">
    <link rel="stylesheet" href="../CSS/profile.css">
    <script>
        function validateForm() {
            // Password validation (only if changing password)
            const currentPass = document.getElementById('currentPassword').value;
            const newPass = document.getElementById('newPassword').value;
            const confirmPass = document.getElementById('confirmPassword').value;

            if (currentPass || newPass || confirmPass) {
                if (!currentPass) {
                    alert('Please enter your current password');
                    return false;
                }
                if (newPass.length < 8) {
                    alert('Password must be at least 8 characters');
                    return false;
                }
                if (newPass !== confirmPass) {
                    alert('New passwords do not match');
                    return false;
                }
            }

            // Phone number validation
            const phone = document.getElementById('phoneNumber').value;
            if (!/^[\d\s\+\-]+$/.test(phone)) {
                alert('Please enter a valid phone number');
                return false;
            }

            // Address validation
            const address = document.getElementById('deliveryAddress').value.trim();
            if (address.length < 10) {
                alert('Please enter a complete delivery address');
                return false;
            }

            return true;
        }

        function checkPasswordStrength() {
            const password = document.getElementById('newPassword').value;
            const strengthBar = document.getElementById('passwordStrength');
            let strength = 0;

            if (password.length >= 8) strength += 1;
            if (password.length >= 12) strength += 1;
            if (/\d/.test(password)) strength += 1;
            if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) strength += 1;

            strengthBar.style.width = (strength * 25) + '%';
            strengthBar.style.backgroundColor =
                strength < 2 ? 'red' :
                    strength < 4 ? 'orange' : 'green';
        }

        function submitForm() {
            if (!validateForm()) return false;

            const formData = new FormData(document.getElementById('updateForm'));

            fetch('updateProfileProcess.php', {
                method: 'POST',
                body: formData
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('Profile updated successfully!');
                        window.location.href = 'Profile.php';
                    } else {
                        alert('Error: ' + data.message);
                    }
                })
                .catch(error => {
                    alert('Error submitting form: ' + error.message);
                });

            return false;
        }
    </script>
</head>
<body>
<header>
    <div class="logo">
        <a href="Index.php">Smile &amp; Sunshine Toy</a>
    </div>
    <div class="user-actions">
        <a href="Index.php">Home</a>
        <a href="MyOrder.php">My Orders</a>
        <a href="Profile.php">My Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>

<div class="container">
    <h1>Update Profile</h1>

    <form id="updateForm" onsubmit="return submitForm()">
        <input type="hidden" name="cid" value="<?= htmlspecialchars($cid) ?>">

        <div class="form-section">
            <h3>Security Settings</h3>
            <div class="form-group">
                <label class="form-label">Current Password (required to change password)</label>
                <input type="password" id="currentPassword" name="currentPassword">
            </div>

            <div class="form-group">
                <label class="form-label">New Password</label>
                <input type="password" id="newPassword" name="newPassword" oninput="checkPasswordStrength()">
                <div class="password-strength">
                    <div class="password-strength-bar" id="passwordStrength"></div>
                </div>
                <small class="hint">Minimum 8 characters with numbers and special characters</small>
            </div>

            <div class="form-group">
                <label class="form-label">Confirm New Password</label>
                <input type="password" id="confirmPassword" name="confirmPassword">
            </div>
        </div>

        <div class="form-section">
            <h3>Contact Information</h3>
            <div class="form-group">
                <label class="form-label">Phone Number</label>
                <input type="tel" id="phoneNumber" name="phoneNumber"
                       value="<?= htmlspecialchars(isset($customer['ctel']) ? $customer['ctel'] : '') ?>" required>
            </div>

            <div class="form-group">
                <label class="form-label">Delivery Address</label>
                <textarea id="deliveryAddress" name="deliveryAddress" rows="3" required><?=
                    htmlspecialchars(isset($customer['caddr']) ? $customer['caddr'] : '') ?></textarea>
            </div>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn-submit">Save Changes</button>
            <a href="Profile.php" class="btn-reset">Cancel</a>
        </div>
    </form>
</div>
</body>
</html>