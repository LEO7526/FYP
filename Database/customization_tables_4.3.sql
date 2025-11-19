-- ================================================================
-- 菜品自訂系統表結構 (用於 YummyRestaurant 4.3+)
-- ================================================================

-- 自訂選項定義表 (每個菜品可以有多個自訂選項)
CREATE TABLE IF NOT EXISTS item_customization_options (
    option_id INT NOT NULL AUTO_INCREMENT,
    item_id INT NOT NULL,
    option_name VARCHAR(255) NOT NULL,      -- 例如: "Spice Level", "Side Dish"
    option_type ENUM('single_choice','multi_choice','quantity','text_note') NOT NULL DEFAULT 'single_choice',
    is_required TINYINT(1) NOT NULL DEFAULT 0,  -- 是否必填
    max_selections INT DEFAULT NULL,         -- 多選時的最大選擇數
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (option_id),
    FOREIGN KEY (item_id) REFERENCES menu_item(item_id) ON DELETE CASCADE,
    KEY idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 自訂選項的具體選擇項 (例如：辛辣度選項中的 "Mild", "Medium", "Hot")
CREATE TABLE IF NOT EXISTS customization_option_choices (
    choice_id INT NOT NULL AUTO_INCREMENT,
    option_id INT NOT NULL,
    choice_name VARCHAR(255) NOT NULL,
    additional_cost DECIMAL(10,2) DEFAULT 0,  -- 例如：加起司需要額外費用
    display_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (choice_id),
    FOREIGN KEY (option_id) REFERENCES item_customization_options(option_id) ON DELETE CASCADE,
    KEY idx_option_id (option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 訂單項目的自訂詳情 (保存客戶的實際選擇)
CREATE TABLE IF NOT EXISTS order_item_customizations (
    customization_id INT NOT NULL AUTO_INCREMENT,
    oid INT NOT NULL,                        -- 訂單 ID
    oid_item_id INT NOT NULL,                -- order_items 的複合 ID (oid, item_id)
    option_id INT NOT NULL,
    option_name VARCHAR(255) NOT NULL,
    choice_ids JSON DEFAULT NULL,             -- 多個選擇的 IDs 陣列
    choice_names JSON DEFAULT NULL,           -- 多個選擇的名稱陣列
    text_value VARCHAR(500) DEFAULT NULL,    -- 文字備註
    additional_cost DECIMAL(10,2) DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customization_id),
    FOREIGN KEY (oid) REFERENCES orders(oid) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES item_customization_options(option_id) ON DELETE CASCADE,
    KEY idx_order_id (oid),
    KEY idx_option_id (option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 示例數據：為菜品添加自訂選項
-- ================================================================

-- 為菜品 "麻婆豆腐" (item_id=6) 添加辛辣度自訂選項
INSERT INTO item_customization_options (item_id, option_name, option_type, is_required, max_selections)
VALUES 
(6, 'Spice Level', 'single_choice', 1, 1),
(6, 'Special Requests', 'text_note', 0, NULL);

-- 為辛辣度選項添加具體選擇
INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
SELECT option_id, choice_name, 0, display_order FROM (
    SELECT (SELECT option_id FROM item_customization_options WHERE item_id=6 AND option_name='Spice Level') as option_id,
           'Mild' as choice_name, 1 as display_order
    UNION ALL
    SELECT (SELECT option_id FROM item_customization_options WHERE item_id=6 AND option_name='Spice Level'),
           'Medium', 2
    UNION ALL
    SELECT (SELECT option_id FROM item_customization_options WHERE item_id=6 AND option_name='Spice Level'),
           'Hot', 3
    UNION ALL
    SELECT (SELECT option_id FROM item_customization_options WHERE item_id=6 AND option_name='Spice Level'),
           'Numbing', 4
) temp
WHERE option_id IS NOT NULL;

-- ================================================================
-- 檢視表：方便查詢菜品的自訂選項
-- ================================================================

CREATE OR REPLACE VIEW v_item_customizations AS
SELECT 
    ico.option_id,
    ico.item_id,
    ico.option_name,
    ico.option_type,
    ico.is_required,
    ico.max_selections,
    JSON_ARRAYAGG(
        JSON_OBJECT(
            'choice_id', cooc.choice_id,
            'choice_name', cooc.choice_name,
            'additional_cost', cooc.additional_cost
        ) ORDER BY cooc.display_order
    ) as choices
FROM item_customization_options ico
LEFT JOIN customization_option_choices cooc ON ico.option_id = cooc.option_id
GROUP BY ico.option_id;
