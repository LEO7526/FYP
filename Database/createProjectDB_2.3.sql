-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
-- Host: localhost    Database: projectdb
-- Server version	8.4.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+08:00";

-- Database: `ProjectDB`
DROP DATABASE IF EXISTS `ProjectDB`;
CREATE DATABASE IF NOT EXISTS `ProjectDB` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ProjectDB`;

-- Table structure for table `staff`
DROP TABLE IF EXISTS `staff`;
CREATE TABLE `staff` (
  `sid` int NOT NULL AUTO_INCREMENT,
  `semail` varchar(191) NOT NULL,       -- Added email column
  `spassword` varchar(255) NOT NULL,
  `sname` varchar(255) NOT NULL,
  `srole` varchar(45) DEFAULT NULL,
  `stel` int DEFAULT NULL,
  simageurl VARCHAR(45) NULL,
  PRIMARY KEY (`sid`),
  UNIQUE KEY `semail_UNIQUE` (`semail`) -- Ensure emails are unique
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `staff`
INSERT INTO staff (semail, spassword, sname, srole, stel,simageurl) VALUES
('peter.wong@example.com', 'password123', 'Peter Wong', 'staff', 25669197,null),
('tina.chan@example.com', 'letmein456', 'Tina Chan', 'Production Supervisor', 31233123,null),
('alex.lam@example.com', 'qwerty789', 'Alex Lam', 'Warehouse Clerk', 29881234,null),
('susan.leung@example.com', 'helloWorld1', 'Susan Leung', 'HR Officer', 28889999,null),
('john.ho@example.com', 'changeme2023', 'John Ho', 'Engineer', 29998888,null),
('maggie.tse@example.com', 'maggiePass!', 'Maggie Tse', 'Accountant', 23881211,null),
('kevin.ng@example.com', 'ngfamily', 'Kevin Ng', 'IT Support', 27889977,null),
('emily.tsui@example.com', 'emily2024', 'Emily Tsui', 'Marketing Lead', 26543210,null);


-- Table structure for table `customer`
DROP TABLE IF EXISTS `customer`;
CREATE TABLE customer (
  cid INT NOT NULL AUTO_INCREMENT,
  cname VARCHAR(255) NOT NULL,
  cpassword VARCHAR(255) NOT NULL,
  ctel INT DEFAULT NULL,
  caddr TEXT,
  company VARCHAR(255) DEFAULT NULL,
  cemail VARCHAR(191) NOT NULL UNIQUE,
  crole VARCHAR(45) NOT NULL DEFAULT 'customer',
  cimageurl VARCHAR(45) NULL,
  PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `customer`
INSERT INTO customer (cname, cpassword, ctel, caddr, company, cemail, crole, cimageurl) VALUES
('Alex Wong', 'password', 21232123, 'G/F, ABC Building, King Yip Street, KwunTong, Kowloon, Hong Kong', 'Fat Cat Company Limited', 'alex.wong@example.com', 'customer', "sample.jpg"),
('Tina Chan', 'password', 31233123, '303, Mei Hing Center, Yuen Long, NT, Hong Kong', 'XDD LOL Company', 'tina.chan@example.com', 'customer', NULL),
('Bowie', 'password', 61236123, '401, Sing Kei Building, Kowloon, Hong Kong', 'GPA4 Company', 'bowie@example.com', 'customer', NULL),
('Samuel Lee', 'samuelpass', 61231212, '111, Example Road, Central, Hong Kong', 'Lee Family Co', 'samuel.lee@example.com', 'customer', NULL),
('Emily Tsang', 'emilypass', 61231555, '88, Happy Valley Road, Hong Kong', 'Happy Valley Enterprises', 'emily.tsang@example.com', 'customer', NULL);

-- Insert a default walk-in customer with cid = 0
INSERT INTO customer (
  cid, cname, cpassword, ctel, caddr, company, cemail, crole, cimageurl
) VALUES (
  0, 'Walk-in Customer', 'walkin', NULL, NULL, NULL, 'walkin@system.local', 'customer', NULL
);

-- Table structure for table `product`
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `pid` int NOT NULL AUTO_INCREMENT,
  `pname` varchar(255) NOT NULL,
  `pdesc` text,
  `pcost` decimal(12,2) NOT NULL,
  PRIMARY KEY (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `product`
INSERT INTO `product` VALUES 
(1,'Cyberpunk Truck C204','Explore the world of imaginative play with our vibrant and durable toy truck. Perfect for little hands, this truck will inspire endless storytelling adventures both indoors and outdoors.',3980.00),
(2,'XDD Wooden Plane','Take to the skies with our charming wooden plane toy. Crafted from eco-friendly and child-safe materials, this beautifully designed plane sparks the imagination and encourages interactive play.',998.00),
(3,'iRobot 3233GG','Introduce your child to the wonders of technology and robotics with our smart robot companion. Packed with interactive features and educational benefits, this futuristic toy engages and educates.',3200.00),
(4,'Apex Ball Ball Helicopter M1297','Experience the thrill of flight with our ball helicopter toy. Easy to launch and navigate, this exciting toy provides hours of entertainment for children of all ages.',1899.00),
(5,'RoboKat AI Cat Robot','Meet our AI Cat Robot – the purr-fect blend of technology and cuddly companionship. This interactive robotic feline offers lifelike movements, sounds, and responses, providing endless fun!',4990.00);


-- Defines categories like Appetizers, Soup, etc
CREATE TABLE menu_category (
  category_id INT PRIMARY KEY AUTO_INCREMENT,
  category_name VARCHAR(100) NOT NULL
);

-- Stores individual dishes
CREATE TABLE menu_item (
  item_id INT PRIMARY KEY AUTO_INCREMENT,
  category_id INT NOT NULL,
  item_price DECIMAL(10,2) NOT NULL,
  image_url VARCHAR(255),
  spice_level VARCHAR(50),
  tags VARCHAR(255),
  is_available BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (category_id) REFERENCES menu_category(category_id)
);

-- For multilingual support
CREATE TABLE menu_item_translation (
  translation_id INT PRIMARY KEY AUTO_INCREMENT,
  item_id INT NOT NULL,
  language_code VARCHAR(10) NOT NULL, -- 'en', 'zh-CN', 'zh-TW'
  item_name VARCHAR(255) NOT NULL,
  item_description TEXT,
  FOREIGN KEY (item_id) REFERENCES menu_item(item_id)
);




-- Drop old table if needed
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;

-- Create orders table (order header)
CREATE TABLE orders (
  oid INT NOT NULL AUTO_INCREMENT,           -- Order ID
  odate DATETIME NOT NULL,                   -- Order date
  cid INT NOT NULL,                          -- Customer ID
  ostatus INT NOT NULL,                      -- Order status
  PRIMARY KEY (oid),
  CONSTRAINT fk_orders_cid FOREIGN KEY (cid) REFERENCES customer(cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


ALTER TABLE orders ADD COLUMN orderRef VARCHAR(255);



-- Dumping data for table `orders`
INSERT INTO orders (oid, odate, cid, ostatus) VALUES
(1, '2025-04-12 17:50:00', 1, 1),
(2, '2025-04-13 12:01:00', 2, 3);




CREATE TABLE table_orders (
  toid INT NOT NULL AUTO_INCREMENT,                      -- Unique ID for table order
  table_number INT NOT NULL,                             -- Physical table number
  oid INT DEFAULT NULL,                                  -- Linked order ID (nullable until ordering starts)
  staff_id INT DEFAULT NULL,                             -- Staff member (nullable until assigned)
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




CREATE TABLE `seatingChart` (
  `tid` int(11) NOT NULL AUTO_INCREMENT,
  `capacity` int(11) NOT NULL COMMENT 'Table capacity',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'state',
  PRIMARY KEY (`tid`)
);

INSERT INTO `seatingChart` (`capacity`, `status`) VALUES
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


CREATE TABLE `booking` (
  `bid` int(11) NOT NULL AUTO_INCREMENT,
  `cid` int(11) DEFAULT NULL COMMENT 'Customer ID',
  `bkcname` varchar(255) NOT NULL COMMENT 'Customer Name',
  `bktel` int(11) NOT NULL COMMENT 'telephone number',
  `tid` int(11) NOT NULL COMMENT 'Table ID',
  `bdate` date NOT NULL COMMENT 'Booking date',
  `btime` time NOT NULL COMMENT 'Booking time',
  `pnum` int(11) NOT NULL COMMENT 'Number of guests',
  `purpose` varchar(255) DEFAULT NULL COMMENT 'Purpose of booking',
  `remark` varchar(255) DEFAULT NULL COMMENT 'Remark of booking',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT 'state',
  PRIMARY KEY (`bid`),
  KEY `bkcname` (`bkcname`),
  KEY `tid` (`tid`),
  CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`cid`) REFERENCES `customer` (`cid`),
  CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`tid`) REFERENCES `seatingChart` (`tid`)
);


-- Table structure for table `material`
DROP TABLE IF EXISTS `material`;
CREATE TABLE `material` (
  `mid` int NOT NULL AUTO_INCREMENT,
  `mname` varchar(255) NOT NULL,
  `mqty` int NOT NULL,
  `mrqty` int NOT NULL,
  `munit` varchar(20) NOT NULL,
  `mreorderqty` int NOT NULL,
  PRIMARY KEY (`mid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `material`
INSERT INTO `material` VALUES 
(1,'Rubber 3233',1000,0,'KG',200),
(2,'Cotten CDC24',2000,200,'KG',400),
(3,'Wood RAW77',5000,0,'KG',1000),
(4,'ABS LL Chem 5026',2000,200,'KG',400),
(5,'4 x 1 Flat Head Stainless Steel Screws',50000,2400,'PC',20000);


-- Create order_items table (order details)
CREATE TABLE order_items (
    oid INT NOT NULL,                         
    item_id INT NOT NULL,                     
    qty INT NOT NULL DEFAULT 1,               
    PRIMARY KEY (oid, item_id),
    FOREIGN KEY (oid) REFERENCES orders(oid),
    FOREIGN KEY (item_id) REFERENCES menu_item_translation(item_id)
);




-- Table structure for table `prodmat`
DROP TABLE IF EXISTS `prodmat`;
CREATE TABLE `prodmat` (
  `pid` int NOT NULL,
  `mid` int NOT NULL,
  `pmqty` int DEFAULT NULL,
  PRIMARY KEY (`pid`,`mid`),
  CONSTRAINT `fk_prodmat_mid` FOREIGN KEY (`mid`) REFERENCES `material` (`mid`),
  CONSTRAINT `fk_prodmat_pid` FOREIGN KEY (`pid`) REFERENCES `product` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `prodmat`
INSERT INTO `prodmat` VALUES 
(1,4,1),(1,5,6),
(2,3,1),(2,5,4),
(3,4,1),(3,5,12),
(4,4,1),(4,5,8),
(5,2,1),(5,5,6);

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



INSERT INTO menu_category (category_name) VALUES
('Appetizers'),
('Soup'),
('Main Courses'),
('Dessert');



-- Appetizers
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, tags, is_available) VALUES
(1, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/PickledCucumberFlowers.jpg', 'Mild', 'vegetarian,refreshing', TRUE),
(1, 26.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/spicy_wood_ear.jpg', 'Mild', 'vegetarian,refreshing', TRUE),
(1, 32.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/kou_shui_chicken.jpg', 'Spicy', 'chicken,cold,spicy', TRUE);

-- Soup
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, tags, is_available) VALUES
(2, 48.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/suan_cai_fish_soup.jpg', 'Medium', 'fish,sour,spicy', TRUE);

-- Main Courses
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, tags, is_available) VALUES
(3, 95.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/Chongqing-styleAngusBeef.jpg', 'Numbing', 'beef,spicy', TRUE),
(3, 42.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/MapoTofu.jpg', 'Spicy', 'tofu,beef,numbing', TRUE),
(3, 38.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/DanDanNoodles.jpg', 'Hot', 'noodles,pork,streetfood', TRUE),
(3, 88.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/TwiceCookedPork.jpg', 'Medium', 'pork,stirfry,classic', TRUE),
(3, 58.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/shui_zhu_beef.jpg', 'Hot', 'beef,spicy,numbing', TRUE),
(3, 66.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/fish_flavored_eggplant.jpg', 'Medium', 'vegetarian,eggplant,sweet', TRUE);

-- Dessert
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, tags, is_available) VALUES
(4, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Website/Staff/SampleImages/dish/sichuan_glutinous_ricecake.jpg', 'None', 'sweet,glutinous', TRUE);





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



-- Dumping data for table `order_items`
INSERT INTO order_items (oid, item_id, qty) VALUES
(1, 1, 200),
(2, 2, 200);

COMMIT;