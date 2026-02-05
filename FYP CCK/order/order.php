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
            document.getElementById('total-count').innerText = total;

            const summary = document.getElementById('order-summary');
            summary.style.display = total > 0 ? 'block' : 'none';
        }

        function submitOrder() {
            const hiddenFields = document.getElementById('hidden-fields');
            hiddenFields.innerHTML = '';

            for (const [id, qty] of Object.entries(quantities)) {
                if (qty > 0) {
                    hiddenFields.innerHTML += `<input type="hidden" name="item_ids[]" value="${id}">`;
                    hiddenFields.innerHTML += `<input type="hidden" name="quantities[]" value="${qty}">`;
                    hiddenFields.innerHTML += `<input type="hidden" name="spice_levels[]" value="${spiceLevels[id] || '未選擇'}">`;
                    hiddenFields.innerHTML += `<input type="hidden" name="remarks[]" value="${remarks[id] || ''}">`;
                }
            }

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
            5 => ['name' => 'Drink', 'key' => 'drink']
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
        <div id="hidden-fields"></div>
        <button type="button" onclick="submitOrder()">Place Order</button>
    </form>
</div>


<div></div>
</body>
</html>
