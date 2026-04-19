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

    $package_items = $_POST['package_selected_items'] ?? [];
    $package_item_modifiers = $_POST['package_item_modifiers'] ?? [];
    $package_id = $_POST['package_id'] ?? null;

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

        function deleteItem(index) {
            // 從 itemData 移除對應項目
            itemData.item_ids.splice(index, 1);
            itemData.quantities.splice(index, 1);
            itemData.spice_levels.splice(index, 1);
            itemData.remarks.splice(index, 1);

            // 從 DOM 移除 li
            const itemList = document.querySelectorAll("form ul li");
            if (itemList[index]) {
                itemList[index].remove();
            }

            // 更新總數量與金額
            updateSummary();

            // 更新隱藏欄位
            updateHiddenInputs();
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
            itemList.forEach((item) => {
                const summaryEl = item.querySelector(".item-summary");
                if (summaryEl) {
                    const unitPrice = parseFloat(summaryEl.dataset.price);
                    const qtyText = summaryEl.innerText.match(/× (\d+)/);
                    const qty = qtyText ? parseInt(qtyText[1]) : 1;
                    totalQty += qty;
                    totalPrice += qty * unitPrice;
                }
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

            // 添加套餐數據
            const packageIdInput = document.querySelector("input[name='package_id']");
            if (packageIdInput) {
                const input = document.createElement("input");
                input.type = "hidden";
                input.name = "package_id";
                input.value = packageIdInput.value;
                form.appendChild(input);
            }

            const packageItemsInputs = document.querySelectorAll("input[name='package_selected_items[]']");
            packageItemsInputs.forEach(input => {
                const newInput = document.createElement("input");
                newInput.type = "hidden";
                newInput.name = "package_selected_items[]";
                newInput.value = input.value;
                form.appendChild(newInput);
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
                echo "<button type='button' onclick='editItem($i)'>Edit</button>";
                echo "<button type='button' onclick='deleteItem($i)'>Delete</button>";
                echo "</div>";
                echo "</li>";
            }
        }
        ?>
        <?php
        if ($package_id) {
            $stmt = $pdo->prepare("SELECT package_name, amounts, package_image_url FROM menu_package WHERE package_id = ?");
            $stmt->execute([$package_id]);
            $package = $stmt->fetch();

            if ($package) {
                $packageName = htmlspecialchars($package['package_name']);
                $packagePrice = (float)$package['amounts'];
                $packageImage = htmlspecialchars($package['package_image_url']);

                echo "<li style='margin-bottom:20px; list-style:none;'>";
                echo "<img src='$packageImage' alt='$packageName' style='width:120px; height:auto; border-radius:8px; margin-right:10px; vertical-align:middle;'>";
                echo "<div style='display:inline-block; vertical-align:middle;'>";
                echo "<p class='item-summary' data-price='$packagePrice'><strong>$packageName</strong> × 1 = $$packagePrice</p>";
                echo "<ul>";

                foreach ($package_items as $index => $pid) {
                    $stmt = $pdo->prepare("SELECT mit.item_name, ppt.type_name 
                                   FROM menu_item mi 
                                   JOIN menu_item_translation mit ON mi.item_id = mit.item_id 
                                   JOIN package_dish pd ON pd.item_id = mi.item_id
                                   JOIN package_type_translation ppt ON pd.type_id = ppt.type_id AND ppt.type_language_code = 'en'
                                   WHERE mi.item_id = ? AND mit.language_code = 'en' AND pd.package_id = ?");
                    $stmt->execute([$pid, $package_id]);
                    $item = $stmt->fetch();
                    if ($item) {
                        $name = htmlspecialchars($item['item_name']);
                        $type = htmlspecialchars($item['type_name']);
                        
                        // 获取价格修改器
                        $modifier = isset($package_item_modifiers[$index]) ? (float)$package_item_modifiers[$index] : 0;
                        if ($modifier > 0) {
                            $modifierDisplay = ' (+$' . number_format($modifier, 2) . ')';
                        } elseif ($modifier < 0) {
                            $modifierDisplay = ' (-$' . number_format(abs($modifier), 2) . ')';
                        } else {
                            $modifierDisplay = '';
                        }
                        
                        echo "<li>$type: $name$modifierDisplay</li>";
                    }
                }

                echo "</ul>";
                echo "</div>";
                echo "</li>";

                // 更新總價 - 加上價格修改器
                $totalPrice += $packagePrice;
                foreach ($package_item_modifiers as $modifier) {
                    $totalPrice += (float)$modifier;
                }
                $totalQty += 1; // 套餐算作 1 個項目
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
    <?php
    foreach ($package_items as $pid) {
        echo '<input type="hidden" name="package_selected_items[]" value="' . htmlspecialchars($pid) . '">';
    }
    ?>
    <?php
    foreach ($package_item_modifiers as $modifier) {
        echo '<input type="hidden" name="package_item_modifiers[]" value="' . htmlspecialchars($modifier) . '">';
    }
    ?>
    <?php
    if ($package_id) {
        echo '<input type="hidden" name="package_id" value="' . htmlspecialchars($package_id) . '">';
    }
    ?>


    <button type="submit" name="submitType">Confirm Order</button>

</form>

<div class="spice-popup" id="spicePopup" style="display:none;">
    <div class="spice-box">
        <span class="close-btn" onclick="closeSpicePopup()" style="position:absolute; top:10px; right:15px; font-size:20px; cursor:pointer;">&times;</span>
        <h3>Please select spice level, quantity, and remarks</h3>

        <img id="popupDishImage" src="" alt="" style="width:100%; max-height:200px; object-fit:contain; margin-bottom:10px;">

        <label for="spiceLevel">Spice Level:</label>
        <select id="spiceLevel">
            <option value="Not Spicy">Not Spicy</option>
            <option value="Mild">Mild</option>
            <option value="Medium">Medium</option>
            <option value="Hot">Hot</option>
        </select>

        <label>Quantity:</label>
        <div class="qty-control">
            <button type="button" onclick="adjustQty(-1)">−</button>
            <span id="spiceQtyDisplay">1</span>
            <button type="button" onclick="adjustQty(1)">＋</button>
        </div>

        <label for="spiceRemark">Remarks:</label>
        <textarea id="spiceRemark" rows="3" style="width:100%; padding:8px; margin-bottom:15px;" placeholder="e.g. No onion, extra rice..."></textarea>

        <button type="button" onclick="saveSpice()">Confirm</button>
    </div>
</div>


</body>
</html>
