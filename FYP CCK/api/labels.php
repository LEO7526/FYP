<?php
/**
 * API file for fetching translatable labels from database
 * Instead of hardcoding strings in string.xml, fetch them from database
 * 
 * Endpoints:
 * - GET /api/labels/spice-levels?lang=zh-TW
 * - GET /api/labels/tags?lang=zh-TW
 * - GET /api/labels/customization-groups?lang=zh-TW
 * - GET /api/labels/all?lang=zh-TW (fetch all labels)
 */

header('Content-Type: application/json; charset=utf-8');

// Include database connection
require_once '../conn.php';

// Default language
$lang = $_GET['lang'] ?? 'zh-TW';

// Validate language code
$allowed_langs = ['en', 'zh-TW', 'zh-CN'];
if (!in_array($lang, $allowed_langs)) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid language code']);
    exit;
}

// Determine the action
$action = $_GET['action'] ?? 'all';

switch ($action) {
    case 'spice-levels':
        getSpiceLevels($conn, $lang);
        break;
    case 'tags':
        getTags($conn, $lang);
        break;
    case 'customization-groups':
        getCustomizationGroups($conn, $lang);
        break;
    case 'all':
        getAllLabels($conn, $lang);
        break;
    default:
        http_response_code(400);
        echo json_encode(['error' => 'Invalid action']);
        break;
}

/**
 * Fetch all spice levels with translations
 */
function getSpiceLevels($conn, $lang) {
    $query = "
        SELECT 
            sl.spice_id,
            sl.spice_key,
            slt.spice_name,
            sl.spice_order
        FROM spice_level sl
        LEFT JOIN spice_level_translation slt 
            ON sl.spice_id = slt.spice_id AND slt.language_code = ?
        ORDER BY sl.spice_order ASC
    ";
    
    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $lang);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $spice_levels = [];
    while ($row = $result->fetch_assoc()) {
        $spice_levels[] = [
            'id' => (int)$row['spice_id'],
            'key' => $row['spice_key'],
            'name' => $row['spice_name'],
            'order' => (int)$row['spice_order']
        ];
    }
    
    echo json_encode([
        'success' => true,
        'language' => $lang,
        'data' => $spice_levels
    ]);
}

/**
 * Fetch all tags with translations
 */
function getTags($conn, $lang) {
    $query = "
        SELECT 
            t.tag_id,
            t.tag_name as tag_key,
            tt.tag_name,
            t.tag_category,
            t.tag_bg_color
        FROM tag t
        LEFT JOIN tag_translation tt 
            ON t.tag_id = tt.tag_id AND tt.language_code = ?
        ORDER BY t.tag_id ASC
    ";
    
    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $lang);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $tags = [];
    while ($row = $result->fetch_assoc()) {
        $tags[] = [
            'id' => (int)$row['tag_id'],
            'key' => $row['tag_key'],
            'name' => $row['tag_name'],
            'category' => $row['tag_category'],
            'bgColor' => $row['tag_bg_color']
        ];
    }
    
    echo json_encode([
        'success' => true,
        'language' => $lang,
        'data' => $tags
    ]);
}

/**
 * Fetch customization groups with their values
 */
function getCustomizationGroups($conn, $lang) {
    $query = "
        SELECT 
            cog.group_id,
            cog.group_type,
            cog_t.group_name as group_label,
            cov.value_id,
            cov.value_name as value_key,
            cov_t.value_name,
            cov.display_order
        FROM customization_option_group cog
        LEFT JOIN customization_option_group_translation cog_t 
            ON cog.group_id = cog_t.group_id AND cog_t.language_code = ?
        LEFT JOIN customization_option_value cov 
            ON cog.group_id = cov.group_id
        LEFT JOIN customization_option_value_translation cov_t 
            ON cov.value_id = cov_t.value_id AND cov_t.language_code = ?
        ORDER BY cog.group_id ASC, cov.display_order ASC
    ";
    
    $stmt = $conn->prepare($query);
    $stmt->bind_param("ss", $lang, $lang);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $groups = [];
    $current_group = null;
    
    while ($row = $result->fetch_assoc()) {
        $group_id = (int)$row['group_id'];
        
        // Create group if not exists
        if (!isset($groups[$group_id])) {
            $groups[$group_id] = [
                'id' => $group_id,
                'type' => $row['group_type'],
                'label' => $row['group_label'],
                'values' => []
            ];
        }
        
        // Add value if exists
        if ($row['value_id']) {
            $groups[$group_id]['values'][] = [
                'id' => (int)$row['value_id'],
                'key' => $row['value_key'],
                'name' => $row['value_name'],
                'order' => (int)$row['display_order']
            ];
        }
    }
    
    // Re-index array (remove associative keys)
    $groups = array_values($groups);
    
    echo json_encode([
        'success' => true,
        'language' => $lang,
        'data' => $groups
    ]);
}

/**
 * Fetch all labels (spice levels, tags, customization groups & values)
 */
function getAllLabels($conn, $lang) {
    $all_data = [
        'success' => true,
        'language' => $lang,
        'data' => []
    ];
    
    // Fetch spice levels
    $query_spice = "
        SELECT 
            sl.spice_id,
            sl.spice_key,
            slt.spice_name,
            sl.spice_order
        FROM spice_level sl
        LEFT JOIN spice_level_translation slt 
            ON sl.spice_id = slt.spice_id AND slt.language_code = ?
        ORDER BY sl.spice_order ASC
    ";
    $stmt = $conn->prepare($query_spice);
    $stmt->bind_param("s", $lang);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $spice_levels = [];
    while ($row = $result->fetch_assoc()) {
        $spice_levels[] = [
            'id' => (int)$row['spice_id'],
            'key' => $row['spice_key'],
            'name' => $row['spice_name']
        ];
    }
    $all_data['data']['spiceLevels'] = $spice_levels;
    
    // Fetch tags
    $query_tags = "
        SELECT 
            t.tag_id,
            t.tag_name as tag_key,
            tt.tag_name,
            t.tag_category,
            t.tag_bg_color
        FROM tag t
        LEFT JOIN tag_translation tt 
            ON t.tag_id = tt.tag_id AND tt.language_code = ?
        ORDER BY t.tag_id ASC
    ";
    $stmt = $conn->prepare($query_tags);
    $stmt->bind_param("s", $lang);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $tags = [];
    while ($row = $result->fetch_assoc()) {
        $tags[] = [
            'id' => (int)$row['tag_id'],
            'key' => $row['tag_key'],
            'name' => $row['tag_name'],
            'category' => $row['tag_category']
        ];
    }
    $all_data['data']['tags'] = $tags;
    
    // Fetch customization groups
    $query_cust = "
        SELECT 
            cog.group_id,
            cog.group_type,
            cog_t.group_name as group_label,
            cov.value_id,
            cov.value_name as value_key,
            cov_t.value_name,
            cov.display_order
        FROM customization_option_group cog
        LEFT JOIN customization_option_group_translation cog_t 
            ON cog.group_id = cog_t.group_id AND cog_t.language_code = ?
        LEFT JOIN customization_option_value cov 
            ON cog.group_id = cov.group_id
        LEFT JOIN customization_option_value_translation cov_t 
            ON cov.value_id = cov_t.value_id AND cov_t.language_code = ?
        ORDER BY cog.group_id ASC, cov.display_order ASC
    ";
    $stmt = $conn->prepare($query_cust);
    $stmt->bind_param("ss", $lang, $lang);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $groups = [];
    while ($row = $result->fetch_assoc()) {
        $group_id = (int)$row['group_id'];
        
        if (!isset($groups[$group_id])) {
            $groups[$group_id] = [
                'id' => $group_id,
                'type' => $row['group_type'],
                'label' => $row['group_label'],
                'values' => []
            ];
        }
        
        if ($row['value_id']) {
            $groups[$group_id]['values'][] = [
                'id' => (int)$row['value_id'],
                'key' => $row['value_key'],
                'name' => $row['value_name']
            ];
        }
    }
    
    $all_data['data']['customizationGroups'] = array_values($groups);
    
    echo json_encode($all_data);
}
?>
