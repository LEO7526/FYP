<?php
require_once '../auth_check.php';
check_staff_auth(); // Verify staff authentication status

include '../conn.php'; // Database connection

// Retrieve materials from database
$materials = [];
$sql = "SELECT mid, mname FROM material";
$result = mysqli_query($conn, $sql);
while ($row = mysqli_fetch_assoc($result)) {
    $materials[] = $row;
}
mysqli_close($conn);
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Insert Dish</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/form.css">
    <link rel="stylesheet" href="../CSS/insertItems.css"> <!-- Custom CSS for this page -->
</head>
<body>
<header>
    <div class="logo">
        <a href="staffIndex.php">Smile & Sunshine Toy</a>
    </div>

    <nav class="main-nav">
        <a href="InsertItems.php" class="nav-button insert-items">Insert Items</a>
        <a href="InsertMaterials.php" class="nav-button insert-materials">Insert Materials</a>
        <a href="bookingList.php" class="nav-button order-list">Order List</a>
        <a href="Report.php" class="nav-button report">Report</a>
        <a href="Delete.php" class="nav-button delete">Delete</a>
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
    <h1>Insert Dish</h1>
    <!-- The form uses multipart/form-data to support file uploads -->
    <form id="productForm" method="post" action="insertItemsProcess.php" enctype="multipart/form-data">
        <div class="form-group">
            <h2 class="form-label">Dish Name</h2>
            <input type="text" id="productName" name="productName" required>
        </div>
        <div class="form-group">
            <h2 class="form-label">Dish Description</h2>
            <textarea id="productDescription" name="productDescription" required></textarea>
        </div>
        <div class="form-group">
            <h2 class="form-label">Dish Cost (HKD)</h2>
            <input type="number" id="productCost" name="productCost" step="0.01" required value="0" min="0">
        </div>

        <div class="form-group">
            <h2 class="form-label">Materials</h2>
            <div class="material-controls">
                <select id="materialID" required>
                    <?php foreach ($materials as $material): ?>
                        <option value="<?php echo $material['mid']; ?>">
                            <?php echo htmlspecialchars($material['mname'] . " (ID: " . $material['mid'] . ")"); ?>
                        </option>
                    <?php endforeach; ?>
                </select>
                <button type="button" id="btn-add-material" class="btn-add-material">Add</button>
            </div>

            <!-- Initially hidden materials container -->
            <div class="selected-materials" id="selectedMaterialsWrapper" style="display: none;">
                <div class="materials-container" id="selectedMaterialsContainer">
                    <!-- Dynamically added material items will appear here -->
                </div>
            </div>
            <!-- Error message container for materials -->
            <div id="materialError" class="error-message" style="display: none; margin-top: 10px;"></div>
        </div>

        <!-- Product image upload (optional) -->
        <div class="form-group">
            <h2 class="form-label">Dish Image</h2>
            <input type="file" id="productImage" name="productImage" accept="image/*" onchange="previewProductImage(event)">
            <!-- 添加图片预览容器 -->
            <div id="productImagePreviewContainer" style="display: none; margin-top: 10px;">
                <img id="productImagePreview" alt="Product preview" style="max-width: 200px;">
            </div>
            <p class="form-note">Optional: Upload a product image (JPG, PNG, GIF)</p>
        </div>

        <input type="submit" class="btn-submit" value="Add Dish">
        <a href="InsertItems.php" class="btn-reset">Reset</a>
    </form>
</div>

<script>
    // Convert PHP materials array to JavaScript array
    const materials = <?php echo json_encode($materials); ?>;

    // Create a mapping from material ID to material name
    const materialNames = {};
    materials.forEach(material => {
        materialNames[material['mid']] = material['mname'];
    });

    // Array to keep track of selected material IDs to avoid duplicates
    const selectedMaterials = [];
    const materialsWrapper = document.getElementById('selectedMaterialsWrapper');

    // Add material button click event handler
    document.getElementById('btn-add-material').addEventListener('click', function() {
        const materialID = document.getElementById('materialID').value;

        // Check if the material is already added
        if (selectedMaterials.includes(materialID)) {
            alert('This material has already been added.');
            return;
        }

        // Add the material ID to the selected array
        selectedMaterials.push(materialID);

        // Show the materials container if it was hidden
        if (selectedMaterials.length === 1) {
            materialsWrapper.style.display = 'block';
        }

        // Create a new material item element
        const materialItem = document.createElement('div');
        materialItem.className = 'material-item';
        materialItem.dataset.mid = materialID; // Store material ID in data attribute

        // Get material name
        const materialName = materialNames[materialID] || 'Material';

        // MODIFIED: Set the path for material images
        // Images are stored in: ../Sample Images/material/
        // File naming convention: [materialID].jpg (e.g., 1.jpg, 2.jpg)
        const materialImagePath = `../Sample Images/material/${materialID}.jpg`;

        // Set inner HTML for the material item
        materialItem.innerHTML = `
            <button type="button" class="btn-remove" onclick="removeMaterial(this)">×</button>
            <!-- MODIFIED: Replaced numeric placeholder with actual image -->
            <img class="material-image" src="${materialImagePath}" alt="${materialName}">
            <p class="material-text">${materialName}</p>
            <input type="number" name="materials[${materialID}][qty]"
                   class="material-quantity" required value="0" min="0"
                   placeholder="Quantity">
            <input type="hidden" name="materials[${materialID}][mid]" value="${materialID}">
        `;

        // Append the new material item to the container
        document.getElementById('selectedMaterialsContainer').appendChild(materialItem);
    });

    // Function to remove a material item
    function removeMaterial(button) {
        const materialItem = button.parentElement;
        const materialID = materialItem.dataset.mid;

        // Remove the material ID from the selected array
        const index = selectedMaterials.indexOf(materialID);
        if (index !== -1) {
            selectedMaterials.splice(index, 1);
        }

        // Remove the material item from the DOM
        materialItem.remove();

        // Hide the materials container if no materials left
        if (selectedMaterials.length === 0) {
            materialsWrapper.style.display = 'none';
        }
    }

    // 产品图片预览功能
    function previewProductImage(event) {
        const previewContainer = document.getElementById('productImagePreviewContainer');
        const preview = document.getElementById('productImagePreview');
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

    // Form validation before submission
    document.getElementById('productForm').addEventListener('submit', function(e) {
        const materialError = document.getElementById('materialError');
        materialError.style.display = 'none';

        // Check if at least one material is added
        if (selectedMaterials.length === 0) {
            e.preventDefault();
            materialError.textContent = 'Please add at least one material.';
            materialError.style.display = 'block';
            return false;
        }

        // Check if all material quantities are greater than 0
        const quantityInputs = document.querySelectorAll('.material-quantity');
        let allValid = true;
        let hasZero = false;

        quantityInputs.forEach(input => {
            const qty = parseInt(input.value);
            if (qty <= 0) {
                allValid = false;
                hasZero = true;
                input.style.borderColor = 'red'; // Highlight invalid fields
            } else {
                input.style.borderColor = ''; // Reset style
            }
        });

        if (!allValid) {
            e.preventDefault();
            if (hasZero) {
                materialError.textContent = 'All materials must have a quantity greater than 0.';
                materialError.style.display = 'block';
            }
            return false;
        }

        return true;
    });
</script>
</body>
</html>