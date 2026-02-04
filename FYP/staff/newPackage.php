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
        <a href="MenuManagement.php" class="nav-button insert-items">Menu Management</a>
        <a href="newInventory.php" class="nav-button insert-materials">Inventory</a>
        <a href="bookingList.php" class="nav-button order-list">Reservations</a>
        <a href="salesReport.php" class="nav-button report">Sales Reports</a>
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

        <!-- Package Image Upload Section -->
        <div class="section package-image-upload">
            <div class="section-title">Package Image</div>
            <div class="form-group">
                <input type="file" id="packageImage" accept="image/*">
                <button type="button" id="removeImage" class="btn btn-secondary" style="display: none; margin-top: 10px;">Remove Image</button>
            </div>
            <div class="image-preview-container">
                <img id="imagePreview" class="image-preview" src="" alt="Image Preview">
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
    let selectedPackageImage = null;

    // Initialize with 2 default types
    document.addEventListener('DOMContentLoaded', function() {
        addType();
        addType();

        // Initialize preset price display
        updatePresetPrice();

        // Initialize image upload
        document.getElementById('packageImage').addEventListener('change', handleImageUpload);
        document.getElementById('removeImage').addEventListener('click', removeImage);
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

        const optionalQuantityInput = typeDiv.querySelector('input[name*="optional_quantity"]');
        optionalQuantityInput.addEventListener('change', function() {
            const dishesList = typeDiv.querySelector('.dishes-list');
            if (dishesList && dishesList.querySelectorAll('.selected-dish-item').length > 0) {
                const listId = dishesList.id;
                const match = listId.match(/dishes-list-(\d+)/);
                if (match) {
                    const index = parseInt(match[1]);
                    updateTypePriceModifiers(index);
                }
            }
        });
    }

    function removeType(button) {
        const typeSection = button.closest('.type-section');
        typeSection.remove();

        reindexTypes();
        updatePresetPrice();
    }
    function reindexTypes() {
        const typeSections = document.querySelectorAll('.type-section');

        typeSections.forEach((typeSection, newIndex) => {
            const inputs = typeSection.querySelectorAll('input');
            inputs.forEach(input => {
                const oldName = input.name;
                const match = oldName.match(/types\[(\d+)\]\[(\w+)\]/);
                if (match) {
                    const fieldName = match[2];
                    input.name = `types[${newIndex}][${fieldName}]`;
                }
            });

            const addDishBtn = typeSection.querySelector('.btn-add-dish');
            if (addDishBtn) {
                addDishBtn.setAttribute('onclick', `openDishModal(${newIndex})`);
            }

            const dishesList = typeSection.querySelector('.dishes-list');
            if (dishesList) {
                dishesList.id = `dishes-list-${newIndex}`;
            }
        });
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

        const typeSection = dishesList.closest('.type-section');
        const optionalQuantityInput = typeSection.querySelector('input[name*="optional_quantity"]');
        let optionalQuantity = parseInt(optionalQuantityInput.value) || 1;

        if (optionalQuantity > dishItems.length) {
            optionalQuantity = dishItems.length;
        }

        const dishData = Array.from(dishItems).map(dishItem => {
            return {
                element: dishItem,
                price: parseFloat(dishItem.getAttribute('data-item-price')),
                id: dishItem.getAttribute('data-item-id')
            };
        });

        dishData.sort((a, b) => a.price - b.price);

        let basePrice = 0;
        if (dishData.length > 0) {
            if (optionalQuantity <= dishData.length) {
                basePrice = dishData[optionalQuantity - 1].price;
            } else {
                basePrice = dishData[dishData.length - 1].price;
            }
        }

        dishData.forEach((dish, index) => {
            const priceInput = dish.element.querySelector('.price-modifier-input');

            if (index < optionalQuantity) {
                priceInput.value = 0;
                priceInput.readOnly = true;
                priceInput.style.backgroundColor = '#f0f0f0';
            } else {
                const priceDifference = dish.price - basePrice;
                priceInput.value = priceDifference.toFixed(2);
                priceInput.readOnly = false;
                priceInput.style.backgroundColor = '';

                priceInput.oninput = updatePresetPrice;
            }
        });

        const sortedDishes = dishData.sort((a, b) => a.price - b.price);

        while (dishesList.firstChild) {
            dishesList.removeChild(dishesList.firstChild);
        }
        sortedDishes.forEach(dish => dishesList.appendChild(dish.element));
    }

    function removeDish(button) {
        const dishItem = button.closest('.selected-dish-item');
        const dishesList = dishItem.closest('.dishes-list');
        const typeIndex = dishesList.id.split('-')[2];

        dishItem.remove();

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

            const optionalQuantityInput = typeSection.querySelector('input[name*="optional_quantity"]');
            let optionalQuantity = parseInt(optionalQuantityInput.value) || 1;

            if (optionalQuantity > dishItems.length) {
                optionalQuantity = dishItems.length;
            }

            const prices = Array.from(dishItems).map(dishItem => {
                return parseFloat(dishItem.getAttribute('data-item-price'));
            }).sort((a, b) => a - b);

            for (let i = 0; i < optionalQuantity; i++) {
                totalPrice += prices[i];
            }
        });

        document.getElementById('presetPriceDisplay').textContent = totalPrice.toFixed(2);
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

    // Image upload functions
    function handleImageUpload(event) {
        const file = event.target.files[0];
        if (file) {
            selectedPackageImage = file;
            const reader = new FileReader();
            reader.onload = function(e) {
                const preview = document.getElementById('imagePreview');
                preview.src = e.target.result;
                preview.style.display = 'block';
                document.getElementById('removeImage').style.display = 'inline-block';
            };
            reader.readAsDataURL(file);
        }
    }

    function removeImage() {
        selectedPackageImage = null;
        document.getElementById('packageImage').value = '';
        document.getElementById('imagePreview').style.display = 'none';
        document.getElementById('removeImage').style.display = 'none';
    }

    // Form submission
    document.getElementById('packageForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = new FormData(this);
        const types = [];

        // Collect type data
        document.querySelectorAll('.type-section').forEach((typeSection) => {
            const nameEnInput = typeSection.querySelector('input[name*="name_en"]');
            const nameZhCnInput = typeSection.querySelector('input[name*="name_zh_cn"]');
            const nameZhTwInput = typeSection.querySelector('input[name*="name_zh_tw"]');
            const optionalQuantityInput = typeSection.querySelector('input[name*="optional_quantity"]');

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

        // Show loading state
        const submitBtn = document.querySelector('.btn-primary');
        const originalBtnText = submitBtn.textContent;
        submitBtn.textContent = 'Uploading...';
        submitBtn.disabled = true;

        try {
            // Step 1: Create package and get packageId
            const packageResult = await createPackage(packageData);

            if (!packageResult.success) {
                throw new Error(packageResult.message);
            }

            const packageId = packageResult.packageId;

            // Step 2: If there's an image, upload it
            let imageUrl = 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/package/default.jpg';
            if (selectedPackageImage) {
                imageUrl = await uploadPackageImage(packageId);
            }

            // Step 3: Update package with image URL
            await updatePackageImage(packageId, imageUrl);

            // Success
            alert('Package created successfully!');
            location.reload();

        } catch (error) {
            alert('Error creating package: ' + error.message);
            submitBtn.textContent = originalBtnText;
            submitBtn.disabled = false;
        }
    });

    // Create package and get packageId
    function createPackage(packageData) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: 'save_package.php',
                type: 'POST',
                data: JSON.stringify(packageData),
                contentType: 'application/json',
                success: function(response) {
                    try {
                        const result = typeof response === 'string' ? JSON.parse(response) : response;
                        if (result.success) {
                            resolve(result);
                        } else {
                            reject(new Error(result.message || '未知服务器错误'));
                        }
                    } catch (e) {
                        reject(new Error('服务器返回无效的JSON响应'));
                    }
                },
                error: function(xhr, status, error) {
                    const errorMsg = xhr.responseText ?
                        (typeof xhr.responseText === 'string' ?
                            (xhr.responseText.substring(0, 200) + '...') :
                            '网络错误') :
                        '发生网络错误';
                    reject(new Error(`服务器错误 (${xhr.status}): ${errorMsg}`));
                }
            });
        });
    }

    // Upload package image
    function uploadPackageImage(packageId) {
        return new Promise((resolve, reject) => {
            const formData = new FormData();
            formData.append('packageImage', selectedPackageImage);
            formData.append('packageId', packageId);

            $.ajax({
                url: 'save_packageImage.php',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    try {
                        const result = typeof response === 'string' ? JSON.parse(response) : response;
                        if (result.success) {
                            resolve(result.imageUrl);
                        } else {
                            reject(new Error(result.message || '图片上传失败'));
                        }
                    } catch (e) {
                        reject(new Error('图片上传返回无效的JSON响应'));
                    }
                },
                error: function(xhr, status, error) {
                    const errorMsg = xhr.responseText || '网络错误';
                    reject(new Error(`图片上传失败 (${xhr.status}): ${errorMsg}`));
                }
            });
        });
    }

    // Update package image URL
    function updatePackageImage(packageId, imageUrl) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: 'update_package_image.php',
                type: 'POST',
                data: JSON.stringify({
                    package_id: packageId,
                    image_url: imageUrl
                }),
                contentType: 'application/json',
                success: function(response) {
                    try {
                        const result = typeof response === 'string' ? JSON.parse(response) : response;
                        if (result.success) {
                            resolve();
                        } else {
                            reject(new Error(result.message || '更新图片URL失败'));
                        }
                    } catch (e) {
                        reject(new Error('更新返回无效的JSON响应'));
                    }
                },
                error: function(xhr, status, error) {
                    const errorMsg = xhr.responseText || '网络错误';
                    reject(new Error(`更新失败 (${xhr.status}): ${errorMsg}`));
                }
            });
        });
    }

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