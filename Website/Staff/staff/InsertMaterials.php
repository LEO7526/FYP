<?php
require_once '../auth_check.php';
check_staff_auth();// Verify staff authentication status

?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Insert Materials</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/form.css">
    <script>
        // Function to update max values based on physical quantity
        function updateMaxValues() {
            const physicalQty = parseInt(document.getElementById('physicalQuantity').value) || 0;

            // Update reorder quantity max value
            const reorderInput = document.getElementById('reorderQuantity');
            reorderInput.max = physicalQty;
            validateReorderQuantity();
        }

        // Function to validate reorder quantity
        function validateReorderQuantity() {
            const reorderInput = document.getElementById('reorderQuantity');
            const physicalQty = parseInt(document.getElementById('physicalQuantity').value) || 0;
            const reorderQty = parseInt(reorderInput.value) || 0;

            if (reorderQty > physicalQty) {
                document.getElementById('reorderError').style.display = 'block';
                reorderInput.value = physicalQty;
            } else {
                document.getElementById('reorderError').style.display = 'none';
            }
        }

        // Function to validate entire form
        function validateForm() {
            const physicalQty = parseInt(document.getElementById('physicalQuantity').value) || 0;
            let isValid = true;

            // Validate reorder quantity
            const reorderInput = document.getElementById('reorderQuantity');
            const reorderQty = parseInt(reorderInput.value) || 0;
            if (reorderQty > physicalQty) {
                document.getElementById('reorderError').style.display = 'block';
                isValid = false;
            }

            return isValid;
        }

        // Function to preview uploaded image
        function previewImage(event) {
            const previewContainer = document.getElementById('imagePreviewContainer');
            const preview = document.getElementById('imagePreview');
            const file = event.target.files[0];

            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    preview.src = e.target.result;
                    previewContainer.style.display = 'block';
                }
                reader.readAsDataURL(file);
            } else {
                previewContainer.style.display = 'none';
            }
        }

        // Initialize validation and preview on page load
        document.addEventListener('DOMContentLoaded', function() {
            // Add validation to physical quantity
            document.getElementById('physicalQuantity').addEventListener('input', function() {
                updateMaxValues();
            });

            // Add validation to reorder quantity
            document.getElementById('reorderQuantity').addEventListener('input', function() {
                validateReorderQuantity();
            });

            // Add image preview functionality
            document.getElementById('materialImage').addEventListener('change', previewImage);
        });
    </script>
</head>
<body>
<header>
    <div class="logo">
        <a href="staffIndex.php">Yummy Restaurant</a>
    </div>

    <nav class="main-nav">
        <a href="MenuManagement.php" class="nav-button insert-items">Menu Management</a>
        <a href="Inventory.php" class="nav-button insert-materials">Inventory</a>
        <a href="ReservationList.php" class="nav-button order-list">Reservations</a>
        <a href="SalesReport.php" class="nav-button report">Sales Reports</a>
        <a href="PurchaseReturn.php" class="nav-button delete">Purchase & Return</a>
    </nav>

    <div class="user-actions">
        <a href="staffProfile.php">Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>
<div class="back-link">
    <a href="staffIndex.php" class="back-link-text"> &#60; Back</a>
</div>
<div class="container">
    <h1>Insert Materials</h1>
    <!-- Add enctype="multipart/form-data" to support file uploads -->
    <form id="materialForm" method="post" action="insertMaterialsProcess.php" onsubmit="return validateForm()" enctype="multipart/form-data">
        <div class="form-group">
            <h2 class="form-label">Material Name</h2>
            <input type="text" id="materialName" name="materialName" required>
        </div>

        <div class="form-group">
            <h2 class="form-label">Unit</h2>
            <input type="text" id="unit" name="unit" placeholder="e.g., KG, PC" required>
        </div>

        <div class="form-group">
            <h2 class="form-label">Physical Quantity</h2>
            <input type="number" id="physicalQuantity" name="physicalQuantity" required value="0" min="0">
        </div>

        <div id="reorderGroup" class="form-group">
            <h2 class="form-label">Re-order Quantity</h2>
            <input type="number" id="reorderQuantity" name="reorderQuantity" required value="0" min="0">
            <p class="error-message" id="reorderError" style="display:none;color:red;">
                Re-order quantity cannot exceed physical quantity
            </p>
        </div>

        <!-- Add image upload field -->
        <div class="form-group">
            <h2 class="form-label">Material Image (Optional)</h2>
            <input type="file" id="materialImage" name="materialImage" accept="image/*" onchange="previewImage(event)">
            <!-- 修改为带容器的预览 -->
            <div id="imagePreviewContainer" style="display: none; margin-top: 10px;">
                <img id="imagePreview" alt="Material preview" style="max-width: 200px;">
            </div>
        </div>

        <input type="submit" class="btn-submit" value="Add Material">
        <a href="InsertMaterials.php" class="btn-reset">Reset</a>
    </form>
</div>
</body>
</html>