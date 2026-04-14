


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

CREATE TABLE menu_category_translation (
  translation_id INT PRIMARY KEY AUTO_INCREMENT,
  category_id INT NOT NULL,
  language_code VARCHAR(10) NOT NULL,
  category_name VARCHAR(100) NOT NULL,
  FOREIGN KEY (category_id) REFERENCES menu_category(category_id) ON DELETE CASCADE,
  UNIQUE KEY uq_category_lang (category_id, language_code)
);

INSERT INTO menu_category (category_name) VALUES
('Appetizers'),
('Soup'),
('Main Courses'),
('Dessert'),
('Drink'),
('Staple Foods');

INSERT INTO menu_category (category_name) VALUES ('Supplies');

INSERT INTO menu_category_translation (category_id, language_code, category_name) VALUES
(1, 'en', 'Appetizers'),
(1, 'zh-CN', '前菜'),
(1, 'zh-TW', '前菜'),
(2, 'en', 'Soup'),
(2, 'zh-CN', '汤'),
(2, 'zh-TW', '湯品'),
(3, 'en', 'Main Courses'),
(3, 'zh-CN', '主菜'),
(3, 'zh-TW', '主菜'),
(4, 'en', 'Dessert'),
(4, 'zh-CN', '甜品'),
(4, 'zh-TW', '甜品'),
(5, 'en', 'Drink'),
(5, 'zh-CN', '饮料'),
(5, 'zh-TW', '飲料'),
(6, 'en', 'Staple Foods'),
(6, 'zh-CN', '主食'),
(6, 'zh-TW', '主食'),
(7, 'en', 'Supplies'),
(7, 'zh-CN', '用品'),
(7, 'zh-TW', '用品');


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
  PRIMARY KEY (tid)
);

-- Table layout based on uploaded floor plan
-- Color legend in drawing: yellow=4 seats, green=2 seats, red=8 seats
INSERT INTO seatingChart (tid, capacity, status) VALUES
(1, 2, 0),
(2, 2, 0),
(3, 2, 0),
(4, 2, 0),
(5, 4, 0),
(6, 4, 0),
(7, 4, 0),
(8, 4, 0),
(9, 4, 0),
(10, 4, 0),
(11, 4, 0),
(12, 4, 0),
(13, 4, 0),
(14, 4, 0),
(15, 4, 0),
(16, 4, 0),
(17, 8, 0),
(18, 8, 0);

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
(1, 'Alex Wong', 21232123, 4, '2024-01-15', '18:30:00', 4, 'Family Dinner', 'We have a baby with us, need a high chair', 2),
(2, 'Tina Chan', 31233123, 7, '2024-01-16', '19:00:00', 2, 'Date Night', NULL, 3),
(3, 'Bowie', 61236123, 17, '2024-01-17', '20:00:00', 6, 'Business Meeting', 'Need a quiet area for discussion', 1),
(4, 'Samuel Lee', 61231212, 10, '2024-01-18', '12:30:00', 3, 'Lunch Meeting', NULL, 2),
(5, 'Emily Tsang', 61231555, 11, '2024-01-19', '13:00:00', 4, 'Birthday Celebration', 'Will bring a cake', 3);

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

INSERT INTO customer (cname, cpassword, ctel, caddr, company, cemail, cbirthday, crole, cimageurl, coupon_point) VALUES
('David Chan', 'password123', 91234567, '30 Canton Road, Tsim Sha Tsui, Kowloon, Hong Kong', 'Eastern Trading Company', 'david.chan@example.com', '05-18', 'customer', NULL, 0),
('Mary Li', 'meiling2025', 92345678, '12/F, Tower 1, Times Square, Causeway Bay, Hong Kong', 'MayLin Design Studio', 'mary.li@example.com', '08-22', 'customer', NULL, 0),
('John Zhang', 'zhangwq789', 93456789, '88 Des Voeux Road Central, Central, Hong Kong', 'StrongTech Solutions', 'john.zhang@example.com', '11-05', 'customer', NULL, 0),
('Sarah Wang', 'xiaowen888', 94567890, '200 Hennessy Road, Wan Chai, Hong Kong', 'Creative Culture Media', 'sarah.wang@example.com', '02-14', 'customer', NULL, 0),
('Kevin Liu', 'liujiahui66', 95678901, '55 Hoi Yuen Road, Kwun Tong, Hong Kong', 'Kevin Logistics Ltd.', 'kevin.liu@example.com', '09-30', 'customer', NULL, 0),
('Michael Wong', 'wongchiwai99', 96789012, 'Shop 238, New Town Plaza Phase 1, Sha Tin, Hong Kong', 'Michael Engineering Consultants', 'michael.wong@example.com', '12-25', 'customer', NULL, 0),
('Susan Lam', 'lamsauman77', 97890123, '398 Castle Peak Road, Tsuen Wan, Hong Kong', 'Susan Fashion Boutique', 'susan.lam@example.com', '07-08', 'customer', NULL, 0);

INSERT INTO materials (mname, category_id, unit, mqty, reorderLevel) VALUES
('Chili Powder', 3, 'grams', 80.00, 100.00),      -- 庫存80g，低於重新訂購水平100g
('Garlic', 1, 'grams', 800.00, 300.00),           -- 庫存800g，高於重新訂購水平300g
('Ginger', 1, 'grams', 60.00, 150.00),            -- 庫存60g，低於重新訂購水平150g
('Soy Sauce', 3, 'ml', 1200.00, 500.00),          -- 庫存1200ml，高於重新訂購水平500ml
('Rice Vinegar', 3, 'ml', 900.00, 400.00);        -- 庫存900ml，高於重新訂購水平400ml

-- 新增材料分類（如果需要）
-- 檢查是否已有這些分類，如果沒有則插入
INSERT IGNORE INTO materials_category (category_name) VALUES
('Spice'),
('Vegetable'),
('Sauce');

-- 新增5份Appetizers (category_id=1)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(1, 32.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/23.jpg', 2, TRUE),
(1, 30.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/24.jpg', 1, TRUE),
(1, 35.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/25.jpg', 3, TRUE),
(1, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/26.jpg', 0, TRUE),
(1, 34.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/27.jpg', 2, TRUE);

-- 新增3份Soup (category_id=2)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(2, 52.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/28.jpg', 1, TRUE),
(2, 56.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/29.jpg', 2, TRUE),
(2, 48.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/30.jpg', 0, TRUE);

-- 新增3份Dessert (category_id=4)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(4, 25.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/31.jpg', 0, TRUE),
(4, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/32.jpg', 0, TRUE),
(4, 30.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/33.jpg', 0, TRUE);

-- 新增5份Main Courses (category_id=3)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(3, 98.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/34.jpg', 4, TRUE),
(3, 85.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/35.jpg', 3, TRUE),
(3, 92.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/36.jpg', 5, TRUE),
(3, 78.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/37.jpg', 2, TRUE),
(3, 88.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/38.jpg', 3, TRUE);

-- 新增3份Drink (category_id=5)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(5, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/39.jpg', 0, TRUE),
(5, 24.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/40.jpg', 0, TRUE),
(5, 30.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/41.jpg', 0, TRUE);

-- =================================================================
-- 新增菜餚的多語言翻譯
-- =================================================================

-- 開胃菜 (Appetizers) 翻譯
-- Item 23
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(23, 'en', 'Crispy Lotus Root', 'Thinly sliced lotus root, deep-fried to perfection with a hint of sesame oil.'),
(23, 'zh-CN', '香脆藕片', '薄切藕片，炸至金黄酥脆，带有芝麻油香气。'),
(23, 'zh-TW', '香脆藕片', '薄切藕片，炸至金黃酥脆，帶有芝麻油香氣。');

-- Item 24
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(24, 'en', 'Spicy Jellyfish', 'Marinated jellyfish with chili oil and sesame, crispy and refreshing.'),
(24, 'zh-CN', '麻辣海蜇', '用辣油和芝麻腌制的海蜇，爽脆开胃。'),
(24, 'zh-TW', '麻辣海蜇', '用辣油和芝麻醃製的海蜇，爽脆開胃。');

-- Item 25
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(25, 'en', 'Garlic Edamame', 'Fresh edamame pods tossed in garlic butter and sea salt.'),
(25, 'zh-CN', '蒜香毛豆', '新鲜毛豆拌入蒜香黄油和海盐，香气扑鼻。'),
(25, 'zh-TW', '蒜香毛豆', '新鮮毛豆拌入蒜香奶油和海鹽，香氣撲鼻。');

-- Item 26
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(26, 'en', 'Vegetable Spring Rolls', 'Crispy spring rolls filled with fresh vegetables and glass noodles.'),
(26, 'zh-CN', '蔬菜春卷', '外皮酥脆，内馅是新鲜蔬菜和粉丝。'),
(26, 'zh-TW', '蔬菜春捲', '外皮酥脆，內餡是新鮮蔬菜和粉絲。');

-- Item 27
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(27, 'en', 'Sichuan Peanuts', 'Spicy peanuts stir-fried with Sichuan peppercorns and chili flakes.'),
(27, 'zh-CN', '四川辣花生', '用四川花椒和辣椒片炒制的辣味花生。'),
(27, 'zh-TW', '四川辣花生', '用四川花椒和辣椒片炒製的辣味花生。');

-- 湯品 (Soup) 翻譯
-- Item 28
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(28, 'en', 'Hot & Sour Soup', 'Classic Sichuan hot and sour soup with tofu, mushrooms, and bamboo shoots.'),
(28, 'zh-CN', '酸辣汤', '经典的四川酸辣汤，内有豆腐、蘑菇和竹笋。'),
(28, 'zh-TW', '酸辣湯', '經典的四川酸辣湯，內有豆腐、蘑菇和竹筍。');

-- Item 29
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(29, 'en', 'Winter Melon Soup', 'Light and clear winter melon soup with goji berries and chicken broth.'),
(29, 'zh-CN', '冬瓜汤', '清淡的冬瓜汤，加入枸杞和鸡汤。'),
(29, 'zh-TW', '冬瓜湯', '清淡的冬瓜湯，加入枸杞和雞湯。');

-- Item 30
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(30, 'en', 'Corn and Crab Meat Soup', 'Creamy corn soup with fresh crab meat and egg white.'),
(30, 'zh-CN', '玉米蟹肉汤', '奶油玉米汤加入新鲜蟹肉和蛋白。'),
(30, 'zh-TW', '玉米蟹肉湯', '奶油玉米湯加入新鮮蟹肉和蛋白。');

-- 甜品 (Dessert) 翻譯
-- Item 31
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(31, 'en', 'Mango Pudding', 'Fresh mango pudding topped with condensed milk and mango chunks.'),
(31, 'zh-CN', '芒果布丁', '新鲜芒果布丁，淋上炼乳和芒果块。'),
(31, 'zh-TW', '芒果布丁', '新鮮芒果布丁，淋上煉乳和芒果塊。');

-- Item 32
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(32, 'en', 'Sesame Balls', 'Deep-fried glutinous rice balls filled with sweet red bean paste.'),
(32, 'zh-CN', '芝麻球', '油炸糯米球，内馅是甜红豆沙。'),
(32, 'zh-TW', '芝麻球', '油炸糯米球，內餡是甜紅豆沙。');

-- Item 33
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(33, 'en', 'Egg Tart', 'Classic Hong Kong-style egg tarts with flaky pastry.'),
(33, 'zh-CN', '蛋挞', '经典港式蛋挞，外皮酥脆。'),
(33, 'zh-TW', '蛋撻', '經典港式蛋撻，外皮酥脆。');

-- 主菜 (Main Courses) 翻譯
-- Item 34
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(34, 'en', 'Kung Pao Chicken', 'Diced chicken stir-fried with peanuts, chili, and Sichuan peppercorns.'),
(34, 'zh-CN', '宫保鸡丁', '鸡肉丁炒花生、辣椒和四川花椒。'),
(34, 'zh-TW', '宮保雞丁', '雞肉丁炒花生、辣椒和四川花椒。');

-- Item 35
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(35, 'en', 'Sweet & Sour Pork', 'Crispy pork pieces in tangy sweet and sour sauce with pineapple.'),
(35, 'zh-CN', '糖醋里脊', '酥脆的猪肉块裹上酸甜酱汁，配菠萝。'),
(35, 'zh-TW', '糖醋里脊', '酥脆的豬肉塊裹上酸甜醬汁，配鳳梨。');

-- Item 36
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(36, 'en', 'Sichuan Dry Pot with Seafood', 'Assorted seafood cooked in a spicy dry pot with vegetables.'),
(36, 'zh-CN', '四川海鲜干锅', '多种海鲜与蔬菜在麻辣干锅中烹制。'),
(36, 'zh-TW', '四川海鮮乾鍋', '多種海鮮與蔬菜在麻辣乾鍋中烹製。');

-- Item 37
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(37, 'en', 'Braised Pork Belly', 'Pork belly braised in soy sauce and spices until tender.'),
(37, 'zh-CN', '红烧肉', '五花肉用酱油和香料炖煮至软嫩。'),
(37, 'zh-TW', '紅燒肉', '五花肉用醬油和香料燉煮至軟嫩。');

-- Item 38
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(38, 'en', 'Lemon Chicken', 'Crispy chicken with lemon sauce, sweet and tangy.'),
(38, 'zh-CN', '柠檬鸡', '酥脆鸡肉配柠檬酱，酸甜可口。'),
(38, 'zh-TW', '檸檬雞', '酥脆雞肉配檸檬醬，酸甜可口。');

-- 飲料 (Drink) 翻譯
-- Item 39
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(39, 'en', 'Iced Honey Lemon', 'Refreshing iced drink with honey and fresh lemon slices.'),
(39, 'zh-CN', '冰蜂蜜柠檬', '蜂蜜和新鲜柠檬片制成的冰镇饮品。'),
(39, 'zh-TW', '冰蜂蜜檸檬', '蜂蜜和新鮮檸檬片製成的冰鎮飲品。');

-- Item 40
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(40, 'en', 'Peach Oolong Tea', 'Oolong tea infused with peach flavor, served hot or cold.'),
(40, 'zh-CN', '蜜桃乌龙茶', '带有蜜桃香气的乌龙茶，可热可冰。'),
(40, 'zh-TW', '蜜桃烏龍茶', '帶有蜜桃香氣的烏龍茶，可熱可冰。');

-- Item 41
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(41, 'en', 'Coconut Milkshake', 'Creamy coconut milkshake topped with shredded coconut.'),
(41, 'zh-CN', '椰奶昔', '浓郁的椰奶奶昔，撒上椰丝。'),
(41, 'zh-TW', '椰奶昔', '濃郁的椰奶奶昔，撒上椰絲。');

-- =================================================================
-- 新增菜餚的標籤
-- =================================================================

-- 首先檢查是否已有這些標籤，若無則插入
INSERT IGNORE INTO tag (tag_name, tag_category, tag_bg_color) VALUES
('vegetarian', 'Dietary', '#4CAF50'),
('refreshing', 'Characteristic', '#2196F3'),
('chicken', 'Protein', '#FFC107'),
('spicy', 'Flavor', '#F44336'),
('sour', 'Flavor', '#FF9800'),
('numbing', 'Flavor', '#9C27B0'),
('pork', 'Protein', '#FF5722'),
('classic', 'Characteristic', '#00BCD4'),
('sweet', 'Flavor', '#FFEB3B'),
('glutinous', 'Type', '#607D8B'),
('lemon', 'Flavor', '#FFEB3B'),
('fish', 'Protein', '#3F51B5'),
('tofu', 'Protein', '#009688'),
('beef', 'Protein', '#E91E63'),
('streetfood', 'Type', '#795548'),
('stirfry', 'Cooking Method', '#8BC34A'),
('traditional', 'Characteristic', '#607D8B'),
('cold', 'Temperature', '#03A9F4'),
('milk', 'Ingredient', '#795548'),
('soda', 'Type', '#03A9F4'),
('noodles', 'Type', '#673AB7'),
('grape', 'Flavor', '#9C27B0');

-- 為新增菜餚添加標籤
-- 注意：這裡使用SELECT語句來獲取標籤ID，確保引用正確
INSERT INTO menu_tag (item_id, tag_id) 
SELECT item_id, t.tag_id 
FROM (
    -- Appetizers
    SELECT 23 AS item_id, 'vegetarian' AS tag_name
    UNION ALL SELECT 23, 'refreshing'
    UNION ALL SELECT 24, 'spicy'
    UNION ALL SELECT 24, 'refreshing'
    UNION ALL SELECT 25, 'vegetarian'
    UNION ALL SELECT 25, 'refreshing'
    UNION ALL SELECT 26, 'vegetarian'
    UNION ALL SELECT 27, 'spicy'
    
    -- Soups
    UNION ALL SELECT 28, 'spicy'
    UNION ALL SELECT 28, 'sour'
    UNION ALL SELECT 29, 'vegetarian'
    UNION ALL SELECT 29, 'refreshing'
    UNION ALL SELECT 30, 'refreshing'
    
    -- Desserts
    UNION ALL SELECT 31, 'sweet'
    UNION ALL SELECT 32, 'sweet'
    UNION ALL SELECT 32, 'glutinous'
    UNION ALL SELECT 33, 'sweet'
    UNION ALL SELECT 33, 'classic'
    
    -- Main Courses
    UNION ALL SELECT 34, 'chicken'
    UNION ALL SELECT 34, 'spicy'
    UNION ALL SELECT 34, 'classic'
    UNION ALL SELECT 35, 'pork'
    UNION ALL SELECT 35, 'sweet'
    UNION ALL SELECT 35, 'sour'
    UNION ALL SELECT 36, 'spicy'
    UNION ALL SELECT 36, 'numbing'
    UNION ALL SELECT 37, 'pork'
    UNION ALL SELECT 37, 'classic'
    UNION ALL SELECT 38, 'chicken'
    UNION ALL SELECT 38, 'sour'
    
    -- Drinks
    UNION ALL SELECT 39, 'sweet'
    UNION ALL SELECT 39, 'refreshing'
    UNION ALL SELECT 40, 'refreshing'
    UNION ALL SELECT 41, 'sweet'
) AS item_tags
JOIN tag t ON item_tags.tag_name = t.tag_name;

-- =================================================================
-- 新增菜餚的自定義選項
-- =================================================================

-- 為新菜餚添加自定義選項
INSERT INTO item_customization_options (item_id, group_id, max_selections, is_required) VALUES
-- Appetizers (23-27): Spice Level
(23, 1, 1, 0), (24, 1, 1, 0), (25, 1, 1, 0), (26, 1, 1, 0), (27, 1, 1, 0),

-- Soups (28-30): Spice Level
(28, 1, 1, 0), (29, 1, 1, 0), (30, 1, 1, 0),

-- Main Courses (34-38): Spice Level
(34, 1, 1, 0), (35, 1, 1, 0), (36, 1, 1, 0), (37, 1, 1, 0), (38, 1, 1, 0),

-- Desserts (31-33): Toppings
(31, 5, 2, 0), (32, 5, 2, 0), (33, 5, 2, 0),

-- Drinks (39-41): Sugar Level + Ice Level
(39, 2, 1, 0), (39, 3, 1, 0),
(40, 2, 1, 0), (40, 3, 1, 0),
(41, 2, 1, 0), (41, 3, 1, 0);


-- =================================================================
-- 銷售數據 (2025年10月1日 - 2026年1月31日)
-- 共50筆訂單，oid 範圍 29～78
-- =================================================================


-- =================================================================
-- 2025年10月訂單 (12筆)
-- =================================================================
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(29, '2025-10-02 11:30:00', 1, 3, 'order_20251002_001', NULL, 'dine_in', 5),
(30, '2025-10-03 12:15:00', 2, 3, 'order_20251003_002', 1, 'takeaway', NULL),
(31, '2025-10-05 13:45:00', 3, 3, 'order_20251005_003', NULL, 'dine_in', 12),
(32, '2025-10-07 18:30:00', 4, 3, 'order_20251007_004', 2, 'dine_in', 8),
(33, '2025-10-09 19:15:00', 5, 3, 'order_20251009_005', NULL, 'dine_in', 15),
(34, '2025-10-11 12:00:00', 1, 3, 'order_20251011_006', 3, 'takeaway', NULL),
(35, '2025-10-13 13:30:00', 2, 3, 'order_20251013_007', NULL, 'dine_in', 22),
(36, '2025-10-15 18:45:00', 3, 3, 'order_20251015_008', 1, 'dine_in', 7),
(37, '2025-10-17 19:30:00', 4, 3, 'order_20251017_009', NULL, 'takeaway', NULL),
(38, '2025-10-19 12:45:00', 5, 3, 'order_20251019_010', 2, 'dine_in', 18),
(39, '2025-10-22 14:00:00', 1, 3, 'order_20251022_011', NULL, 'dine_in', 9),
(40, '2025-10-25 17:30:00', 2, 3, 'order_20251025_012', 3, 'dine_in', 25);

-- =================================================================
-- 2025年11月訂單 (13筆)
-- =================================================================
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(41, '2025-11-01 11:45:00', 3, 3, 'order_20251101_013', NULL, 'takeaway', NULL),
(42, '2025-11-03 12:30:00', 4, 3, 'order_20251103_014', 1, 'dine_in', 6),
(43, '2025-11-05 13:15:00', 5, 3, 'order_20251105_015', NULL, 'dine_in', 14),
(44, '2025-11-07 18:00:00', 1, 3, 'order_20251107_016', 2, 'dine_in', 11),
(45, '2025-11-09 19:45:00', 2, 3, 'order_20251109_017', NULL, 'takeaway', NULL),
(46, '2025-11-12 12:30:00', 3, 3, 'order_20251112_018', 3, 'dine_in', 20),
(47, '2025-11-14 13:45:00', 4, 3, 'order_20251114_019', NULL, 'dine_in', 3),
(48, '2025-11-16 18:30:00', 5, 3, 'order_20251116_020', 1, 'dine_in', 16),
(49, '2025-11-18 19:15:00', 1, 3, 'order_20251118_021', NULL, 'takeaway', NULL),
(50, '2025-11-21 12:15:00', 2, 3, 'order_20251121_022', 2, 'dine_in', 24),
(51, '2025-11-23 14:30:00', 3, 3, 'order_20251123_023', NULL, 'dine_in', 10),
(52, '2025-11-25 17:45:00', 4, 3, 'order_20251125_024', 3, 'takeaway', NULL),
(53, '2025-11-28 19:00:00', 5, 3, 'order_20251128_025', NULL, 'dine_in', 19);

-- =================================================================
-- 2025年12月訂單 (13筆)
-- =================================================================
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(54, '2025-12-01 11:30:00', 1, 3, 'order_20251201_026', NULL, 'dine_in', 4),
(55, '2025-12-03 12:45:00', 2, 3, 'order_20251203_027', 1, 'takeaway', NULL),
(56, '2025-12-05 13:30:00', 3, 3, 'order_20251205_028', NULL, 'dine_in', 13),
(57, '2025-12-07 18:15:00', 4, 3, 'order_20251207_029', 2, 'dine_in', 21),
(58, '2025-12-09 19:30:00', 5, 3, 'order_20251209_030', NULL, 'dine_in', 8),
(59, '2025-12-11 12:45:00', 1, 3, 'order_20251211_031', 3, 'takeaway', NULL),
(60, '2025-12-13 14:00:00', 2, 3, 'order_20251213_032', NULL, 'dine_in', 17),
(61, '2025-12-15 18:45:00', 3, 3, 'order_20251215_033', 1, 'dine_in', 2),
(62, '2025-12-17 19:15:00', 4, 3, 'order_20251217_034', NULL, 'takeaway', NULL),
(63, '2025-12-19 13:00:00', 5, 3, 'order_20251219_035', 2, 'dine_in', 23),
(64, '2025-12-22 14:30:00', 1, 3, 'order_20251222_036', NULL, 'dine_in', 7),
(65, '2025-12-25 18:00:00', 2, 3, 'order_20251225_037', 3, 'dine_in', 15),
(66, '2025-12-28 19:45:00', 3, 3, 'order_20251228_038', NULL, 'takeaway', NULL);

-- =================================================================
-- 2026年1月訂單 (12筆)
-- =================================================================
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(67, '2026-01-02 11:15:00', 4, 3, 'order_20260102_039', NULL, 'dine_in', 9),
(68, '2026-01-04 12:30:00', 5, 3, 'order_20260104_040', 1, 'takeaway', NULL),
(69, '2026-01-06 13:45:00', 1, 3, 'order_20260106_041', NULL, 'dine_in', 18),
(70, '2026-01-08 18:30:00', 2, 3, 'order_20260108_042', 2, 'dine_in', 5),
(71, '2026-01-10 19:15:00', 3, 3, 'order_20260110_043', NULL, 'dine_in', 12),
(72, '2026-01-12 12:00:00', 4, 3, 'order_20260112_044', 3, 'takeaway', NULL),
(73, '2026-01-14 13:30:00', 5, 3, 'order_20260114_045', NULL, 'dine_in', 22),
(74, '2026-01-16 18:45:00', 1, 3, 'order_20260116_046', 1, 'dine_in', 14),
(75, '2026-01-18 19:30:00', 2, 3, 'order_20260118_047', NULL, 'takeaway', NULL),
(76, '2026-01-20 12:45:00', 3, 3, 'order_20260120_048', 2, 'dine_in', 10),
(77, '2026-01-23 14:00:00', 4, 3, 'order_20260123_049', NULL, 'dine_in', 25),
(78, '2026-01-26 17:30:00', 5, 3, 'order_20260126_050', 3, 'dine_in', 6);

-- =================================================================
-- 訂單項目 (包含原有菜餚和新菜餚)
-- =================================================================
INSERT INTO order_items (oid, item_id, qty) VALUES
-- 訂單 29 (2025-10-02)
(29, 5, 1), (29, 12, 2), (29, 23, 1),
-- 訂單 30 (2025-10-03)
(30, 6, 1), (30, 17, 1), (30, 24, 2),
-- 訂單 31 (2025-10-05)
(31, 8, 1), (31, 14, 2), (31, 28, 1),
-- 訂單 32 (2025-10-07)
(32, 9, 2), (32, 15, 1), (32, 25, 1),
-- 訂單 33 (2025-10-09)
(33, 10, 1), (33, 16, 2), (33, 29, 1),
-- 訂單 34 (2025-10-11)
(34, 7, 1), (34, 18, 1), (34, 26, 1), (34, 31, 1),
-- 訂單 35 (2025-10-13)
(35, 5, 2), (35, 13, 1), (35, 27, 1),
-- 訂單 36 (2025-10-15)
(36, 6, 1), (36, 12, 2), (36, 30, 1),
-- 訂單 37 (2025-10-17)
(37, 8, 2), (37, 17, 1), (37, 32, 1),
-- 訂單 38 (2025-10-19)
(38, 9, 1), (38, 14, 2), (38, 33, 1),
-- 訂單 39 (2025-10-22)
(39, 10, 1), (39, 15, 1), (39, 34, 1),
-- 訂單 40 (2025-10-25)
(40, 7, 2), (40, 16, 1), (40, 35, 1),
-- 訂單 41 (2025-11-01)
(41, 5, 1), (41, 18, 2), (41, 36, 1),
-- 訂單 42 (2025-11-03)
(42, 6, 2), (42, 12, 1), (42, 37, 1),
-- 訂單 43 (2025-11-05)
(43, 8, 1), (43, 17, 1), (43, 38, 1),
-- 訂單 44 (2025-11-07)
(44, 9, 2), (44, 14, 1), (44, 23, 1), (44, 39, 2),
-- 訂單 45 (2025-11-09)
(45, 10, 1), (45, 15, 2), (45, 24, 1),
-- 訂單 46 (2025-11-12)
(46, 7, 1), (46, 16, 1), (46, 25, 1), (46, 40, 1),
-- 訂單 47 (2025-11-14)
(47, 5, 2), (47, 18, 1), (47, 26, 1),
-- 訂單 48 (2025-11-16)
(48, 6, 1), (48, 12, 2), (48, 27, 1),
-- 訂單 49 (2025-11-18)
(49, 8, 1), (49, 17, 1), (49, 28, 1), (49, 41, 1),
-- 訂單 50 (2025-11-21)
(50, 9, 2), (50, 14, 1), (50, 29, 1),
-- 訂單 51 (2025-11-23)
(51, 10, 1), (51, 15, 2), (51, 30, 1),
-- 訂單 52 (2025-11-25)
(52, 7, 1), (52, 16, 1), (52, 31, 1),
-- 訂單 53 (2025-11-28)
(53, 5, 2), (53, 18, 1), (53, 32, 1),
-- 訂單 54 (2025-12-01)
(54, 6, 1), (54, 12, 2), (54, 33, 1),
-- 訂單 55 (2025-12-03)
(55, 8, 1), (55, 17, 1), (55, 34, 1),
-- 訂單 56 (2025-12-05)
(56, 9, 2), (56, 14, 1), (56, 35, 1),
-- 訂單 57 (2025-12-07)
(57, 10, 1), (57, 15, 2), (57, 36, 1),
-- 訂單 58 (2025-12-09)
(58, 7, 1), (58, 16, 1), (58, 37, 1),
-- 訂單 59 (2025-12-11)
(59, 5, 2), (59, 18, 1), (59, 38, 1),
-- 訂單 60 (2025-12-13)
(60, 6, 1), (60, 12, 2), (60, 23, 1), (60, 39, 1),
-- 訂單 61 (2025-12-15)
(61, 8, 1), (61, 17, 1), (61, 24, 1),
-- 訂單 62 (2025-12-17)
(62, 9, 2), (62, 14, 1), (62, 25, 1),
-- 訂單 63 (2025-12-19)
(63, 10, 1), (63, 15, 2), (63, 26, 1),
-- 訂單 64 (2025-12-22)
(64, 7, 1), (64, 16, 1), (64, 27, 1),
-- 訂單 65 (2025-12-25)
(65, 5, 2), (65, 18, 1), (65, 28, 1),
-- 訂單 66 (2025-12-28)
(66, 6, 1), (66, 12, 2), (66, 29, 1),
-- 訂單 67 (2026-01-02)
(67, 8, 1), (67, 17, 1), (67, 30, 1),
-- 訂單 68 (2026-01-04)
(68, 9, 2), (68, 14, 1), (68, 31, 1),
-- 訂單 69 (2026-01-06)
(69, 10, 1), (69, 15, 2), (69, 32, 1),
-- 訂單 70 (2026-01-08)
(70, 7, 1), (70, 16, 1), (70, 33, 1),
-- 訂單 71 (2026-01-10)
(71, 5, 2), (71, 18, 1), (71, 34, 1),
-- 訂單 72 (2026-01-12)
(72, 6, 1), (72, 12, 2), (72, 35, 1),
-- 訂單 73 (2026-01-14)
(73, 8, 1), (73, 17, 1), (73, 36, 1),
-- 訂單 74 (2026-01-16)
(74, 9, 2), (74, 14, 1), (74, 37, 1),
-- 訂單 75 (2026-01-18)
(75, 10, 1), (75, 15, 2), (75, 38, 1),
-- 訂單 76 (2026-01-20)
(76, 7, 1), (76, 16, 1), (76, 23, 1), (76, 39, 1),
-- 訂單 77 (2026-01-23)
(77, 5, 2), (77, 18, 1), (77, 24, 1),
-- 訂單 78 (2026-01-26)
(78, 6, 1), (78, 12, 2), (78, 25, 1);

-- =================================================================
-- 優惠券使用記錄
-- =================================================================
INSERT INTO order_coupons (oid, coupon_id, discount_amount) VALUES
-- 10月訂單優惠券
(30, 1, 15.60),  -- 10% off
(32, 2, 26.00),  -- Free drink (item 15)
(34, 3, 50.00),  -- HK$50 off
(36, 1, 12.80),  -- 10% off
(38, 2, 28.00),  -- Free drink (item 14)
(40, 3, 50.00),  -- HK$50 off
(42, 1, 18.40),  -- 10% off

-- 11月訂單優惠券
(44, 2, 28.00),  -- Free drink (item 39)
(46, 3, 50.00),  -- HK$50 off
(48, 1, 14.20),  -- 10% off
(50, 2, 26.00),  -- Free drink (item 14)
(52, 3, 50.00),  -- HK$50 off
(54, 1, 16.80),  -- 10% off

-- 12月訂單優惠券
(56, 2, 30.00),  -- Free drink (item 41)
(58, 3, 50.00),  -- HK$50 off
(60, 1, 13.40),  -- 10% off
(62, 2, 28.00),  -- Free drink (item 14)
(64, 3, 50.00),  -- HK$50 off
(66, 1, 15.20),  -- 10% off

-- 1月訂單優惠券
(68, 2, 25.00),  -- Free drink (item 31)
(70, 3, 50.00),  -- HK$50 off
(72, 1, 17.60),  -- 10% off
(74, 2, 28.00),  -- Free drink (item 14)
(76, 3, 50.00),  -- HK$50 off
(78, 1, 14.80);  -- 10% off

-- =================================================================
-- 更新顧客積分
-- =================================================================
UPDATE customer SET coupon_point = coupon_point + 150 WHERE cid = 1;
UPDATE customer SET coupon_point = coupon_point + 180 WHERE cid = 2;
UPDATE customer SET coupon_point = coupon_point + 120 WHERE cid = 3;
UPDATE customer SET coupon_point = coupon_point + 200 WHERE cid = 4;
UPDATE customer SET coupon_point = coupon_point + 160 WHERE cid = 5;

-- 插入積分歷史記錄（使用子查詢獲取更新後的積分）
INSERT INTO coupon_point_history (cid, delta, resulting_points, action, note) VALUES
(1, 150, (SELECT coupon_point FROM customer WHERE cid = 1), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(2, 180, (SELECT coupon_point FROM customer WHERE cid = 2), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(3, 120, (SELECT coupon_point FROM customer WHERE cid = 3), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(4, 200, (SELECT coupon_point FROM customer WHERE cid = 4), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(5, 160, (SELECT coupon_point FROM customer WHERE cid = 5), 'earn', 'Points from Oct 2025 - Jan 2026 orders');

-- =================================================================
-- 非會員銷售數據 (Walk-in Customer)
-- 50筆訂單，時間範圍：2025年10月1日 - 2026年1月31日
-- 使用cid=0 (Walk-in Customer)
-- oid 範圍：79～128
-- =================================================================


-- =================================================================
-- 插入50筆非會員訂單
-- =================================================================

-- 2025年10月非會員訂單 (13筆)  oid 79-91
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(79, '2025-10-01 121500', 0, 3, 'walkin_20251001_001', NULL, 'dine_in', 3),
(80, '2025-10-02 133000', 0, 3, 'walkin_20251002_002', NULL, 'takeaway', NULL),
(81, '2025-10-04 184500', 0, 3, 'walkin_20251004_003', NULL, 'dine_in', 7),
(82, '2025-10-06 192000', 0, 3, 'walkin_20251006_004', NULL, 'dine_in', 11),
(83, '2025-10-08 124500', 0, 3, 'walkin_20251008_005', NULL, 'takeaway', NULL),
(84, '2025-10-10 131500', 0, 3, 'walkin_20251010_006', NULL, 'dine_in', 16),
(85, '2025-10-12 183000', 0, 3, 'walkin_20251012_007', NULL, 'dine_in', 22),
(86, '2025-10-14 194500', 0, 3, 'walkin_20251014_008', NULL, 'dine_in', 8),
(87, '2025-10-16 123000', 0, 3, 'walkin_20251016_009', NULL, 'takeaway', NULL),
(88, '2025-10-18 135000', 0, 3, 'walkin_20251018_010', NULL, 'dine_in', 19),
(89, '2025-10-20 181500', 0, 3, 'walkin_20251020_011', NULL, 'dine_in', 5),
(90, '2025-10-22 193000', 0, 3, 'walkin_20251022_012', NULL, 'dine_in', 14),
(91, '2025-10-25 122000', 0, 3, 'walkin_20251025_013', NULL, 'takeaway', NULL);

-- 2025年11月非會員訂單 (13筆)  oid 92-104
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(92, '2025-11-01 134000', 0, 3, 'walkin_20251101_014', NULL, 'dine_in', 9),
(93, '2025-11-03 185000', 0, 3, 'walkin_20251103_015', NULL, 'dine_in', 17),
(94, '2025-11-05 191000', 0, 3, 'walkin_20251105_016', NULL, 'dine_in', 23),
(95, '2025-11-07 123500', 0, 3, 'walkin_20251107_017', NULL, 'takeaway', NULL),
(96, '2025-11-09 135500', 0, 3, 'walkin_20251109_018', NULL, 'dine_in', 6),
(97, '2025-11-11 182500', 0, 3, 'walkin_20251111_019', NULL, 'dine_in', 20),
(98, '2025-11-13 194000', 0, 3, 'walkin_20251113_020', NULL, 'dine_in', 12),
(99, '2025-11-15 124000', 0, 3, 'walkin_20251115_021', NULL, 'takeaway', NULL),
(100, '2025-11-17 132000', 0, 3, 'walkin_20251117_022', NULL, 'dine_in', 25),
(101, '2025-11-19 183500', 0, 3, 'walkin_20251119_023', NULL, 'dine_in', 10),
(102, '2025-11-21 195000', 0, 3, 'walkin_20251121_024', NULL, 'dine_in', 4),
(103, '2025-11-24 125000', 0, 3, 'walkin_20251124_025', NULL, 'takeaway', NULL),
(104, '2025-11-27 133000', 0, 3, 'walkin_20251127_026', NULL, 'dine_in', 15);

-- 2025年12月非會員訂單 (12筆)  oid 105-116
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(105, '2025-12-01 184000', 0, 3, 'walkin_20251201_027', NULL, 'dine_in', 21),
(106, '2025-12-03 191500', 0, 3, 'walkin_20251203_028', NULL, 'dine_in', 13),
(107, '2025-12-05 125500', 0, 3, 'walkin_20251205_029', NULL, 'takeaway', NULL),
(108, '2025-12-07 133500', 0, 3, 'walkin_20251207_030', NULL, 'dine_in', 18),
(109, '2025-12-09 185000', 0, 3, 'walkin_20251209_031', NULL, 'dine_in', 24),
(110, '2025-12-11 192000', 0, 3, 'walkin_20251211_032', NULL, 'dine_in', 2),
(111, '2025-12-13 130000', 0, 3, 'walkin_20251213_033', NULL, 'takeaway', NULL),
(112, '2025-12-15 142000', 0, 3, 'walkin_20251215_034', NULL, 'dine_in', 7),
(113, '2025-12-17 183000', 0, 3, 'walkin_20251217_035', NULL, 'dine_in', 16),
(114, '2025-12-19 194500', 0, 3, 'walkin_20251219_036', NULL, 'dine_in', 11),
(115, '2025-12-22 131000', 0, 3, 'walkin_20251222_037', NULL, 'takeaway', NULL),
(116, '2025-12-26 143000', 0, 3, 'walkin_20251226_038', NULL, 'dine_in', 5);

-- 2026年1月非會員訂單 (12筆)  oid 117-128
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(117, '2026-01-02 183500', 0, 3, 'walkin_20260102_039', NULL, 'dine_in', 9),
(118, '2026-01-04 191000', 0, 3, 'walkin_20260104_040', NULL, 'dine_in', 14),
(119, '2026-01-06 131500', 0, 3, 'walkin_20260106_041', NULL, 'takeaway', NULL),
(120, '2026-01-08 144000', 0, 3, 'walkin_20260108_042', NULL, 'dine_in', 19),
(121, '2026-01-10 184500', 0, 3, 'walkin_20260110_043', NULL, 'dine_in', 22),
(122, '2026-01-12 192500', 0, 3, 'walkin_20260112_044', NULL, 'dine_in', 8),
(123, '2026-01-14 132500', 0, 3, 'walkin_20260114_045', NULL, 'takeaway', NULL),
(124, '2026-01-16 145000', 0, 3, 'walkin_20260116_046', NULL, 'dine_in', 12),
(125, '2026-01-18 185500', 0, 3, 'walkin_20260118_047', NULL, 'dine_in', 25),
(126, '2026-01-20 193000', 0, 3, 'walkin_20260120_048', NULL, 'dine_in', 6),
(127, '2026-01-23 133500', 0, 3, 'walkin_20260123_049', NULL, 'takeaway', NULL),
(128, '2026-01-27 150000', 0, 3, 'walkin_20260127_050', NULL, 'dine_in', 17);

-- =================================================================
-- 插入非會員訂單項目
-- 註：訂單oid從79開始（已有訂單到78）
-- =================================================================

-- 訂單79-91 (2025年10月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(79, 3, 1), (79, 12, 1), (79, 19, 1),  -- 口水雞 + 咸柠7 + 米飯
(80, 6, 1), (80, 17, 1), (80, 20, 1),  -- 麻婆豆腐 + 凍奶茶 + 麵
(81, 8, 1), (81, 14, 1), (81, 19, 1),  -- 回鍋肉 + 熱奶茶 + 米飯
(82, 5, 1), (82, 13, 1), (82, 19, 1),  -- 重慶牛肉 + 紅豆冰 + 米飯
(83, 7, 1), (83, 18, 1), (83, 21, 1),  -- 擔擔麵 + 凍檸茶 + 薯粉
(84, 9, 1), (84, 15, 1), (84, 19, 1),  -- 水煮牛肉 + 葡萄烏龍茶 + 米飯
(85, 10, 1), (85, 16, 1), (85, 20, 1), -- 魚香茄子 + 熱檸茶 + 麵
(86, 1, 2), (86, 12, 2), (86, 19, 2),  -- 黃瓜花x2 + 咸柠7x2 + 米飯x2
(87, 2, 1), (87, 14, 1), (87, 19, 1),  -- 木耳 + 熱奶茶 + 米飯
(88, 4, 1), (88, 17, 1), (88, 21, 1),  -- 酸菜魚湯 + 凍奶茶 + 薯粉
(89, 23, 1), (89, 28, 1), (89, 34, 1), (89, 39, 1), -- 香脆藕片 + 酸辣湯 + 宮保雞丁 + 冰蜂蜜檸檬
(90, 24, 1), (90, 29, 1), (90, 35, 1), (90, 19, 1), -- 麻辣海蜇 + 冬瓜湯 + 糖醋里脊 + 米飯
(91, 25, 1), (91, 30, 1), (91, 36, 1), (91, 40, 1); -- 蒜香毛豆 + 玉米蟹肉湯 + 四川海鮮乾鍋 + 蜜桃烏龍茶

-- 訂單92-104 (2025年11月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(92, 26, 1), (92, 31, 1), (92, 37, 1), (92, 41, 1), -- 蔬菜春卷 + 芒果布丁 + 紅燒肉 + 椰奶昔
(93, 27, 2), (93, 32, 1), (93, 38, 1), (93, 12, 2), -- 四川辣花生x2 + 芝麻球 + 檸檬雞 + 咸柠7x2
(94, 3, 1), (94, 28, 1), (94, 34, 1), (94, 18, 1), -- 口水雞 + 酸辣湯 + 宮保雞丁 + 凍檸茶
(95, 5, 1), (95, 29, 1), (95, 35, 1), (95, 17, 1), -- 重慶牛肉 + 冬瓜湯 + 糖醋里脊 + 凍奶茶
(96, 6, 1), (96, 30, 1), (96, 36, 1), (96, 16, 1), -- 麻婆豆腐 + 玉米蟹肉湯 + 四川海鮮乾鍋 + 熱檸茶
(97, 8, 1), (97, 23, 1), (97, 37, 1), (97, 14, 1), -- 回鍋肉 + 香脆藕片 + 紅燒肉 + 熱奶茶
(98, 9, 1), (98, 24, 1), (98, 38, 1), (98, 15, 1), -- 水煮牛肉 + 麻辣海蜇 + 檸檬雞 + 葡萄烏龍茶
(99, 10, 1), (99, 25, 1), (99, 34, 1), (99, 13, 1), -- 魚香茄子 + 蒜香毛豆 + 宮保雞丁 + 紅豆冰
(100, 1, 2), (100, 26, 1), (100, 35, 1), (100, 39, 2), -- 黃瓜花x2 + 蔬菜春卷 + 糖醋里脊 + 冰蜂蜜檸檬x2
(101, 2, 1), (101, 27, 1), (101, 36, 1), (101, 40, 1), -- 木耳 + 四川辣花生 + 四川海鮮乾鍋 + 蜜桃烏龍茶
(102, 4, 1), (102, 23, 1), (102, 37, 1), (102, 41, 1), -- 酸菜魚湯 + 香脆藕片 + 紅燒肉 + 椰奶昔
(103, 7, 1), (103, 24, 1), (103, 38, 1), (103, 12, 1), -- 擔擔麵 + 麻辣海蜇 + 檸檬雞 + 咸柠7
(104, 11, 2), (104, 31, 1), (104, 17, 2); -- 糯米糕x2 + 芒果布丁 + 凍奶茶x2

-- 訂單105-116 (2025年12月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(105, 3, 1), (105, 25, 1), (105, 34, 1), (105, 18, 1), -- 口水雞 + 蒜香毛豆 + 宮保雞丁 + 凍檸茶
(106, 5, 1), (106, 26, 1), (106, 35, 1), (106, 14, 1), -- 重慶牛肉 + 蔬菜春卷 + 糖醋里脊 + 熱奶茶
(107, 6, 2), (107, 27, 1), (107, 36, 1), (107, 16, 2), -- 麻婆豆腐x2 + 四川辣花生 + 四川海鮮乾鍋 + 熱檸茶x2
(108, 8, 1), (108, 28, 1), (108, 37, 1), (108, 13, 1), -- 回鍋肉 + 酸辣湯 + 紅燒肉 + 紅豆冰
(109, 9, 1), (109, 29, 1), (109, 38, 1), (109, 15, 1), -- 水煮牛肉 + 冬瓜湯 + 檸檬雞 + 葡萄烏龍茶
(110, 10, 1), (110, 30, 1), (110, 34, 1), (110, 17, 1), -- 魚香茄子 + 玉米蟹肉湯 + 宮保雞丁 + 凍奶茶
(111, 1, 2), (111, 23, 1), (111, 35, 1), (111, 39, 1), -- 黃瓜花x2 + 香脆藕片 + 糖醋里脊 + 冰蜂蜜檸檬
(112, 2, 1), (112, 24, 1), (112, 36, 1), (112, 40, 1), -- 木耳 + 麻辣海蜇 + 四川海鮮乾鍋 + 蜜桃烏龍茶
(113, 4, 1), (113, 31, 1), (113, 37, 1), (113, 12, 1), -- 酸菜魚湯 + 芒果布丁 + 紅燒肉 + 咸柠7
(114, 7, 1), (114, 32, 1), (114, 38, 1), (114, 18, 1), -- 擔擔麵 + 芝麻球 + 檸檬雞 + 凍檸茶
(115, 11, 1), (115, 33, 1), (115, 14, 1), (115, 19, 1), -- 糯米糕 + 蛋挞 + 熱奶茶 + 米飯
(116, 5, 1), (116, 25, 1), (116, 34, 1), (116, 17, 1); -- 重慶牛肉 + 蒜香毛豆 + 宮保雞丁 + 凍奶茶

-- 訂單117-128 (2026年1月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(117, 6, 1), (117, 26, 1), (117, 35, 1), (117, 16, 1), -- 麻婆豆腐 + 蔬菜春卷 + 糖醋里脊 + 熱檸茶
(118, 8, 1), (118, 27, 1), (118, 36, 1), (118, 13, 1), -- 回鍋肉 + 四川辣花生 + 四川海鮮乾鍋 + 紅豆冰
(119, 9, 1), (119, 28, 1), (119, 37, 1), (119, 15, 1), -- 水煮牛肉 + 酸辣湯 + 紅燒肉 + 葡萄烏龍茶
(120, 10, 1), (120, 29, 1), (120, 38, 1), (120, 39, 1), -- 魚香茄子 + 冬瓜湯 + 檸檬雞 + 冰蜂蜜檸檬
(121, 1, 1), (121, 30, 1), (121, 34, 1), (121, 40, 1), -- 黃瓜花 + 玉米蟹肉湯 + 宮保雞丁 + 蜜桃烏龍茶
(122, 2, 1), (122, 31, 1), (122, 35, 1), (122, 41, 1), -- 木耳 + 芒果布丁 + 糖醋里脊 + 椰奶昔
(123, 3, 1), (123, 32, 1), (123, 36, 1), (123, 12, 1), -- 口水雞 + 芝麻球 + 四川海鮮乾鍋 + 咸柠7
(124, 4, 1), (124, 33, 1), (124, 37, 1), (124, 14, 1), -- 酸菜魚湯 + 蛋挞 + 紅燒肉 + 熱奶茶
(125, 23, 2), (125, 28, 1), (125, 38, 1), (125, 17, 2), -- 香脆藕片x2 + 酸辣湯 + 檸檬雞 + 凍奶茶x2
(126, 24, 1), (126, 29, 1), (126, 34, 1), (126, 18, 1), -- 麻辣海蜇 + 冬瓜湯 + 宮保雞丁 + 凍檸茶
(127, 25, 1), (127, 30, 1), (127, 35, 1), (127, 16, 1), -- 蒜香毛豆 + 玉米蟹肉湯 + 糖醋里脊 + 熱檸茶
(128, 26, 1), (128, 31, 1), (128, 36, 1), (128, 39, 1); -- 蔬菜春卷 + 芒果布丁 + 四川海鮮乾鍋 + 冰蜂蜜檸檬

-- =================================================================
-- 非會員銷售數據 (Walk-in Customer) - 第三批
-- 72筆訂單（前50筆+後22筆），時間範圍：2025年9月1日 - 2026年2月28日
-- 使用cid=0 (Walk-in Customer)
-- oid 範圍：129～200
-- =================================================================


-- =================================================================
-- 插入72筆非會員訂單
-- =================================================================

-- 2025年9月非會員訂單 (10筆)  oid 129-138
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(129, '2025-09-03 12:30:00', 0, 3, 'walkin_20250903_051', NULL, 'dine_in', 4),
(130, '2025-09-05 13:45:00', 0, 3, 'walkin_20250905_052', NULL, 'takeaway', NULL),
(131, '2025-09-07 18:20:00', 0, 3, 'walkin_20250907_053', NULL, 'dine_in', 10),
(132, '2025-09-09 19:35:00', 0, 3, 'walkin_20250909_054', NULL, 'dine_in', 15),
(133, '2025-09-11 12:50:00', 0, 3, 'walkin_20250911_055', NULL, 'takeaway', NULL),
(134, '2025-09-13 14:15:00', 0, 3, 'walkin_20250913_056', NULL, 'dine_in', 21),
(135, '2025-09-15 18:40:00', 0, 3, 'walkin_20250915_057', NULL, 'dine_in', 8),
(136, '2025-09-17 19:55:00', 0, 3, 'walkin_20250917_058', NULL, 'dine_in', 12),
(137, '2025-09-20 13:10:00', 0, 3, 'walkin_20250920_059', NULL, 'takeaway', NULL),
(138, '2025-09-23 14:35:00', 0, 3, 'walkin_20250923_060', NULL, 'dine_in', 18);

-- 2025年10月非會員訂單 (10筆)  oid 139-148
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(139, '2025-10-03 18:25:00', 0, 3, 'walkin_20251003_061', NULL, 'dine_in', 6),
(140, '2025-10-06 19:40:00', 0, 3, 'walkin_20251006_062', NULL, 'dine_in', 22),
(141, '2025-10-09 12:55:00', 0, 3, 'walkin_20251009_063', NULL, 'takeaway', NULL),
(142, '2025-10-12 14:20:00', 0, 3, 'walkin_20251012_064', NULL, 'dine_in', 9),
(143, '2025-10-15 18:45:00', 0, 3, 'walkin_20251015_065', NULL, 'dine_in', 25),
(144, '2025-10-18 19:15:00', 0, 3, 'walkin_20251018_066', NULL, 'dine_in', 7),
(145, '2025-10-21 13:30:00', 0, 3, 'walkin_20251021_067', NULL, 'takeaway', NULL),
(146, '2025-10-24 14:55:00', 0, 3, 'walkin_20251024_068', NULL, 'dine_in', 14),
(147, '2025-10-27 18:30:00', 0, 3, 'walkin_20251027_069', NULL, 'dine_in', 19),
(148, '2025-10-30 19:45:00', 0, 3, 'walkin_20251030_070', NULL, 'dine_in', 3);

-- 2025年11月非會員訂單 (10筆)  oid 149-158
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(149, '2025-11-02 13:45:00', 0, 3, 'walkin_20251102_071', NULL, 'takeaway', NULL),
(150, '2025-11-05 15:10:00', 0, 3, 'walkin_20251105_072', NULL, 'dine_in', 16),
(151, '2025-11-08 18:35:00', 0, 3, 'walkin_20251108_073', NULL, 'dine_in', 11),
(152, '2025-11-11 19:20:00', 0, 3, 'walkin_20251111_074', NULL, 'dine_in', 24),
(153, '2025-11-14 14:05:00', 0, 3, 'walkin_20251114_075', NULL, 'takeaway', NULL),
(154, '2025-11-17 15:30:00', 0, 3, 'walkin_20251117_076', NULL, 'dine_in', 5),
(155, '2025-11-20 18:55:00', 0, 3, 'walkin_20251120_077', NULL, 'dine_in', 20),
(156, '2025-11-23 19:30:00', 0, 3, 'walkin_20251123_078', NULL, 'dine_in', 13),
(157, '2025-11-26 14:15:00', 0, 3, 'walkin_20251126_079', NULL, 'takeaway', NULL),
(158, '2025-11-29 15:40:00', 0, 3, 'walkin_20251129_080', NULL, 'dine_in', 17);

-- 2025年12月非會員訂單 (10筆)  oid 159-168
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(159, '2025-12-02 18:15:00', 0, 3, 'walkin_20251202_081', NULL, 'dine_in', 2),
(160, '2025-12-05 19:50:00', 0, 3, 'walkin_20251205_082', NULL, 'dine_in', 23),
(161, '2025-12-08 14:25:00', 0, 3, 'walkin_20251208_083', NULL, 'takeaway', NULL),
(162, '2025-12-11 15:50:00', 0, 3, 'walkin_20251211_084', NULL, 'dine_in', 10),
(163, '2025-12-14 18:25:00', 0, 3, 'walkin_20251214_085', NULL, 'dine_in', 18),
(164, '2025-12-17 19:10:00', 0, 3, 'walkin_20251217_086', NULL, 'dine_in', 6),
(165, '2025-12-20 14:35:00', 0, 3, 'walkin_20251220_087', NULL, 'takeaway', NULL),
(166, '2025-12-23 16:00:00', 0, 3, 'walkin_20251223_088', NULL, 'dine_in', 14),
(167, '2025-12-26 18:35:00', 0, 3, 'walkin_20251226_089', NULL, 'dine_in', 21),
(168, '2025-12-29 19:25:00', 0, 3, 'walkin_20251229_090', NULL, 'dine_in', 8);

-- 2026年1月非會員訂單 (5筆)  oid 169-173
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(169, '2026-01-03 14:45:00', 0, 3, 'walkin_20260103_091', NULL, 'takeaway', NULL),
(170, '2026-01-06 16:10:00', 0, 3, 'walkin_20260106_092', NULL, 'dine_in', 12),
(171, '2026-01-09 18:45:00', 0, 3, 'walkin_20260109_093', NULL, 'dine_in', 25),
(172, '2026-01-12 19:35:00', 0, 3, 'walkin_20260112_094', NULL, 'dine_in', 9),
(173, '2026-01-15 15:00:00', 0, 3, 'walkin_20260115_095', NULL, 'takeaway', NULL);

-- 2026年2月非會員訂單 (5筆)  oid 174-178
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(174, '2026-02-01 16:20:00', 0, 3, 'walkin_20260201_096', NULL, 'dine_in', 16),
(175, '2026-02-04 18:55:00', 0, 3, 'walkin_20260204_097', NULL, 'dine_in', 7),
(176, '2026-02-07 19:45:00', 0, 3, 'walkin_20260207_098', NULL, 'dine_in', 19),
(177, '2026-02-10 15:20:00', 0, 3, 'walkin_20260210_099', NULL, 'takeaway', NULL),
(178, '2026-02-13 16:45:00', 0, 3, 'walkin_20260213_100', NULL, 'dine_in', 22);

-- 再增加22筆較簡單的訂單（179-200）
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(179, '2025-09-26 12:40:00', 0, 3, 'walkin_20250926_101', NULL, 'dine_in', 11),
(180, '2025-09-28 13:55:00', 0, 3, 'walkin_20250928_102', NULL, 'takeaway', NULL),
(181, '2025-10-04 18:15:00', 0, 3, 'walkin_20251004_103', NULL, 'dine_in', 16),
(182, '2025-10-08 19:30:00', 0, 3, 'walkin_20251008_104', NULL, 'dine_in', 24),
(183, '2025-10-12 13:20:00', 0, 3, 'walkin_20251012_105', NULL, 'takeaway', NULL),
(184, '2025-11-07 18:50:00', 0, 3, 'walkin_20251107_106', NULL, 'dine_in', 3),
(185, '2025-11-11 19:15:00', 0, 3, 'walkin_20251111_107', NULL, 'dine_in', 14),
(186, '2025-11-15 14:40:00', 0, 3, 'walkin_20251115_108', NULL, 'takeaway', NULL),
(187, '2025-12-04 18:30:00', 0, 3, 'walkin_20251204_109', NULL, 'dine_in', 5),
(188, '2025-12-08 19:45:00', 0, 3, 'walkin_20251208_110', NULL, 'dine_in', 17),
(189, '2025-12-12 15:10:00', 0, 3, 'walkin_20251212_111', NULL, 'takeaway', NULL),
(190, '2025-12-16 18:40:00', 0, 3, 'walkin_20251216_112', NULL, 'dine_in', 9),
(191, '2025-12-20 19:55:00', 0, 3, 'walkin_20251220_113', NULL, 'dine_in', 21),
(192, '2026-01-05 15:30:00', 0, 3, 'walkin_20260105_114', NULL, 'takeaway', NULL),
(193, '2026-01-10 18:20:00', 0, 3, 'walkin_20260110_115', NULL, 'dine_in', 8),
(194, '2026-01-14 19:10:00', 0, 3, 'walkin_20260114_116', NULL, 'dine_in', 20),
(195, '2026-01-18 16:00:00', 0, 3, 'walkin_20260118_117', NULL, 'takeaway', NULL),
(196, '2026-01-22 18:35:00', 0, 3, 'walkin_20260122_118', NULL, 'dine_in', 13),
(197, '2026-02-05 19:25:00', 0, 3, 'walkin_20260205_119', NULL, 'dine_in', 18),
(198, '2026-02-09 16:30:00', 0, 3, 'walkin_20260209_120', NULL, 'takeaway', NULL),
(199, '2026-02-14 18:50:00', 0, 3, 'walkin_20260214_121', NULL, 'dine_in', 10),
(200, '2026-02-18 19:40:00', 0, 3, 'walkin_20260218_122', NULL, 'dine_in', 23);

-- =================================================================
-- 插入非會員訂單項目
-- 註：訂單oid從129開始（已有訂單到128）
-- =================================================================

-- 訂單129-138 (2025年9月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(129, 1, 1), (129, 19, 1), (129, 12, 1),  -- 黃瓜花 + 米飯 + 咸柠7
(130, 2, 1), (130, 20, 1), (130, 17, 1),  -- 木耳 + 麵 + 凍奶茶
(131, 3, 1), (131, 19, 1), (131, 14, 1),  -- 口水雞 + 米飯 + 熱奶茶
(132, 4, 1), (132, 21, 1), (132, 18, 1),  -- 酸菜魚湯 + 薯粉 + 凍檸茶
(133, 5, 1), (133, 19, 1), (133, 13, 1),  -- 重慶牛肉 + 米飯 + 紅豆冰
(134, 6, 2), (134, 20, 2), (134, 16, 2),  -- 麻婆豆腐x2 + 麵x2 + 熱檸茶x2
(135, 7, 1), (135, 19, 1), (135, 15, 1),  -- 擔擔麵 + 米飯 + 葡萄烏龍茶
(136, 8, 1), (136, 19, 1), (136, 17, 1),  -- 回鍋肉 + 米飯 + 凍奶茶
(137, 9, 1), (137, 21, 1), (137, 12, 1),  -- 水煮牛肉 + 薯粉 + 咸柠7
(138, 10, 1), (138, 19, 1), (138, 14, 1); -- 魚香茄子 + 米飯 + 熱奶茶

-- 訂單139-148 (2025年10月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(139, 23, 1), (139, 28, 1), (139, 34, 1), (139, 39, 1),  -- 香脆藕片 + 酸辣湯 + 宮保雞丁 + 冰蜂蜜檸檬
(140, 24, 1), (140, 29, 1), (140, 35, 1), (140, 40, 1),  -- 麻辣海蜇 + 冬瓜湯 + 糖醋里脊 + 蜜桃烏龍茶
(141, 25, 1), (141, 30, 1), (141, 36, 1), (141, 41, 1),  -- 蒜香毛豆 + 玉米蟹肉湯 + 四川海鮮乾鍋 + 椰奶昔
(142, 26, 1), (142, 28, 1), (142, 37, 1), (142, 12, 1),  -- 蔬菜春卷 + 酸辣湯 + 紅燒肉 + 咸柠7
(143, 27, 2), (143, 29, 1), (143, 38, 1), (143, 17, 1),  -- 四川辣花生x2 + 冬瓜湯 + 檸檬雞 + 凍奶茶
(144, 23, 1), (144, 30, 1), (144, 34, 1), (144, 18, 1),  -- 香脆藕片 + 玉米蟹肉湯 + 宮保雞丁 + 凍檸茶
(145, 24, 1), (145, 28, 1), (145, 35, 1), (145, 13, 1),  -- 麻辣海蜇 + 酸辣湯 + 糖醋里脊 + 紅豆冰
(146, 25, 1), (146, 29, 1), (146, 36, 1), (146, 14, 1),  -- 蒜香毛豆 + 冬瓜湯 + 四川海鮮乾鍋 + 熱奶茶
(147, 26, 1), (147, 30, 1), (147, 37, 1), (147, 15, 1),  -- 蔬菜春卷 + 玉米蟹肉湯 + 紅燒肉 + 葡萄烏龍茶
(148, 27, 1), (148, 28, 1), (148, 38, 1), (148, 16, 1);  -- 四川辣花生 + 酸辣湯 + 檸檬雞 + 熱檸茶

-- 訂單149-158 (2025年11月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(149, 31, 1), (149, 34, 1), (149, 39, 1), (149, 19, 1),  -- 芒果布丁 + 宮保雞丁 + 冰蜂蜜檸檬 + 米飯
(150, 32, 1), (150, 35, 1), (150, 40, 1), (150, 19, 1),  -- 芝麻球 + 糖醋里脊 + 蜜桃烏龍茶 + 米飯
(151, 33, 1), (151, 36, 1), (151, 41, 1), (151, 20, 1),  -- 蛋挞 + 四川海鮮乾鍋 + 椰奶昔 + 麵
(152, 31, 2), (152, 37, 1), (152, 12, 2), (152, 19, 1),  -- 芒果布丁x2 + 紅燒肉 + 咸柠7x2 + 米飯
(153, 32, 1), (153, 38, 1), (153, 17, 1), (153, 20, 1),  -- 芝麻球 + 檸檬雞 + 凍奶茶 + 麵
(154, 33, 1), (154, 34, 1), (154, 18, 1), (154, 19, 1),  -- 蛋挞 + 宮保雞丁 + 凍檸茶 + 米飯
(155, 31, 1), (155, 35, 1), (155, 13, 1), (155, 19, 1),  -- 芒果布丁 + 糖醋里脊 + 紅豆冰 + 米飯
(156, 32, 1), (156, 36, 1), (156, 14, 1), (156, 20, 1),  -- 芝麻球 + 四川海鮮乾鍋 + 熱奶茶 + 麵
(157, 33, 1), (157, 37, 1), (157, 15, 1), (157, 19, 1),  -- 蛋挞 + 紅燒肉 + 葡萄烏龍茶 + 米飯
(158, 31, 1), (158, 38, 1), (158, 16, 1), (158, 20, 1);  -- 芒果布丁 + 檸檬雞 + 熱檸茶 + 麵

-- 訂單159-168 (2025年12月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(159, 1, 1), (159, 3, 1), (159, 12, 2), (159, 19, 2),  -- 黃瓜花 + 口水雞 + 咸柠7x2 + 米飯x2
(160, 2, 1), (160, 5, 1), (160, 17, 1), (160, 19, 1),  -- 木耳 + 重慶牛肉 + 凍奶茶 + 米飯
(161, 4, 1), (161, 6, 1), (161, 18, 1), (161, 21, 1),  -- 酸菜魚湯 + 麻婆豆腐 + 凍檸茶 + 薯粉
(162, 7, 1), (162, 8, 1), (162, 13, 1), (162, 20, 1),  -- 擔擔麵 + 回鍋肉 + 紅豆冰 + 麵
(163, 9, 1), (163, 10, 1), (163, 14, 2), (163, 19, 2),  -- 水煮牛肉 + 魚香茄子 + 熱奶茶x2 + 米飯x2
(164, 23, 1), (164, 24, 1), (164, 15, 1), (164, 16, 1),  -- 香脆藕片 + 麻辣海蜇 + 葡萄烏龍茶 + 熱檸茶
(165, 25, 1), (165, 26, 1), (165, 17, 1), (165, 18, 1),  -- 蒜香毛豆 + 蔬菜春卷 + 凍奶茶 + 凍檸茶
(166, 27, 2), (166, 28, 1), (166, 12, 1), (166, 13, 1),  -- 四川辣花生x2 + 酸辣湯 + 咸柠7 + 紅豆冰
(167, 29, 1), (167, 30, 1), (167, 14, 1), (167, 15, 1),  -- 冬瓜湯 + 玉米蟹肉湯 + 熱奶茶 + 葡萄烏龍茶
(168, 31, 1), (168, 32, 1), (168, 16, 1), (168, 17, 1);  -- 芒果布丁 + 芝麻球 + 熱檸茶 + 凍奶茶

-- 訂單169-173 (2026年1月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(169, 33, 1), (169, 34, 1), (169, 18, 1), (169, 19, 1),  -- 蛋挞 + 宮保雞丁 + 凍檸茶 + 米飯
(170, 35, 1), (170, 36, 1), (170, 39, 1), (170, 20, 1),  -- 糖醋里脊 + 四川海鮮乾鍋 + 冰蜂蜜檸檬 + 麵
(171, 37, 1), (171, 38, 1), (171, 40, 1), (171, 19, 1),  -- 紅燒肉 + 檸檬雞 + 蜜桃烏龍茶 + 米飯
(172, 34, 1), (172, 35, 1), (172, 41, 1), (172, 20, 1),  -- 宮保雞丁 + 糖醋里脊 + 椰奶昔 + 麵
(173, 36, 1), (173, 37, 1), (173, 12, 1), (173, 13, 1);  -- 四川海鮮乾鍋 + 紅燒肉 + 咸柠7 + 紅豆冰

-- 訂單174-178 (2026年2月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(174, 38, 1), (174, 23, 1), (174, 14, 1), (174, 15, 1),  -- 檸檬雞 + 香脆藕片 + 熱奶茶 + 葡萄烏龍茶
(175, 24, 1), (175, 25, 1), (175, 16, 1), (175, 17, 1),  -- 麻辣海蜇 + 蒜香毛豆 + 熱檸茶 + 凍奶茶
(176, 26, 1), (176, 27, 1), (176, 18, 1), (176, 19, 1),  -- 蔬菜春卷 + 四川辣花生 + 凍檸茶 + 米飯
(177, 28, 1), (177, 29, 1), (177, 39, 1), (177, 40, 1),  -- 酸辣湯 + 冬瓜湯 + 冰蜂蜜檸檬 + 蜜桃烏龍茶
(178, 30, 1), (178, 31, 1), (178, 41, 1), (178, 19, 1);  -- 玉米蟹肉湯 + 芒果布丁 + 椰奶昔 + 米飯

-- 訂單179-200 (後22筆)
INSERT INTO order_items (oid, item_id, qty) VALUES
(179, 1, 1), (179, 12, 1),  -- 黃瓜花 + 咸柠7
(180, 2, 1), (180, 17, 1),  -- 木耳 + 凍奶茶
(181, 3, 1), (181, 14, 1),  -- 口水雞 + 熱奶茶
(182, 4, 1), (182, 18, 1),  -- 酸菜魚湯 + 凍檸茶
(183, 5, 1), (183, 13, 1),  -- 重慶牛肉 + 紅豆冰
(184, 6, 1), (184, 16, 1),  -- 麻婆豆腐 + 熱檸茶
(185, 7, 1), (185, 15, 1),  -- 擔擔麵 + 葡萄烏龍茶
(186, 8, 1), (186, 17, 1),  -- 回鍋肉 + 凍奶茶
(187, 9, 1), (187, 12, 1),  -- 水煮牛肉 + 咸柠7
(188, 10, 1), (188, 14, 1), -- 魚香茄子 + 熱奶茶
(189, 23, 1), (189, 34, 1), (189, 39, 1),  -- 香脆藕片 + 宮保雞丁 + 冰蜂蜜檸檬
(190, 24, 1), (190, 35, 1), (190, 40, 1),  -- 麻辣海蜇 + 糖醋里脊 + 蜜桃烏龍茶
(191, 25, 1), (191, 36, 1), (191, 41, 1),  -- 蒜香毛豆 + 四川海鮮乾鍋 + 椰奶昔
(192, 26, 1), (192, 37, 1), (192, 12, 1),  -- 蔬菜春卷 + 紅燒肉 + 咸柠7
(193, 27, 1), (193, 38, 1), (193, 17, 1),  -- 四川辣花生 + 檸檬雞 + 凍奶茶
(194, 31, 1), (194, 34, 1), (194, 39, 1),  -- 芒果布丁 + 宮保雞丁 + 冰蜂蜜檸檬
(195, 32, 1), (195, 35, 1), (195, 40, 1),  -- 芝麻球 + 糖醋里脊 + 蜜桃烏龍茶
(196, 33, 1), (196, 36, 1), (196, 41, 1),  -- 蛋挞 + 四川海鮮乾鍋 + 椰奶昔
(197, 23, 1), (197, 28, 1), (197, 17, 1),  -- 香脆藕片 + 酸辣湯 + 凍奶茶
(198, 24, 1), (198, 29, 1), (198, 18, 1),  -- 麻辣海蜇 + 冬瓜湯 + 凍檸茶
(199, 25, 1), (199, 30, 1), (199, 13, 1),  -- 蒜香毛豆 + 玉米蟹肉湯 + 紅豆冰
(200, 26, 1), (200, 28, 1), (200, 14, 1);  -- 蔬菜春卷 + 酸辣湯 + 熱奶茶

-- =================================================================
-- 銷售數據4.txt - 為新增7名會員增加50份訂單
-- 時間範圍：2025年10月 - 2026年2月
-- 使用cid=6-12 (新會員)
-- oid 範圍：201～250
-- =================================================================


-- =================================================================
-- 插入50筆會員訂單
-- =================================================================

-- 2025年10月會員訂單 (10筆)  oid 201-210
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(201, '2025-10-05 12:20:00', 6, 3, 'member_20251005_001', 1, 'dine_in', 3),
(202, '2025-10-07 13:45:00', 7, 3, 'member_20251007_002', NULL, 'takeaway', NULL),
(203, '2025-10-09 18:30:00', 8, 3, 'member_20251009_003', 2, 'dine_in', 8),
(204, '2025-10-11 19:15:00', 9, 3, 'member_20251011_004', NULL, 'dine_in', 12),
(205, '2025-10-13 12:40:00', 10, 3, 'member_20251013_005', 3, 'takeaway', NULL),
(206, '2025-10-15 14:00:00', 11, 3, 'member_20251015_006', NULL, 'dine_in', 16),
(207, '2025-10-17 18:45:00', 12, 3, 'member_20251017_007', 1, 'dine_in', 5),
(208, '2025-10-19 19:30:00', 6, 3, 'member_20251019_008', NULL, 'dine_in', 20),
(209, '2025-10-21 13:10:00', 7, 3, 'member_20251021_009', 2, 'takeaway', NULL),
(210, '2025-10-23 14:30:00', 8, 3, 'member_20251023_010', NULL, 'dine_in', 9);

-- 2025年11月會員訂單 (10筆)  oid 211-220
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(211, '2025-11-02 18:20:00', 9, 3, 'member_20251102_011', 1, 'dine_in', 14),
(212, '2025-11-04 19:40:00', 10, 3, 'member_20251104_012', NULL, 'dine_in', 22),
(213, '2025-11-06 12:50:00', 11, 3, 'member_20251106_013', 3, 'takeaway', NULL),
(214, '2025-11-08 14:15:00', 12, 3, 'member_20251108_014', NULL, 'dine_in', 7),
(215, '2025-11-10 18:35:00', 6, 3, 'member_20251110_015', 2, 'dine_in', 18),
(216, '2025-11-12 19:20:00', 7, 3, 'member_20251112_016', NULL, 'dine_in', 11),
(217, '2025-11-14 13:30:00', 8, 3, 'member_20251114_017', 1, 'takeaway', NULL),
(218, '2025-11-16 15:00:00', 9, 3, 'member_20251116_018', NULL, 'dine_in', 25),
(219, '2025-11-18 18:50:00', 10, 3, 'member_20251118_019', 3, 'dine_in', 6),
(220, '2025-11-20 19:35:00', 11, 3, 'member_20251120_020', NULL, 'dine_in', 15);

-- 2025年12月會員訂單 (10筆)  oid 221-230
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(221, '2025-12-03 13:40:00', 12, 3, 'member_20251203_021', 1, 'takeaway', NULL),
(222, '2025-12-05 15:10:00', 6, 3, 'member_20251205_022', NULL, 'dine_in', 10),
(223, '2025-12-07 18:25:00', 7, 3, 'member_20251207_023', 2, 'dine_in', 21),
(224, '2025-12-09 19:10:00', 8, 3, 'member_20251209_024', NULL, 'dine_in', 4),
(225, '2025-12-11 14:20:00', 9, 3, 'member_20251211_025', 3, 'takeaway', NULL),
(226, '2025-12-13 15:45:00', 10, 3, 'member_20251213_026', NULL, 'dine_in', 17),
(227, '2025-12-15 18:40:00', 11, 3, 'member_20251215_027', 1, 'dine_in', 13),
(228, '2025-12-17 19:25:00', 12, 3, 'member_20251217_028', NULL, 'dine_in', 8),
(229, '2025-12-19 14:35:00', 6, 3, 'member_20251219_029', 2, 'takeaway', NULL),
(230, '2025-12-21 16:00:00', 7, 3, 'member_20251221_030', NULL, 'dine_in', 23);

-- 2026年1月會員訂單 (10筆)  oid 231-240
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(231, '2026-01-04 18:30:00', 8, 3, 'member_20260104_031', 1, 'dine_in', 5),
(232, '2026-01-06 19:15:00', 9, 3, 'member_20260106_032', NULL, 'dine_in', 12),
(233, '2026-01-08 14:45:00', 10, 3, 'member_20260108_033', 3, 'takeaway', NULL),
(234, '2026-01-10 16:10:00', 11, 3, 'member_20260110_034', NULL, 'dine_in', 19),
(235, '2026-01-12 18:50:00', 12, 3, 'member_20260112_035', 2, 'dine_in', 7),
(236, '2026-01-14 19:40:00', 6, 3, 'member_20260114_036', NULL, 'dine_in', 14),
(237, '2026-01-16 15:20:00', 7, 3, 'member_20260116_037', 1, 'takeaway', NULL),
(238, '2026-01-18 16:45:00', 8, 3, 'member_20260118_038', NULL, 'dine_in', 22),
(239, '2026-01-20 18:55:00', 9, 3, 'member_20260120_039', 3, 'dine_in', 9),
(240, '2026-01-22 19:30:00', 10, 3, 'member_20260122_040', NULL, 'dine_in', 16);

-- 2026年2月會員訂單 (10筆)  oid 241-250
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(241, '2026-02-03 15:30:00', 11, 3, 'member_20260203_041', 1, 'takeaway', NULL),
(242, '2026-02-05 17:00:00', 12, 3, 'member_20260205_042', NULL, 'dine_in', 11),
(243, '2026-02-07 18:20:00', 6, 3, 'member_20260207_043', 2, 'dine_in', 25),
(244, '2026-02-09 19:10:00', 7, 3, 'member_20260209_044', NULL, 'dine_in', 3),
(245, '2026-02-11 15:40:00', 8, 3, 'member_20260211_045', 3, 'takeaway', NULL),
(246, '2026-02-13 17:10:00', 9, 3, 'member_20260213_046', NULL, 'dine_in', 18),
(247, '2026-02-15 18:35:00', 10, 3, 'member_20260215_047', 1, 'dine_in', 6),
(248, '2026-02-17 19:25:00', 11, 3, 'member_20260217_048', NULL, 'dine_in', 20),
(249, '2026-02-19 16:00:00', 12, 3, 'member_20260219_049', 2, 'takeaway', NULL),
(250, '2026-02-21 17:30:00', 6, 3, 'member_20260221_050', NULL, 'dine_in', 13);

-- =================================================================
-- 插入會員訂單項目
-- 註：訂單oid從201開始（已有訂單到200）
-- =================================================================

-- 訂單201-210 (2025年10月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(201, 3, 1), (201, 12, 2), (201, 19, 2),  -- 口水雞 + 咸柠7x2 + 米飯x2
(202, 5, 1), (202, 17, 1), (202, 20, 1),  -- 重慶牛肉 + 凍奶茶 + 麵
(203, 6, 1), (203, 14, 2), (203, 19, 2),  -- 麻婆豆腐 + 熱奶茶x2 + 米飯x2
(204, 8, 1), (204, 15, 1), (204, 19, 1),  -- 回鍋肉 + 葡萄烏龍茶 + 米飯
(205, 9, 2), (205, 18, 2), (205, 21, 2),  -- 水煮牛肉x2 + 凍檸茶x2 + 薯粉x2
(206, 10, 1), (206, 13, 1), (206, 19, 1),  -- 魚香茄子 + 紅豆冰 + 米飯
(207, 1, 2), (207, 16, 2), (207, 19, 2),  -- 黃瓜花x2 + 熱檸茶x2 + 米飯x2
(208, 2, 1), (208, 12, 1), (208, 20, 1),  -- 木耳 + 咸柠7 + 麵
(209, 4, 1), (209, 17, 1), (209, 19, 1),  -- 酸菜魚湯 + 凍奶茶 + 米飯
(210, 7, 1), (210, 14, 1), (210, 20, 1);  -- 擔擔麵 + 熱奶茶 + 麵

-- 訂單211-220 (2025年11月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(211, 23, 1), (211, 28, 1), (211, 34, 1), (211, 39, 1),  -- 香脆藕片 + 酸辣湯 + 宮保雞丁 + 冰蜂蜜檸檬
(212, 24, 1), (212, 29, 1), (212, 35, 1), (212, 40, 1),  -- 麻辣海蜇 + 冬瓜湯 + 糖醋里脊 + 蜜桃烏龍茶
(213, 25, 1), (213, 30, 1), (213, 36, 1), (213, 41, 1),  -- 蒜香毛豆 + 玉米蟹肉湯 + 四川海鮮乾鍋 + 椰奶昔
(214, 26, 1), (214, 31, 1), (214, 37, 1), (214, 12, 1),  -- 蔬菜春卷 + 芒果布丁 + 紅燒肉 + 咸柠7
(215, 27, 1), (215, 32, 1), (215, 38, 1), (215, 17, 1),  -- 四川辣花生 + 芝麻球 + 檸檬雞 + 凍奶茶
(216, 23, 2), (216, 28, 1), (216, 34, 2), (216, 39, 2),  -- 香脆藕片x2 + 酸辣湯 + 宮保雞丁x2 + 冰蜂蜜檸檬x2
(217, 24, 1), (217, 29, 1), (217, 35, 1), (217, 40, 1),  -- 麻辣海蜇 + 冬瓜湯 + 糖醋里脊 + 蜜桃烏龍茶
(218, 25, 1), (218, 30, 1), (218, 36, 1), (218, 41, 1),  -- 蒜香毛豆 + 玉米蟹肉湯 + 四川海鮮乾鍋 + 椰奶昔
(219, 26, 2), (219, 31, 1), (219, 37, 2), (219, 12, 2),  -- 蔬菜春卷x2 + 芒果布丁 + 紅燒肉x2 + 咸柠7x2
(220, 27, 1), (220, 32, 1), (220, 38, 1), (220, 17, 1);  -- 四川辣花生 + 芝麻球 + 檸檬雞 + 凍奶茶

-- 訂單221-230 (2025年12月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(221, 3, 1), (221, 25, 1), (221, 34, 1), (221, 18, 1),  -- 口水雞 + 蒜香毛豆 + 宮保雞丁 + 凍檸茶
(222, 5, 1), (222, 26, 1), (222, 35, 1), (222, 14, 1),  -- 重慶牛肉 + 蔬菜春卷 + 糖醋里脊 + 熱奶茶
(223, 6, 2), (223, 27, 1), (223, 36, 1), (223, 16, 2),  -- 麻婆豆腐x2 + 四川辣花生 + 四川海鮮乾鍋 + 熱檸茶x2
(224, 8, 1), (224, 28, 1), (224, 37, 1), (224, 13, 1),  -- 回鍋肉 + 酸辣湯 + 紅燒肉 + 紅豆冰
(225, 9, 1), (225, 29, 1), (225, 38, 1), (225, 15, 1),  -- 水煮牛肉 + 冬瓜湯 + 檸檬雞 + 葡萄烏龍茶
(226, 10, 1), (226, 30, 1), (226, 34, 1), (226, 17, 1),  -- 魚香茄子 + 玉米蟹肉湯 + 宮保雞丁 + 凍奶茶
(227, 1, 2), (227, 23, 1), (227, 35, 1), (227, 39, 1),  -- 黃瓜花x2 + 香脆藕片 + 糖醋里脊 + 冰蜂蜜檸檬
(228, 2, 1), (228, 24, 1), (228, 36, 1), (228, 40, 1),  -- 木耳 + 麻辣海蜇 + 四川海鮮乾鍋 + 蜜桃烏龍茶
(229, 4, 1), (229, 31, 1), (229, 37, 1), (229, 12, 1),  -- 酸菜魚湯 + 芒果布丁 + 紅燒肉 + 咸柠7
(230, 7, 1), (230, 32, 1), (230, 38, 1), (230, 18, 1);  -- 擔擔麵 + 芝麻球 + 檸檬雞 + 凍檸茶

-- 訂單231-240 (2026年1月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(231, 11, 1), (231, 33, 1), (231, 14, 1), (231, 19, 1),  -- 糯米糕 + 蛋挞 + 熱奶茶 + 米飯
(232, 5, 1), (232, 25, 1), (232, 34, 1), (232, 17, 1),  -- 重慶牛肉 + 蒜香毛豆 + 宮保雞丁 + 凍奶茶
(233, 6, 1), (233, 26, 1), (233, 35, 1), (233, 16, 1),  -- 麻婆豆腐 + 蔬菜春卷 + 糖醋里脊 + 熱檸茶
(234, 8, 1), (234, 27, 1), (234, 36, 1), (234, 13, 1),  -- 回鍋肉 + 四川辣花生 + 四川海鮮乾鍋 + 紅豆冰
(235, 9, 1), (235, 28, 1), (235, 37, 1), (235, 15, 1),  -- 水煮牛肉 + 酸辣湯 + 紅燒肉 + 葡萄烏龍茶
(236, 10, 1), (236, 29, 1), (236, 38, 1), (236, 39, 1),  -- 魚香茄子 + 冬瓜湯 + 檸檬雞 + 冰蜂蜜檸檬
(237, 1, 1), (237, 30, 1), (237, 34, 1), (237, 40, 1),  -- 黃瓜花 + 玉米蟹肉湯 + 宮保雞丁 + 蜜桃烏龍茶
(238, 2, 1), (238, 31, 1), (238, 35, 1), (238, 41, 1),  -- 木耳 + 芒果布丁 + 糖醋里脊 + 椰奶昔
(239, 3, 1), (239, 32, 1), (239, 36, 1), (239, 12, 1),  -- 口水雞 + 芝麻球 + 四川海鮮乾鍋 + 咸柠7
(240, 4, 1), (240, 33, 1), (240, 37, 1), (240, 14, 1);  -- 酸菜魚湯 + 蛋挞 + 紅燒肉 + 熱奶茶

-- 訂單241-250 (2026年2月)
INSERT INTO order_items (oid, item_id, qty) VALUES
(241, 23, 2), (241, 28, 1), (241, 38, 1), (241, 17, 2),  -- 香脆藕片x2 + 酸辣湯 + 檸檬雞 + 凍奶茶x2
(242, 24, 1), (242, 29, 1), (242, 34, 1), (242, 18, 1),  -- 麻辣海蜇 + 冬瓜湯 + 宮保雞丁 + 凍檸茶
(243, 25, 1), (243, 30, 1), (243, 35, 1), (243, 16, 1),  -- 蒜香毛豆 + 玉米蟹肉湯 + 糖醋里脊 + 熱檸茶
(244, 26, 1), (244, 31, 1), (244, 36, 1), (244, 39, 1),  -- 蔬菜春卷 + 芒果布丁 + 四川海鮮乾鍋 + 冰蜂蜜檸檬
(245, 27, 1), (245, 32, 1), (245, 37, 1), (245, 40, 1),  -- 四川辣花生 + 芝麻球 + 紅燒肉 + 蜜桃烏龍茶
(246, 23, 1), (246, 33, 1), (246, 38, 1), (246, 41, 1),  -- 香脆藕片 + 蛋挞 + 檸檬雞 + 椰奶昔
(247, 24, 2), (247, 28, 1), (247, 34, 2), (247, 12, 2),  -- 麻辣海蜇x2 + 酸辣湯 + 宮保雞丁x2 + 咸柠7x2
(248, 25, 1), (248, 29, 1), (248, 35, 1), (248, 14, 1),  -- 蒜香毛豆 + 冬瓜湯 + 糖醋里脊 + 熱奶茶
(249, 26, 2), (249, 30, 1), (249, 36, 2), (249, 15, 2),  -- 蔬菜春卷x2 + 玉米蟹肉湯 + 四川海鮮乾鍋x2 + 葡萄烏龍茶x2
(250, 27, 1), (250, 31, 1), (250, 37, 1), (250, 16, 1);  -- 四川辣花生 + 芒果布丁 + 紅燒肉 + 熱檸茶

-- =================================================================
-- 優惠券使用記錄
-- =================================================================

INSERT INTO order_coupons (oid, coupon_id, discount_amount) VALUES
-- 10月訂單優惠券
(201, 1, 18.20),  -- 10% off
(203, 2, 26.00),  -- Free drink (item 14)
(205, 3, 50.00),  -- HK$50 off
(207, 1, 15.80),  -- 10% off
(209, 2, 28.00),  -- Free drink (item 17)

-- 11月訂單優惠券
(211, 3, 50.00),  -- HK$50 off
(213, 1, 20.40),  -- 10% off
(215, 2, 30.00),  -- Free drink (item 41)
(217, 3, 50.00),  -- HK$50 off
(219, 1, 22.60),  -- 10% off

-- 12月訂單優惠券
(221, 2, 26.00),  -- Free drink (item 18)
(223, 3, 50.00),  -- HK$50 off
(225, 1, 19.80),  -- 10% off
(227, 2, 28.00),  -- Free drink (item 39)
(229, 3, 50.00),  -- HK$50 off

-- 1月訂單優惠券
(231, 1, 17.40),  -- 10% off
(233, 2, 24.00),  -- Free drink (item 16)
(235, 3, 50.00),  -- HK$50 off
(237, 1, 21.20),  -- 10% off
(239, 2, 26.00),  -- Free drink (item 12)

-- 2月訂單優惠券
(241, 3, 50.00),  -- HK$50 off
(243, 1, 23.60),  -- 10% off
(245, 2, 30.00),  -- Free drink (item 40)
(247, 3, 50.00),  -- HK$50 off
(249, 1, 25.80);  -- 10% off

-- =================================================================
-- 更新會員積分
-- =================================================================

-- 計算每位會員的總消費並分配積分（每消費HK$1 = 1積分）
-- 我們先更新顧客表中的coupon_point字段

-- 陳大明 (cid=6) - 預計約消費HK$1200
UPDATE customer SET coupon_point = coupon_point + 1200 WHERE cid = 6;

-- 李美玲 (cid=7) - 預計約消費HK$1050
UPDATE customer SET coupon_point = coupon_point + 1050 WHERE cid = 7;

-- 張偉強 (cid=8) - 預計約消費HK$1350
UPDATE customer SET coupon_point = coupon_point + 1350 WHERE cid = 8;

-- 王曉雯 (cid=9) - 預計約消費HK$1150
UPDATE customer SET coupon_point = coupon_point + 1150 WHERE cid = 9;

-- 劉家輝 (cid=10) - 預計約消費HK$1250
UPDATE customer SET coupon_point = coupon_point + 1250 WHERE cid = 10;

-- 黃志偉 (cid=11) - 預計約消費HK$1100
UPDATE customer SET coupon_point = coupon_point + 1100 WHERE cid = 11;

-- 林秀文 (cid=12) - 預計約消費HK$1300
UPDATE customer SET coupon_point = coupon_point + 1300 WHERE cid = 12;

-- =================================================================
-- 插入積分歷史記錄
-- =================================================================

INSERT INTO coupon_point_history (cid, coupon_id, delta, resulting_points, action, note) VALUES
(6, NULL, 1200, 
 (SELECT coupon_point FROM customer WHERE cid = 6), 
 'earn', 
 'Points from Oct 2025 - Feb 2026 orders'),
(7, NULL, 1050, 
 (SELECT coupon_point FROM customer WHERE cid = 7), 
 'earn', 
 'Points from Oct 2025 - Feb 2026 orders'),
(8, NULL, 1350, 
 (SELECT coupon_point FROM customer WHERE cid = 8), 
 'earn', 
 'Points from Oct 2025 - Feb 2026 orders'),
(9, NULL, 1150, 
 (SELECT coupon_point FROM customer WHERE cid = 9), 
 'earn', 
 'Points from Oct 2025 - Feb 2026 orders'),
(10, NULL, 1250, 
 (SELECT coupon_point FROM customer WHERE cid = 10), 
 'earn', 
 'Points from Oct 2025 - Feb 2026 orders'),
(11, NULL, 1100, 
 (SELECT coupon_point FROM customer WHERE cid = 11), 
 'earn', 
 'Points from Oct 2025 - Feb 2026 orders'),
(12, NULL, 1300, 
 (SELECT coupon_point FROM customer WHERE cid = 12), 
 'earn', 
 'Points from Oct 2025 - Feb 2026 orders');


-- -----------------------------------------------------------------
-- 1.  Tag translations
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tag_translation (
  tag_id         INT          NOT NULL,
  language_code  VARCHAR(10)  NOT NULL,
  tag_name       VARCHAR(255) NOT NULL,
  PRIMARY KEY (tag_id, language_code),
  CONSTRAINT fk_tag_trans_tag FOREIGN KEY (tag_id) REFERENCES tag(tag_id) ON DELETE CASCADE
);

-- Traditional Chinese (zh-TW)
INSERT INTO tag_translation (tag_id, language_code, tag_name) VALUES
(1,  'zh-TW', '素食'),
(2,  'zh-TW', '清爽'),
(3,  'zh-TW', '雞肉'),
(4,  'zh-TW', '凍飲'),
(5,  'zh-TW', '辣'),
(6,  'zh-TW', '魚肉'),
(7,  'zh-TW', '酸'),
(8,  'zh-TW', '牛肉'),
(9,  'zh-TW', '麻'),
(10, 'zh-TW', '豆腐'),
(11, 'zh-TW', '麵條'),
(12, 'zh-TW', '豬肉'),
(13, 'zh-TW', '街頭小吃'),
(14, 'zh-TW', '炒'),
(15, 'zh-TW', '經典'),
(16, 'zh-TW', '甜'),
(17, 'zh-TW', '糯'),
(18, 'zh-TW', '檸檬'),
(19, 'zh-TW', '葡萄'),
(20, 'zh-TW', '奶'),
(21, 'zh-TW', '氣泡水'),
(22, 'zh-TW', '傳統');

-- Simplified Chinese (zh-CN)
INSERT INTO tag_translation (tag_id, language_code, tag_name) VALUES
(1,  'zh-CN', '素食'),
(2,  'zh-CN', '清爽'),
(3,  'zh-CN', '鸡肉'),
(4,  'zh-CN', '冷饮'),
(5,  'zh-CN', '辣'),
(6,  'zh-CN', '鱼肉'),
(7,  'zh-CN', '酸'),
(8,  'zh-CN', '牛肉'),
(9,  'zh-CN', '麻'),
(10, 'zh-CN', '豆腐'),
(11, 'zh-CN', '面条'),
(12, 'zh-CN', '猪肉'),
(13, 'zh-CN', '街头小吃'),
(14, 'zh-CN', '炒'),
(15, 'zh-CN', '经典'),
(16, 'zh-CN', '甜'),
(17, 'zh-CN', '糯'),
(18, 'zh-CN', '柠檬'),
(19, 'zh-CN', '葡萄'),
(20, 'zh-CN', '奶'),
(21, 'zh-CN', '气泡水'),
(22, 'zh-CN', '传统');

-- -----------------------------------------------------------------
-- 2.  Customization option group translations
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customization_option_group_translation (
  group_id       INT          NOT NULL,
  language_code  VARCHAR(10)  NOT NULL,
  group_name     VARCHAR(255) NOT NULL,
  PRIMARY KEY (group_id, language_code),
  CONSTRAINT fk_cog_trans_group FOREIGN KEY (group_id) REFERENCES customization_option_group(group_id) ON DELETE CASCADE
);

-- group_id 1 = Spice Level, 2 = Sugar Level, 3 = Ice Level,
--           4 = Milk Level, 5 = Toppings

-- Traditional Chinese (zh-TW)
INSERT INTO customization_option_group_translation (group_id, language_code, group_name) VALUES
(1, 'zh-TW', '辣度'),
(2, 'zh-TW', '甜度'),
(3, 'zh-TW', '冰量'),
(4, 'zh-TW', '奶量'),
(5, 'zh-TW', '配料');

-- Simplified Chinese (zh-CN)
INSERT INTO customization_option_group_translation (group_id, language_code, group_name) VALUES
(1, 'zh-CN', '辣度'),
(2, 'zh-CN', '甜度'),
(3, 'zh-CN', '冰量'),
(4, 'zh-CN', '奶量'),
(5, 'zh-CN', '配料');

-- -----------------------------------------------------------------
-- 3.  Customization option value translations
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customization_option_value_translation (
  value_id       INT          NOT NULL,
  language_code  VARCHAR(10)  NOT NULL,
  value_name     VARCHAR(255) NOT NULL,
  PRIMARY KEY (value_id, language_code),
  CONSTRAINT fk_cov_trans_value FOREIGN KEY (value_id) REFERENCES customization_option_value(value_id) ON DELETE CASCADE
);

-- value_id assignments (from createProjectDB_5.8.sql):
--  Spice Level (group 1):  1=Mild  2=Medium  3=Hot  4=Numbing
--  Sugar Level (group 2):  5=More Sweet  6=Less Sweet  7=No Sweet
--  Ice Level   (group 3):  8=More Ice    9=Less Ice   10=No Ice
--  Milk Level  (group 4): 11=More Milk  12=Less Milk  13=No Milk
--  Toppings    (group 5): 14=Extra Sesame  15=Peanuts  16=Honey Drizzle  17=Chocolate Chips

-- Traditional Chinese (zh-TW)
INSERT INTO customization_option_value_translation (value_id, language_code, value_name) VALUES
-- Spice Level
(1,  'zh-TW', '微辣'),
(2,  'zh-TW', '中辣'),
(3,  'zh-TW', '辣'),
(4,  'zh-TW', '麻辣'),
-- Sugar Level
(5,  'zh-TW', '多糖'),
(6,  'zh-TW', '少糖'),
(7,  'zh-TW', '無糖'),
-- Ice Level
(8,  'zh-TW', '多冰'),
(9,  'zh-TW', '少冰'),
(10, 'zh-TW', '無冰'),
-- Milk Level
(11, 'zh-TW', '多奶'),
(12, 'zh-TW', '少奶'),
(13, 'zh-TW', '無奶'),
-- Toppings
(14, 'zh-TW', '加芝麻'),
(15, 'zh-TW', '花生'),
(16, 'zh-TW', '蜂蜜淋醬'),
(17, 'zh-TW', '朱古力粒');

-- Simplified Chinese (zh-CN)
INSERT INTO customization_option_value_translation (value_id, language_code, value_name) VALUES
-- Spice Level
(1,  'zh-CN', '微辣'),
(2,  'zh-CN', '中辣'),
(3,  'zh-CN', '辣'),
(4,  'zh-CN', '麻辣'),
-- Sugar Level
(5,  'zh-CN', '多糖'),
(6,  'zh-CN', '少糖'),
(7,  'zh-CN', '无糖'),
-- Ice Level
(8,  'zh-CN', '多冰'),
(9,  'zh-CN', '少冰'),
(10, 'zh-CN', '无冰'),
-- Milk Level
(11, 'zh-CN', '多奶'),
(12, 'zh-CN', '少奶'),
(13, 'zh-CN', '无奶'),
-- Toppings
(14, 'zh-CN', '加芝麻'),
(15, 'zh-CN', '花生'),
(16, 'zh-CN', '蜂蜜淋酱'),
(17, 'zh-CN', '巧克力粒');


-- =================================================================
-- ADD SPICE LEVEL TABLE (NEW)
-- =================================================================
-- This table stores predefined spice levels that can be used as 
-- menu item modifiers or for displaying spice level information

CREATE TABLE IF NOT EXISTS spice_level (
  spice_id INT NOT NULL AUTO_INCREMENT,
  spice_key VARCHAR(50) NOT NULL UNIQUE COMMENT 'Key for API/code reference (e.g., mild, medium, hot, numbing)',
  spice_order INT DEFAULT 0 COMMENT 'Display order in UI',
  PRIMARY KEY (spice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert predefined spice levels
INSERT INTO spice_level (spice_key, spice_order) VALUES
('no_spice', 0),
('mild', 1),
('medium', 2),
('hot', 3),
('numbing', 4);

-- -----------------------------------------------------------------
-- SPICE LEVEL TRANSLATION TABLE
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS spice_level_translation (
  spice_id INT NOT NULL,
  language_code VARCHAR(10) NOT NULL,
  spice_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (spice_id, language_code),
  CONSTRAINT fk_spice_trans_spice FOREIGN KEY (spice_id) REFERENCES spice_level(spice_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Traditional Chinese (zh-TW)
INSERT INTO spice_level_translation (spice_id, language_code, spice_name) VALUES
(1, 'zh-TW', '不辣'),
(2, 'zh-TW', '微辣'),
(3, 'zh-TW', '中辣'),
(4, 'zh-TW', '辣'),
(5, 'zh-TW', '麻辣');

-- Simplified Chinese (zh-CN)
INSERT INTO spice_level_translation (spice_id, language_code, spice_name) VALUES
(1, 'zh-CN', '不辣'),
(2, 'zh-CN', '微辣'),
(3, 'zh-CN', '中辣'),
(4, 'zh-CN', '辣'),
(5, 'zh-CN', '麻辣');

-- English (default)
INSERT INTO spice_level_translation (spice_id, language_code, spice_name) VALUES
(1, 'en', 'No Spice'),
(2, 'en', 'Mild'),
(3, 'en', 'Medium'),
(4, 'en', 'Hot'),
(5, 'en', 'Numbing');

-- =================================================================
-- STATUS NORMALIZATION
-- =================================================================
-- Business rule: card-paid orders should be pending (1), not cancelled (3)
UPDATE orders
SET ostatus = 1
WHERE payment_method = 'card' AND ostatus = 3;

COMMIT;