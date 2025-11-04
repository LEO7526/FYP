<?php
require_once '../auth_check.php';
check_staff_auth(); // Check staff authentication status
include '../conn.php'; // Database connection

// Fetch categories and menu items
$categories = [];
$menuItems = [];

$categoryQuery = "SELECT * FROM menu_category";
$categoryResult = mysqli_query($conn, $categoryQuery);
while ($row = mysqli_fetch_assoc($categoryResult)) {
    $categories[] = $row;
}

// Modified query to include price information
$menuQuery = "SELECT mi.item_id, mi.image_url, mit.item_name, mc.category_name, mi.item_price 
              FROM menu_item mi 
              JOIN menu_item_translation mit ON mi.item_id = mit.item_id 
              JOIN menu_category mc ON mi.category_id = mc.category_id 
              WHERE mit.language_code = 'en' AND mi.is_available = TRUE";
$menuResult = mysqli_query($conn, $menuQuery);
while ($row = mysqli_fetch_assoc($menuResult)) {
    $menuItems[$row['category_name']][] = $row;
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create New Package - Yummy Restaurant</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/newPackage.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>

<body>
<header>
    <div class="hamburger-menu" id="hamburgerMenu">
        <span></span>
        <span></span>
        <span></span>
    </div>
    <div class="logo">
        <a href="staffIndex.php">Yummy Restaurant</a>
    </div>
    <nav class="main-nav">
        <a href="MenuManagement.html" class="nav-button insert-items">Menu Management</a>
        <a href="Inventory.php" class="nav-button insert-materials">Inventory</a>
        <a href="bookingList.php" class="nav-button order-list">Reservations</a>
        <a href="SalesReport.php" class="nav-button report">Sales Reports</a>
        <a href="PurchaseReturn.php" class="nav-button delete">Purchase & Return</a>
    </nav>

    <div class="user-actions">
        <a href="staffProfile.php" class="profile-btn">Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>

<main class="package-container">
    <h1>Create New Set</h1>

    <form id="packageForm" class="package-form">
        <div class="package-basic-info">
            <div class="form-group package-name">
                <label for="packageName">Set Name:</label>
                <input type="text" id="packageName" name="packageName" required>
            </div>
        </div>

        <div id="typesContainer" class="types-container">
            <!-- Types will be added here dynamically -->
        </div>

        <button type="button" id="addTypeBtn" class="btn-secondary">+ Add New Type</button>

        <div class="form-bottom-section">
            <div class="pricing-fields">
                <div class="form-group preset-price">
                    <label>Preset Price:</label>
                    <span id="presetPriceDisplay">0</span>
                </div>

                <div class="form-group amounts">
                    <label for="amounts">Amount (HKD):</label>
                    <input type="number" id="amounts" name="amounts" min="0" step="0.01" value="0" required class="fixed-input">
                </div>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn-primary">Create Set</button>
            </div>
        </div>
    </form>
</main>

<!-- Dish Selection Modal -->
<div id="dishModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h2>Select Dishes</h2>
            <span class="close">&times;</span>
        </div>
        <div class="modal-body">
            <div class="category-tabs">
                <?php foreach ($categories as $category): ?>
                    <button class="tab-btn" data-category="<?php echo htmlspecialchars($category['category_name']); ?>">
                        <?php echo htmlspecialchars($category['category_name']); ?>
                    </button>
                <?php endforeach; ?>
            </div>

            <div class="dishes-grid">
                <?php foreach ($menuItems as $categoryName => $items): ?>
                    <div class="dish-category" id="category-<?php echo htmlspecialchars(str_replace(' ', '-', $categoryName)); ?>"
                         style="<?php echo $categoryName !== array_key_first($menuItems) ? 'display: none;' : ''; ?>">
                        <?php foreach ($items as $item): ?>
                            <div class="dish-item"
                                 data-item-id="<?php echo $item['item_id']; ?>"
                                 data-item-price="<?php echo $item['item_price']; ?>">
                                <img src="<?php echo htmlspecialchars($item['image_url']); ?>"
                                     alt="<?php echo htmlspecialchars($item['item_name']); ?>">
                                <span><?php echo htmlspecialchars($item['item_name']); ?></span>
                            </div>
                        <?php endforeach; ?>
                    </div>
                <?php endforeach; ?>
            </div>
        </div>
    </div>
</div>

<script>
    let currentTypeIndex = 0;

    // Initialize with 2 default types
    document.addEventListener('DOMContentLoaded', function() {
        addType();
        addType();

        // Initialize preset price display
        updatePresetPrice();
    });

    // Add new type
    document.getElementById('addTypeBtn').addEventListener('click', addType);

    function addType() {
        const typesContainer = document.getElementById('typesContainer');
        const typeIndex = currentTypeIndex++;

        const typeDiv = document.createElement('div');
        typeDiv.className = 'type-section';
        typeDiv.innerHTML = `
        <div class="type-header">
            <div class="optional-quantity-group">
                <label>Optional Quantity:</label>
                <input type="number" name="types[${typeIndex}][optional_quantity]" min="1" value="1" required class="fixed-input">
            </div>
            <button type="button" class="btn-remove-type" onclick="removeType(this)">Remove</button>
        </div>

        <div class="type-languages">
            <div class="form-group small-input">
                <label>Type Name (English):</label>
                <input type="text" name="types[${typeIndex}][name_en]" required class="fixed-input">
            </div>

            <div class="form-group small-input">
                <label>Type Name (Chinese Simplified):</label>
                <input type="text" name="types[${typeIndex}][name_zh_cn]" required class="fixed-input">
            </div>

            <div class="form-group small-input">
                <label>Type Name (Chinese Traditional):</label>
                <input type="text" name="types[${typeIndex}][name_zh_tw]" required class="fixed-input">
            </div>
        </div>

        <div class="selected-dishes">
            <h4>Selected Dishes</h4>
            <button type="button" class="btn-add-dish" onclick="openDishModal(${typeIndex})">+ Add Dishes</button>
            <div class="dishes-list" id="dishes-list-${typeIndex}"></div>
        </div>
    `;

        typesContainer.appendChild(typeDiv);
    }

    function removeType(button) {
        const typeSection = button.closest('.type-section');
        typeSection.remove();
        // Re-index remaining types
        const types = document.querySelectorAll('.type-section');
        types.forEach((type, index) => {
            // Update the optional quantity group label if needed
            const optionalLabel = type.querySelector('.optional-quantity-group label');
            if (optionalLabel) {
                optionalLabel.textContent = 'Optional Quantity:';
            }
        });
        updatePresetPrice();
    }

    // Modal functionality
    let currentModalTypeIndex = null;

    function openDishModal(typeIndex) {
        currentModalTypeIndex = typeIndex;
        document.getElementById('dishModal').style.display = 'block';
        // Show first category by default
        const firstTab = document.querySelector('.tab-btn');
        if (firstTab) {
            showCategory(firstTab);
        }
    }

    function closeModal() {
        document.getElementById('dishModal').style.display = 'none';
        currentModalTypeIndex = null;
    }

    // Category tab switching
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            showCategory(this);
        });
    });

    function showCategory(button) {
        // Remove active class from all tabs
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
        });

        // Add active class to clicked tab
        button.classList.add('active');

        // Hide all categories
        document.querySelectorAll('.dish-category').forEach(category => {
            category.style.display = 'none';
        });

        // Show selected category
        const categoryName = button.getAttribute('data-category');
        const categoryId = 'category-' + categoryName.replace(' ', '-');
        const categoryElement = document.getElementById(categoryId);
        if (categoryElement) {
            categoryElement.style.display = 'grid';
        }
    }

    // Dish selection
    document.querySelectorAll('.dish-item').forEach(item => {
        item.addEventListener('click', function() {
            if (currentModalTypeIndex === null) return;

            const itemId = this.getAttribute('data-item-id');
            const itemName = this.querySelector('span').textContent;
            const itemImage = this.querySelector('img').src;
            const itemPrice = parseFloat(this.getAttribute('data-item-price'));

            addDishToType(currentModalTypeIndex, itemId, itemName, itemImage, itemPrice);
        });
    });

    function addDishToType(typeIndex, itemId, itemName, itemImage, itemPrice) {
        const dishesList = document.getElementById(`dishes-list-${typeIndex}`);

        // Check if dish already exists
        const existingDish = dishesList.querySelector(`[data-item-id="${itemId}"]`);
        if (existingDish) return;

        const dishDiv = document.createElement('div');
        dishDiv.className = 'selected-dish-item';
        dishDiv.setAttribute('data-item-id', itemId);
        dishDiv.setAttribute('data-item-price', itemPrice);

        dishDiv.innerHTML = `
        <img src="${itemImage}" alt="${itemName}">
        <span>${itemName}</span>
        <div class="price-modifier-group">
            <input type="number" class="price-modifier-input" value="0" step="0.01" min="0" readonly>
            <button type="button" class="btn-remove-dish" onclick="removeDish(this)">Remove</button>
        </div>
    `;

        dishesList.appendChild(dishDiv);

        // Update all dish price modifiers in this type
        updateTypePriceModifiers(typeIndex);
        updatePresetPrice();
    }

    // Function: Update price modifiers for all dishes in a specific type
    function updateTypePriceModifiers(typeIndex) {
        const dishesList = document.getElementById(`dishes-list-${typeIndex}`);
        const dishItems = dishesList.querySelectorAll('.selected-dish-item');

        if (dishItems.length === 0) return;

        // Find the minimum price in this type
        let minPrice = Infinity;
        dishItems.forEach(dishItem => {
            const price = parseFloat(dishItem.getAttribute('data-item-price'));
            if (price < minPrice) {
                minPrice = price;
            }
        });

        // Update price modifiers for all dishes
        dishItems.forEach(dishItem => {
            const price = parseFloat(dishItem.getAttribute('data-item-price'));
            const priceInput = dishItem.querySelector('.price-modifier-input');

            // Check if this dish has the minimum price
            if (price === minPrice) {
                // All dishes with minimum price: set to 0 and lock
                priceInput.value = 0;
                priceInput.readOnly = true;
                priceInput.style.backgroundColor = '#f0f0f0';
            } else {
                // Other dishes: set to price difference and allow editing
                const priceDifference = price - minPrice;
                priceInput.value = priceDifference.toFixed(2);
                priceInput.readOnly = false;
                priceInput.style.backgroundColor = '';

                // Add event listener to update preset price when user modifies price
                priceInput.oninput = updatePresetPrice;
            }
        });

        // Sort by price: lowest price on the left
        const sortedDishes = Array.from(dishItems).sort((a, b) => {
            const priceA = parseFloat(a.getAttribute('data-item-price'));
            const priceB = parseFloat(b.getAttribute('data-item-price'));
            return priceA - priceB;
        });

        // Clear and re-add in sorted order
        while (dishesList.firstChild) {
            dishesList.removeChild(dishesList.firstChild);
        }
        sortedDishes.forEach(dish => dishesList.appendChild(dish));
    }

    function removeDish(button) {
        const dishItem = button.closest('.selected-dish-item');
        const dishesList = dishItem.closest('.dishes-list');
        const typeIndex = dishesList.id.split('-')[2];

        dishItem.remove();

        // Update price modifiers for this type
        updateTypePriceModifiers(typeIndex);
        updatePresetPrice();
    }

    function updatePresetPrice() {
        let totalPrice = 0;

        // Calculate the sum of the lowest price dish in each type
        document.querySelectorAll('.type-section').forEach(typeSection => {
            const dishesList = typeSection.querySelector('.dishes-list');
            if (!dishesList) return;

            const dishItems = dishesList.querySelectorAll('.selected-dish-item');
            if (dishItems.length === 0) return;

            // Find the lowest price dish in this type
            let minPrice = Infinity;
            dishItems.forEach(dishItem => {
                const price = parseFloat(dishItem.getAttribute('data-item-price'));
                if (price < minPrice) {
                    minPrice = price;
                }
            });

            totalPrice += minPrice;
        });

        // Update preset price display
        document.getElementById('presetPriceDisplay').textContent = totalPrice.toFixed(2);

        // Note: Amount (HKD) field is not automatically updated
        // to allow manual control of the final package price
    }

    // Close modal when clicking X
    document.querySelector('.close').addEventListener('click', closeModal);

    // Close modal when clicking outside
    window.addEventListener('click', function(event) {
        const modal = document.getElementById('dishModal');
        if (event.target === modal) {
            closeModal();
        }
    });

    // Form submission
    document.getElementById('packageForm').addEventListener('submit', function(e) {
        e.preventDefault();

        const formData = new FormData(this);
        const types = [];

        // Collect type data
        document.querySelectorAll('.type-section').forEach((typeSection, index) => {
            const nameEnInput = typeSection.querySelector(`input[name="types[${index}][name_en]"]`);
            const nameZhCnInput = typeSection.querySelector(`input[name="types[${index}][name_zh_cn]"]`);
            const nameZhTwInput = typeSection.querySelector(`input[name="types[${index}][name_zh_tw]"]`);
            const optionalQuantityInput = typeSection.querySelector(`input[name="types[${index}][optional_quantity]"]`);

            // Check if inputs exist (in case of dynamic removal)
            if (!nameEnInput || !nameZhCnInput || !nameZhTwInput || !optionalQuantityInput) return;

            const typeData = {
                name_en: nameEnInput.value,
                name_zh_cn: nameZhCnInput.value,
                name_zh_tw: nameZhTwInput.value,
                optional_quantity: optionalQuantityInput.value,
                dishes: []
            };

            // Collect dish IDs and price modifiers
            const dishItems = typeSection.querySelectorAll('.selected-dish-item');
            dishItems.forEach(dishItem => {
                const itemId = dishItem.getAttribute('data-item-id');
                const priceModifierInput = dishItem.querySelector('.price-modifier-input');
                const priceModifier = priceModifierInput ? parseFloat(priceModifierInput.value) : 0;

                typeData.dishes.push({
                    item_id: itemId,
                    price_modifier: priceModifier
                });
            });

            types.push(typeData);
        });

        const packageData = {
            packageName: document.getElementById('packageName').value,
            amounts: document.getElementById('amounts').value,
            types: types
        };

        // Validate at least one type exists
        if (types.length === 0) {
            alert('Please add at least one type to the package.');
            return;
        }

        // Validate package name
        if (!packageData.packageName.trim()) {
            alert('Please enter a package name.');
            return;
        }

        // Validate amounts
        if (!packageData.amounts || parseFloat(packageData.amounts) <= 0) {
            alert('Please enter a valid amount greater than 0.');
            return;
        }

        // Send data to server
        $.ajax({
            url: 'save_package.php',
            type: 'POST',
            data: JSON.stringify(packageData),
            contentType: 'application/json',
            success: function(response) {
                const result = JSON.parse(response);
                if (result.success) {
                    alert('Package created successfully!');
                    location.reload();
                } else {
                    alert('Error creating package: ' + result.message);
                }
            },
            error: function(xhr, status, error) {
                alert('Error creating package: ' + error);
            }
        });
    });

    // Header menu functionality
    document.addEventListener('DOMContentLoaded', function() {
        const hamburger = document.getElementById('hamburgerMenu');
        const mainNav = document.querySelector('.main-nav');
        const userActions = document.querySelector('.user-actions');

        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            mainNav.classList.toggle('active');
            document.body.style.overflow = mainNav.classList.contains('active') ? 'hidden' : '';
        });

        document.querySelectorAll('.nav-button').forEach(button => {
            button.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        document.querySelectorAll('.user-actions a').forEach(link => {
            link.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        document.addEventListener('click', function(e) {
            if (!hamburger.contains(e.target) && !mainNav.contains(e.target) && !userActions.contains(e.target)) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && mainNav.classList.contains('active')) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });
    });
</script>
</body>
</html>