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

// If not processing dish addition request, continue to display page
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

// Fetch menu categories (original code)
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
<div class="back-link">
    <a href="<?= htmlspecialchars($page) ?>" class="back-link-text"> &#60; Back</a>
</div>
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
    });

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
    // Modify the success handling part in the confirmForm function
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
                imageUrl = 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/default.jpg ';
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
        return {
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
</script>
<script>
    // Mobile menu functionality
    document.addEventListener('DOMContentLoaded', function() {
        const hamburger = document.getElementById('hamburgerMenu');
        const mainNav = document.querySelector('.main-nav');
        const userActions = document.querySelector('.user-actions');

        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            mainNav.classList.toggle('active');
            // Prevent background scrolling
            document.body.style.overflow = mainNav.classList.contains('active') ? 'hidden' : '';
        });

        // Close menu when menu item is clicked
        document.querySelectorAll('.nav-button').forEach(button => {
            button.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        // Close menu when user action link is clicked
        document.querySelectorAll('.user-actions a').forEach(link => {
            link.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        // Close menu when clicking outside
        document.addEventListener('click', function(e) {
            if (!hamburger.contains(e.target) && !mainNav.contains(e.target) && !userActions.contains(e.target)) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });

        // Close menu with ESC key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && mainNav.classList.contains('active')) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });
    });
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
    }
</script>
</body>
</html>