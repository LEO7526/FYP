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
  PRIMARY KEY (`sid`),
  UNIQUE KEY `semail_UNIQUE` (`semail`) -- Ensure emails are unique
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `staff`
INSERT INTO staff (semail, spassword, sname, srole, stel) VALUES
('peter.wong@example.com', 'password123', 'Peter Wong', 'Sales Manager', 25669197),
('tina.chan@example.com', 'letmein456', 'Tina Chan', 'Production Supervisor', 31233123),
('alex.lam@example.com', 'qwerty789', 'Alex Lam', 'Warehouse Clerk', 29881234),
('susan.leung@example.com', 'helloWorld1', 'Susan Leung', 'HR Officer', 28889999),
('john.ho@example.com', 'changeme2023', 'John Ho', 'Engineer', 29998888),
('maggie.tse@example.com', 'maggiePass!', 'Maggie Tse', 'Accountant', 23881211),
('kevin.ng@example.com', 'ngfamily', 'Kevin Ng', 'IT Support', 27889977),
('emily.tsui@example.com', 'emily2024', 'Emily Tsui', 'Marketing Lead', 26543210);

-- Change srole to 'staff' for Peter Wong
UPDATE staff SET srole='staff' WHERE semail='peter.wong@example.com';

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
  PRIMARY KEY (cid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `customer`
INSERT INTO customer (cname, cpassword, ctel, caddr, company, cemail, crole) VALUES
('Alex Wong', 'password', 21232123, 'G/F, ABC Building, King Yip Street, KwunTong, Kowloon, Hong Kong', 'Fat Cat Company Limited', 'alex.wong@example.com', 'customer'),
('Tina Chan', 'password', 31233123, '303, Mei Hing Center, Yuen Long, NT, Hong Kong', 'XDD LOL Company', 'tina.chan@example.com', 'customer'),
('Bowie', 'password', 61236123, '401, Sing Kei Building, Kowloon, Hong Kong', 'GPA4 Company', 'bowie@example.com', 'customer'),
('Samuel Lee', 'samuelpass', 61231212, '111, Example Road, Central, Hong Kong', 'Lee Family Co', 'samuel.lee@example.com', 'customer'),
('Emily Tsang', 'emilypass', 61231555, '88, Happy Valley Road, Hong Kong', 'Happy Valley Enterprises', 'emily.tsang@example.com', 'customer');

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



-- Insert categories
INSERT INTO menu_category (category_name) VALUES
('Appetizers'),
('Starter'),
('Soup'),
('Main Courses'),
('Seasonal Vegetables'),
('Rice'),
('Dessert');

-- Insert menu items
INSERT INTO menu_item (category_id, item_price, image_url, spice_level, tags, is_available) VALUES
(1, 28.00, 'images/cucumber_flowers.jpg', 'Mild', 'vegetarian,refreshing', TRUE),
(4, 95.00, 'images/chongqing_beef.jpg', 'Numbing', 'beef,spicy', TRUE);

-- Pickled Cucumber Flowers
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(1, 'en', 'Pickled Cucumber Flowers', 'Delicate cucumber blossoms pickled with aromatic spices.'),
(1, 'zh-CN', '腌制黄瓜花', '用香料腌制的黄瓜花，清爽可口。'),
(1, 'zh-TW', '醃製黃瓜花', '以香料醃製的黃瓜花，清新爽口。');

-- Chongqing-style Angus Beef
INSERT INTO menu_item_translation (item_id, language_code, item_name, item_description) VALUES
(2, 'en', 'Chongqing-style Angus Beef', 'Spicy Angus beef with bean paste and lemongrass, known for its numbing effect.'),
(2, 'zh-CN', '重庆风味安格斯牛肉', '辣味安格斯牛肉配豆瓣酱和香茅，麻辣持久。'),
(2, 'zh-TW', '重慶風味安格斯牛肉', '辣味安格斯牛肉搭配豆瓣醬與香茅，麻辣持久。');

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
  `cid` int(11) NOT NULL COMMENT 'Customer ID',
  `tid` int(11) NOT NULL COMMENT 'Table ID',
  `bdate` date NOT NULL COMMENT 'Booking date',
  `btime` time NOT NULL COMMENT 'Booking time',
  `pnum` int(11) NOT NULL COMMENT 'Number of guests',
  `purpose` varchar(255) DEFAULT NULL COMMENT 'Purpose of booking',
  `remark` varchar(255) DEFAULT NULL COMMENT 'Remark of booking',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT 'state',
  PRIMARY KEY (`bid`),
  KEY `cid` (`cid`),
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

-- Table structure for table `orders`
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `oid` int NOT NULL AUTO_INCREMENT,
  `odate` datetime NOT NULL,
  `pid` int NOT NULL,
  `oqty` int NOT NULL,
  `ocost` decimal(20,2) NOT NULL,
  `cid` int NOT NULL,
  `odeliverdate` datetime DEFAULT NULL,
  `ostatus` int NOT NULL,
  PRIMARY KEY (`oid`),
  CONSTRAINT `fk_orders_cid` FOREIGN KEY (`cid`) REFERENCES `customer` (`cid`),
  CONSTRAINT `fk_orders_pid` FOREIGN KEY (`pid`) REFERENCES `product` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `orders`
INSERT INTO `orders` VALUES 
(1,'2025-04-12 17:50:00',1,200,3980.00,1,NULL,1),
(2,'2025-04-13 12:01:00',5,200,99800.00,2,'2025-06-22 12:30:00',3);

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

COMMIT;