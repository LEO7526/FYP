-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
-- Host: localhost    Database: projectdb
-- Server version	8.4.3

-- "NO_AUTO_VALUE_ON_ZERO" suppress generate the next sequence number for AUTO_INCREMENT column
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
  `spassword` varchar(255) NOT NULL,
  `sname` varchar(255) NOT NULL,
  `srole` varchar(45) DEFAULT NULL,
  `stel` int DEFAULT NULL,
  PRIMARY KEY (`sid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `staff`
INSERT INTO `staff` VALUES (1,'password','Peter Wong','Sales Manager',25669197);

-- Table structure for table `customer`
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
  `cid` int NOT NULL AUTO_INCREMENT,
  `cname` varchar(255) NOT NULL,
  `cpassword` varchar(255) NOT NULL,
  `ctel` int DEFAULT NULL,
  `caddr` text,
  `company` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data for table `customer`
INSERT INTO `customer` VALUES 
(1,'Alex Wong','password',21232123,'G/F, ABC Building, King Yip Street, KwunTong, Kowloon, Hong Kong','Fat Cat Company Limited'),
(2,'Tina Chan','password',31233123,'303, Mei Hing Center, Yuen Long, NT, Hong Kong','XDD LOL Company'),
(3,'Bowie','password',61236123,'401, Sing Kei Building, Kowloon, Hong Kong','GPA4 Company');

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
(1,'Cyberpunk Truck C204','Explore the world of imaginative play with our vibrant and durable toy truck. Perfect for little hands, this truck will inspire endless storytelling adventures both indoors and outdoors. Made from high-quality materials, it is built to withstand hours of creative playtime.',19.90),
(2,'XDD Wooden Plane','Take to the skies with our charming wooden plane toy. Crafted from eco-friendly and child-safe materials, this beautifully designed plane sparks the imagination and encourages interactive play. With smooth edges and a sturdy construction, it\'s a delightful addition to any young aviator\'s toy collection.',9.90),
(3,'iRobot 3233GG','Introduce your child to the wonders of technology and robotics with our smart robot companion. Packed with interactive features and educational benefits, this futuristic toy engages curious minds and promotes STEM learning in a fun and engaging way. Watch as your child explores coding, problem-solving, and innovation with this cutting-edge robot friend.',249.90),
(4,'Apex Ball Ball Helicopter M1297','Experience the thrill of flight with our ball helicopter toy. Easy to launch and navigate, this exciting toy provides hours of entertainment for children of all ages. With colorful LED lights and a durable design, it\'s a fantastic outdoor toy that brings joy and excitement to playtime.',30.00),
(5,'RoboKat AI Cat Robot','Meet our AI Cat Robot – the purr-fect blend of technology and cuddly companionship. This interactive robotic feline offers lifelike movements, sounds, and responses, providing a realistic pet experience without the hassle. With customizable features and playful interactions, this charming cat robot is a delightful addition to your child\'s playroom.',499.00);

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
