-- MySQL dump 10.13 Distrib 8.0.40, for Win64 (x86_64)
-- Host: localhost Database: projectdb
-- Server version 8.4.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+08:00";

-- Database: ProjectDB
DROP DATABASE IF EXISTS ProjectDB;
CREATE DATABASE IF NOT EXISTS ProjectDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ProjectDB;

-- Table structure for table staff
DROP TABLE IF EXISTS staff;
CREATE TABLE staff (
sid int NOT NULL AUTO_INCREMENT,
semail varchar(191) NOT NULL, -- Added email column
spassword varchar(255) NOT NULL,
sname varchar(255) NOT NULL,
srole varchar(45) DEFAULT NULL,
stel int DEFAULT NULL,
simageurl VARCHAR(255) NULL,
PRIMARY KEY (sid),
UNIQUE KEY semail_UNIQUE (semail) -- Ensure emails are unique
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table staff
INSERT INTO staff (semail, spassword, sname, srole, stel,simageurl) VALUES
('peter.wong@example.com', 'password123', 'Peter Wong', 'staff', 25669197,null),
('tina.chan@example.com', 'letmein456', 'Tina Chan', 'Production Supervisor', 31233123,null),
('alex.lam@example.com', 'qwerty789', 'Alex Lam', 'Warehouse Clerk', 29881234,null),
('susan.leung@example.com', 'helloWorld1', 'Susan Leung', 'HR Officer', 28889999,null),
('john.ho@example.com', 'changeme2023', 'John Ho', 'Engineer', 29998888,null),
('maggie.tse@example.com', 'maggiePass!', 'Maggie Tse', 'Accountant', 23881211,null),
('kevin.ng@example.com', 'ngfamily', 'Kevin Ng', 'IT Support', 27889977,null),
('emily.tsui@example.com', 'emily2024', 'Emily Tsui', 'Marketing Lead', 26543210,null);

DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
  cid INT NOT NULL AUTO_INCREMENT,
  cname VARCHAR(255) NOT NULL,
  cpassword VARCHAR(255) NOT NULL,
  ctel INT DEFAULT NULL,
  caddr TEXT,
  company VARCHAR(255) DEFAULT NULL,
  cemail VARCHAR(191) NOT NULL UNIQUE,
  cbirthday CHAR(5) DEFAULT NULL,   -- store as MM-DD only
  crole VARCHAR(45) NOT NULL DEFAULT 'customer',
  cimageurl VARCHAR(255) NULL,
  coupon_point INT NOT NULL DEFAULT 0,
  PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample data
INSERT INTO customer (cname, cpassword, ctel, caddr, company, cemail, cbirthday, crole, cimageurl) VALUES
('Alex Wong', 'password', 21232123, 'G/F, ABC Building, King Yip Street, KwunTong, Kowloon, Hong Kong',
 'Fat Cat Company Limited', 'alex.wong@example.com', NULL, 'customer', NULL),
('Tina Chan', 'password', 31233123, '303, Mei Hing Center, Yuen Long, NT, Hong Kong',
 'XDD LOL Company', 'tina.chan@example.com', '07-20', 'customer', NULL),
('Bowie', 'password', 61236123, '401, Sing Kei Building, Kowloon, Hong Kong',
 'GPA4 Company', 'bowie@example.com', '03-15', 'customer', NULL),
('Samuel Lee', 'samuelpass', 61231212, '111, Example Road, Central, Hong Kong',
 'Lee Family Co', 'samuel.lee@example.com', '11-02', 'customer', NULL),
('Emily Tsang', 'emilypass', 61231555, '88, Happy Valley Road, Hong Kong',
 'Happy Valley Enterprises', 'emily.tsang@example.com', '01-30', 'customer', NULL);

-- Insert a default walk-in customer with cid = 0
INSERT INTO customer (
cid, cname, cpassword, ctel, caddr, company, cemail, crole, cimageurl
) VALUES (
0, 'Walk-in Customer', 'walkin', NULL, NULL, NULL, 'walkin@system.local', 'customer', NULL
);



-- Coupon master table
DROP TABLE IF EXISTS coupons;
CREATE TABLE coupons (
  coupon_id INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  points_required INT NOT NULL DEFAULT 0,
  type ENUM('cash','percent','free_item') NOT NULL DEFAULT 'cash',
  discount_amount INT DEFAULT 0,              -- in cents for cash, or percentage value for percent
  item_category VARCHAR(50) DEFAULT NULL,     -- e.g. 'drink' for free_item
  expiry_date DATE DEFAULT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE coupons
SET type = 'free_item',
    discount_amount = 0,
    item_category = 'drink'
WHERE title = 'Free Drink';

ALTER TABLE coupons
  DROP COLUMN title,
  DROP COLUMN description;


INSERT INTO coupons (points_required, type, discount_amount, item_category, expiry_date, is_active) VALUES
(100, 'percent', 10, NULL, '2025-12-31', 1),
(50, 'free_item', 0, 'drink', '2025-12-30', 1),
(200, 'cash', 5000, NULL, '2025-12-31', 1), -- 5000 cents = HK$50
(0, 'free_item', 0, NULL, NULL, 1); -- free 1 main dish


-- Defines categories like Appetizers, Soup, etc
CREATE TABLE menu_category (
category_id INT PRIMARY KEY AUTO_INCREMENT,
category_name VARCHAR(100) NOT NULL
);


INSERT INTO menu_category (category_name) VALUES
('Appetizers'),
('Soup'),
('Main Courses'),
('Dessert'),
('Drink');

-- Stores individual dishes
CREATE TABLE menu_item (
item_id INT PRIMARY KEY AUTO_INCREMENT,
category_id INT NOT NULL,
item_price DECIMAL(10,2) NOT NULL,
image_url VARCHAR(255),
spice_level INT NOT NULL CHECK (spice_level BETWEEN 0 AND 5),
is_available BOOLEAN DEFAULT TRUE,
FOREIGN KEY (category_id) REFERENCES menu_category(category_id)
);



-- Appetizers
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(1, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/1.jpg', 1, TRUE),
(1, 26.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/2.jpg', 1, TRUE),
(1, 32.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/3.jpg', 3, TRUE);

-- Soup
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(2, 48.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/4.jpg', 2, TRUE);

-- Main Courses
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(3, 95.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/5.jpg', 5, TRUE),
(3, 42.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/6.jpg', 3, TRUE),
(3, 38.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/7.jpg', 4, TRUE),
(3, 88.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/8.jpg', 2, TRUE),
(3, 58.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/9.jpg', 4, TRUE),
(3, 66.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/10.jpg', 2, TRUE);

-- Dessert
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(4, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/11.jpg', 0, TRUE);

-- Drink 
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/12.jpg', 0, TRUE),
(5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/13.jpg', 0, TRUE),
(5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/14.jpg', 0, TRUE),
(5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/15.jpg', 0, TRUE),
(5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/16.jpg', 0, TRUE),
(5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/17.jpg', 0, TRUE),
(5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/18.jpg', 0, TRUE);



DROP TABLE IF EXISTS orders;

-- Create orders table (order header)
CREATE TABLE orders (
oid INT NOT NULL AUTO_INCREMENT, -- Order ID
odate DATETIME NOT NULL, -- Order date
cid INT NOT NULL, -- Customer ID
ostatus INT NOT NULL, -- Order status
note TEXT DEFAULT NULL, -- Order note
PRIMARY KEY (oid),
CONSTRAINT fk_orders_cid FOREIGN KEY (cid) REFERENCES customer(cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE orders
ADD COLUMN orderRef VARCHAR(100) NOT NULL UNIQUE AFTER ostatus;

ALTER TABLE orders ADD COLUMN coupon_id INT NULL,
    ADD CONSTRAINT fk_orders_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id);



-- Dumping data for table orders
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id) VALUES
(1, '2025-04-12 17:50:00', 1, 1, 'order_20250412A', NULL),
(2, '2025-04-13 12:01:00', 2, 3, 'order_20250413B', 1);



CREATE TABLE coupon_translation (
  translation_id INT AUTO_INCREMENT PRIMARY KEY,
  coupon_id INT NOT NULL,
  language_code VARCHAR(10) NOT NULL, -- 'en', 'zh-CN', 'zh-TW'
  title VARCHAR(255) NOT NULL,
  description TEXT,
  FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE
);


-- Coupon 1: 10% OFF Any Order
INSERT INTO coupon_translation (coupon_id, language_code, title, description) VALUES
(1, 'en', '10% OFF Any Order', 'Get 10% discount on your next order.'),
(1, 'zh-CN', '全单九折', '下次消费可享受九折优惠。'),
(1, 'zh-TW', '全單九折', '下次消費可享受九折優惠。');

-- Coupon 2: Free Drink
INSERT INTO coupon_translation (coupon_id, language_code, title, description) VALUES
(2, 'en', 'Free Drink', 'Redeem one free drink of your choice.'),
(2, 'zh-CN', '免费饮品', '兑换一杯您选择的免费饮品。'),
(2, 'zh-TW', '免費飲品', '兌換一杯您選擇的免費飲品。');

-- Coupon 3: HK$50 OFF
INSERT INTO coupon_translation (coupon_id, language_code, title, description) VALUES
(3, 'en', 'HK$50 OFF', 'Enjoy HK$50 off when you spend HK$300 or more.'),
(3, 'zh-CN', '立减50港元', '消费满300港元即可减50港元。'),
(3, 'zh-TW', '立減50港元', '消費滿300港元即可減50港元。');

-- Coupon 4: Birthday Special
INSERT INTO coupon_translation (coupon_id, language_code, title, description) VALUES
(4, 'en', 'Birthday Special', 'Exclusive coupon for your birthday month.'),
(4, 'zh-CN', '生日特惠', '生日月份专属优惠券。'),
(4, 'zh-TW', '生日特惠', '生日月份專屬優惠券。');


CREATE TABLE coupon_terms (
    term_id INT AUTO_INCREMENT PRIMARY KEY,
    coupon_id INT NOT NULL,
    language_code VARCHAR(10) NOT NULL,   -- 'en', 'zh-CN', 'zh-TW'
    term_text VARCHAR(500) NOT NULL,
    FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Coupon 1: 10% OFF Any Order
INSERT INTO coupon_terms (coupon_id, language_code, term_text) VALUES
(1, 'en', 'Valid for dine-in and takeaway orders'),
(1, 'en', 'Not applicable to delivery'),
(1, 'en', 'Cannot be combined with other discounts'),
(1, 'zh-CN', '适用于堂食和外卖'),
(1, 'zh-CN', '不适用于外送服务'),
(1, 'zh-CN', '不可与其他优惠同时使用'),
(1, 'zh-TW', '適用於堂食和外賣'),
(1, 'zh-TW', '不適用於外送服務'),
(1, 'zh-TW', '不可與其他優惠同時使用');

-- Coupon 2: Free Drink
INSERT INTO coupon_terms (coupon_id, language_code, term_text) VALUES
(2, 'en', 'Choice of soft drink, coffee, or tea'),
(2, 'en', 'Limit one free drink per customer per day'),
(2, 'zh-CN', '可选择汽水、咖啡或茶'),
(2, 'zh-CN', '每位顾客每天限兑一杯'),
(2, 'zh-TW', '可選擇汽水、咖啡或茶'),
(2, 'zh-TW', '每位顧客每天限兌一杯');

-- Coupon 3: HK$50 OFF
INSERT INTO coupon_terms (coupon_id, language_code, term_text) VALUES
(3, 'en', 'Minimum spend of HK$300 required'),
(3, 'en', 'Discount applied before service charge'),
(3, 'zh-CN', '需满300港元方可使用'),
(3, 'zh-CN', '折扣在加收服务费前计算'),
(3, 'zh-TW', '需滿300港元方可使用'),
(3, 'zh-TW', '折扣於加收服務費前計算');

-- Coupon 4: Birthday Special
INSERT INTO coupon_terms (coupon_id, language_code, term_text) VALUES
(4, 'en', 'Valid only during your birthday month'),
(4, 'en', 'Must present valid ID for verification'),
(4, 'zh-CN', '仅限生日月份使用'),
(4, 'zh-CN', '需出示有效身份证明'),
(4, 'zh-TW', '僅限生日月份使用'),
(4, 'zh-TW', '需出示有效身份證明');

-- English disclaimers
INSERT INTO coupon_terms (coupon_id, language_code, term_text)
SELECT c.coupon_id, 'en', 'Photos are for reference only; actual products may vary'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4
UNION ALL
SELECT c.coupon_id, 'en', 'Coupons cannot be exchanged for cash, credit, or other products'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4
UNION ALL
SELECT c.coupon_id, 'en', 'Yummy Restaurant reserves the right to cancel, amend, or change the terms and conditions without prior notice'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4
UNION ALL
SELECT c.coupon_id, 'en', 'In case of product unavailability, the company may replace the coupon with an item of equal or greater value'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4;

-- Traditional Chinese disclaimers
INSERT INTO coupon_terms (coupon_id, language_code, term_text)
SELECT c.coupon_id, 'zh-TW', '圖片只供參考，實際供應可能有所不同'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4
UNION ALL
SELECT c.coupon_id, 'zh-TW', '優惠券不可兌換現金、信用額或其他產品'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4
UNION ALL
SELECT c.coupon_id, 'zh-TW', 'Yummy Restaurant 保留隨時取消、更改或修訂條款及細則之權利，恕不另行通知'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4
UNION ALL
SELECT c.coupon_id, 'zh-TW', '如有產品缺貨，公司可更換為同等或更高價值之食品'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4;

-- Simplified Chinese disclaimers
INSERT INTO coupon_terms (coupon_id, language_code, term_text)
SELECT c.coupon_id, 'zh-CN', '图片仅供参考，实际供应可能有所不同'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4
UNION ALL
SELECT c.coupon_id, 'zh-CN', '优惠券不可兑换现金、信用额或其他产品'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4
UNION ALL
SELECT c.coupon_id, 'zh-CN', 'Yummy Restaurant 保留随时取消、更改或修订条款及细则的权利，恕不另行通知'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4
UNION ALL
SELECT c.coupon_id, 'zh-CN', '如有产品缺货，公司可更换为同等或更高价值的食品'
FROM coupons c WHERE c.coupon_id BETWEEN 1 AND 4;


CREATE TABLE coupon_rules (
    rule_id INT AUTO_INCREMENT PRIMARY KEY,
    coupon_id INT NOT NULL,
    
    -- Scope of coupon
    applies_to ENUM('whole_order','category','item','package') NOT NULL DEFAULT 'whole_order',
    
    -- Discount logic
    discount_type ENUM('percent','cash','free_item') NOT NULL,
    discount_value DECIMAL(10,2) DEFAULT NULL,   -- e.g. 10% or 50.00 HKD
    
    -- Conditions
    min_spend DECIMAL(10,2) DEFAULT NULL,        -- e.g. HK$300 minimum
    max_discount DECIMAL(10,2) DEFAULT NULL,     -- cap for percentage discounts
    per_customer_per_day INT DEFAULT NULL,       -- usage limit
    
    -- Flags
    valid_dine_in TINYINT(1) NOT NULL DEFAULT 0,
    valid_takeaway TINYINT(1) NOT NULL DEFAULT 0,
    valid_delivery TINYINT(1) NOT NULL DEFAULT 0,
    combine_with_other_discounts TINYINT(1) NOT NULL DEFAULT 1,
    birthday_only TINYINT(1) NOT NULL DEFAULT 0,
    
    FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Coupon 1: 10% OFF Any Order
INSERT INTO coupon_rules (coupon_id, applies_to, discount_type, discount_value, min_spend, valid_dine_in, valid_takeaway, valid_delivery, combine_with_other_discounts)
VALUES (1, 'whole_order', 'percent', 10, NULL, 1, 1, 0, 0);

-- Coupon 2: Free Drink
INSERT INTO coupon_rules (coupon_id, applies_to, discount_type, discount_value, per_customer_per_day, valid_dine_in, valid_takeaway, valid_delivery)
VALUES (2, 'item', 'free_item', 1, 1, 1, 1, 1);

-- Coupon 3: HK$50 OFF (min spend HK$300)
INSERT INTO coupon_rules (coupon_id, applies_to, discount_type, discount_value, min_spend, valid_dine_in, valid_takeaway, valid_delivery)
VALUES (3, 'whole_order', 'cash', 50.00, 300.00, 1, 1, 1);

-- Coupon 4: Birthday Special – Free Main Dish
INSERT INTO coupon_rules (coupon_id, applies_to, discount_type, discount_value, birthday_only, valid_dine_in, valid_takeaway, valid_delivery)
VALUES (4, 'category', 'free_item', 1, 1, 1, 1, 1);

-- History of all point changes (earn/redeem), now with coupon_id
DROP TABLE IF EXISTS coupon_point_history;
CREATE TABLE coupon_point_history (
  cph_id INT NOT NULL AUTO_INCREMENT,
  cid INT NOT NULL,
  coupon_id INT NULL,  -- direct link to coupons
  delta INT NOT NULL,
  resulting_points INT NOT NULL,
  action VARCHAR(50) NOT NULL,
  note VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (cph_id),
  KEY idx_cph_cid (cid),
  KEY idx_cph_coupon_id (coupon_id),
  CONSTRAINT fk_cph_cid FOREIGN KEY (cid) REFERENCES customer(cid) ON DELETE CASCADE,
  CONSTRAINT fk_cph_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- 🔄 Backfill old rows (run only if you already had history data before adding coupon_id)
UPDATE coupon_point_history h
SET h.coupon_id = CAST(SUBSTRING_INDEX(h.note, ' ', -1) AS UNSIGNED)
WHERE h.note LIKE 'Coupon ID %' AND h.coupon_id IS NULL;


-- Track actual coupon redemptions
DROP TABLE IF EXISTS coupon_redemptions;
CREATE TABLE coupon_redemptions (
  redemption_id INT NOT NULL AUTO_INCREMENT,
  coupon_id INT NOT NULL,
  cid INT NOT NULL, -- customer who redeemed
  redeemed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (redemption_id),
  CONSTRAINT fk_redemption_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id),
  CONSTRAINT fk_redemption_customer FOREIGN KEY (cid) REFERENCES customer(cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE coupon_redemptions
ADD COLUMN is_used TINYINT(1) NOT NULL DEFAULT 0,
ADD COLUMN used_at DATETIME NULL;


-- For multilingual support
CREATE TABLE menu_item_translation (
translation_id INT PRIMARY KEY AUTO_INCREMENT,
item_id INT NOT NULL,
language_code VARCHAR(10) NOT NULL, -- 'en', 'zh-CN', 'zh-TW'
item_name VARCHAR(255) NOT NULL,
item_description TEXT,
FOREIGN KEY (item_id) REFERENCES menu_item(item_id)
);

-- Pickled Cucumber Flowers
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(1, 'en', 'Pickled Cucumber Flowers', 'Delicate cucumber blossoms pickled with aromatic spices.'),
(1, 'zh-CN', '腌制黄瓜花', '用香料腌制的黄瓜花，清爽可口。'),
(1, 'zh-TW', '醃製黃瓜花', '以香料醃製的黃瓜花，清新爽口。');

-- Spicy Wood Ear Mushrooms
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(2, 'en', 'Spicy Wood Ear Mushrooms', 'Black fungus tossed in vinegar, garlic, and chili oil.'),
(2, 'zh-CN', '麻辣木耳', '黑木耳拌醋、蒜和辣油，爽口开胃。'),
(2, 'zh-TW', '麻辣木耳', '黑木耳拌醋、蒜與辣油，爽口開胃。');

-- Mouthwatering Chicken
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(3, 'en', 'Mouthwatering Chicken', 'Poached chicken drenched in spicy Sichuan chili sauce.'),
(3, 'zh-CN', '口水鸡', '嫩鸡浸泡在麻辣红油中，香辣诱人。'),
(3, 'zh-TW', '口水雞', '嫩雞浸泡在麻辣紅油中，香辣誘人。');

-- Suan Cai Fish Soup
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(4, 'en', 'Suan Cai Fish Soup', 'Sliced fish simmered in pickled mustard greens and chili broth.'),
(4, 'zh-CN', '酸菜鱼汤', '鱼片炖酸菜和辣汤，酸辣开胃。'),
(4, 'zh-TW', '酸菜魚湯', '魚片燉酸菜與辣湯，酸辣開胃。');

-- Chongqing-style Angus Beef
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(5, 'en', 'Chongqing-style Angus Beef', 'Spicy Angus beef with bean paste and lemongrass, known for its numbing effect.'),
(5, 'zh-CN', '重庆风味安格斯牛肉', '辣味安格斯牛肉配豆瓣酱和香茅，麻辣持久。'),
(5, 'zh-TW', '重慶風味安格斯牛肉', '辣味安格斯牛肉搭配豆瓣醬與香茅，麻辣持久。');

-- Mapo Tofu
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(6, 'en', 'Mapo Tofu', 'Silken tofu in spicy bean paste sauce with minced beef and numbing Sichuan peppercorns.'),
(6, 'zh-CN', '麻婆豆腐', '嫩豆腐配牛肉末和麻辣豆瓣酱，风味十足。'),
(6, 'zh-TW', '麻婆豆腐', '嫩豆腐搭配牛肉末與麻辣豆瓣醬，風味十足。');

-- Dan Dan Noodles
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(7, 'en', 'Dan Dan Noodles', 'Spicy noodles topped with minced pork, preserved vegetables, and chili oil.'),
(7, 'zh-CN', '担担面', '辣味面条配猪肉末、芽菜和红油，香辣诱人。'),
(7, 'zh-TW', '擔擔麵', '辣味麵條搭配豬肉末、芽菜與紅油，香辣誘人。');

-- Twice-Cooked Pork
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(8, 'en', 'Twice-Cooked Pork', 'Pork belly simmered then stir-fried with leeks and chili bean paste for a rich, savory flavor.'),
(8, 'zh-CN', '回锅肉', '五花肉先煮后炒，搭配蒜苗和豆瓣酱，香浓可口。'),
(8, 'zh-TW', '回鍋肉', '五花肉先煮後炒，搭配蒜苗與豆瓣醬，香濃可口。');

-- Boiled Beef in Chili Broth
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(9, 'en', 'Boiled Beef in Chili Broth', 'Tender beef slices in a fiery broth with Sichuan peppercorns.'),
(9, 'zh-CN', '水煮牛肉', '牛肉片浸泡在麻辣红汤中，香辣过瘾。'),
(9, 'zh-TW', '水煮牛肉', '牛肉片浸泡在麻辣紅湯中，香辣過癮。');

-- Fish-Fragrant Eggplant
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(10, 'en', 'Fish-Fragrant Eggplant', 'Braised eggplant in garlic, ginger, and sweet chili sauce.'),
(10, 'zh-CN', '鱼香茄子', '茄子炖煮于蒜姜和甜辣酱中，香气扑鼻。'),
(10, 'zh-TW', '魚香茄子', '茄子燉煮於蒜薑與甜辣醬中，香氣撲鼻。');

-- Sichuan Glutinous Rice Cake
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(11, 'en', 'Sichuan Glutinous Rice Cake', 'Sticky rice cake with brown sugar and sesame.'),
(11, 'zh-CN', '四川糯米糕', '糯米糕配红糖和芝麻，甜而不腻。'),
(11, 'zh-TW', '四川糯米糕', '糯米糕搭配紅糖與芝麻，甜而不膩。');

-- 咸檸7 (Salty Lemon 7-Up)
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(12, 'en', 'Salty Lemon 7-Up', 'Classic Hong Kong salty lemon soda with 7-Up.'),
(12, 'zh-CN', '咸柠7', '港式经典咸柠七喜，清爽解渴。'),
(12, 'zh-TW', '咸檸7', '港式經典鹹檸七喜，清爽解渴。');

-- 紅豆冰 (Red Bean Ice)
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(13, 'en', 'Red Bean Ice', 'Sweet red beans served over crushed ice.'),
(13, 'zh-CN', '红豆冰', '香甜红豆配上碎冰，夏日必备。'),
(13, 'zh-TW', '紅豆冰', '香甜紅豆配上碎冰，夏日必備。');

-- 熱奶茶 (Hot Milk Tea)
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(14, 'en', 'Hot Milk Tea', 'Rich Hong Kong-style milk tea, best served hot.'),
(14, 'zh-CN', '热奶茶', '浓郁港式奶茶，热饮最佳。'),
(14, 'zh-TW', '熱奶茶', '濃郁港式奶茶，熱飲最佳。');

-- 葡萄烏龍茶 (Grape Oolong Tea)
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(15, 'en', 'Grape Oolong Tea', 'Oolong tea infused with grape aroma, refreshing and unique.'),
(15, 'zh-CN', '葡萄乌龙茶', '乌龙茶融合葡萄香气，清新独特。'),
(15, 'zh-TW', '葡萄烏龍茶', '烏龍茶融合葡萄香氣，清新獨特。');

-- 熱檸茶 (Hot Lemon Tea)
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(16, 'en', 'Hot Lemon Tea', 'Hot lemon tea, tangy and comforting.'),
(16, 'zh-CN', '热柠茶', '热柠檬茶，酸甜暖心。'),
(16, 'zh-TW', '熱檸茶', '熱檸檬茶，酸甜暖心。');

-- 凍奶茶 (Iced Milk Tea)
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(17, 'en', 'Iced Milk Tea', 'Classic Hong Kong-style milk tea, served chilled.'),
(17, 'zh-CN', '冻奶茶', '经典港式奶茶，冰凉爽口。'),
(17, 'zh-TW', '凍奶茶', '經典港式奶茶，冰涼爽口。');

-- 凍檸茶 (Iced Lemon Tea)
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(18, 'en', 'Iced Lemon Tea', 'Crisp iced tea with fresh lemon slices.'),
(18, 'zh-CN', '冻柠茶', '冰镇柠檬茶，清爽解渴。'),
(18, 'zh-TW', '凍檸茶', '冰鎮檸檬茶，清爽解渴。');



-- Create tag table
CREATE TABLE tag (
tag_id INT NOT NULL AUTO_INCREMENT,
tag_name VARCHAR(255) NOT NULL,
tag_category VARCHAR(255) NOT NULL,
tag_bg_color VARCHAR(7) DEFAULT NULL,
PRIMARY KEY (tag_id),
UNIQUE KEY (tag_name)
);






















-- Coupon applies to specific menu items
CREATE TABLE coupon_applicable_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    coupon_id INT NOT NULL,
    item_id INT NOT NULL,
    FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES menu_item(item_id) ON DELETE CASCADE
);


-- Coupon 2: Free Drink applies to all drinks (IDs 12–18 in your menu_item table)
INSERT INTO coupon_applicable_items (coupon_id, item_id) VALUES
(2, 12),(2, 13),(2, 14),(2, 15),(2, 16),(2, 17),(2, 18);

-- Coupon 3: HK$50 OFF applies to all items (no restriction, so no rows needed here)

-- Coupon 4: Birthday Special – could apply to one free main dish (example: Mapo Tofu item_id=6)
INSERT INTO coupon_applicable_items (coupon_id, item_id) VALUES
(4, 6);







-- Coupon applies to specific categories (e.g. "Main Courses")
CREATE TABLE coupon_applicable_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    coupon_id INT NOT NULL,
    category_id INT NOT NULL,
    FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES menu_category(category_id) ON DELETE CASCADE
);

-- Coupon 4: Birthday Special applies to Main Courses category (category_id=3)
INSERT INTO coupon_applicable_categories (coupon_id, category_id) VALUES
(4, 3);


CREATE TABLE order_coupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    oid INT NOT NULL,             -- order ID
    coupon_id INT NOT NULL,       -- coupon used
    redemption_id INT DEFAULT NULL, -- optional link to coupon_redemptions
    discount_amount DECIMAL(10,2) DEFAULT NULL, -- actual discount applied
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (oid) REFERENCES orders(oid) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE,
    FOREIGN KEY (redemption_id) REFERENCES coupon_redemptions(redemption_id) ON DELETE SET NULL
);

-- Order 1 used Coupon 1 (10% OFF Any Order), discount HK$20
INSERT INTO order_coupons (oid, coupon_id, discount_amount)
VALUES (1, 1, 20.00);

-- Order 2 used Coupon 2 (Free Drink), discount HK$22
INSERT INTO order_coupons (oid, coupon_id, discount_amount)
VALUES (2, 2, 22.00);


-- Insert tags data
INSERT INTO tag (tag_name, tag_category, tag_bg_color) VALUES
('vegetarian', 'Dietary', '#4CAF50'),
('refreshing', 'Characteristic', '#2196F3'),
('chicken', 'Protein', '#FFC107'),
('cold', 'Temperature', '#03A9F4'),
('spicy', 'Flavor', '#F44336'),
('fish', 'Protein', '#3F51B5'),
('sour', 'Flavor', '#FF9800'),
('beef', 'Protein', '#E91E63'),
('numbing', 'Flavor', '#9C27B0'),
('tofu', 'Protein', '#009688'),
('noodles', 'Type', '#673AB7'),
('pork', 'Protein', '#FF5722'),
('streetfood', 'Type', '#795548'),
('stirfry', 'Cooking Method', '#8BC34A'),
('classic', 'Characteristic', '#00BCD4'),
('sweet', 'Flavor', '#FFEB3B'),
('glutinous', 'Type', '#607D8B');

-- New tags for drinks
INSERT INTO tag (tag_name, tag_category, tag_bg_color) VALUES
('lemon', 'Flavor', '#FFEB3B'),         -- for lemon-based drinks
('grape', 'Flavor', '#9C27B0'),         -- for grape oolong
('milk', 'Ingredient', '#795548'),      -- for milk tea
('soda', 'Type', '#03A9F4'),            -- for fizzy drinks like 咸檸7
('traditional', 'Characteristic', '#607D8B'); -- for classic HK-style drinks


-- Create menu_tag table
CREATE TABLE menu_tag (
item_id INT NOT NULL,
tag_id INT NOT NULL,
PRIMARY KEY (item_id, tag_id),
CONSTRAINT fk_menu_tag_item_id FOREIGN KEY (item_id) REFERENCES menu_item(item_id),
CONSTRAINT fk_menu_tag_tag_id FOREIGN KEY (tag_id) REFERENCES tag(tag_id)
);

-- Insert menu_tag relationships
INSERT INTO menu_tag (item_id, tag_id) VALUES
(1, 1), (1, 2),
(2, 1), (2, 2),
(3, 3), (3, 4), (3, 5),
(4, 6), (4, 7), (4, 5),
(5, 8), (5, 5), (5, 9),
(6, 10), (6, 8), (6, 9),
(7, 11), (7, 12), (7, 5),
(8, 12), (8, 13), (8, 14), (8, 15),
(9, 8), (9, 5), (9, 9),
(10, 1), (10, 16),
(11, 16), (11, 17);

-- 咸檸7 (Salty Lemon 7-Up) - fizzy, lemony, refreshing
INSERT INTO menu_tag (item_id, tag_id) VALUES
(12, (SELECT tag_id FROM tag WHERE tag_name='soda')),
(12, (SELECT tag_id FROM tag WHERE tag_name='lemon')),
(12, (SELECT tag_id FROM tag WHERE tag_name='refreshing')),
(12, (SELECT tag_id FROM tag WHERE tag_name='cold'));

-- 紅豆冰 (Red Bean Ice) - sweet, cold, traditional
INSERT INTO menu_tag (item_id, tag_id) VALUES
(13, (SELECT tag_id FROM tag WHERE tag_name='sweet')),
(13, (SELECT tag_id FROM tag WHERE tag_name='cold')),
(13, (SELECT tag_id FROM tag WHERE tag_name='traditional'));

-- 熱奶茶 (Hot Milk Tea) - milk, classic, hot
INSERT INTO menu_tag (item_id, tag_id) VALUES
(14, (SELECT tag_id FROM tag WHERE tag_name='milk')),
(14, (SELECT tag_id FROM tag WHERE tag_name='classic'));

-- 葡萄烏龍茶 (Grape Oolong Tea) - grape, refreshing, cold
INSERT INTO menu_tag (item_id, tag_id) VALUES
(15, (SELECT tag_id FROM tag WHERE tag_name='grape')),
(15, (SELECT tag_id FROM tag WHERE tag_name='refreshing')),
(15, (SELECT tag_id FROM tag WHERE tag_name='cold'));

-- 熱檸茶 (Hot Lemon Tea) - lemon, sour, classic
INSERT INTO menu_tag (item_id, tag_id) VALUES
(16, (SELECT tag_id FROM tag WHERE tag_name='lemon')),
(16, (SELECT tag_id FROM tag WHERE tag_name='sour')),
(16, (SELECT tag_id FROM tag WHERE tag_name='classic'));

-- 凍奶茶 (Iced Milk Tea) - milk, cold, classic
INSERT INTO menu_tag (item_id, tag_id) VALUES
(17, (SELECT tag_id FROM tag WHERE tag_name='milk')),
(17, (SELECT tag_id FROM tag WHERE tag_name='cold')),
(17, (SELECT tag_id FROM tag WHERE tag_name='classic'));

-- 凍檸茶 (Iced Lemon Tea) - lemon, refreshing, cold
INSERT INTO menu_tag (item_id, tag_id) VALUES
(18, (SELECT tag_id FROM tag WHERE tag_name='lemon')),
(18, (SELECT tag_id FROM tag WHERE tag_name='refreshing')),
(18, (SELECT tag_id FROM tag WHERE tag_name='cold'));


-- Drop old table if needed
DROP TABLE IF EXISTS order_items;
-- Create order_items table (order details)
CREATE TABLE order_items (
    oid INT NOT NULL,
    item_id INT NOT NULL,
    qty INT NOT NULL DEFAULT 1,
    note TEXT DEFAULT NULL,
    PRIMARY KEY (oid, item_id),
    FOREIGN KEY (oid) REFERENCES orders(oid),
    FOREIGN KEY (item_id) REFERENCES menu_item(item_id)
);

-- Dumping data for table order_items
INSERT INTO order_items (oid, item_id, qty) VALUES
(1, 1, 2),   -- Order 1 includes 2x Pickled Cucumber Flowers
(1, 3, 1),   -- Order 1 also includes 1x Mouthwatering Chicken
(2, 4, 1),   -- Order 2 includes 1x Suan Cai Fish Soup
(2, 6, 3);   -- Order 2 includes 3x Mapo Tofu


CREATE TABLE table_orders (
toid INT NOT NULL AUTO_INCREMENT, -- Unique ID for table order
table_number INT NOT NULL, -- Physical table number
oid INT DEFAULT NULL, -- Linked order ID (nullable until ordering starts)
staff_id INT DEFAULT NULL, -- Staff member (nullable until assigned)
status ENUM('available', 'reserved', 'seated', 'ordering', 'ready_to_pay', 'paid') NOT NULL DEFAULT 'available',
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (toid),
CONSTRAINT fk_table_orders_oid FOREIGN KEY (oid) REFERENCES orders(oid),
CONSTRAINT fk_table_orders_staff FOREIGN KEY (staff_id) REFERENCES staff(sid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 1: Available, no staff or order assigned
INSERT INTO table_orders (table_number, status)
VALUES (1, 'available');

-- Table 2: Reserved, staff assigned (sid = 1), no order yet
INSERT INTO table_orders (table_number, staff_id, status)
VALUES (2, 1, 'reserved');

-- Table 3: Seated, staff assigned (sid = 2), no order yet
INSERT INTO table_orders (table_number, staff_id, status)
VALUES (3, 2, 'seated');

-- Table 4: Ordering, staff assigned (sid = 3), order linked (oid = 1)
INSERT INTO table_orders (table_number, oid, staff_id, status)
VALUES (4, 1, 3, 'ordering');

-- Table 5: Ready to pay, staff assigned (sid = 4), order linked (oid = 2)
INSERT INTO table_orders (table_number, oid, staff_id, status)
VALUES (5, 2, 4, 'ready_to_pay');

-- Table 6: Paid, staff assigned (sid = 5), order linked (oid = 2)
INSERT INTO table_orders (table_number, oid, staff_id, status)
VALUES (6, 2, 5, 'paid');


CREATE TABLE seatingChart (
tid int(11) NOT NULL AUTO_INCREMENT,
capacity int(11) NOT NULL COMMENT 'Table capacity',
status tinyint(1) NOT NULL DEFAULT 0 COMMENT 'state',
PRIMARY KEY (tid)
);

INSERT INTO seatingChart (capacity, status) VALUES
(2, 0),(2, 0),(2, 0),(2, 0),(2, 0),
(2, 0),(2, 0),(2, 0),(2, 0),(2, 0),
(2, 0),(2, 0),(2, 0),(2, 0),(2, 0),
(2, 0),(2, 0),(2, 0),(2, 0),(2, 0),

(4, 0),(4, 0),(4, 0),(4, 0),(4, 0),
(4, 0),(4, 0),(4, 0),(4, 0),(4, 0),
(4, 0),(4, 0),(4, 0),(4, 0),(4, 0),
(4, 0),(4, 0),(4, 0),(4, 0),(4, 0),
(4, 0),(4, 0),(4, 0),(4, 0),(4, 0),

(8, 0),(8, 0),(8, 0),(8, 0),(8, 0);

CREATE TABLE booking (
bid int(11) NOT NULL AUTO_INCREMENT,
cid int(11) DEFAULT NULL COMMENT 'Customer ID',
bkcname varchar(255) NOT NULL COMMENT 'Customer Name',
bktel int(11) NOT NULL COMMENT 'telephone number',
tid int(11) NOT NULL COMMENT 'Table ID',
bdate date NOT NULL COMMENT 'Booking date',
btime time NOT NULL COMMENT 'Booking time',
pnum int(11) NOT NULL COMMENT 'Number of guests',
purpose varchar(255) DEFAULT NULL COMMENT 'Purpose of booking',
remark varchar(255) DEFAULT NULL COMMENT 'Remark of booking',
status tinyint(1) NOT NULL DEFAULT 1 COMMENT 'state',
PRIMARY KEY (bid),
KEY bkcname (bkcname),
KEY tid (tid),
CONSTRAINT booking_ibfk_1 FOREIGN KEY (cid) REFERENCES customer (cid),
CONSTRAINT booking_ibfk_2 FOREIGN KEY (tid) REFERENCES seatingChart (tid)
);


INSERT INTO booking (cid, bkcname, bktel, tid, bdate, btime, pnum, purpose, remark, status) VALUES
(1, 'Alex Wong', 21232123, 5, '2024-01-15', '18:30:00', 4, 'Family Dinner', 'We have a baby with us, need a high chair', 2),
(2, 'Tina Chan', 31233123, 12, '2024-01-16', '19:00:00', 2, 'Date Night', NULL, 3),
(3, 'Bowie', 61236123, 8, '2024-01-17', '20:00:00', 6, 'Business Meeting', 'Need a quiet area for discussion', 1),
(4, 'Samuel Lee', 61231212, 25, '2024-01-18', '12:30:00', 3, 'Lunch Meeting', NULL, 2),
(5, 'Emily Tsang', 61231555, 30, '2024-01-19', '13:00:00', 4, 'Birthday Celebration', 'Will bring a cake', 3);

INSERT INTO booking (cid, bkcname, bktel, tid, bdate, btime, pnum, purpose, remark, status) VALUES
(NULL, 'Michael Johnson', 5551234, 3, '2024-01-15', '19:30:00', 2, 'Casual Dinner', NULL, 0),
(NULL, 'Sarah Williams', 5555678, 15, '2024-01-16', '20:30:00', 4, 'Family Gathering', NULL, 1),
(NULL, 'David Brown', 5559012, 40, '2024-01-17', '18:00:00', 8, 'Company Party', NULL, 2),
(NULL, 'Jennifer Davis', 5553456, 10, '2024-01-18', '19:00:00', 2, 'Anniversary', NULL, 3),
(NULL, 'Robert Miller', 5557890, 20, '2024-01-19', '12:00:00', 4, 'Business Lunch', 'Need power outlet for laptop', 1);



-- Create menu_package table
CREATE TABLE menu_package (
    package_id INT NOT NULL AUTO_INCREMENT,
    package_name VARCHAR(255) NOT NULL,
    num_of_type INT NOT NULL,
	package_image_url VARCHAR(255),
    amounts DECIMAL(10,2) NOT NULL, -- Changed from discount to fixed price amounts
    PRIMARY KEY (package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create package_type table (without type_name)
CREATE TABLE package_type (
    type_id INT NOT NULL AUTO_INCREMENT,
    package_id INT NOT NULL,
    optional_quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (type_id),
    CONSTRAINT fk_package_type_package_id FOREIGN KEY (package_id) REFERENCES menu_package(package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create package_type_translation table
CREATE TABLE package_type_translation (
    type_translation_id INT NOT NULL AUTO_INCREMENT,
    type_id INT NOT NULL,
    type_language_code VARCHAR(10) NOT NULL,
    type_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (type_translation_id),
    CONSTRAINT fk_package_type_translation_type_id FOREIGN KEY (type_id) REFERENCES package_type(type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create package_dish table with price_modifier column
CREATE TABLE package_dish (
    package_id INT NOT NULL,
    type_id INT NOT NULL,
    item_id INT NOT NULL,
    price_modifier DECIMAL(10,2) NOT NULL DEFAULT 0.00, -- Added price_modifier column
    PRIMARY KEY (package_id, type_id, item_id),
    CONSTRAINT fk_package_dish_package_id FOREIGN KEY (package_id) REFERENCES menu_package(package_id),
    CONSTRAINT fk_package_dish_type_id FOREIGN KEY (type_id) REFERENCES package_type(type_id),
    CONSTRAINT fk_package_dish_item_id FOREIGN KEY (item_id) REFERENCES menu_item(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert package data with fixed prices instead of discounts
INSERT INTO menu_package (package_name, num_of_type, package_image_url, amounts) VALUES
('Double Set', 3, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/package/1.jpg', 180.00),      -- Fixed price: HK$180
('Four Person Set', 4, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/package/2.jpg', 380.00), -- Fixed price: HK$380
('Business Set', 2, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/package/3.jpg', 120.00);    -- Fixed price: HK$120


-- Insert package type data (without type_name)
INSERT INTO package_type (package_id, optional_quantity) VALUES
-- Double Set types
(1, 1), (1, 2), (1, 2),

-- Four Person Set types
(2, 2), (2, 1), (2, 3), (2, 4),

-- Business Set types
(3, 1), (3, 1);

-- Insert package type translations
INSERT INTO package_type_translation (type_id, type_language_code, type_name) VALUES
-- Double Set types - English
(1, 'en', 'Appetizer'),
(2, 'en', 'Main Course'),
(3, 'en', 'Drink'),

-- Four Person Set types - English
(4, 'en', 'Appetizer'),
(5, 'en', 'Soup'),
(6, 'en', 'Main Course'),
(7, 'en', 'Drink'),

-- Business Set types - English
(8, 'en', 'Main Course'),
(9, 'en', 'Drink'),

-- Double Set types - Chinese (Simplified)
(1, 'zh-CN', '前菜'),
(2, 'zh-CN', '主菜'),
(3, 'zh-CN', '饮料'),

-- Four Person Set types - Chinese (Simplified)
(4, 'zh-CN', '前菜'),
(5, 'zh-CN', '汤品'),
(6, 'zh-CN', '主菜'),
(7, 'zh-CN', '饮料'),

-- Business Set types - Chinese (Simplified)
(8, 'zh-CN', '主菜'),
(9, 'zh-CN', '饮料'),

-- Double Set types - Chinese (Traditional)
(1, 'zh-TW', '前菜'),
(2, 'zh-TW', '主菜'),
(3, 'zh-TW', '飲料'),

-- Four Person Set types - Chinese (Traditional)
(4, 'zh-TW', '前菜'),
(5, 'zh-TW', '湯品'),
(6, 'zh-TW', '主菜'),
(7, 'zh-TW', '飲料'),

-- Business Set types - Chinese (Traditional)
(8, 'zh-TW', '主菜'),
(9, 'zh-TW', '飲料');

-- Insert package dish relationships with price_modifier (positive values only)
INSERT INTO package_dish (package_id, type_id, item_id, price_modifier) VALUES
-- Double Set appetizer options
(1, 1, 1, 0.00), (1, 1, 2, 3.00), (1, 1, 3, 8.00),

-- Double Set main course options
(1, 2, 5, 0.00), (1, 2, 6, 8.00), (1, 2, 7, 6.00), (1, 2, 8, 12.00), (1, 2, 9, 10.00), (1, 2, 10, 7.00),

-- Double Set drink options
(1, 3, 12,0.00), (1, 3, 13, 2.00), (1, 3, 14, 3.00), (1, 3, 15, 4.00), (1, 3, 16, 2.00), (1, 3, 17, 3.00), (1, 3, 18, 2.00),

-- Four Person Set appetizer options
(2, 4, 1,0.00), (2, 4, 2, 5.00), (2, 4, 3, 12.00),

-- Four Person Set soup options
(2, 5, 4,0.00),

-- Four Person Set main course options
(2, 6, 5,0.00), (2, 6, 6, 15.00), (2, 6, 7, 12.00), (2, 6, 8, 20.00), (2, 6, 9, 18.00), (2, 6, 10, 14.00),

-- Four Person Set drink options
(2, 7, 12,0.00), (2, 7, 13, 3.00), (2, 7, 14, 4.00), (2, 7, 15, 5.00), (2, 7, 16, 3.00), (2, 7, 17, 4.00), (2, 7, 18, 3.00),

-- Business Set main course options
(3, 8, 5,0.00), (3, 8, 6, 12.00), (3, 8, 7, 10.00), (3, 8, 8, 18.00), (3, 8, 9, 15.00), (3, 8, 10, 12.00),

-- Business Set drink options
(3, 9, 14,0.00), (3, 9, 16, 3.00), (3, 9, 17, 4.00), (3, 9, 18, 3.00);

-- Coupon applies to specific packages
CREATE TABLE coupon_applicable_package (
    id INT AUTO_INCREMENT PRIMARY KEY,
    coupon_id INT NOT NULL,
    package_id INT NOT NULL,
    FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE,
    FOREIGN KEY (package_id) REFERENCES menu_package(package_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create order_packages table (to list packages of an order)
DROP TABLE IF EXISTS order_packages;
CREATE TABLE order_packages (
    op_id INT NOT NULL AUTO_INCREMENT,
    oid INT NOT NULL,
    package_id INT NOT NULL,
    qty INT NOT NULL DEFAULT 1,
    note TEXT DEFAULT NULL,
    PRIMARY KEY (op_id),
    FOREIGN KEY (oid) REFERENCES orders(oid) ON DELETE CASCADE,
    FOREIGN KEY (package_id) REFERENCES menu_package(package_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

COMMIT;

-- Table structure for table material
DROP TABLE IF EXISTS material;
-- Raw materials / ingredients
CREATE TABLE materials (
  mid INT NOT NULL AUTO_INCREMENT,
  mname VARCHAR(255) NOT NULL,       -- e.g. "Chicken", "Soy Sauce"
  mcategory VARCHAR(100) DEFAULT NULL, -- e.g. "Meat", "Vegetable", "Condiment"
  unit VARCHAR(50) DEFAULT NULL,     -- e.g. "grams", "ml", "pieces"
  mqty DECIMAL(10,2) DEFAULT NULL,   -- quantity available
  PRIMARY KEY (mid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Materials sample data with quantities
INSERT INTO materials (mname, mcategory, unit, mqty) VALUES
('Cucumber', 'Vegetable', 'grams', 500.00),
('Chicken', 'Meat', 'grams', 2000.00),
('Soy Sauce', 'Condiment', 'ml', 1000.00),
('Chili Oil', 'Condiment', 'ml', 500.00),
('Rice', 'Grain', 'grams', 10000.00),
('Beef', 'Meat', 'grams', 1500.00),
('Tofu', 'Protein', 'grams', 800.00);


CREATE TABLE `consumption_history` (
    `log_id` INT AUTO_INCREMENT PRIMARY KEY,
    `mid` INT NOT NULL,
    `log_date` DATE NOT NULL,
    `log_type` ENUM('Deduction', 'Forecast', 'Reorder') NOT NULL COMMENT '操作类型: 扣减, 预测, 补货',
    `details` TEXT NOT NULL COMMENT '详细说明或备注',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_consumption_material`
        FOREIGN KEY (`mid`) REFERENCES `materials`(`mid`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Junction table linking dishes to ingredients
DROP TABLE IF EXISTS recipe_materials;

CREATE TABLE recipe_materials (
  id INT NOT NULL AUTO_INCREMENT,
  item_id INT NOT NULL,   -- menu_item
  mid INT NOT NULL,       -- materials
  quantity DECIMAL(10,2) DEFAULT NULL, -- required amount per dish
  PRIMARY KEY (id),
  FOREIGN KEY (item_id) REFERENCES menu_item(item_id) ON DELETE CASCADE,
  FOREIGN KEY (mid) REFERENCES materials(mid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Example mappings
-- Mouthwatering Chicken (item_id=3)
INSERT INTO recipe_materials (item_id, mid, quantity) VALUES
(3, 2, 200),  -- Chicken
(3, 3, 20),   -- Soy Sauce
(3, 4, 10);   -- Chili Oil

-- Mapo Tofu (item_id=6)
INSERT INTO recipe_materials (item_id, mid, quantity) VALUES
(6, 7, 150),  -- Tofu
(6, 6, 50),   -- Beef
(6, 3, 15);   -- Soy Sauce


-- ================================================================
-- 菜品自訂系統表結構 (YummyRestaurant 4.3+)
-- ================================================================

-- 自訂選項定義表 (每個菜品可以有多個自訂選項)
CREATE TABLE IF NOT EXISTS item_customization_options (
    option_id INT NOT NULL AUTO_INCREMENT,
    item_id INT NOT NULL,
    option_name VARCHAR(255) NOT NULL,      -- 例如: "Spice Level", "Temperature"
    max_selections INT NOT NULL,         -- 多選時的最大選擇數
    is_required TINYINT(1) DEFAULT 0,    -- 是否為必填項 (1=required, 0=optional)
    PRIMARY KEY (option_id),
    FOREIGN KEY (item_id) REFERENCES menu_item(item_id) ON DELETE CASCADE,
    KEY idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 自訂選項的具體選擇項 (例如：辛辣度選項中的 "Mild", "Medium", "Hot")
CREATE TABLE IF NOT EXISTS customization_option_choices (
    choice_id INT NOT NULL AUTO_INCREMENT,
    option_id INT NOT NULL,
    choice_name VARCHAR(255) NOT NULL,
    additional_cost DECIMAL(10,2) DEFAULT 0,  -- 例如：加冰需要額外費用
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
    item_id INT NOT NULL,                    -- 菜品 ID (關聯 order_items)
    option_id INT NOT NULL,
    option_name VARCHAR(255) NOT NULL,
    choice_ids JSON DEFAULT NULL,             -- 多個選擇的 IDs 陣列
    choice_names JSON DEFAULT NULL,           -- 多個選擇的名稱陣列
    text_value VARCHAR(500) DEFAULT NULL,    -- 文字備註
    additional_cost DECIMAL(10,2) DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customization_id),
    FOREIGN KEY (oid) REFERENCES orders(oid) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES menu_item(item_id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES item_customization_options(option_id) ON DELETE CASCADE,
    KEY idx_order_id (oid),
    KEY idx_item_id (item_id),
    KEY idx_option_id (option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- 示例數據：為不同菜品類別添加自訂選項
-- ================================================================

-- 開胃菜自訂選項：辛辣度 (Spice Level) - 為口水雞 (item_id=3)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (3, 'Spice Level', 1);

SET @spice_option_id_3 = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@spice_option_id_3, 'Mild', 0, 1),
(@spice_option_id_3, 'Medium', 0, 2),
(@spice_option_id_3, 'Hot', 0, 3),
(@spice_option_id_3, 'Numbing', 0, 4);

-- 主菜自訂選項：辛辣度 (Spice Level) - 為麻婆豆腐 (item_id=6)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (6, 'Spice Level', 1);

SET @spice_option_id_6 = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@spice_option_id_6, 'Mild', 0, 1),
(@spice_option_id_6, 'Medium', 0, 2),
(@spice_option_id_6, 'Hot', 0, 3),
(@spice_option_id_6, 'Numbing', 0, 4);

-- 主菜自訂選項：辛辣度 (Spice Level) - 為擔擔麵 (item_id=7)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (7, 'Spice Level', 1);

SET @spice_option_id_7 = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@spice_option_id_7, 'Mild', 0, 1),
(@spice_option_id_7, 'Medium', 0, 2),
(@spice_option_id_7, 'Hot', 0, 3),
(@spice_option_id_7, 'Numbing', 0, 4);

-- 主菜自訂選項：辛辣度 (Spice Level) - 為水煮牛肉 (item_id=9)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (9, 'Spice Level', 1);

SET @spice_option_id_9 = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@spice_option_id_9, 'Mild', 0, 1),
(@spice_option_id_9, 'Medium', 0, 2),
(@spice_option_id_9, 'Hot', 0, 3),
(@spice_option_id_9, 'Numbing', 0, 4);

-- 主菜自訂選項：特殊要求 (Special Requests) - 為所有辣菜
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (3, 'Special Requests', 1);

INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (6, 'Special Requests', 1);

INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (7, 'Special Requests', 1);

INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (9, 'Special Requests', 1);

-- 飲料自訂選項：溫度 (Temperature) - 為熱奶茶 (item_id=14)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (14, 'Temperature', 1);

SET @temp_option_id_14 = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@temp_option_id_14, 'Very Hot', 0, 1),
(@temp_option_id_14, 'Hot', 0, 2),
(@temp_option_id_14, 'Warm', 0, 3);

-- 飲料自訂選項：溫度 (Temperature) - 為熱檸檬茶 (item_id=16)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (16, 'Temperature', 1);

SET @temp_option_id_16 = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@temp_option_id_16, 'Very Hot', 0, 1),
(@temp_option_id_16, 'Hot', 0, 2),
(@temp_option_id_16, 'Warm', 0, 3);

-- 飲料自訂選項：糖度 (Sugar Level) - 為冷奶茶 (item_id=17)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (17, 'Sugar Level', 1);

SET @sugar_option_id_17 = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@sugar_option_id_17, 'Full Sugar', 0, 1),
(@sugar_option_id_17, 'Less Sugar', 0, 2),
(@sugar_option_id_17, 'Light Sugar', 0, 3),
(@sugar_option_id_17, 'No Sugar', 0, 4);

-- 飲料自訂選項：加冰 (Ice Level) - 為冷奶茶 (item_id=17)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (17, 'Ice Level', 1);

SET @ice_option_id_17 = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@ice_option_id_17, 'No Ice', 0, 1),
(@ice_option_id_17, 'Light Ice', 0, 2),
(@ice_option_id_17, 'Normal Ice', 0, 3),
(@ice_option_id_17, 'Extra Ice', 0, 4);

-- 飲料自訂選項：糖度 (Sugar Level) - 為凍檸茶 (item_id=18)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (18, 'Sugar Level', 1);

SET @sugar_option_id_18 = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@sugar_option_id_18, 'Full Sugar', 0, 1),
(@sugar_option_id_18, 'Less Sugar', 0, 2),
(@sugar_option_id_18, 'Light Sugar', 0, 3),
(@sugar_option_id_18, 'No Sugar', 0, 4);

-- 飲料自訂選項：加冰 (Ice Level) - 為凍檸茶 (item_id=18)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (18, 'Ice Level', 1);

SET @ice_option_id_18 = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@ice_option_id_18, 'No Ice', 0, 1),
(@ice_option_id_18, 'Light Ice', 0, 2),
(@ice_option_id_18, 'Normal Ice', 0, 3),
(@ice_option_id_18, 'Extra Ice', 0, 4);

-- 甜品自訂選項：配菜 (Toppings) - 為四川糯米糕 (item_id=11)
INSERT INTO item_customization_options (item_id, option_name, max_selections)
VALUES (11, 'Toppings', 3);

SET @topping_option_id = LAST_INSERT_ID();

INSERT INTO customization_option_choices (option_id, choice_name, additional_cost, display_order)
VALUES 
(@topping_option_id, 'Extra Sesame', 0.50, 1),
(@topping_option_id, 'Peanuts', 0.50, 2),
(@topping_option_id, 'Honey Drizzle', 1.00, 3),
(@topping_option_id, 'Chocolate Chips', 1.00, 4);