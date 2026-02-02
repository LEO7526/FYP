<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

$message = '';
$message_type = '';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $mname = $_POST['mname'] ?? '';
    $category_id = $_POST['category_id'] ?? '';
    $unit = $_POST['unit'] ?? '';
    $mqty = $_POST['mqty'] ?? 0;
    $reorderLevel = $_POST['reorderLevel'] ?? 0;

    if (!empty($mname) && !empty($category_id) && !empty($unit)) {
        try {
            $check_sql = "SELECT mname FROM materials WHERE mname = ?";
            $check_stmt = $conn->prepare($check_sql);
            $check_stmt->bind_param("s", $mname);
            $check_stmt->execute();
            $check_stmt->store_result();

            if ($check_stmt->num_rows > 0) {
                $message = "Material '$mname' Already in stock！";
                $message_type = 'error';
            } else {
                $sql = "INSERT INTO materials (mname, category_id, unit, mqty, reorderLevel) 
                        VALUES (?, ?, ?, ?, ?)";
                $stmt = $conn->prepare($sql);
                $stmt->bind_param("sisdd", $mname, $category_id, $unit, $mqty, $reorderLevel);

                if ($stmt->execute()) {
                    $message = "New materials added successfully！";
                    $message_type = 'success';

                    $_POST = [];
                } else {
                    $message = "Adding failed";
                    $message_type = 'error';
                }
                $stmt->close();
            }
            $check_stmt->close();
        } catch (Exception $e) {
            $message = "error：" . $e->getMessage();
            $message_type = 'error';
        }
    } else {
        $message = "Please fill in all required fields.";
        $message_type = 'error';
    }
}

$categories = [];
$category_sql = "SELECT category_id, category_name FROM materials_category ORDER BY category_name";
$category_result = $conn->query($category_sql);
if ($category_result) {
    while ($row = $category_result->fetch_assoc()) {
        $categories[] = $row;
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yummy Restaurant - Add New Material</title>
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/addInventory.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function() {
            $('#unit').on('input', function() {
                const unit = $(this).val();
                if (unit) {
                    $('.unit-display').attr('data-unit', unit);
                }
            });

            $('#mqty, #reorderLevel').on('input', function() {
                const value = parseFloat($(this).val());
                if (value < 0) {
                    $(this).val(0);
                }
            });

            $('form').on('submit', function(e) {
                const mname = $('#mname').val().trim();
                const category = $('#category_id').val();
                const unit = $('#unit').val().trim();

                if (!mname) {
                    alert('Please enter the material name！');
                    $('#mname').focus();
                    e.preventDefault();
                    return false;
                }

                if (!category) {
                    alert('Please select material category！');
                    $('#category_id').focus();
                    e.preventDefault();
                    return false;
                }

                if (!unit) {
                    alert('Please enter unit！');
                    $('#unit').focus();
                    e.preventDefault();
                    return false;
                }

                return true;
            });
        });
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
    <h1>Add New Material</h1>

    <?php if ($message): ?>
        <div class="message <?php echo $message_type; ?>">
            <?php echo $message; ?>
        </div>
    <?php endif; ?>

    <form method="POST" action="">
        <div class="form-group">
            <label for="mname">Material Name *</label>
            <input type="text" id="mname" name="mname"
                   value="<?php echo htmlspecialchars($_POST['mname'] ?? ''); ?>"
                   placeholder="Enter material name" required>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label for="category_id">Category *</label>
                <select id="category_id" name="category_id" required>
                    <option value="">Select a category</option>
                    <?php foreach ($categories as $category): ?>
                        <option value="<?php echo $category['category_id']; ?>"
                            <?php echo ($_POST['category_id'] ?? '') == $category['category_id'] ? 'selected' : ''; ?>>
                            <?php echo htmlspecialchars($category['category_name']); ?>
                        </option>
                    <?php endforeach; ?>
                </select>
            </div>

            <div class="form-group">
                <label for="unit">Unit *</label>
                <input type="text" id="unit" name="unit"
                       value="<?php echo htmlspecialchars($_POST['unit'] ?? ''); ?>"
                       placeholder="e.g., grams, ml, pcs" required>
            </div>
        </div>

        <div class="form-row quantity-row">
            <div class="form-group quantity-input">
                <label for="mqty">Initial Quantity</label>
                <input type="number" id="mqty" name="mqty"
                       value="<?php echo htmlspecialchars($_POST['mqty'] ?? '0'); ?>"
                       min="0" step="0.01" placeholder="0.00">
            </div>

            <div class="unit-display" data-unit="<?php echo htmlspecialchars($_POST['unit'] ?? ''); ?>">
                <span></span>
            </div>

            <div class="form-group quantity-input">
                <label for="reorderLevel">Reorder Level</label>
                <input type="number" id="reorderLevel" name="reorderLevel"
                       value="<?php echo htmlspecialchars($_POST['reorderLevel'] ?? '0'); ?>"
                       min="0" step="0.01" placeholder="0.00">
            </div>
        </div>

        <button type="submit" class="submit-btn">Add Material</button>

        <a href="newInventory.php" class="back-btn">← Back to Inventory</a>
    </form>
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