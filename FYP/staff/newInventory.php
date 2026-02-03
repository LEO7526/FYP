<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

$message = '';
$message_type = '';
$selected_material = null;

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_POST['select_material'])) {
        $mid = $_POST['mid'] ?? '';
        if ($mid) {
            $sql = "SELECT m.*, mc.category_name 
                    FROM materials m 
                    LEFT JOIN materials_category mc ON m.category_id = mc.category_id 
                    WHERE m.mid = ?";
            $stmt = $conn->prepare($sql);
            $stmt->bind_param("i", $mid);
            $stmt->execute();
            $result = $stmt->get_result();
            $selected_material = $result->fetch_assoc();
            $stmt->close();
        }
    } elseif (isset($_POST['restock_material'])) {
        $mid = $_POST['mid'] ?? '';
        $restock_qty = $_POST['restock_qty'] ?? 0;
        $new_total = $_POST['new_total'] ?? 0;

        if ($mid && ($restock_qty > 0 || $new_total > 0)) {
            $current_sql = "SELECT mqty, mname FROM materials WHERE mid = ?";
            $current_stmt = $conn->prepare($current_sql);
            $current_stmt->bind_param("i", $mid);
            $current_stmt->execute();
            $current_result = $current_stmt->get_result();
            $current_data = $current_result->fetch_assoc();
            $current_stmt->close();

            if ($current_data) {
                $current_qty = $current_data['mqty'];
                $material_name = $current_data['mname'];

                if ($new_total > 0) {
                    $new_qty = $new_total;
                    $added_qty = $new_total - $current_qty;
                } else {
                    $new_qty = $current_qty + $restock_qty;
                    $added_qty = $restock_qty;
                }

                $update_sql = "UPDATE materials SET mqty = ? WHERE mid = ?";
                $update_stmt = $conn->prepare($update_sql);
                $update_stmt->bind_param("di", $new_qty, $mid);

                if ($update_stmt->execute()) {
                    $log_sql = "INSERT INTO consumption_history (mid, log_date, log_type, details) 
                                VALUES (?, CURDATE(), 'Reorder', ?)";
                    $log_stmt = $conn->prepare($log_sql);
                    $details = "Manually restocked: $added_qty added (New total: $new_qty)";
                    $log_stmt->bind_param("is", $mid, $details);
                    $log_stmt->execute();
                    $log_stmt->close();

                    $message = "Successfully '$material_name'！Add quantity: $added_qty,New quantity: $new_qty";
                    $message_type = 'success';

                    $refresh_sql = "SELECT m.*, mc.category_name 
                                   FROM materials m 
                                   LEFT JOIN materials_category mc ON m.category_id = mc.category_id 
                                   WHERE m.mid = ?";
                    $refresh_stmt = $conn->prepare($refresh_sql);
                    $refresh_stmt->bind_param("i", $mid);
                    $refresh_stmt->execute();
                    $refresh_result = $refresh_stmt->get_result();
                    $selected_material = $refresh_result->fetch_assoc();
                    $refresh_stmt->close();
                } else {
                    $message = "Failed. Please try again later.";
                    $message_type = 'error';
                }
                $update_stmt->close();
            }
        } else {
            $message = "Please enter a valid quantity!";
            $message_type = 'error';
        }
    }
}

$search = $_GET['search'] ?? '';
$search_condition = '';
$search_params = [];

if (!empty($search)) {
    $search_condition = "WHERE m.mname LIKE ? OR mc.category_name LIKE ?";
    $search_param = "%$search%";
    $search_params = [$search_param, $search_param];
}

$materials = [];
$materials_sql = "SELECT m.mid, m.mname, m.category_id, m.unit, m.mqty, m.reorderLevel, 
                         mc.category_name 
                  FROM materials m 
                  LEFT JOIN materials_category mc ON m.category_id = mc.category_id 
                  $search_condition 
                  ORDER BY m.mname";

if (!empty($search_condition)) {
    $stmt = $conn->prepare($materials_sql);
    $stmt->bind_param("ss", ...$search_params);
    $stmt->execute();
    $result = $stmt->get_result();
} else {
    $result = $conn->query($materials_sql);
}

if ($result) {
    while ($row = $result->fetch_assoc()) {
        $materials[] = $row;
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yummy Restaurant - Restock Material</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/newInventory.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <script>
        $(document).ready(function() {
            $('#searchInput').on('input', function() {
                const searchTerm = $(this).val().toLowerCase();
                $('.material-item').each(function() {
                    const materialName = $(this).find('.material-name').text().toLowerCase();
                    const category = $(this).find('.material-category').text().toLowerCase();

                    if (materialName.includes(searchTerm) || category.includes(searchTerm)) {
                        $(this).show();
                    } else {
                        $(this).hide();
                    }
                });
            });

            $('.select-btn').on('click', function() {
                const materialId = $(this).data('id');
                $('#selected_mid').val(materialId);
                $('#select_form').submit();
            });

            $('#restock_qty, #new_total').on('input', function() {
                const value = parseFloat($(this).val());
                if (value < 0) {
                    $(this).val(0);
                }
            });

            $('#restock_qty').on('input', function() {
                const currentQty = parseFloat($('#current_qty').val());
                const restockQty = parseFloat($(this).val()) || 0;
                const newTotal = currentQty + restockQty;
                $('#calculated_total').val(newTotal.toFixed(2));
            });

            $('#new_total').on('input', function() {
                $('#restock_qty').val(0);
            });

            $('#restock_form').on('submit', function(e) {
                const restockQty = parseFloat($('#restock_qty').val());
                const newTotal = parseFloat($('#new_total').val());

                if (restockQty <= 0 && newTotal <= 0) {
                    alert('Please enter the replenishment quantity or the new total quantity！');
                    e.preventDefault();
                    return false;
                }

                if (newTotal > 0) {
                    const currentQty = parseFloat($('#current_qty').val());
                    if (newTotal < currentQty) {
                        if (!confirm('The new total quantity is less than the current quantity, which will reduce inventory. Are you sure you want to continue?')) {
                            e.preventDefault();
                            return false;
                        }
                    }
                }

                return true;
            });
        });

        function clearSearch() {
            $('#searchInput').val('');
            $('.material-item').show();
        }
    </script>
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
    <h1>Restock Material</h1>

    <?php if ($message): ?>
        <div class="message <?php echo $message_type; ?>">
            <?php echo $message; ?>
        </div>
    <?php endif; ?>

    <div class="search-box">
        <input type="text" id="searchInput" placeholder="Search materials by name or category...">
        <div class="search-icon">
            <i class="fas fa-search"></i>
        </div>
    </div>

    <form id="select_form" method="POST" style="display: none;">
        <input type="hidden" id="selected_mid" name="mid">
        <input type="hidden" name="select_material" value="1">
    </form>

    <div class="materials-list">
        <?php if (empty($materials)): ?>
            <div style="text-align: center; padding: 40px; color: #7f8c8d;">
                <i class="fas fa-box-open" style="font-size: 48px; margin-bottom: 15px;"></i>
                <p>No materials found. <a href="addInventory.php">Add new materials</a></p>
            </div>
        <?php else: ?>
            <?php foreach ($materials as $material):
                $isLowStock = floatval($material['mqty']) < floatval($material['reorderLevel']);
                $quantityClass = $isLowStock ? 'material-quantity low-stock' : 'material-quantity';
                ?>
                <div class="material-item">
                    <div class="material-info">
                        <div class="material-name"><?php echo htmlspecialchars($material['mname']); ?></div>
                        <div class="material-details">
                            <span class="material-category"><?php echo htmlspecialchars($material['category_name']); ?></span>
                            <span>Current: <span class="<?php echo $quantityClass; ?>"><?php echo $material['mqty']; ?> <?php echo $material['unit']; ?></span></span>
                            <span>Reorder Level: <?php echo $material['reorderLevel']; ?> <?php echo $material['unit']; ?></span>
                        </div>
                    </div>
                    <button type="button" class="select-btn" data-id="<?php echo $material['mid']; ?>">
                        Select
                    </button>
                </div>
            <?php endforeach; ?>
        <?php endif; ?>
    </div>

    <?php if ($selected_material):
        $isSelectedLowStock = floatval($selected_material['mqty']) < floatval($selected_material['reorderLevel']);
        $selectedQuantityClass = $isSelectedLowStock ? 'low-stock' : '';
        ?>
        <div class="selected-material">
            <h3>Selected Material: <?php echo htmlspecialchars($selected_material['mname']); ?></h3>

            <div class="current-info">
                <div class="info-item">
                    <div class="label">Category</div>
                    <div class="value"><?php echo htmlspecialchars($selected_material['category_name']); ?></div>
                </div>
                <div class="info-item">
                    <div class="label">Current Quantity</div>
                    <div class="value">
                        <span class="<?php echo $selectedQuantityClass; ?>">
                            <?php echo $selected_material['mqty']; ?> <?php echo $selected_material['unit']; ?>
                        </span>
                    </div>
                </div>
                <div class="info-item">
                    <div class="label">Reorder Level</div>
                    <div class="value"><?php echo $selected_material['reorderLevel']; ?> <?php echo $selected_material['unit']; ?></div>
                </div>
                <div class="info-item">
                    <div class="label">Unit</div>
                    <div class="value"><?php echo $selected_material['unit']; ?></div>
                </div>
            </div>

            <form id="restock_form" method="POST">
                <input type="hidden" name="mid" value="<?php echo $selected_material['mid']; ?>">
                <input type="hidden" id="current_qty" value="<?php echo $selected_material['mqty']; ?>">

                <div class="quantity-controls">
                    <div class="quantity-input-group">
                        <label for="restock_qty">Quantity to Add</label>
                        <input type="number" id="restock_qty" name="restock_qty"
                               min="0" step="0.01" placeholder="0.00">
                    </div>

                    <div class="unit-display">
                        <?php echo $selected_material['unit']; ?>
                    </div>

                    <div class="quantity-input-group">
                        <label for="new_total">OR Set New Total</label>
                        <input type="number" id="new_total" name="new_total"
                               min="0" step="0.01" placeholder="0.00">
                    </div>

                    <div class="quantity-input-group calculated-total-group">
                        <label>Calculated Total</label>
                        <input type="number" id="calculated_total"
                               value="<?php echo $selected_material['mqty']; ?>"
                               readonly style="background-color: #e8f4fd;">
                    </div>
                </div>

                <button type="submit" name="restock_material" value="1" class="submit-btn">
                    <i class="fas fa-plus-circle"></i> Restock Material
                </button>
            </form>
        </div>
    <?php endif; ?>

    <div style="text-align: center; margin-top: 30px;">

        <a href="addInventory.php" class="back-btn" style="background: #43a047;">
            <i class="fas fa-plus"></i> Add New Material
        </a>
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