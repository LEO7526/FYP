<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

$categories = [];
$menuItems = [];
$packages = [];

$categoryQuery = "SELECT * FROM menu_category";
$categoryResult = mysqli_query($conn, $categoryQuery);
while ($row = mysqli_fetch_assoc($categoryResult)) {
    $categories[] = $row;
}

$menuQuery = "SELECT mi.item_id, mi.image_url, mit.item_name, mc.category_name, mi.item_price 
              FROM menu_item mi 
              JOIN menu_item_translation mit ON mi.item_id = mit.item_id 
              JOIN menu_category mc ON mi.category_id = mc.category_id 
              WHERE mit.language_code = 'en' AND mi.is_available = TRUE";
$menuResult = mysqli_query($conn, $menuQuery);
while ($row = mysqli_fetch_assoc($menuResult)) {
    $menuItems[$row['category_name']][] = $row;
}

$packageQuery = "SELECT package_id, package_name, package_image_url FROM menu_package";
$packageResult = mysqli_query($conn, $packageQuery);
while ($row = mysqli_fetch_assoc($packageResult)) {
    $packages[] = $row;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $conn->begin_transaction();

    try {
        $points_required = $_POST['points_required'] ?? 0;
        $type = $_POST['discount_type'];
        $discount_amount = $_POST['discount_value'] ?? 0;
        $expiry_date = $_POST['expiry_date'];
        $is_active = 1;

        $stmt = $conn->prepare("INSERT INTO coupons (points_required, type, discount_amount, expiry_date, is_active) 
                              VALUES (?, ?, ?, ?, ?)");
        $stmt->bind_param("isssi", $points_required, $type, $discount_amount, $expiry_date, $is_active);
        $stmt->execute();
        $coupon_id = $conn->insert_id;
        $stmt->close();

        $applies_to = $_POST['applies_to'];
        $discount_type = $_POST['discount_type'];
        $discount_value = $_POST['discount_value'] ?? 0;

        $min_spend = (isset($_POST['use_min_spend']) && $_POST['use_min_spend'] === 'on') ? ($_POST['min_spend'] ?? null) : null;
        $max_discount = (isset($_POST['use_max_discount']) && $_POST['use_max_discount'] === 'on') ? ($_POST['max_discount'] ?? null) : null;
        $per_customer_per_day = (isset($_POST['use_per_customer_per_day']) && $_POST['use_per_customer_per_day'] === 'on') ? ($_POST['per_customer_per_day'] ?? null) : null;

        $valid_dine_in = isset($_POST['valid_dine_in']) ? 1 : 0;
        $valid_takeaway = isset($_POST['valid_takeaway']) ? 1 : 0;
        $valid_delivery = isset($_POST['valid_delivery']) ? 1 : 0;
        $combine_with_other_discounts = isset($_POST['combine_with_other_discounts']) ? 1 : 0;
        $birthday_only = isset($_POST['birthday_only']) ? 1 : 0;

        $stmt = $conn->prepare("INSERT INTO coupon_rules (coupon_id, applies_to, discount_type, discount_value, 
                              min_spend, max_discount, per_customer_per_day, valid_dine_in, valid_takeaway, 
                              valid_delivery, combine_with_other_discounts, birthday_only) 
                              VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("issddidiiiii", $coupon_id, $applies_to, $discount_type, $discount_value,
            $min_spend, $max_discount, $per_customer_per_day, $valid_dine_in, $valid_takeaway,
            $valid_delivery, $combine_with_other_discounts, $birthday_only);
        $stmt->execute();
        $stmt->close();

        $languages = ['en', 'zh-CN', 'zh-TW'];
        foreach ($languages as $lang) {
            $title = $_POST["title_{$lang}"] ?? '';
            $description = $_POST["description_{$lang}"] ?? '';

            if (!empty($title)) {
                $stmt = $conn->prepare("INSERT INTO coupon_translation (coupon_id, language_code, title, description) 
                                      VALUES (?, ?, ?, ?)");
                $stmt->bind_param("isss", $coupon_id, $lang, $title, $description);
                $stmt->execute();
                $stmt->close();
            }
        }

        foreach ($languages as $lang) {
            $terms = $_POST["terms_{$lang}"] ?? [];
            foreach ($terms as $term_text) {
                if (!empty($term_text)) {
                    $stmt = $conn->prepare("INSERT INTO coupon_terms (coupon_id, language_code, term_text) 
                                          VALUES (?, ?, ?)");
                    $stmt->bind_param("iss", $coupon_id, $lang, $term_text);
                    $stmt->execute();
                    $stmt->close();
                }
            }
        }

        if ($applies_to == 'category' && !empty($_POST['categories'])) {
            foreach ($_POST['categories'] as $category_id) {
                $stmt = $conn->prepare("INSERT INTO coupon_applicable_categories (coupon_id, category_id) 
                                      VALUES (?, ?)");
                $stmt->bind_param("ii", $coupon_id, $category_id);
                $stmt->execute();
                $stmt->close();
            }
        } elseif ($applies_to == 'item' && !empty($_POST['selected_items'])) {
            $item_ids = explode(',', $_POST['selected_items']);
            foreach ($item_ids as $item_id) {
                if (!empty($item_id)) {
                    $stmt = $conn->prepare("INSERT INTO coupon_applicable_items (coupon_id, item_id) 
                                          VALUES (?, ?)");
                    $stmt->bind_param("ii", $coupon_id, $item_id);
                    $stmt->execute();
                    $stmt->close();
                }
            }
        } elseif ($applies_to == 'package' && !empty($_POST['selected_packages'])) {
            $package_ids = explode(',', $_POST['selected_packages']);
            foreach ($package_ids as $package_id) {
                if (!empty($package_id)) {
                    $stmt = $conn->prepare("INSERT INTO coupon_applicable_package (coupon_id, package_id) 
                                          VALUES (?, ?)");
                    $stmt->bind_param("ii", $coupon_id, $package_id);
                    $stmt->execute();
                    $stmt->close();
                }
            }
        }

        $conn->commit();
        $success = "Coupon created successfully!";
    } catch (Exception $e) {
        $conn->rollback();
        $error = "Error creating coupon: " . $e->getMessage();
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create New Coupon - Yummy Restaurant</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/newCoupon.css">
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

<div class="container">
    <h1>Create New Coupon</h1>

    <?php if (isset($success)): ?>
        <div style="color: green; margin-bottom: 20px;"><?php echo $success; ?></div>
    <?php endif; ?>

    <?php if (isset($error)): ?>
        <div style="color: red; margin-bottom: 20px;"><?php echo $error; ?></div>
    <?php endif; ?>

    <form method="post" action="" id="couponForm">
        <h2>Basic Information</h2>

        <div class="form-group">
            <label for="points_required">Points Required</label>
            <input type="number" id="points_required" name="points_required" min="0" value="0">
        </div>

        <div class="form-group">
            <label for="expiry_date">Expiry Date</label>
            <input type="date" id="expiry_date" name="expiry_date" required>
        </div>

        <div class="form-group">
            <label>Discount Type</label>
            <div class="radio-group">
                <div class="radio-item">
                    <input type="radio" id="discount_percent" name="discount_type" value="percent" checked>
                    <label for="discount_percent">Percentage Off</label>
                </div>
                <div class="radio-item">
                    <input type="radio" id="discount_cash" name="discount_type" value="cash">
                    <label for="discount_cash">Fixed Amount Off</label>
                </div>
                <div class="radio-item">
                    <input type="radio" id="discount_free" name="discount_type" value="free_item">
                    <label for="discount_free">Free Item</label>
                </div>
            </div>
        </div>

        <div class="form-group">
            <label for="discount_value">Discount Value</label>
            <input type="number" id="discount_value" name="discount_value" min="0" step="0.01" required>
            <small id="discount_value_help">For percentage, enter a number between 1-100. For fixed amount, enter the amount in HKD.</small>
        </div>

        <div class="form-group">
            <label>Applicable To</label>
            <div class="radio-group">
                <div class="radio-item">
                    <input type="radio" id="applies_whole" name="applies_to" value="whole_order" checked>
                    <label for="applies_whole">Whole Order</label>
                </div>
                <div class="radio-item">
                    <input type="radio" id="applies_category" name="applies_to" value="category">
                    <label for="applies_category">Specific Categories</label>
                </div>
                <div class="radio-item">
                    <input type="radio" id="applies_item" name="applies_to" value="item">
                    <label for="applies_item">Specific Items</label>
                </div>
                <div class="radio-item">
                    <input type="radio" id="applies_package" name="applies_to" value="package">
                    <label for="applies_package">Specific Packages</label>
                </div>
            </div>
        </div>

        <div id="category_section" class="scope-section hidden">
            <h3>Select Categories</h3>
            <div class="checkbox-group">
                <?php foreach ($categories as $category): ?>
                    <div class="checkbox-item">
                        <input type="checkbox" id="category_<?php echo $category['category_id']; ?>"
                               name="categories[]" value="<?php echo $category['category_id']; ?>">
                        <label for="category_<?php echo $category['category_id']; ?>">
                            <?php echo $category['category_name']; ?>
                        </label>
                    </div>
                <?php endforeach; ?>
            </div>
        </div>

        <div id="item_section" class="scope-section hidden">
            <h3>Select Items</h3>
            <button type="button" class="btn-secondary" id="openItemModal">+ Select Items</button>
            <div class="selected-items-list" id="selectedItemsList">
            </div>
            <input type="hidden" name="selected_items" id="selectedItemsInput">
        </div>

        <div id="package_section" class="scope-section hidden">
            <h3>Select Packages</h3>
            <button type="button" class="btn-secondary" id="openPackageModal">+ Select Packages</button>
            <div class="selected-packages-list" id="selectedPackagesList">
            </div>
            <input type="hidden" name="selected_packages" id="selectedPackagesInput">
        </div>

        <h2>Usage Rules</h2>

        <div class="form-group usage-rule-group">
            <div class="usage-rule-item">
                <div class="checkbox-item">
                    <input type="checkbox" id="use_min_spend" name="use_min_spend">
                    <label for="use_min_spend">Minimum Spend (HKD)</label>
                </div>
                <div id="min_spend_container" class="hidden">
                    <input type="number" id="min_spend" name="min_spend" min="0" step="0.01" placeholder="Enter minimum spend">
                </div>
            </div>
        </div>

        <div class="form-group usage-rule-group">
            <div class="usage-rule-item">
                <div class="checkbox-item">
                    <input type="checkbox" id="use_max_discount" name="use_max_discount">
                    <label for="use_max_discount">Maximum Discount (HKD)</label>
                </div>
                <div id="max_discount_container" class="hidden">
                    <input type="number" id="max_discount" name="max_discount" min="0" step="0.01" placeholder="Enter maximum discount">
                </div>
            </div>
        </div>

        <div class="form-group usage-rule-group">
            <div class="usage-rule-item">
                <div class="checkbox-item">
                    <input type="checkbox" id="use_per_customer_per_day" name="use_per_customer_per_day">
                    <label for="use_per_customer_per_day">Usage Limit Per Customer Per Day</label>
                </div>
                <div id="per_customer_container" class="hidden">
                    <input type="number" id="per_customer_per_day" name="per_customer_per_day" min="1" value="1" placeholder="Enter usage limit">
                </div>
            </div>
        </div>

        <div class="form-group">
            <label>Valid For</label>
            <div class="checkbox-group">
                <div class="checkbox-item">
                    <input type="checkbox" id="valid_dine_in" name="valid_dine_in" checked>
                    <label for="valid_dine_in">Dine-in</label>
                </div>
                <div class="checkbox-item">
                    <input type="checkbox" id="valid_takeaway" name="valid_takeaway" checked>
                    <label for="valid_takeaway">Takeaway</label>
                </div>
                <div class="checkbox-item">
                    <input type="checkbox" id="valid_delivery" name="valid_delivery">
                    <label for="valid_delivery">Delivery</label>
                </div>
            </div>
        </div>

        <div class="form-group">
            <div class="checkbox-item">
                <input type="checkbox" id="combine_with_other_discounts" name="combine_with_other_discounts">
                <label for="combine_with_other_discounts">Can be combined with other discounts</label>
            </div>
        </div>

        <div class="form-group">
            <div class="checkbox-item">
                <input type="checkbox" id="birthday_only" name="birthday_only">
                <label for="birthday_only">Valid only for birthday</label>
            </div>
        </div>

        <h2>Translations</h2>

        <div class="language-tabs">
            <button type="button" class="language-tab active" data-lang="en">English</button>
            <button type="button" class="language-tab" data-lang="zh-CN">Chinese (Simplified)</button>
            <button type="button" class="language-tab" data-lang="zh-TW">Chinese (Traditional)</button>
        </div>

        <div class="language-content active" data-lang="en">
            <div class="form-group">
                <label for="title_en">Title (English)</label>
                <input type="text" id="title_en" name="title_en" required>
            </div>

            <div class="form-group">
                <label for="description_en">Description (English)</label>
                <textarea id="description_en" name="description_en"></textarea>
            </div>

            <div class="form-group">
                <label>Terms & Conditions (English)</label>
                <div id="terms_en_container" class="terms-container">
                    <div class="term-item" data-index="0">
                        <input type="text" name="terms_en[]" placeholder="Enter term in English" class="term-input" required>
                        <button type="button" class="btn btn-secondary remove-term">- Remove</button>
                    </div>
                </div>
                <button type="button" class="btn btn-secondary add-term" data-lang="en">+ Add Term</button>
            </div>
        </div>

        <div class="language-content" data-lang="zh-CN">
            <div class="form-group">
                <label for="title_zh-CN">标题 (简体中文)</label>
                <input type="text" id="title_zh-CN" name="title_zh-CN" required>
            </div>

            <div class="form-group">
                <label for="description_zh-CN">描述 (简体中文)</label>
                <textarea id="description_zh-CN" name="description_zh-CN"></textarea>
            </div>

            <div class="form-group">
                <label>条款和条件 (简体中文)</label>
                <div id="terms_zh-CN_container" class="terms-container">
                    <div class="term-item" data-index="0">
                        <input type="text" name="terms_zh-CN[]" placeholder="输入条款" class="term-input" required>
                        <button type="button" class="btn btn-secondary remove-term">- 删除</button>
                    </div>
                </div>
                <button type="button" class="btn btn-secondary add-term" data-lang="zh-CN">+ 添加条款</button>
            </div>
        </div>

        <div class="language-content" data-lang="zh-TW">
            <div class="form-group">
                <label for="title_zh-TW">標題 (繁體中文)</label>
                <input type="text" id="title_zh-TW" name="title_zh-TW" required>
            </div>

            <div class="form-group">
                <label for="description_zh-TW">描述 (繁體中文)</label>
                <textarea id="description_zh-TW" name="description_zh-TW"></textarea>
            </div>

            <div class="form-group">
                <label>條款和條件 (繁體中文)</label>
                <div id="terms_zh-TW_container" class="terms-container">
                    <div class="term-item" data-index="0">
                        <input type="text" name="terms_zh-TW[]" placeholder="輸入條款" class="term-input" required>
                        <button type="button" class="btn btn-secondary remove-term">- 刪除</button>
                    </div>
                </div>
                <button type="button" class="btn btn-secondary add-term" data-lang="zh-TW">+ 新增條款</button>
            </div>
        </div>

        <div class="buttons">
            <button type="submit" class="btn btn-primary">Create Coupon</button>
            <button type="reset" class="btn btn-secondary">Reset</button>
        </div>
    </form>
</div>

<div id="itemModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h2>Select Items</h2>
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
                                 data-item-name="<?php echo htmlspecialchars($item['item_name']); ?>"
                                 data-item-image="<?php echo htmlspecialchars($item['image_url']); ?>">
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

<div id="packageModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h2>Select Packages</h2>
            <span class="close">&times;</span>
        </div>
        <div class="modal-body">
            <div class="packages-grid">
                <?php foreach ($packages as $package): ?>
                    <div class="package-item"
                         data-package-id="<?php echo $package['package_id']; ?>"
                         data-package-name="<?php echo htmlspecialchars($package['package_name']); ?>"
                         data-package-image="<?php echo htmlspecialchars($package['package_image_url']); ?>">
                        <img src="<?php echo htmlspecialchars($package['package_image_url']); ?>"
                             alt="<?php echo htmlspecialchars($package['package_name']); ?>">
                        <span><?php echo htmlspecialchars($package['package_name']); ?></span>
                    </div>
                <?php endforeach; ?>
            </div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const hamburger = document.getElementById('hamburgerMenu');
        const mainNav = document.querySelector('.main-nav');
        const userActions = document.querySelector('.user-actions');

        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            mainNav.classList.toggle('active');
            document.body.style.overflow = mainNav.classList.contains('active') ? 'hidden' : '';
        });

        document.querySelectorAll('.nav-button, .user-actions a').forEach(item => {
            item.addEventListener('click', function() {
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

        const languageTabs = document.querySelectorAll('.language-tab');
        const languageContents = document.querySelectorAll('.language-content');

        languageTabs.forEach(tab => {
            tab.addEventListener('click', function() {
                const lang = this.getAttribute('data-lang');

                languageTabs.forEach(t => t.classList.remove('active'));
                this.classList.add('active');

                languageContents.forEach(content => {
                    content.classList.remove('active');
                    if (content.getAttribute('data-lang') === lang) {
                        content.classList.add('active');
                    }
                });
            });
        });

        const appliesToRadios = document.querySelectorAll('input[name="applies_to"]');
        const categorySection = document.getElementById('category_section');
        const itemSection = document.getElementById('item_section');
        const packageSection = document.getElementById('package_section');

        appliesToRadios.forEach(radio => {
            radio.addEventListener('change', function() {
                categorySection.classList.add('hidden');
                itemSection.classList.add('hidden');
                packageSection.classList.add('hidden');

                if (this.value === 'category') {
                    categorySection.classList.remove('hidden');
                } else if (this.value === 'item') {
                    itemSection.classList.remove('hidden');
                } else if (this.value === 'package') {
                    packageSection.classList.remove('hidden');
                }
            });
        });

        const discountTypeRadios = document.querySelectorAll('input[name="discount_type"]');
        const discountValueHelp = document.getElementById('discount_value_help');

        discountTypeRadios.forEach(radio => {
            radio.addEventListener('change', updateDiscountHelp);
        });

        function updateDiscountHelp() {
            const selectedType = document.querySelector('input[name="discount_type"]:checked').value;

            if (selectedType === 'percent') {
                discountValueHelp.textContent = 'Enter a number between 1-100 (e.g., 10 for 10%)';
            } else if (selectedType === 'cash') {
                discountValueHelp.textContent = 'Enter the fixed amount in HKD (e.g., 50 for HK$50)';
            } else if (selectedType === 'free_item') {
                discountValueHelp.textContent = 'Enter 1 for one free item';
            }
        }

        updateDiscountHelp();

        const useMinSpend = document.getElementById('use_min_spend');
        const minSpendContainer = document.getElementById('min_spend_container');
        const minSpendInput = document.getElementById('min_spend');

        const useMaxDiscount = document.getElementById('use_max_discount');
        const maxDiscountContainer = document.getElementById('max_discount_container');
        const maxDiscountInput = document.getElementById('max_discount');

        const usePerCustomer = document.getElementById('use_per_customer_per_day');
        const perCustomerContainer = document.getElementById('per_customer_container');
        const perCustomerInput = document.getElementById('per_customer_per_day');

        updateFieldVisibility(useMinSpend, minSpendContainer, minSpendInput);
        updateFieldVisibility(useMaxDiscount, maxDiscountContainer, maxDiscountInput);
        updateFieldVisibility(usePerCustomer, perCustomerContainer, perCustomerInput);

        useMinSpend.addEventListener('change', function() {
            updateFieldVisibility(this, minSpendContainer, minSpendInput);
        });

        useMaxDiscount.addEventListener('change', function() {
            updateFieldVisibility(this, maxDiscountContainer, maxDiscountInput);
        });

        usePerCustomer.addEventListener('change', function() {
            updateFieldVisibility(this, perCustomerContainer, perCustomerInput);
        });

        function updateFieldVisibility(checkbox, container, input) {
            if (checkbox.checked) {
                container.classList.remove('hidden');
                input.disabled = false;
            } else {
                container.classList.add('hidden');
                input.disabled = true;
                input.value = '';
            }
        }

        const termIndices = {
            'en': 1,
            'zh-CN': 1,
            'zh-TW': 1
        };

        document.querySelectorAll('.add-term').forEach(btn => {
            btn.addEventListener('click', function() {
                const lang = this.getAttribute('data-lang');
                addTermToAllLanguages();
            });
        });

        document.addEventListener('click', function(e) {
            if (e.target.classList.contains('remove-term')) {
                const termItem = e.target.closest('.term-item');
                const container = termItem.closest('.terms-container');
                const lang = container.id.split('_')[1].split('_')[0];

                const currentLangCount = container.querySelectorAll('.term-item').length;

                if (currentLangCount <= 1) {
                    alert('At least one term is required.');
                    return;
                }

                const indexToRemove = parseInt(termItem.getAttribute('data-index'));

                removeTermFromAllLanguages(indexToRemove);
            }
        });

        function addTermToAllLanguages() {
            const languages = ['en', 'zh-CN', 'zh-TW'];

            languages.forEach(lang => {
                const container = document.getElementById(`terms_${lang}_container`);
                const newIndex = termIndices[lang];

                const newTerm = document.createElement('div');
                newTerm.className = 'term-item';
                newTerm.setAttribute('data-index', newIndex);

                let placeholder = '';
                let removeText = '';

                switch(lang) {
                    case 'en':
                        placeholder = 'Enter term in English';
                        removeText = '- Remove';
                        break;
                    case 'zh-CN':
                        placeholder = '输入条款';
                        removeText = '- 删除';
                        break;
                    case 'zh-TW':
                        placeholder = '輸入條款';
                        removeText = '- 刪除';
                        break;
                }

                newTerm.innerHTML = `
                    <input type="text" name="terms_${lang}[]" placeholder="${placeholder}" class="term-input" required>
                    <button type="button" class="btn btn-secondary remove-term">${removeText}</button>
                `;

                container.appendChild(newTerm);
                termIndices[lang]++;
            });
        }

        function removeTermFromAllLanguages(indexToRemove) {
            const languages = ['en', 'zh-CN', 'zh-TW'];

            languages.forEach(lang => {
                const container = document.getElementById(`terms_${lang}_container`);
                const termToRemove = container.querySelector(`.term-item[data-index="${indexToRemove}"]`);
                if (termToRemove) {
                    termToRemove.remove();
                }

                const remainingTerms = container.querySelectorAll('.term-item');
                remainingTerms.forEach((term, newIndex) => {
                    term.setAttribute('data-index', newIndex);
                });

                termIndices[lang] = remainingTerms.length;
            });
        }

        const itemModal = document.getElementById('itemModal');
        const packageModal = document.getElementById('packageModal');
        const openItemModalBtn = document.getElementById('openItemModal');
        const openPackageModalBtn = document.getElementById('openPackageModal');
        const closeButtons = document.querySelectorAll('.close');

        let selectedItems = [];
        let selectedPackages = [];

        openItemModalBtn.addEventListener('click', function() {
            itemModal.style.display = 'block';
        });

        openPackageModalBtn.addEventListener('click', function() {
            packageModal.style.display = 'block';
        });

        closeButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                itemModal.style.display = 'none';
                packageModal.style.display = 'none';
            });
        });

        window.addEventListener('click', function(event) {
            if (event.target === itemModal) {
                itemModal.style.display = 'none';
            }
            if (event.target === packageModal) {
                packageModal.style.display = 'none';
            }
        });

        document.querySelectorAll('.dish-item').forEach(item => {
            item.addEventListener('click', function() {
                const itemId = this.getAttribute('data-item-id');
                const itemName = this.getAttribute('data-item-name');
                const itemImage = this.getAttribute('data-item-image');

                const existingItem = selectedItems.find(item => item.id === itemId);
                if (!existingItem) {
                    selectedItems.push({
                        id: itemId,
                        name: itemName,
                        image: itemImage
                    });
                    updateSelectedItemsDisplay();
                }
            });
        });

        document.querySelectorAll('.package-item').forEach(pkg => {
            pkg.addEventListener('click', function() {
                const packageId = this.getAttribute('data-package-id');
                const packageName = this.getAttribute('data-package-name');
                const packageImage = this.getAttribute('data-package-image');

                const existingPackage = selectedPackages.find(pkg => pkg.id === packageId);
                if (!existingPackage) {
                    selectedPackages.push({
                        id: packageId,
                        name: packageName,
                        image: packageImage
                    });
                    updateSelectedPackagesDisplay();
                }
            });
        });

        function updateSelectedItemsDisplay() {
            const container = document.getElementById('selectedItemsList');
            const input = document.getElementById('selectedItemsInput');

            container.innerHTML = '';
            const itemIds = [];

            selectedItems.forEach(item => {
                const itemDiv = document.createElement('div');
                itemDiv.className = 'selected-item';
                itemDiv.innerHTML = `
                    <img src="${item.image}" alt="${item.name}">
                    <span>${item.name}</span>
                    <button type="button" class="btn-remove-item" data-id="${item.id}">×</button>
                `;
                container.appendChild(itemDiv);
                itemIds.push(item.id);
            });

            input.value = itemIds.join(',');
        }

        function updateSelectedPackagesDisplay() {
            const container = document.getElementById('selectedPackagesList');
            const input = document.getElementById('selectedPackagesInput');

            container.innerHTML = '';
            const packageIds = [];

            selectedPackages.forEach(pkg => {
                const pkgDiv = document.createElement('div');
                pkgDiv.className = 'selected-package';
                pkgDiv.innerHTML = `
                    <img src="${pkg.image}" alt="${pkg.name}">
                    <span>${pkg.name}</span>
                    <button type="button" class="btn-remove-package" data-id="${pkg.id}">×</button>
                `;
                container.appendChild(pkgDiv);
                packageIds.push(pkg.id);
            });

            input.value = packageIds.join(',');
        }

        document.getElementById('selectedItemsList').addEventListener('click', function(e) {
            if (e.target.classList.contains('btn-remove-item')) {
                const itemId = e.target.getAttribute('data-id');
                selectedItems = selectedItems.filter(item => item.id !== itemId);
                updateSelectedItemsDisplay();
            }
        });

        document.getElementById('selectedPackagesList').addEventListener('click', function(e) {
            if (e.target.classList.contains('btn-remove-package')) {
                const packageId = e.target.getAttribute('data-id');
                selectedPackages = selectedPackages.filter(pkg => pkg.id !== packageId);
                updateSelectedPackagesDisplay();
            }
        });

        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                this.classList.add('active');

                document.querySelectorAll('.dish-category').forEach(cat => {
                    cat.style.display = 'none';
                });

                const categoryName = this.getAttribute('data-category');
                const categoryId = 'category-' + categoryName.replace(' ', '-');
                const categoryElement = document.getElementById(categoryId);
                if (categoryElement) {
                    categoryElement.style.display = 'grid';
                }
            });
        });
    });
</script>
</body>
</html>