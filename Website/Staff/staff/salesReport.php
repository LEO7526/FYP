<?php
require_once '../auth_check.php';
check_staff_auth();
include '../conn.php';

// Get current month start and end dates
$currentMonthStart = date('Y-m-01');
$currentMonthEnd = date('Y-m-t');

// Set default dates
$startDate = $currentMonthStart;
$endDate = $currentMonthEnd;

// Check if form is submitted
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $startDate = $_POST['start_date'] ?? $currentMonthStart;
    $endDate = $_POST['end_date'] ?? $currentMonthEnd;
}

// Function to get sales report data
function getSalesReport($conn, $startDate, $endDate) {
    $report = [];

    // Get total revenue and order count
    $sql = "SELECT COUNT(DISTINCT o.oid) as total_orders, 
                   SUM(oi.qty * mi.item_price) as total_revenue, 
                   AVG(oi.qty * mi.item_price) as average_order_value 
            FROM orders o 
            JOIN order_items oi ON o.oid = oi.oid 
            JOIN menu_item mi ON oi.item_id = mi.item_id 
            WHERE DATE(o.odate) BETWEEN ? AND ? AND o.ostatus = 3";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ss", $startDate, $endDate);
    $stmt->execute();
    $result = $stmt->get_result();
    $report['summary'] = $result->fetch_assoc();
    $stmt->close();

    // Get top selling items
    $sql = "SELECT mit.item_name, 
                   mit.language_code, 
                   SUM(oi.qty) as total_sold, 
                   SUM(oi.qty * mi.item_price) as revenue, 
                   mc.category_name 
            FROM order_items oi 
            JOIN orders o ON oi.oid = o.oid 
            JOIN menu_item mi ON oi.item_id = mi.item_id 
            JOIN menu_item_translation mit ON mi.item_id = mit.item_id 
            JOIN menu_category mc ON mi.category_id = mc.category_id 
            WHERE DATE(o.odate) BETWEEN ? AND ? 
            AND o.ostatus = 3 
            AND mit.language_code = 'en' 
            GROUP BY mi.item_id, mit.item_name, mc.category_name 
            ORDER BY total_sold DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ss", $startDate, $endDate);
    $stmt->execute();
    $result = $stmt->get_result();
    $report['top_items'] = $result->fetch_all(MYSQLI_ASSOC);
    $stmt->close();

    // Get sales by category for pie chart
    $sql = "SELECT mc.category_name, 
                   SUM(oi.qty * mi.item_price) as revenue,
                   SUM(oi.qty) as total_sold
            FROM order_items oi 
            JOIN orders o ON oi.oid = o.oid 
            JOIN menu_item mi ON oi.item_id = mi.item_id 
            JOIN menu_category mc ON mi.category_id = mc.category_id 
            WHERE DATE(o.odate) BETWEEN ? AND ? 
            AND o.ostatus = 3 
            GROUP BY mc.category_id, mc.category_name 
            ORDER BY revenue DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ss", $startDate, $endDate);
    $stmt->execute();
    $result = $stmt->get_result();
    $report['category_sales'] = $result->fetch_all(MYSQLI_ASSOC);
    $stmt->close();

    return $report;
}

// Get sales data
$salesData = getSalesReport($conn, $startDate, $endDate);

// Prepare data for pie chart
$pieChartData = [];
$pieChartLabels = [];
$pieChartRevenue = [];
$pieChartColors = [
    '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0',
    '#9966FF', '#FF9F40', '#FF6384', '#C9CBCF',
    '#8AC926', '#1982C4', '#6A4C93', '#F15BB5'
];

if (!empty($salesData['category_sales'])) {
    foreach ($salesData['category_sales'] as $index => $category) {
        $pieChartLabels[] = $category['category_name'];
        $pieChartRevenue[] = floatval($category['revenue']);
        $pieChartData[] = [
            'category' => $category['category_name'],
            'revenue' => floatval($category['revenue']),
            'total_sold' => $category['total_sold'],
            'color' => $pieChartColors[$index % count($pieChartColors)]
        ];
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sales Report - Yummy Restaurant</title>
    <link rel="stylesheet" href="../CSS/common.css">
    <link rel="stylesheet" href="../CSS/header.css">
    <link rel="stylesheet" href="../CSS/salesReport.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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

<div class="container">
    <div class="sales-report-container">
        <div class="report-header">
            <h1>Sales Report</h1>
            <p>View sales performance and top-selling items</p>
        </div>

        <div class="date-filter-section">
            <form method="POST" class="date-filter-form">
                <div class="form-group">
                    <label for="start_date">Start Date</label>
                    <input type="date" id="start_date" name="start_date"
                           value="<?php echo htmlspecialchars($startDate); ?>"
                           class="form-control" required>
                </div>
                <div class="form-group">
                    <label for="end_date">End Date</label>
                    <input type="date" id="end_date" name="end_date"
                           value="<?php echo htmlspecialchars($endDate); ?>"
                           class="form-control" required>
                </div>
                <div class="form-group">
                    <button type="submit" class="btn-primary">Generate Report</button>
                </div>
            </form>
        </div>

        <div class="sales-summary">
            <div class="summary-card total-revenue">
                <h3>Total Revenue</h3>
                <div class="value">HK$ <?php echo number_format($salesData['summary']['total_revenue'] ?? 0, 2); ?></div>
            </div>
            <div class="summary-card total-orders">
                <h3>Total Orders</h3>
                <div class="value"><?php echo $salesData['summary']['total_orders'] ?? 0; ?></div>
            </div>
            <div class="summary-card average-order">
                <h3>Average Order Value</h3>
                <div class="value">HK$ <?php echo number_format($salesData['summary']['average_order_value'] ?? 0, 2); ?></div>
            </div>
        </div>

        <!-- Pie Chart Section -->
        <div class="chart-section">
            <h2>Sales by Category</h2>
            <div class="chart-container">
                <div class="pie-chart-wrapper">
                    <canvas id="salesPieChart"></canvas>
                </div>
                <div class="chart-legend" id="pieChartLegend">
                    <?php if (!empty($pieChartData)): ?>
                        <?php
                        $totalRevenue = array_sum($pieChartRevenue);
                        foreach ($pieChartData as $item):
                            $percentage = $totalRevenue > 0 ? ($item['revenue'] / $totalRevenue) * 100 : 0;
                            ?>
                            <div class="legend-item">
                                <span class="legend-color" style="background-color: <?php echo $item['color']; ?>"></span>
                                <span class="legend-label"><?php echo htmlspecialchars($item['category']); ?></span>
                                <span class="legend-value">HK$ <?php echo number_format($item['revenue'], 2); ?> (<?php echo number_format($percentage, 1); ?>%)</span>
                            </div>
                        <?php endforeach; ?>
                    <?php else: ?>
                        <div class="no-data">No category data available</div>
                    <?php endif; ?>
                </div>
            </div>
        </div>

        <div class="sales-details">
            <h2>Top Selling Items</h2>
            <?php if (!empty($salesData['top_items'])): ?>
                <table class="sales-table">
                    <thead>
                    <tr>
                        <th>Item Name</th>
                        <th>Category</th>
                        <th>Quantity Sold</th>
                        <th>Revenue</th>
                    </tr>
                    </thead>
                    <tbody>
                    <?php foreach ($salesData['top_items'] as $item): ?>
                        <tr>
                            <td><?php echo htmlspecialchars($item['item_name']); ?></td>
                            <td><?php echo htmlspecialchars($item['category_name']); ?></td>
                            <td><?php echo $item['total_sold']; ?></td>
                            <td>HK$ <?php echo number_format($item['revenue'], 2); ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            <?php else: ?>
                <div class="no-data">
                    No sales data found for the selected period.
                </div>
            <?php endif; ?>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const hamburger = document.getElementById('hamburgerMenu');
        const mainNav = document.querySelector('.main-nav');
        const userActions = document.querySelector('.user-actions');

        // Hamburger menu functionality
        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            mainNav.classList.toggle('active');
            document.body.style.overflow = mainNav.classList.contains('active') ? 'hidden' : '';
        });

        // Close menu when nav buttons are clicked
        document.querySelectorAll('.nav-button').forEach(button => {
            button.addEventListener('click', function() {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            });
        });

        // Close menu when user actions are clicked
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

        // Close menu with Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && mainNav.classList.contains('active')) {
                hamburger.classList.remove('active');
                mainNav.classList.remove('active');
                document.body.style.overflow = '';
            }
        });

        // Date validation
        const startDateInput = document.getElementById('start_date');
        const endDateInput = document.getElementById('end_date');

        startDateInput.addEventListener('change', function() {
            if (endDateInput.value && startDateInput.value > endDateInput.value) {
                endDateInput.value = startDateInput.value;
            }
        });

        endDateInput.addEventListener('change', function() {
            if (startDateInput.value && endDateInput.value < startDateInput.value) {
                startDateInput.value = endDateInput.value;
            }
        });

        // Initialize Pie Chart
        initializePieChart();
    });

    function initializePieChart() {
        const pieChartData = <?php echo json_encode($pieChartData); ?>;

        if (pieChartData.length === 0) {
            return;
        }

        const ctx = document.getElementById('salesPieChart').getContext('2d');
        const labels = pieChartData.map(item => item.category);
        const data = pieChartData.map(item => item.revenue);
        const backgroundColors = pieChartData.map(item => item.color);

        new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: backgroundColors,
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.raw || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
                                return `${label}: HK$ ${value.toFixed(2)} (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
    }
</script>
</body>
</html>