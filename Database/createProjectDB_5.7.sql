


-- MySQL dump 10.13 Distrib 8.0.40, for Win64 (x86_64)
-- Host: localhost Database: projectdb
-- Server version 8.4.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+08:00";

DROP DATABASE IF EXISTS ProjectDB;
CREATE DATABASE IF NOT EXISTS ProjectDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ProjectDB;

-- =================================================================
-- STAFF AND CUSTOMER MANAGEMENT
-- =================================================================

DROP TABLE IF EXISTS staff;
CREATE TABLE staff (
  sid INT NOT NULL AUTO_INCREMENT,
  semail VARCHAR(191) NOT NULL,
  spassword VARCHAR(255) NOT NULL,
  sname VARCHAR(255) NOT NULL,
  srole VARCHAR(45) DEFAULT NULL,
  stel INT DEFAULT NULL,
  simageurl VARCHAR(255) NULL,
  PRIMARY KEY (sid),
  UNIQUE KEY semail_UNIQUE (semail)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO staff (semail, spassword, sname, srole, stel, simageurl) VALUES
('peter.wong@example.com', 'password123', 'Peter Wong', 'staff', 25669197, NULL),
('tina.chan@example.com', 'letmein456', 'Tina Chan', 'Production Supervisor', 31233123, NULL),
('alex.lam@example.com', 'qwerty789', 'Alex Lam', 'Warehouse Clerk', 29881234, NULL),
('susan.leung@example.com', 'helloWorld1', 'Susan Leung', 'HR Officer', 28889999, NULL),
('john.ho@example.com', 'changeme2023', 'John Ho', 'Engineer', 29998888, NULL),
('maggie.tse@example.com', 'maggiePass!', 'Maggie Tse', 'Accountant', 23881211, NULL),
('kevin.ng@example.com', 'ngfamily', 'Kevin Ng', 'IT Support', 27889977, NULL),
('emily.tsui@example.com', 'emily2024', 'Emily Tsui', 'Marketing Lead', 26543210, NULL);

DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
  cid INT NOT NULL AUTO_INCREMENT,
  cname VARCHAR(255) NOT NULL,
  cpassword VARCHAR(255) NOT NULL,
  ctel INT DEFAULT NULL,
  caddr TEXT,
  company VARCHAR(255) DEFAULT NULL,
  cemail VARCHAR(191) NOT NULL UNIQUE,
  cbirthday CHAR(5) DEFAULT NULL,
  crole VARCHAR(45) NOT NULL DEFAULT 'customer',
  cimageurl VARCHAR(255) NULL,
  coupon_point INT NOT NULL DEFAULT 0,
  PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO customer (cname, cpassword, ctel, caddr, company, cemail, cbirthday, crole, cimageurl) VALUES
('Alex Wong', 'password', 21232123, 'G/F, ABC Building, King Yip Street, KwunTong, Kowloon, Hong Kong', 'Fat Cat Company Limited', 'alex.wong@example.com', NULL, 'customer', NULL),
('Tina Chan', 'password', 31233123, '303, Mei Hing Center, Yuen Long, NT, Hong Kong', 'XDD LOL Company', 'tina.chan@example.com', '07-20', 'customer', NULL),
('Bowie', 'password', 61236123, '401, Sing Kei Building, Kowloon, Hong Kong', 'GPA4 Company', 'bowie@example.com', '03-15', 'customer', NULL),
('Samuel Lee', 'samuelpass', 61231212, '111, Example Road, Central, Hong Kong', 'Lee Family Co', 'samuel.lee@example.com', '11-02', 'customer', NULL),
('Emily Tsang', 'emilypass', 61231555, '88, Happy Valley Road, Hong Kong', 'Happy Valley Enterprises', 'emily.tsang@example.com', '01-30', 'customer', NULL);

INSERT INTO customer (cid, cname, cpassword, ctel, caddr, company, cemail, crole, cimageurl)
VALUES (0, 'Walk-in Customer', 'walkin', NULL, NULL, NULL, 'walkin@system.local', 'customer', NULL);

-- =================================================================
-- COUPON SYSTEM
-- =================================================================

DROP TABLE IF EXISTS coupons;
CREATE TABLE coupons (
  coupon_id INT NOT NULL AUTO_INCREMENT,
  points_required INT NOT NULL DEFAULT 0,
  type ENUM('cash','percent','free_item') NOT NULL DEFAULT 'cash',
  discount_amount INT DEFAULT 0,
  item_category VARCHAR(50) DEFAULT NULL,
  expiry_date DATE DEFAULT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO coupons (points_required, type, discount_amount, item_category, expiry_date, is_active) VALUES
(100, 'percent', 10, NULL, '2026-12-31', 1),
(50, 'free_item', 0, 'drink', '2026-12-30', 1),
(200, 'cash', 5000, NULL, '2026-12-31', 1),
(0, 'free_item', 0, NULL, NULL, 1);

CREATE TABLE coupon_translation (
  translation_id INT AUTO_INCREMENT PRIMARY KEY,
  coupon_id INT NOT NULL,
  language_code VARCHAR(10) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO coupon_translation (coupon_id, language_code, title, description) VALUES
(1, 'en', '10% OFF Any Order', 'Get 10% discount on your next order.'),
(1, 'zh-CN', '全单九折', '下次消费可享受九折优惠。'),
(1, 'zh-TW', '全單九折', '下次消費可享受九折優惠。'),
(2, 'en', 'Free Drink', 'Redeem one free drink of your choice.'),
(2, 'zh-CN', '免费饮品', '兑换一杯您选择的免费饮品。'),
(2, 'zh-TW', '免費飲品', '兌換一杯您選擇的免費飲品。'),
(3, 'en', 'HK$50 OFF', 'Enjoy HK$50 off when you spend HK$300 or more.'),
(3, 'zh-CN', '立减50港元', '消费满300港元即可减50港元。'),
(3, 'zh-TW', '立減50港元', '消費滿300港元即可減50港元。'),
(4, 'en', 'Birthday Special', 'Exclusive coupon for your birthday month.'),
(4, 'zh-CN', '生日特惠', '生日月份专属优惠券。'),
(4, 'zh-TW', '生日特惠', '生日月份專屬優惠券。');

CREATE TABLE coupon_terms (
  term_id INT AUTO_INCREMENT PRIMARY KEY,
  coupon_id INT NOT NULL,
  language_code VARCHAR(10) NOT NULL,
  term_text VARCHAR(500) NOT NULL,
  FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO coupon_terms (coupon_id, language_code, term_text) VALUES
(1, 'en', 'Valid for dine-in and takeaway orders'),
(1, 'en', 'Not applicable to delivery'),
(1, 'en', 'Cannot be combined with other discounts'),
(1, 'zh-CN', '适用于堂食和外卖'),
(1, 'zh-CN', '不适用于外送服务'),
(1, 'zh-CN', '不可与其他优惠同时使用'),
(1, 'zh-TW', '適用於堂食和外賣'),
(1, 'zh-TW', '不適用於外送服務'),
(1, 'zh-TW', '不可與其他優惠同時使用'),
(2, 'en', 'Choice of soft drink, coffee, or tea'),
(2, 'en', 'Limit one free drink per customer per day'),
(2, 'zh-CN', '可选择汽水、咖啡或茶'),
(2, 'zh-CN', '每位顾客每天限兑一杯'),
(2, 'zh-TW', '可選擇汽水、咖啡或茶'),
(2, 'zh-TW', '每位顧客每天限兌一杯'),
(3, 'en', 'Minimum spend of HK$300 required'),
(3, 'en', 'Discount applied before service charge'),
(3, 'zh-CN', '需满300港元方可使用'),
(3, 'zh-CN', '折扣在加收服务费前计算'),
(3, 'zh-TW', '需滿300港元方可使用'),
(3, 'zh-TW', '折扣於加收服務費前計算'),
(4, 'en', 'Valid only during your birthday month'),
(4, 'en', 'Must present valid ID for verification'),
(4, 'zh-CN', '仅限生日月份使用'),
(4, 'zh-CN', '需出示有效身份证明'),
(4, 'zh-TW', '僅限生日月份使用'),
(4, 'zh-TW', '需出示有效身份證明'),
(1, 'en', 'Photos are for reference only; actual products may vary'),
(2, 'en', 'Photos are for reference only; actual products may vary'),
(3, 'en', 'Photos are for reference only; actual products may vary'),
(4, 'en', 'Photos are for reference only; actual products may vary'),
(1, 'en', 'Coupons cannot be exchanged for cash, credit, or other products'),
(2, 'en', 'Coupons cannot be exchanged for cash, credit, or other products'),
(3, 'en', 'Coupons cannot be exchanged for cash, credit, or other products'),
(4, 'en', 'Coupons cannot be exchanged for cash, credit, or other products'),
(1, 'en', 'Yummy Restaurant reserves the right to cancel, amend, or change the terms and conditions without prior notice'),
(2, 'en', 'Yummy Restaurant reserves the right to cancel, amend, or change the terms and conditions without prior notice'),
(3, 'en', 'Yummy Restaurant reserves the right to cancel, amend, or change the terms and conditions without prior notice'),
(4, 'en', 'Yummy Restaurant reserves the right to cancel, amend, or change the terms and conditions without prior notice'),
(1, 'en', 'In case of product unavailability, the company may replace the coupon with an item of equal or greater value'),
(2, 'en', 'In case of product unavailability, the company may replace the coupon with an item of equal or greater value'),
(3, 'en', 'In case of product unavailability, the company may replace the coupon with an item of equal or greater value'),
(4, 'en', 'In case of product unavailability, the company may replace the coupon with an item of equal or greater value'),
(1, 'zh-TW', '圖片只供參考，實際供應可能有所不同'),
(2, 'zh-TW', '圖片只供參考，實際供應可能有所不同'),
(3, 'zh-TW', '圖片只供參考，實際供應可能有所不同'),
(4, 'zh-TW', '圖片只供參考，實際供應可能有所不同'),
(1, 'zh-TW', '優惠券不可兌換現金、信用額或其他產品'),
(2, 'zh-TW', '優惠券不可兌換現金、信用額或其他產品'),
(3, 'zh-TW', '優惠券不可兌換現金、信用額或其他產品'),
(4, 'zh-TW', '優惠券不可兌換現金、信用額或其他產品'),
(1, 'zh-TW', 'Yummy Restaurant 保留隨時取消、更改或修訂條款及細則之權利，恕不另行通知'),
(2, 'zh-TW', 'Yummy Restaurant 保留隨時取消、更改或修訂條款及細則之權利，恕不另行通知'),
(3, 'zh-TW', 'Yummy Restaurant 保留隨時取消、更改或修訂條款及細則之權利，恕不另行通知'),
(4, 'zh-TW', 'Yummy Restaurant 保留隨時取消、更改或修訂條款及細則之權利，恕不另行通知'),
(1, 'zh-TW', '如有產品缺貨，公司可更換為同等或更高價值之食品'),
(2, 'zh-TW', '如有產品缺貨，公司可更換為同等或更高價值之食品'),
(3, 'zh-TW', '如有產品缺貨，公司可更換為同等或更高價值之食品'),
(4, 'zh-TW', '如有產品缺貨，公司可更換為同等或更高價值之食品'),
(1, 'zh-CN', '图片仅供参考，实际供应可能有所不同'),
(2, 'zh-CN', '图片仅供参考，实际供应可能有所不同'),
(3, 'zh-CN', '图片仅供参考，实际供应可能有所不同'),
(4, 'zh-CN', '图片仅供参考，实际供应可能有所不同'),
(1, 'zh-CN', '优惠券不可兑换现金、信用额或其他产品'),
(2, 'zh-CN', '优惠券不可兑换现金、信用额或其他产品'),
(3, 'zh-CN', '优惠券不可兑换现金、信用额或其他产品'),
(4, 'zh-CN', '优惠券不可兑换现金、信用额或其他产品'),
(1, 'zh-CN', 'Yummy Restaurant 保留随时取消、更改或修订条款及细则的权利，恕不另行通知'),
(2, 'zh-CN', 'Yummy Restaurant 保留随时取消、更改或修订条款及细则的权利，恕不另行通知'),
(3, 'zh-CN', 'Yummy Restaurant 保留随时取消、更改或修订条款及细则的权利，恕不另行通知'),
(4, 'zh-CN', 'Yummy Restaurant 保留随时取消、更改或修订条款及细则的权利，恕不另行通知'),
(1, 'zh-CN', '如有产品缺货，公司可更换为同等或更高价值的食品'),
(2, 'zh-CN', '如有产品缺货，公司可更换为同等或更高价值的食品'),
(3, 'zh-CN', '如有产品缺货，公司可更换为同等或更高价值的食品'),
(4, 'zh-CN', '如有产品缺货，公司可更换为同等或更高价值的食品');

CREATE TABLE coupon_rules (
  rule_id INT AUTO_INCREMENT PRIMARY KEY,
  coupon_id INT NOT NULL,
  applies_to ENUM('whole_order','category','item','package') NOT NULL DEFAULT 'whole_order',
  discount_type ENUM('percent','cash','free_item') NOT NULL,
  discount_value DECIMAL(10,2) DEFAULT NULL,
  min_spend DECIMAL(10,2) DEFAULT NULL,
  max_discount DECIMAL(10,2) DEFAULT NULL,
  per_customer_per_day INT DEFAULT NULL,
  valid_dine_in TINYINT(1) NOT NULL DEFAULT 0,
  valid_takeaway TINYINT(1) NOT NULL DEFAULT 0,
  valid_delivery TINYINT(1) NOT NULL DEFAULT 0,
  combine_with_other_discounts TINYINT(1) NOT NULL DEFAULT 1,
  birthday_only TINYINT(1) NOT NULL DEFAULT 0,
  FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO coupon_rules (coupon_id, applies_to, discount_type, discount_value, min_spend, valid_dine_in, valid_takeaway, valid_delivery, combine_with_other_discounts) VALUES
(1, 'whole_order', 'percent', 10, NULL, 1, 1, 0, 0),
(2, 'item', 'free_item', 1, NULL, 1, 1, 1, 1),
(3, 'whole_order', 'cash', 50.00, 300.00, 1, 1, 1, 1),
(4, 'category', 'free_item', 1, NULL, 1, 1, 1, 1);

DROP TABLE IF EXISTS coupon_point_history;
CREATE TABLE coupon_point_history (
  cph_id INT NOT NULL AUTO_INCREMENT,
  cid INT NOT NULL,
  coupon_id INT NULL,
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

DROP TABLE IF EXISTS coupon_redemptions;
CREATE TABLE coupon_redemptions (
  redemption_id INT NOT NULL AUTO_INCREMENT,
  coupon_id INT NOT NULL,
  cid INT NOT NULL,
  redeemed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_used TINYINT(1) NOT NULL DEFAULT 0,
  used_at DATETIME NULL,
  PRIMARY KEY (redemption_id),
  CONSTRAINT fk_redemption_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id),
  CONSTRAINT fk_redemption_customer FOREIGN KEY (cid) REFERENCES customer(cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE coupon_applicable_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  coupon_id INT NOT NULL,
  item_id INT NOT NULL,
  FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO coupon_applicable_items (coupon_id, item_id) VALUES
(2, 12), (2, 13), (2, 14), (2, 15), (2, 16), (2, 17), (2, 18),
(4, 6);

CREATE TABLE coupon_applicable_categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  coupon_id INT NOT NULL,
  category_id INT NOT NULL,
  FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO coupon_applicable_categories (coupon_id, category_id) VALUES
(4, 3);

CREATE TABLE coupon_applicable_package (
  id INT AUTO_INCREMENT PRIMARY KEY,
  coupon_id INT NOT NULL,
  package_id INT NOT NULL,
  FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =================================================================
-- MENU AND CATEGORIES
-- =================================================================

CREATE TABLE menu_category (
  category_id INT PRIMARY KEY AUTO_INCREMENT,
  category_name VARCHAR(100) NOT NULL
);

INSERT INTO menu_category (category_name) VALUES
('Appetizers'),
('Soup'),
('Main Courses'),
('Dessert'),
('Drink'),
('Staple Foods');

INSERT INTO menu_category (category_name) VALUES ('Supplies');


CREATE TABLE menu_item (
  item_id INT PRIMARY KEY AUTO_INCREMENT,
  category_id INT NOT NULL,
  item_price DECIMAL(10,2) NOT NULL,
  image_url VARCHAR(255),
  spice_level INT NOT NULL CHECK (spice_level BETWEEN 0 AND 5),
  is_available BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (category_id) REFERENCES menu_category(category_id)
);

INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(1, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/1.jpg', 1, TRUE),
(1, 26.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/2.jpg', 1, TRUE),
(1, 32.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/3.jpg', 3, TRUE),
(2, 48.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/4.jpg', 2, TRUE),
(3, 95.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/5.jpg', 5, TRUE),
(3, 42.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/6.jpg', 3, TRUE),
(3, 38.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/7.jpg', 4, TRUE),
(3, 88.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/8.jpg', 2, TRUE),
(3, 58.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/9.jpg', 4, TRUE),
(3, 66.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/10.jpg', 2, TRUE),
(4, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/11.jpg', 0, TRUE),
(5, 26.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/12.jpg', 0, TRUE),
(5, 26.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/13.jpg', 0, TRUE),
(5, 20.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/14.jpg', 0, TRUE),
(5, 26.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/15.jpg', 0, TRUE),
(5, 20.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/16.jpg', 0, TRUE),
(5, 26.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/17.jpg', 0, TRUE),
(5, 26.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/18.jpg', 0, TRUE),
(6, 5.00,   'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/19.jpg',0,TRUE),
(6, 5.00,   'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/20.jpg',0,TRUE),
(6, 6.00,   'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/21.jpg',0,TRUE);

INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(7, 1.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/22.jpg', 0, TRUE);


CREATE TABLE menu_item_translation (
  translation_id INT PRIMARY KEY AUTO_INCREMENT,
  item_id INT NOT NULL,
  language_code VARCHAR(10) NOT NULL,
  item_name VARCHAR(255) NOT NULL,
  item_description TEXT,
  FOREIGN KEY (item_id) REFERENCES menu_item(item_id)
);

INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(1, 'en', 'Pickled Cucumber Flowers', 'Delicate cucumber blossoms pickled with aromatic spices.'),
(1, 'zh-CN', '腌制黄瓜花', '用香料腌制的黄瓜花，清爽可口。'),
(1, 'zh-TW', '醃製黃瓜花', '以香料醃製的黃瓜花，清新爽口。'),
(2, 'en', 'Spicy Wood Ear Mushrooms', 'Black fungus tossed in vinegar, garlic, and chili oil.'),
(2, 'zh-CN', '麻辣木耳', '黑木耳拌醋、蒜和辣油，爽口开胃。'),
(2, 'zh-TW', '麻辣木耳', '黑木耳拌醋、蒜與辣油，爽口開胃。'),
(3, 'en', 'Mouthwatering Chicken', 'Poached chicken drenched in spicy Sichuan chili sauce.'),
(3, 'zh-CN', '口水鸡', '嫩鸡浸泡在麻辣红油中，香辣诱人。'),
(3, 'zh-TW', '口水雞', '嫩雞浸泡在麻辣紅油中，香辣誘人。'),
(4, 'en', 'Suan Cai Fish Soup', 'Sliced fish simmered in pickled mustard greens and chili broth.'),
(4, 'zh-CN', '酸菜鱼汤', '鱼片炖酸菜和辣汤，酸辣开胃。'),
(4, 'zh-TW', '酸菜魚湯', '魚片燉酸菜與辣湯，酸辣開胃。'),
(5, 'en', 'Chongqing-style Angus Beef', 'Spicy Angus beef with bean paste and lemongrass.'),
(5, 'zh-CN', '重庆风味安格斯牛肉', '辣味安格斯牛肉配豆瓣酱和香茅，麻辣持久。'),
(5, 'zh-TW', '重慶風味安格斯牛肉', '辣味安格斯牛肉搭配豆瓣醬與香茅，麻辣持久。'),
(6, 'en', 'Mapo Tofu', 'Silken tofu in spicy bean paste sauce with minced beef and Sichuan peppercorns.'),
(6, 'zh-CN', '麻婆豆腐', '嫩豆腐配牛肉末和麻辣豆瓣酱，风味十足。'),
(6, 'zh-TW', '麻婆豆腐', '嫩豆腐搭配牛肉末與麻辣豆瓣醬，風味十足。'),
(7, 'en', 'Dan Dan Noodles', 'Spicy noodles topped with minced pork, preserved vegetables, and chili oil.'),
(7, 'zh-CN', '担担面', '辣味面条配猪肉末、芽菜和红油，香辣诱人。'),
(7, 'zh-TW', '擔擔麵', '辣味麵條搭配豬肉末、芽菜與紅油，香辣誘人。'),
(8, 'en', 'Twice-Cooked Pork', 'Pork belly simmered then stir-fried with leeks and chili bean paste.'),
(8, 'zh-CN', '回锅肉', '五花肉先煮后炒，搭配蒜苗和豆瓣酱，香浓可口。'),
(8, 'zh-TW', '回鍋肉', '五花肉先煮後炒，搭配蒜苗與豆瓣醬，香濃可口。'),
(9, 'en', 'Boiled Beef in Chili Broth', 'Tender beef slices in a fiery broth with Sichuan peppercorns.'),
(9, 'zh-CN', '水煮牛肉', '牛肉片浸泡在麻辣红汤中，香辣过瘾。'),
(9, 'zh-TW', '水煮牛肉', '牛肉片浸泡在麻辣紅湯中，香辣過癮。'),
(10, 'en', 'Fish-Fragrant Eggplant', 'Braised eggplant in garlic, ginger, and sweet chili sauce.'),
(10, 'zh-CN', '鱼香茄子', '茄子炖煮于蒜姜和甜辣酱中，香气扑鼻。'),
(10, 'zh-TW', '魚香茄子', '茄子燉煮於蒜薑與甜辣醬中，香氣撲鼻。'),
(11, 'en', 'Sichuan Glutinous Rice Cake', 'Sticky rice cake with brown sugar and sesame.'),
(11, 'zh-CN', '四川糯米糕', '糯米糕配红糖和芝麻，甜而不腻。'),
(11, 'zh-TW', '四川糯米糕', '糯米糕搭配紅糖與芝麻，甜而不膩。'),
(12, 'en', 'Salty Lemon 7-Up', 'Classic Hong Kong salty lemon soda with 7-Up.'),
(12, 'zh-CN', '咸柠7', '港式经典咸柠七喜，清爽解渴。'),
(12, 'zh-TW', '咸檸7', '港式經典鹹檸七喜，清爽解渴。'),
(13, 'en', 'Red Bean Ice', 'Sweet red beans served over crushed ice.'),
(13, 'zh-CN', '红豆冰', '香甜红豆配上碎冰，夏日必备。'),
(13, 'zh-TW', '紅豆冰', '香甜紅豆配上碎冰，夏日必備。'),
(14, 'en', 'Hot Milk Tea', 'Rich Hong Kong-style milk tea, best served hot.'),
(14, 'zh-CN', '热奶茶', '浓郁港式奶茶，热饮最佳。'),
(14, 'zh-TW', '熱奶茶', '濃郁港式奶茶，熱飲最佳。'),
(15, 'en', 'Grape Oolong Tea', 'Oolong tea infused with grape aroma, refreshing and unique.'),
(15, 'zh-CN', '葡萄乌龙茶', '乌龙茶融合葡萄香气，清新独特。'),
(15, 'zh-TW', '葡萄烏龍茶', '烏龍茶融合葡萄香氣，清新獨特。'),
(16, 'en', 'Hot Lemon Tea', 'Hot lemon tea, tangy and comforting.'),
(16, 'zh-CN', '热柠茶', '热柠檬茶，酸甜暖心。'),
(16, 'zh-TW', '熱檸茶', '熱檸檬茶，酸甜暖心。'),
(17, 'en', 'Iced Milk Tea', 'Classic Hong Kong-style milk tea, served chilled.'),
(17, 'zh-CN', '冻奶茶', '经典港式奶茶，冰凉爽口。'),
(17, 'zh-TW', '凍奶茶', '經典港式奶茶，冰涼爽口。'),
(18, 'en', 'Iced Lemon Tea', 'Crisp iced tea with fresh lemon slices.'),
(18, 'zh-CN', '冻柠茶', '冰镇柠檬茶，清爽解渴。'),
(18, 'zh-TW', '凍檸茶', '冰鎮檸檬茶，清爽解渴。'),
(19, 'en', 'Steamed Rice', 'Fluffy steamed rice, perfect as a staple side dish.'),
(19, 'zh-CN', '米饭', '蓬松的蒸米饭，完美的主食配菜。'),
(19, 'zh-TW', '米飯', '蓬鬆的蒸米飯，完美的主食配菜。'),
(20, 'en', 'Noodles', 'Soft and tender wheat noodles.'),
(20, 'zh-CN', '麵', '软而嫩的小麦面条。'),
(20, 'zh-TW', '麵', '軟而嫩的小麥麵條。'),
(21, 'en', 'Potato Starch', 'Smooth and creamy potato starch dish.'),
(21, 'zh-CN', '薯粉', '光滑细腻的薯粉食品。'),
(21, 'zh-TW', '薯粉', '光滑細膩的薯粉食品。');

INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(22, 'en', 'Wooden Chopsticks', 'Disposable wooden chopsticks for takeaway.'),
(22, 'zh-CN', '木筷', '一次性木筷，仅限外卖使用。'),
(22, 'zh-TW', '木筷', '一次性木筷，僅限外帶使用。');

-- =================================================================
-- TAGS
-- =================================================================

CREATE TABLE tag (
  tag_id INT NOT NULL AUTO_INCREMENT,
  tag_name VARCHAR(255) NOT NULL,
  tag_category VARCHAR(255) NOT NULL,
  tag_bg_color VARCHAR(7) DEFAULT NULL,
  PRIMARY KEY (tag_id),
  UNIQUE KEY (tag_name)
);

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
('glutinous', 'Type', '#607D8B'),
('lemon', 'Flavor', '#FFEB3B'),
('grape', 'Flavor', '#9C27B0'),
('milk', 'Ingredient', '#795548'),
('soda', 'Type', '#03A9F4'),
('traditional', 'Characteristic', '#607D8B');

CREATE TABLE menu_tag (
  item_id INT NOT NULL,
  tag_id INT NOT NULL,
  PRIMARY KEY (item_id, tag_id),
  CONSTRAINT fk_menu_tag_item_id FOREIGN KEY (item_id) REFERENCES menu_item(item_id),
  CONSTRAINT fk_menu_tag_tag_id FOREIGN KEY (tag_id) REFERENCES tag(tag_id)
);

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
(11, 16), (11, 17),
(12, 21), (12, 18), (12, 2), (12, 4),
(13, 16), (13, 4), (13, 22),
(14, 20), (14, 15),
(15, 19), (15, 2), (15, 4),
(16, 18), (16, 7), (16, 15),
(17, 20), (17, 4), (17, 15),
(18, 18), (18, 2), (18, 4),
(19, 22),
(20, 11), (20, 22),
(21, 22);

-- =================================================================
-- ORDERS
-- =================================================================

DROP TABLE IF EXISTS orders;

CREATE TABLE orders (
  oid INT NOT NULL AUTO_INCREMENT,
  odate DATETIME NOT NULL,
  cid INT NOT NULL,
  ostatus INT NOT NULL DEFAULT 1 COMMENT '0=Awaiting Cash Payment, 1=Pending, 2=Done, 3=Cancelled',
  note TEXT DEFAULT NULL,
  orderRef VARCHAR(100) NOT NULL UNIQUE,
  coupon_id INT NULL,
  order_type ENUM('dine_in', 'takeaway') NOT NULL DEFAULT 'dine_in',
  table_number INT NULL DEFAULT NULL,
  payment_method VARCHAR(50) DEFAULT 'card' COMMENT 'card, cash',
  payment_intent_id VARCHAR(255) DEFAULT NULL COMMENT 'Stripe payment intent ID or pseudo ID for cash',
  PRIMARY KEY (oid),
  CONSTRAINT fk_orders_cid FOREIGN KEY (cid) REFERENCES customer(cid),
  CONSTRAINT fk_orders_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert sample data
INSERT INTO orders (odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
('2024-01-15 18:30:00', 1, 2, 'order_20240115_001', NULL, 'dine_in', 5),
('2024-01-16 19:00:00', 2, 3, 'order_20240116_002', 1, 'dine_in', 12),
('2024-01-18 12:30:00', 4, 2, 'order_20240118_003', NULL, 'dine_in', 25),
('2025-04-12 17:50:00', 1, 1, 'order_20250412A', NULL, 'dine_in', 5),
('2025-04-13 12:01:00', 2, 3, 'order_20250413B', 1, 'takeaway', NULL);


DROP TABLE IF EXISTS order_items;
CREATE TABLE order_items (
  oid INT NOT NULL,
  item_id INT NOT NULL,
  qty INT NOT NULL DEFAULT 1,
  note TEXT DEFAULT NULL,
  PRIMARY KEY (oid, item_id),
  FOREIGN KEY (oid) REFERENCES orders(oid),
  FOREIGN KEY (item_id) REFERENCES menu_item(item_id)
);

INSERT INTO order_items (oid, item_id, qty) VALUES
(1, 1, 2),
(1, 3, 1),
(2, 4, 1),
(2, 6, 3),
-- Order 3: Alex Wong (bid1) - Family Dinner
(3, 2, 2),
(3, 5, 1),
(3, 12, 2),
-- Order 4: Tina Chan (bid2) - Date Night
(4, 3, 1),
(4, 14, 2),
-- Order 5: Samuel Lee (bid4) - Lunch Meeting
(5, 1, 1),
(5, 7, 1),
(5, 16, 2);

CREATE TABLE order_coupons (
  id INT AUTO_INCREMENT PRIMARY KEY,
  oid INT NOT NULL,
  coupon_id INT NOT NULL,
  redemption_id INT DEFAULT NULL,
  discount_amount DECIMAL(10,2) DEFAULT NULL,
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (oid) REFERENCES orders(oid) ON DELETE CASCADE,
  FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON DELETE CASCADE,
  FOREIGN KEY (redemption_id) REFERENCES coupon_redemptions(redemption_id) ON DELETE SET NULL
);

INSERT INTO order_coupons (oid, coupon_id, discount_amount) VALUES
(1, 1, 20.00),
(2, 2, 22.00);

-- =================================================================
-- CUSTOMIZATION SYSTEM
-- =================================================================

CREATE TABLE IF NOT EXISTS customization_option_group (
  group_id INT NOT NULL AUTO_INCREMENT,
  group_name VARCHAR(255) NOT NULL,
  group_type VARCHAR(50) NOT NULL,
  PRIMARY KEY (group_id),
  UNIQUE KEY (group_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customization_option_value (
  value_id INT NOT NULL AUTO_INCREMENT,
  group_id INT NOT NULL,
  value_name VARCHAR(255) NOT NULL,
  display_order INT DEFAULT 0,
  PRIMARY KEY (value_id),
  UNIQUE KEY (group_id, value_name),
  FOREIGN KEY (group_id) REFERENCES customization_option_group(group_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS item_customization_options (
  option_id INT NOT NULL AUTO_INCREMENT,
  item_id INT NOT NULL,
  group_id INT NOT NULL,
  max_selections INT NOT NULL DEFAULT 1,
  is_required TINYINT(1) DEFAULT 0,
  PRIMARY KEY (option_id),
  UNIQUE KEY (item_id, group_id),
  FOREIGN KEY (item_id) REFERENCES menu_item(item_id) ON DELETE CASCADE,
  FOREIGN KEY (group_id) REFERENCES customization_option_group(group_id) ON DELETE CASCADE,
  KEY idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item_customizations (
  customization_id INT NOT NULL AUTO_INCREMENT,
  oid INT NOT NULL,
  item_id INT NOT NULL,
  option_id INT NOT NULL,
  group_id INT NOT NULL,
  selected_value_ids JSON DEFAULT NULL,
  selected_values JSON DEFAULT NULL,
  text_value VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (customization_id),
  FOREIGN KEY (oid) REFERENCES orders(oid) ON DELETE CASCADE,
  FOREIGN KEY (item_id) REFERENCES menu_item(item_id) ON DELETE CASCADE,
  FOREIGN KEY (option_id) REFERENCES item_customization_options(option_id) ON DELETE CASCADE,
  FOREIGN KEY (group_id) REFERENCES customization_option_group(group_id) ON DELETE CASCADE,
  KEY idx_order_id (oid),
  KEY idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Insert option groups
INSERT INTO customization_option_group (group_name, group_type) VALUES
('Spice Level', 'spice'),
('Sugar Level', 'sugar'),
('Ice Level', 'ice'),
('Milk Level', 'milk'),
('Toppings', 'topping');

-- Insert option values for Spice Level (group_id=1)
INSERT INTO customization_option_value (group_id, value_name, display_order) VALUES
(1, 'Mild', 1),
(1, 'Medium', 2),
(1, 'Hot', 3),
(1, 'Numbing', 4);

-- Insert option values for Sugar Level (group_id=2)
INSERT INTO customization_option_value (group_id, value_name, display_order) VALUES
(2, 'More Sweet', 1),
(2, 'Less Sweet', 2),
(2, 'No Sweet', 3);

-- Insert option values for Ice Level (group_id=3)
INSERT INTO customization_option_value (group_id, value_name, display_order) VALUES
(3, 'More Ice', 1),
(3, 'Less Ice', 2),
(3, 'No Ice', 3);

-- Insert option values for Milk Level (group_id=4)
INSERT INTO customization_option_value (group_id, value_name, display_order) VALUES
(4, 'More Milk', 1),
(4, 'Less Milk', 2),
(4, 'No Milk', 3);

-- Insert option values for Toppings (group_id=5)
INSERT INTO customization_option_value (group_id, value_name, display_order) VALUES
(5, 'Extra Sesame', 1),
(5, 'Peanuts', 2),
(5, 'Honey Drizzle', 3),
(5, 'Chocolate Chips', 4);

-- Customization options for all 18 dishes
-- Items 1-10: Spice Level (group_id=1)
INSERT INTO item_customization_options (item_id, group_id, max_selections, is_required) VALUES
(1, 1, 1, 0),
(2, 1, 1, 0),
(3, 1, 1, 0),
(4, 1, 1, 0),
(5, 1, 1, 0),
(6, 1, 1, 0),
(7, 1, 1, 0),
(8, 1, 1, 0),
(9, 1, 1, 0),
(10, 1, 1, 0),
-- Item 11: Toppings (group_id=5)
(11, 5, 3, 0),
-- Item 12: Sugar Level (2) + Ice Level (3)
(12, 2, 1, 0), (12, 3, 1, 0),
-- Item 13: Sugar Level (2) + Ice Level (3)
(13, 2, 1, 0), (13, 3, 1, 0),
-- Item 14: Sugar Level (2) + Milk Level (4)
(14, 2, 1, 0), (14, 4, 1, 0),
-- Item 15: Sugar Level (2) + Ice Level (3)
(15, 2, 1, 0), (15, 3, 1, 0),
-- Item 16: Sugar Level (2)
(16, 2, 1, 0),
-- Item 17: Sugar Level (2) + Milk Level (4) + Ice Level (3)
(17, 2, 1, 0), (17, 4, 1, 0), (17, 3, 1, 0),
-- Item 18: Sugar Level (2) + Ice Level (3)
(18, 2, 1, 0), (18, 3, 1, 0);

-- =================================================================
-- TABLES AND BOOKINGS
-- =================================================================

CREATE TABLE seatingChart (
  tid INT NOT NULL AUTO_INCREMENT,
  capacity INT NOT NULL,
  status TINYINT(1) NOT NULL DEFAULT 0,
  x_position DECIMAL(5,2) DEFAULT NULL,
  y_position DECIMAL(5,2) DEFAULT NULL,
  PRIMARY KEY (tid)
);

-- Table layout coordinates from seating_layout.json
-- Format: tid, capacity, status, x_position, y_position
INSERT INTO seatingChart (capacity, status, x_position, y_position) VALUES
-- Row 1: 2-person tables (IDs 1-10)
(2, 0, 10, 10), (2, 0, 20, 10), (2, 0, 30, 10), (2, 0, 40, 10), (2, 1, 50, 10),
(2, 0, 60, 10), (2, 0, 70, 10), (2, 0, 80, 10), (2, 0, 90, 10), (2, 0, 10, 25),
-- Row 2: 4-person tables (IDs 11-27)
(4, 0, 20, 25), (4, 1, 30, 25), (4, 0, 40, 25), (4, 0, 50, 25), (4, 0, 60, 25),
(4, 0, 70, 25), (4, 0, 80, 25), (4, 0, 90, 25), (4, 0, 10, 40), (4, 0, 20, 40),
(4, 0, 30, 40), (4, 0, 40, 40), (4, 1, 50, 40), (4, 0, 60, 40), (4, 0, 70, 40),
(4, 0, 80, 40), (4, 0, 90, 40),
-- Row 3: 8-person tables (IDs 28-30)
(8, 0, 20, 55), (8, 0, 45, 55), (8, 0, 75, 55),
-- Row 4: 2-person tables (IDs 31-39)
(2, 0, 10, 70), (2, 0, 20, 70), (2, 0, 30, 70), (2, 0, 40, 70), (2, 0, 50, 70),
(2, 0, 60, 70), (2, 0, 70, 70), (2, 0, 80, 70), (2, 0, 90, 70),
-- Row 5: 4-person tables (IDs 40-44)
(4, 0, 20, 85), (4, 0, 35, 85), (4, 0, 50, 85), (4, 0, 65, 85), (4, 0, 80, 85),
-- Extra tables (IDs 45-50)
(8, 0, 10, 55), (8, 0, 40, 70), (8, 0, 65, 55), (2, 0, 30, 55), (2, 0, 55, 55), (2, 0, 75, 55);

CREATE TABLE booking (
  bid INT NOT NULL AUTO_INCREMENT,
  cid INT DEFAULT NULL,
  bkcname VARCHAR(255) NOT NULL,
  bktel INT NOT NULL,
  tid INT NOT NULL,
  bdate DATE NOT NULL,
  btime TIME NOT NULL,
  pnum INT NOT NULL,
  purpose VARCHAR(255) DEFAULT NULL,
  remark VARCHAR(255) DEFAULT NULL,
  status TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (bid),
  KEY bkcname (bkcname),
  KEY tid (tid),
  CONSTRAINT booking_ibfk_1 FOREIGN KEY (cid) REFERENCES customer(cid),
  CONSTRAINT booking_ibfk_2 FOREIGN KEY (tid) REFERENCES seatingChart(tid)
);

INSERT INTO booking (cid, bkcname, bktel, tid, bdate, btime, pnum, purpose, remark, status) VALUES
(1, 'Alex Wong', 21232123, 5, '2024-01-15', '18:30:00', 4, 'Family Dinner', 'We have a baby with us, need a high chair', 2),
(2, 'Tina Chan', 31233123, 12, '2024-01-16', '19:00:00', 2, 'Date Night', NULL, 3),
(3, 'Bowie', 61236123, 8, '2024-01-17', '20:00:00', 6, 'Business Meeting', 'Need a quiet area for discussion', 1),
(4, 'Samuel Lee', 61231212, 25, '2024-01-18', '12:30:00', 3, 'Lunch Meeting', NULL, 2),
(5, 'Emily Tsang', 61231555, 30, '2024-01-19', '13:00:00', 4, 'Birthday Celebration', 'Will bring a cake', 3);

CREATE TABLE table_orders (
  toid INT NOT NULL AUTO_INCREMENT,
  table_number INT NOT NULL,
  oid INT DEFAULT NULL,
  staff_id INT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (toid),
  CONSTRAINT fk_table_orders_oid FOREIGN KEY (oid) REFERENCES orders(oid),
  CONSTRAINT fk_table_orders_staff FOREIGN KEY (staff_id) REFERENCES staff(sid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO table_orders (table_number) VALUES (1);
INSERT INTO table_orders (table_number, staff_id) VALUES (2, 1);
INSERT INTO table_orders (table_number, staff_id) VALUES (3, 2);
INSERT INTO table_orders (table_number, oid, staff_id) VALUES (4, 1, 3);
INSERT INTO table_orders (table_number, oid, staff_id) VALUES (5, 2, 4);
INSERT INTO table_orders (table_number, oid, staff_id) VALUES (6, 2, 5);

-- =================================================================
-- PACKAGES
-- =================================================================

CREATE TABLE menu_package (
  package_id INT NOT NULL AUTO_INCREMENT,
  package_name VARCHAR(255) NOT NULL,
  num_of_type INT NOT NULL,
  package_image_url VARCHAR(255),
  amounts DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO menu_package (package_name, num_of_type, package_image_url, amounts) VALUES
('Double Set', 3, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/package/1.jpg', 180.00),
('Four Person Set', 4, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/package/2.jpg', 380.00),
('Business Set', 2, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/package/3.jpg', 120.00);

CREATE TABLE package_type (
  type_id INT NOT NULL AUTO_INCREMENT,
  package_id INT NOT NULL,
  optional_quantity INT NOT NULL DEFAULT 1,
  PRIMARY KEY (type_id),
  CONSTRAINT fk_package_type_package_id FOREIGN KEY (package_id) REFERENCES menu_package(package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO package_type (package_id, optional_quantity) VALUES
(1, 1), (1, 2), (1, 2), (1, 1),
(2, 2), (2, 1), (2, 3), (2, 4), (2, 1),
(3, 1), (3, 1), (3, 1);

CREATE TABLE package_type_translation (
  type_translation_id INT NOT NULL AUTO_INCREMENT,
  type_id INT NOT NULL,
  type_language_code VARCHAR(10) NOT NULL,
  type_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (type_translation_id),
  CONSTRAINT fk_package_type_translation_type_id FOREIGN KEY (type_id) REFERENCES package_type(type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO package_type_translation (type_id, type_language_code, type_name) VALUES
(1, 'en', 'Appetizer'), (2, 'en', 'Main Course'), (3, 'en', 'Drink'), (4, 'en', 'Staple Foods'),
(5, 'en', 'Appetizer'), (6, 'en', 'Soup'), (7, 'en', 'Main Course'), (8, 'en', 'Drink'), (9, 'en', 'Staple Foods'),
(10, 'en', 'Main Course'), (11, 'en', 'Drink'), (12, 'en', 'Staple Foods'),
(1, 'zh-CN', '前菜'), (2, 'zh-CN', '主菜'), (3, 'zh-CN', '饮料'), (4, 'zh-CN', '主食'),
(5, 'zh-CN', '前菜'), (6, 'zh-CN', '汤品'), (7, 'zh-CN', '主菜'), (8, 'zh-CN', '饮料'), (9, 'zh-CN', '主食'),
(10, 'zh-CN', '主菜'), (11, 'zh-CN', '饮料'), (12, 'zh-CN', '主食'),
(1, 'zh-TW', '前菜'), (2, 'zh-TW', '主菜'), (3, 'zh-TW', '飲料'), (4, 'zh-TW', '主食'),
(5, 'zh-TW', '前菜'), (6, 'zh-TW', '湯品'), (7, 'zh-TW', '主菜'), (8, 'zh-TW', '飲料'), (9, 'zh-TW', '主食'),
(10, 'zh-TW', '主菜'), (11, 'zh-TW', '飲料'), (12, 'zh-TW', '主食');

CREATE TABLE package_dish (
  package_id INT NOT NULL,
  type_id INT NOT NULL,
  item_id INT NOT NULL,
  price_modifier DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (package_id, type_id, item_id),
  CONSTRAINT fk_package_dish_package_id FOREIGN KEY (package_id) REFERENCES menu_package(package_id),
  CONSTRAINT fk_package_dish_type_id FOREIGN KEY (type_id) REFERENCES package_type(type_id),
  CONSTRAINT fk_package_dish_item_id FOREIGN KEY (item_id) REFERENCES menu_item(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO package_dish (package_id, type_id, item_id, price_modifier) VALUES
(1, 1, 1, 0.00), (1, 1, 2, 3.00), (1, 1, 3, 8.00),
(1, 2, 5, 0.00), (1, 2, 6, 8.00), (1, 2, 7, 6.00), (1, 2, 8, 12.00), (1, 2, 9, 10.00), (1, 2, 10, 7.00),
(1, 3, 12, 0.00), (1, 3, 13, 2.00), (1, 3, 14, 3.00), (1, 3, 15, 4.00), (1, 3, 16, 2.00), (1, 3, 17, 3.00), (1, 3, 18, 2.00),
(1, 4, 19, 0.00), (1, 4, 20, 0.00), (1, 4, 21, 0.00),
(2, 5, 1, 0.00), (2, 5, 2, 5.00), (2, 5, 3, 12.00),
(2, 6, 4, 0.00),
(2, 7, 5, 0.00), (2, 7, 6, 15.00), (2, 7, 7, 12.00), (2, 7, 8, 20.00), (2, 7, 9, 18.00), (2, 7, 10, 14.00),
(2, 8, 12, 0.00), (2, 8, 13, 3.00), (2, 8, 14, 4.00), (2, 8, 15, 5.00), (2, 8, 16, 3.00), (2, 8, 17, 4.00), (2, 8, 18, 3.00),
(2, 9, 19, 0.00), (2, 9, 20, 0.00), (2, 9, 21, 0.00),
(3, 10, 5, 0.00), (3, 10, 6, 12.00), (3, 10, 7, 10.00), (3, 10, 8, 18.00), (3, 10, 9, 15.00), (3, 10, 10, 12.00),
(3, 11, 14, 0.00), (3, 11, 16, 3.00), (3, 11, 17, 4.00), (3, 11, 18, 3.00),
(3, 12, 19, 0.00), (3, 12, 20, 0.00), (3, 12, 21, 0.00);

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

-- =================================================================
-- PACKAGE ITEM CUSTOMIZATIONS (v4.6 NEW)
-- =================================================================
-- Store customization details for individual items within a package order

DROP TABLE IF EXISTS order_package_item_customizations;
CREATE TABLE order_package_item_customizations (
  package_customization_id INT NOT NULL AUTO_INCREMENT,
  oid INT NOT NULL,
  op_id INT NOT NULL,
  package_id INT NOT NULL,
  item_id INT NOT NULL,
  group_id INT NOT NULL,
  option_id INT NOT NULL,
  selected_value_ids JSON DEFAULT NULL,
  selected_values JSON DEFAULT NULL,
  text_value VARCHAR(500) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (package_customization_id),
  FOREIGN KEY (oid) REFERENCES orders(oid) ON DELETE CASCADE,
  FOREIGN KEY (op_id) REFERENCES order_packages(op_id) ON DELETE CASCADE,
  FOREIGN KEY (package_id) REFERENCES menu_package(package_id) ON DELETE CASCADE,
  FOREIGN KEY (item_id) REFERENCES menu_item(item_id) ON DELETE CASCADE,
  FOREIGN KEY (group_id) REFERENCES customization_option_group(group_id) ON DELETE CASCADE,
  FOREIGN KEY (option_id) REFERENCES item_customization_options(option_id) ON DELETE CASCADE,
  KEY idx_order_package (oid, op_id),
  KEY idx_package_item (package_id, item_id),
  KEY idx_group_option (group_id, option_id),
  KEY idx_package_customizations (oid, package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =================================================================
-- MATERIALS AND RECIPES
-- =================================================================

CREATE TABLE materials_category (
  category_id INT NOT NULL AUTO_INCREMENT,
  category_name VARCHAR(100) NOT NULL,
  PRIMARY KEY (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO materials_category (category_name) VALUES
('Vegetable'),
('Meat'),
('Condiment'),
('Grain'),
('Protein'),
('Beverage'),
('Other');

CREATE TABLE materials (
  mid INT NOT NULL AUTO_INCREMENT,
  mname VARCHAR(255) NOT NULL,
  category_id INT DEFAULT NULL,
  unit VARCHAR(50) DEFAULT NULL,
  mqty DECIMAL(10,2) DEFAULT NULL,
  reorderLevel DECIMAL(10,2) DEFAULT NULL,  
  PRIMARY KEY (mid),
  FOREIGN KEY (category_id) REFERENCES materials_category(category_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO materials (mname, category_id, unit, mqty, reorderLevel) VALUES
('Cucumber', 1, 'grams', 500.00, 200.00),          
('Chicken', 2, 'grams', 2000.00, 500.00),         
('Soy Sauce', 3, 'ml', 1000.00, 300.00),          
('Chili Oil', 3, 'ml', 500.00, 150.00),           
('Rice', 4, 'grams', 10000.00, 2000.00),          
('Beef', 2, 'grams', 1500.00, 400.00),            
('Tofu', 5, 'grams', 800.00, 250.00),
('Eggplant', 1, 'grams', 2000.00, 500.00),
('Glutinous Rice', 4, 'grams', 3000.00, 800.00),
('Lemon', 1, 'grams', 1500.00, 400.00),
('7-Up', 6, 'ml', 5000.00, 1000.00),
('Red Beans', 1, 'grams', 2500.00, 600.00),
('Milk', 6, 'ml', 3000.00, 800.00),
('Tea Leaves', 1, 'grams', 1000.00, 300.00),
('Grapes', 1, 'grams', 2000.00, 500.00),
('Noodles', 4, 'grams', 5000.00, 1200.00),
('Potato Starch', 4, 'grams', 1500.00, 400.00);             

CREATE TABLE consumption_history (
  log_id INT AUTO_INCREMENT PRIMARY KEY,
  mid INT NOT NULL,
  log_date DATE NOT NULL,
  log_type ENUM('Deduction', 'Forecast', 'Reorder') NOT NULL,
  details TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_consumption_material FOREIGN KEY (mid) REFERENCES materials(mid) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recipe_materials (
  id INT NOT NULL AUTO_INCREMENT,
  item_id INT NOT NULL,
  mid INT NOT NULL,
  quantity DECIMAL(10,2) DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (item_id) REFERENCES menu_item(item_id) ON DELETE CASCADE,
  FOREIGN KEY (mid) REFERENCES materials(mid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO recipe_materials (item_id, mid, quantity) VALUES
-- Existing recipes
(3, 2, 200), (3, 3, 20), (3, 4, 10),
(6, 7, 150), (6, 6, 50), (6, 3, 15),
-- Added material requirements for missing items
(1, 2, 120), (1, 3, 10),
(2, 2, 150), (2, 3, 15), (2, 5, 300),
(4, 6, 200), (4, 3, 25), (4, 5, 250),
(5, 2, 250), (5, 6, 100), (5, 3, 30), (5, 4, 15),
-- Additional recipes for previously missing items
(7, 2, 100), (7, 3, 20),                    -- Dan Dan Noodles
(8, 2, 180), (8, 3, 15),                    -- Twice-Cooked Pork
(9, 6, 200),                                -- Boiled Beef in Chili Broth
(10, 8, 300), (10, 3, 20), (10, 4, 10),     -- Fish-Fragrant Eggplant
(11, 9, 200), (11, 3, 10),                  -- Sichuan Glutinous Rice Cake
(12, 11, 250), (12, 10, 30),                -- Salty Lemon 7-Up
(13, 12, 100), (13, 13, 150),               -- Red Bean Ice
(14, 13, 200), (14, 14, 5),                 -- Hot Milk Tea
(15, 15, 80), (15, 14, 8),                  -- Grape Oolong Tea
(16, 10, 40), (16, 14, 6),                  -- Hot Lemon Tea
(17, 13, 180), (17, 14, 5),                 -- Iced Milk Tea
(18, 10, 35), (18, 14, 6),                  -- Iced Lemon Tea
(19, 5, 150),                               -- Steamed Rice
(20, 16, 120),                              -- Noodles
(21, 17, 50);                               -- Potato Starch
-- Note: Wooden Chopsticks (item_id: 22) has no recipe - directly purchased




COMMIT;
