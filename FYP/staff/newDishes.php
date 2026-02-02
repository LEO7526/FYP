<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

// Process POST request for adding new dish
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'add_dish') {
    header('Content-Type: application/json');

    $response = [
        'success' => false,
        'message' => '',
        'item_id' => null
    ];

    try {
        // Validate required fields
        $required_fields = ['names', 'descriptions', 'price', 'categoryId', 'spiceLevel', 'tags'];
        foreach ($required_fields as $field) {
            if (!isset($_POST[$field])) {
                throw new Exception("Missing required field: $field");
            }
        }

        // Get data
        $names = json_decode($_POST['names'], true);
        $descriptions = json_decode($_POST['descriptions'], true);
        $item_price = floatval($_POST['price']);
        $category_id = intval($_POST['categoryId']);
        $spice_level = intval($_POST['spiceLevel']);
        $tags = json_decode($_POST['tags'], true);

        // Get New Data (Recipe and Customization)
        $recipe = isset($_POST['recipe']) ? json_decode($_POST['recipe'], true) : [];
        $customizations = isset($_POST['customizations']) ? json_decode($_POST['customizations'], true) : [];

        // Validate price and category ID
        if ($item_price <= 0) {
            throw new Exception('Price must be greater than 0');
        }

        if ($category_id <= 0) {
            throw new Exception('Invalid category ID');
        }

        // Validate spice level (0-5)
        if ($spice_level < 0 || $spice_level > 5) {
            throw new Exception('Spice level must be between 0 and 5');
        }

        // Validate multi-language names (at least one language has content)
        $has_name = !empty(trim($names['en'] ?? '')) ||
            !empty(trim($names['zh-CN'] ?? '')) ||
            !empty(trim($names['zh-TW'] ?? ''));

        if (!$has_name) {
            throw new Exception('At least one language dish name is required');
        }

        // Start transaction
        mysqli_autocommit($conn, false);

        // Step 1: Insert main menu item (menu_item)
        $stmt = $conn->prepare("INSERT INTO menu_item (category_id, item_price, spice_level, image_url, is_available) VALUES (?, ?, ?, ?, TRUE)");
        $default_image = 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/default.jpg';
        $stmt->bind_param("idis", $category_id, $item_price, $spice_level, $default_image);

        if (!$stmt->execute()) {
            throw new Exception("Failed to add menu item: " . $stmt->error);
        }

        $item_id = $conn->insert_id;
        $stmt->close();

        // Step 2: Insert multi-language translations (menu_item_translation)
        $stmt_trans = $conn->prepare("INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES (?, ?, ?, ?)");

        $languages = [
            ['en', $names['en'] ?? '', $descriptions['en'] ?? ''],
            ['zh-CN', $names['zh-CN'] ?? '', $descriptions['zh-CN'] ?? ''],
            ['zh-TW', $names['zh-TW'] ?? '', $descriptions['zh-TW'] ?? '']
        ];

        foreach ($languages as $lang_data) {
            $lang_code = $lang_data[0];
            $name = trim($lang_data[1]);
            $desc = trim($lang_data[2]);

            // Only insert languages with content
            if (!empty($name) || !empty($desc)) {
                $stmt_trans->bind_param("isss", $item_id, $lang_code, $name, $desc);
                if (!$stmt_trans->execute()) {
                    throw new Exception("Failed to add translation ({$lang_code}): " . $stmt_trans->error);
                }
            }
        }
        $stmt_trans->close();

        // Step 3: Associate tags (menu_tag)
        if (!empty($tags)) {
            $stmt_tag = $conn->prepare("INSERT INTO menu_tag (item_id, tag_id) VALUES (?, ?)");

            foreach ($tags as $tag_id) {
                $tag_id = intval($tag_id);
                if ($tag_id > 0) {
                    // Check if tag_id exists
                    $check_tag = $conn->prepare("SELECT tag_id FROM tag WHERE tag_id = ?");
                    $check_tag->bind_param("i", $tag_id);
                    $check_tag->execute();
                    $check_tag->store_result();
                    if ($check_tag->num_rows == 0) {
                        throw new Exception("Invalid tag ID: {$tag_id}");
                    }
                    $check_tag->close();

                    $stmt_tag->bind_param("ii", $item_id, $tag_id);
                    if (!$stmt_tag->execute()) {
                        throw new Exception("Failed to associate tag: " . $stmt_tag->error);
                    }
                }
            }
            $stmt_tag->close();
        }

        // Step 4: Insert Recipe Materials
        if (!empty($recipe)) {
            $stmt_recipe = $conn->prepare("INSERT INTO recipe_materials (item_id, mid, quantity) VALUES (?, ?, ?)");
            foreach ($recipe as $ing) {
                $mid = intval($ing['materialId']);
                $qty = floatval($ing['quantity']);
                if ($mid > 0 && $qty > 0) {
                    $stmt_recipe->bind_param("iid", $item_id, $mid, $qty);
                    $stmt_recipe->execute();
                }
            }
            $stmt_recipe->close();
        }

        // Step 5: Insert Customization Options
        if (!empty($customizations)) {
            $stmt_cust = $conn->prepare("INSERT INTO item_customization_options (item_id, group_id, max_selections, is_required) VALUES (?, ?, ?, 0)");
            foreach ($customizations as $cust) {
                $groupId = intval($cust['groupId']);
                $maxSel = intval($cust['maxSelections']);
                if ($groupId > 0 && $maxSel > 0) {
                    $stmt_cust->bind_param("iii", $item_id, $groupId, $maxSel);
                    $stmt_cust->execute();
                }
            }
            $stmt_cust->close();
        }

        // All operations successful, commit transaction
        mysqli_commit($conn);
        $response['success'] = true;
        $response['message'] = 'Menu item added successfully';
        $response['item_id'] = $item_id;

    } catch (Exception $e) {
        // Error occurred, rollback transaction
        mysqli_rollback($conn);
        $response['message'] = $e->getMessage();
    } finally {
        // Restore auto-commit
        mysqli_autocommit($conn, true);
    }

    echo json_encode($response);
    exit;
}

// Get filter parameter (original code)
$filter = isset($_GET['filter']) ? $_GET['filter'] : 'all';
$page = isset($_GET['page']) ? $_GET['page'] : 'staffIndex.php';

// Build SQL WHERE conditions (original code)
$whereConditions = [];
if ($filter === 'pending') {
    $whereConditions[] = "b.status = 1";
} elseif ($filter === 'confirmed') {
    $whereConditions[] = "b.status = 2";
} elseif ($filter === 'history') {
    $whereConditions[] = "b.status = 3";
} elseif ($filter === 'cancelled') {
    $whereConditions[] = "b.status = 0";
} else {
    // Default show all (except cancelled)
    $whereConditions[] = "b.status != 0";
}

$whereSql = implode(" AND ", $whereConditions);

$sql = "SELECT b.bid, b.bkcname, b.bktel, b.tid, b.bdate, b.btime, b.pnum, b.purpose, b.status, b.remark, 
               b.cid, c.cname as member_name
        FROM booking b
        LEFT JOIN customer c ON b.cid = c.cid
        WHERE $whereSql
        ORDER BY b.bdate ASC, b.btime ASC";
$result = mysqli_query($conn, $sql);

$bookings = [];
while ($row = mysqli_fetch_assoc($result)) {
    $bookings[] = $row;
}

// Fetch distinct tag categories from tag table (original code)
$tagCategoriesQuery = "SELECT DISTINCT tag_category FROM tag ORDER BY tag_category";
$tagCategoriesResult = mysqli_query($conn, $tagCategoriesQuery);
$tagCategories = [];
while ($row = mysqli_fetch_assoc($tagCategoriesResult)) {
    $tagCategories[] = $row['tag_category'];
}

// Fetch all tags from database (original code)
$tagsQuery = "SELECT * FROM tag ORDER BY tag_category, tag_name";
$tagsResult = mysqli_query($conn, $tagsQuery);
$allTags = [];
while ($row = mysqli_fetch_assoc($tagsResult)) {
    $allTags[] = $row;
}

// Fetch menu categories
$menuCategoriesQuery = "SELECT * FROM menu_category ORDER BY category_name";
$menuCategoriesResult = mysqli_query($conn, $menuCategoriesQuery);
$menuCategories = [];
while ($row = mysqli_fetch_assoc($menuCategoriesResult)) {
    $menuCategories[] = $row;
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>New Dishes</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/newDishes.css">
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
<div class="container">
    <h1>New Dish</h1>

    <!-- Image upload area -->
    <div class="section">
        <div class="section-title">Dish Image</div>
        <div class="form-group">
            <label for="dishImage">Select Image</label>
            <input type="file" id="dishImage" accept="image/*">
            <button type="button" id="removeImage" class="btn btn-secondary" style="display: none; margin-top: 10px;">Remove Image</button>
        </div>
        <div class="image-preview-container">
            <img id="imagePreview" class="image-preview" src="" alt="Image Preview">
        </div>
    </div>

    <!-- Basic Information Section -->
    <div class="section">
        <div class="section-title">Basic Information</div>

        <!-- Dish Name (Multi-language) -->
        <div class="form-group">
            <label>Dish Name</label>
            <div class="language-row">
                <div class="language-col">
                    <div class="language-label">English</div>
                    <input type="text" id="nameEn" placeholder="English Name">
                </div>
                <div class="language-col">
                    <div class="language-label">Simplified Chinese</div>
                    <input type="text" id="nameZhCn" placeholder="Simplified Chinese Name">
                </div>
                <div class="language-col">
                    <div class="language-label">Traditional Chinese</div>
                    <input type="text" id="nameZhTw" placeholder="Traditional Chinese Name">
                </div>
            </div>
        </div>

        <!-- Dish Description (Multi-language) -->
        <div class="form-group">
            <label>Dish Description</label>
            <div class="language-row">
                <div class="language-col">
                    <div class="language-label">English</div>
                    <textarea id="descEn" placeholder="English Description"></textarea>
                </div>
                <div class="language-col">
                    <div class="language-label">Simplified Chinese</div>
                    <textarea id="descZhCn" placeholder="Simplified Chinese Description"></textarea>
                </div>
                <div class="language-col">
                    <div class="language-label">Traditional Chinese</div>
                    <textarea id="descZhTw" placeholder="Traditional Chinese Description"></textarea>
                </div>
            </div>
        </div>

        <!-- Price -->
        <div class="form-group">
            <label for="price">Price (HKD)</label>
            <input type="number" id="price" min="0" step="0.01" placeholder="0.00">
        </div>

        <!-- Category and Spiciness -->
        <div class="form-group">
            <div class="language-row">
                <div class="language-col">
                    <label for="category">Category</label>
                    <select id="category">
                        <option value="">Select Category</option>
                        <?php foreach ($menuCategories as $category): ?>
                            <option value="<?= $category['category_id'] ?>"><?= htmlspecialchars($category['category_name']) ?></option>
                        <?php endforeach; ?>
                    </select>
                </div>
                <div class="language-col">
                    <label for="spiceLevel">Spiciness Level</label>
                    <select id="spiceLevel">
                        <option value="0">Not Spicy (0)</option>
                        <option value="1">Mild Spicy (1)</option>
                        <option value="2">Medium Spicy (2)</option>
                        <option value="3">Spicy (3)</option>
                        <option value="4">Very Spicy (4)</option>
                        <option value="5">Extremely Spicy (5)</option>
                    </select>
                </div>
            </div>
        </div>
    </div>

    <!-- Recipe Materials Section -->
    <div class="section">
        <div class="section-title">Recipe Materials</div>
        <div id="recipeContainer">
        </div>
        <button type="button" class="btn btn-secondary add-row-btn" onclick="addRecipeRow()">+ Add Material</button>
    </div>

    <!-- Customization Options Section -->
    <div class="section">
        <div class="header-with-action">
            <div class="section-title" style="margin-bottom:0">Customization Options</div>
            <button type="button" class="btn btn-secondary edit-options-btn" style="font-size: 14px; padding: 5px 10px;">Edit Options</button>
        </div>

        <div id="customizationContainer">
        </div>
        <button type="button" class="btn btn-secondary add-row-btn" onclick="addCustomizationRow()">+ Add Option Group</button>
    </div>

    <!-- Tags Section -->
    <div class="section">
        <div class="section-title">Tag Management</div>

        <!-- Selected Tags Display -->
        <div class="form-group">
            <label>Selected Tags</label>
            <div id="selectedTags" class="tag-container">
            </div>
        </div>

        <!-- Tag Selector -->
        <div class="form-group">
            <label>Select Tag
                <button type="button" id="deleteTagBtn" class="btn btn-danger" style="margin-left: 10px; float: right;">Delete Mode</button>
            </label>
            <div style="clear: both;"></div>

            <div id="tagCategoryButtons" class="tag-category-buttons">
            </div>

            <div id="tagSelectorBox" class="tag-selector-box">
            </div>

            <button type="button" id="newTagBtn" class="btn btn-secondary">+ Add New Tag</button>

            <!-- New Tag Form -->
            <div id="newTagForm" class="new-tag-form">
                <div class="new-tag-row">
                    <input type="text" id="newTagName" placeholder="Tag Name">
                    <select id="newTagCategory">
                        <option value="">Select Category</option>
                        <?php foreach ($tagCategories as $category): ?>
                            <option value="<?= htmlspecialchars($category) ?>"><?= htmlspecialchars($category) ?></option>
                        <?php endforeach; ?>
                    </select>
                </div>
                <div class="color-Select-row">
                    <div id="colorPreview" class="color-preview"></div>
                    <button type="button" id="colorPickerBtn" class="btn btn-secondary">Select Color</button>
                    <input type="hidden" id="newTagColor" value="#4fc3f7">
                </div>
                <div class="new-tag-row">
                    <button type="button" id="addNewTagBtn" class="btn btn-success">Add</button>
                    <button type="button" id="cancelNewTagBtn" class="btn btn-secondary">Cancel</button>
                </div>
                <div id="newTagError" class="error-message"></div>
            </div>
        </div>
    </div>

    <!-- Action Buttons -->
    <div class="action-buttons">
        <button type="button" id="cancelBtn" class="btn btn-secondary">Cancel</button>
        <button type="button" id="confirmBtn" class="btn btn-primary">Confirm Addition</button>
    </div>
</div>

<!-- Customization Modal -->
<div id="customModal" class="custom-modal">
    <div class="custom-modal-content">
        <div class="modal-header">
            <h3>Manage Customization Groups</h3>
            <button class="close-btn" onclick="closeCustomizationModal()">×</button>
        </div>

        <div class="modal-tabs">
            <div class="modal-tab active" onclick="switchTab('create')">Create Group</div>
            <div class="modal-tab" onclick="switchTab('delete')">Delete Group</div>
        </div>

        <div id="createSection" class="modal-section active">
            <div class="form-group">
                <label>Group Type</label>
                <input type="text" id="newGroupType" placeholder="e.g., spice, sugar, ice, milk, topping, other">
                <small style="color:#666;">Enter a type for the new group (e.g., spice, sugar, ice, etc.)</small>
            </div>
            <div class="form-group">
                <label>Group Name</label>
                <input type="text" id="newGroupName" placeholder="e.g., Sweetness Level">
            </div>
            <div class="form-group">
                <label>Values (one per line)</label>
                <div id="newValueContainer">
                </div>
                <button type="button" class="btn btn-secondary" onclick="addNewValueRow()" style="margin-top:5px; font-size:12px;">+ Add Value</button>
            </div>
            <div class="action-buttons">
                <button class="btn btn-success" onclick="submitNewGroup()">Create</button>
            </div>
        </div>

        <div id="deleteSection" class="modal-section">
            <p style="color:#666; margin-bottom:10px;">Select unused groups to delete:</p>
            <div id="unusedGroupsList">
            </div>
            <div class="action-buttons">
                <button class="btn btn-danger" onclick="submitDeleteGroups()">Delete Selected</button>
            </div>
        </div>
    </div>
</div>

<!-- Color Picker Modal -->
<div id="colorPickerModal" class="color-picker-modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Select Color</h3>
            <button id="closeColorPicker" class="close-btn">×</button>
        </div>

        <div id="hueSaturation" class="hue-saturation">
            <div id="pickerHandle" class="picker-handle"></div>
        </div>

        <div id="hueSlider" class="hue-slider">
            <div id="sliderHandle" class="slider-handle"></div>
        </div>

        <div class="color-controls">
            <div id="colorValue" class="color-value">#4a6cf7</div>
            <button id="applyColorBtn" class="apply-btn">Apply</button>
        </div>
    </div>
</div>

<script>
    // Global variables
    let selectedImage = null;
    let selectedTags = [];
    let allTags = <?php echo json_encode($allTags); ?>;
    let tagCategories = <?php echo json_encode($tagCategories); ?>;
    let allCategories = <?php echo json_encode($menuCategories); ?>;
    let colorPickerColor = '#4a6cf7';
    let deleteMode = false;

    // New features global cache
    let materialCategories = [];
    let customizationGroups = [];

    // Initialize when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        // Initialize color picker
        initColorPicker();

        // Load tags data
        loadTags();

        // Bind event listeners
        bindEventListeners();

        // Initialize color preview
        document.getElementById('colorPreview').style.backgroundColor = '#4fc3f7';

        // Initialize new features
        (async function() {
            try {
                // Load material categories first
                await loadMaterialCategories();
                // Then load customization groups
                await loadCustomizationGroups();

                // Add initial rows only after data is loaded
                addRecipeRow();
                addCustomizationRow();
            } catch (error) {
                console.error('Failed to load initial data:', error);
                // Even if loading fails, add initial rows
                addRecipeRow();
                addCustomizationRow();
            }
        })();
    });

    // Modify loadMaterialCategories to return Promise
    function loadMaterialCategories() {
        return new Promise((resolve, reject) => {
            fetch('get_materials_data.php?action=get_categories')
                .then(res => res.json())
                .then(data => {
                    if(data.success) {
                        materialCategories = data.data;
                        console.log('Material categories loaded:', materialCategories.length);
                        resolve();
                    } else {
                        reject(new Error('Failed to load material categories'));
                    }
                })
                .catch(error => {
                    console.error('Error loading material categories:', error);
                    materialCategories = [];
                    reject(error);
                });
        });
    }

    // Modify loadCustomizationGroups to return Promise
    function loadCustomizationGroups() {
        return new Promise((resolve, reject) => {
            fetch('manage_customization.php?action=get_all_groups')
                .then(res => res.json())
                .then(data => {
                    if(data.success) {
                        customizationGroups = data.data;
                        console.log('Customization groups loaded:', customizationGroups.length);
                        updateAllGroupDropdowns();
                        resolve();
                    } else {
                        reject(new Error('Failed to load customization groups'));
                    }
                })
                .catch(error => {
                    console.error('Error loading customization groups:', error);
                    customizationGroups = [];
                    reject(error);
                });
        });
    }

    // Bind event listeners
    function bindEventListeners() {
        // Image upload
        document.getElementById('dishImage').addEventListener('change', handleImageUpload);
        document.getElementById('removeImage').addEventListener('click', removeImage);

        // Tag management
        document.getElementById('newTagBtn').addEventListener('click', toggleNewTagForm);
        document.getElementById('cancelNewTagBtn').addEventListener('click', toggleNewTagForm);
        document.getElementById('addNewTagBtn').addEventListener('click', addNewTag);
        document.getElementById('colorPickerBtn').addEventListener('click', openColorPicker);
        document.getElementById('deleteTagBtn').addEventListener('click', toggleDeleteMode);

        // Color picker
        document.getElementById('closeColorPicker').addEventListener('click', closeColorPicker);
        document.getElementById('applyColorBtn').addEventListener('click', applyColor);

        // Action buttons
        document.getElementById('cancelBtn').addEventListener('click', cancelForm);
        document.getElementById('confirmBtn').addEventListener('click', confirmForm);

        // Edit Options button
        const editOptionsBtn = document.querySelector('.edit-options-btn');
        if (editOptionsBtn) {
            editOptionsBtn.addEventListener('click', openCustomizationModal);
        }
    }

    // Handle image upload
    function handleImageUpload(event) {
        const file = event.target.files[0];
        if (file) {
            selectedImage = file;
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

    // Remove image
    function removeImage() {
        selectedImage = null;
        document.getElementById('dishImage').value = '';
        document.getElementById('imagePreview').style.display = 'none';
        document.getElementById('removeImage').style.display = 'none';
    }

    // Load tags data
    function loadTags() {
        // Create tag category buttons
        const tagCategoryButtons = document.getElementById('tagCategoryButtons');
        tagCategories.forEach(category => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'tag-category-btn';
            button.textContent = category;
            button.dataset.category = category;
            button.addEventListener('click', function() {
                // Toggle active state
                document.querySelectorAll('.tag-category-btn').forEach(btn => {
                    btn.classList.remove('active');
                });
                this.classList.add('active');

                // Show tags for the selected category
                showTagsByCategory(category);
            });
            tagCategoryButtons.appendChild(button);
        });
    }

    // Show tags by category
    function showTagsByCategory(category) {
        const tagSelectorBox = document.getElementById('tagSelectorBox');
        tagSelectorBox.innerHTML = '';
        tagSelectorBox.style.display = 'block';

        const categoryTags = allTags.filter(tag => tag.tag_category === category);

        categoryTags.forEach(tag => {
            const tagElement = document.createElement('div');
            tagElement.className = 'tag-capsule';
            tagElement.textContent = tag.tag_name;
            tagElement.style.backgroundColor = tag.tag_bg_color;
            tagElement.dataset.tagId = tag.tag_id;

            // If in delete mode, add red border
            if (deleteMode) {
                tagElement.style.border = '2px solid red';
                tagElement.classList.add('delete-mode');
            }

            tagElement.addEventListener('click', function() {
                if (deleteMode) {
                    deleteTag(tag.tag_id, tagElement);
                } else {
                    addTagToSelection(tag);
                }
            });

            tagSelectorBox.appendChild(tagElement);
        });
    }

    // Add tag to selection
    function addTagToSelection(tag) {
        // Check if already selected
        if (selectedTags.some(t => t.tag_id === tag.tag_id)) {
            return;
        }

        selectedTags.push(tag);
        updateSelectedTagsDisplay();
    }

    // Update selected tags display
    function updateSelectedTagsDisplay() {
        const selectedTagsContainer = document.getElementById('selectedTags');
        selectedTagsContainer.innerHTML = '';

        selectedTags.forEach(tag => {
            const tagElement = document.createElement('div');
            tagElement.className = 'tag-capsule';
            tagElement.style.backgroundColor = tag.tag_bg_color;

            const tagName = document.createElement('span');
            tagName.textContent = tag.tag_name;

            const removeBtn = document.createElement('span');
            removeBtn.className = 'remove';
            removeBtn.textContent = '×';
            removeBtn.addEventListener('click', function(e) {
                e.stopPropagation();
                removeTagFromSelection(tag.tag_id);
            });

            tagElement.appendChild(tagName);
            tagElement.appendChild(removeBtn);
            selectedTagsContainer.appendChild(tagElement);
        });
    }

    // Remove tag from selection
    function removeTagFromSelection(tagId) {
        selectedTags = selectedTags.filter(tag => tag.tag_id !== tagId);
        updateSelectedTagsDisplay();
    }

    // Toggle delete mode
    function toggleDeleteMode() {
        deleteMode = !deleteMode;
        const btn = document.getElementById('deleteTagBtn');

        if (deleteMode) {
            btn.classList.add('active');
            btn.textContent = 'Exit Delete Mode';
            // Add red border to all tags
            document.querySelectorAll('.tag-capsule').forEach(tag => {
                tag.style.border = '2px solid red';
                tag.classList.add('delete-mode');
            });
        } else {
            btn.classList.remove('active');
            btn.textContent = 'Delete Mode';
            // Remove red border from all tags
            document.querySelectorAll('.tag-capsule').forEach(tag => {
                tag.style.border = '';
                tag.classList.remove('delete-mode');
            });
        }

        // Refresh current category display
        const activeCategory = document.querySelector('.tag-category-btn.active');
        if (activeCategory) {
            showTagsByCategory(activeCategory.dataset.category);
        }
    }

    // Delete tag
    function deleteTag(tagId, tagElement) {
        if (!confirm('Are you sure you want to delete this tag?')) {
            return;
        }

        // Check if tag is being used
        checkTagUsage(tagId)
            .then(isUsed => {
                if (isUsed) {
                    alert('This tag is being used by dishes and cannot be deleted');
                    return;
                }

                // Send delete request
                return fetch('delete_tag.php', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ tag_id: tagId })
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            // Remove from allTags
                            allTags = allTags.filter(tag => tag.tag_id !== tagId);
                            // Remove from selectedTags
                            removeTagFromSelection(tagId);
                            // Remove from UI
                            tagElement.remove();
                            alert('Tag deleted successfully');

                            // Refresh current category display
                            const activeCategory = document.querySelector('.tag-category-btn.active');
                            if (activeCategory) {
                                showTagsByCategory(activeCategory.dataset.category);
                            }
                        } else {
                            alert('Failed to delete tag: ' + data.message);
                        }
                    });
            })
            .catch(error => {
                alert('Error checking tag usage: ' + error);
            });
    }

    // Check tag usage
    function checkTagUsage(tagId) {
        return fetch('check_tag_usage.php?tag_id=' + tagId)
            .then(response => response.json())
            .then(data => {
                return data.is_used;
            });
    }

    // Toggle new tag form display
    function toggleNewTagForm() {
        const form = document.getElementById('newTagForm');
        form.style.display = form.style.display === 'block' ? 'none' : 'block';
    }

    // Add new tag
    async function addNewTag() {
        const name = document.getElementById('newTagName').value.trim();
        const category = document.getElementById('newTagCategory').value;
        const color = document.getElementById('newTagColor').value;
        const errorElement = document.getElementById('newTagError');

        // Validate input
        if (!name || !category) {
            errorElement.textContent = 'Please fill in tag name and select category';
            return;
        }

        try {
            // Step 1: Check if tag name already exists in database
            const checkResponse = await fetch('check_tag_duplicate.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ tag_name: name })
            });

            const checkData = await checkResponse.json();

            if (!checkData.success) {
                errorElement.textContent = 'Error checking tag: ' + checkData.message;
                return;
            }

            if (checkData.exists) {
                errorElement.textContent = 'Tag name already exists in database';
                return;
            }

            // Step 2: Add new tag to database
            const addResponse = await fetch('add_tag.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    tag_name: name,
                    tag_category: category,
                    tag_color: color
                })
            });

            const addData = await addResponse.json();

            if (!addData.success) {
                errorElement.textContent = 'Failed to add tag: ' + addData.message;
                return;
            }

            // Add to tags list
            const newTag = addData.tag;
            allTags.push(newTag);

            // If tag category is new, add to categories list
            if (!tagCategories.includes(category)) {
                tagCategories.push(category);

                // Add new category button
                const tagCategoryButtons = document.getElementById('tagCategoryButtons');
                const button = document.createElement('button');
                button.type = 'button';
                button.className = 'tag-category-btn';
                button.textContent = category;
                button.dataset.category = category;
                button.addEventListener('click', function() {
                    document.querySelectorAll('.tag-category-btn').forEach(btn => {
                        btn.classList.remove('active');
                    });
                    this.classList.add('active');
                    showTagsByCategory(category);
                });
                tagCategoryButtons.appendChild(button);
            }

            // Reset form
            document.getElementById('newTagName').value = '';
            document.getElementById('newTagCategory').selectedIndex = 0;
            document.getElementById('newTagColor').value = '#4fc3f7';
            document.getElementById('colorPreview').style.backgroundColor = '#4fc3f7';

            // Hide form
            toggleNewTagForm();

            // Clear error message
            errorElement.textContent = '';

            // Automatically select the new tag
            addTagToSelection(newTag);

        } catch (error) {
            errorElement.textContent = 'Network error: ' + error;
        }
    }

    // Open color picker
    function openColorPicker() {
        document.getElementById('colorPickerModal').style.display = 'block';
    }

    // Close color picker
    function closeColorPicker() {
        document.getElementById('colorPickerModal').style.display = 'none';
    }

    // Apply selected color
    function applyColor() {
        document.getElementById('newTagColor').value = colorPickerColor;
        document.getElementById('colorPreview').style.backgroundColor = colorPickerColor;
        closeColorPicker();
    }

    // Cancel form
    function cancelForm() {
        if (confirm('Are you sure you want to cancel? All unsaved changes will be lost.')) {
            resetForm();
            window.history.back();
        }
    }

    // Confirm dish addition
    async function confirmForm() {
        // Validate form
        if (!validateForm()) {
            return;
        }

        // Show loading state
        const confirmBtn = document.getElementById('confirmBtn');
        const originalText = confirmBtn.textContent;
        confirmBtn.textContent = 'Uploading...';
        confirmBtn.disabled = true;

        let itemId = null;
        let rollbackNeeded = false;

        try {
            // Collect form data (excluding image)
            const formData = collectFormData();

            // Step 1: Add dish basic info to database
            itemId = await addDish(formData);
            rollbackNeeded = true; // Mark as needing rollback

            // Step 2: If there's an image, upload it to GitHub
            let imageUrl = null;
            if (selectedImage) {
                imageUrl = await uploadImage(itemId);
            } else {
                // If no image, use placeholder
                imageUrl = 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/default.jpg';
            }

            // Step 3: Update dish image URL in database
            await updateDishImage(itemId, imageUrl);
            rollbackNeeded = false; // All steps successful, no rollback needed

            // Show success message and reset form
            alert('Dish added successfully!');

            // Reset the form
            resetForm();

            // Reset button state
            confirmBtn.textContent = originalText;
            confirmBtn.disabled = false;

        } catch (error) {
            // If rollback is needed, delete the created dish
            if (rollbackNeeded && itemId) {
                try {
                    await deleteDish(itemId);
                    console.log('Rolled back and deleted dish ID:', itemId);
                } catch (deleteError) {
                    console.error('Failed to delete dish:', deleteError);
                    // Continue to show the main error even if deletion fails
                }
            }

            alert('Failed to add dish: ' + error);
            // Reset button state
            confirmBtn.textContent = originalText;
            confirmBtn.disabled = false;
        }
    }

    // Function to delete a dish
    function deleteDish(itemId) {
        return new Promise((resolve, reject) => {
            const data = {
                item_id: itemId
            };

            fetch('delete_dish.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        resolve();
                    } else {
                        reject(data.message || 'Failed to delete dish');
                    }
                })
                .catch(error => {
                    reject('Network error: ' + error.message);
                });
        });
    }

    // Add dish to database - Modified to send to current page
    function addDish(formData) {
        return new Promise((resolve, reject) => {
            // Create FormData object
            const data = new FormData();
            data.append('action', 'add_dish');
            data.append('names', JSON.stringify(formData.names));
            data.append('descriptions', JSON.stringify(formData.descriptions));
            data.append('price', formData.price);
            data.append('categoryId', formData.categoryId);
            data.append('spiceLevel', formData.spiceLevel);
            data.append('tags', JSON.stringify(formData.tags));
            data.append('recipe', JSON.stringify(formData.recipe));
            data.append('customizations', JSON.stringify(formData.customizations));

            // Send to current page
            fetch('newDishes.php', {
                method: 'POST',
                body: data
            })
                .then(response => {
                    const contentType = response.headers.get('content-type');
                    if (!contentType || !contentType.includes('application/json')) {
                        return response.text().then(text => {
                            throw new Error(`Server returned non-JSON response: ${text.substring(0, 100)}`);
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success) {
                        resolve(data.item_id);
                    } else {
                        reject(data.message || 'Unknown error');
                    }
                })
                .catch(error => {
                    reject('Network error: ' + error.message);
                });
        });
    }

    // Upload image to GitHub
    function uploadImage(itemId) {
        return new Promise((resolve, reject) => {
            const formData = new FormData();
            formData.append('dishImage', selectedImage);
            formData.append('itemId', itemId);

            fetch('save_dishesImage.php', {
                method: 'POST',
                body: formData
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        resolve(data.imageUrl);
                    } else {
                        reject(data.message);
                    }
                })
                .catch(error => {
                    reject('Network error: ' + error);
                });
        });
    }

    // Update dish image URL in database
    function updateDishImage(itemId, imageUrl) {
        return new Promise((resolve, reject) => {
            const data = {
                item_id: itemId,
                image_url: imageUrl
            };

            fetch('update_dish_image.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        resolve();
                    } else {
                        reject(data.message);
                    }
                })
                .catch(error => {
                    reject('Network error: ' + error);
                });
        });
    }

    // Validate form
    function validateForm() {
        let isValid = true;
        const errorMessages = [];

        // Check names
        if (!document.getElementById('nameEn').value.trim() ||
            !document.getElementById('nameZhCn').value.trim() ||
            !document.getElementById('nameZhTw').value.trim()) {
            errorMessages.push('Please fill in dish names in all languages');
            isValid = false;
        }

        // Check descriptions
        if (!document.getElementById('descEn').value.trim() ||
            !document.getElementById('descZhCn').value.trim() ||
            !document.getElementById('descZhTw').value.trim()) {
            errorMessages.push('Please fill in dish descriptions in all languages');
            isValid = false;
        }

        // Check price
        if (!document.getElementById('price').value || parseFloat(document.getElementById('price').value) <= 0) {
            errorMessages.push('Please enter a valid price');
            isValid = false;
        }

        // Check category
        if (!document.getElementById('category').value) {
            errorMessages.push('Please select a dish category');
            isValid = false;
        }

        // Show error messages
        if (errorMessages.length > 0) {
            alert('Please complete the following items:\n' + errorMessages.join('\n'));
        }

        return isValid;
    }

    // Collect form data
    function collectFormData() {
        const data = {
            names: {
                en: document.getElementById('nameEn').value.trim(),
                'zh-CN': document.getElementById('nameZhCn').value.trim(),
                'zh-TW': document.getElementById('nameZhTw').value.trim()
            },
            descriptions: {
                en: document.getElementById('descEn').value.trim(),
                'zh-CN': document.getElementById('descZhCn').value.trim(),
                'zh-TW': document.getElementById('descZhTw').value.trim()
            },
            price: parseFloat(document.getElementById('price').value),
            categoryId: document.getElementById('category').value,
            spiceLevel: document.getElementById('spiceLevel').value,
            tags: selectedTags.map(tag => tag.tag_id)
        };

        // Collect Recipe
        const recipeData = [];
        document.querySelectorAll('#recipeContainer .dynamic-row').forEach(row => {
            const matId = row.querySelector('.recipe-mat-select').value;
            const qty = row.querySelector('.recipe-qty').value;
            if (matId && qty) {
                recipeData.push({ materialId: matId, quantity: qty });
            }
        });
        data.recipe = recipeData;

        // Collect Customizations
        const custData = [];
        document.querySelectorAll('#customizationContainer .dynamic-row').forEach(row => {
            const grpId = row.querySelector('.cust-group-select').value;
            const max = row.querySelector('.cust-max-select').value;
            if (grpId) {
                custData.push({ groupId: grpId, maxSelections: max || 1 });
            }
        });
        data.customizations = custData;

        return data;
    }

    // Color picker functionality
    function initColorPicker() {
        const hueSaturation = document.getElementById('hueSaturation');
        const hueSlider = document.getElementById('hueSlider');
        const pickerHandle = document.getElementById('pickerHandle');
        const sliderHandle = document.getElementById('sliderHandle');
        const colorValue = document.getElementById('colorValue');

        let currentColor = {
            h: 230,
            s: 0.9,
            v: 0.97
        };

        updateHueSaturationBackground();
        positionHandles();

        hueSaturation.addEventListener('mousedown', startHueSaturationDrag);
        hueSlider.addEventListener('mousedown', startHueSliderDrag);

        function updateHueSaturationBackground() {
            hueSaturation.style.background =
                `linear-gradient(to right, #fff 0%, rgba(255,255,255,0) 100%),
                     linear-gradient(to bottom, transparent 0%, #000 100%),
                     hsl(${currentColor.h}, 100%, 50%)`;
        }

        function positionHandles() {
            const x = currentColor.s * hueSaturation.offsetWidth;
            const y = (1 - currentColor.v) * hueSaturation.offsetHeight;
            pickerHandle.style.left = `${x}px`;
            pickerHandle.style.top = `${y}px`;

            const hueX = (currentColor.h / 360) * hueSlider.offsetWidth;
            sliderHandle.style.left = `${hueX}px`;
        }

        function startHueSaturationDrag(e) {
            document.addEventListener('mousemove', dragHueSaturation);
            document.addEventListener('mouseup', stopHueSaturationDrag);
            updateHueSaturation(e);
        }

        function dragHueSaturation(e) {
            updateHueSaturation(e);
        }

        function stopHueSaturationDrag() {
            document.removeEventListener('mousemove', dragHueSaturation);
            document.removeEventListener('mouseup', stopHueSaturationDrag);
        }

        function updateHueSaturation(e) {
            const rect = hueSaturation.getBoundingClientRect();
            let x = e.clientX - rect.left;
            let y = e.clientY - rect.top;

            x = Math.max(0, Math.min(x, rect.width));
            y = Math.max(0, Math.min(y, rect.height));

            currentColor.s = x / rect.width;
            currentColor.v = 1 - (y / rect.height);

            updateColorValue();
            pickerHandle.style.left = `${x}px`;
            pickerHandle.style.top = `${y}px`;
        }

        function startHueSliderDrag(e) {
            document.addEventListener('mousemove', dragHueSlider);
            document.addEventListener('mouseup', stopHueSliderDrag);
            updateHueSlider(e);
        }

        function dragHueSlider(e) {
            updateHueSlider(e);
        }

        function stopHueSliderDrag() {
            document.removeEventListener('mousemove', dragHueSlider);
            document.removeEventListener('mouseup', stopHueSliderDrag);
        }

        function updateHueSlider(e) {
            const rect = hueSlider.getBoundingClientRect();
            let x = e.clientX - rect.left;

            x = Math.max(0, Math.min(x, rect.width));

            currentColor.h = (x / rect.width) * 360;

            updateColorValue();
            updateHueSaturationBackground();
            sliderHandle.style.left = `${x}px`;
        }

        function updateColorValue() {
            const rgb = hsvToRgb(currentColor.h, currentColor.s, currentColor.v);
            colorPickerColor = rgbToHex(rgb.r, rgb.g, rgb.b);
            colorValue.textContent = colorPickerColor;
        }

        function hsvToRgb(h, s, v) {
            let r, g, b;

            const i = Math.floor(h / 60);
            const f = h / 60 - i;
            const p = v * (1 - s);
            const q = v * (1 - f * s);
            const t = v * (1 - (1 - f) * s);

            switch (i % 6) {
                case 0: r = v, g = t, b = p; break;
                case 1: r = q, g = v, b = p; break;
                case 2: r = p, g = v, b = t; break;
                case 3: r = p, g = q, b = v; break;
                case 4: r = t, g = p, b = v; break;
                case 5: r = v, g = p, b = q; break;
            }

            return {
                r: Math.round(r * 255),
                g: Math.round(g * 255),
                b: Math.round(b * 255)
            };
        }

        function rgbToHex(r, g, b) {
            return '#' + [r, g, b].map(x => {
                const hex = x.toString(16);
                return hex.length === 1 ? '0' + hex : hex;
            }).join('');
        }
    }

    // Function to reset the form
    function resetForm() {
        // Clear image-related fields
        selectedImage = null;
        document.getElementById('dishImage').value = '';
        document.getElementById('imagePreview').style.display = 'none';
        document.getElementById('removeImage').style.display = 'none';

        // Clear multilingual names
        document.getElementById('nameEn').value = '';
        document.getElementById('nameZhCn').value = '';
        document.getElementById('nameZhTw').value = '';

        // Clear multilingual descriptions
        document.getElementById('descEn').value = '';
        document.getElementById('descZhCn').value = '';
        document.getElementById('descZhTw').value = '';

        // Clear price
        document.getElementById('price').value = '';

        // Reset category selection
        document.getElementById('category').selectedIndex = 0;

        // Reset spice level selection
        document.getElementById('spiceLevel').selectedIndex = 0;

        // Clear selected tags
        selectedTags = [];
        updateSelectedTagsDisplay();

        // Reset new tag form
        document.getElementById('newTagName').value = '';
        document.getElementById('newTagCategory').selectedIndex = 0;
        document.getElementById('newTagColor').value = '#4fc3f7';
        document.getElementById('colorPreview').style.backgroundColor = '#4fc3f7';
        document.getElementById('newTagForm').style.display = 'none';
        document.getElementById('newTagError').textContent = '';

        // Reset tag selector
        const activeCategory = document.querySelector('.tag-category-btn.active');
        if (activeCategory) {
            activeCategory.classList.remove('active');
        }
        document.getElementById('tagSelectorBox').innerHTML = '';
        document.getElementById('tagSelectorBox').style.display = 'none';

        // Exit delete mode (if in delete mode)
        if (deleteMode) {
            toggleDeleteMode();
        }

        // Clear recipe section
        document.getElementById('recipeContainer').innerHTML = '';
        addRecipeRow();

        // Clear customization section
        document.getElementById('customizationContainer').innerHTML = '';
        addCustomizationRow();
    }

    // --- RECIPE LOGIC ---

    function addRecipeRow() {
        const container = document.getElementById('recipeContainer');
        const rowId = 'recipe-row-' + Date.now();
        const div = document.createElement('div');
        div.className = 'dynamic-row';
        div.id = rowId;

        // Create category select options
        let catOptions = '<option value="">Select Category</option>';
        if (materialCategories && materialCategories.length > 0) {
            materialCategories.forEach(c => {
                catOptions += `<option value="${c.category_id}">${c.category_name}</option>`;
            });
        }

        div.innerHTML = `
            <select onchange="onRecipeCategoryChange(this)" class="recipe-cat-select">
                ${catOptions}
            </select>
            <select class="recipe-mat-select" style="display:none" onchange="onMaterialChange(this)">
                <option value="">Select Material</option>
            </select>
            <input type="number" class="recipe-qty" placeholder="Quantity" style="display:none" min="0" step="0.01">
            <div class="unit-display" style="display:none">-</div>
            <button type="button" class="btn-remove" onclick="removeRow('${rowId}')">×</button>
        `;

        // Remove delete button from first row only
        if (container.children.length === 0) {
            const removeBtn = div.querySelector('.btn-remove');
            if (removeBtn) {
                removeBtn.remove();
            }
        }

        container.appendChild(div);
    }

    function onRecipeCategoryChange(selectElem) {
        const row = selectElem.parentNode;
        const matSelect = row.querySelector('.recipe-mat-select');
        const qtyInput = row.querySelector('.recipe-qty');
        const unitDiv = row.querySelector('.unit-display');

        // Reset subsequent fields
        matSelect.style.display = 'none';
        qtyInput.style.display = 'none';
        unitDiv.style.display = 'none';
        matSelect.innerHTML = '<option value="">Select Material</option>';

        const catId = selectElem.value;
        if (!catId) return;

        // Fetch materials
        fetch(`get_materials_data.php?action=get_materials_by_category&category_id=${catId}`)
            .then(res => res.json())
            .then(data => {
                if (data.success && data.data) {
                    data.data.forEach(m => {
                        const opt = document.createElement('option');
                        opt.value = m.mid;
                        opt.textContent = m.mname;
                        opt.dataset.unit = m.unit;
                        matSelect.appendChild(opt);
                    });
                    matSelect.style.display = 'block';
                }
            })
            .catch(error => {
                console.error('Error loading materials:', error);
            });
    }

    function onMaterialChange(selectElem) {
        const row = selectElem.parentNode;
        const qtyInput = row.querySelector('.recipe-qty');
        const unitDiv = row.querySelector('.unit-display');

        const selectedOpt = selectElem.options[selectElem.selectedIndex];
        if (selectedOpt.value) {
            unitDiv.textContent = selectedOpt.dataset.unit || '';
            qtyInput.style.display = 'block';
            unitDiv.style.display = 'block';
        } else {
            qtyInput.style.display = 'none';
            unitDiv.style.display = 'none';
        }
    }

    // --- CUSTOMIZATION LOGIC ---

    function updateGroupDropdown(selectElem) {
        if (!selectElem) return;

        let opts = '<option value="">Select Group</option>';
        if (customizationGroups && customizationGroups.length > 0) {
            customizationGroups.forEach(g => {
                opts += `<option value="${g.group_id}">${g.group_type} (${g.group_name})</option>`;
            });
        }
        selectElem.innerHTML = opts;
    }

    function addCustomizationRow() {
        const container = document.getElementById('customizationContainer');
        const rowId = 'cust-row-' + Date.now();
        const div = document.createElement('div');
        div.className = 'dynamic-row';
        div.id = rowId;

        div.innerHTML = `
            <div style="width: 100%;">
                <select onchange="onGroupChange(this)" class="cust-group-select" style="width: 100%; margin-bottom: 10px;"></select>
                <div class="cust-values-table-container" style="display:none; width:100%; margin-top:10px;"></div>
                <div class="cust-max-container" style="display:none; align-items:center; gap:10px; margin-top:10px;">
                    <label style="white-space: nowrap;">Max:</label>
                    <input type="number" class="cust-max-select" placeholder="Max Selections" min="1" value="1" style="width:100px;">
                </div>
            </div>
            <button type="button" class="btn-remove" onclick="removeRow('${rowId}')">×</button>
        `;

        // Remove delete button from first row only
        if (container.children.length === 0) {
            const removeBtn = div.querySelector('.btn-remove');
            if (removeBtn) {
                removeBtn.remove();
            }
        }

        container.appendChild(div);

        // Populate options
        const select = div.querySelector('.cust-group-select');
        updateGroupDropdown(select);
    }

    function onGroupChange(selectElem) {
        const row = selectElem.closest('.dynamic-row');
        const tableContainer = row.querySelector('.cust-values-table-container');
        const maxContainer = row.querySelector('.cust-max-container');

        const groupId = selectElem.value;
        if (!groupId) {
            tableContainer.style.display = 'none';
            maxContainer.style.display = 'none';
            return;
        }

        const selectedGroup = customizationGroups.find(g => g.group_id == groupId);
        if (!selectedGroup) {
            tableContainer.style.display = 'none';
            maxContainer.style.display = 'none';
            return;
        }

        fetch(`manage_customization.php?action=get_group_values&group_id=${groupId}`)
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    let valuesHTML = `
                        <div style="margin-bottom:5px; font-weight:bold; color:#333;">${selectedGroup.group_name} Values:</div>
                        <div style="padding: 8px; background: #f8f9fa; border: 1px solid #ddd; border-radius: 4px;">
                    `;

                    const values = data.data ? data.data.split(',').map(v => v.trim()).filter(v => v) : [];
                    if (values.length > 0) {
                        valuesHTML += '<ul style="margin: 0; padding-left: 20px;">';
                        values.forEach(value => {
                            valuesHTML += `<li style="margin-bottom: 3px;">${value}</li>`;
                        });
                        valuesHTML += '</ul>';
                    } else {
                        valuesHTML += '<div style="color: #666; font-style: italic;">No values defined</div>';
                    }

                    valuesHTML += '</div>';

                    tableContainer.innerHTML = valuesHTML;
                    tableContainer.style.display = 'block';
                    maxContainer.style.display = 'flex';
                }
            })
            .catch(error => {
                console.error('Error loading group values:', error);
                tableContainer.innerHTML = '<div style="color: #d32f2f;">Error loading group values</div>';
                tableContainer.style.display = 'block';
            });
    }

    // --- GENERAL ROW REMOVAL ---
    function removeRow(id) {
        const el = document.getElementById(id);
        if (el) {
            el.remove();
        }
    }

    // --- MODAL LOGIC ---
    function openCustomizationModal() {
        console.log('Opening customization modal');
        document.getElementById('customModal').style.display = 'block';
        switchTab('create'); // Default tab
    }

    function closeCustomizationModal() {
        console.log('Closing customization modal');
        document.getElementById('customModal').style.display = 'none';
        // Reload groups on close to ensure main form is up to date
        loadCustomizationGroups().then(() => {
            console.log('Customization groups reloaded');
            updateAllGroupDropdowns();
        }).catch(error => {
            console.error('Failed to reload customization groups:', error);
        });
    }

    function updateAllGroupDropdowns() {
        const allDropdowns = document.querySelectorAll('.cust-group-select');
        console.log('Updating all group dropdowns, found:', allDropdowns.length);

        allDropdowns.forEach(dropdown => {
            updateGroupDropdown(dropdown);
        });
    }

    function switchTab(tab) {
        console.log('Switching to tab:', tab);
        document.querySelectorAll('.modal-tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.modal-section').forEach(s => s.classList.remove('active'));

        if (tab === 'create') {
            document.querySelector('.modal-tab:first-child').classList.add('active');
            document.getElementById('createSection').classList.add('active');
            // Reset Create Form
            document.getElementById('newGroupType').value = '';
            document.getElementById('newGroupName').value = '';
            document.getElementById('newValueContainer').innerHTML = '';
            addNewValueRow(); // Add one initial value row
        } else {
            document.querySelector('.modal-tab:last-child').classList.add('active');
            document.getElementById('deleteSection').classList.add('active');
            loadUnusedGroups();
        }
    }

    function addNewValueRow() {
        const container = document.getElementById('newValueContainer');
        const div = document.createElement('div');
        div.className = 'value-input-group';
        div.innerHTML = `
            <input type="text" class="new-value-input" placeholder="Value Name">
            <button type="button" class="btn-remove" onclick="this.parentElement.remove()">×</button>
        `;
        container.appendChild(div);
    }

    function submitNewGroup() {
        const type = document.getElementById('newGroupType').value.trim();
        const name = document.getElementById('newGroupName').value.trim();
        const valueInputs = document.querySelectorAll('.new-value-input');
        const values = [];

        valueInputs.forEach(input => {
            if(input.value.trim()) {
                values.push(input.value.trim());
            }
        });

        if (!name || values.length === 0) {
            alert("Please provide a Group Name and at least one Value.");
            return;
        }

        if (!type) {
            alert("Please provide a Group Type.");
            return;
        }

        fetch('manage_customization.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                action: 'create_group',
                type: type,
                name: name,
                values: values
            })
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    alert('Group Created!');
                    // Clear form
                    document.getElementById('newGroupType').value = '';
                    document.getElementById('newGroupName').value = '';
                    document.getElementById('newValueContainer').innerHTML = '';
                    addNewValueRow();
                    closeCustomizationModal();

                    // Reload groups to update the main form
                    loadCustomizationGroups().then(() => {
                        console.log('Groups reloaded after creation');
                        updateAllGroupDropdowns();
                    });
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error creating group:', error);
                alert('Network error: ' + error);
            });
    }

    function loadUnusedGroups() {
        const list = document.getElementById('unusedGroupsList');
        list.innerHTML = 'Loading...';

        fetch('manage_customization.php?action=get_unused_groups')
            .then(res => res.json())
            .then(data => {
                list.innerHTML = '';
                if (!data.success || data.data.length === 0) {
                    list.innerHTML = '<p>No unused groups available to delete.</p>';
                    return;
                }

                data.data.forEach(g => {
                    const item = document.createElement('div');
                    item.className = 'unused-group-item';
                    // 修改顯示：只顯示 group_type
                    item.innerHTML = `
                        <span>${g.group_type}</span>
                        <input type="checkbox" class="delete-group-check" value="${g.group_id}">
                    `;
                    item.onclick = function(e) {
                        if (e.target.type !== 'checkbox') {
                            const cb = this.querySelector('input');
                            cb.checked = !cb.checked;
                        }
                        this.classList.toggle('selected', this.querySelector('input').checked);
                    };
                    list.appendChild(item);
                });
            })
            .catch(error => {
                console.error('Error loading unused groups:', error);
                list.innerHTML = '<p>Error loading groups.</p>';
            });
    }

    function submitDeleteGroups() {
        const checks = document.querySelectorAll('.delete-group-check:checked');
        if (checks.length === 0) {
            alert('Please select at least one group to delete.');
            return;
        }

        const ids = Array.from(checks).map(c => c.value);

        if (!confirm(`Delete ${ids.length} selected group(s)?`)) return;

        fetch('manage_customization.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                action: 'delete_groups',
                group_ids: ids
            })
        })
            .then(res => res.json())
            .then(data => {
                if(data.success) {
                    alert('Groups Deleted');
                    loadUnusedGroups(); // Refresh list
                    // Reload groups to update the main form
                    loadCustomizationGroups().then(() => {
                        console.log('Groups reloaded after deletion');
                    });
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error deleting groups:', error);
                alert('Network error: ' + error);
            });
    }
</script>
<script>
    // Mobile menu functionality
    document.addEventListener('DOMContentLoaded', function() {
        const hamburger = document.getElementById('hamburgerMenu');
        const mainNav = document.querySelector('.main-nav');
        const userActions = document.querySelector('.user-actions');

        if (hamburger) {
            hamburger.addEventListener('click', function() {
                hamburger.classList.toggle('active');
                mainNav.classList.toggle('active');
                // Prevent background scrolling
                document.body.style.overflow = mainNav.classList.contains('active') ? 'hidden' : '';
            });
        }

        // Close menu when menu item is clicked
        document.querySelectorAll('.nav-button').forEach(button => {
            button.addEventListener('click', function() {
                if (hamburger) hamburger.classList.remove('active');
                if (mainNav) mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        // Close menu when user action link is clicked
        document.querySelectorAll('.user-actions a').forEach(link => {
            link.addEventListener('click', function() {
                if (hamburger) hamburger.classList.remove('active');
                if (mainNav) mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        // Close menu when clicking outside
        document.addEventListener('click', function(e) {
            if (hamburger && !hamburger.contains(e.target) &&
                mainNav && !mainNav.contains(e.target) &&
                userActions && !userActions.contains(e.target)) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });

        // Close menu with ESC key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && mainNav && mainNav.classList.contains('active')) {
                if (hamburger) hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });
    });
</script>
</body>
</html>