


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
(1, 'zh-CN', '鍏ㄥ崟涔濇姌', '涓嬫娑堣垂鍙韩鍙椾節鎶樹紭鎯犮€?),
(1, 'zh-TW', '鍏ㄥ柈涔濇姌', '涓嬫娑堣不鍙韩鍙椾節鎶樺劒鎯犮€?),
(2, 'en', 'Free Drink', 'Redeem one free drink of your choice.'),
(2, 'zh-CN', '鍏嶈垂楗搧', '鍏戞崲涓€鏉偍閫夋嫨鐨勫厤璐归ギ鍝併€?),
(2, 'zh-TW', '鍏嶈不椋插搧', '鍏屾彌涓€鏉偍閬告搰鐨勫厤璨婚２鍝併€?),
(3, 'en', 'HK$50 OFF', 'Enjoy HK$50 off when you spend HK$300 or more.'),
(3, 'zh-CN', '绔嬪噺50娓厓', '娑堣垂婊?00娓厓鍗冲彲鍑?0娓厓銆?),
(3, 'zh-TW', '绔嬫笡50娓厓', '娑堣不婊?00娓厓鍗冲彲娓?0娓厓銆?),
(4, 'en', 'Birthday Special', 'Exclusive coupon for your birthday month.'),
(4, 'zh-CN', '鐢熸棩鐗规儬', '鐢熸棩鏈堜唤涓撳睘浼樻儬鍒搞€?),
(4, 'zh-TW', '鐢熸棩鐗规儬', '鐢熸棩鏈堜唤灏堝爆鍎儬鍒搞€?);

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
(1, 'zh-CN', '閫傜敤浜庡爞椋熷拰澶栧崠'),
(1, 'zh-CN', '涓嶉€傜敤浜庡閫佹湇鍔?),
(1, 'zh-CN', '涓嶅彲涓庡叾浠栦紭鎯犲悓鏃朵娇鐢?),
(1, 'zh-TW', '閬╃敤鏂煎爞椋熷拰澶栬常'),
(1, 'zh-TW', '涓嶉仼鐢ㄦ柤澶栭€佹湇鍕?),
(1, 'zh-TW', '涓嶅彲鑸囧叾浠栧劒鎯犲悓鏅備娇鐢?),
(2, 'en', 'Choice of soft drink, coffee, or tea'),
(2, 'en', 'Limit one free drink per customer per day'),
(2, 'zh-CN', '鍙€夋嫨姹芥按銆佸挅鍟℃垨鑼?),
(2, 'zh-CN', '姣忎綅椤惧姣忓ぉ闄愬厬涓€鏉?),
(2, 'zh-TW', '鍙伕鎿囨苯姘淬€佸挅鍟℃垨鑼?),
(2, 'zh-TW', '姣忎綅椤у姣忓ぉ闄愬厡涓€鏉?),
(3, 'en', 'Minimum spend of HK$300 required'),
(3, 'en', 'Discount applied before service charge'),
(3, 'zh-CN', '闇€婊?00娓厓鏂瑰彲浣跨敤'),
(3, 'zh-CN', '鎶樻墸鍦ㄥ姞鏀舵湇鍔¤垂鍓嶈绠?),
(3, 'zh-TW', '闇€婊?00娓厓鏂瑰彲浣跨敤'),
(3, 'zh-TW', '鎶樻墸鏂煎姞鏀舵湇鍕欒不鍓嶈▓绠?),
(4, 'en', 'Valid only during your birthday month'),
(4, 'en', 'Must present valid ID for verification'),
(4, 'zh-CN', '浠呴檺鐢熸棩鏈堜唤浣跨敤'),
(4, 'zh-CN', '闇€鍑虹ず鏈夋晥韬唤璇佹槑'),
(4, 'zh-TW', '鍍呴檺鐢熸棩鏈堜唤浣跨敤'),
(4, 'zh-TW', '闇€鍑虹ず鏈夋晥韬唤璀夋槑'),
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
(1, 'zh-TW', '鍦栫墖鍙緵鍙冭€冿紝瀵﹂殯渚涙噳鍙兘鏈夋墍涓嶅悓'),
(2, 'zh-TW', '鍦栫墖鍙緵鍙冭€冿紝瀵﹂殯渚涙噳鍙兘鏈夋墍涓嶅悓'),
(3, 'zh-TW', '鍦栫墖鍙緵鍙冭€冿紝瀵﹂殯渚涙噳鍙兘鏈夋墍涓嶅悓'),
(4, 'zh-TW', '鍦栫墖鍙緵鍙冭€冿紝瀵﹂殯渚涙噳鍙兘鏈夋墍涓嶅悓'),
(1, 'zh-TW', '鍎儬鍒镐笉鍙厡鎻涚従閲戙€佷俊鐢ㄩ鎴栧叾浠栫敘鍝?),
(2, 'zh-TW', '鍎儬鍒镐笉鍙厡鎻涚従閲戙€佷俊鐢ㄩ鎴栧叾浠栫敘鍝?),
(3, 'zh-TW', '鍎儬鍒镐笉鍙厡鎻涚従閲戙€佷俊鐢ㄩ鎴栧叾浠栫敘鍝?),
(4, 'zh-TW', '鍎儬鍒镐笉鍙厡鎻涚従閲戙€佷俊鐢ㄩ鎴栧叾浠栫敘鍝?),
(1, 'zh-TW', 'Yummy Restaurant 淇濈暀闅ㄦ檪鍙栨秷銆佹洿鏀规垨淇▊姊濇鍙婄窗鍓囦箣娆婂埄锛屾仌涓嶅彟琛岄€氱煡'),
(2, 'zh-TW', 'Yummy Restaurant 淇濈暀闅ㄦ檪鍙栨秷銆佹洿鏀规垨淇▊姊濇鍙婄窗鍓囦箣娆婂埄锛屾仌涓嶅彟琛岄€氱煡'),
(3, 'zh-TW', 'Yummy Restaurant 淇濈暀闅ㄦ檪鍙栨秷銆佹洿鏀规垨淇▊姊濇鍙婄窗鍓囦箣娆婂埄锛屾仌涓嶅彟琛岄€氱煡'),
(4, 'zh-TW', 'Yummy Restaurant 淇濈暀闅ㄦ檪鍙栨秷銆佹洿鏀规垨淇▊姊濇鍙婄窗鍓囦箣娆婂埄锛屾仌涓嶅彟琛岄€氱煡'),
(1, 'zh-TW', '濡傛湁鐢㈠搧缂鸿波锛屽叕鍙稿彲鏇存彌鐐哄悓绛夋垨鏇撮珮鍍瑰€间箣椋熷搧'),
(2, 'zh-TW', '濡傛湁鐢㈠搧缂鸿波锛屽叕鍙稿彲鏇存彌鐐哄悓绛夋垨鏇撮珮鍍瑰€间箣椋熷搧'),
(3, 'zh-TW', '濡傛湁鐢㈠搧缂鸿波锛屽叕鍙稿彲鏇存彌鐐哄悓绛夋垨鏇撮珮鍍瑰€间箣椋熷搧'),
(4, 'zh-TW', '濡傛湁鐢㈠搧缂鸿波锛屽叕鍙稿彲鏇存彌鐐哄悓绛夋垨鏇撮珮鍍瑰€间箣椋熷搧'),
(1, 'zh-CN', '鍥剧墖浠呬緵鍙傝€冿紝瀹為檯渚涘簲鍙兘鏈夋墍涓嶅悓'),
(2, 'zh-CN', '鍥剧墖浠呬緵鍙傝€冿紝瀹為檯渚涘簲鍙兘鏈夋墍涓嶅悓'),
(3, 'zh-CN', '鍥剧墖浠呬緵鍙傝€冿紝瀹為檯渚涘簲鍙兘鏈夋墍涓嶅悓'),
(4, 'zh-CN', '鍥剧墖浠呬緵鍙傝€冿紝瀹為檯渚涘簲鍙兘鏈夋墍涓嶅悓'),
(1, 'zh-CN', '浼樻儬鍒镐笉鍙厬鎹㈢幇閲戙€佷俊鐢ㄩ鎴栧叾浠栦骇鍝?),
(2, 'zh-CN', '浼樻儬鍒镐笉鍙厬鎹㈢幇閲戙€佷俊鐢ㄩ鎴栧叾浠栦骇鍝?),
(3, 'zh-CN', '浼樻儬鍒镐笉鍙厬鎹㈢幇閲戙€佷俊鐢ㄩ鎴栧叾浠栦骇鍝?),
(4, 'zh-CN', '浼樻儬鍒镐笉鍙厬鎹㈢幇閲戙€佷俊鐢ㄩ鎴栧叾浠栦骇鍝?),
(1, 'zh-CN', 'Yummy Restaurant 淇濈暀闅忔椂鍙栨秷銆佹洿鏀规垨淇鏉℃鍙婄粏鍒欑殑鏉冨埄锛屾仌涓嶅彟琛岄€氱煡'),
(2, 'zh-CN', 'Yummy Restaurant 淇濈暀闅忔椂鍙栨秷銆佹洿鏀规垨淇鏉℃鍙婄粏鍒欑殑鏉冨埄锛屾仌涓嶅彟琛岄€氱煡'),
(3, 'zh-CN', 'Yummy Restaurant 淇濈暀闅忔椂鍙栨秷銆佹洿鏀规垨淇鏉℃鍙婄粏鍒欑殑鏉冨埄锛屾仌涓嶅彟琛岄€氱煡'),
(4, 'zh-CN', 'Yummy Restaurant 淇濈暀闅忔椂鍙栨秷銆佹洿鏀规垨淇鏉℃鍙婄粏鍒欑殑鏉冨埄锛屾仌涓嶅彟琛岄€氱煡'),
(1, 'zh-CN', '濡傛湁浜у搧缂鸿揣锛屽叕鍙稿彲鏇存崲涓哄悓绛夋垨鏇撮珮浠峰€肩殑椋熷搧'),
(2, 'zh-CN', '濡傛湁浜у搧缂鸿揣锛屽叕鍙稿彲鏇存崲涓哄悓绛夋垨鏇撮珮浠峰€肩殑椋熷搧'),
(3, 'zh-CN', '濡傛湁浜у搧缂鸿揣锛屽叕鍙稿彲鏇存崲涓哄悓绛夋垨鏇撮珮浠峰€肩殑椋熷搧'),
(4, 'zh-CN', '濡傛湁浜у搧缂鸿揣锛屽叕鍙稿彲鏇存崲涓哄悓绛夋垨鏇撮珮浠峰€肩殑椋熷搧');

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
(1, 'zh-CN', '鍓嶈彍'),
(1, 'zh-TW', '鍓嶈彍'),
(2, 'en', 'Soup'),
(2, 'zh-CN', '姹?),
(2, 'zh-TW', '婀搧'),
(3, 'en', 'Main Courses'),
(3, 'zh-CN', '涓昏彍'),
(3, 'zh-TW', '涓昏彍'),
(4, 'en', 'Dessert'),
(4, 'zh-CN', '鐢滃搧'),
(4, 'zh-TW', '鐢滃搧'),
(5, 'en', 'Drink'),
(5, 'zh-CN', '楗枡'),
(5, 'zh-TW', '椋叉枡'),
(6, 'en', 'Staple Foods'),
(6, 'zh-CN', '涓婚'),
(6, 'zh-TW', '涓婚'),
(7, 'en', 'Supplies'),
(7, 'zh-CN', '鐢ㄥ搧'),
(7, 'zh-TW', '鐢ㄥ搧');


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
(1, 'zh-CN', '鑵屽埗榛勭摐鑺?, '鐢ㄩ鏂欒厡鍒剁殑榛勭摐鑺憋紝娓呯埥鍙彛銆?),
(1, 'zh-TW', '閱冭＝榛冪摐鑺?, '浠ラ鏂欓唭瑁界殑榛冪摐鑺憋紝娓呮柊鐖藉彛銆?),
(2, 'en', 'Spicy Wood Ear Mushrooms', 'Black fungus tossed in vinegar, garlic, and chili oil.'),
(2, 'zh-CN', '楹昏荆鏈ㄨ€?, '榛戞湪鑰虫媽閱嬨€佽挏鍜岃荆娌癸紝鐖藉彛寮€鑳冦€?),
(2, 'zh-TW', '楹昏荆鏈ㄨ€?, '榛戞湪鑰虫媽閱嬨€佽挏鑸囪荆娌癸紝鐖藉彛闁嬭儍銆?),
(3, 'en', 'Mouthwatering Chicken', 'Poached chicken drenched in spicy Sichuan chili sauce.'),
(3, 'zh-CN', '鍙ｆ按楦?, '瀚╅浮娴告场鍦ㄩ夯杈ｇ孩娌逛腑锛岄杈ｈ浜恒€?),
(3, 'zh-TW', '鍙ｆ按闆?, '瀚╅洖娴告场鍦ㄩ夯杈ｇ磪娌逛腑锛岄杈ｈ獦浜恒€?),
(4, 'en', 'Suan Cai Fish Soup', 'Sliced fish simmered in pickled mustard greens and chili broth.'),
(4, 'zh-CN', '閰歌彍楸兼堡', '楸肩墖鐐栭吀鑿滃拰杈ｆ堡锛岄吀杈ｅ紑鑳冦€?),
(4, 'zh-TW', '閰歌彍榄氭汞', '榄氱墖鐕夐吀鑿滆垏杈ｆ汞锛岄吀杈ｉ枊鑳冦€?),
(5, 'en', 'Chongqing-style Angus Beef', 'Spicy Angus beef with bean paste and lemongrass.'),
(5, 'zh-CN', '閲嶅簡椋庡懗瀹夋牸鏂墰鑲?, '杈ｅ懗瀹夋牸鏂墰鑲夐厤璞嗙摚閰卞拰棣欒寘锛岄夯杈ｆ寔涔呫€?),
(5, 'zh-TW', '閲嶆叾棰ㄥ懗瀹夋牸鏂墰鑲?, '杈ｅ懗瀹夋牸鏂墰鑲夋惌閰嶈眴鐡ｉ啲鑸囬鑼咃紝楹昏荆鎸佷箙銆?),
(6, 'en', 'Mapo Tofu', 'Silken tofu in spicy bean paste sauce with minced beef and Sichuan peppercorns.'),
(6, 'zh-CN', '楹诲﹩璞嗚厫', '瀚╄眴鑵愰厤鐗涜倝鏈拰楹昏荆璞嗙摚閰憋紝椋庡懗鍗佽冻銆?),
(6, 'zh-TW', '楹诲﹩璞嗚厫', '瀚╄眴鑵愭惌閰嶇墰鑲夋湯鑸囬夯杈ｈ眴鐡ｉ啲锛岄ⅷ鍛冲崄瓒炽€?),
(7, 'en', 'Dan Dan Noodles', 'Spicy noodles topped with minced pork, preserved vegetables, and chili oil.'),
(7, 'zh-CN', '鎷呮媴闈?, '杈ｅ懗闈㈡潯閰嶇尓鑲夋湯銆佽娊鑿滃拰绾㈡补锛岄杈ｈ浜恒€?),
(7, 'zh-TW', '鎿旀摂楹?, '杈ｅ懗楹垫鎼厤璞倝鏈€佽娊鑿滆垏绱呮补锛岄杈ｈ獦浜恒€?),
(8, 'en', 'Twice-Cooked Pork', 'Pork belly simmered then stir-fried with leeks and chili bean paste.'),
(8, 'zh-CN', '鍥為攨鑲?, '浜旇姳鑲夊厛鐓悗鐐掞紝鎼厤钂滆嫍鍜岃眴鐡ｉ叡锛岄娴撳彲鍙ｃ€?),
(8, 'zh-TW', '鍥為崑鑲?, '浜旇姳鑲夊厛鐓緦鐐掞紝鎼厤钂滆嫍鑸囪眴鐡ｉ啲锛岄婵冨彲鍙ｃ€?),
(9, 'en', 'Boiled Beef in Chili Broth', 'Tender beef slices in a fiery broth with Sichuan peppercorns.'),
(9, 'zh-CN', '姘寸叜鐗涜倝', '鐗涜倝鐗囨蹈娉″湪楹昏荆绾㈡堡涓紝棣欒荆杩囩樉銆?),
(9, 'zh-TW', '姘寸叜鐗涜倝', '鐗涜倝鐗囨蹈娉″湪楹昏荆绱呮汞涓紝棣欒荆閬庣櫘銆?),
(10, 'en', 'Fish-Fragrant Eggplant', 'Braised eggplant in garlic, ginger, and sweet chili sauce.'),
(10, 'zh-CN', '楸奸鑼勫瓙', '鑼勫瓙鐐栫叜浜庤挏濮滃拰鐢滆荆閰变腑锛岄姘旀墤榧汇€?),
(10, 'zh-TW', '榄氶鑼勫瓙', '鑼勫瓙鐕夌叜鏂艰挏钖戣垏鐢滆荆閱腑锛岄姘ｆ挷榧汇€?),
(11, 'en', 'Sichuan Glutinous Rice Cake', 'Sticky rice cake with brown sugar and sesame.'),
(11, 'zh-CN', '鍥涘窛绯背绯?, '绯背绯曢厤绾㈢硸鍜岃姖楹伙紝鐢滆€屼笉鑵汇€?),
(11, 'zh-TW', '鍥涘窛绯背绯?, '绯背绯曟惌閰嶇磪绯栬垏鑺濋夯锛岀敎鑰屼笉鑶┿€?),
(12, 'en', 'Salty Lemon 7-Up', 'Classic Hong Kong salty lemon soda with 7-Up.'),
(12, 'zh-CN', '鍜告煚7', '娓紡缁忓吀鍜告煚涓冨枩锛屾竻鐖借В娓淬€?),
(12, 'zh-TW', '鍜告7', '娓紡缍撳吀楣规涓冨枩锛屾竻鐖借В娓淬€?),
(13, 'en', 'Red Bean Ice', 'Sweet red beans served over crushed ice.'),
(13, 'zh-CN', '绾㈣眴鍐?, '棣欑敎绾㈣眴閰嶄笂纰庡啺锛屽鏃ュ繀澶囥€?),
(13, 'zh-TW', '绱呰眴鍐?, '棣欑敎绱呰眴閰嶄笂纰庡啺锛屽鏃ュ繀鍌欍€?),
(14, 'en', 'Hot Milk Tea', 'Rich Hong Kong-style milk tea, best served hot.'),
(14, 'zh-CN', '鐑ザ鑼?, '娴撻儊娓紡濂惰尪锛岀儹楗渶浣炽€?),
(14, 'zh-TW', '鐔卞ザ鑼?, '婵冮儊娓紡濂惰尪锛岀啽椋叉渶浣炽€?),
(15, 'en', 'Grape Oolong Tea', 'Oolong tea infused with grape aroma, refreshing and unique.'),
(15, 'zh-CN', '钁¤悇涔岄緳鑼?, '涔岄緳鑼惰瀺鍚堣憽钀勯姘旓紝娓呮柊鐙壒銆?),
(15, 'zh-TW', '钁¤悇鐑忛緧鑼?, '鐑忛緧鑼惰瀺鍚堣憽钀勯姘ｏ紝娓呮柊鐛ㄧ壒銆?),
(16, 'en', 'Hot Lemon Tea', 'Hot lemon tea, tangy and comforting.'),
(16, 'zh-CN', '鐑煚鑼?, '鐑煚妾尪锛岄吀鐢滄殩蹇冦€?),
(16, 'zh-TW', '鐔辨鑼?, '鐔辨妾尪锛岄吀鐢滄殩蹇冦€?),
(17, 'en', 'Iced Milk Tea', 'Classic Hong Kong-style milk tea, served chilled.'),
(17, 'zh-CN', '鍐诲ザ鑼?, '缁忓吀娓紡濂惰尪锛屽啺鍑夌埥鍙ｃ€?),
(17, 'zh-TW', '鍑嶅ザ鑼?, '缍撳吀娓紡濂惰尪锛屽啺娑肩埥鍙ｃ€?),
(18, 'en', 'Iced Lemon Tea', 'Crisp iced tea with fresh lemon slices.'),
(18, 'zh-CN', '鍐绘煚鑼?, '鍐伴晣鏌犳鑼讹紝娓呯埥瑙ｆ复銆?),
(18, 'zh-TW', '鍑嶆鑼?, '鍐伴幃妾告鑼讹紝娓呯埥瑙ｆ复銆?),
(19, 'en', 'Steamed Rice', 'Fluffy steamed rice, perfect as a staple side dish.'),
(19, 'zh-CN', '绫抽キ', '钃澗鐨勮捀绫抽キ锛屽畬缇庣殑涓婚閰嶈彍銆?),
(19, 'zh-TW', '绫抽／', '钃瑔鐨勮捀绫抽／锛屽畬缇庣殑涓婚閰嶈彍銆?),
(20, 'en', 'Noodles', 'Soft and tender wheat noodles.'),
(20, 'zh-CN', '楹?, '杞€屽鐨勫皬楹﹂潰鏉°€?),
(20, 'zh-TW', '楹?, '杌熻€屽鐨勫皬楹ラ旱姊濄€?),
(21, 'en', 'Potato Starch', 'Smooth and creamy potato starch dish.'),
(21, 'zh-CN', '钖矇', '鍏夋粦缁嗚吇鐨勮柉绮夐鍝併€?),
(21, 'zh-TW', '钖矇', '鍏夋粦绱拌啯鐨勮柉绮夐鍝併€?);

INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(22, 'en', 'Wooden Chopsticks', 'Disposable wooden chopsticks for takeaway.'),
(22, 'zh-CN', '鏈ㄧ', '涓€娆℃€ф湪绛凤紝浠呴檺澶栧崠浣跨敤銆?),
(22, 'zh-TW', '鏈ㄧ', '涓€娆℃€ф湪绛凤紝鍍呴檺澶栧付浣跨敤銆?);

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
(1, 'zh-CN', '鍓嶈彍'), (2, 'zh-CN', '涓昏彍'), (3, 'zh-CN', '楗枡'), (4, 'zh-CN', '涓婚'),
(5, 'zh-CN', '鍓嶈彍'), (6, 'zh-CN', '姹ゅ搧'), (7, 'zh-CN', '涓昏彍'), (8, 'zh-CN', '楗枡'), (9, 'zh-CN', '涓婚'),
(10, 'zh-CN', '涓昏彍'), (11, 'zh-CN', '楗枡'), (12, 'zh-CN', '涓婚'),
(1, 'zh-TW', '鍓嶈彍'), (2, 'zh-TW', '涓昏彍'), (3, 'zh-TW', '椋叉枡'), (4, 'zh-TW', '涓婚'),
(5, 'zh-TW', '鍓嶈彍'), (6, 'zh-TW', '婀搧'), (7, 'zh-TW', '涓昏彍'), (8, 'zh-TW', '椋叉枡'), (9, 'zh-TW', '涓婚'),
(10, 'zh-TW', '涓昏彍'), (11, 'zh-TW', '椋叉枡'), (12, 'zh-TW', '涓婚');

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
('Chili Powder', 3, 'grams', 80.00, 100.00),      -- 搴瓨80g锛屼綆鏂奸噸鏂拌▊璩兼按骞?00g
('Garlic', 1, 'grams', 800.00, 300.00),           -- 搴瓨800g锛岄珮鏂奸噸鏂拌▊璩兼按骞?00g
('Ginger', 1, 'grams', 60.00, 150.00),            -- 搴瓨60g锛屼綆鏂奸噸鏂拌▊璩兼按骞?50g
('Soy Sauce', 3, 'ml', 1200.00, 500.00),          -- 搴瓨1200ml锛岄珮鏂奸噸鏂拌▊璩兼按骞?00ml
('Rice Vinegar', 3, 'ml', 900.00, 400.00);        -- 搴瓨900ml锛岄珮鏂奸噸鏂拌▊璩兼按骞?00ml

-- 鏂板鏉愭枡鍒嗛锛堝鏋滈渶瑕侊級
-- 妾㈡煡鏄惁宸叉湁閫欎簺鍒嗛锛屽鏋滄矑鏈夊墖鎻掑叆
INSERT IGNORE INTO materials_category (category_name) VALUES
('Spice'),
('Vegetable'),
('Sauce');

-- 鏂板5浠紸ppetizers (category_id=1)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(1, 32.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/23.jpg', 2, TRUE),
(1, 30.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/24.jpg', 1, TRUE),
(1, 35.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/25.jpg', 3, TRUE),
(1, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/26.jpg', 0, TRUE),
(1, 34.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/27.jpg', 2, TRUE);

-- 鏂板3浠絊oup (category_id=2)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(2, 52.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/28.jpg', 1, TRUE),
(2, 56.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/29.jpg', 2, TRUE),
(2, 48.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/30.jpg', 0, TRUE);

-- 鏂板3浠紻essert (category_id=4)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(4, 25.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/31.jpg', 0, TRUE),
(4, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/32.jpg', 0, TRUE),
(4, 30.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/33.jpg', 0, TRUE);

-- 鏂板5浠組ain Courses (category_id=3)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(3, 98.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/34.jpg', 4, TRUE),
(3, 85.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/35.jpg', 3, TRUE),
(3, 92.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/36.jpg', 5, TRUE),
(3, 78.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/37.jpg', 2, TRUE),
(3, 88.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/38.jpg', 3, TRUE);

-- 鏂板3浠紻rink (category_id=5)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(5, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/39.jpg', 0, TRUE),
(5, 24.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/40.jpg', 0, TRUE),
(5, 30.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/41.jpg', 0, TRUE);

-- =================================================================
-- 鏂板鑿滈鐨勫瑾炶█缈昏
-- =================================================================

-- 闁嬭儍鑿?(Appetizers) 缈昏
-- Item 23
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(23, 'en', 'Crispy Lotus Root', 'Thinly sliced lotus root, deep-fried to perfection with a hint of sesame oil.'),
(23, 'zh-CN', '棣欒剢钘曠墖', '钖勫垏钘曠墖锛岀偢鑷抽噾榛勯叆鑴嗭紝甯︽湁鑺濋夯娌归姘斻€?),
(23, 'zh-TW', '棣欒剢钘曠墖', '钖勫垏钘曠墖锛岀偢鑷抽噾榛冮叆鑴嗭紝甯舵湁鑺濋夯娌归姘ｃ€?);

-- Item 24
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(24, 'en', 'Spicy Jellyfish', 'Marinated jellyfish with chili oil and sesame, crispy and refreshing.'),
(24, 'zh-CN', '楹昏荆娴疯渿', '鐢ㄨ荆娌瑰拰鑺濋夯鑵屽埗鐨勬捣铚囷紝鐖借剢寮€鑳冦€?),
(24, 'zh-TW', '楹昏荆娴疯渿', '鐢ㄨ荆娌瑰拰鑺濋夯閱冭＝鐨勬捣铚囷紝鐖借剢闁嬭儍銆?);

-- Item 25
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(25, 'en', 'Garlic Edamame', 'Fresh edamame pods tossed in garlic butter and sea salt.'),
(25, 'zh-CN', '钂滈姣涜眴', '鏂伴矞姣涜眴鎷屽叆钂滈榛勬补鍜屾捣鐩愶紝棣欐皵鎵戦蓟銆?),
(25, 'zh-TW', '钂滈姣涜眴', '鏂伴姣涜眴鎷屽叆钂滈濂舵补鍜屾捣楣斤紝棣欐埃鎾查蓟銆?);

-- Item 26
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(26, 'en', 'Vegetable Spring Rolls', 'Crispy spring rolls filled with fresh vegetables and glass noodles.'),
(26, 'zh-CN', '钄彍鏄ュ嵎', '澶栫毊閰ヨ剢锛屽唴棣呮槸鏂伴矞钄彍鍜岀矇涓濄€?),
(26, 'zh-TW', '钄彍鏄ユ嵅', '澶栫毊閰ヨ剢锛屽収椁℃槸鏂伴钄彍鍜岀矇绲层€?);

-- Item 27
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(27, 'en', 'Sichuan Peanuts', 'Spicy peanuts stir-fried with Sichuan peppercorns and chili flakes.'),
(27, 'zh-CN', '鍥涘窛杈ｈ姳鐢?, '鐢ㄥ洓宸濊姳妞掑拰杈ｆ鐗囩倰鍒剁殑杈ｅ懗鑺辩敓銆?),
(27, 'zh-TW', '鍥涘窛杈ｈ姳鐢?, '鐢ㄥ洓宸濊姳妞掑拰杈ｆ鐗囩倰瑁界殑杈ｅ懗鑺辩敓銆?);

-- 婀搧 (Soup) 缈昏
-- Item 28
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(28, 'en', 'Hot & Sour Soup', 'Classic Sichuan hot and sour soup with tofu, mushrooms, and bamboo shoots.'),
(28, 'zh-CN', '閰歌荆姹?, '缁忓吀鐨勫洓宸濋吀杈ｆ堡锛屽唴鏈夎眴鑵愩€佽槕鑿囧拰绔圭瑡銆?),
(28, 'zh-TW', '閰歌荆婀?, '缍撳吀鐨勫洓宸濋吀杈ｆ汞锛屽収鏈夎眴鑵愩€佽槕鑿囧拰绔圭瓖銆?);

-- Item 29
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(29, 'en', 'Winter Melon Soup', 'Light and clear winter melon soup with goji berries and chicken broth.'),
(29, 'zh-CN', '鍐摐姹?, '娓呮贰鐨勫啲鐡滄堡锛屽姞鍏ユ灨鏉炲拰楦℃堡銆?),
(29, 'zh-TW', '鍐摐婀?, '娓呮贰鐨勫啲鐡滄汞锛屽姞鍏ユ灨鏉炲拰闆炴汞銆?);

-- Item 30
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(30, 'en', 'Corn and Crab Meat Soup', 'Creamy corn soup with fresh crab meat and egg white.'),
(30, 'zh-CN', '鐜夌背锜硅倝姹?, '濂舵补鐜夌背姹ゅ姞鍏ユ柊椴滆煿鑲夊拰铔嬬櫧銆?),
(30, 'zh-TW', '鐜夌背锜硅倝婀?, '濂舵补鐜夌背婀姞鍏ユ柊楫煿鑲夊拰铔嬬櫧銆?);

-- 鐢滃搧 (Dessert) 缈昏
-- Item 31
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(31, 'en', 'Mango Pudding', 'Fresh mango pudding topped with condensed milk and mango chunks.'),
(31, 'zh-CN', '鑺掓灉甯冧竵', '鏂伴矞鑺掓灉甯冧竵锛屾穻涓婄偧涔冲拰鑺掓灉鍧椼€?),
(31, 'zh-TW', '鑺掓灉甯冧竵', '鏂伴鑺掓灉甯冧竵锛屾穻涓婄厜涔冲拰鑺掓灉濉娿€?);

-- Item 32
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(32, 'en', 'Sesame Balls', 'Deep-fried glutinous rice balls filled with sweet red bean paste.'),
(32, 'zh-CN', '鑺濋夯鐞?, '娌圭偢绯背鐞冿紝鍐呴鏄敎绾㈣眴娌欍€?),
(32, 'zh-TW', '鑺濋夯鐞?, '娌圭偢绯背鐞冿紝鍏чぁ鏄敎绱呰眴娌欍€?);

-- Item 33
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(33, 'en', 'Egg Tart', 'Classic Hong Kong-style egg tarts with flaky pastry.'),
(33, 'zh-CN', '铔嬫尀', '缁忓吀娓紡铔嬫尀锛屽鐨叆鑴嗐€?),
(33, 'zh-TW', '铔嬫捇', '缍撳吀娓紡铔嬫捇锛屽鐨叆鑴嗐€?);

-- 涓昏彍 (Main Courses) 缈昏
-- Item 34
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(34, 'en', 'Kung Pao Chicken', 'Diced chicken stir-fried with peanuts, chili, and Sichuan peppercorns.'),
(34, 'zh-CN', '瀹繚楦′竵', '楦¤倝涓佺倰鑺辩敓銆佽荆妞掑拰鍥涘窛鑺辨銆?),
(34, 'zh-TW', '瀹繚闆炰竵', '闆炶倝涓佺倰鑺辩敓銆佽荆妞掑拰鍥涘窛鑺辨銆?);

-- Item 35
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(35, 'en', 'Sweet & Sour Pork', 'Crispy pork pieces in tangy sweet and sour sauce with pineapple.'),
(35, 'zh-CN', '绯栭唻閲岃剨', '閰ヨ剢鐨勭尓鑲夊潡瑁逛笂閰哥敎閰辨眮锛岄厤鑿犺悵銆?),
(35, 'zh-TW', '绯栭唻閲岃剨', '閰ヨ剢鐨勮爆鑲夊瑁逛笂閰哥敎閱眮锛岄厤槌虫ⅷ銆?);

-- Item 36
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(36, 'en', 'Sichuan Dry Pot with Seafood', 'Assorted seafood cooked in a spicy dry pot with vegetables.'),
(36, 'zh-CN', '鍥涘窛娴烽矞骞查攨', '澶氱娴烽矞涓庤敩鑿滃湪楹昏荆骞查攨涓児鍒躲€?),
(36, 'zh-TW', '鍥涘窛娴烽涔鹃崑', '澶氱ó娴烽鑸囪敩鑿滃湪楹昏荆涔鹃崑涓児瑁姐€?);

-- Item 37
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(37, 'en', 'Braised Pork Belly', 'Pork belly braised in soy sauce and spices until tender.'),
(37, 'zh-CN', '绾㈢儳鑲?, '浜旇姳鑲夌敤閰辨补鍜岄鏂欑倴鐓嚦杞銆?),
(37, 'zh-TW', '绱呯噿鑲?, '浜旇姳鑲夌敤閱补鍜岄鏂欑噳鐓嚦杌熷銆?);

-- Item 38
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(38, 'en', 'Lemon Chicken', 'Crispy chicken with lemon sauce, sweet and tangy.'),
(38, 'zh-CN', '鏌犳楦?, '閰ヨ剢楦¤倝閰嶆煚妾叡锛岄吀鐢滃彲鍙ｃ€?),
(38, 'zh-TW', '妾告闆?, '閰ヨ剢闆炶倝閰嶆妾啲锛岄吀鐢滃彲鍙ｃ€?);

-- 椋叉枡 (Drink) 缈昏
-- Item 39
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(39, 'en', 'Iced Honey Lemon', 'Refreshing iced drink with honey and fresh lemon slices.'),
(39, 'zh-CN', '鍐拌渹铚滄煚妾?, '铚傝湝鍜屾柊椴滄煚妾墖鍒舵垚鐨勫啺闀囬ギ鍝併€?),
(39, 'zh-TW', '鍐拌渹铚滄妾?, '铚傝湝鍜屾柊楫妾墖瑁芥垚鐨勫啺閹２鍝併€?);

-- Item 40
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(40, 'en', 'Peach Oolong Tea', 'Oolong tea infused with peach flavor, served hot or cold.'),
(40, 'zh-CN', '铚滄涔岄緳鑼?, '甯︽湁铚滄棣欐皵鐨勪箤榫欒尪锛屽彲鐑彲鍐般€?),
(40, 'zh-TW', '铚滄鐑忛緧鑼?, '甯舵湁铚滄棣欐埃鐨勭儚榫嶈尪锛屽彲鐔卞彲鍐般€?);

-- Item 41
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(41, 'en', 'Coconut Milkshake', 'Creamy coconut milkshake topped with shredded coconut.'),
(41, 'zh-CN', '妞板ザ鏄?, '娴撻儊鐨勬ぐ濂跺ザ鏄旓紝鎾掍笂妞颁笣銆?),
(41, 'zh-TW', '妞板ザ鏄?, '婵冮儊鐨勬ぐ濂跺ザ鏄旓紝鎾掍笂妞扮挡銆?);

-- =================================================================
-- 鏂板鑿滈鐨勬绫?-- =================================================================

-- 棣栧厛妾㈡煡鏄惁宸叉湁閫欎簺妯欑堡锛岃嫢鐒″墖鎻掑叆
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

-- 鐐烘柊澧炶彍椁氭坊鍔犳绫?-- 娉ㄦ剰锛氶€欒！浣跨敤SELECT瑾炲彞渚嗙嵅鍙栨绫D锛岀⒑淇濆紩鐢ㄦ纰?INSERT INTO menu_tag (item_id, tag_id) 
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
-- 鏂板鑿滈鐨勮嚜瀹氱京閬搁爡
-- =================================================================

-- 鐐烘柊鑿滈娣诲姞鑷畾缇╅伕闋?INSERT INTO item_customization_options (item_id, group_id, max_selections, is_required) VALUES
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
-- 閵峰敭鏁告摎 (2025骞?0鏈?鏃?- 2026骞?鏈?1鏃?
-- 鍏?0绛嗚▊鍠紝oid 绡勫湇 29锝?8
-- =================================================================


-- =================================================================
-- 2025骞?0鏈堣▊鍠?(12绛?
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
-- 2025骞?1鏈堣▊鍠?(13绛?
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
-- 2025骞?2鏈堣▊鍠?(13绛?
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
-- 2026骞?鏈堣▊鍠?(12绛?
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
-- 瑷傚柈闋呯洰 (鍖呭惈鍘熸湁鑿滈鍜屾柊鑿滈)
-- =================================================================
INSERT INTO order_items (oid, item_id, qty) VALUES
-- 瑷傚柈 29 (2025-10-02)
(29, 5, 1), (29, 12, 2), (29, 23, 1),
-- 瑷傚柈 30 (2025-10-03)
(30, 6, 1), (30, 17, 1), (30, 24, 2),
-- 瑷傚柈 31 (2025-10-05)
(31, 8, 1), (31, 14, 2), (31, 28, 1),
-- 瑷傚柈 32 (2025-10-07)
(32, 9, 2), (32, 15, 1), (32, 25, 1),
-- 瑷傚柈 33 (2025-10-09)
(33, 10, 1), (33, 16, 2), (33, 29, 1),
-- 瑷傚柈 34 (2025-10-11)
(34, 7, 1), (34, 18, 1), (34, 26, 1), (34, 31, 1),
-- 瑷傚柈 35 (2025-10-13)
(35, 5, 2), (35, 13, 1), (35, 27, 1),
-- 瑷傚柈 36 (2025-10-15)
(36, 6, 1), (36, 12, 2), (36, 30, 1),
-- 瑷傚柈 37 (2025-10-17)
(37, 8, 2), (37, 17, 1), (37, 32, 1),
-- 瑷傚柈 38 (2025-10-19)
(38, 9, 1), (38, 14, 2), (38, 33, 1),
-- 瑷傚柈 39 (2025-10-22)
(39, 10, 1), (39, 15, 1), (39, 34, 1),
-- 瑷傚柈 40 (2025-10-25)
(40, 7, 2), (40, 16, 1), (40, 35, 1),
-- 瑷傚柈 41 (2025-11-01)
(41, 5, 1), (41, 18, 2), (41, 36, 1),
-- 瑷傚柈 42 (2025-11-03)
(42, 6, 2), (42, 12, 1), (42, 37, 1),
-- 瑷傚柈 43 (2025-11-05)
(43, 8, 1), (43, 17, 1), (43, 38, 1),
-- 瑷傚柈 44 (2025-11-07)
(44, 9, 2), (44, 14, 1), (44, 23, 1), (44, 39, 2),
-- 瑷傚柈 45 (2025-11-09)
(45, 10, 1), (45, 15, 2), (45, 24, 1),
-- 瑷傚柈 46 (2025-11-12)
(46, 7, 1), (46, 16, 1), (46, 25, 1), (46, 40, 1),
-- 瑷傚柈 47 (2025-11-14)
(47, 5, 2), (47, 18, 1), (47, 26, 1),
-- 瑷傚柈 48 (2025-11-16)
(48, 6, 1), (48, 12, 2), (48, 27, 1),
-- 瑷傚柈 49 (2025-11-18)
(49, 8, 1), (49, 17, 1), (49, 28, 1), (49, 41, 1),
-- 瑷傚柈 50 (2025-11-21)
(50, 9, 2), (50, 14, 1), (50, 29, 1),
-- 瑷傚柈 51 (2025-11-23)
(51, 10, 1), (51, 15, 2), (51, 30, 1),
-- 瑷傚柈 52 (2025-11-25)
(52, 7, 1), (52, 16, 1), (52, 31, 1),
-- 瑷傚柈 53 (2025-11-28)
(53, 5, 2), (53, 18, 1), (53, 32, 1),
-- 瑷傚柈 54 (2025-12-01)
(54, 6, 1), (54, 12, 2), (54, 33, 1),
-- 瑷傚柈 55 (2025-12-03)
(55, 8, 1), (55, 17, 1), (55, 34, 1),
-- 瑷傚柈 56 (2025-12-05)
(56, 9, 2), (56, 14, 1), (56, 35, 1),
-- 瑷傚柈 57 (2025-12-07)
(57, 10, 1), (57, 15, 2), (57, 36, 1),
-- 瑷傚柈 58 (2025-12-09)
(58, 7, 1), (58, 16, 1), (58, 37, 1),
-- 瑷傚柈 59 (2025-12-11)
(59, 5, 2), (59, 18, 1), (59, 38, 1),
-- 瑷傚柈 60 (2025-12-13)
(60, 6, 1), (60, 12, 2), (60, 23, 1), (60, 39, 1),
-- 瑷傚柈 61 (2025-12-15)
(61, 8, 1), (61, 17, 1), (61, 24, 1),
-- 瑷傚柈 62 (2025-12-17)
(62, 9, 2), (62, 14, 1), (62, 25, 1),
-- 瑷傚柈 63 (2025-12-19)
(63, 10, 1), (63, 15, 2), (63, 26, 1),
-- 瑷傚柈 64 (2025-12-22)
(64, 7, 1), (64, 16, 1), (64, 27, 1),
-- 瑷傚柈 65 (2025-12-25)
(65, 5, 2), (65, 18, 1), (65, 28, 1),
-- 瑷傚柈 66 (2025-12-28)
(66, 6, 1), (66, 12, 2), (66, 29, 1),
-- 瑷傚柈 67 (2026-01-02)
(67, 8, 1), (67, 17, 1), (67, 30, 1),
-- 瑷傚柈 68 (2026-01-04)
(68, 9, 2), (68, 14, 1), (68, 31, 1),
-- 瑷傚柈 69 (2026-01-06)
(69, 10, 1), (69, 15, 2), (69, 32, 1),
-- 瑷傚柈 70 (2026-01-08)
(70, 7, 1), (70, 16, 1), (70, 33, 1),
-- 瑷傚柈 71 (2026-01-10)
(71, 5, 2), (71, 18, 1), (71, 34, 1),
-- 瑷傚柈 72 (2026-01-12)
(72, 6, 1), (72, 12, 2), (72, 35, 1),
-- 瑷傚柈 73 (2026-01-14)
(73, 8, 1), (73, 17, 1), (73, 36, 1),
-- 瑷傚柈 74 (2026-01-16)
(74, 9, 2), (74, 14, 1), (74, 37, 1),
-- 瑷傚柈 75 (2026-01-18)
(75, 10, 1), (75, 15, 2), (75, 38, 1),
-- 瑷傚柈 76 (2026-01-20)
(76, 7, 1), (76, 16, 1), (76, 23, 1), (76, 39, 1),
-- 瑷傚柈 77 (2026-01-23)
(77, 5, 2), (77, 18, 1), (77, 24, 1),
-- 瑷傚柈 78 (2026-01-26)
(78, 6, 1), (78, 12, 2), (78, 25, 1);

-- =================================================================
-- 鍎儬鍒镐娇鐢ㄨ閷?-- =================================================================
INSERT INTO order_coupons (oid, coupon_id, discount_amount) VALUES
-- 10鏈堣▊鍠劒鎯犲埜
(30, 1, 15.60),  -- 10% off
(32, 2, 26.00),  -- Free drink (item 15)
(34, 3, 50.00),  -- HK$50 off
(36, 1, 12.80),  -- 10% off
(38, 2, 28.00),  -- Free drink (item 14)
(40, 3, 50.00),  -- HK$50 off
(42, 1, 18.40),  -- 10% off

-- 11鏈堣▊鍠劒鎯犲埜
(44, 2, 28.00),  -- Free drink (item 39)
(46, 3, 50.00),  -- HK$50 off
(48, 1, 14.20),  -- 10% off
(50, 2, 26.00),  -- Free drink (item 14)
(52, 3, 50.00),  -- HK$50 off
(54, 1, 16.80),  -- 10% off

-- 12鏈堣▊鍠劒鎯犲埜
(56, 2, 30.00),  -- Free drink (item 41)
(58, 3, 50.00),  -- HK$50 off
(60, 1, 13.40),  -- 10% off
(62, 2, 28.00),  -- Free drink (item 14)
(64, 3, 50.00),  -- HK$50 off
(66, 1, 15.20),  -- 10% off

-- 1鏈堣▊鍠劒鎯犲埜
(68, 2, 25.00),  -- Free drink (item 31)
(70, 3, 50.00),  -- HK$50 off
(72, 1, 17.60),  -- 10% off
(74, 2, 28.00),  -- Free drink (item 14)
(76, 3, 50.00),  -- HK$50 off
(78, 1, 14.80);  -- 10% off

-- =================================================================
-- 鏇存柊椤у绌嶅垎
-- =================================================================
UPDATE customer SET coupon_point = coupon_point + 150 WHERE cid = 1;
UPDATE customer SET coupon_point = coupon_point + 180 WHERE cid = 2;
UPDATE customer SET coupon_point = coupon_point + 120 WHERE cid = 3;
UPDATE customer SET coupon_point = coupon_point + 200 WHERE cid = 4;
UPDATE customer SET coupon_point = coupon_point + 160 WHERE cid = 5;

-- 鎻掑叆绌嶅垎姝峰彶瑷橀寗锛堜娇鐢ㄥ瓙鏌ヨ鐛插彇鏇存柊寰岀殑绌嶅垎锛?INSERT INTO coupon_point_history (cid, delta, resulting_points, action, note) VALUES
(1, 150, (SELECT coupon_point FROM customer WHERE cid = 1), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(2, 180, (SELECT coupon_point FROM customer WHERE cid = 2), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(3, 120, (SELECT coupon_point FROM customer WHERE cid = 3), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(4, 200, (SELECT coupon_point FROM customer WHERE cid = 4), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(5, 160, (SELECT coupon_point FROM customer WHERE cid = 5), 'earn', 'Points from Oct 2025 - Jan 2026 orders');

-- =================================================================
-- 闈炴渻鍝￠姺鍞暩鎿?(Walk-in Customer)
-- 50绛嗚▊鍠紝鏅傞枔绡勫湇锛?025骞?0鏈?鏃?- 2026骞?鏈?1鏃?-- 浣跨敤cid=0 (Walk-in Customer)
-- oid 绡勫湇锛?9锝?28
-- =================================================================


-- =================================================================
-- 鎻掑叆50绛嗛潪鏈冨摗瑷傚柈
-- =================================================================

-- 2025骞?0鏈堥潪鏈冨摗瑷傚柈 (13绛?  oid 79-91
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

-- 2025骞?1鏈堥潪鏈冨摗瑷傚柈 (13绛?  oid 92-104
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

-- 2025骞?2鏈堥潪鏈冨摗瑷傚柈 (12绛?  oid 105-116
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

-- 2026骞?鏈堥潪鏈冨摗瑷傚柈 (12绛?  oid 117-128
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
-- 鎻掑叆闈炴渻鍝¤▊鍠爡鐩?-- 瑷伙細瑷傚柈oid寰?9闁嬪锛堝凡鏈夎▊鍠埌78锛?-- =================================================================

-- 瑷傚柈79-91 (2025骞?0鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(79, 3, 1), (79, 12, 1), (79, 19, 1),  -- 鍙ｆ按闆?+ 鍜告煚7 + 绫抽／
(80, 6, 1), (80, 17, 1), (80, 20, 1),  -- 楹诲﹩璞嗚厫 + 鍑嶅ザ鑼?+ 楹?(81, 8, 1), (81, 14, 1), (81, 19, 1),  -- 鍥為崑鑲?+ 鐔卞ザ鑼?+ 绫抽／
(82, 5, 1), (82, 13, 1), (82, 19, 1),  -- 閲嶆叾鐗涜倝 + 绱呰眴鍐?+ 绫抽／
(83, 7, 1), (83, 18, 1), (83, 21, 1),  -- 鎿旀摂楹?+ 鍑嶆鑼?+ 钖矇
(84, 9, 1), (84, 15, 1), (84, 19, 1),  -- 姘寸叜鐗涜倝 + 钁¤悇鐑忛緧鑼?+ 绫抽／
(85, 10, 1), (85, 16, 1), (85, 20, 1), -- 榄氶鑼勫瓙 + 鐔辨鑼?+ 楹?(86, 1, 2), (86, 12, 2), (86, 19, 2),  -- 榛冪摐鑺眡2 + 鍜告煚7x2 + 绫抽／x2
(87, 2, 1), (87, 14, 1), (87, 19, 1),  -- 鏈ㄨ€?+ 鐔卞ザ鑼?+ 绫抽／
(88, 4, 1), (88, 17, 1), (88, 21, 1),  -- 閰歌彍榄氭汞 + 鍑嶅ザ鑼?+ 钖矇
(89, 23, 1), (89, 28, 1), (89, 34, 1), (89, 39, 1), -- 棣欒剢钘曠墖 + 閰歌荆婀?+ 瀹繚闆炰竵 + 鍐拌渹铚滄妾?(90, 24, 1), (90, 29, 1), (90, 35, 1), (90, 19, 1), -- 楹昏荆娴疯渿 + 鍐摐婀?+ 绯栭唻閲岃剨 + 绫抽／
(91, 25, 1), (91, 30, 1), (91, 36, 1), (91, 40, 1); -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑 + 铚滄鐑忛緧鑼?
-- 瑷傚柈92-104 (2025骞?1鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(92, 26, 1), (92, 31, 1), (92, 37, 1), (92, 41, 1), -- 钄彍鏄ュ嵎 + 鑺掓灉甯冧竵 + 绱呯噿鑲?+ 妞板ザ鏄?(93, 27, 2), (93, 32, 1), (93, 38, 1), (93, 12, 2), -- 鍥涘窛杈ｈ姳鐢焫2 + 鑺濋夯鐞?+ 妾告闆?+ 鍜告煚7x2
(94, 3, 1), (94, 28, 1), (94, 34, 1), (94, 18, 1), -- 鍙ｆ按闆?+ 閰歌荆婀?+ 瀹繚闆炰竵 + 鍑嶆鑼?(95, 5, 1), (95, 29, 1), (95, 35, 1), (95, 17, 1), -- 閲嶆叾鐗涜倝 + 鍐摐婀?+ 绯栭唻閲岃剨 + 鍑嶅ザ鑼?(96, 6, 1), (96, 30, 1), (96, 36, 1), (96, 16, 1), -- 楹诲﹩璞嗚厫 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑 + 鐔辨鑼?(97, 8, 1), (97, 23, 1), (97, 37, 1), (97, 14, 1), -- 鍥為崑鑲?+ 棣欒剢钘曠墖 + 绱呯噿鑲?+ 鐔卞ザ鑼?(98, 9, 1), (98, 24, 1), (98, 38, 1), (98, 15, 1), -- 姘寸叜鐗涜倝 + 楹昏荆娴疯渿 + 妾告闆?+ 钁¤悇鐑忛緧鑼?(99, 10, 1), (99, 25, 1), (99, 34, 1), (99, 13, 1), -- 榄氶鑼勫瓙 + 钂滈姣涜眴 + 瀹繚闆炰竵 + 绱呰眴鍐?(100, 1, 2), (100, 26, 1), (100, 35, 1), (100, 39, 2), -- 榛冪摐鑺眡2 + 钄彍鏄ュ嵎 + 绯栭唻閲岃剨 + 鍐拌渹铚滄妾瑇2
(101, 2, 1), (101, 27, 1), (101, 36, 1), (101, 40, 1), -- 鏈ㄨ€?+ 鍥涘窛杈ｈ姳鐢?+ 鍥涘窛娴烽涔鹃崑 + 铚滄鐑忛緧鑼?(102, 4, 1), (102, 23, 1), (102, 37, 1), (102, 41, 1), -- 閰歌彍榄氭汞 + 棣欒剢钘曠墖 + 绱呯噿鑲?+ 妞板ザ鏄?(103, 7, 1), (103, 24, 1), (103, 38, 1), (103, 12, 1), -- 鎿旀摂楹?+ 楹昏荆娴疯渿 + 妾告闆?+ 鍜告煚7
(104, 11, 2), (104, 31, 1), (104, 17, 2); -- 绯背绯晉2 + 鑺掓灉甯冧竵 + 鍑嶅ザ鑼秞2

-- 瑷傚柈105-116 (2025骞?2鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(105, 3, 1), (105, 25, 1), (105, 34, 1), (105, 18, 1), -- 鍙ｆ按闆?+ 钂滈姣涜眴 + 瀹繚闆炰竵 + 鍑嶆鑼?(106, 5, 1), (106, 26, 1), (106, 35, 1), (106, 14, 1), -- 閲嶆叾鐗涜倝 + 钄彍鏄ュ嵎 + 绯栭唻閲岃剨 + 鐔卞ザ鑼?(107, 6, 2), (107, 27, 1), (107, 36, 1), (107, 16, 2), -- 楹诲﹩璞嗚厫x2 + 鍥涘窛杈ｈ姳鐢?+ 鍥涘窛娴烽涔鹃崑 + 鐔辨鑼秞2
(108, 8, 1), (108, 28, 1), (108, 37, 1), (108, 13, 1), -- 鍥為崑鑲?+ 閰歌荆婀?+ 绱呯噿鑲?+ 绱呰眴鍐?(109, 9, 1), (109, 29, 1), (109, 38, 1), (109, 15, 1), -- 姘寸叜鐗涜倝 + 鍐摐婀?+ 妾告闆?+ 钁¤悇鐑忛緧鑼?(110, 10, 1), (110, 30, 1), (110, 34, 1), (110, 17, 1), -- 榄氶鑼勫瓙 + 鐜夌背锜硅倝婀?+ 瀹繚闆炰竵 + 鍑嶅ザ鑼?(111, 1, 2), (111, 23, 1), (111, 35, 1), (111, 39, 1), -- 榛冪摐鑺眡2 + 棣欒剢钘曠墖 + 绯栭唻閲岃剨 + 鍐拌渹铚滄妾?(112, 2, 1), (112, 24, 1), (112, 36, 1), (112, 40, 1), -- 鏈ㄨ€?+ 楹昏荆娴疯渿 + 鍥涘窛娴烽涔鹃崑 + 铚滄鐑忛緧鑼?(113, 4, 1), (113, 31, 1), (113, 37, 1), (113, 12, 1), -- 閰歌彍榄氭汞 + 鑺掓灉甯冧竵 + 绱呯噿鑲?+ 鍜告煚7
(114, 7, 1), (114, 32, 1), (114, 38, 1), (114, 18, 1), -- 鎿旀摂楹?+ 鑺濋夯鐞?+ 妾告闆?+ 鍑嶆鑼?(115, 11, 1), (115, 33, 1), (115, 14, 1), (115, 19, 1), -- 绯背绯?+ 铔嬫尀 + 鐔卞ザ鑼?+ 绫抽／
(116, 5, 1), (116, 25, 1), (116, 34, 1), (116, 17, 1); -- 閲嶆叾鐗涜倝 + 钂滈姣涜眴 + 瀹繚闆炰竵 + 鍑嶅ザ鑼?
-- 瑷傚柈117-128 (2026骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(117, 6, 1), (117, 26, 1), (117, 35, 1), (117, 16, 1), -- 楹诲﹩璞嗚厫 + 钄彍鏄ュ嵎 + 绯栭唻閲岃剨 + 鐔辨鑼?(118, 8, 1), (118, 27, 1), (118, 36, 1), (118, 13, 1), -- 鍥為崑鑲?+ 鍥涘窛杈ｈ姳鐢?+ 鍥涘窛娴烽涔鹃崑 + 绱呰眴鍐?(119, 9, 1), (119, 28, 1), (119, 37, 1), (119, 15, 1), -- 姘寸叜鐗涜倝 + 閰歌荆婀?+ 绱呯噿鑲?+ 钁¤悇鐑忛緧鑼?(120, 10, 1), (120, 29, 1), (120, 38, 1), (120, 39, 1), -- 榄氶鑼勫瓙 + 鍐摐婀?+ 妾告闆?+ 鍐拌渹铚滄妾?(121, 1, 1), (121, 30, 1), (121, 34, 1), (121, 40, 1), -- 榛冪摐鑺?+ 鐜夌背锜硅倝婀?+ 瀹繚闆炰竵 + 铚滄鐑忛緧鑼?(122, 2, 1), (122, 31, 1), (122, 35, 1), (122, 41, 1), -- 鏈ㄨ€?+ 鑺掓灉甯冧竵 + 绯栭唻閲岃剨 + 妞板ザ鏄?(123, 3, 1), (123, 32, 1), (123, 36, 1), (123, 12, 1), -- 鍙ｆ按闆?+ 鑺濋夯鐞?+ 鍥涘窛娴烽涔鹃崑 + 鍜告煚7
(124, 4, 1), (124, 33, 1), (124, 37, 1), (124, 14, 1), -- 閰歌彍榄氭汞 + 铔嬫尀 + 绱呯噿鑲?+ 鐔卞ザ鑼?(125, 23, 2), (125, 28, 1), (125, 38, 1), (125, 17, 2), -- 棣欒剢钘曠墖x2 + 閰歌荆婀?+ 妾告闆?+ 鍑嶅ザ鑼秞2
(126, 24, 1), (126, 29, 1), (126, 34, 1), (126, 18, 1), -- 楹昏荆娴疯渿 + 鍐摐婀?+ 瀹繚闆炰竵 + 鍑嶆鑼?(127, 25, 1), (127, 30, 1), (127, 35, 1), (127, 16, 1), -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 绯栭唻閲岃剨 + 鐔辨鑼?(128, 26, 1), (128, 31, 1), (128, 36, 1), (128, 39, 1); -- 钄彍鏄ュ嵎 + 鑺掓灉甯冧竵 + 鍥涘窛娴烽涔鹃崑 + 鍐拌渹铚滄妾?
-- =================================================================
-- 闈炴渻鍝￠姺鍞暩鎿?(Walk-in Customer) - 绗笁鎵?-- 72绛嗚▊鍠紙鍓?0绛?寰?2绛嗭級锛屾檪闁撶瘎鍦嶏細2025骞?鏈?鏃?- 2026骞?鏈?8鏃?-- 浣跨敤cid=0 (Walk-in Customer)
-- oid 绡勫湇锛?29锝?00
-- =================================================================


-- =================================================================
-- 鎻掑叆72绛嗛潪鏈冨摗瑷傚柈
-- =================================================================

-- 2025骞?鏈堥潪鏈冨摗瑷傚柈 (10绛?  oid 129-138
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

-- 2025骞?0鏈堥潪鏈冨摗瑷傚柈 (10绛?  oid 139-148
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

-- 2025骞?1鏈堥潪鏈冨摗瑷傚柈 (10绛?  oid 149-158
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

-- 2025骞?2鏈堥潪鏈冨摗瑷傚柈 (10绛?  oid 159-168
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

-- 2026骞?鏈堥潪鏈冨摗瑷傚柈 (5绛?  oid 169-173
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(169, '2026-01-03 14:45:00', 0, 3, 'walkin_20260103_091', NULL, 'takeaway', NULL),
(170, '2026-01-06 16:10:00', 0, 3, 'walkin_20260106_092', NULL, 'dine_in', 12),
(171, '2026-01-09 18:45:00', 0, 3, 'walkin_20260109_093', NULL, 'dine_in', 25),
(172, '2026-01-12 19:35:00', 0, 3, 'walkin_20260112_094', NULL, 'dine_in', 9),
(173, '2026-01-15 15:00:00', 0, 3, 'walkin_20260115_095', NULL, 'takeaway', NULL);

-- 2026骞?鏈堥潪鏈冨摗瑷傚柈 (5绛?  oid 174-178
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(174, '2026-02-01 16:20:00', 0, 3, 'walkin_20260201_096', NULL, 'dine_in', 16),
(175, '2026-02-04 18:55:00', 0, 3, 'walkin_20260204_097', NULL, 'dine_in', 7),
(176, '2026-02-07 19:45:00', 0, 3, 'walkin_20260207_098', NULL, 'dine_in', 19),
(177, '2026-02-10 15:20:00', 0, 3, 'walkin_20260210_099', NULL, 'takeaway', NULL),
(178, '2026-02-13 16:45:00', 0, 3, 'walkin_20260213_100', NULL, 'dine_in', 22);

-- 鍐嶅鍔?2绛嗚純绨″柈鐨勮▊鍠紙179-200锛?INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
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
-- 鎻掑叆闈炴渻鍝¤▊鍠爡鐩?-- 瑷伙細瑷傚柈oid寰?29闁嬪锛堝凡鏈夎▊鍠埌128锛?-- =================================================================

-- 瑷傚柈129-138 (2025骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(129, 1, 1), (129, 19, 1), (129, 12, 1),  -- 榛冪摐鑺?+ 绫抽／ + 鍜告煚7
(130, 2, 1), (130, 20, 1), (130, 17, 1),  -- 鏈ㄨ€?+ 楹?+ 鍑嶅ザ鑼?(131, 3, 1), (131, 19, 1), (131, 14, 1),  -- 鍙ｆ按闆?+ 绫抽／ + 鐔卞ザ鑼?(132, 4, 1), (132, 21, 1), (132, 18, 1),  -- 閰歌彍榄氭汞 + 钖矇 + 鍑嶆鑼?(133, 5, 1), (133, 19, 1), (133, 13, 1),  -- 閲嶆叾鐗涜倝 + 绫抽／ + 绱呰眴鍐?(134, 6, 2), (134, 20, 2), (134, 16, 2),  -- 楹诲﹩璞嗚厫x2 + 楹祒2 + 鐔辨鑼秞2
(135, 7, 1), (135, 19, 1), (135, 15, 1),  -- 鎿旀摂楹?+ 绫抽／ + 钁¤悇鐑忛緧鑼?(136, 8, 1), (136, 19, 1), (136, 17, 1),  -- 鍥為崑鑲?+ 绫抽／ + 鍑嶅ザ鑼?(137, 9, 1), (137, 21, 1), (137, 12, 1),  -- 姘寸叜鐗涜倝 + 钖矇 + 鍜告煚7
(138, 10, 1), (138, 19, 1), (138, 14, 1); -- 榄氶鑼勫瓙 + 绫抽／ + 鐔卞ザ鑼?
-- 瑷傚柈139-148 (2025骞?0鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(139, 23, 1), (139, 28, 1), (139, 34, 1), (139, 39, 1),  -- 棣欒剢钘曠墖 + 閰歌荆婀?+ 瀹繚闆炰竵 + 鍐拌渹铚滄妾?(140, 24, 1), (140, 29, 1), (140, 35, 1), (140, 40, 1),  -- 楹昏荆娴疯渿 + 鍐摐婀?+ 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?(141, 25, 1), (141, 30, 1), (141, 36, 1), (141, 41, 1),  -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?(142, 26, 1), (142, 28, 1), (142, 37, 1), (142, 12, 1),  -- 钄彍鏄ュ嵎 + 閰歌荆婀?+ 绱呯噿鑲?+ 鍜告煚7
(143, 27, 2), (143, 29, 1), (143, 38, 1), (143, 17, 1),  -- 鍥涘窛杈ｈ姳鐢焫2 + 鍐摐婀?+ 妾告闆?+ 鍑嶅ザ鑼?(144, 23, 1), (144, 30, 1), (144, 34, 1), (144, 18, 1),  -- 棣欒剢钘曠墖 + 鐜夌背锜硅倝婀?+ 瀹繚闆炰竵 + 鍑嶆鑼?(145, 24, 1), (145, 28, 1), (145, 35, 1), (145, 13, 1),  -- 楹昏荆娴疯渿 + 閰歌荆婀?+ 绯栭唻閲岃剨 + 绱呰眴鍐?(146, 25, 1), (146, 29, 1), (146, 36, 1), (146, 14, 1),  -- 钂滈姣涜眴 + 鍐摐婀?+ 鍥涘窛娴烽涔鹃崑 + 鐔卞ザ鑼?(147, 26, 1), (147, 30, 1), (147, 37, 1), (147, 15, 1),  -- 钄彍鏄ュ嵎 + 鐜夌背锜硅倝婀?+ 绱呯噿鑲?+ 钁¤悇鐑忛緧鑼?(148, 27, 1), (148, 28, 1), (148, 38, 1), (148, 16, 1);  -- 鍥涘窛杈ｈ姳鐢?+ 閰歌荆婀?+ 妾告闆?+ 鐔辨鑼?
-- 瑷傚柈149-158 (2025骞?1鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(149, 31, 1), (149, 34, 1), (149, 39, 1), (149, 19, 1),  -- 鑺掓灉甯冧竵 + 瀹繚闆炰竵 + 鍐拌渹铚滄妾?+ 绫抽／
(150, 32, 1), (150, 35, 1), (150, 40, 1), (150, 19, 1),  -- 鑺濋夯鐞?+ 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?+ 绫抽／
(151, 33, 1), (151, 36, 1), (151, 41, 1), (151, 20, 1),  -- 铔嬫尀 + 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?+ 楹?(152, 31, 2), (152, 37, 1), (152, 12, 2), (152, 19, 1),  -- 鑺掓灉甯冧竵x2 + 绱呯噿鑲?+ 鍜告煚7x2 + 绫抽／
(153, 32, 1), (153, 38, 1), (153, 17, 1), (153, 20, 1),  -- 鑺濋夯鐞?+ 妾告闆?+ 鍑嶅ザ鑼?+ 楹?(154, 33, 1), (154, 34, 1), (154, 18, 1), (154, 19, 1),  -- 铔嬫尀 + 瀹繚闆炰竵 + 鍑嶆鑼?+ 绫抽／
(155, 31, 1), (155, 35, 1), (155, 13, 1), (155, 19, 1),  -- 鑺掓灉甯冧竵 + 绯栭唻閲岃剨 + 绱呰眴鍐?+ 绫抽／
(156, 32, 1), (156, 36, 1), (156, 14, 1), (156, 20, 1),  -- 鑺濋夯鐞?+ 鍥涘窛娴烽涔鹃崑 + 鐔卞ザ鑼?+ 楹?(157, 33, 1), (157, 37, 1), (157, 15, 1), (157, 19, 1),  -- 铔嬫尀 + 绱呯噿鑲?+ 钁¤悇鐑忛緧鑼?+ 绫抽／
(158, 31, 1), (158, 38, 1), (158, 16, 1), (158, 20, 1);  -- 鑺掓灉甯冧竵 + 妾告闆?+ 鐔辨鑼?+ 楹?
-- 瑷傚柈159-168 (2025骞?2鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(159, 1, 1), (159, 3, 1), (159, 12, 2), (159, 19, 2),  -- 榛冪摐鑺?+ 鍙ｆ按闆?+ 鍜告煚7x2 + 绫抽／x2
(160, 2, 1), (160, 5, 1), (160, 17, 1), (160, 19, 1),  -- 鏈ㄨ€?+ 閲嶆叾鐗涜倝 + 鍑嶅ザ鑼?+ 绫抽／
(161, 4, 1), (161, 6, 1), (161, 18, 1), (161, 21, 1),  -- 閰歌彍榄氭汞 + 楹诲﹩璞嗚厫 + 鍑嶆鑼?+ 钖矇
(162, 7, 1), (162, 8, 1), (162, 13, 1), (162, 20, 1),  -- 鎿旀摂楹?+ 鍥為崑鑲?+ 绱呰眴鍐?+ 楹?(163, 9, 1), (163, 10, 1), (163, 14, 2), (163, 19, 2),  -- 姘寸叜鐗涜倝 + 榄氶鑼勫瓙 + 鐔卞ザ鑼秞2 + 绫抽／x2
(164, 23, 1), (164, 24, 1), (164, 15, 1), (164, 16, 1),  -- 棣欒剢钘曠墖 + 楹昏荆娴疯渿 + 钁¤悇鐑忛緧鑼?+ 鐔辨鑼?(165, 25, 1), (165, 26, 1), (165, 17, 1), (165, 18, 1),  -- 钂滈姣涜眴 + 钄彍鏄ュ嵎 + 鍑嶅ザ鑼?+ 鍑嶆鑼?(166, 27, 2), (166, 28, 1), (166, 12, 1), (166, 13, 1),  -- 鍥涘窛杈ｈ姳鐢焫2 + 閰歌荆婀?+ 鍜告煚7 + 绱呰眴鍐?(167, 29, 1), (167, 30, 1), (167, 14, 1), (167, 15, 1),  -- 鍐摐婀?+ 鐜夌背锜硅倝婀?+ 鐔卞ザ鑼?+ 钁¤悇鐑忛緧鑼?(168, 31, 1), (168, 32, 1), (168, 16, 1), (168, 17, 1);  -- 鑺掓灉甯冧竵 + 鑺濋夯鐞?+ 鐔辨鑼?+ 鍑嶅ザ鑼?
-- 瑷傚柈169-173 (2026骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(169, 33, 1), (169, 34, 1), (169, 18, 1), (169, 19, 1),  -- 铔嬫尀 + 瀹繚闆炰竵 + 鍑嶆鑼?+ 绫抽／
(170, 35, 1), (170, 36, 1), (170, 39, 1), (170, 20, 1),  -- 绯栭唻閲岃剨 + 鍥涘窛娴烽涔鹃崑 + 鍐拌渹铚滄妾?+ 楹?(171, 37, 1), (171, 38, 1), (171, 40, 1), (171, 19, 1),  -- 绱呯噿鑲?+ 妾告闆?+ 铚滄鐑忛緧鑼?+ 绫抽／
(172, 34, 1), (172, 35, 1), (172, 41, 1), (172, 20, 1),  -- 瀹繚闆炰竵 + 绯栭唻閲岃剨 + 妞板ザ鏄?+ 楹?(173, 36, 1), (173, 37, 1), (173, 12, 1), (173, 13, 1);  -- 鍥涘窛娴烽涔鹃崑 + 绱呯噿鑲?+ 鍜告煚7 + 绱呰眴鍐?
-- 瑷傚柈174-178 (2026骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(174, 38, 1), (174, 23, 1), (174, 14, 1), (174, 15, 1),  -- 妾告闆?+ 棣欒剢钘曠墖 + 鐔卞ザ鑼?+ 钁¤悇鐑忛緧鑼?(175, 24, 1), (175, 25, 1), (175, 16, 1), (175, 17, 1),  -- 楹昏荆娴疯渿 + 钂滈姣涜眴 + 鐔辨鑼?+ 鍑嶅ザ鑼?(176, 26, 1), (176, 27, 1), (176, 18, 1), (176, 19, 1),  -- 钄彍鏄ュ嵎 + 鍥涘窛杈ｈ姳鐢?+ 鍑嶆鑼?+ 绫抽／
(177, 28, 1), (177, 29, 1), (177, 39, 1), (177, 40, 1),  -- 閰歌荆婀?+ 鍐摐婀?+ 鍐拌渹铚滄妾?+ 铚滄鐑忛緧鑼?(178, 30, 1), (178, 31, 1), (178, 41, 1), (178, 19, 1);  -- 鐜夌背锜硅倝婀?+ 鑺掓灉甯冧竵 + 妞板ザ鏄?+ 绫抽／

-- 瑷傚柈179-200 (寰?2绛?
INSERT INTO order_items (oid, item_id, qty) VALUES
(179, 1, 1), (179, 12, 1),  -- 榛冪摐鑺?+ 鍜告煚7
(180, 2, 1), (180, 17, 1),  -- 鏈ㄨ€?+ 鍑嶅ザ鑼?(181, 3, 1), (181, 14, 1),  -- 鍙ｆ按闆?+ 鐔卞ザ鑼?(182, 4, 1), (182, 18, 1),  -- 閰歌彍榄氭汞 + 鍑嶆鑼?(183, 5, 1), (183, 13, 1),  -- 閲嶆叾鐗涜倝 + 绱呰眴鍐?(184, 6, 1), (184, 16, 1),  -- 楹诲﹩璞嗚厫 + 鐔辨鑼?(185, 7, 1), (185, 15, 1),  -- 鎿旀摂楹?+ 钁¤悇鐑忛緧鑼?(186, 8, 1), (186, 17, 1),  -- 鍥為崑鑲?+ 鍑嶅ザ鑼?(187, 9, 1), (187, 12, 1),  -- 姘寸叜鐗涜倝 + 鍜告煚7
(188, 10, 1), (188, 14, 1), -- 榄氶鑼勫瓙 + 鐔卞ザ鑼?(189, 23, 1), (189, 34, 1), (189, 39, 1),  -- 棣欒剢钘曠墖 + 瀹繚闆炰竵 + 鍐拌渹铚滄妾?(190, 24, 1), (190, 35, 1), (190, 40, 1),  -- 楹昏荆娴疯渿 + 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?(191, 25, 1), (191, 36, 1), (191, 41, 1),  -- 钂滈姣涜眴 + 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?(192, 26, 1), (192, 37, 1), (192, 12, 1),  -- 钄彍鏄ュ嵎 + 绱呯噿鑲?+ 鍜告煚7
(193, 27, 1), (193, 38, 1), (193, 17, 1),  -- 鍥涘窛杈ｈ姳鐢?+ 妾告闆?+ 鍑嶅ザ鑼?(194, 31, 1), (194, 34, 1), (194, 39, 1),  -- 鑺掓灉甯冧竵 + 瀹繚闆炰竵 + 鍐拌渹铚滄妾?(195, 32, 1), (195, 35, 1), (195, 40, 1),  -- 鑺濋夯鐞?+ 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?(196, 33, 1), (196, 36, 1), (196, 41, 1),  -- 铔嬫尀 + 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?(197, 23, 1), (197, 28, 1), (197, 17, 1),  -- 棣欒剢钘曠墖 + 閰歌荆婀?+ 鍑嶅ザ鑼?(198, 24, 1), (198, 29, 1), (198, 18, 1),  -- 楹昏荆娴疯渿 + 鍐摐婀?+ 鍑嶆鑼?(199, 25, 1), (199, 30, 1), (199, 13, 1),  -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 绱呰眴鍐?(200, 26, 1), (200, 28, 1), (200, 14, 1);  -- 钄彍鏄ュ嵎 + 閰歌荆婀?+ 鐔卞ザ鑼?
-- =================================================================
-- 閵峰敭鏁告摎4.txt - 鐐烘柊澧?鍚嶆渻鍝″鍔?0浠借▊鍠?-- 鏅傞枔绡勫湇锛?025骞?0鏈?- 2026骞?鏈?-- 浣跨敤cid=6-12 (鏂版渻鍝?
-- oid 绡勫湇锛?01锝?50
-- =================================================================


-- =================================================================
-- 鎻掑叆50绛嗘渻鍝¤▊鍠?-- =================================================================

-- 2025骞?0鏈堟渻鍝¤▊鍠?(10绛?  oid 201-210
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

-- 2025骞?1鏈堟渻鍝¤▊鍠?(10绛?  oid 211-220
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

-- 2025骞?2鏈堟渻鍝¤▊鍠?(10绛?  oid 221-230
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

-- 2026骞?鏈堟渻鍝¤▊鍠?(10绛?  oid 231-240
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

-- 2026骞?鏈堟渻鍝¤▊鍠?(10绛?  oid 241-250
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
-- 鎻掑叆鏈冨摗瑷傚柈闋呯洰
-- 瑷伙細瑷傚柈oid寰?01闁嬪锛堝凡鏈夎▊鍠埌200锛?-- =================================================================

-- 瑷傚柈201-210 (2025骞?0鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(201, 3, 1), (201, 12, 2), (201, 19, 2),  -- 鍙ｆ按闆?+ 鍜告煚7x2 + 绫抽／x2
(202, 5, 1), (202, 17, 1), (202, 20, 1),  -- 閲嶆叾鐗涜倝 + 鍑嶅ザ鑼?+ 楹?(203, 6, 1), (203, 14, 2), (203, 19, 2),  -- 楹诲﹩璞嗚厫 + 鐔卞ザ鑼秞2 + 绫抽／x2
(204, 8, 1), (204, 15, 1), (204, 19, 1),  -- 鍥為崑鑲?+ 钁¤悇鐑忛緧鑼?+ 绫抽／
(205, 9, 2), (205, 18, 2), (205, 21, 2),  -- 姘寸叜鐗涜倝x2 + 鍑嶆鑼秞2 + 钖矇x2
(206, 10, 1), (206, 13, 1), (206, 19, 1),  -- 榄氶鑼勫瓙 + 绱呰眴鍐?+ 绫抽／
(207, 1, 2), (207, 16, 2), (207, 19, 2),  -- 榛冪摐鑺眡2 + 鐔辨鑼秞2 + 绫抽／x2
(208, 2, 1), (208, 12, 1), (208, 20, 1),  -- 鏈ㄨ€?+ 鍜告煚7 + 楹?(209, 4, 1), (209, 17, 1), (209, 19, 1),  -- 閰歌彍榄氭汞 + 鍑嶅ザ鑼?+ 绫抽／
(210, 7, 1), (210, 14, 1), (210, 20, 1);  -- 鎿旀摂楹?+ 鐔卞ザ鑼?+ 楹?
-- 瑷傚柈211-220 (2025骞?1鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(211, 23, 1), (211, 28, 1), (211, 34, 1), (211, 39, 1),  -- 棣欒剢钘曠墖 + 閰歌荆婀?+ 瀹繚闆炰竵 + 鍐拌渹铚滄妾?(212, 24, 1), (212, 29, 1), (212, 35, 1), (212, 40, 1),  -- 楹昏荆娴疯渿 + 鍐摐婀?+ 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?(213, 25, 1), (213, 30, 1), (213, 36, 1), (213, 41, 1),  -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?(214, 26, 1), (214, 31, 1), (214, 37, 1), (214, 12, 1),  -- 钄彍鏄ュ嵎 + 鑺掓灉甯冧竵 + 绱呯噿鑲?+ 鍜告煚7
(215, 27, 1), (215, 32, 1), (215, 38, 1), (215, 17, 1),  -- 鍥涘窛杈ｈ姳鐢?+ 鑺濋夯鐞?+ 妾告闆?+ 鍑嶅ザ鑼?(216, 23, 2), (216, 28, 1), (216, 34, 2), (216, 39, 2),  -- 棣欒剢钘曠墖x2 + 閰歌荆婀?+ 瀹繚闆炰竵x2 + 鍐拌渹铚滄妾瑇2
(217, 24, 1), (217, 29, 1), (217, 35, 1), (217, 40, 1),  -- 楹昏荆娴疯渿 + 鍐摐婀?+ 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?(218, 25, 1), (218, 30, 1), (218, 36, 1), (218, 41, 1),  -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?(219, 26, 2), (219, 31, 1), (219, 37, 2), (219, 12, 2),  -- 钄彍鏄ュ嵎x2 + 鑺掓灉甯冧竵 + 绱呯噿鑲墄2 + 鍜告煚7x2
(220, 27, 1), (220, 32, 1), (220, 38, 1), (220, 17, 1);  -- 鍥涘窛杈ｈ姳鐢?+ 鑺濋夯鐞?+ 妾告闆?+ 鍑嶅ザ鑼?
-- 瑷傚柈221-230 (2025骞?2鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(221, 3, 1), (221, 25, 1), (221, 34, 1), (221, 18, 1),  -- 鍙ｆ按闆?+ 钂滈姣涜眴 + 瀹繚闆炰竵 + 鍑嶆鑼?(222, 5, 1), (222, 26, 1), (222, 35, 1), (222, 14, 1),  -- 閲嶆叾鐗涜倝 + 钄彍鏄ュ嵎 + 绯栭唻閲岃剨 + 鐔卞ザ鑼?(223, 6, 2), (223, 27, 1), (223, 36, 1), (223, 16, 2),  -- 楹诲﹩璞嗚厫x2 + 鍥涘窛杈ｈ姳鐢?+ 鍥涘窛娴烽涔鹃崑 + 鐔辨鑼秞2
(224, 8, 1), (224, 28, 1), (224, 37, 1), (224, 13, 1),  -- 鍥為崑鑲?+ 閰歌荆婀?+ 绱呯噿鑲?+ 绱呰眴鍐?(225, 9, 1), (225, 29, 1), (225, 38, 1), (225, 15, 1),  -- 姘寸叜鐗涜倝 + 鍐摐婀?+ 妾告闆?+ 钁¤悇鐑忛緧鑼?(226, 10, 1), (226, 30, 1), (226, 34, 1), (226, 17, 1),  -- 榄氶鑼勫瓙 + 鐜夌背锜硅倝婀?+ 瀹繚闆炰竵 + 鍑嶅ザ鑼?(227, 1, 2), (227, 23, 1), (227, 35, 1), (227, 39, 1),  -- 榛冪摐鑺眡2 + 棣欒剢钘曠墖 + 绯栭唻閲岃剨 + 鍐拌渹铚滄妾?(228, 2, 1), (228, 24, 1), (228, 36, 1), (228, 40, 1),  -- 鏈ㄨ€?+ 楹昏荆娴疯渿 + 鍥涘窛娴烽涔鹃崑 + 铚滄鐑忛緧鑼?(229, 4, 1), (229, 31, 1), (229, 37, 1), (229, 12, 1),  -- 閰歌彍榄氭汞 + 鑺掓灉甯冧竵 + 绱呯噿鑲?+ 鍜告煚7
(230, 7, 1), (230, 32, 1), (230, 38, 1), (230, 18, 1);  -- 鎿旀摂楹?+ 鑺濋夯鐞?+ 妾告闆?+ 鍑嶆鑼?
-- 瑷傚柈231-240 (2026骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(231, 11, 1), (231, 33, 1), (231, 14, 1), (231, 19, 1),  -- 绯背绯?+ 铔嬫尀 + 鐔卞ザ鑼?+ 绫抽／
(232, 5, 1), (232, 25, 1), (232, 34, 1), (232, 17, 1),  -- 閲嶆叾鐗涜倝 + 钂滈姣涜眴 + 瀹繚闆炰竵 + 鍑嶅ザ鑼?(233, 6, 1), (233, 26, 1), (233, 35, 1), (233, 16, 1),  -- 楹诲﹩璞嗚厫 + 钄彍鏄ュ嵎 + 绯栭唻閲岃剨 + 鐔辨鑼?(234, 8, 1), (234, 27, 1), (234, 36, 1), (234, 13, 1),  -- 鍥為崑鑲?+ 鍥涘窛杈ｈ姳鐢?+ 鍥涘窛娴烽涔鹃崑 + 绱呰眴鍐?(235, 9, 1), (235, 28, 1), (235, 37, 1), (235, 15, 1),  -- 姘寸叜鐗涜倝 + 閰歌荆婀?+ 绱呯噿鑲?+ 钁¤悇鐑忛緧鑼?(236, 10, 1), (236, 29, 1), (236, 38, 1), (236, 39, 1),  -- 榄氶鑼勫瓙 + 鍐摐婀?+ 妾告闆?+ 鍐拌渹铚滄妾?(237, 1, 1), (237, 30, 1), (237, 34, 1), (237, 40, 1),  -- 榛冪摐鑺?+ 鐜夌背锜硅倝婀?+ 瀹繚闆炰竵 + 铚滄鐑忛緧鑼?(238, 2, 1), (238, 31, 1), (238, 35, 1), (238, 41, 1),  -- 鏈ㄨ€?+ 鑺掓灉甯冧竵 + 绯栭唻閲岃剨 + 妞板ザ鏄?(239, 3, 1), (239, 32, 1), (239, 36, 1), (239, 12, 1),  -- 鍙ｆ按闆?+ 鑺濋夯鐞?+ 鍥涘窛娴烽涔鹃崑 + 鍜告煚7
(240, 4, 1), (240, 33, 1), (240, 37, 1), (240, 14, 1);  -- 閰歌彍榄氭汞 + 铔嬫尀 + 绱呯噿鑲?+ 鐔卞ザ鑼?
-- 瑷傚柈241-250 (2026骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(241, 23, 2), (241, 28, 1), (241, 38, 1), (241, 17, 2),  -- 棣欒剢钘曠墖x2 + 閰歌荆婀?+ 妾告闆?+ 鍑嶅ザ鑼秞2
(242, 24, 1), (242, 29, 1), (242, 34, 1), (242, 18, 1),  -- 楹昏荆娴疯渿 + 鍐摐婀?+ 瀹繚闆炰竵 + 鍑嶆鑼?(243, 25, 1), (243, 30, 1), (243, 35, 1), (243, 16, 1),  -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 绯栭唻閲岃剨 + 鐔辨鑼?(244, 26, 1), (244, 31, 1), (244, 36, 1), (244, 39, 1),  -- 钄彍鏄ュ嵎 + 鑺掓灉甯冧竵 + 鍥涘窛娴烽涔鹃崑 + 鍐拌渹铚滄妾?(245, 27, 1), (245, 32, 1), (245, 37, 1), (245, 40, 1),  -- 鍥涘窛杈ｈ姳鐢?+ 鑺濋夯鐞?+ 绱呯噿鑲?+ 铚滄鐑忛緧鑼?(246, 23, 1), (246, 33, 1), (246, 38, 1), (246, 41, 1),  -- 棣欒剢钘曠墖 + 铔嬫尀 + 妾告闆?+ 妞板ザ鏄?(247, 24, 2), (247, 28, 1), (247, 34, 2), (247, 12, 2),  -- 楹昏荆娴疯渿x2 + 閰歌荆婀?+ 瀹繚闆炰竵x2 + 鍜告煚7x2
(248, 25, 1), (248, 29, 1), (248, 35, 1), (248, 14, 1),  -- 钂滈姣涜眴 + 鍐摐婀?+ 绯栭唻閲岃剨 + 鐔卞ザ鑼?(249, 26, 2), (249, 30, 1), (249, 36, 2), (249, 15, 2),  -- 钄彍鏄ュ嵎x2 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑x2 + 钁¤悇鐑忛緧鑼秞2
(250, 27, 1), (250, 31, 1), (250, 37, 1), (250, 16, 1);  -- 鍥涘窛杈ｈ姳鐢?+ 鑺掓灉甯冧竵 + 绱呯噿鑲?+ 鐔辨鑼?
-- =================================================================
-- 鍎儬鍒镐娇鐢ㄨ閷?-- =================================================================

INSERT INTO order_coupons (oid, coupon_id, discount_amount) VALUES
-- 10鏈堣▊鍠劒鎯犲埜
(201, 1, 18.20),  -- 10% off
(203, 2, 26.00),  -- Free drink (item 14)
(205, 3, 50.00),  -- HK$50 off
(207, 1, 15.80),  -- 10% off
(209, 2, 28.00),  -- Free drink (item 17)

-- 11鏈堣▊鍠劒鎯犲埜
(211, 3, 50.00),  -- HK$50 off
(213, 1, 20.40),  -- 10% off
(215, 2, 30.00),  -- Free drink (item 41)
(217, 3, 50.00),  -- HK$50 off
(219, 1, 22.60),  -- 10% off

-- 12鏈堣▊鍠劒鎯犲埜
(221, 2, 26.00),  -- Free drink (item 18)
(223, 3, 50.00),  -- HK$50 off
(225, 1, 19.80),  -- 10% off
(227, 2, 28.00),  -- Free drink (item 39)
(229, 3, 50.00),  -- HK$50 off

-- 1鏈堣▊鍠劒鎯犲埜
(231, 1, 17.40),  -- 10% off
(233, 2, 24.00),  -- Free drink (item 16)
(235, 3, 50.00),  -- HK$50 off
(237, 1, 21.20),  -- 10% off
(239, 2, 26.00),  -- Free drink (item 12)

-- 2鏈堣▊鍠劒鎯犲埜
(241, 3, 50.00),  -- HK$50 off
(243, 1, 23.60),  -- 10% off
(245, 2, 30.00),  -- Free drink (item 40)
(247, 3, 50.00),  -- HK$50 off
(249, 1, 25.80);  -- 10% off

-- =================================================================
-- 鏇存柊鏈冨摗绌嶅垎
-- =================================================================

-- 瑷堢畻姣忎綅鏈冨摗鐨勭附娑堣不涓﹀垎閰嶇鍒嗭紙姣忔秷璨籋K$1 = 1绌嶅垎锛?-- 鎴戝€戝厛鏇存柊椤у琛ㄤ腑鐨刢oupon_point瀛楁

-- 闄冲ぇ鏄?(cid=6) - 闋愯▓绱勬秷璨籋K$1200
UPDATE customer SET coupon_point = coupon_point + 1200 WHERE cid = 6;

-- 鏉庣編鐜?(cid=7) - 闋愯▓绱勬秷璨籋K$1050
UPDATE customer SET coupon_point = coupon_point + 1050 WHERE cid = 7;

-- 寮靛亯寮?(cid=8) - 闋愯▓绱勬秷璨籋K$1350
UPDATE customer SET coupon_point = coupon_point + 1350 WHERE cid = 8;

-- 鐜嬫泬闆?(cid=9) - 闋愯▓绱勬秷璨籋K$1150
UPDATE customer SET coupon_point = coupon_point + 1150 WHERE cid = 9;

-- 鍔夊杓?(cid=10) - 闋愯▓绱勬秷璨籋K$1250
UPDATE customer SET coupon_point = coupon_point + 1250 WHERE cid = 10;

-- 榛冨織鍋?(cid=11) - 闋愯▓绱勬秷璨籋K$1100
UPDATE customer SET coupon_point = coupon_point + 1100 WHERE cid = 11;

-- 鏋楃鏂?(cid=12) - 闋愯▓绱勬秷璨籋K$1300
UPDATE customer SET coupon_point = coupon_point + 1300 WHERE cid = 12;

-- =================================================================
-- 鎻掑叆绌嶅垎姝峰彶瑷橀寗
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
(1,  'zh-TW', '绱犻'),
(2,  'zh-TW', '娓呯埥'),
(3,  'zh-TW', '闆炶倝'),
(4,  'zh-TW', '鍑嶉２'),
(5,  'zh-TW', '杈?),
(6,  'zh-TW', '榄氳倝'),
(7,  'zh-TW', '閰?),
(8,  'zh-TW', '鐗涜倝'),
(9,  'zh-TW', '楹?),
(10, 'zh-TW', '璞嗚厫'),
(11, 'zh-TW', '楹垫'),
(12, 'zh-TW', '璞倝'),
(13, 'zh-TW', '琛楅牠灏忓悆'),
(14, 'zh-TW', '鐐?),
(15, 'zh-TW', '缍撳吀'),
(16, 'zh-TW', '鐢?),
(17, 'zh-TW', '绯?),
(18, 'zh-TW', '妾告'),
(19, 'zh-TW', '钁¤悇'),
(20, 'zh-TW', '濂?),
(21, 'zh-TW', '姘ｆ场姘?),
(22, 'zh-TW', '鍌崇当');

-- Simplified Chinese (zh-CN)
INSERT INTO tag_translation (tag_id, language_code, tag_name) VALUES
(1,  'zh-CN', '绱犻'),
(2,  'zh-CN', '娓呯埥'),
(3,  'zh-CN', '楦¤倝'),
(4,  'zh-CN', '鍐烽ギ'),
(5,  'zh-CN', '杈?),
(6,  'zh-CN', '楸艰倝'),
(7,  'zh-CN', '閰?),
(8,  'zh-CN', '鐗涜倝'),
(9,  'zh-CN', '楹?),
(10, 'zh-CN', '璞嗚厫'),
(11, 'zh-CN', '闈㈡潯'),
(12, 'zh-CN', '鐚倝'),
(13, 'zh-CN', '琛楀ご灏忓悆'),
(14, 'zh-CN', '鐐?),
(15, 'zh-CN', '缁忓吀'),
(16, 'zh-CN', '鐢?),
(17, 'zh-CN', '绯?),
(18, 'zh-CN', '鏌犳'),
(19, 'zh-CN', '钁¤悇'),
(20, 'zh-CN', '濂?),
(21, 'zh-CN', '姘旀场姘?),
(22, 'zh-CN', '浼犵粺');

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
(1, 'zh-TW', '杈ｅ害'),
(2, 'zh-TW', '鐢滃害'),
(3, 'zh-TW', '鍐伴噺'),
(4, 'zh-TW', '濂堕噺'),
(5, 'zh-TW', '閰嶆枡');

-- Simplified Chinese (zh-CN)
INSERT INTO customization_option_group_translation (group_id, language_code, group_name) VALUES
(1, 'zh-CN', '杈ｅ害'),
(2, 'zh-CN', '鐢滃害'),
(3, 'zh-CN', '鍐伴噺'),
(4, 'zh-CN', '濂堕噺'),
(5, 'zh-CN', '閰嶆枡');

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
(1,  'zh-TW', '寰荆'),
(2,  'zh-TW', '涓荆'),
(3,  'zh-TW', '杈?),
(4,  'zh-TW', '楹昏荆'),
-- Sugar Level
(5,  'zh-TW', '澶氱硸'),
(6,  'zh-TW', '灏戠硸'),
(7,  'zh-TW', '鐒＄硸'),
-- Ice Level
(8,  'zh-TW', '澶氬啺'),
(9,  'zh-TW', '灏戝啺'),
(10, 'zh-TW', '鐒″啺'),
-- Milk Level
(11, 'zh-TW', '澶氬ザ'),
(12, 'zh-TW', '灏戝ザ'),
(13, 'zh-TW', '鐒″ザ'),
-- Toppings
(14, 'zh-TW', '鍔犺姖楹?),
(15, 'zh-TW', '鑺辩敓'),
(16, 'zh-TW', '铚傝湝娣嬮啲'),
(17, 'zh-TW', '鏈卞彜鍔涚矑');

-- Simplified Chinese (zh-CN)
INSERT INTO customization_option_value_translation (value_id, language_code, value_name) VALUES
-- Spice Level
(1,  'zh-CN', '寰荆'),
(2,  'zh-CN', '涓荆'),
(3,  'zh-CN', '杈?),
(4,  'zh-CN', '楹昏荆'),
-- Sugar Level
(5,  'zh-CN', '澶氱硸'),
(6,  'zh-CN', '灏戠硸'),
(7,  'zh-CN', '鏃犵硸'),
-- Ice Level
(8,  'zh-CN', '澶氬啺'),
(9,  'zh-CN', '灏戝啺'),
(10, 'zh-CN', '鏃犲啺'),
-- Milk Level
(11, 'zh-CN', '澶氬ザ'),
(12, 'zh-CN', '灏戝ザ'),
(13, 'zh-CN', '鏃犲ザ'),
-- Toppings
(14, 'zh-CN', '鍔犺姖楹?),
(15, 'zh-CN', '鑺辩敓'),
(16, 'zh-CN', '铚傝湝娣嬮叡'),
(17, 'zh-CN', '宸у厠鍔涚矑');


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
(1, 'zh-TW', '涓嶈荆'),
(2, 'zh-TW', '寰荆'),
(3, 'zh-TW', '涓荆'),
(4, 'zh-TW', '杈?),
(5, 'zh-TW', '楹昏荆');

-- Simplified Chinese (zh-CN)
INSERT INTO spice_level_translation (spice_id, language_code, spice_name) VALUES
(1, 'zh-CN', '涓嶈荆'),
(2, 'zh-CN', '寰荆'),
(3, 'zh-CN', '涓荆'),
(4, 'zh-CN', '杈?),
(5, 'zh-CN', '楹昏荆');

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

-- Idempotent customer insert: safe to run multiple times without duplicate-key errors
INSERT INTO customer (cid, cname, cpassword, ctel, caddr, company, cemail, cbirthday, crole, cimageurl, coupon_point) VALUES
(0, 'Walk-in Customer', 'walkin', NULL, NULL, NULL, 'walkin@system.local', NULL, 'customer', NULL, 0),
(1, 'Alex Wong', 'password', 21232123, 'G/F, ABC Building, King Yip Street, KwunTong, Kowloon, Hong Kong', 'Fat Cat Company Limited', 'alex.wong@example.com', NULL, 'customer', NULL, 0),
(2, 'Tina Chan', 'password', 31233123, '303, Mei Hing Center, Yuen Long, NT, Hong Kong', 'XDD LOL Company', 'tina.chan@example.com', '07-20', 'customer', NULL, 0),
(3, 'Bowie', 'password', 61236123, '401, Sing Kei Building, Kowloon, Hong Kong', 'GPA4 Company', 'bowie@example.com', '03-15', 'customer', NULL, 0),
(4, 'Samuel Lee', 'samuelpass', 61231212, '111, Example Road, Central, Hong Kong', 'Lee Family Co', 'samuel.lee@example.com', '11-02', 'customer', NULL, 0),
(5, 'Emily Tsang', 'emilypass', 61231555, '88, Happy Valley Road, Hong Kong', 'Happy Valley Enterprises', 'emily.tsang@example.com', '01-30', 'customer', NULL, 0),
(6, 'David Chan', 'password123', 91234567, '30 Canton Road, Tsim Sha Tsui, Kowloon, Hong Kong', 'Eastern Trading Company', 'david.chan@example.com', '05-18', 'customer', NULL, 0),
(7, 'Mary Li', 'meiling2025', 92345678, '12/F, Tower 1, Times Square, Causeway Bay, Hong Kong', 'MayLin Design Studio', 'mary.li@example.com', '08-22', 'customer', NULL, 0),
(8, 'John Zhang', 'zhangwq789', 93456789, '88 Des Voeux Road Central, Central, Hong Kong', 'StrongTech Solutions', 'john.zhang@example.com', '11-05', 'customer', NULL, 0),
(9, 'Sarah Wang', 'xiaowen888', 94567890, '200 Hennessy Road, Wan Chai, Hong Kong', 'Creative Culture Media', 'sarah.wang@example.com', '02-14', 'customer', NULL, 0),
(10, 'Kevin Liu', 'liujiahui66', 95678901, '55 Hoi Yuen Road, Kwun Tong, Hong Kong', 'Kevin Logistics Ltd.', 'kevin.liu@example.com', '09-09', 'customer', NULL, 0),
(11, 'Michael Wong', 'michael2025', 96789012, '9 Queen''s Road East, Admiralty, Hong Kong', 'Harbour Finance Group', 'michael.wong@example.com', '12-03', 'customer', NULL, 0),
(12, 'Susan Lam', 'susanlam88', 97890123, '28 Johnston Road, Wan Chai, Hong Kong', 'Blue Peak Consulting', 'susan.lam@example.com', '04-27', 'customer', NULL, 0),
(13, 'Test Wong', 'password', 93456789, 'Demo booking customer', NULL, 'test.wong@example.com', NULL, 'customer', NULL, 0)
ON DUPLICATE KEY UPDATE
cname = VALUES(cname),
cpassword = VALUES(cpassword),
ctel = VALUES(ctel),
caddr = VALUES(caddr),
company = VALUES(company),
cemail = VALUES(cemail),
cbirthday = VALUES(cbirthday),
crole = VALUES(crole),
cimageurl = VALUES(cimageurl),
coupon_point = VALUES(coupon_point);

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
(1, 'zh-CN', '鍏ㄥ崟涔濇姌', '涓嬫娑堣垂鍙韩鍙椾節鎶樹紭鎯犮€?),
(1, 'zh-TW', '鍏ㄥ柈涔濇姌', '涓嬫娑堣不鍙韩鍙椾節鎶樺劒鎯犮€?),
(2, 'en', 'Free Drink', 'Redeem one free drink of your choice.'),
(2, 'zh-CN', '鍏嶈垂楗搧', '鍏戞崲涓€鏉偍閫夋嫨鐨勫厤璐归ギ鍝併€?),
(2, 'zh-TW', '鍏嶈不椋插搧', '鍏屾彌涓€鏉偍閬告搰鐨勫厤璨婚２鍝併€?),
(3, 'en', 'HK$50 OFF', 'Enjoy HK$50 off when you spend HK$300 or more.'),
(3, 'zh-CN', '绔嬪噺50娓厓', '娑堣垂婊?00娓厓鍗冲彲鍑?0娓厓銆?),
(3, 'zh-TW', '绔嬫笡50娓厓', '娑堣不婊?00娓厓鍗冲彲娓?0娓厓銆?),
(4, 'en', 'Birthday Special', 'Exclusive coupon for your birthday month.'),
(4, 'zh-CN', '鐢熸棩鐗规儬', '鐢熸棩鏈堜唤涓撳睘浼樻儬鍒搞€?),
(4, 'zh-TW', '鐢熸棩鐗规儬', '鐢熸棩鏈堜唤灏堝爆鍎儬鍒搞€?);

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
(1, 'zh-CN', '閫傜敤浜庡爞椋熷拰澶栧崠'),
(1, 'zh-CN', '涓嶉€傜敤浜庡閫佹湇鍔?),
(1, 'zh-CN', '涓嶅彲涓庡叾浠栦紭鎯犲悓鏃朵娇鐢?),
(1, 'zh-TW', '閬╃敤鏂煎爞椋熷拰澶栬常'),
(1, 'zh-TW', '涓嶉仼鐢ㄦ柤澶栭€佹湇鍕?),
(1, 'zh-TW', '涓嶅彲鑸囧叾浠栧劒鎯犲悓鏅備娇鐢?),
(2, 'en', 'Choice of soft drink, coffee, or tea'),
(2, 'en', 'Limit one free drink per customer per day'),
(2, 'zh-CN', '鍙€夋嫨姹芥按銆佸挅鍟℃垨鑼?),
(2, 'zh-CN', '姣忎綅椤惧姣忓ぉ闄愬厬涓€鏉?),
(2, 'zh-TW', '鍙伕鎿囨苯姘淬€佸挅鍟℃垨鑼?),
(2, 'zh-TW', '姣忎綅椤у姣忓ぉ闄愬厡涓€鏉?),
(3, 'en', 'Minimum spend of HK$300 required'),
(3, 'en', 'Discount applied before service charge'),
(3, 'zh-CN', '闇€婊?00娓厓鏂瑰彲浣跨敤'),
(3, 'zh-CN', '鎶樻墸鍦ㄥ姞鏀舵湇鍔¤垂鍓嶈绠?),
(3, 'zh-TW', '闇€婊?00娓厓鏂瑰彲浣跨敤'),
(3, 'zh-TW', '鎶樻墸鏂煎姞鏀舵湇鍕欒不鍓嶈▓绠?),
(4, 'en', 'Valid only during your birthday month'),
(4, 'en', 'Must present valid ID for verification'),
(4, 'zh-CN', '浠呴檺鐢熸棩鏈堜唤浣跨敤'),
(4, 'zh-CN', '闇€鍑虹ず鏈夋晥韬唤璇佹槑'),
(4, 'zh-TW', '鍍呴檺鐢熸棩鏈堜唤浣跨敤'),
(4, 'zh-TW', '闇€鍑虹ず鏈夋晥韬唤璀夋槑'),
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
(1, 'zh-TW', '鍦栫墖鍙緵鍙冭€冿紝瀵﹂殯渚涙噳鍙兘鏈夋墍涓嶅悓'),
(2, 'zh-TW', '鍦栫墖鍙緵鍙冭€冿紝瀵﹂殯渚涙噳鍙兘鏈夋墍涓嶅悓'),
(3, 'zh-TW', '鍦栫墖鍙緵鍙冭€冿紝瀵﹂殯渚涙噳鍙兘鏈夋墍涓嶅悓'),
(4, 'zh-TW', '鍦栫墖鍙緵鍙冭€冿紝瀵﹂殯渚涙噳鍙兘鏈夋墍涓嶅悓'),
(1, 'zh-TW', '鍎儬鍒镐笉鍙厡鎻涚従閲戙€佷俊鐢ㄩ鎴栧叾浠栫敘鍝?),
(2, 'zh-TW', '鍎儬鍒镐笉鍙厡鎻涚従閲戙€佷俊鐢ㄩ鎴栧叾浠栫敘鍝?),
(3, 'zh-TW', '鍎儬鍒镐笉鍙厡鎻涚従閲戙€佷俊鐢ㄩ鎴栧叾浠栫敘鍝?),
(4, 'zh-TW', '鍎儬鍒镐笉鍙厡鎻涚従閲戙€佷俊鐢ㄩ鎴栧叾浠栫敘鍝?),
(1, 'zh-TW', 'Yummy Restaurant 淇濈暀闅ㄦ檪鍙栨秷銆佹洿鏀规垨淇▊姊濇鍙婄窗鍓囦箣娆婂埄锛屾仌涓嶅彟琛岄€氱煡'),
(2, 'zh-TW', 'Yummy Restaurant 淇濈暀闅ㄦ檪鍙栨秷銆佹洿鏀规垨淇▊姊濇鍙婄窗鍓囦箣娆婂埄锛屾仌涓嶅彟琛岄€氱煡'),
(3, 'zh-TW', 'Yummy Restaurant 淇濈暀闅ㄦ檪鍙栨秷銆佹洿鏀规垨淇▊姊濇鍙婄窗鍓囦箣娆婂埄锛屾仌涓嶅彟琛岄€氱煡'),
(4, 'zh-TW', 'Yummy Restaurant 淇濈暀闅ㄦ檪鍙栨秷銆佹洿鏀规垨淇▊姊濇鍙婄窗鍓囦箣娆婂埄锛屾仌涓嶅彟琛岄€氱煡'),
(1, 'zh-TW', '濡傛湁鐢㈠搧缂鸿波锛屽叕鍙稿彲鏇存彌鐐哄悓绛夋垨鏇撮珮鍍瑰€间箣椋熷搧'),
(2, 'zh-TW', '濡傛湁鐢㈠搧缂鸿波锛屽叕鍙稿彲鏇存彌鐐哄悓绛夋垨鏇撮珮鍍瑰€间箣椋熷搧'),
(3, 'zh-TW', '濡傛湁鐢㈠搧缂鸿波锛屽叕鍙稿彲鏇存彌鐐哄悓绛夋垨鏇撮珮鍍瑰€间箣椋熷搧'),
(4, 'zh-TW', '濡傛湁鐢㈠搧缂鸿波锛屽叕鍙稿彲鏇存彌鐐哄悓绛夋垨鏇撮珮鍍瑰€间箣椋熷搧'),
(1, 'zh-CN', '鍥剧墖浠呬緵鍙傝€冿紝瀹為檯渚涘簲鍙兘鏈夋墍涓嶅悓'),
(2, 'zh-CN', '鍥剧墖浠呬緵鍙傝€冿紝瀹為檯渚涘簲鍙兘鏈夋墍涓嶅悓'),
(3, 'zh-CN', '鍥剧墖浠呬緵鍙傝€冿紝瀹為檯渚涘簲鍙兘鏈夋墍涓嶅悓'),
(4, 'zh-CN', '鍥剧墖浠呬緵鍙傝€冿紝瀹為檯渚涘簲鍙兘鏈夋墍涓嶅悓'),
(1, 'zh-CN', '浼樻儬鍒镐笉鍙厬鎹㈢幇閲戙€佷俊鐢ㄩ鎴栧叾浠栦骇鍝?),
(2, 'zh-CN', '浼樻儬鍒镐笉鍙厬鎹㈢幇閲戙€佷俊鐢ㄩ鎴栧叾浠栦骇鍝?),
(3, 'zh-CN', '浼樻儬鍒镐笉鍙厬鎹㈢幇閲戙€佷俊鐢ㄩ鎴栧叾浠栦骇鍝?),
(4, 'zh-CN', '浼樻儬鍒镐笉鍙厬鎹㈢幇閲戙€佷俊鐢ㄩ鎴栧叾浠栦骇鍝?),
(1, 'zh-CN', 'Yummy Restaurant 淇濈暀闅忔椂鍙栨秷銆佹洿鏀规垨淇鏉℃鍙婄粏鍒欑殑鏉冨埄锛屾仌涓嶅彟琛岄€氱煡'),
(2, 'zh-CN', 'Yummy Restaurant 淇濈暀闅忔椂鍙栨秷銆佹洿鏀规垨淇鏉℃鍙婄粏鍒欑殑鏉冨埄锛屾仌涓嶅彟琛岄€氱煡'),
(3, 'zh-CN', 'Yummy Restaurant 淇濈暀闅忔椂鍙栨秷銆佹洿鏀规垨淇鏉℃鍙婄粏鍒欑殑鏉冨埄锛屾仌涓嶅彟琛岄€氱煡'),
(4, 'zh-CN', 'Yummy Restaurant 淇濈暀闅忔椂鍙栨秷銆佹洿鏀规垨淇鏉℃鍙婄粏鍒欑殑鏉冨埄锛屾仌涓嶅彟琛岄€氱煡'),
(1, 'zh-CN', '濡傛湁浜у搧缂鸿揣锛屽叕鍙稿彲鏇存崲涓哄悓绛夋垨鏇撮珮浠峰€肩殑椋熷搧'),
(2, 'zh-CN', '濡傛湁浜у搧缂鸿揣锛屽叕鍙稿彲鏇存崲涓哄悓绛夋垨鏇撮珮浠峰€肩殑椋熷搧'),
(3, 'zh-CN', '濡傛湁浜у搧缂鸿揣锛屽叕鍙稿彲鏇存崲涓哄悓绛夋垨鏇撮珮浠峰€肩殑椋熷搧'),
(4, 'zh-CN', '濡傛湁浜у搧缂鸿揣锛屽叕鍙稿彲鏇存崲涓哄悓绛夋垨鏇撮珮浠峰€肩殑椋熷搧');

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
(1, 'zh-CN', '鑵屽埗榛勭摐鑺?, '鐢ㄩ鏂欒厡鍒剁殑榛勭摐鑺憋紝娓呯埥鍙彛銆?),
(1, 'zh-TW', '閱冭＝榛冪摐鑺?, '浠ラ鏂欓唭瑁界殑榛冪摐鑺憋紝娓呮柊鐖藉彛銆?),
(2, 'en', 'Spicy Wood Ear Mushrooms', 'Black fungus tossed in vinegar, garlic, and chili oil.'),
(2, 'zh-CN', '楹昏荆鏈ㄨ€?, '榛戞湪鑰虫媽閱嬨€佽挏鍜岃荆娌癸紝鐖藉彛寮€鑳冦€?),
(2, 'zh-TW', '楹昏荆鏈ㄨ€?, '榛戞湪鑰虫媽閱嬨€佽挏鑸囪荆娌癸紝鐖藉彛闁嬭儍銆?),
(3, 'en', 'Mouthwatering Chicken', 'Poached chicken drenched in spicy Sichuan chili sauce.'),
(3, 'zh-CN', '鍙ｆ按楦?, '瀚╅浮娴告场鍦ㄩ夯杈ｇ孩娌逛腑锛岄杈ｈ浜恒€?),
(3, 'zh-TW', '鍙ｆ按闆?, '瀚╅洖娴告场鍦ㄩ夯杈ｇ磪娌逛腑锛岄杈ｈ獦浜恒€?),
(4, 'en', 'Suan Cai Fish Soup', 'Sliced fish simmered in pickled mustard greens and chili broth.'),
(4, 'zh-CN', '閰歌彍楸兼堡', '楸肩墖鐐栭吀鑿滃拰杈ｆ堡锛岄吀杈ｅ紑鑳冦€?),
(4, 'zh-TW', '閰歌彍榄氭汞', '榄氱墖鐕夐吀鑿滆垏杈ｆ汞锛岄吀杈ｉ枊鑳冦€?),
(5, 'en', 'Chongqing-style Angus Beef', 'Spicy Angus beef with bean paste and lemongrass.'),
(5, 'zh-CN', '閲嶅簡椋庡懗瀹夋牸鏂墰鑲?, '杈ｅ懗瀹夋牸鏂墰鑲夐厤璞嗙摚閰卞拰棣欒寘锛岄夯杈ｆ寔涔呫€?),
(5, 'zh-TW', '閲嶆叾棰ㄥ懗瀹夋牸鏂墰鑲?, '杈ｅ懗瀹夋牸鏂墰鑲夋惌閰嶈眴鐡ｉ啲鑸囬鑼咃紝楹昏荆鎸佷箙銆?),
(6, 'en', 'Mapo Tofu', 'Silken tofu in spicy bean paste sauce with minced beef and Sichuan peppercorns.'),
(6, 'zh-CN', '楹诲﹩璞嗚厫', '瀚╄眴鑵愰厤鐗涜倝鏈拰楹昏荆璞嗙摚閰憋紝椋庡懗鍗佽冻銆?),
(6, 'zh-TW', '楹诲﹩璞嗚厫', '瀚╄眴鑵愭惌閰嶇墰鑲夋湯鑸囬夯杈ｈ眴鐡ｉ啲锛岄ⅷ鍛冲崄瓒炽€?),
(7, 'en', 'Dan Dan Noodles', 'Spicy noodles topped with minced pork, preserved vegetables, and chili oil.'),
(7, 'zh-CN', '鎷呮媴闈?, '杈ｅ懗闈㈡潯閰嶇尓鑲夋湯銆佽娊鑿滃拰绾㈡补锛岄杈ｈ浜恒€?),
(7, 'zh-TW', '鎿旀摂楹?, '杈ｅ懗楹垫鎼厤璞倝鏈€佽娊鑿滆垏绱呮补锛岄杈ｈ獦浜恒€?),
(8, 'en', 'Twice-Cooked Pork', 'Pork belly simmered then stir-fried with leeks and chili bean paste.'),
(8, 'zh-CN', '鍥為攨鑲?, '浜旇姳鑲夊厛鐓悗鐐掞紝鎼厤钂滆嫍鍜岃眴鐡ｉ叡锛岄娴撳彲鍙ｃ€?),
(8, 'zh-TW', '鍥為崑鑲?, '浜旇姳鑲夊厛鐓緦鐐掞紝鎼厤钂滆嫍鑸囪眴鐡ｉ啲锛岄婵冨彲鍙ｃ€?),
(9, 'en', 'Boiled Beef in Chili Broth', 'Tender beef slices in a fiery broth with Sichuan peppercorns.'),
(9, 'zh-CN', '姘寸叜鐗涜倝', '鐗涜倝鐗囨蹈娉″湪楹昏荆绾㈡堡涓紝棣欒荆杩囩樉銆?),
(9, 'zh-TW', '姘寸叜鐗涜倝', '鐗涜倝鐗囨蹈娉″湪楹昏荆绱呮汞涓紝棣欒荆閬庣櫘銆?),
(10, 'en', 'Fish-Fragrant Eggplant', 'Braised eggplant in garlic, ginger, and sweet chili sauce.'),
(10, 'zh-CN', '楸奸鑼勫瓙', '鑼勫瓙鐐栫叜浜庤挏濮滃拰鐢滆荆閰变腑锛岄姘旀墤榧汇€?),
(10, 'zh-TW', '榄氶鑼勫瓙', '鑼勫瓙鐕夌叜鏂艰挏钖戣垏鐢滆荆閱腑锛岄姘ｆ挷榧汇€?),
(11, 'en', 'Sichuan Glutinous Rice Cake', 'Sticky rice cake with brown sugar and sesame.'),
(11, 'zh-CN', '鍥涘窛绯背绯?, '绯背绯曢厤绾㈢硸鍜岃姖楹伙紝鐢滆€屼笉鑵汇€?),
(11, 'zh-TW', '鍥涘窛绯背绯?, '绯背绯曟惌閰嶇磪绯栬垏鑺濋夯锛岀敎鑰屼笉鑶┿€?),
(12, 'en', 'Salty Lemon 7-Up', 'Classic Hong Kong salty lemon soda with 7-Up.'),
(12, 'zh-CN', '鍜告煚7', '娓紡缁忓吀鍜告煚涓冨枩锛屾竻鐖借В娓淬€?),
(12, 'zh-TW', '鍜告7', '娓紡缍撳吀楣规涓冨枩锛屾竻鐖借В娓淬€?),
(13, 'en', 'Red Bean Ice', 'Sweet red beans served over crushed ice.'),
(13, 'zh-CN', '绾㈣眴鍐?, '棣欑敎绾㈣眴閰嶄笂纰庡啺锛屽鏃ュ繀澶囥€?),
(13, 'zh-TW', '绱呰眴鍐?, '棣欑敎绱呰眴閰嶄笂纰庡啺锛屽鏃ュ繀鍌欍€?),
(14, 'en', 'Hot Milk Tea', 'Rich Hong Kong-style milk tea, best served hot.'),
(14, 'zh-CN', '鐑ザ鑼?, '娴撻儊娓紡濂惰尪锛岀儹楗渶浣炽€?),
(14, 'zh-TW', '鐔卞ザ鑼?, '婵冮儊娓紡濂惰尪锛岀啽椋叉渶浣炽€?),
(15, 'en', 'Grape Oolong Tea', 'Oolong tea infused with grape aroma, refreshing and unique.'),
(15, 'zh-CN', '钁¤悇涔岄緳鑼?, '涔岄緳鑼惰瀺鍚堣憽钀勯姘旓紝娓呮柊鐙壒銆?),
(15, 'zh-TW', '钁¤悇鐑忛緧鑼?, '鐑忛緧鑼惰瀺鍚堣憽钀勯姘ｏ紝娓呮柊鐛ㄧ壒銆?),
(16, 'en', 'Hot Lemon Tea', 'Hot lemon tea, tangy and comforting.'),
(16, 'zh-CN', '鐑煚鑼?, '鐑煚妾尪锛岄吀鐢滄殩蹇冦€?),
(16, 'zh-TW', '鐔辨鑼?, '鐔辨妾尪锛岄吀鐢滄殩蹇冦€?),
(17, 'en', 'Iced Milk Tea', 'Classic Hong Kong-style milk tea, served chilled.'),
(17, 'zh-CN', '鍐诲ザ鑼?, '缁忓吀娓紡濂惰尪锛屽啺鍑夌埥鍙ｃ€?),
(17, 'zh-TW', '鍑嶅ザ鑼?, '缍撳吀娓紡濂惰尪锛屽啺娑肩埥鍙ｃ€?),
(18, 'en', 'Iced Lemon Tea', 'Crisp iced tea with fresh lemon slices.'),
(18, 'zh-CN', '鍐绘煚鑼?, '鍐伴晣鏌犳鑼讹紝娓呯埥瑙ｆ复銆?),
(18, 'zh-TW', '鍑嶆鑼?, '鍐伴幃妾告鑼讹紝娓呯埥瑙ｆ复銆?),
(19, 'en', 'Steamed Rice', 'Fluffy steamed rice, perfect as a staple side dish.'),
(19, 'zh-CN', '绫抽キ', '钃澗鐨勮捀绫抽キ锛屽畬缇庣殑涓婚閰嶈彍銆?),
(19, 'zh-TW', '绫抽／', '钃瑔鐨勮捀绫抽／锛屽畬缇庣殑涓婚閰嶈彍銆?),
(20, 'en', 'Noodles', 'Soft and tender wheat noodles.'),
(20, 'zh-CN', '楹?, '杞€屽鐨勫皬楹﹂潰鏉°€?),
(20, 'zh-TW', '楹?, '杌熻€屽鐨勫皬楹ラ旱姊濄€?),
(21, 'en', 'Potato Starch', 'Smooth and creamy potato starch dish.'),
(21, 'zh-CN', '钖矇', '鍏夋粦缁嗚吇鐨勮柉绮夐鍝併€?),
(21, 'zh-TW', '钖矇', '鍏夋粦绱拌啯鐨勮柉绮夐鍝併€?);

INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(22, 'en', 'Wooden Chopsticks', 'Disposable wooden chopsticks for takeaway.'),
(22, 'zh-CN', '鏈ㄧ', '涓€娆℃€ф湪绛凤紝浠呴檺澶栧崠浣跨敤銆?),
(22, 'zh-TW', '鏈ㄧ', '涓€娆℃€ф湪绛凤紝鍍呴檺澶栧付浣跨敤銆?);

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

-- Demo cash payment orders for presentation
INSERT INTO orders (odate, cid, ostatus, note, orderRef, coupon_id, order_type, table_number, payment_method) VALUES
(NOW(), 1, 0, 'Demo pending cash order for Table #20', 'DEMO-CASH-T20-SEED', NULL, 'dine_in', 20, 'cash'),
(NOW(), 13, 0, 'Demo pending cash order for Table #15', 'DEMO-CASH-T15-SEED', NULL, 'dine_in', 15, 'cash'),
(NOW(), 2, 1, 'Demo paid cash order for Table #21', 'DEMO-CASH-PAID-T21-SEED', NULL, 'dine_in', 21, 'cash');


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
(5, 16, 2),
-- Order 6: Demo pending cash order for Table #20
(6, 1, 2),
(6, 4, 1),
-- Order 7: Demo pending cash order for Table #15
(7, 2, 1),
(7, 5, 1),
(7, 3, 2),
-- Order 8: Demo paid cash order for Table #21
(8, 6, 1),
(8, 3, 1);

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

-- Table layout coordinates from seating layout
-- Format: capacity, status, x_position, y_position
INSERT INTO seatingChart (capacity, status, x_position, y_position) VALUES
(2, 0, 10, 10),
(2, 0, 20, 10),
(2, 0, 30, 10),
(2, 0, 40, 10),
(2, 0, 50, 10),
(2, 0, 60, 10),
(2, 0, 70, 10),
(2, 0, 80, 10),
(2, 0, 90, 10),
(2, 0, 10, 25),
(4, 0, 20, 25),
(4, 0, 30, 25),
(4, 0, 40, 25),
(4, 0, 50, 25),
(4, 0, 60, 25),
(4, 0, 70, 25),
(4, 0, 80, 25),
(4, 0, 90, 25),
(4, 0, 10, 40),
(4, 0, 20, 40),
(4, 0, 30, 40),
(4, 0, 40, 40),
(4, 0, 50, 40),
(4, 0, 60, 40),
(4, 0, 70, 40),
(4, 0, 80, 40),
(4, 0, 90, 40),
(8, 0, 20, 55),
(8, 0, 45, 55),
(8, 0, 75, 55),
(2, 0, 10, 70),
(2, 0, 20, 70),
(2, 0, 30, 70),
(2, 0, 40, 70),
(2, 0, 50, 70),
(2, 0, 60, 70),
(2, 0, 70, 70),
(2, 0, 80, 70),
(2, 0, 90, 70),
(4, 0, 20, 85),
(4, 0, 35, 85),
(4, 0, 50, 85),
(4, 0, 65, 85),
(4, 0, 80, 85),
(8, 0, 10, 55),
(8, 0, 40, 70),
(8, 0, 65, 55),
(2, 0, 30, 55),
(2, 0, 55, 55),
(2, 0, 75, 55);

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
(5, 'Emily Tsang', 61231555, 11, '2024-01-19', '13:00:00', 4, 'Birthday Celebration', 'Will bring a cake', 3),
(1, 'Test Chan', 91234567, 1, CURDATE(), '12:30:00', 2, 'Lunch', 'seed by copilot', 1),
(2, 'Test Lee', 92345678, 11, CURDATE(), '18:30:00', 4, 'Family Dinner', 'window seat preferred', 1),
(3, 'Test Wong', 93456789, 15, CURDATE(), '19:00:00', 6, 'Birthday', 'cake service', 2);

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
(1, 'zh-CN', '鍓嶈彍'), (2, 'zh-CN', '涓昏彍'), (3, 'zh-CN', '楗枡'), (4, 'zh-CN', '涓婚'),
(5, 'zh-CN', '鍓嶈彍'), (6, 'zh-CN', '姹ゅ搧'), (7, 'zh-CN', '涓昏彍'), (8, 'zh-CN', '楗枡'), (9, 'zh-CN', '涓婚'),
(10, 'zh-CN', '涓昏彍'), (11, 'zh-CN', '楗枡'), (12, 'zh-CN', '涓婚'),
(1, 'zh-TW', '鍓嶈彍'), (2, 'zh-TW', '涓昏彍'), (3, 'zh-TW', '椋叉枡'), (4, 'zh-TW', '涓婚'),
(5, 'zh-TW', '鍓嶈彍'), (6, 'zh-TW', '婀搧'), (7, 'zh-TW', '涓昏彍'), (8, 'zh-TW', '椋叉枡'), (9, 'zh-TW', '涓婚'),
(10, 'zh-TW', '涓昏彍'), (11, 'zh-TW', '椋叉枡'), (12, 'zh-TW', '涓婚');

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

INSERT INTO materials (mname, category_id, unit, mqty, reorderLevel) VALUES
('Chili Powder', 3, 'grams', 80.00, 100.00),      -- 搴瓨80g锛屼綆鏂奸噸鏂拌▊璩兼按骞?00g
('Garlic', 1, 'grams', 800.00, 300.00),           -- 搴瓨800g锛岄珮鏂奸噸鏂拌▊璩兼按骞?00g
('Ginger', 1, 'grams', 60.00, 150.00),            -- 搴瓨60g锛屼綆鏂奸噸鏂拌▊璩兼按骞?50g
('Soy Sauce', 3, 'ml', 1200.00, 500.00),          -- 搴瓨1200ml锛岄珮鏂奸噸鏂拌▊璩兼按骞?00ml
('Rice Vinegar', 3, 'ml', 900.00, 400.00);        -- 搴瓨900ml锛岄珮鏂奸噸鏂拌▊璩兼按骞?00ml

-- 鏂板鏉愭枡鍒嗛锛堝鏋滈渶瑕侊級
-- 妾㈡煡鏄惁宸叉湁閫欎簺鍒嗛锛屽鏋滄矑鏈夊墖鎻掑叆
INSERT IGNORE INTO materials_category (category_name) VALUES
('Spice'),
('Vegetable'),
('Sauce');

-- 鏂板5浠紸ppetizers (category_id=1)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(1, 32.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/23.jpg', 2, TRUE),
(1, 30.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/24.jpg', 1, TRUE),
(1, 35.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/25.jpg', 3, TRUE),
(1, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/26.jpg', 0, TRUE),
(1, 34.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/27.jpg', 2, TRUE);

-- 鏂板3浠絊oup (category_id=2)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(2, 52.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/28.jpg', 1, TRUE),
(2, 56.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/29.jpg', 2, TRUE),
(2, 48.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/30.jpg', 0, TRUE);

-- 鏂板3浠紻essert (category_id=4)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(4, 25.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/31.jpg', 0, TRUE),
(4, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/32.jpg', 0, TRUE),
(4, 30.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/33.jpg', 0, TRUE);

-- 鏂板5浠組ain Courses (category_id=3)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(3, 98.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/34.jpg', 4, TRUE),
(3, 85.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/35.jpg', 3, TRUE),
(3, 92.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/36.jpg', 5, TRUE),
(3, 78.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/37.jpg', 2, TRUE),
(3, 88.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/38.jpg', 3, TRUE);

-- 鏂板3浠紻rink (category_id=5)
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, is_available) VALUES
(5, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/39.jpg', 0, TRUE),
(5, 24.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/40.jpg', 0, TRUE),
(5, 30.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/41.jpg', 0, TRUE);

-- =================================================================
-- 鏂板鑿滈鐨勫瑾炶█缈昏
-- =================================================================

-- 闁嬭儍鑿?(Appetizers) 缈昏
-- Item 23
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(23, 'en', 'Crispy Lotus Root', 'Thinly sliced lotus root, deep-fried to perfection with a hint of sesame oil.'),
(23, 'zh-CN', '棣欒剢钘曠墖', '钖勫垏钘曠墖锛岀偢鑷抽噾榛勯叆鑴嗭紝甯︽湁鑺濋夯娌归姘斻€?),
(23, 'zh-TW', '棣欒剢钘曠墖', '钖勫垏钘曠墖锛岀偢鑷抽噾榛冮叆鑴嗭紝甯舵湁鑺濋夯娌归姘ｃ€?);

-- Item 24
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(24, 'en', 'Spicy Jellyfish', 'Marinated jellyfish with chili oil and sesame, crispy and refreshing.'),
(24, 'zh-CN', '楹昏荆娴疯渿', '鐢ㄨ荆娌瑰拰鑺濋夯鑵屽埗鐨勬捣铚囷紝鐖借剢寮€鑳冦€?),
(24, 'zh-TW', '楹昏荆娴疯渿', '鐢ㄨ荆娌瑰拰鑺濋夯閱冭＝鐨勬捣铚囷紝鐖借剢闁嬭儍銆?);

-- Item 25
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(25, 'en', 'Garlic Edamame', 'Fresh edamame pods tossed in garlic butter and sea salt.'),
(25, 'zh-CN', '钂滈姣涜眴', '鏂伴矞姣涜眴鎷屽叆钂滈榛勬补鍜屾捣鐩愶紝棣欐皵鎵戦蓟銆?),
(25, 'zh-TW', '钂滈姣涜眴', '鏂伴姣涜眴鎷屽叆钂滈濂舵补鍜屾捣楣斤紝棣欐埃鎾查蓟銆?);

-- Item 26
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(26, 'en', 'Vegetable Spring Rolls', 'Crispy spring rolls filled with fresh vegetables and glass noodles.'),
(26, 'zh-CN', '钄彍鏄ュ嵎', '澶栫毊閰ヨ剢锛屽唴棣呮槸鏂伴矞钄彍鍜岀矇涓濄€?),
(26, 'zh-TW', '钄彍鏄ユ嵅', '澶栫毊閰ヨ剢锛屽収椁℃槸鏂伴钄彍鍜岀矇绲层€?);

-- Item 27
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(27, 'en', 'Sichuan Peanuts', 'Spicy peanuts stir-fried with Sichuan peppercorns and chili flakes.'),
(27, 'zh-CN', '鍥涘窛杈ｈ姳鐢?, '鐢ㄥ洓宸濊姳妞掑拰杈ｆ鐗囩倰鍒剁殑杈ｅ懗鑺辩敓銆?),
(27, 'zh-TW', '鍥涘窛杈ｈ姳鐢?, '鐢ㄥ洓宸濊姳妞掑拰杈ｆ鐗囩倰瑁界殑杈ｅ懗鑺辩敓銆?);

-- 婀搧 (Soup) 缈昏
-- Item 28
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(28, 'en', 'Hot & Sour Soup', 'Classic Sichuan hot and sour soup with tofu, mushrooms, and bamboo shoots.'),
(28, 'zh-CN', '閰歌荆姹?, '缁忓吀鐨勫洓宸濋吀杈ｆ堡锛屽唴鏈夎眴鑵愩€佽槕鑿囧拰绔圭瑡銆?),
(28, 'zh-TW', '閰歌荆婀?, '缍撳吀鐨勫洓宸濋吀杈ｆ汞锛屽収鏈夎眴鑵愩€佽槕鑿囧拰绔圭瓖銆?);

-- Item 29
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(29, 'en', 'Winter Melon Soup', 'Light and clear winter melon soup with goji berries and chicken broth.'),
(29, 'zh-CN', '鍐摐姹?, '娓呮贰鐨勫啲鐡滄堡锛屽姞鍏ユ灨鏉炲拰楦℃堡銆?),
(29, 'zh-TW', '鍐摐婀?, '娓呮贰鐨勫啲鐡滄汞锛屽姞鍏ユ灨鏉炲拰闆炴汞銆?);

-- Item 30
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(30, 'en', 'Corn and Crab Meat Soup', 'Creamy corn soup with fresh crab meat and egg white.'),
(30, 'zh-CN', '鐜夌背锜硅倝姹?, '濂舵补鐜夌背姹ゅ姞鍏ユ柊椴滆煿鑲夊拰铔嬬櫧銆?),
(30, 'zh-TW', '鐜夌背锜硅倝婀?, '濂舵补鐜夌背婀姞鍏ユ柊楫煿鑲夊拰铔嬬櫧銆?);

-- 鐢滃搧 (Dessert) 缈昏
-- Item 31
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(31, 'en', 'Mango Pudding', 'Fresh mango pudding topped with condensed milk and mango chunks.'),
(31, 'zh-CN', '鑺掓灉甯冧竵', '鏂伴矞鑺掓灉甯冧竵锛屾穻涓婄偧涔冲拰鑺掓灉鍧椼€?),
(31, 'zh-TW', '鑺掓灉甯冧竵', '鏂伴鑺掓灉甯冧竵锛屾穻涓婄厜涔冲拰鑺掓灉濉娿€?);

-- Item 32
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(32, 'en', 'Sesame Balls', 'Deep-fried glutinous rice balls filled with sweet red bean paste.'),
(32, 'zh-CN', '鑺濋夯鐞?, '娌圭偢绯背鐞冿紝鍐呴鏄敎绾㈣眴娌欍€?),
(32, 'zh-TW', '鑺濋夯鐞?, '娌圭偢绯背鐞冿紝鍏чぁ鏄敎绱呰眴娌欍€?);

-- Item 33
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(33, 'en', 'Egg Tart', 'Classic Hong Kong-style egg tarts with flaky pastry.'),
(33, 'zh-CN', '铔嬫尀', '缁忓吀娓紡铔嬫尀锛屽鐨叆鑴嗐€?),
(33, 'zh-TW', '铔嬫捇', '缍撳吀娓紡铔嬫捇锛屽鐨叆鑴嗐€?);

-- 涓昏彍 (Main Courses) 缈昏
-- Item 34
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(34, 'en', 'Kung Pao Chicken', 'Diced chicken stir-fried with peanuts, chili, and Sichuan peppercorns.'),
(34, 'zh-CN', '瀹繚楦′竵', '楦¤倝涓佺倰鑺辩敓銆佽荆妞掑拰鍥涘窛鑺辨銆?),
(34, 'zh-TW', '瀹繚闆炰竵', '闆炶倝涓佺倰鑺辩敓銆佽荆妞掑拰鍥涘窛鑺辨銆?);

-- Item 35
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(35, 'en', 'Sweet & Sour Pork', 'Crispy pork pieces in tangy sweet and sour sauce with pineapple.'),
(35, 'zh-CN', '绯栭唻閲岃剨', '閰ヨ剢鐨勭尓鑲夊潡瑁逛笂閰哥敎閰辨眮锛岄厤鑿犺悵銆?),
(35, 'zh-TW', '绯栭唻閲岃剨', '閰ヨ剢鐨勮爆鑲夊瑁逛笂閰哥敎閱眮锛岄厤槌虫ⅷ銆?);

-- Item 36
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(36, 'en', 'Sichuan Dry Pot with Seafood', 'Assorted seafood cooked in a spicy dry pot with vegetables.'),
(36, 'zh-CN', '鍥涘窛娴烽矞骞查攨', '澶氱娴烽矞涓庤敩鑿滃湪楹昏荆骞查攨涓児鍒躲€?),
(36, 'zh-TW', '鍥涘窛娴烽涔鹃崑', '澶氱ó娴烽鑸囪敩鑿滃湪楹昏荆涔鹃崑涓児瑁姐€?);

-- Item 37
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(37, 'en', 'Braised Pork Belly', 'Pork belly braised in soy sauce and spices until tender.'),
(37, 'zh-CN', '绾㈢儳鑲?, '浜旇姳鑲夌敤閰辨补鍜岄鏂欑倴鐓嚦杞銆?),
(37, 'zh-TW', '绱呯噿鑲?, '浜旇姳鑲夌敤閱补鍜岄鏂欑噳鐓嚦杌熷銆?);

-- Item 38
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(38, 'en', 'Lemon Chicken', 'Crispy chicken with lemon sauce, sweet and tangy.'),
(38, 'zh-CN', '鏌犳楦?, '閰ヨ剢楦¤倝閰嶆煚妾叡锛岄吀鐢滃彲鍙ｃ€?),
(38, 'zh-TW', '妾告闆?, '閰ヨ剢闆炶倝閰嶆妾啲锛岄吀鐢滃彲鍙ｃ€?);

-- 椋叉枡 (Drink) 缈昏
-- Item 39
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(39, 'en', 'Iced Honey Lemon', 'Refreshing iced drink with honey and fresh lemon slices.'),
(39, 'zh-CN', '鍐拌渹铚滄煚妾?, '铚傝湝鍜屾柊椴滄煚妾墖鍒舵垚鐨勫啺闀囬ギ鍝併€?),
(39, 'zh-TW', '鍐拌渹铚滄妾?, '铚傝湝鍜屾柊楫妾墖瑁芥垚鐨勫啺閹２鍝併€?);

-- Item 40
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(40, 'en', 'Peach Oolong Tea', 'Oolong tea infused with peach flavor, served hot or cold.'),
(40, 'zh-CN', '铚滄涔岄緳鑼?, '甯︽湁铚滄棣欐皵鐨勪箤榫欒尪锛屽彲鐑彲鍐般€?),
(40, 'zh-TW', '铚滄鐑忛緧鑼?, '甯舵湁铚滄棣欐埃鐨勭儚榫嶈尪锛屽彲鐔卞彲鍐般€?);

-- Item 41
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(41, 'en', 'Coconut Milkshake', 'Creamy coconut milkshake topped with shredded coconut.'),
(41, 'zh-CN', '妞板ザ鏄?, '娴撻儊鐨勬ぐ濂跺ザ鏄旓紝鎾掍笂妞颁笣銆?),
(41, 'zh-TW', '妞板ザ鏄?, '婵冮儊鐨勬ぐ濂跺ザ鏄旓紝鎾掍笂妞扮挡銆?);

-- =================================================================
-- 鏂板鑿滈鐨勬绫?-- =================================================================

-- 棣栧厛妾㈡煡鏄惁宸叉湁閫欎簺妯欑堡锛岃嫢鐒″墖鎻掑叆
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

-- 鐐烘柊澧炶彍椁氭坊鍔犳绫?-- 娉ㄦ剰锛氶€欒！浣跨敤SELECT瑾炲彞渚嗙嵅鍙栨绫D锛岀⒑淇濆紩鐢ㄦ纰?INSERT INTO menu_tag (item_id, tag_id) 
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
-- 鏂板鑿滈鐨勮嚜瀹氱京閬搁爡
-- =================================================================

-- 鐐烘柊鑿滈娣诲姞鑷畾缇╅伕闋?INSERT INTO item_customization_options (item_id, group_id, max_selections, is_required) VALUES
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
-- 閵峰敭鏁告摎 (2025骞?0鏈?鏃?- 2026骞?鏈?1鏃?
-- 鍏?0绛嗚▊鍠紝oid 绡勫湇 29锝?8
-- =================================================================


-- =================================================================
-- 2025骞?0鏈堣▊鍠?(12绛?
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
-- 2025骞?1鏈堣▊鍠?(13绛?
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
-- 2025骞?2鏈堣▊鍠?(13绛?
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
-- 2026骞?鏈堣▊鍠?(12绛?
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
-- 瑷傚柈闋呯洰 (鍖呭惈鍘熸湁鑿滈鍜屾柊鑿滈)
-- =================================================================
INSERT INTO order_items (oid, item_id, qty) VALUES
-- 瑷傚柈 29 (2025-10-02)
(29, 5, 1), (29, 12, 2), (29, 23, 1),
-- 瑷傚柈 30 (2025-10-03)
(30, 6, 1), (30, 17, 1), (30, 24, 2),
-- 瑷傚柈 31 (2025-10-05)
(31, 8, 1), (31, 14, 2), (31, 28, 1),
-- 瑷傚柈 32 (2025-10-07)
(32, 9, 2), (32, 15, 1), (32, 25, 1),
-- 瑷傚柈 33 (2025-10-09)
(33, 10, 1), (33, 16, 2), (33, 29, 1),
-- 瑷傚柈 34 (2025-10-11)
(34, 7, 1), (34, 18, 1), (34, 26, 1), (34, 31, 1),
-- 瑷傚柈 35 (2025-10-13)
(35, 5, 2), (35, 13, 1), (35, 27, 1),
-- 瑷傚柈 36 (2025-10-15)
(36, 6, 1), (36, 12, 2), (36, 30, 1),
-- 瑷傚柈 37 (2025-10-17)
(37, 8, 2), (37, 17, 1), (37, 32, 1),
-- 瑷傚柈 38 (2025-10-19)
(38, 9, 1), (38, 14, 2), (38, 33, 1),
-- 瑷傚柈 39 (2025-10-22)
(39, 10, 1), (39, 15, 1), (39, 34, 1),
-- 瑷傚柈 40 (2025-10-25)
(40, 7, 2), (40, 16, 1), (40, 35, 1),
-- 瑷傚柈 41 (2025-11-01)
(41, 5, 1), (41, 18, 2), (41, 36, 1),
-- 瑷傚柈 42 (2025-11-03)
(42, 6, 2), (42, 12, 1), (42, 37, 1),
-- 瑷傚柈 43 (2025-11-05)
(43, 8, 1), (43, 17, 1), (43, 38, 1),
-- 瑷傚柈 44 (2025-11-07)
(44, 9, 2), (44, 14, 1), (44, 23, 1), (44, 39, 2),
-- 瑷傚柈 45 (2025-11-09)
(45, 10, 1), (45, 15, 2), (45, 24, 1),
-- 瑷傚柈 46 (2025-11-12)
(46, 7, 1), (46, 16, 1), (46, 25, 1), (46, 40, 1),
-- 瑷傚柈 47 (2025-11-14)
(47, 5, 2), (47, 18, 1), (47, 26, 1),
-- 瑷傚柈 48 (2025-11-16)
(48, 6, 1), (48, 12, 2), (48, 27, 1),
-- 瑷傚柈 49 (2025-11-18)
(49, 8, 1), (49, 17, 1), (49, 28, 1), (49, 41, 1),
-- 瑷傚柈 50 (2025-11-21)
(50, 9, 2), (50, 14, 1), (50, 29, 1),
-- 瑷傚柈 51 (2025-11-23)
(51, 10, 1), (51, 15, 2), (51, 30, 1),
-- 瑷傚柈 52 (2025-11-25)
(52, 7, 1), (52, 16, 1), (52, 31, 1),
-- 瑷傚柈 53 (2025-11-28)
(53, 5, 2), (53, 18, 1), (53, 32, 1),
-- 瑷傚柈 54 (2025-12-01)
(54, 6, 1), (54, 12, 2), (54, 33, 1),
-- 瑷傚柈 55 (2025-12-03)
(55, 8, 1), (55, 17, 1), (55, 34, 1),
-- 瑷傚柈 56 (2025-12-05)
(56, 9, 2), (56, 14, 1), (56, 35, 1),
-- 瑷傚柈 57 (2025-12-07)
(57, 10, 1), (57, 15, 2), (57, 36, 1),
-- 瑷傚柈 58 (2025-12-09)
(58, 7, 1), (58, 16, 1), (58, 37, 1),
-- 瑷傚柈 59 (2025-12-11)
(59, 5, 2), (59, 18, 1), (59, 38, 1),
-- 瑷傚柈 60 (2025-12-13)
(60, 6, 1), (60, 12, 2), (60, 23, 1), (60, 39, 1),
-- 瑷傚柈 61 (2025-12-15)
(61, 8, 1), (61, 17, 1), (61, 24, 1),
-- 瑷傚柈 62 (2025-12-17)
(62, 9, 2), (62, 14, 1), (62, 25, 1),
-- 瑷傚柈 63 (2025-12-19)
(63, 10, 1), (63, 15, 2), (63, 26, 1),
-- 瑷傚柈 64 (2025-12-22)
(64, 7, 1), (64, 16, 1), (64, 27, 1),
-- 瑷傚柈 65 (2025-12-25)
(65, 5, 2), (65, 18, 1), (65, 28, 1),
-- 瑷傚柈 66 (2025-12-28)
(66, 6, 1), (66, 12, 2), (66, 29, 1),
-- 瑷傚柈 67 (2026-01-02)
(67, 8, 1), (67, 17, 1), (67, 30, 1),
-- 瑷傚柈 68 (2026-01-04)
(68, 9, 2), (68, 14, 1), (68, 31, 1),
-- 瑷傚柈 69 (2026-01-06)
(69, 10, 1), (69, 15, 2), (69, 32, 1),
-- 瑷傚柈 70 (2026-01-08)
(70, 7, 1), (70, 16, 1), (70, 33, 1),
-- 瑷傚柈 71 (2026-01-10)
(71, 5, 2), (71, 18, 1), (71, 34, 1),
-- 瑷傚柈 72 (2026-01-12)
(72, 6, 1), (72, 12, 2), (72, 35, 1),
-- 瑷傚柈 73 (2026-01-14)
(73, 8, 1), (73, 17, 1), (73, 36, 1),
-- 瑷傚柈 74 (2026-01-16)
(74, 9, 2), (74, 14, 1), (74, 37, 1),
-- 瑷傚柈 75 (2026-01-18)
(75, 10, 1), (75, 15, 2), (75, 38, 1),
-- 瑷傚柈 76 (2026-01-20)
(76, 7, 1), (76, 16, 1), (76, 23, 1), (76, 39, 1),
-- 瑷傚柈 77 (2026-01-23)
(77, 5, 2), (77, 18, 1), (77, 24, 1),
-- 瑷傚柈 78 (2026-01-26)
(78, 6, 1), (78, 12, 2), (78, 25, 1);

-- =================================================================
-- 鍎儬鍒镐娇鐢ㄨ閷?-- =================================================================
INSERT INTO order_coupons (oid, coupon_id, discount_amount) VALUES
-- 10鏈堣▊鍠劒鎯犲埜
(30, 1, 15.60),  -- 10% off
(32, 2, 26.00),  -- Free drink (item 15)
(34, 3, 50.00),  -- HK$50 off
(36, 1, 12.80),  -- 10% off
(38, 2, 28.00),  -- Free drink (item 14)
(40, 3, 50.00),  -- HK$50 off
(42, 1, 18.40),  -- 10% off

-- 11鏈堣▊鍠劒鎯犲埜
(44, 2, 28.00),  -- Free drink (item 39)
(46, 3, 50.00),  -- HK$50 off
(48, 1, 14.20),  -- 10% off
(50, 2, 26.00),  -- Free drink (item 14)
(52, 3, 50.00),  -- HK$50 off
(54, 1, 16.80),  -- 10% off

-- 12鏈堣▊鍠劒鎯犲埜
(56, 2, 30.00),  -- Free drink (item 41)
(58, 3, 50.00),  -- HK$50 off
(60, 1, 13.40),  -- 10% off
(62, 2, 28.00),  -- Free drink (item 14)
(64, 3, 50.00),  -- HK$50 off
(66, 1, 15.20),  -- 10% off

-- 1鏈堣▊鍠劒鎯犲埜
(68, 2, 25.00),  -- Free drink (item 31)
(70, 3, 50.00),  -- HK$50 off
(72, 1, 17.60),  -- 10% off
(74, 2, 28.00),  -- Free drink (item 14)
(76, 3, 50.00),  -- HK$50 off
(78, 1, 14.80);  -- 10% off

-- =================================================================
-- 鏇存柊椤у绌嶅垎
-- =================================================================
UPDATE customer SET coupon_point = coupon_point + 150 WHERE cid = 1;
UPDATE customer SET coupon_point = coupon_point + 180 WHERE cid = 2;
UPDATE customer SET coupon_point = coupon_point + 120 WHERE cid = 3;
UPDATE customer SET coupon_point = coupon_point + 200 WHERE cid = 4;
UPDATE customer SET coupon_point = coupon_point + 160 WHERE cid = 5;

-- 鎻掑叆绌嶅垎姝峰彶瑷橀寗锛堜娇鐢ㄥ瓙鏌ヨ鐛插彇鏇存柊寰岀殑绌嶅垎锛?INSERT INTO coupon_point_history (cid, delta, resulting_points, action, note) VALUES
(1, 150, (SELECT coupon_point FROM customer WHERE cid = 1), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(2, 180, (SELECT coupon_point FROM customer WHERE cid = 2), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(3, 120, (SELECT coupon_point FROM customer WHERE cid = 3), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(4, 200, (SELECT coupon_point FROM customer WHERE cid = 4), 'earn', 'Points from Oct 2025 - Jan 2026 orders'),
(5, 160, (SELECT coupon_point FROM customer WHERE cid = 5), 'earn', 'Points from Oct 2025 - Jan 2026 orders');

-- =================================================================
-- 闈炴渻鍝￠姺鍞暩鎿?(Walk-in Customer)
-- 50绛嗚▊鍠紝鏅傞枔绡勫湇锛?025骞?0鏈?鏃?- 2026骞?鏈?1鏃?-- 浣跨敤cid=0 (Walk-in Customer)
-- oid 绡勫湇锛?9锝?28
-- =================================================================


-- =================================================================
-- 鎻掑叆50绛嗛潪鏈冨摗瑷傚柈
-- =================================================================

-- 2025骞?0鏈堥潪鏈冨摗瑷傚柈 (13绛?  oid 79-91
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

-- 2025骞?1鏈堥潪鏈冨摗瑷傚柈 (13绛?  oid 92-104
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

-- 2025骞?2鏈堥潪鏈冨摗瑷傚柈 (12绛?  oid 105-116
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

-- 2026骞?鏈堥潪鏈冨摗瑷傚柈 (12绛?  oid 117-128
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
-- 鎻掑叆闈炴渻鍝¤▊鍠爡鐩?-- 瑷伙細瑷傚柈oid寰?9闁嬪锛堝凡鏈夎▊鍠埌78锛?-- =================================================================

-- 瑷傚柈79-91 (2025骞?0鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(79, 3, 1), (79, 12, 1), (79, 19, 1),  -- 鍙ｆ按闆?+ 鍜告煚7 + 绫抽／
(80, 6, 1), (80, 17, 1), (80, 20, 1),  -- 楹诲﹩璞嗚厫 + 鍑嶅ザ鑼?+ 楹?(81, 8, 1), (81, 14, 1), (81, 19, 1),  -- 鍥為崑鑲?+ 鐔卞ザ鑼?+ 绫抽／
(82, 5, 1), (82, 13, 1), (82, 19, 1),  -- 閲嶆叾鐗涜倝 + 绱呰眴鍐?+ 绫抽／
(83, 7, 1), (83, 18, 1), (83, 21, 1),  -- 鎿旀摂楹?+ 鍑嶆鑼?+ 钖矇
(84, 9, 1), (84, 15, 1), (84, 19, 1),  -- 姘寸叜鐗涜倝 + 钁¤悇鐑忛緧鑼?+ 绫抽／
(85, 10, 1), (85, 16, 1), (85, 20, 1), -- 榄氶鑼勫瓙 + 鐔辨鑼?+ 楹?(86, 1, 2), (86, 12, 2), (86, 19, 2),  -- 榛冪摐鑺眡2 + 鍜告煚7x2 + 绫抽／x2
(87, 2, 1), (87, 14, 1), (87, 19, 1),  -- 鏈ㄨ€?+ 鐔卞ザ鑼?+ 绫抽／
(88, 4, 1), (88, 17, 1), (88, 21, 1),  -- 閰歌彍榄氭汞 + 鍑嶅ザ鑼?+ 钖矇
(89, 23, 1), (89, 28, 1), (89, 34, 1), (89, 39, 1), -- 棣欒剢钘曠墖 + 閰歌荆婀?+ 瀹繚闆炰竵 + 鍐拌渹铚滄妾?(90, 24, 1), (90, 29, 1), (90, 35, 1), (90, 19, 1), -- 楹昏荆娴疯渿 + 鍐摐婀?+ 绯栭唻閲岃剨 + 绫抽／
(91, 25, 1), (91, 30, 1), (91, 36, 1), (91, 40, 1); -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑 + 铚滄鐑忛緧鑼?
-- 瑷傚柈92-104 (2025骞?1鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(92, 26, 1), (92, 31, 1), (92, 37, 1), (92, 41, 1), -- 钄彍鏄ュ嵎 + 鑺掓灉甯冧竵 + 绱呯噿鑲?+ 妞板ザ鏄?(93, 27, 2), (93, 32, 1), (93, 38, 1), (93, 12, 2), -- 鍥涘窛杈ｈ姳鐢焫2 + 鑺濋夯鐞?+ 妾告闆?+ 鍜告煚7x2
(94, 3, 1), (94, 28, 1), (94, 34, 1), (94, 18, 1), -- 鍙ｆ按闆?+ 閰歌荆婀?+ 瀹繚闆炰竵 + 鍑嶆鑼?(95, 5, 1), (95, 29, 1), (95, 35, 1), (95, 17, 1), -- 閲嶆叾鐗涜倝 + 鍐摐婀?+ 绯栭唻閲岃剨 + 鍑嶅ザ鑼?(96, 6, 1), (96, 30, 1), (96, 36, 1), (96, 16, 1), -- 楹诲﹩璞嗚厫 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑 + 鐔辨鑼?(97, 8, 1), (97, 23, 1), (97, 37, 1), (97, 14, 1), -- 鍥為崑鑲?+ 棣欒剢钘曠墖 + 绱呯噿鑲?+ 鐔卞ザ鑼?(98, 9, 1), (98, 24, 1), (98, 38, 1), (98, 15, 1), -- 姘寸叜鐗涜倝 + 楹昏荆娴疯渿 + 妾告闆?+ 钁¤悇鐑忛緧鑼?(99, 10, 1), (99, 25, 1), (99, 34, 1), (99, 13, 1), -- 榄氶鑼勫瓙 + 钂滈姣涜眴 + 瀹繚闆炰竵 + 绱呰眴鍐?(100, 1, 2), (100, 26, 1), (100, 35, 1), (100, 39, 2), -- 榛冪摐鑺眡2 + 钄彍鏄ュ嵎 + 绯栭唻閲岃剨 + 鍐拌渹铚滄妾瑇2
(101, 2, 1), (101, 27, 1), (101, 36, 1), (101, 40, 1), -- 鏈ㄨ€?+ 鍥涘窛杈ｈ姳鐢?+ 鍥涘窛娴烽涔鹃崑 + 铚滄鐑忛緧鑼?(102, 4, 1), (102, 23, 1), (102, 37, 1), (102, 41, 1), -- 閰歌彍榄氭汞 + 棣欒剢钘曠墖 + 绱呯噿鑲?+ 妞板ザ鏄?(103, 7, 1), (103, 24, 1), (103, 38, 1), (103, 12, 1), -- 鎿旀摂楹?+ 楹昏荆娴疯渿 + 妾告闆?+ 鍜告煚7
(104, 11, 2), (104, 31, 1), (104, 17, 2); -- 绯背绯晉2 + 鑺掓灉甯冧竵 + 鍑嶅ザ鑼秞2

-- 瑷傚柈105-116 (2025骞?2鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(105, 3, 1), (105, 25, 1), (105, 34, 1), (105, 18, 1), -- 鍙ｆ按闆?+ 钂滈姣涜眴 + 瀹繚闆炰竵 + 鍑嶆鑼?(106, 5, 1), (106, 26, 1), (106, 35, 1), (106, 14, 1), -- 閲嶆叾鐗涜倝 + 钄彍鏄ュ嵎 + 绯栭唻閲岃剨 + 鐔卞ザ鑼?(107, 6, 2), (107, 27, 1), (107, 36, 1), (107, 16, 2), -- 楹诲﹩璞嗚厫x2 + 鍥涘窛杈ｈ姳鐢?+ 鍥涘窛娴烽涔鹃崑 + 鐔辨鑼秞2
(108, 8, 1), (108, 28, 1), (108, 37, 1), (108, 13, 1), -- 鍥為崑鑲?+ 閰歌荆婀?+ 绱呯噿鑲?+ 绱呰眴鍐?(109, 9, 1), (109, 29, 1), (109, 38, 1), (109, 15, 1), -- 姘寸叜鐗涜倝 + 鍐摐婀?+ 妾告闆?+ 钁¤悇鐑忛緧鑼?(110, 10, 1), (110, 30, 1), (110, 34, 1), (110, 17, 1), -- 榄氶鑼勫瓙 + 鐜夌背锜硅倝婀?+ 瀹繚闆炰竵 + 鍑嶅ザ鑼?(111, 1, 2), (111, 23, 1), (111, 35, 1), (111, 39, 1), -- 榛冪摐鑺眡2 + 棣欒剢钘曠墖 + 绯栭唻閲岃剨 + 鍐拌渹铚滄妾?(112, 2, 1), (112, 24, 1), (112, 36, 1), (112, 40, 1), -- 鏈ㄨ€?+ 楹昏荆娴疯渿 + 鍥涘窛娴烽涔鹃崑 + 铚滄鐑忛緧鑼?(113, 4, 1), (113, 31, 1), (113, 37, 1), (113, 12, 1), -- 閰歌彍榄氭汞 + 鑺掓灉甯冧竵 + 绱呯噿鑲?+ 鍜告煚7
(114, 7, 1), (114, 32, 1), (114, 38, 1), (114, 18, 1), -- 鎿旀摂楹?+ 鑺濋夯鐞?+ 妾告闆?+ 鍑嶆鑼?(115, 11, 1), (115, 33, 1), (115, 14, 1), (115, 19, 1), -- 绯背绯?+ 铔嬫尀 + 鐔卞ザ鑼?+ 绫抽／
(116, 5, 1), (116, 25, 1), (116, 34, 1), (116, 17, 1); -- 閲嶆叾鐗涜倝 + 钂滈姣涜眴 + 瀹繚闆炰竵 + 鍑嶅ザ鑼?
-- 瑷傚柈117-128 (2026骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(117, 6, 1), (117, 26, 1), (117, 35, 1), (117, 16, 1), -- 楹诲﹩璞嗚厫 + 钄彍鏄ュ嵎 + 绯栭唻閲岃剨 + 鐔辨鑼?(118, 8, 1), (118, 27, 1), (118, 36, 1), (118, 13, 1), -- 鍥為崑鑲?+ 鍥涘窛杈ｈ姳鐢?+ 鍥涘窛娴烽涔鹃崑 + 绱呰眴鍐?(119, 9, 1), (119, 28, 1), (119, 37, 1), (119, 15, 1), -- 姘寸叜鐗涜倝 + 閰歌荆婀?+ 绱呯噿鑲?+ 钁¤悇鐑忛緧鑼?(120, 10, 1), (120, 29, 1), (120, 38, 1), (120, 39, 1), -- 榄氶鑼勫瓙 + 鍐摐婀?+ 妾告闆?+ 鍐拌渹铚滄妾?(121, 1, 1), (121, 30, 1), (121, 34, 1), (121, 40, 1), -- 榛冪摐鑺?+ 鐜夌背锜硅倝婀?+ 瀹繚闆炰竵 + 铚滄鐑忛緧鑼?(122, 2, 1), (122, 31, 1), (122, 35, 1), (122, 41, 1), -- 鏈ㄨ€?+ 鑺掓灉甯冧竵 + 绯栭唻閲岃剨 + 妞板ザ鏄?(123, 3, 1), (123, 32, 1), (123, 36, 1), (123, 12, 1), -- 鍙ｆ按闆?+ 鑺濋夯鐞?+ 鍥涘窛娴烽涔鹃崑 + 鍜告煚7
(124, 4, 1), (124, 33, 1), (124, 37, 1), (124, 14, 1), -- 閰歌彍榄氭汞 + 铔嬫尀 + 绱呯噿鑲?+ 鐔卞ザ鑼?(125, 23, 2), (125, 28, 1), (125, 38, 1), (125, 17, 2), -- 棣欒剢钘曠墖x2 + 閰歌荆婀?+ 妾告闆?+ 鍑嶅ザ鑼秞2
(126, 24, 1), (126, 29, 1), (126, 34, 1), (126, 18, 1), -- 楹昏荆娴疯渿 + 鍐摐婀?+ 瀹繚闆炰竵 + 鍑嶆鑼?(127, 25, 1), (127, 30, 1), (127, 35, 1), (127, 16, 1), -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 绯栭唻閲岃剨 + 鐔辨鑼?(128, 26, 1), (128, 31, 1), (128, 36, 1), (128, 39, 1); -- 钄彍鏄ュ嵎 + 鑺掓灉甯冧竵 + 鍥涘窛娴烽涔鹃崑 + 鍐拌渹铚滄妾?
-- =================================================================
-- 闈炴渻鍝￠姺鍞暩鎿?(Walk-in Customer) - 绗笁鎵?-- 72绛嗚▊鍠紙鍓?0绛?寰?2绛嗭級锛屾檪闁撶瘎鍦嶏細2025骞?鏈?鏃?- 2026骞?鏈?8鏃?-- 浣跨敤cid=0 (Walk-in Customer)
-- oid 绡勫湇锛?29锝?00
-- =================================================================


-- =================================================================
-- 鎻掑叆72绛嗛潪鏈冨摗瑷傚柈
-- =================================================================

-- 2025骞?鏈堥潪鏈冨摗瑷傚柈 (10绛?  oid 129-138
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

-- 2025骞?0鏈堥潪鏈冨摗瑷傚柈 (10绛?  oid 139-148
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

-- 2025骞?1鏈堥潪鏈冨摗瑷傚柈 (10绛?  oid 149-158
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

-- 2025骞?2鏈堥潪鏈冨摗瑷傚柈 (10绛?  oid 159-168
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

-- 2026骞?鏈堥潪鏈冨摗瑷傚柈 (5绛?  oid 169-173
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(169, '2026-01-03 14:45:00', 0, 3, 'walkin_20260103_091', NULL, 'takeaway', NULL),
(170, '2026-01-06 16:10:00', 0, 3, 'walkin_20260106_092', NULL, 'dine_in', 12),
(171, '2026-01-09 18:45:00', 0, 3, 'walkin_20260109_093', NULL, 'dine_in', 25),
(172, '2026-01-12 19:35:00', 0, 3, 'walkin_20260112_094', NULL, 'dine_in', 9),
(173, '2026-01-15 15:00:00', 0, 3, 'walkin_20260115_095', NULL, 'takeaway', NULL);

-- 2026骞?鏈堥潪鏈冨摗瑷傚柈 (5绛?  oid 174-178
INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
(174, '2026-02-01 16:20:00', 0, 3, 'walkin_20260201_096', NULL, 'dine_in', 16),
(175, '2026-02-04 18:55:00', 0, 3, 'walkin_20260204_097', NULL, 'dine_in', 7),
(176, '2026-02-07 19:45:00', 0, 3, 'walkin_20260207_098', NULL, 'dine_in', 19),
(177, '2026-02-10 15:20:00', 0, 3, 'walkin_20260210_099', NULL, 'takeaway', NULL),
(178, '2026-02-13 16:45:00', 0, 3, 'walkin_20260213_100', NULL, 'dine_in', 22);

-- 鍐嶅鍔?2绛嗚純绨″柈鐨勮▊鍠紙179-200锛?INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
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
-- 鎻掑叆闈炴渻鍝¤▊鍠爡鐩?-- 瑷伙細瑷傚柈oid寰?29闁嬪锛堝凡鏈夎▊鍠埌128锛?-- =================================================================

-- 瑷傚柈129-138 (2025骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(129, 1, 1), (129, 19, 1), (129, 12, 1),  -- 榛冪摐鑺?+ 绫抽／ + 鍜告煚7
(130, 2, 1), (130, 20, 1), (130, 17, 1),  -- 鏈ㄨ€?+ 楹?+ 鍑嶅ザ鑼?(131, 3, 1), (131, 19, 1), (131, 14, 1),  -- 鍙ｆ按闆?+ 绫抽／ + 鐔卞ザ鑼?(132, 4, 1), (132, 21, 1), (132, 18, 1),  -- 閰歌彍榄氭汞 + 钖矇 + 鍑嶆鑼?(133, 5, 1), (133, 19, 1), (133, 13, 1),  -- 閲嶆叾鐗涜倝 + 绫抽／ + 绱呰眴鍐?(134, 6, 2), (134, 20, 2), (134, 16, 2),  -- 楹诲﹩璞嗚厫x2 + 楹祒2 + 鐔辨鑼秞2
(135, 7, 1), (135, 19, 1), (135, 15, 1),  -- 鎿旀摂楹?+ 绫抽／ + 钁¤悇鐑忛緧鑼?(136, 8, 1), (136, 19, 1), (136, 17, 1),  -- 鍥為崑鑲?+ 绫抽／ + 鍑嶅ザ鑼?(137, 9, 1), (137, 21, 1), (137, 12, 1),  -- 姘寸叜鐗涜倝 + 钖矇 + 鍜告煚7
(138, 10, 1), (138, 19, 1), (138, 14, 1); -- 榄氶鑼勫瓙 + 绫抽／ + 鐔卞ザ鑼?
-- 瑷傚柈139-148 (2025骞?0鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(139, 23, 1), (139, 28, 1), (139, 34, 1), (139, 39, 1),  -- 棣欒剢钘曠墖 + 閰歌荆婀?+ 瀹繚闆炰竵 + 鍐拌渹铚滄妾?(140, 24, 1), (140, 29, 1), (140, 35, 1), (140, 40, 1),  -- 楹昏荆娴疯渿 + 鍐摐婀?+ 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?(141, 25, 1), (141, 30, 1), (141, 36, 1), (141, 41, 1),  -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?(142, 26, 1), (142, 28, 1), (142, 37, 1), (142, 12, 1),  -- 钄彍鏄ュ嵎 + 閰歌荆婀?+ 绱呯噿鑲?+ 鍜告煚7
(143, 27, 2), (143, 29, 1), (143, 38, 1), (143, 17, 1),  -- 鍥涘窛杈ｈ姳鐢焫2 + 鍐摐婀?+ 妾告闆?+ 鍑嶅ザ鑼?(144, 23, 1), (144, 30, 1), (144, 34, 1), (144, 18, 1),  -- 棣欒剢钘曠墖 + 鐜夌背锜硅倝婀?+ 瀹繚闆炰竵 + 鍑嶆鑼?(145, 24, 1), (145, 28, 1), (145, 35, 1), (145, 13, 1),  -- 楹昏荆娴疯渿 + 閰歌荆婀?+ 绯栭唻閲岃剨 + 绱呰眴鍐?(146, 25, 1), (146, 29, 1), (146, 36, 1), (146, 14, 1),  -- 钂滈姣涜眴 + 鍐摐婀?+ 鍥涘窛娴烽涔鹃崑 + 鐔卞ザ鑼?(147, 26, 1), (147, 30, 1), (147, 37, 1), (147, 15, 1),  -- 钄彍鏄ュ嵎 + 鐜夌背锜硅倝婀?+ 绱呯噿鑲?+ 钁¤悇鐑忛緧鑼?(148, 27, 1), (148, 28, 1), (148, 38, 1), (148, 16, 1);  -- 鍥涘窛杈ｈ姳鐢?+ 閰歌荆婀?+ 妾告闆?+ 鐔辨鑼?
-- 瑷傚柈149-158 (2025骞?1鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(149, 31, 1), (149, 34, 1), (149, 39, 1), (149, 19, 1),  -- 鑺掓灉甯冧竵 + 瀹繚闆炰竵 + 鍐拌渹铚滄妾?+ 绫抽／
(150, 32, 1), (150, 35, 1), (150, 40, 1), (150, 19, 1),  -- 鑺濋夯鐞?+ 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?+ 绫抽／
(151, 33, 1), (151, 36, 1), (151, 41, 1), (151, 20, 1),  -- 铔嬫尀 + 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?+ 楹?(152, 31, 2), (152, 37, 1), (152, 12, 2), (152, 19, 1),  -- 鑺掓灉甯冧竵x2 + 绱呯噿鑲?+ 鍜告煚7x2 + 绫抽／
(153, 32, 1), (153, 38, 1), (153, 17, 1), (153, 20, 1),  -- 鑺濋夯鐞?+ 妾告闆?+ 鍑嶅ザ鑼?+ 楹?(154, 33, 1), (154, 34, 1), (154, 18, 1), (154, 19, 1),  -- 铔嬫尀 + 瀹繚闆炰竵 + 鍑嶆鑼?+ 绫抽／
(155, 31, 1), (155, 35, 1), (155, 13, 1), (155, 19, 1),  -- 鑺掓灉甯冧竵 + 绯栭唻閲岃剨 + 绱呰眴鍐?+ 绫抽／
(156, 32, 1), (156, 36, 1), (156, 14, 1), (156, 20, 1),  -- 鑺濋夯鐞?+ 鍥涘窛娴烽涔鹃崑 + 鐔卞ザ鑼?+ 楹?(157, 33, 1), (157, 37, 1), (157, 15, 1), (157, 19, 1),  -- 铔嬫尀 + 绱呯噿鑲?+ 钁¤悇鐑忛緧鑼?+ 绫抽／
(158, 31, 1), (158, 38, 1), (158, 16, 1), (158, 20, 1);  -- 鑺掓灉甯冧竵 + 妾告闆?+ 鐔辨鑼?+ 楹?
-- 瑷傚柈159-168 (2025骞?2鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(159, 1, 1), (159, 3, 1), (159, 12, 2), (159, 19, 2),  -- 榛冪摐鑺?+ 鍙ｆ按闆?+ 鍜告煚7x2 + 绫抽／x2
(160, 2, 1), (160, 5, 1), (160, 17, 1), (160, 19, 1),  -- 鏈ㄨ€?+ 閲嶆叾鐗涜倝 + 鍑嶅ザ鑼?+ 绫抽／
(161, 4, 1), (161, 6, 1), (161, 18, 1), (161, 21, 1),  -- 閰歌彍榄氭汞 + 楹诲﹩璞嗚厫 + 鍑嶆鑼?+ 钖矇
(162, 7, 1), (162, 8, 1), (162, 13, 1), (162, 20, 1),  -- 鎿旀摂楹?+ 鍥為崑鑲?+ 绱呰眴鍐?+ 楹?(163, 9, 1), (163, 10, 1), (163, 14, 2), (163, 19, 2),  -- 姘寸叜鐗涜倝 + 榄氶鑼勫瓙 + 鐔卞ザ鑼秞2 + 绫抽／x2
(164, 23, 1), (164, 24, 1), (164, 15, 1), (164, 16, 1),  -- 棣欒剢钘曠墖 + 楹昏荆娴疯渿 + 钁¤悇鐑忛緧鑼?+ 鐔辨鑼?(165, 25, 1), (165, 26, 1), (165, 17, 1), (165, 18, 1),  -- 钂滈姣涜眴 + 钄彍鏄ュ嵎 + 鍑嶅ザ鑼?+ 鍑嶆鑼?(166, 27, 2), (166, 28, 1), (166, 12, 1), (166, 13, 1),  -- 鍥涘窛杈ｈ姳鐢焫2 + 閰歌荆婀?+ 鍜告煚7 + 绱呰眴鍐?(167, 29, 1), (167, 30, 1), (167, 14, 1), (167, 15, 1),  -- 鍐摐婀?+ 鐜夌背锜硅倝婀?+ 鐔卞ザ鑼?+ 钁¤悇鐑忛緧鑼?(168, 31, 1), (168, 32, 1), (168, 16, 1), (168, 17, 1);  -- 鑺掓灉甯冧竵 + 鑺濋夯鐞?+ 鐔辨鑼?+ 鍑嶅ザ鑼?
-- 瑷傚柈169-173 (2026骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(169, 33, 1), (169, 34, 1), (169, 18, 1), (169, 19, 1),  -- 铔嬫尀 + 瀹繚闆炰竵 + 鍑嶆鑼?+ 绫抽／
(170, 35, 1), (170, 36, 1), (170, 39, 1), (170, 20, 1),  -- 绯栭唻閲岃剨 + 鍥涘窛娴烽涔鹃崑 + 鍐拌渹铚滄妾?+ 楹?(171, 37, 1), (171, 38, 1), (171, 40, 1), (171, 19, 1),  -- 绱呯噿鑲?+ 妾告闆?+ 铚滄鐑忛緧鑼?+ 绫抽／
(172, 34, 1), (172, 35, 1), (172, 41, 1), (172, 20, 1),  -- 瀹繚闆炰竵 + 绯栭唻閲岃剨 + 妞板ザ鏄?+ 楹?(173, 36, 1), (173, 37, 1), (173, 12, 1), (173, 13, 1);  -- 鍥涘窛娴烽涔鹃崑 + 绱呯噿鑲?+ 鍜告煚7 + 绱呰眴鍐?
-- 瑷傚柈174-178 (2026骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(174, 38, 1), (174, 23, 1), (174, 14, 1), (174, 15, 1),  -- 妾告闆?+ 棣欒剢钘曠墖 + 鐔卞ザ鑼?+ 钁¤悇鐑忛緧鑼?(175, 24, 1), (175, 25, 1), (175, 16, 1), (175, 17, 1),  -- 楹昏荆娴疯渿 + 钂滈姣涜眴 + 鐔辨鑼?+ 鍑嶅ザ鑼?(176, 26, 1), (176, 27, 1), (176, 18, 1), (176, 19, 1),  -- 钄彍鏄ュ嵎 + 鍥涘窛杈ｈ姳鐢?+ 鍑嶆鑼?+ 绫抽／
(177, 28, 1), (177, 29, 1), (177, 39, 1), (177, 40, 1),  -- 閰歌荆婀?+ 鍐摐婀?+ 鍐拌渹铚滄妾?+ 铚滄鐑忛緧鑼?(178, 30, 1), (178, 31, 1), (178, 41, 1), (178, 19, 1);  -- 鐜夌背锜硅倝婀?+ 鑺掓灉甯冧竵 + 妞板ザ鏄?+ 绫抽／

-- 瑷傚柈179-200 (寰?2绛?
INSERT INTO order_items (oid, item_id, qty) VALUES
(179, 1, 1), (179, 12, 1),  -- 榛冪摐鑺?+ 鍜告煚7
(180, 2, 1), (180, 17, 1),  -- 鏈ㄨ€?+ 鍑嶅ザ鑼?(181, 3, 1), (181, 14, 1),  -- 鍙ｆ按闆?+ 鐔卞ザ鑼?(182, 4, 1), (182, 18, 1),  -- 閰歌彍榄氭汞 + 鍑嶆鑼?(183, 5, 1), (183, 13, 1),  -- 閲嶆叾鐗涜倝 + 绱呰眴鍐?(184, 6, 1), (184, 16, 1),  -- 楹诲﹩璞嗚厫 + 鐔辨鑼?(185, 7, 1), (185, 15, 1),  -- 鎿旀摂楹?+ 钁¤悇鐑忛緧鑼?(186, 8, 1), (186, 17, 1),  -- 鍥為崑鑲?+ 鍑嶅ザ鑼?(187, 9, 1), (187, 12, 1),  -- 姘寸叜鐗涜倝 + 鍜告煚7
(188, 10, 1), (188, 14, 1), -- 榄氶鑼勫瓙 + 鐔卞ザ鑼?(189, 23, 1), (189, 34, 1), (189, 39, 1),  -- 棣欒剢钘曠墖 + 瀹繚闆炰竵 + 鍐拌渹铚滄妾?(190, 24, 1), (190, 35, 1), (190, 40, 1),  -- 楹昏荆娴疯渿 + 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?(191, 25, 1), (191, 36, 1), (191, 41, 1),  -- 钂滈姣涜眴 + 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?(192, 26, 1), (192, 37, 1), (192, 12, 1),  -- 钄彍鏄ュ嵎 + 绱呯噿鑲?+ 鍜告煚7
(193, 27, 1), (193, 38, 1), (193, 17, 1),  -- 鍥涘窛杈ｈ姳鐢?+ 妾告闆?+ 鍑嶅ザ鑼?(194, 31, 1), (194, 34, 1), (194, 39, 1),  -- 鑺掓灉甯冧竵 + 瀹繚闆炰竵 + 鍐拌渹铚滄妾?(195, 32, 1), (195, 35, 1), (195, 40, 1),  -- 鑺濋夯鐞?+ 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?(196, 33, 1), (196, 36, 1), (196, 41, 1),  -- 铔嬫尀 + 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?(197, 23, 1), (197, 28, 1), (197, 17, 1),  -- 棣欒剢钘曠墖 + 閰歌荆婀?+ 鍑嶅ザ鑼?(198, 24, 1), (198, 29, 1), (198, 18, 1),  -- 楹昏荆娴疯渿 + 鍐摐婀?+ 鍑嶆鑼?(199, 25, 1), (199, 30, 1), (199, 13, 1),  -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 绱呰眴鍐?(200, 26, 1), (200, 28, 1), (200, 14, 1);  -- 钄彍鏄ュ嵎 + 閰歌荆婀?+ 鐔卞ザ鑼?
-- =================================================================
-- 閵峰敭鏁告摎4.txt - 鐐烘柊澧?鍚嶆渻鍝″鍔?0浠借▊鍠?-- 鏅傞枔绡勫湇锛?025骞?0鏈?- 2026骞?鏈?-- 浣跨敤cid=6-12 (鏂版渻鍝?
-- oid 绡勫湇锛?01锝?50
-- =================================================================


-- =================================================================
-- 鎻掑叆50绛嗘渻鍝¤▊鍠?-- =================================================================

-- 2025骞?0鏈堟渻鍝¤▊鍠?(10绛?  oid 201-210
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

-- 2025骞?1鏈堟渻鍝¤▊鍠?(10绛?  oid 211-220
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

-- 2025骞?2鏈堟渻鍝¤▊鍠?(10绛?  oid 221-230
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

-- 2026骞?鏈堟渻鍝¤▊鍠?(10绛?  oid 231-240
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

-- 2026骞?鏈堟渻鍝¤▊鍠?(10绛?  oid 241-250
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
-- 鎻掑叆鏈冨摗瑷傚柈闋呯洰
-- 瑷伙細瑷傚柈oid寰?01闁嬪锛堝凡鏈夎▊鍠埌200锛?-- =================================================================

-- 瑷傚柈201-210 (2025骞?0鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(201, 3, 1), (201, 12, 2), (201, 19, 2),  -- 鍙ｆ按闆?+ 鍜告煚7x2 + 绫抽／x2
(202, 5, 1), (202, 17, 1), (202, 20, 1),  -- 閲嶆叾鐗涜倝 + 鍑嶅ザ鑼?+ 楹?(203, 6, 1), (203, 14, 2), (203, 19, 2),  -- 楹诲﹩璞嗚厫 + 鐔卞ザ鑼秞2 + 绫抽／x2
(204, 8, 1), (204, 15, 1), (204, 19, 1),  -- 鍥為崑鑲?+ 钁¤悇鐑忛緧鑼?+ 绫抽／
(205, 9, 2), (205, 18, 2), (205, 21, 2),  -- 姘寸叜鐗涜倝x2 + 鍑嶆鑼秞2 + 钖矇x2
(206, 10, 1), (206, 13, 1), (206, 19, 1),  -- 榄氶鑼勫瓙 + 绱呰眴鍐?+ 绫抽／
(207, 1, 2), (207, 16, 2), (207, 19, 2),  -- 榛冪摐鑺眡2 + 鐔辨鑼秞2 + 绫抽／x2
(208, 2, 1), (208, 12, 1), (208, 20, 1),  -- 鏈ㄨ€?+ 鍜告煚7 + 楹?(209, 4, 1), (209, 17, 1), (209, 19, 1),  -- 閰歌彍榄氭汞 + 鍑嶅ザ鑼?+ 绫抽／
(210, 7, 1), (210, 14, 1), (210, 20, 1);  -- 鎿旀摂楹?+ 鐔卞ザ鑼?+ 楹?
-- 瑷傚柈211-220 (2025骞?1鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(211, 23, 1), (211, 28, 1), (211, 34, 1), (211, 39, 1),  -- 棣欒剢钘曠墖 + 閰歌荆婀?+ 瀹繚闆炰竵 + 鍐拌渹铚滄妾?(212, 24, 1), (212, 29, 1), (212, 35, 1), (212, 40, 1),  -- 楹昏荆娴疯渿 + 鍐摐婀?+ 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?(213, 25, 1), (213, 30, 1), (213, 36, 1), (213, 41, 1),  -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?(214, 26, 1), (214, 31, 1), (214, 37, 1), (214, 12, 1),  -- 钄彍鏄ュ嵎 + 鑺掓灉甯冧竵 + 绱呯噿鑲?+ 鍜告煚7
(215, 27, 1), (215, 32, 1), (215, 38, 1), (215, 17, 1),  -- 鍥涘窛杈ｈ姳鐢?+ 鑺濋夯鐞?+ 妾告闆?+ 鍑嶅ザ鑼?(216, 23, 2), (216, 28, 1), (216, 34, 2), (216, 39, 2),  -- 棣欒剢钘曠墖x2 + 閰歌荆婀?+ 瀹繚闆炰竵x2 + 鍐拌渹铚滄妾瑇2
(217, 24, 1), (217, 29, 1), (217, 35, 1), (217, 40, 1),  -- 楹昏荆娴疯渿 + 鍐摐婀?+ 绯栭唻閲岃剨 + 铚滄鐑忛緧鑼?(218, 25, 1), (218, 30, 1), (218, 36, 1), (218, 41, 1),  -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑 + 妞板ザ鏄?(219, 26, 2), (219, 31, 1), (219, 37, 2), (219, 12, 2),  -- 钄彍鏄ュ嵎x2 + 鑺掓灉甯冧竵 + 绱呯噿鑲墄2 + 鍜告煚7x2
(220, 27, 1), (220, 32, 1), (220, 38, 1), (220, 17, 1);  -- 鍥涘窛杈ｈ姳鐢?+ 鑺濋夯鐞?+ 妾告闆?+ 鍑嶅ザ鑼?
-- 瑷傚柈221-230 (2025骞?2鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(221, 3, 1), (221, 25, 1), (221, 34, 1), (221, 18, 1),  -- 鍙ｆ按闆?+ 钂滈姣涜眴 + 瀹繚闆炰竵 + 鍑嶆鑼?(222, 5, 1), (222, 26, 1), (222, 35, 1), (222, 14, 1),  -- 閲嶆叾鐗涜倝 + 钄彍鏄ュ嵎 + 绯栭唻閲岃剨 + 鐔卞ザ鑼?(223, 6, 2), (223, 27, 1), (223, 36, 1), (223, 16, 2),  -- 楹诲﹩璞嗚厫x2 + 鍥涘窛杈ｈ姳鐢?+ 鍥涘窛娴烽涔鹃崑 + 鐔辨鑼秞2
(224, 8, 1), (224, 28, 1), (224, 37, 1), (224, 13, 1),  -- 鍥為崑鑲?+ 閰歌荆婀?+ 绱呯噿鑲?+ 绱呰眴鍐?(225, 9, 1), (225, 29, 1), (225, 38, 1), (225, 15, 1),  -- 姘寸叜鐗涜倝 + 鍐摐婀?+ 妾告闆?+ 钁¤悇鐑忛緧鑼?(226, 10, 1), (226, 30, 1), (226, 34, 1), (226, 17, 1),  -- 榄氶鑼勫瓙 + 鐜夌背锜硅倝婀?+ 瀹繚闆炰竵 + 鍑嶅ザ鑼?(227, 1, 2), (227, 23, 1), (227, 35, 1), (227, 39, 1),  -- 榛冪摐鑺眡2 + 棣欒剢钘曠墖 + 绯栭唻閲岃剨 + 鍐拌渹铚滄妾?(228, 2, 1), (228, 24, 1), (228, 36, 1), (228, 40, 1),  -- 鏈ㄨ€?+ 楹昏荆娴疯渿 + 鍥涘窛娴烽涔鹃崑 + 铚滄鐑忛緧鑼?(229, 4, 1), (229, 31, 1), (229, 37, 1), (229, 12, 1),  -- 閰歌彍榄氭汞 + 鑺掓灉甯冧竵 + 绱呯噿鑲?+ 鍜告煚7
(230, 7, 1), (230, 32, 1), (230, 38, 1), (230, 18, 1);  -- 鎿旀摂楹?+ 鑺濋夯鐞?+ 妾告闆?+ 鍑嶆鑼?
-- 瑷傚柈231-240 (2026骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(231, 11, 1), (231, 33, 1), (231, 14, 1), (231, 19, 1),  -- 绯背绯?+ 铔嬫尀 + 鐔卞ザ鑼?+ 绫抽／
(232, 5, 1), (232, 25, 1), (232, 34, 1), (232, 17, 1),  -- 閲嶆叾鐗涜倝 + 钂滈姣涜眴 + 瀹繚闆炰竵 + 鍑嶅ザ鑼?(233, 6, 1), (233, 26, 1), (233, 35, 1), (233, 16, 1),  -- 楹诲﹩璞嗚厫 + 钄彍鏄ュ嵎 + 绯栭唻閲岃剨 + 鐔辨鑼?(234, 8, 1), (234, 27, 1), (234, 36, 1), (234, 13, 1),  -- 鍥為崑鑲?+ 鍥涘窛杈ｈ姳鐢?+ 鍥涘窛娴烽涔鹃崑 + 绱呰眴鍐?(235, 9, 1), (235, 28, 1), (235, 37, 1), (235, 15, 1),  -- 姘寸叜鐗涜倝 + 閰歌荆婀?+ 绱呯噿鑲?+ 钁¤悇鐑忛緧鑼?(236, 10, 1), (236, 29, 1), (236, 38, 1), (236, 39, 1),  -- 榄氶鑼勫瓙 + 鍐摐婀?+ 妾告闆?+ 鍐拌渹铚滄妾?(237, 1, 1), (237, 30, 1), (237, 34, 1), (237, 40, 1),  -- 榛冪摐鑺?+ 鐜夌背锜硅倝婀?+ 瀹繚闆炰竵 + 铚滄鐑忛緧鑼?(238, 2, 1), (238, 31, 1), (238, 35, 1), (238, 41, 1),  -- 鏈ㄨ€?+ 鑺掓灉甯冧竵 + 绯栭唻閲岃剨 + 妞板ザ鏄?(239, 3, 1), (239, 32, 1), (239, 36, 1), (239, 12, 1),  -- 鍙ｆ按闆?+ 鑺濋夯鐞?+ 鍥涘窛娴烽涔鹃崑 + 鍜告煚7
(240, 4, 1), (240, 33, 1), (240, 37, 1), (240, 14, 1);  -- 閰歌彍榄氭汞 + 铔嬫尀 + 绱呯噿鑲?+ 鐔卞ザ鑼?
-- 瑷傚柈241-250 (2026骞?鏈?
INSERT INTO order_items (oid, item_id, qty) VALUES
(241, 23, 2), (241, 28, 1), (241, 38, 1), (241, 17, 2),  -- 棣欒剢钘曠墖x2 + 閰歌荆婀?+ 妾告闆?+ 鍑嶅ザ鑼秞2
(242, 24, 1), (242, 29, 1), (242, 34, 1), (242, 18, 1),  -- 楹昏荆娴疯渿 + 鍐摐婀?+ 瀹繚闆炰竵 + 鍑嶆鑼?(243, 25, 1), (243, 30, 1), (243, 35, 1), (243, 16, 1),  -- 钂滈姣涜眴 + 鐜夌背锜硅倝婀?+ 绯栭唻閲岃剨 + 鐔辨鑼?(244, 26, 1), (244, 31, 1), (244, 36, 1), (244, 39, 1),  -- 钄彍鏄ュ嵎 + 鑺掓灉甯冧竵 + 鍥涘窛娴烽涔鹃崑 + 鍐拌渹铚滄妾?(245, 27, 1), (245, 32, 1), (245, 37, 1), (245, 40, 1),  -- 鍥涘窛杈ｈ姳鐢?+ 鑺濋夯鐞?+ 绱呯噿鑲?+ 铚滄鐑忛緧鑼?(246, 23, 1), (246, 33, 1), (246, 38, 1), (246, 41, 1),  -- 棣欒剢钘曠墖 + 铔嬫尀 + 妾告闆?+ 妞板ザ鏄?(247, 24, 2), (247, 28, 1), (247, 34, 2), (247, 12, 2),  -- 楹昏荆娴疯渿x2 + 閰歌荆婀?+ 瀹繚闆炰竵x2 + 鍜告煚7x2
(248, 25, 1), (248, 29, 1), (248, 35, 1), (248, 14, 1),  -- 钂滈姣涜眴 + 鍐摐婀?+ 绯栭唻閲岃剨 + 鐔卞ザ鑼?(249, 26, 2), (249, 30, 1), (249, 36, 2), (249, 15, 2),  -- 钄彍鏄ュ嵎x2 + 鐜夌背锜硅倝婀?+ 鍥涘窛娴烽涔鹃崑x2 + 钁¤悇鐑忛緧鑼秞2
(250, 27, 1), (250, 31, 1), (250, 37, 1), (250, 16, 1);  -- 鍥涘窛杈ｈ姳鐢?+ 鑺掓灉甯冧竵 + 绱呯噿鑲?+ 鐔辨鑼?
-- =================================================================
-- 杩?澶╄▊鍠暩鎿氾紙鍕曟厠鏃ユ湡锛? 鐢ㄦ柤 Material Analysis / Consumption
-- oid 251-271锛屽叏閮?ostatus=2 (completed)锛宨tem_id 1-21 (鏈夐厤鏂规潗鏂?
-- 姣忔閲嶅缓 DB 鏈冭嚜鍕曞皪榻娿€屼粖澶╁線鍓?澶┿€嶇殑鍙紨绀鸿硣鏂?-- =================================================================

SET @seed_base_date = CURDATE();

INSERT INTO orders (oid, odate, cid, ostatus, orderRef, coupon_id, order_type, table_number) VALUES
-- 7澶╁墠
(251, DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 7 DAY), INTERVAL 12 HOUR), 1, 2, 'week_recent_251', NULL, 'dine_in', 5),
(252, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 7 DAY), INTERVAL 18 HOUR), INTERVAL 45 MINUTE), 2, 2, 'week_recent_252', NULL, 'dine_in', 8),
(253, DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 7 DAY), INTERVAL 13 HOUR), 0, 2, 'week_recent_253', NULL, 'takeaway', NULL),
-- 6澶╁墠
(254, DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 6 DAY), INTERVAL 12 HOUR), 3, 2, 'week_recent_254', NULL, 'dine_in', 11),
(255, DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 6 DAY), INTERVAL 19 HOUR), 4, 2, 'week_recent_255', NULL, 'dine_in', 14),
(256, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 6 DAY), INTERVAL 13 HOUR), INTERVAL 30 MINUTE), 0, 2, 'week_recent_256', NULL, 'takeaway', NULL),
-- 5澶╁墠
(257, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 5 DAY), INTERVAL 18 HOUR), INTERVAL 30 MINUTE), 5, 2, 'week_recent_257', NULL, 'dine_in', 7),
(258, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 5 DAY), INTERVAL 12 HOUR), INTERVAL 45 MINUTE), 1, 2, 'week_recent_258', NULL, 'dine_in', 3),
(259, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 5 DAY), INTERVAL 13 HOUR), INTERVAL 15 MINUTE), 0, 2, 'week_recent_259', NULL, 'takeaway', NULL),
-- 4澶╁墠
(260, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 4 DAY), INTERVAL 12 HOUR), INTERVAL 30 MINUTE), 2, 2, 'week_recent_260', NULL, 'dine_in', 6),
(261, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 4 DAY), INTERVAL 19 HOUR), INTERVAL 15 MINUTE), 3, 2, 'week_recent_261', NULL, 'dine_in', 10),
(262, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 4 DAY), INTERVAL 13 HOUR), INTERVAL 45 MINUTE), 0, 2, 'week_recent_262', NULL, 'takeaway', NULL),
-- 3澶╁墠
(263, DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 3 DAY), INTERVAL 18 HOUR), 4, 2, 'week_recent_263', NULL, 'dine_in', 15),
(264, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 3 DAY), INTERVAL 12 HOUR), INTERVAL 30 MINUTE), 5, 2, 'week_recent_264', NULL, 'dine_in', 18),
(265, DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 3 DAY), INTERVAL 13 HOUR), 0, 2, 'week_recent_265', NULL, 'takeaway', NULL),
-- 2澶╁墠
(266, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 2 DAY), INTERVAL 12 HOUR), INTERVAL 15 MINUTE), 1, 2, 'week_recent_266', NULL, 'dine_in', 4),
(267, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 2 DAY), INTERVAL 19 HOUR), INTERVAL 30 MINUTE), 2, 2, 'week_recent_267', NULL, 'dine_in', 9),
(268, DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 2 DAY), INTERVAL 13 HOUR), 0, 2, 'week_recent_268', NULL, 'takeaway', NULL),
-- 1澶╁墠
(269, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 1 DAY), INTERVAL 18 HOUR), INTERVAL 45 MINUTE), 3, 2, 'week_recent_269', NULL, 'dine_in', 12),
(270, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 1 DAY), INTERVAL 12 HOUR), INTERVAL 30 MINUTE), 4, 2, 'week_recent_270', NULL, 'dine_in', 17),
(271, DATE_ADD(DATE_ADD(DATE_SUB(@seed_base_date, INTERVAL 1 DAY), INTERVAL 13 HOUR), INTERVAL 15 MINUTE), 0, 2, 'week_recent_271', NULL, 'takeaway', NULL);

-- 杩?澶╄▊鍠爡鐩紙鍏ㄩ儴鐢ㄦ湁閰嶆柟鏉愭枡鐨?item_id 1-21锛?INSERT INTO order_items (oid, item_id, qty) VALUES
-- 251 (03-17): 鍙ｆ按闆瀤2 + 閲嶆叾鐗涜倝x1 + 鐔卞ザ鑼秞2 + 绫抽／x2
(251, 3, 2), (251, 5, 1), (251, 14, 2), (251, 19, 2),
-- 252 (03-17): 姘寸叜鐗涜倝x2 + 楹诲﹩璞嗚厫x1 + 鍜告煚7x2
(252, 9, 2), (252, 6, 1), (252, 12, 2),
-- 253 (03-17 takeaway): 鎿旀摂楹祒1 + 鍑嶅ザ鑼秞1 + 楹祒1
(253, 7, 1), (253, 17, 1), (253, 20, 1),

-- 254 (03-18): 榛冪摐鑺眡2 + 閰歌彍榄氭汞x2 + 鍑嶆鑼秞2 + 绫抽／x2
(254, 1, 2), (254, 4, 2), (254, 18, 2), (254, 19, 2),
-- 255 (03-18): 榄氶鑼勫瓙x2 + 鏈ㄨ€硏1 + 鐔辨鑼秞2
(255, 10, 2), (255, 2, 1), (255, 16, 2),
-- 256 (03-18 takeaway): 鍥為崑鑲墄1 + 绱呰眴鍐皒1 + 鍑嶅ザ鑼秞1
(256, 8, 1), (256, 13, 1), (256, 17, 1),

-- 257 (03-19): 閲嶆叾鐗涜倝x2 + 姘寸叜鐗涜倝x1 + 钁¤悇鐑忛緧鑼秞1
(257, 5, 2), (257, 9, 1), (257, 15, 1),
-- 258 (03-19): 楹诲﹩璞嗚厫x1 + 绯背绯晉2 + 鍜告煚7x1
(258, 6, 1), (258, 11, 2), (258, 12, 1),
-- 259 (03-19 takeaway): 鏈ㄨ€硏1 + 鐔卞ザ鑼秞2 + 楹祒1
(259, 2, 1), (259, 14, 2), (259, 20, 1),

-- 260 (03-20): 鍙ｆ按闆瀤2 + 鍥為崑鑲墄1 + 鍑嶅ザ鑼秞2 + 绫抽／x2
(260, 3, 2), (260, 8, 1), (260, 17, 2), (260, 19, 2),
-- 261 (03-20): 閰歌彍榄氭汞x2 + 榄氶鑼勫瓙x1 + 鐔辨鑼秞1
(261, 4, 2), (261, 10, 1), (261, 16, 1),
-- 262 (03-20 takeaway): 鎿旀摂楹祒1 + 绱呰眴鍐皒1 + 鍑嶆鑼秞1
(262, 7, 1), (262, 13, 1), (262, 18, 1),

-- 263 (03-21): 榛冪摐鑺眡2 + 閲嶆叾鐗涜倝x2 + 鐔卞ザ鑼秞2 + 绫抽／x2
(263, 1, 2), (263, 5, 2), (263, 14, 2), (263, 19, 2),
-- 264 (03-21): 姘寸叜鐗涜倝x2 + 楹诲﹩璞嗚厫x1 + 鍜告煚7x2
(264, 9, 2), (264, 6, 1), (264, 12, 2),
-- 265 (03-21 takeaway): 鏈ㄨ€硏1 + 钁¤悇鐑忛緧鑼秞1 + 楹祒1
(265, 2, 1), (265, 15, 1), (265, 20, 1),

-- 266 (03-22): 鍙ｆ按闆瀤2 + 閰歌彍榄氭汞x1 + 鍑嶆鑼秞1 + 绫抽／x1
(266, 3, 2), (266, 4, 1), (266, 18, 1), (266, 19, 1),
-- 267 (03-22): 榄氶鑼勫瓙x2 + 鍥為崑鑲墄1 + 鐔辨鑼秞2
(267, 10, 2), (267, 8, 1), (267, 16, 2),
-- 268 (03-22 takeaway): 楹诲﹩璞嗚厫x1 + 绯背绯晉1 + 绱呰眴鍐皒1
(268, 6, 1), (268, 11, 1), (268, 13, 1),

-- 269 (03-23): 閲嶆叾鐗涜倝x2 + 姘寸叜鐗涜倝x1 + 鐔卞ザ鑼秞2 + 绫抽／x2
(269, 5, 2), (269, 9, 1), (269, 14, 2), (269, 19, 2),
-- 270 (03-23): 榛冪摐鑺眡2 + 鎿旀摂楹祒2 + 鍑嶅ザ鑼秞2
(270, 1, 2), (270, 7, 2), (270, 17, 2),
-- 271 (03-23 takeaway): 鏈ㄨ€硏1 + 鍜告煚7x1 + 楹祒1
(271, 2, 1), (271, 12, 1), (271, 20, 1);

-- =================================================================
-- Takeaway Cash Orders 娓│璩囨枡
-- oid 272-275锛岀郸 Takeaway Cash Orders 鐣潰鐩存帴娓│鐢?-- 272-273: 寰呬粯娆?-- 274-275: 浠婃棩宸蹭粯娆?-- =================================================================

INSERT INTO orders (oid, odate, cid, ostatus, note, orderRef, coupon_id, order_type, table_number, payment_method) VALUES
(272, NOW() - INTERVAL 2 HOUR, 1, 0, 'Seed takeaway cash order - pending payment', 'seed_takeaway_cash_pending_001', NULL, 'takeaway', NULL, 'cash'),
(273, NOW() - INTERVAL 75 MINUTE, 13, 0, 'Seed takeaway cash order - pending payment', 'seed_takeaway_cash_pending_002', NULL, 'takeaway', NULL, 'cash'),
(274, NOW() - INTERVAL 45 MINUTE, 2, 1, 'Seed takeaway cash order - paid today', 'seed_takeaway_cash_paid_001', NULL, 'takeaway', NULL, 'cash'),
(275, NOW() - INTERVAL 20 MINUTE, 0, 2, 'Seed takeaway cash order - paid today', 'seed_takeaway_cash_paid_002', NULL, 'takeaway', NULL, 'cash');

INSERT INTO order_items (oid, item_id, qty) VALUES
-- 272 pending: 鍙ｆ按闆?+ 鍑嶅ザ鑼?+ 绫抽／
(272, 3, 1), (272, 17, 1), (272, 19, 1),
-- 273 pending: 鎿旀摂楹祒2 + 鍜告煚7
(273, 7, 2), (273, 12, 1),
-- 274 paid today: 鍥為崑鑲?+ 绱呰眴鍐?+ 楹?(274, 8, 1), (274, 13, 1), (274, 20, 1),
-- 275 paid today: 楹诲﹩璞嗚厫 + 鐔卞ザ鑼?+ 绫抽／x2 
(275, 6, 1), (275, 14, 1), (275, 19, 2);

-- =================================================================
-- 鍎儬鍒镐娇鐢ㄨ閷?-- =================================================================

INSERT INTO order_coupons (oid, coupon_id, discount_amount) VALUES
-- 10鏈堣▊鍠劒鎯犲埜
(201, 1, 18.20),  -- 10% off
(203, 2, 26.00),  -- Free drink (item 14)
(205, 3, 50.00),  -- HK$50 off
(207, 1, 15.80),  -- 10% off
(209, 2, 28.00),  -- Free drink (item 17)

-- 11鏈堣▊鍠劒鎯犲埜
(211, 3, 50.00),  -- HK$50 off
(213, 1, 20.40),  -- 10% off
(215, 2, 30.00),  -- Free drink (item 41)
(217, 3, 50.00),  -- HK$50 off
(219, 1, 22.60),  -- 10% off

-- 12鏈堣▊鍠劒鎯犲埜
(221, 2, 26.00),  -- Free drink (item 18)
(223, 3, 50.00),  -- HK$50 off
(225, 1, 19.80),  -- 10% off
(227, 2, 28.00),  -- Free drink (item 39)
(229, 3, 50.00),  -- HK$50 off

-- 1鏈堣▊鍠劒鎯犲埜
(231, 1, 17.40),  -- 10% off
(233, 2, 24.00),  -- Free drink (item 16)
(235, 3, 50.00),  -- HK$50 off
(237, 1, 21.20),  -- 10% off
(239, 2, 26.00),  -- Free drink (item 12)

-- 2鏈堣▊鍠劒鎯犲埜
(241, 3, 50.00),  -- HK$50 off
(243, 1, 23.60),  -- 10% off
(245, 2, 30.00),  -- Free drink (item 40)
(247, 3, 50.00),  -- HK$50 off
(249, 1, 25.80);  -- 10% off

-- =================================================================
-- 鏇存柊鏈冨摗绌嶅垎
-- =================================================================

-- 瑷堢畻姣忎綅鏈冨摗鐨勭附娑堣不涓﹀垎閰嶇鍒嗭紙姣忔秷璨籋K$1 = 1绌嶅垎锛?-- 鎴戝€戝厛鏇存柊椤у琛ㄤ腑鐨刢oupon_point瀛楁

-- 闄冲ぇ鏄?(cid=6) - 闋愯▓绱勬秷璨籋K$1200
UPDATE customer SET coupon_point = coupon_point + 1200 WHERE cid = 6;

-- 鏉庣編鐜?(cid=7) - 闋愯▓绱勬秷璨籋K$1050
UPDATE customer SET coupon_point = coupon_point + 1050 WHERE cid = 7;

-- 寮靛亯寮?(cid=8) - 闋愯▓绱勬秷璨籋K$1350
UPDATE customer SET coupon_point = coupon_point + 1350 WHERE cid = 8;

-- 鐜嬫泬闆?(cid=9) - 闋愯▓绱勬秷璨籋K$1150
UPDATE customer SET coupon_point = coupon_point + 1150 WHERE cid = 9;

-- 鍔夊杓?(cid=10) - 闋愯▓绱勬秷璨籋K$1250
UPDATE customer SET coupon_point = coupon_point + 1250 WHERE cid = 10;

-- 榛冨織鍋?(cid=11) - 闋愯▓绱勬秷璨籋K$1100
  UPDATE customer SET coupon_point = coupon_point + 1100 WHERE cid = 11;

-- 鏋楃鏂?(cid=12) - 闋愯▓绱勬秷璨籋K$1300
UPDATE customer SET coupon_point = coupon_point + 1300 WHERE cid = 12;

-- =================================================================
-- 鎻掑叆绌嶅垎姝峰彶瑷橀寗
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

-- =================================================================
-- Seed Data Date Refresh (keep test data current)
-- =================================================================
-- Roll legacy sample orders (oid 1-5) forward so they stay near recent days.
SET @legacy_max_order_date = (SELECT DATE(MAX(odate)) FROM orders WHERE oid BETWEEN 1 AND 5);
SET @legacy_shift_days = GREATEST(0, DATEDIFF(CURDATE(), @legacy_max_order_date) - 3);
UPDATE orders
SET odate = DATE_ADD(odate, INTERVAL @legacy_shift_days DAY)
WHERE oid BETWEEN 1 AND 5;

-- Roll historical order dataset (oid 29-250) so newest record is about 7 days ago.
SET @history_max_order_date = (SELECT DATE(MAX(odate)) FROM orders WHERE oid BETWEEN 29 AND 250);
SET @history_shift_days = GREATEST(0, DATEDIFF(CURDATE(), @history_max_order_date) - 7);
UPDATE orders
SET odate = DATE_ADD(odate, INTERVAL @history_shift_days DAY)
WHERE oid BETWEEN 29 AND 250;

-- Keep old bookings usable by moving outdated rows to around current date.
UPDATE booking
SET bdate = DATE_ADD(CURDATE(), INTERVAL (bid % 5) - 2 DAY)
WHERE bdate < DATE_SUB(CURDATE(), INTERVAL 30 DAY);

-- Keep active coupon expiry dates in the future.
UPDATE coupons
SET expiry_date = DATE_ADD(CURDATE(), INTERVAL 365 DAY)
WHERE coupon_id IN (1, 3);

UPDATE coupons
SET expiry_date = DATE_ADD(CURDATE(), INTERVAL 300 DAY)
WHERE coupon_id = 2;

COMMIT;

