<?php
/**
 * Get Available Tables with Seating Chart Layout
 * Returns full seating chart with coordinates and real-time occupancy status
 * 
 * URL: GET /projectapi/get_available_tables_layout.php?date=2024-01-15&time=18:30&pnum=4
 * 
 * Response includes:
 * - All tables with coordinates and status (available/occupied/reserved)
 * - Layout dimensions
 * - Occupied tables from table_orders real-time data
 * 
 * Author: System
 * Version: 1.0
 */

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=utf-8");

// Database connection
$conn = new mysqli("localhost", "root", "", "ProjectDB");

if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Database connection failed: " . $conn->connect_error
    ]);
    exit;
}

// Get parameters
$date = $_GET['date'] ?? null;
$time = $_GET['time'] ?? null;
$pnum = isset($_GET['pnum']) ? (int)$_GET['pnum'] : 0;

// Validate input
if (!$date || !$time || $pnum <= 0) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Missing or invalid parameters: date, time, pnum required"
    ]);
    $conn->close();
    exit;
}

// Calculate suitable capacity based on guest count
$requiredCapacity = 2;
if ($pnum <= 2) {
    $requiredCapacity = 2;
} elseif ($pnum <= 4) {
    $requiredCapacity = 4;
} else {
    $requiredCapacity = 8;
}

// Calculate booking time window (±2 hours)
$startTime = date('H:i:s', strtotime($time . ' -2 hours'));
$endTime = date('H:i:s', strtotime($time . ' +2 hours'));

try {
    // Get booked table IDs from booking table
    $sql_booked = "
        SELECT tid 
        FROM booking 
        WHERE bdate = ? 
        AND btime BETWEEN ? AND ? 
        AND status != 0
    ";
    
    $stmt_booked = $conn->prepare($sql_booked);
    if ($stmt_booked === false) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt_booked->bind_param("sss", $date, $startTime, $endTime);
    $stmt_booked->execute();
    $result_booked = $stmt_booked->get_result();
    
    $booked_tids = [];
    while ($row = $result_booked->fetch_assoc()) {
        $booked_tids[] = (int)$row['tid'];
    }
    $stmt_booked->close();
    
    // Get occupied tables from table_orders (real-time occupancy)
    // A table is occupied if it has an associated order (oid is not null)
    $sql_occupied = "
        SELECT DISTINCT table_number 
        FROM table_orders 
        WHERE oid IS NOT NULL
    ";
    
    $stmt_occupied = $conn->prepare($sql_occupied);
    if ($stmt_occupied === false) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt_occupied->execute();
    $result_occupied = $stmt_occupied->get_result();
    
    $occupied_tables = [];
    while ($row = $result_occupied->fetch_assoc()) {
        $occupied_tables[] = (int)$row['table_number'];
    }
    $stmt_occupied->close();
    
    // Get all tables with coordinates
    $sql_all_tables = "
        SELECT 
            tid,
            capacity,
            status,
            x_position,
            y_position
        FROM seatingChart
        ORDER BY tid ASC
    ";
    
    $result_tables = $conn->query($sql_all_tables);
    if ($result_tables === false) {
        throw new Exception("Query failed: " . $conn->error);
    }
    
    $tables = [];
    $available_tables = [];
    
    while ($row = $result_tables->fetch_assoc()) {
        $tid = (int)$row['tid'];
        $capacity = (int)$row['capacity'];
        
        // Determine table status
        $table_status = 'available';
        $is_available = true;
        
        if (in_array($tid, $occupied_tables)) {
            $table_status = 'occupied';
            $is_available = false;
        } elseif (in_array($tid, $booked_tids)) {
            $table_status = 'reserved';
            $is_available = false;
        } elseif ((int)$row['status'] === 1) {
            $table_status = 'reserved';
            $is_available = false;
        }
        
        $table_data = [
            'id' => $tid,
            'capacity' => $capacity,
            'status' => $table_status,
            'x' => (float)$row['x_position'],
            'y' => (float)$row['y_position'],
            'is_available' => $is_available,
            'suitable_for_booking' => $is_available && $capacity >= $requiredCapacity
        ];
        
        $tables[] = $table_data;
        
        // Collect available tables for the specified guest count
        if ($is_available && $capacity >= $requiredCapacity) {
            $available_tables[] = $table_data;
        }
    }
    
    // Prepare layout dimensions
    $layout_config = [
        'width_percent' => 100,
        'height_percent' => 100,
        'cell_width' => 10,    // Each cell represents 10% width
        'cell_height' => 15    // Each cell represents 15% height
    ];
    
    // Success response
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'date' => $date,
        'time' => $time,
        'guest_count' => $pnum,
        'required_capacity' => $requiredCapacity,
        'layout' => $layout_config,
        'tables' => $tables,
        'available_tables' => $available_tables,
        'total_tables' => count($tables),
        'total_available' => count($available_tables),
        'summary' => [
            'available' => count(array_filter($tables, fn($t) => $t['status'] === 'available')),
            'occupied' => count(array_filter($tables, fn($t) => $t['status'] === 'occupied')),
            'reserved' => count(array_filter($tables, fn($t) => $t['status'] === 'reserved'))
        ]
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Server error: ' . $e->getMessage()
    ]);
} finally {
    $conn->close();
}
?>
