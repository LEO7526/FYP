<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yummy Restaurant - Staff Dashboard</title>
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/dashboard.css">
    <link rel="stylesheet" href="../CSS/order.css">
    <link rel="stylesheet" href="../CSS/form.css">
    <link rel="stylesheet" href="../CSS/staff-index-fix.css">
</head>
<body>
<header>
    <div class="logo">
        <a href="staffIndex.php">Yummy Restaurant</a>
    </div>

    <nav class="main-nav">
        <a href="MenuManagement.php" class="nav-button insert-items">Menu Management</a>
        <a href="Inventory.php" class="nav-button insert-materials">Inventory</a>
        <a href="ReservationList.php" class="nav-button order-list">Reservations</a>
        <a href="SalesReport.php" class="nav-button report">Sales Reports</a>
        <a href="PurchaseReturn.php" class="nav-button delete">Purchase & Return</a>
    </nav>

    <div class="user-actions">
        <a href="staffProfile.php">Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>

<div class="container">
    <div class="order-section">
        <h2>Today's Reservations</h2>
        <table>
            <thead>
            <tr>
                <th>Reservation ID</th>
                <th>Customer Name</th>
                <th>Time</th>
                <th>Party Size</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>R-0001</td>
                <td>Customer A</td>
                <td>7:00 PM</td>
                <td>4</td>
                <td><span class="status completed">Confirmed</span></td>
                <td>
                    <a href="#" class="btn-view">Details</a>
                    <a href="#" class="btn-accept">Accept</a>
                    <a href="#" class="btn-reject">Reject</a>
                </td>
            </tr>
            <tr>
                <td>R-0002</td>
                <td>Customer B</td>
                <td>8:30 PM</td>
                <td>2</td>
                <td><span class="status processing">Pending</span></td>
                <td>
                    <a href="#" class="btn-view">Details</a>
                    <a href="#" class="btn-accept">Accept</a>
                    <a href="#" class="btn-reject">Reject</a>
                </td>
            </tr>
            <tr>
                <td>R-0003</td>
                <td>Customer C</td>
                <td>6:00 PM</td>
                <td>6</td>
                <td><span class="status completed">Confirmed</span></td>
                <td>
                    <a href="#" class="btn-view">Details</a>
                    <a href="#" class="btn-modify">Modify</a>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="materials-section">
        <h2>Inventory Alerts</h2>
        <table>
            <thead>
            <tr>
                <th>Ingredient</th>
                <th>Current Stock</th>
                <th>Reorder Level</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Inventory A</td>
                <td>8 kg</td>
                <td>10 kg</td>
                <td><span class="stock-alert insufficient">Low Stock</span></td>
                <td><a href="#" class="btn-view">Reorder</a></td>
            </tr>
            <tr>
                <td>Inventory B</td>
                <td>2 kg</td>
                <td>5 kg</td>
                <td><span class="stock-alert insufficient">Critical</span></td>
                <td><a href="#" class="btn-view">Reorder</a></td>
            </tr>
            <tr>
                <td>Inventory C</td>
                <td>15 kg</td>
                <td>8 kg</td>
                <td><span class="stock-alert safety">In Stock</span></td>
                <td><a href="#" class="btn-view">View</a></td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="form-section">
        <h2>Daily Sales Summary</h2>
        <div class="report-summary">
            Total Sales: <span class="sales-amount">$8,542.00</span> |
            Orders Served: 124 |
            Top Dish: Dish A
        </div>

        <div class="form-group">
            <h2 class="form-label">Generate Custom Report</h2>
            <select>
                <option>Daily Report</option>
                <option>Weekly Report</option>
                <option>Monthly Report</option>
                <option>Custom Range</option>
            </select>
            <input type="submit" class="btn-submit" value="Generate Report">
        </div>
    </div>
</div>

<script>
    // 实时库存更新模拟
    function updateStockLevels() {
        const stockElements = document.querySelectorAll('.materials-section tbody tr td:nth-child(2)');
        stockElements.forEach(el => {
            const currentVal = parseInt(el.textContent);
            // 随机小幅度变化
            const change = Math.floor(Math.random() * 3) - 1; // -1, 0, or 1
            const newVal = Math.max(0, currentVal + change);
            el.textContent = newVal + ' kg';

            // 更新状态
            const row = el.parentElement;
            const statusCell = row.querySelector('td:nth-child(4)');
            const reorderLevel = parseInt(row.querySelector('td:nth-child(3)').textContent);

            if (newVal < reorderLevel * 0.3) {
                statusCell.innerHTML = '<span class="stock-alert insufficient">Critical</span>';
            } else if (newVal < reorderLevel) {
                statusCell.innerHTML = '<span class="stock-alert danger">Low Stock</span>';
            } else {
                statusCell.innerHTML = '<span class="stock-alert safety">In Stock</span>';
            }
        });
    }

    // 每30秒更新一次库存状态
    setInterval(updateStockLevels, 30000);

    // 添加交互效果
    document.querySelectorAll('.dashboard-button').forEach(button => {
        button.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 6px 12px rgba(0, 0, 0, 0.15)';
        });

        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.1)';
        });
    });
</script>
</body>
</html>