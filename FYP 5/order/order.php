<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>XXX Restaurant</title>
    <link rel="stylesheet" href="../Com.css">
    <link rel="stylesheet" href="order.css">
    <script>
        function openLogin() {
            document.getElementById("loginPopup").style.display = "flex";
        }

        function closeLogin() {
            document.getElementById("loginPopup").style.display = "none";
        }
    </script>

    <script>
        let currentPackageId = null;
        let selectedPackageItems = []; // 存儲選中的套餐項目
        let selectedPackageModifiers = {}; // 存儲選中項目的價格修改器 {item_id: price_modifier}

        function openSetPopup(packageId) {
            currentPackageId = packageId;
            document.getElementById("setPopup").style.display = "flex";

            fetch(`get_package_items.php?package_id=${packageId}`)
                .then(response => response.json())
                .then(data => {
                    const setOptions = document.getElementById("setOptions");
                    setOptions.innerHTML = "";

                    const items = data.items;
                    const limits = {};
                    data.limits.forEach(l => {
                        limits[l.type_id] = parseInt(l.optional_quantity);
                    });

                    // 分組
                    const grouped = {};
                    items.forEach(item => {
                        if (!grouped[item.type_id]) grouped[item.type_id] = [];
                        grouped[item.type_id].push(item);
                    });

                    // 輸出每組
                    for (const [typeId, items] of Object.entries(grouped)) {
                        const groupDiv = document.createElement("div");
                        groupDiv.classList.add("set-group");

                        // 直接用 SQL 查到的 type_name
                        const typeName = items[0].type_name || "其他";
                        const maxLimit = limits[typeId] || null;

                        // 標題顯示「已選 / 最多」
                        const titleEl = document.createElement("h4");
                        titleEl.innerText = `${typeName}${maxLimit ? ` (已選 0 / 最多 ${maxLimit} 項)` : ""}`;
                        groupDiv.appendChild(titleEl);

                        items.forEach(item => {
                            const option = document.createElement("div");
                            option.classList.add("set-option");
                            
                            // 显示价格修改器（如果有的话）
                            const priceDisplay = item.price_modifier > 0 ? ` (+$${parseFloat(item.price_modifier).toFixed(2)})` : 
                                                 item.price_modifier < 0 ? ` (-$${Math.abs(parseFloat(item.price_modifier)).toFixed(2)})` : '';
                            
                            option.innerHTML = `
                        <label>
                            <input type="checkbox" name="set_items_${typeId}[]" value="${item.item_id}" data-price-modifier="${item.price_modifier}">
                            <img src="${item.image_url}" alt="${item.item_name}" class="set-item-img">
                            <span class="set-item-name">${item.item_name}${priceDisplay}</span>
                        </label>
                    `;
                            groupDiv.appendChild(option);

                            // 为每个复选框添加 change 事件
                            const checkbox = option.querySelector("input[type='checkbox']");
                            checkbox.addEventListener("change", (e) => {
                                const checked = groupDiv.querySelectorAll("input[type='checkbox']:checked").length;
                                
                                // 如果该复选框被选中，且已达到最大数量，则取消选中
                                if (e.target.checked && maxLimit && checked > maxLimit) {
                                    e.target.checked = false;
                                    alert(`${typeName} 最多只能選 ${maxLimit} 項，請先取消一個選項`);
                                } else {
                                    // 更新标题计数
                                    const newChecked = groupDiv.querySelectorAll("input[type='checkbox']:checked").length;
                                    titleEl.innerText = `${typeName}${maxLimit ? ` (已選 ${newChecked} / 最多 ${maxLimit} 項)` : ""}`;
                                }
                            });
                        });

                        setOptions.appendChild(groupDiv);
                    }
                });
        }

        function closeSetPopup() {
            document.getElementById("setPopup").style.display = "none";
        }

        function saveSetSelection() {
            const form = document.getElementById("orderForm");
            let hiddenFields = document.getElementById("hidden-fields");

            // 如果 hidden-fields 不存在，就建立一個在 form 裡
            if (!hiddenFields) {
                hiddenFields = document.createElement("div");
                hiddenFields.id = "hidden-fields";
                form.appendChild(hiddenFields);
            }

            // 清空舊的隱藏欄位
            hiddenFields.innerHTML = "";

            // 抓取所有已勾選的 checkbox
            const selectedItems = Array.from(document.querySelectorAll("input[name^='set_items_']:checked"));

            // 保存到全局變量
            selectedPackageItems = selectedItems.map(input => input.value);
            
            // 保存价格修改器
            selectedPackageModifiers = {};
            selectedItems.forEach(input => {
                const priceModifier = parseFloat(input.getAttribute("data-price-modifier")) || 0;
                selectedPackageModifiers[input.value] = priceModifier;
            });

            // 添加套餐ID
            if (currentPackageId) {
                hiddenFields.innerHTML += `<input type="hidden" name="package_id" value="${currentPackageId}">`;
            }

            // 收集名稱和價格修改器
            const selectedNames = [];

            selectedItems.forEach(input => {
                // 加到隱藏欄位
                hiddenFields.innerHTML += `<input type="hidden" name="package_selected_items[]" value="${input.value}">`;
                
                // 添加價格修改器到隱藏欄位
                const priceModifier = parseFloat(input.getAttribute("data-price-modifier")) || 0;
                hiddenFields.innerHTML += `<input type="hidden" name="package_item_modifiers[]" value="${priceModifier}">`;

                // 收集名稱
                const itemName = input.parentElement.textContent.trim();
                selectedNames.push(itemName);
            });

            // 顯示在摘要
            const summaryList = document.getElementById("order-summary-list");
            if (summaryList) {
                summaryList.innerHTML = "";
                if (selectedNames.length > 0) {
                    const li = document.createElement("li");
                    li.textContent = `套餐：${selectedNames.join(", ")}`;
                    summaryList.appendChild(li);
                }
            }

            closeSetPopup();
            updateOrderSummary();
        }

    </script>
    <script>
        const spiceLevels = {};
        const quantities = {};
        const remarks = {};
        let currentSpiceItemId = null;


        function openSpicePopup(itemId) {
            currentSpiceItemId = itemId;
            document.getElementById("spicePopup").style.display = "flex";
            document.getElementById("spiceLevel").value = spiceLevels[itemId] || "不辣";
            document.getElementById("spiceQtyDisplay").innerText = quantities[itemId] || 1;
            document.getElementById("spiceRemark").value = remarks[itemId] || "";

            const dishCard = document.querySelector(`.dish-card[data-id='${itemId}'] img`);
            const popupImage = document.getElementById("popupDishImage");

            if (dishCard) {
                popupImage.src = dishCard.src;
                popupImage.alt = dishCard.alt;
            } else {
                popupImage.src = "";
                popupImage.alt = "Image not found";
            }
        }



        function closeSpicePopup() {
            document.getElementById("spicePopup").style.display = "none";
        }

        function saveSpice() {
            const level = document.getElementById("spiceLevel").value;
            const qty = parseInt(document.getElementById("spiceQtyDisplay").innerText);
            const remark = document.getElementById("spiceRemark").value;

            spiceLevels[currentSpiceItemId] = level;
            quantities[currentSpiceItemId] = qty;
            remarks[currentSpiceItemId] = remark;

            closeSpicePopup();
            updateOrderSummary();
        }

        function adjustQty(delta) {
            let qty = parseInt(document.getElementById("spiceQtyDisplay").innerText);
            qty = Math.max(1, qty + delta); // 最少為 1
            document.getElementById("spiceQtyDisplay").innerText = qty;
        }

        function updateOrderSummary() {
            const total = Object.values(quantities).reduce((sum, qty) => sum + qty, 0);
            const packageCount = (currentPackageId && selectedPackageItems.length > 0) ? 1 : 0;

            document.getElementById('total-count').innerText = total + packageCount;

            const summary = document.getElementById('order-summary');
            summary.style.display = (total > 0 || packageCount > 0) ? 'block' : 'none';
        }

        function submitOrder() {
            const hiddenFields = document.getElementById('hidden-fields');
            hiddenFields.innerHTML = '';

            // 單點菜品
            for (const [id, qty] of Object.entries(quantities)) {
                if (qty > 0) {
                    hiddenFields.innerHTML += `<input type="hidden" name="item_ids[]" value="${id}">`;
                    hiddenFields.innerHTML += `<input type="hidden" name="quantities[]" value="${qty}">`;
                    hiddenFields.innerHTML += `<input type="hidden" name="spice_levels[]" value="${spiceLevels[id] || '未選擇'}">`;
                    hiddenFields.innerHTML += `<input type="hidden" name="remarks[]" value="${remarks[id] || ''}">`;
                }
            }

            // 套餐ID
            if (currentPackageId) {
                hiddenFields.innerHTML += `<input type="hidden" name="package_id" value="${currentPackageId}">`;
            }

            // 套餐選項 - 使用全局變量而不是查詢 DOM
            selectedPackageItems.forEach(itemId => {
                hiddenFields.innerHTML += `<input type="hidden" name="package_selected_items[]" value="${itemId}">`;
                
                // 添加該項目的價格修改器
                const modifier = selectedPackageModifiers[itemId] || 0;
                hiddenFields.innerHTML += `<input type="hidden" name="package_item_modifiers[]" value="${modifier}">`;
            });

            document.getElementById('orderForm').submit();
        }

    </script>

    <script>
        function filterCategory(category) {
            const selected = category.trim().toLowerCase();

            const sections = document.querySelectorAll('.category-section');
            sections.forEach(section => {
                const cat = section.getAttribute('data-category').trim().toLowerCase();
                section.style.display = (selected === 'all' || cat === selected) ? 'block' : 'none';
            });

            const buttons = document.querySelectorAll('.category-btn');
            buttons.forEach(btn => {
                const btnText = btn.innerText.trim().toLowerCase();
                btn.classList.toggle('active', btnText === selected || (selected === 'all' && btnText === 'all'));
            });
        }
    </script>

</head>
<body>
<div class="header">
    <a href="../Index/home.php">
        Yummy Restaurant
    </a>
    <ul>
        <li><a href="../Index/menu.php">MENU</a></li>
        <li><a href="../Index/reservation.php">RESERVATION</a></li>
        <li><a href="../order/order.php">ORDER</a></li>
        <li>
            <a href="../Index/coupon.php">COUPON</a>
        </li>
    </ul>

    <?php
    if (isset($_COOKIE['user'])) {
        $username = ($_COOKIE['username']);
        echo '<div class="user-bar">
            <a class="username" href="profile.php">Hello,' . $username . '</a>
            <a href="../login/logout.php" class="logout-btn">LOGOUT</a>
          </div>';
    } else {
        echo '<a href="#" class="login-btn" onclick="openLogin()">LOGIN</a>';
    }
    ?>

    <div class="login-popup" id="loginPopup">
        <div class="login-box">
            <span class="close-btn" onclick="closeLogin()">&times;</span>
            <h2>Login</h2>
            <form method="post" action="../login/login.php">
                <label for="useremail">Email:</label>
                <input type="text" id="useremail" name="useremail" required>

                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required>

                <button type="submit">Login</button>
            </form>

            <!-- Sign Up button -->
            <div class="register-link">
                <p>Don't have an account? <a href="../register/register.php">Sign Up</a></p>
            </div>
        </div>
    </div>
</div>

<div class="category-buttons">
    <button onclick="filterCategory('all')" class="category-btn">All</button>
    <button onclick="filterCategory('appetizers')" class="category-btn">Appetizers</button>
    <button onclick="filterCategory('soup')" class="category-btn">Soup</button>
    <button onclick="filterCategory('maincourses')" class="category-btn">Main Courses</button>
    <button onclick="filterCategory('dessert')" class="category-btn">Dessert</button>
    <button onclick="filterCategory('drink')" class="category-btn">Drink</button>
    <button onclick="filterCategory('set')" class="category-btn">Set</button>
</div>



<?php
$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $stmt = $pdo->query("SELECT mi.image_url, mi.item_price, mi.item_id , mit.item_name, mi.category_id FROM menu_item mi , menu_item_translation mit where mi.item_id = mit.item_id and mit.language_code = 'en'");
    $dishes = $stmt->fetchAll();

    $stmt = $pdo->query("SELECT * FROM menu_package");
    $packages = $stmt->fetchAll();

    // Fetch package items
    $stmt = $pdo->query("
        SELECT pd.package_id, mit.item_name
        FROM package_dish pd
        JOIN menu_item_translation mit ON pd.item_id = mit.item_id AND mit.language_code = 'en'
    ");
    $packageItems = $stmt->fetchAll();

    // Group items by package_id
    $groupedPackageItems = [];
    foreach ($packageItems as $item) {
        $pid = $item['package_id'];
        if (!isset($groupedPackageItems[$pid])) $groupedPackageItems[$pid] = [];
        $groupedPackageItems[$pid][] = $item['item_name'];
    }
} catch (PDOException $e) {
    die("資料庫連線失敗：" . $e->getMessage());
}
?>
<div class="dish-container">
    <?php
    $categories = [
            1 => ['name' => 'Appetizers', 'key' => 'appetizers'],
            2 => ['name' => 'Soup', 'key' => 'soup'],
            3 => ['name' => 'Main Courses', 'key' => 'maincourses'],
            4 => ['name' => 'Dessert', 'key' => 'dessert'],
            5 => ['name' => 'Drink', 'key' => 'drink'],
    ];

    $groupedDishes = [];
    foreach ($dishes as $dish) {
        $catId = $dish['category_id'];
        if (!isset($groupedDishes[$catId])) {
            $groupedDishes[$catId] = [];
        }
        $groupedDishes[$catId][] = $dish;
    }
    ?>

    <?php foreach ($categories as $catId => $catInfo): ?>
        <?php if (!empty($groupedDishes[$catId])): ?>
            <div class="category-section" data-category="<?= $catInfo['key'] ?>">
                <h2 class="category-title"><?= $catInfo['name'] ?></h2>
                <div class="dish-container">
                    <?php foreach ($groupedDishes[$catId] as $dish): ?>
                        <div class="dish-card" data-id="<?= $dish['item_id'] ?>">
                            <h3><?= htmlspecialchars($dish['item_name']) ?></h3>
                            <img src="<?= htmlspecialchars($dish['image_url']) ?>" alt="<?= htmlspecialchars($dish['item_name']) ?>" onclick="openSpicePopup(<?= $dish['item_id'] ?>)">
                            <p>Price: $<?= htmlspecialchars($dish['item_price']) ?></p>
                        </div>
                    <?php endforeach; ?>
                </div>
            </div>
        <?php endif; ?>
    <?php endforeach; ?>
</div>
<div class="category-section" data-category="set">
    <h2 class="category-title">Set Menu</h2>
    <div class="dish-container">
        <?php foreach ($packages as $package): ?>
            <div class="dish-card" data-id="package-<?= $package['package_id'] ?>">
                <h3><?= htmlspecialchars($package['package_name']) ?></h3>
                <img src="<?= htmlspecialchars($package['package_image_url']) ?>"
                     alt="<?= htmlspecialchars($package['package_name']) ?>"
                     onclick="openSetPopup(<?= $package['package_id'] ?>)">
                <p>Price: $<?= htmlspecialchars($package['amounts']) ?></p>
            </div>
        <?php endforeach; ?>
    </div>
</div>

<div class="set-popup" id="setPopup" style="display:none;">
    <div class="set-box">
        <span class="close-btn" onclick="closeSetPopup()">&times;</span>
        <h3>請選擇套餐內的菜品</h3>
        <div id="setOptions"></div>
        <div class="set-button-group">
            <button class="btn-confirm" onclick="saveSetSelection()">確認</button>
            <button class="btn-cancel" onclick="closeSetPopup()">取消</button>
        </div>
    </div>
</div>



<div class="spice-popup" id="spicePopup">
    <div class="spice-box">
        <span class="close-btn" onclick="closeSpicePopup()">&times;</span>
        <h3>請選擇辣度、數量與備註</h3>

        <img id="popupDishImage" src="" alt="">

        <label for="spiceLevel">辣度：</label>
        <select id="spiceLevel">
            <option value="不辣">不辣</option>
            <option value="微辣">微辣</option>
            <option value="中辣">中辣</option>
            <option value="重辣">重辣</option>
        </select>

        <label>數量：</label>
        <div class="qty-control">
            <button type="button" onclick="adjustQty(-1)">−</button>
            <span id="spiceQtyDisplay">1</span>
            <button type="button" onclick="adjustQty(1)">＋</button>
        </div>

        <label for="spiceRemark">備註：</label>
        <textarea id="spiceRemark" rows="3" style="width:100%; padding:8px; margin-bottom:15px;" placeholder="例如：不要蔥、加飯..."></textarea>

        <button onclick="saveSpice()">確認</button>
    </div>
</div>

<div id="order-summary" style="display:none;" class="order-summary">
    <form id="orderForm" method="post" action="confirm_order.php">
        <p>You have selected <span id="total-count">0</span> item(s).</p>
        <ul id="order-summary-list"></ul> <!-- 顯示套餐選項 -->
        <div id="hidden-fields"></div>
        <button type="button" onclick="submitOrder()">Place Order</button>
    </form>
</div>


<div></div>
</body>
</html>
