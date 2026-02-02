<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>XXX Restaurant - Confirm Order</title>
    <link rel="stylesheet" href="../Com.css">
    <link rel="stylesheet" href="confirm_order.css">

    <?php
    $item_ids = $_POST['item_ids'] ?? [];
    $quantities = $_POST['quantities'] ?? [];
    $spice_levels = $_POST['spice_levels'] ?? [];
    $remarks = $_POST['remarks'] ?? [];
    ?>

    <script>
        let currentEditIndex = null;

        const itemData = <?php echo json_encode([
                'item_ids' => $item_ids,
                'quantities' => $quantities,
                'spice_levels' => $spice_levels,
                'remarks' => $remarks
        ], JSON_UNESCAPED_UNICODE); ?>;

        function editItem(index) {
            currentEditIndex = index;

            const qty = itemData.quantities[index];
            const spice = itemData.spice_levels[index] || '不辣';
            const remark = itemData.remarks[index] || '';

            document.getElementById("spicePopup").style.display = "flex";
            document.getElementById("spiceLevel").value = spice;
            document.getElementById("spiceQtyDisplay").innerText = qty;
            document.getElementById("spiceRemark").value = remark;
        }


        function adjustQty(delta) {
            let qty = parseInt(document.getElementById("spiceQtyDisplay").innerText);
            qty = Math.max(1, qty + delta);
            document.getElementById("spiceQtyDisplay").innerText = qty;
        }

        function saveSpice() {
            const level = document.getElementById("spiceLevel").value;
            const qty = parseInt(document.getElementById("spiceQtyDisplay").innerText);
            const remark = document.getElementById("spiceRemark").value;

            itemData.spice_levels[currentEditIndex] = level;
            itemData.quantities[currentEditIndex] = qty;
            itemData.remarks[currentEditIndex] = remark;

            const itemList = document.querySelectorAll("form ul li");
            const item = itemList[currentEditIndex];
            if (item) {
                const summaryEl = item.querySelector(".item-summary");
                const name = summaryEl.querySelector("strong").innerText;
                const unitPrice = parseFloat(summaryEl.dataset.price); // 直接讀取單價
                const total = (unitPrice * qty).toFixed(2);

                summaryEl.innerHTML = `<strong>${name}</strong> × ${qty} = $${total}`;
                summaryEl.dataset.price = unitPrice; // 保持單價不變
                item.querySelector(".item-spice").innerText = `辣度：${level}`;
                item.querySelector(".item-remark").innerText = `備註：${remark}`;
            }

            updateSummary();
            updateHiddenInputs();
            document.getElementById("spicePopup").style.display = "none";
        }

        function updateSummary() {
            let totalQty = 0;
            let totalPrice = 0;

            const itemList = document.querySelectorAll("form ul li");
            itemList.forEach((item, index) => {
                const qty = parseInt(itemData.quantities[index]);
                const unitPrice = parseFloat(item.querySelector(".item-summary").dataset.price); // 直接讀取單價
                totalQty += qty;
                totalPrice += qty * unitPrice;
            });

            document.getElementById("totalQty").innerText = totalQty;
            document.getElementById("totalPrice").innerText = totalPrice.toFixed(2);
        }


        function closeSpicePopup() {
            document.getElementById("spicePopup").style.display = "none";
        }



        function updateHiddenInputs() {
            const form = document.getElementById("orderForm");
            const hiddenFields = form.querySelectorAll("input[type='hidden']");
            hiddenFields.forEach(input => input.remove());

            itemData.item_ids.forEach((id, index) => {
                const inputs = [
                    { name: "item_ids[]", value: id },
                    { name: "quantities[]", value: itemData.quantities[index] },
                    { name: "spice_levels[]", value: itemData.spice_levels[index] },
                    { name: "remarks[]", value: itemData.remarks[index] }
                ];

                inputs.forEach(({ name, value }) => {
                    const input = document.createElement("input");
                    input.type = "hidden";
                    input.name = name;
                    input.value = value;
                    form.appendChild(input);
                });
            });
        }
    </script>
</head>
<body>

<?php
if (count($item_ids) !== count($quantities)) {
    die("Data error. Please reselect your items.");
}

$host = 'localhost';
$dbname = 'projectdb';
$user = 'root';
$pass = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die("Database connection failed: " . $e->getMessage());
}
?>

<h2>Confirm Your Order</h2>

<form method="post" action="checkLogin.php" id="orderForm">
    <ul>
        <?php
        $totalQty = 0;
        $totalPrice = 0;

        for ($i = 0; $i < count($item_ids); $i++) {
            $stmt = $pdo->prepare("SELECT mit.item_name, mi.item_price, mi.image_url FROM menu_item mi JOIN menu_item_translation mit ON mi.item_id = mit.item_id WHERE mi.item_id = ? AND mit.language_code = 'en'");
            $stmt->execute([$item_ids[$i]]);
            $item = $stmt->fetch();

            if ($item) {
                $name = htmlspecialchars($item['item_name']);
                $price = htmlspecialchars($item['item_price']);
                $qty = htmlspecialchars($quantities[$i]);
                $spice = htmlspecialchars($spice_levels[$i] ?? '未選擇');
                $remark = htmlspecialchars($remarks[$i] ?? '');
                $total = $price * $qty;
                $image = htmlspecialchars($item['image_url']);

                $totalQty += $qty;
                $totalPrice += $total;

                echo "<li style='margin-bottom:20px; list-style:none;'>";
                echo "<img src='$image' alt='$name' style='width:120px; height:auto; border-radius:8px; margin-right:10px; vertical-align:middle;'>";
                echo "<div style='display:inline-block; vertical-align:middle;'>";
                echo "<p class='item-summary' data-price='$price'><strong>$name</strong> × $qty = $$total</p>";
                echo "<p class='item-spice'>辣度：$spice</p>";
                echo "<p class='item-remark'>備註：$remark</p>";
                echo "<button type='button' onclick='editItem($i)'>修改</button>";
                echo "</div>";
                echo "</li>";
            }
        }
        ?>
    </ul>

    <p style='text-align:right; font-weight:bold;'>Total Quantity: <span id="totalQty"><?= $totalQty ?></span> item(s)</p>
    <p style='text-align:right; font-weight:bold;'>Total Price: $<span id="totalPrice"><?= $totalPrice ?></span></p>


    <?php
    foreach ($item_ids as $index => $id) {
        echo '<input type="hidden" name="item_ids[]" value="' . htmlspecialchars($id) . '">';
        echo '<input type="hidden" name="quantities[]" value="' . htmlspecialchars($quantities[$index]) . '">';
        echo '<input type="hidden" name="spice_levels[]" value="' . htmlspecialchars($spice_levels[$index] ?? '未選擇') . '">';
        echo '<input type="hidden" name="remarks[]" value="' . htmlspecialchars($remarks[$index] ?? '') . '">';
    }
    ?>

    <button type="submit" name="submitType">Confirm Order</button>

</form>

<div class="spice-popup" id="spicePopup" style="display:none;">
    <div class="spice-box">
        <span class="close-btn" onclick="closeSpicePopup()" style="position:absolute; top:10px; right:15px; font-size:20px; cursor:pointer;">&times;</span>
        <h3>請選擇辣度、數量與備註</h3>

        <img id="popupDishImage" src="" alt="" style="width:100%; max-height:200px; object-fit:contain; margin-bottom:10px;">

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

        <button type="button" onclick="saveSpice()">確認</button>


    </div>
</div>

</body>
</html>
