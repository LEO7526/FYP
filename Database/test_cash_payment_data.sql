-- 測試現金支付管理功能的測試數據
-- 此腳本創建一些測試訂單來驗證現金支付確認功能

USE ProjectDB;

-- 插入一些現金支付的測試訂單 (ostatus=0 表示待付款)
INSERT INTO orders (odate, cid, ostatus, orderRef, order_type, table_number, payment_method, payment_intent_id) VALUES
('2026-02-04 18:30:00', 1, 0, 'cash_order_001', 'dine_in', 5, 'cash', 'cash_1707049800'),
('2026-02-04 19:15:00', 2, 0, 'cash_order_002', 'dine_in', 12, 'cash', 'cash_1707052500'),
('2026-02-04 20:00:00', 4, 0, 'cash_order_003', 'dine_in', 8, 'cash', 'cash_1707055200');

-- 獲取剛插入的訂單ID
SET @order1_id = LAST_INSERT_ID() - 2;
SET @order2_id = LAST_INSERT_ID() - 1;
SET @order3_id = LAST_INSERT_ID();

-- 為這些訂單添加訂單項目
INSERT INTO order_items (oid, item_id, qty) VALUES
-- 第一個訂單 (桌號5)
(@order1_id, 3, 2),  -- 2x 宮保雞丁
(@order1_id, 12, 1), -- 1x 冰檸茶
(@order1_id, 19, 2), -- 2x 米飯

-- 第二個訂單 (桌號12)
(@order2_id, 5, 1),  -- 1x 麻婆豆腐
(@order2_id, 14, 2), -- 2x 牛奶
(@order2_id, 20, 1), -- 1x 麵

-- 第三個訂單 (桌號8)
(@order3_id, 7, 1),  -- 1x 擔擔麵
(@order3_id, 16, 1), -- 1x 檸檬茶
(@order3_id, 21, 1); -- 1x 薯粉

-- 顯示插入的測試數據
SELECT 
    o.oid,
    o.table_number,
    c.cname as customer_name,
    o.odate,
    o.ostatus,
    o.payment_method,
    GROUP_CONCAT(CONCAT(mit.item_name, ' x', oi.qty) SEPARATOR ', ') as items
FROM orders o
LEFT JOIN customer c ON o.cid = c.cid
LEFT JOIN order_items oi ON o.oid = oi.oid
LEFT JOIN menu_item mi ON oi.item_id = mi.item_id
LEFT JOIN menu_item_translation mit ON mi.item_id = mit.item_id AND mit.language_code = 'zh-TW'
WHERE o.payment_method = 'cash' AND o.ostatus = 0
GROUP BY o.oid
ORDER BY o.odate DESC;

-- 顯示總金額
SELECT 
    o.oid,
    o.table_number,
    SUM(mi.price * oi.qty) as total_amount
FROM orders o
JOIN order_items oi ON o.oid = oi.oid
JOIN menu_item mi ON oi.item_id = mi.item_id
WHERE o.payment_method = 'cash' AND o.ostatus = 0
GROUP BY o.oid;

COMMIT;