<?php
/**
 * generatePackageRecommendation.php (Enhanced Error Handling)
 *
 * Receives date range, queries sales data, calls Gemini API,
 * and returns package suggestions as plain text.
 */

// Turn on error reporting for debugging (remove in production)
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// Start output buffering to catch any unexpected output
ob_start();

try {
    // ==================== Authentication ====================
    $authFile = __DIR__ . '/../auth_check.php';
    if (!file_exists($authFile)) {
        throw new Exception("Authentication file not found: $authFile");
    }
    require_once $authFile;
    if (!function_exists('check_staff_auth')) {
        throw new Exception("Function check_staff_auth() not defined in auth_check.php");
    }
    check_staff_auth();

    // ==================== Database Connection ====================
    $connFile = __DIR__ . '/../conn.php';
    if (!file_exists($connFile)) {
        throw new Exception("Database connection file not found: $connFile");
    }
    require_once $connFile;
    if (!isset($conn) || !$conn instanceof mysqli) {
        throw new Exception("Database connection not established");
    }

    // ==================== Gemini API Key ====================
    $apiKey = getenv('GEMINI_API_KEY');
    if (!$apiKey) {
        // Try to load from config file
        $configFile = __DIR__ . '/../gemini_config.php';
        if (file_exists($configFile)) {
            require_once $configFile;
            $apiKey = defined('GEMINI_API_KEY') ? GEMINI_API_KEY : null;
        }
    }
    if (!$apiKey) {
        throw new Exception("Gemini API key not configured. Set GEMINI_API_KEY environment variable or define it in gemini_config.php");
    }

    // ==================== Input Validation ====================
    $startDate = $_POST['start_date'] ?? '';
    $endDate   = $_POST['end_date'] ?? '';
    if (!$startDate || !$endDate) {
        throw new Exception("Missing start_date or end_date");
    }

    // ==================== Fetch Sales Data ====================
    $sql = "SELECT 
                mi.item_id,
                mit.item_name,
                mc.category_name,
                SUM(oi.qty) AS total_sold,
                SUM(oi.qty * mi.item_price) AS revenue,
                GROUP_CONCAT(DISTINCT t.tag_name SEPARATOR ', ') AS tags
            FROM order_items oi
            JOIN orders o ON oi.oid = o.oid
            JOIN menu_item mi ON oi.item_id = mi.item_id
            JOIN menu_item_translation mit ON mi.item_id = mit.item_id AND mit.language_code = 'en'
            JOIN menu_category mc ON mi.category_id = mc.category_id
            LEFT JOIN menu_tag mt ON mi.item_id = mt.item_id
            LEFT JOIN tag t ON mt.tag_id = t.tag_id
            WHERE DATE(o.odate) BETWEEN ? AND ?
              AND o.ostatus = 3
            GROUP BY mi.item_id, mit.item_name, mc.category_name
            ORDER BY total_sold DESC";

    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception("Failed to prepare SQL: " . $conn->error);
    }
    $stmt->bind_param("ss", $startDate, $endDate);
    if (!$stmt->execute()) {
        throw new Exception("SQL execution failed: " . $stmt->error);
    }
    $result = $stmt->get_result();
    $items = $result->fetch_all(MYSQLI_ASSOC);
    $stmt->close();

    if (empty($items)) {
        echo "No sales data found for the selected period.";
        exit;
    }

    // ==================== Build Prompt ====================
    $itemLines = [];
    foreach ($items as $item) {
        $itemLines[] = sprintf(
            "- %s (Category: %s, Sold: %d, Revenue: HK$ %.2f, Tags: %s)",
            $item['item_name'],
            $item['category_name'],
            $item['total_sold'],
            $item['revenue'],
            $item['tags'] ?: 'none'
        );
    }
    $itemText = implode("\n", $itemLines);

    $prompt = "Based on the sales data from {$startDate} to {$endDate} at our restaurant, here are the top selling dishes and their associated tags:\n\n";
    $prompt .= $itemText . "\n\n";
    $prompt .= "Please suggest 3 new package deals (combo meals) that could be offered on the menu. For each package, provide a creative name, list of dishes included (with quantities), a brief description, and a suggested discounted price (original total price vs package price). Consider the tags to create balanced and appealing combinations.\n\n";
    $prompt .= "Format the response as plain text, with each package clearly separated. Do not include any markdown or JSON.";

    // ==================== Call Gemini API ====================
    $apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" . urlencode($apiKey);

    $requestData = [
        'contents' => [
            [
                'parts' => [
                    ['text' => $prompt]
                ]
            ]
        ],
        'generationConfig' => [
            'temperature' => 0.7,
            'maxOutputTokens' => 2048
        ]
    ];

    $ch = curl_init($apiUrl);
    if (!$ch) {
        throw new Exception("Failed to initialize cURL");
    }

    curl_setopt_array($ch, [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_POST => true,
        CURLOPT_HTTPHEADER => ['Content-Type: application/json'],
        CURLOPT_POSTFIELDS => json_encode($requestData),
        CURLOPT_TIMEOUT => 30, // seconds
        CURLOPT_SSL_VERIFYPEER => true,
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curlError = curl_error($ch);
    curl_close($ch);

    if ($response === false) {
        throw new Exception("cURL error: " . $curlError);
    }

    if ($httpCode !== 200) {
        throw new Exception("Gemini API returned HTTP $httpCode: " . substr($response, 0, 500));
    }

    $responseData = json_decode($response, true);
    if (!$responseData || !isset($responseData['candidates'][0]['content']['parts'][0]['text'])) {
        throw new Exception("Invalid response structure from Gemini API");
    }

    $generatedText = $responseData['candidates'][0]['content']['parts'][0]['text'];

    // Clear output buffer and send plain text response
    ob_end_clean();
    header('Content-Type: text/plain; charset=utf-8');
    echo $generatedText;

} catch (Exception $e) {
    // Log the error to server log (optional)
    error_log("PackageRecommendation Error: " . $e->getMessage());

    // Clear any output so far and send error message
    ob_end_clean();
    http_response_code(500);
    header('Content-Type: text/plain; charset=utf-8');
    echo "Error: " . $e->getMessage();
    exit;
}