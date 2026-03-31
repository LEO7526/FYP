<?php
header('Content-Type: application/json');

$host = 'localhost';
$user = 'root';
$pass = '';
$dbname = 'ProjectDB';

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(['error' => $conn->connect_error]);
    exit;
}

$cid = isset($_GET['cid']) ? intval($_GET['cid']) : 0;
$language = $_GET['lang'] ?? 'en'; // default to English

function normalize_customization_entry(array $entry): array {
    $valueIdsRaw = $entry['selected_value_ids'] ?? [];
    if (is_string($valueIdsRaw)) {
        $decoded = json_decode($valueIdsRaw, true);
        $valueIdsRaw = is_array($decoded) ? $decoded : [];
    }

    $selectedValueIds = [];
    if (is_array($valueIdsRaw)) {
        foreach ($valueIdsRaw as $valueId) {
            if (is_numeric($valueId)) {
                $selectedValueIds[] = (int)$valueId;
            }
        }
    }

    $selectedValuesRaw = $entry['selected_values'] ?? [];
    if (is_string($selectedValuesRaw)) {
        $decoded = json_decode($selectedValuesRaw, true);
        if (json_last_error() === JSON_ERROR_NONE) {
            $selectedValuesRaw = $decoded;
        }
    }

    $selectedValues = [];
    if (is_array($selectedValuesRaw)) {
        foreach ($selectedValuesRaw as $value) {
            if ($value !== null && $value !== '') {
                $selectedValues[] = (string)$value;
            }
        }
    } elseif ($selectedValuesRaw !== null && $selectedValuesRaw !== '') {
        $selectedValues[] = (string)$selectedValuesRaw;
    }

    return [
        'option_id' => (int)($entry['option_id'] ?? 0),
        'group_id' => (int)($entry['group_id'] ?? 0),
        'group_name' => (string)($entry['group_name'] ?? ''),
        'selected_value_ids' => $selectedValueIds,
        'selected_values' => $selectedValues,
        'text_value' => (string)($entry['text_value'] ?? '')
    ];
}

function parse_note_customizations($note): array {
    if (!is_string($note) || trim($note) === '') {
        return [];
    }

    $decoded = json_decode($note, true);
    if (json_last_error() !== JSON_ERROR_NONE || !is_array($decoded)) {
        return [];
    }

    $entries = [];

    // Legacy format: note is directly an array of customization objects.
    if (array_keys($decoded) === range(0, count($decoded) - 1)) {
        $entries = $decoded;
    }

    // Newer object format support if needed in future.
    if (isset($decoded['customizations']) && is_array($decoded['customizations'])) {
        $entries = $decoded['customizations'];
    }

    $customizations = [];
    foreach ($entries as $entry) {
        if (is_array($entry)) {
            $customizations[] = normalize_customization_entry($entry);
        }
    }

    return $customizations;
}

function build_customization_map(mysqli $conn, int $oid, string $language = 'en'): array {
    $map = [];

    $customSql = "
        SELECT
            oic.item_id,
            oic.option_id,
            oic.group_id,
            COALESCE(cogt.group_name, cog.group_name) AS group_name,
            oic.selected_value_ids,
            oic.selected_values,
            oic.text_value
        FROM order_item_customizations oic
        LEFT JOIN customization_option_group cog ON oic.group_id = cog.group_id
        LEFT JOIN customization_option_group_translation cogt
            ON cog.group_id = cogt.group_id AND cogt.language_code = ?
        WHERE oic.oid = ?
    ";

    $customStmt = $conn->prepare($customSql);
    if (!$customStmt) {
        error_log("Prepare failed for customization map: " . $conn->error);
        return $map;
    }

    $customStmt->bind_param("si", $language, $oid);
    $customStmt->execute();
    $customResult = $customStmt->get_result();

    while ($customRow = $customResult->fetch_assoc()) {
        $itemId = (int)$customRow['item_id'];
        if (!isset($map[$itemId])) {
            $map[$itemId] = [];
        }
        $map[$itemId][] = normalize_customization_entry($customRow);
    }

    $customStmt->close();

    // Translate value names in a single batch query when a non-English language is requested
    if ($language !== 'en') {
        // Collect all unique value IDs across the whole map
        $allValueIds = [];
        foreach ($map as $entries) {
            foreach ($entries as $entry) {
                foreach ($entry['selected_value_ids'] as $vid) {
                    $allValueIds[$vid] = true;
                }
            }
        }

        if (!empty($allValueIds)) {
            $idList = implode(',', array_map('intval', array_keys($allValueIds)));
            $batchSql = "
                SELECT cov.value_id,
                       COALESCE(covt.value_name, cov.value_name) AS value_name
                FROM customization_option_value cov
                LEFT JOIN customization_option_value_translation covt
                    ON cov.value_id = covt.value_id AND covt.language_code = ?
                WHERE cov.value_id IN ($idList)
            ";
            $batchStmt = $conn->prepare($batchSql);
            if ($batchStmt) {
                $batchStmt->bind_param("s", $language);
                $batchStmt->execute();
                $batchResult = $batchStmt->get_result();
                $valueTranslations = [];
                while ($batchRow = $batchResult->fetch_assoc()) {
                    $valueTranslations[(int)$batchRow['value_id']] = $batchRow['value_name'];
                }
                $batchStmt->close();

                // Apply translations back to each entry
                foreach ($map as $itemId => &$entries) {
                    foreach ($entries as &$entry) {
                        if (!empty($entry['selected_value_ids'])) {
                            $translated = [];
                            foreach ($entry['selected_value_ids'] as $vid) {
                                if (isset($valueTranslations[$vid])) {
                                    $translated[] = $valueTranslations[$vid];
                                }
                            }
                            if (!empty($translated)) {
                                $entry['selected_values'] = $translated;
                            }
                        }
                    }
                    unset($entry);
                }
                unset($entries);
            }
        }
    }

    return $map;
}

// Include all non-cancelled orders for the customer.
$sql = "
    SELECT
        o.oid,
        o.odate,
        o.ostatus,
        c.cname,
        t.table_number,
        s.sname AS staff_name
    FROM orders o
    LEFT JOIN customer c ON o.cid = c.cid
    LEFT JOIN table_orders t ON o.oid = t.oid
    LEFT JOIN staff s ON t.staff_id = s.sid
    WHERE o.cid = ? AND o.ostatus != 4
    ORDER BY o.odate DESC
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $cid);
$stmt->execute();
$result = $stmt->get_result();

$orders = [];

while ($row = $result->fetch_assoc()) {
    $order = $row;
    $oid = (int)$order['oid'];

    $customizationMap = build_customization_map($conn, $oid, $language);

    $packages = [];
    $reservedQtyByItem = [];

    $packageSql = "
        SELECT
            op.package_id,
            mp.package_name,
            mp.amounts AS package_price,
            op.qty AS quantity,
            op.note
        FROM order_packages op
        JOIN menu_package mp ON op.package_id = mp.package_id
        WHERE op.oid = ?
    ";

    $packageStmt = $conn->prepare($packageSql);
    if (!$packageStmt) {
        error_log("Prepare failed for packages: " . $conn->error);
        $order['packages'] = [];
    } else {
        $packageStmt->bind_param("i", $oid);
        $packageStmt->execute();
        $packageResult = $packageStmt->get_result();

        while ($packageRow = $packageResult->fetch_assoc()) {
            $packageId = (int)$packageRow['package_id'];
            $packageQty = max(1, (int)$packageRow['quantity']);
            $packageNote = $packageRow['note'] ?? '';

            $dishes = [];
            $selectedItemsFromNote = [];

            // Preferred source: package snapshot in order_packages.note (if present).
            $decodedPackageNote = json_decode($packageNote, true);
            if (json_last_error() === JSON_ERROR_NONE
                && is_array($decodedPackageNote)
                && isset($decodedPackageNote['selected_items'])
                && is_array($decodedPackageNote['selected_items'])) {
                $selectedItemsFromNote = $decodedPackageNote['selected_items'];
            }

            if (!empty($selectedItemsFromNote)) {
                $dishMetaSql = "
                    SELECT
                        mi.item_id,
                        mit.item_name,
                        mi.item_price
                    FROM menu_item mi
                    JOIN menu_item_translation mit ON mi.item_id = mit.item_id
                    WHERE mi.item_id = ? AND mit.language_code = ?
                    LIMIT 1
                ";
                $dishMetaStmt = $conn->prepare($dishMetaSql);

                foreach ($selectedItemsFromNote as $selectedItem) {
                    if (!is_array($selectedItem)) {
                        continue;
                    }

                    $dishItemId = (int)($selectedItem['item_id'] ?? ($selectedItem['id'] ?? 0));
                    if ($dishItemId <= 0) {
                        continue;
                    }

                    $dishQty = max(1, (int)($selectedItem['qty'] ?? 1));

                    $dishName = 'Item #' . $dishItemId;
                    $dishPrice = 0.0;

                    if ($dishMetaStmt) {
                        $dishMetaStmt->bind_param("is", $dishItemId, $language);
                        $dishMetaStmt->execute();
                        $dishMetaResult = $dishMetaStmt->get_result();
                        if ($dishMetaResult->num_rows > 0) {
                            $dishMetaRow = $dishMetaResult->fetch_assoc();
                            $dishName = $dishMetaRow['item_name'];
                            $dishPrice = (float)$dishMetaRow['item_price'];
                        }
                    }

                    $customizations = [];
                    if (isset($selectedItem['customizations']) && is_array($selectedItem['customizations'])) {
                        foreach ($selectedItem['customizations'] as $customEntry) {
                            if (is_array($customEntry)) {
                                $customizations[] = normalize_customization_entry($customEntry);
                            }
                        }
                    }

                    if (empty($customizations) && isset($customizationMap[$dishItemId])) {
                        $customizations = $customizationMap[$dishItemId];
                    }

                    $dishes[] = [
                        'item_id' => $dishItemId,
                        'name' => $dishName,
                        'price' => $dishPrice,
                        'quantity' => $dishQty,
                        'customizations' => $customizations
                    ];

                    if (!isset($reservedQtyByItem[$dishItemId])) {
                        $reservedQtyByItem[$dishItemId] = 0;
                    }
                    $reservedQtyByItem[$dishItemId] += $dishQty;
                }

                if ($dishMetaStmt) {
                    $dishMetaStmt->close();
                }
            } else {
                // Legacy fallback: infer package dishes from saved order_items + package_dish mapping.
                $dishSql = "
                    SELECT
                        oi.item_id,
                        mit.item_name,
                        mi.item_price,
                        oi.qty,
                        oi.note
                    FROM order_items oi
                    JOIN package_dish pd ON oi.item_id = pd.item_id AND pd.package_id = ?
                    JOIN menu_item mi ON oi.item_id = mi.item_id
                    JOIN menu_item_translation mit ON mi.item_id = mit.item_id
                    WHERE oi.oid = ? AND mit.language_code = ?
                ";

                $dishStmt = $conn->prepare($dishSql);
                if ($dishStmt) {
                    $dishStmt->bind_param("iis", $packageId, $oid, $language);
                    $dishStmt->execute();
                    $dishResult = $dishStmt->get_result();

                    while ($dishRow = $dishResult->fetch_assoc()) {
                        $dishItemId = (int)$dishRow['item_id'];
                        $dishQty = max(1, (int)$dishRow['qty']);

                        $customizations = $customizationMap[$dishItemId] ?? [];
                        if (empty($customizations)) {
                            $customizations = parse_note_customizations($dishRow['note'] ?? '');
                        }

                        $dishes[] = [
                            'item_id' => $dishItemId,
                            'name' => $dishRow['item_name'],
                            'price' => (float)$dishRow['item_price'],
                            'quantity' => $dishQty,
                            'customizations' => $customizations
                        ];

                        if (!isset($reservedQtyByItem[$dishItemId])) {
                            $reservedQtyByItem[$dishItemId] = 0;
                        }
                        $reservedQtyByItem[$dishItemId] += $dishQty;
                    }

                    $dishStmt->close();
                } else {
                    error_log("Prepare failed for package dishes: " . $conn->error);
                }
            }

            $packages[] = [
                'package_id' => $packageId,
                'package_name' => $packageRow['package_name'],
                'package_price' => (float)$packageRow['package_price'],
                'quantity' => $packageQty,
                'note' => $packageNote,
                'dishes' => $dishes,
                'packageCost' => ((float)$packageRow['package_price']) * $packageQty
            ];
        }

        $packageStmt->close();
        $order['packages'] = $packages;
    }

    // Build top-level regular items and remove quantities already attributed to packages.
    $itemSql = "
        SELECT
            oi.item_id,
            mi.item_price,
            mit.item_name,
            mi.image_url,
            oi.qty AS quantity,
            oi.note
        FROM order_items oi
        JOIN menu_item_translation mit ON oi.item_id = mit.item_id
        JOIN menu_item mi ON mit.item_id = mi.item_id
        WHERE mit.language_code = ? AND oi.oid = ?
    ";

    $itemStmt = $conn->prepare($itemSql);
    $items = [];

    if ($itemStmt) {
        $itemStmt->bind_param("si", $language, $oid);
        $itemStmt->execute();
        $itemResult = $itemStmt->get_result();

        while ($itemRow = $itemResult->fetch_assoc()) {
            $itemId = (int)$itemRow['item_id'];
            $rawQty = (int)$itemRow['quantity'];
            $reservedQty = (int)($reservedQtyByItem[$itemId] ?? 0);
            $displayQty = $rawQty - $reservedQty;

            if ($displayQty <= 0) {
                continue;
            }

            $itemPrice = (float)$itemRow['item_price'];
            $customizations = $customizationMap[$itemId] ?? [];
            if (empty($customizations)) {
                $customizations = parse_note_customizations($itemRow['note'] ?? '');
            }

            $items[] = [
                'item_id' => $itemId,
                'name' => $itemRow['item_name'],
                'quantity' => $displayQty,
                'itemPrice' => $itemPrice,
                'itemCost' => $itemPrice * $displayQty,
                'image_url' => $itemRow['image_url'],
                'customizations' => $customizations,
                'isFromPackage' => false
            ];
        }

        $itemStmt->close();
    } else {
        error_log("Prepare failed for order items: " . $conn->error);
    }

    $order['items'] = $items;
    $orders[] = $order;
}

$stmt->close();
$conn->close();

echo json_encode($orders);
?>
