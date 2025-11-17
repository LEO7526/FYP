-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- 主機： 127.0.0.1
-- 產生時間： 2025-11-17 18:51:52
-- 伺服器版本： 10.4.32-MariaDB
-- PHP 版本： 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 資料庫： `projectdb`
--

-- --------------------------------------------------------

--
-- 資料表結構 `booking`
--

CREATE TABLE `booking` (
  `bid` int(11) NOT NULL,
  `cid` int(11) DEFAULT NULL COMMENT 'Customer ID',
  `bkcname` varchar(255) NOT NULL COMMENT 'Customer Name',
  `bktel` int(11) NOT NULL COMMENT 'telephone number',
  `tid` int(11) NOT NULL COMMENT 'Table ID',
  `bdate` date NOT NULL COMMENT 'Booking date',
  `btime` time NOT NULL COMMENT 'Booking time',
  `pnum` int(11) NOT NULL COMMENT 'Number of guests',
  `purpose` varchar(255) DEFAULT NULL COMMENT 'Purpose of booking',
  `remark` varchar(255) DEFAULT NULL COMMENT 'Remark of booking',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT 'state'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `booking`
--

INSERT INTO `booking` (`bid`, `cid`, `bkcname`, `bktel`, `tid`, `bdate`, `btime`, `pnum`, `purpose`, `remark`, `status`) VALUES
(1, 1, 'Alex Wong', 21232123, 5, '2024-01-15', '18:30:00', 4, 'Family Dinner', 'We have a baby with us, need a high chair', 2),
(2, 2, 'Tina Chan', 31233123, 12, '2024-01-16', '19:00:00', 2, 'Date Night', NULL, 3),
(3, 3, 'Bowie', 61236123, 8, '2024-01-17', '20:00:00', 6, 'Business Meeting', 'Need a quiet area for discussion', 1),
(4, 4, 'Samuel Lee', 61231212, 25, '2024-01-18', '12:30:00', 3, 'Lunch Meeting', NULL, 2),
(5, 5, 'Emily Tsang', 61231555, 30, '2024-01-19', '13:00:00', 4, 'Birthday Celebration', 'Will bring a cake', 3),
(6, NULL, 'Michael Johnson', 5551234, 3, '2024-01-15', '19:30:00', 2, 'Casual Dinner', NULL, 0),
(7, NULL, 'Sarah Williams', 5555678, 15, '2024-01-16', '20:30:00', 4, 'Family Gathering', NULL, 1),
(8, NULL, 'David Brown', 5559012, 40, '2024-01-17', '18:00:00', 8, 'Company Party', NULL, 2),
(9, NULL, 'Jennifer Davis', 5553456, 10, '2024-01-18', '19:00:00', 2, 'Anniversary', NULL, 3),
(10, NULL, 'Robert Miller', 5557890, 20, '2024-01-19', '12:00:00', 4, 'Business Lunch', 'Need power outlet for laptop', 1);

-- --------------------------------------------------------

--
-- 資料表結構 `consumption_history`
--

CREATE TABLE `consumption_history` (
  `log_id` int(11) NOT NULL,
  `log_date` date NOT NULL,
  `log_type` enum('Deduction','Forecast','Reorder') NOT NULL,
  `details` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `coupons`
--

CREATE TABLE `coupons` (
  `coupon_id` int(11) NOT NULL,
  `points_required` int(11) NOT NULL DEFAULT 0,
  `type` enum('cash','percent','free_item') NOT NULL DEFAULT 'cash',
  `discount_amount` int(11) DEFAULT 0,
  `item_category` varchar(50) DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `coupons`
--

INSERT INTO `coupons` (`coupon_id`, `points_required`, `type`, `discount_amount`, `item_category`, `expiry_date`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 100, 'percent', 10, NULL, '2025-12-31', 1, '2025-11-17 17:47:01', '2025-11-17 17:47:01'),
(2, 50, 'free_item', 0, 'drink', '2025-12-30', 1, '2025-11-17 17:47:01', '2025-11-17 17:47:01'),
(3, 200, 'cash', 5000, NULL, '2025-12-31', 1, '2025-11-17 17:47:01', '2025-11-17 17:47:01'),
(4, 0, 'free_item', 0, NULL, NULL, 1, '2025-11-17 17:47:01', '2025-11-17 17:47:01');

-- --------------------------------------------------------

--
-- 資料表結構 `coupon_applicable_categories`
--

CREATE TABLE `coupon_applicable_categories` (
  `id` int(11) NOT NULL,
  `coupon_id` int(11) NOT NULL,
  `category_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `coupon_applicable_categories`
--

INSERT INTO `coupon_applicable_categories` (`id`, `coupon_id`, `category_id`) VALUES
(1, 4, 3);

-- --------------------------------------------------------

--
-- 資料表結構 `coupon_applicable_items`
--

CREATE TABLE `coupon_applicable_items` (
  `id` int(11) NOT NULL,
  `coupon_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `coupon_applicable_items`
--

INSERT INTO `coupon_applicable_items` (`id`, `coupon_id`, `item_id`) VALUES
(1, 2, 12),
(2, 2, 13),
(3, 2, 14),
(4, 2, 15),
(5, 2, 16),
(6, 2, 17),
(7, 2, 18),
(8, 4, 6);

-- --------------------------------------------------------

--
-- 資料表結構 `coupon_applicable_package`
--

CREATE TABLE `coupon_applicable_package` (
  `id` int(11) NOT NULL,
  `coupon_id` int(11) NOT NULL,
  `package_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `coupon_point`
--

CREATE TABLE `coupon_point` (
  `cp_id` int(11) NOT NULL,
  `cid` int(11) NOT NULL,
  `points` int(11) NOT NULL DEFAULT 0,
  `last_changed_by` varchar(255) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `expire_at` datetime DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `coupon_point`
--

INSERT INTO `coupon_point` (`cp_id`, `cid`, `points`, `last_changed_by`, `reason`, `created_at`, `updated_at`, `expire_at`, `is_active`) VALUES
(1, 1, 1000, NULL, NULL, '2025-11-18 01:47:01', '2025-11-18 01:47:01', NULL, 1),
(2, 3, 0, NULL, NULL, '2025-11-18 01:47:01', '2025-11-18 01:47:01', NULL, 1),
(3, 5, 0, NULL, NULL, '2025-11-18 01:47:01', '2025-11-18 01:47:01', NULL, 1),
(4, 4, 0, NULL, NULL, '2025-11-18 01:47:01', '2025-11-18 01:47:01', NULL, 1),
(5, 2, 0, NULL, NULL, '2025-11-18 01:47:01', '2025-11-18 01:47:01', NULL, 1),
(6, 0, 0, NULL, NULL, '2025-11-18 01:47:01', '2025-11-18 01:47:01', NULL, 1);

-- --------------------------------------------------------

--
-- 資料表結構 `coupon_point_history`
--

CREATE TABLE `coupon_point_history` (
  `cph_id` int(11) NOT NULL,
  `cp_id` int(11) NOT NULL,
  `cid` int(11) NOT NULL,
  `coupon_id` int(11) DEFAULT NULL,
  `delta` int(11) NOT NULL,
  `resulting_points` int(11) NOT NULL,
  `action` varchar(50) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `coupon_redemptions`
--

CREATE TABLE `coupon_redemptions` (
  `redemption_id` int(11) NOT NULL,
  `coupon_id` int(11) NOT NULL,
  `cid` int(11) NOT NULL,
  `redeemed_at` datetime NOT NULL DEFAULT current_timestamp(),
  `is_used` tinyint(1) NOT NULL DEFAULT 0,
  `used_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `coupon_rules`
--

CREATE TABLE `coupon_rules` (
  `rule_id` int(11) NOT NULL,
  `coupon_id` int(11) NOT NULL,
  `applies_to` enum('whole_order','category','item','package') NOT NULL DEFAULT 'whole_order',
  `discount_type` enum('percent','cash','free_item') NOT NULL,
  `discount_value` decimal(10,2) DEFAULT NULL,
  `min_spend` decimal(10,2) DEFAULT NULL,
  `max_discount` decimal(10,2) DEFAULT NULL,
  `per_customer_per_day` int(11) DEFAULT NULL,
  `valid_dine_in` tinyint(1) NOT NULL DEFAULT 0,
  `valid_takeaway` tinyint(1) NOT NULL DEFAULT 0,
  `valid_delivery` tinyint(1) NOT NULL DEFAULT 0,
  `combine_with_other_discounts` tinyint(1) NOT NULL DEFAULT 1,
  `birthday_only` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `coupon_rules`
--

INSERT INTO `coupon_rules` (`rule_id`, `coupon_id`, `applies_to`, `discount_type`, `discount_value`, `min_spend`, `max_discount`, `per_customer_per_day`, `valid_dine_in`, `valid_takeaway`, `valid_delivery`, `combine_with_other_discounts`, `birthday_only`) VALUES
(1, 1, 'whole_order', 'percent', 10.00, NULL, NULL, NULL, 1, 1, 0, 0, 0),
(2, 2, 'item', 'free_item', 1.00, NULL, NULL, 1, 1, 1, 1, 1, 0),
(3, 3, 'whole_order', 'cash', 50.00, 300.00, NULL, NULL, 1, 1, 1, 1, 0),
(4, 4, 'category', 'free_item', 1.00, NULL, NULL, NULL, 1, 1, 1, 1, 1);

-- --------------------------------------------------------

--
-- 資料表結構 `coupon_terms`
--

CREATE TABLE `coupon_terms` (
  `term_id` int(11) NOT NULL,
  `coupon_id` int(11) NOT NULL,
  `language_code` varchar(10) NOT NULL,
  `term_text` varchar(500) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `coupon_terms`
--

INSERT INTO `coupon_terms` (`term_id`, `coupon_id`, `language_code`, `term_text`) VALUES
(1, 1, 'en', 'Valid for dine-in and takeaway orders'),
(2, 1, 'en', 'Not applicable to delivery'),
(3, 1, 'en', 'Cannot be combined with other discounts'),
(4, 1, 'zh-CN', '适用于堂食和外卖'),
(5, 1, 'zh-CN', '不适用于外送服务'),
(6, 1, 'zh-CN', '不可与其他优惠同时使用'),
(7, 1, 'zh-TW', '適用於堂食和外賣'),
(8, 1, 'zh-TW', '不適用於外送服務'),
(9, 1, 'zh-TW', '不可與其他優惠同時使用'),
(10, 2, 'en', 'Choice of soft drink, coffee, or tea'),
(11, 2, 'en', 'Limit one free drink per customer per day'),
(12, 2, 'zh-CN', '可选择汽水、咖啡或茶'),
(13, 2, 'zh-CN', '每位顾客每天限兑一杯'),
(14, 2, 'zh-TW', '可選擇汽水、咖啡或茶'),
(15, 2, 'zh-TW', '每位顧客每天限兌一杯'),
(16, 3, 'en', 'Minimum spend of HK$300 required'),
(17, 3, 'en', 'Discount applied before service charge'),
(18, 3, 'zh-CN', '需满300港元方可使用'),
(19, 3, 'zh-CN', '折扣在加收服务费前计算'),
(20, 3, 'zh-TW', '需滿300港元方可使用'),
(21, 3, 'zh-TW', '折扣於加收服務費前計算'),
(22, 4, 'en', 'Valid only during your birthday month'),
(23, 4, 'en', 'Must present valid ID for verification'),
(24, 4, 'zh-CN', '仅限生日月份使用'),
(25, 4, 'zh-CN', '需出示有效身份证明'),
(26, 4, 'zh-TW', '僅限生日月份使用'),
(27, 4, 'zh-TW', '需出示有效身份證明'),
(28, 1, 'en', 'Photos are for reference only; actual products may vary'),
(29, 2, 'en', 'Photos are for reference only; actual products may vary'),
(30, 3, 'en', 'Photos are for reference only; actual products may vary'),
(31, 4, 'en', 'Photos are for reference only; actual products may vary'),
(32, 1, 'en', 'Coupons cannot be exchanged for cash, credit, or other products'),
(33, 2, 'en', 'Coupons cannot be exchanged for cash, credit, or other products'),
(34, 3, 'en', 'Coupons cannot be exchanged for cash, credit, or other products'),
(35, 4, 'en', 'Coupons cannot be exchanged for cash, credit, or other products'),
(36, 1, 'en', 'Yummy Restaurant reserves the right to cancel, amend, or change the terms and conditions without prior notice'),
(37, 2, 'en', 'Yummy Restaurant reserves the right to cancel, amend, or change the terms and conditions without prior notice'),
(38, 3, 'en', 'Yummy Restaurant reserves the right to cancel, amend, or change the terms and conditions without prior notice'),
(39, 4, 'en', 'Yummy Restaurant reserves the right to cancel, amend, or change the terms and conditions without prior notice'),
(40, 1, 'en', 'In case of product unavailability, the company may replace the coupon with an item of equal or greater value'),
(41, 2, 'en', 'In case of product unavailability, the company may replace the coupon with an item of equal or greater value'),
(42, 3, 'en', 'In case of product unavailability, the company may replace the coupon with an item of equal or greater value'),
(43, 4, 'en', 'In case of product unavailability, the company may replace the coupon with an item of equal or greater value'),
(59, 1, 'zh-TW', '圖片只供參考，實際供應可能有所不同'),
(60, 2, 'zh-TW', '圖片只供參考，實際供應可能有所不同'),
(61, 3, 'zh-TW', '圖片只供參考，實際供應可能有所不同'),
(62, 4, 'zh-TW', '圖片只供參考，實際供應可能有所不同'),
(63, 1, 'zh-TW', '優惠券不可兌換現金、信用額或其他產品'),
(64, 2, 'zh-TW', '優惠券不可兌換現金、信用額或其他產品'),
(65, 3, 'zh-TW', '優惠券不可兌換現金、信用額或其他產品'),
(66, 4, 'zh-TW', '優惠券不可兌換現金、信用額或其他產品'),
(67, 1, 'zh-TW', 'Yummy Restaurant 保留隨時取消、更改或修訂條款及細則之權利，恕不另行通知'),
(68, 2, 'zh-TW', 'Yummy Restaurant 保留隨時取消、更改或修訂條款及細則之權利，恕不另行通知'),
(69, 3, 'zh-TW', 'Yummy Restaurant 保留隨時取消、更改或修訂條款及細則之權利，恕不另行通知'),
(70, 4, 'zh-TW', 'Yummy Restaurant 保留隨時取消、更改或修訂條款及細則之權利，恕不另行通知'),
(71, 1, 'zh-TW', '如有產品缺貨，公司可更換為同等或更高價值之食品'),
(72, 2, 'zh-TW', '如有產品缺貨，公司可更換為同等或更高價值之食品'),
(73, 3, 'zh-TW', '如有產品缺貨，公司可更換為同等或更高價值之食品'),
(74, 4, 'zh-TW', '如有產品缺貨，公司可更換為同等或更高價值之食品'),
(90, 1, 'zh-CN', '图片仅供参考，实际供应可能有所不同'),
(91, 2, 'zh-CN', '图片仅供参考，实际供应可能有所不同'),
(92, 3, 'zh-CN', '图片仅供参考，实际供应可能有所不同'),
(93, 4, 'zh-CN', '图片仅供参考，实际供应可能有所不同'),
(94, 1, 'zh-CN', '优惠券不可兑换现金、信用额或其他产品'),
(95, 2, 'zh-CN', '优惠券不可兑换现金、信用额或其他产品'),
(96, 3, 'zh-CN', '优惠券不可兑换现金、信用额或其他产品'),
(97, 4, 'zh-CN', '优惠券不可兑换现金、信用额或其他产品'),
(98, 1, 'zh-CN', 'Yummy Restaurant 保留随时取消、更改或修订条款及细则的权利，恕不另行通知'),
(99, 2, 'zh-CN', 'Yummy Restaurant 保留随时取消、更改或修订条款及细则的权利，恕不另行通知'),
(100, 3, 'zh-CN', 'Yummy Restaurant 保留随时取消、更改或修订条款及细则的权利，恕不另行通知'),
(101, 4, 'zh-CN', 'Yummy Restaurant 保留随时取消、更改或修订条款及细则的权利，恕不另行通知'),
(102, 1, 'zh-CN', '如有产品缺货，公司可更换为同等或更高价值的食品'),
(103, 2, 'zh-CN', '如有产品缺货，公司可更换为同等或更高价值的食品'),
(104, 3, 'zh-CN', '如有产品缺货，公司可更换为同等或更高价值的食品'),
(105, 4, 'zh-CN', '如有产品缺货，公司可更换为同等或更高价值的食品');

-- --------------------------------------------------------

--
-- 資料表結構 `coupon_translation`
--

CREATE TABLE `coupon_translation` (
  `translation_id` int(11) NOT NULL,
  `coupon_id` int(11) NOT NULL,
  `language_code` varchar(10) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `coupon_translation`
--

INSERT INTO `coupon_translation` (`translation_id`, `coupon_id`, `language_code`, `title`, `description`) VALUES
(1, 1, 'en', '10% OFF Any Order', 'Get 10% discount on your next order.'),
(2, 1, 'zh-CN', '全单九折', '下次消费可享受九折优惠。'),
(3, 1, 'zh-TW', '全單九折', '下次消費可享受九折優惠。'),
(4, 2, 'en', 'Free Drink', 'Redeem one free drink of your choice.'),
(5, 2, 'zh-CN', '免费饮品', '兑换一杯您选择的免费饮品。'),
(6, 2, 'zh-TW', '免費飲品', '兌換一杯您選擇的免費飲品。'),
(7, 3, 'en', 'HK$50 OFF', 'Enjoy HK$50 off when you spend HK$300 or more.'),
(8, 3, 'zh-CN', '立减50港元', '消费满300港元即可减50港元。'),
(9, 3, 'zh-TW', '立減50港元', '消費滿300港元即可減50港元。'),
(10, 4, 'en', 'Birthday Special', 'Exclusive coupon for your birthday month.'),
(11, 4, 'zh-CN', '生日特惠', '生日月份专属优惠券。'),
(12, 4, 'zh-TW', '生日特惠', '生日月份專屬優惠券。');

-- --------------------------------------------------------

--
-- 資料表結構 `customer`
--

CREATE TABLE `customer` (
  `cid` int(11) NOT NULL,
  `cname` varchar(255) NOT NULL,
  `cpassword` varchar(255) NOT NULL,
  `ctel` int(11) DEFAULT NULL,
  `caddr` text DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `cemail` varchar(191) NOT NULL,
  `cbirthday` char(5) DEFAULT NULL,
  `crole` varchar(45) NOT NULL DEFAULT 'customer',
  `cimageurl` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `customer`
--

INSERT INTO `customer` (`cid`, `cname`, `cpassword`, `ctel`, `caddr`, `company`, `cemail`, `cbirthday`, `crole`, `cimageurl`) VALUES
(0, 'Walk-in Customer', 'walkin', NULL, NULL, NULL, 'walkin@system.local', NULL, 'customer', NULL),
(1, 'Alex Wong', 'password', 21232123, 'G/F, ABC Building, King Yip Street, KwunTong, Kowloon, Hong Kong', 'Fat Cat Company Limited', 'alex.wong@example.com', NULL, 'customer', NULL),
(2, 'Tina Chan', 'password', 31233123, '303, Mei Hing Center, Yuen Long, NT, Hong Kong', 'XDD LOL Company', 'tina.chan@example.com', '07-20', 'customer', NULL),
(3, 'Bowie', 'password', 61236123, '401, Sing Kei Building, Kowloon, Hong Kong', 'GPA4 Company', 'bowie@example.com', '03-15', 'customer', NULL),
(4, 'Samuel Lee', 'samuelpass', 61231212, '111, Example Road, Central, Hong Kong', 'Lee Family Co', 'samuel.lee@example.com', '11-02', 'customer', NULL),
(5, 'Emily Tsang', 'emilypass', 61231555, '88, Happy Valley Road, Hong Kong', 'Happy Valley Enterprises', 'emily.tsang@example.com', '01-30', 'customer', NULL);

-- --------------------------------------------------------

--
-- 資料表結構 `materials`
--

CREATE TABLE `materials` (
  `mid` int(11) NOT NULL,
  `mname` varchar(255) NOT NULL,
  `mqty` decimal(10,2) NOT NULL,
  `munit` varchar(50) NOT NULL,
  `mreorderqty` decimal(10,2) NOT NULL,
  `last_updated` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `materials`
--

INSERT INTO `materials` (`mid`, `mname`, `mqty`, `munit`, `mreorderqty`, `last_updated`) VALUES
(1, 'Pork', 50.00, 'kg', 10.00, '2025-11-17 17:48:15'),
(2, 'Tofu', 100.00, 'block', 20.00, '2025-11-17 17:48:15'),
(3, 'Chili Bean Paste', 20.00, 'kg', 5.00, '2025-11-17 17:48:15'),
(4, 'Sichuan Peppercorns', 5.00, 'kg', 1.00, '2025-11-17 17:48:15'),
(5, 'Chicken', 40.00, 'kg', 10.00, '2025-11-17 17:48:15'),
(6, 'Cucumber', 30.00, 'kg', 5.00, '2025-11-17 17:48:15');

-- --------------------------------------------------------

--
-- 資料表結構 `menu_category`
--

CREATE TABLE `menu_category` (
  `category_id` int(11) NOT NULL,
  `category_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `menu_category`
--

INSERT INTO `menu_category` (`category_id`, `category_name`) VALUES
(1, 'Appetizers'),
(2, 'Soup'),
(3, 'Main Courses'),
(4, 'Dessert'),
(5, 'Drink');

-- --------------------------------------------------------

--
-- 資料表結構 `menu_item`
--

CREATE TABLE `menu_item` (
  `item_id` int(11) NOT NULL,
  `category_id` int(11) NOT NULL,
  `item_price` decimal(10,2) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `spice_level` int(11) NOT NULL CHECK (`spice_level` between 0 and 5),
  `is_available` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `menu_item`
--

INSERT INTO `menu_item` (`item_id`, `category_id`, `item_price`, `image_url`, `spice_level`, `is_available`) VALUES
(1, 1, 28.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/1.jpg', 1, 1),
(2, 1, 26.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/2.jpg', 1, 1),
(3, 1, 32.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/3.jpg', 3, 1),
(4, 2, 48.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/4.jpg', 2, 1),
(5, 3, 95.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/5.jpg', 5, 1),
(6, 3, 42.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/6.jpg', 3, 1),
(7, 3, 38.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/7.jpg', 4, 1),
(8, 3, 88.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/8.jpg', 2, 1),
(9, 3, 58.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/9.jpg', 4, 1),
(10, 3, 66.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/10.jpg', 2, 1),
(11, 4, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/11.jpg', 0, 1),
(12, 5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/12.jpg', 0, 1),
(13, 5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/13.jpg', 0, 1),
(14, 5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/14.jpg', 0, 1),
(15, 5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/15.jpg', 0, 1),
(16, 5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/16.jpg', 0, 1),
(17, 5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/17.jpg', 0, 1),
(18, 5, 22.00, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/dish/18.jpg', 0, 1);

-- --------------------------------------------------------

--
-- 資料表結構 `menu_item_translation`
--

CREATE TABLE `menu_item_translation` (
  `translation_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `language_code` varchar(10) NOT NULL,
  `item_name` varchar(255) NOT NULL,
  `item_description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `menu_item_translation`
--

INSERT INTO `menu_item_translation` (`translation_id`, `item_id`, `language_code`, `item_name`, `item_description`) VALUES
(1, 1, 'en', 'Pickled Cucumber Flowers', 'Delicate cucumber blossoms pickled with aromatic spices.'),
(2, 1, 'zh-CN', '腌制黄瓜花', '用香料腌制的黄瓜花，清爽可口。'),
(3, 1, 'zh-TW', '醃製黃瓜花', '以香料醃製的黃瓜花，清新爽口。'),
(4, 2, 'en', 'Spicy Wood Ear Mushrooms', 'Black fungus tossed in vinegar, garlic, and chili oil.'),
(5, 2, 'zh-CN', '麻辣木耳', '黑木耳拌醋、蒜和辣油，爽口开胃。'),
(6, 2, 'zh-TW', '麻辣木耳', '黑木耳拌醋、蒜與辣油，爽口開胃。'),
(7, 3, 'en', 'Mouthwatering Chicken', 'Poached chicken drenched in spicy Sichuan chili sauce.'),
(8, 3, 'zh-CN', '口水鸡', '嫩鸡浸泡在麻辣红油中，香辣诱人。'),
(9, 3, 'zh-TW', '口水雞', '嫩雞浸泡在麻辣紅油中，香辣誘人。'),
(10, 4, 'en', 'Suan Cai Fish Soup', 'Sliced fish simmered in pickled mustard greens and chili broth.'),
(11, 4, 'zh-CN', '酸菜鱼汤', '鱼片炖酸菜和辣汤，酸辣开胃。'),
(12, 4, 'zh-TW', '酸菜魚湯', '魚片燉酸菜與辣湯，酸辣開胃。'),
(13, 5, 'en', 'Chongqing-style Angus Beef', 'Spicy Angus beef with bean paste and lemongrass, known for its numbing effect.'),
(14, 5, 'zh-CN', '重庆风味安格斯牛肉', '辣味安格斯牛肉配豆瓣酱和香茅，麻辣持久。'),
(15, 5, 'zh-TW', '重慶風味安格斯牛肉', '辣味安格斯牛肉搭配豆瓣醬與香茅，麻辣持久。'),
(16, 6, 'en', 'Mapo Tofu', 'Silken tofu in spicy bean paste sauce with minced beef and numbing Sichuan peppercorns.'),
(17, 6, 'zh-CN', '麻婆豆腐', '嫩豆腐配牛肉末和麻辣豆瓣酱，风味十足。'),
(18, 6, 'zh-TW', '麻婆豆腐', '嫩豆腐搭配牛肉末與麻辣豆瓣醬，風味十足。'),
(19, 7, 'en', 'Dan Dan Noodles', 'Spicy noodles topped with minced pork, preserved vegetables, and chili oil.'),
(20, 7, 'zh-CN', '担担面', '辣味面条配猪肉末、芽菜和红油，香辣诱人。'),
(21, 7, 'zh-TW', '擔擔麵', '辣味麵條搭配豬肉末、芽菜與紅油，香辣誘人。'),
(22, 8, 'en', 'Twice-Cooked Pork', 'Pork belly simmered then stir-fried with leeks and chili bean paste for a rich, savory flavor.'),
(23, 8, 'zh-CN', '回锅肉', '五花肉先煮后炒，搭配蒜苗和豆瓣酱，香浓可口。'),
(24, 8, 'zh-TW', '回鍋肉', '五花肉先煮後炒，搭配蒜苗與豆瓣醬，香濃可口。'),
(25, 9, 'en', 'Boiled Beef in Chili Broth', 'Tender beef slices in a fiery broth with Sichuan peppercorns.'),
(26, 9, 'zh-CN', '水煮牛肉', '牛肉片浸泡在麻辣红汤中，香辣过瘾。'),
(27, 9, 'zh-TW', '水煮牛肉', '牛肉片浸泡在麻辣紅湯中，香辣過癮。'),
(28, 10, 'en', 'Fish-Fragrant Eggplant', 'Braised eggplant in garlic, ginger, and sweet chili sauce.'),
(29, 10, 'zh-CN', '鱼香茄子', '茄子炖煮于蒜姜和甜辣酱中，香气扑鼻。'),
(30, 10, 'zh-TW', '魚香茄子', '茄子燉煮於蒜薑與甜辣醬中，香氣撲鼻。'),
(31, 11, 'en', 'Sichuan Glutinous Rice Cake', 'Sticky rice cake with brown sugar and sesame.'),
(32, 11, 'zh-CN', '四川糯米糕', '糯米糕配红糖和芝麻，甜而不腻。'),
(33, 11, 'zh-TW', '四川糯米糕', '糯米糕搭配紅糖與芝麻，甜而不膩。'),
(34, 12, 'en', 'Salty Lemon 7-Up', 'Classic Hong Kong salty lemon soda with 7-Up.'),
(35, 12, 'zh-CN', '咸柠7', '港式经典咸柠七喜，清爽解渴。'),
(36, 12, 'zh-TW', '咸檸7', '港式經典鹹檸七喜，清爽解渴。'),
(37, 13, 'en', 'Red Bean Ice', 'Sweet red beans served over crushed ice.'),
(38, 13, 'zh-CN', '红豆冰', '香甜红豆配上碎冰，夏日必备。'),
(39, 13, 'zh-TW', '紅豆冰', '香甜紅豆配上碎冰，夏日必備。'),
(40, 14, 'en', 'Hot Milk Tea', 'Rich Hong Kong-style milk tea, best served hot.'),
(41, 14, 'zh-CN', '热奶茶', '浓郁港式奶茶，热饮最佳。'),
(42, 14, 'zh-TW', '熱奶茶', '濃郁港式奶茶，熱飲最佳。'),
(43, 15, 'en', 'Grape Oolong Tea', 'Oolong tea infused with grape aroma, refreshing and unique.'),
(44, 15, 'zh-CN', '葡萄乌龙茶', '乌龙茶融合葡萄香气，清新独特。'),
(45, 15, 'zh-TW', '葡萄烏龍茶', '烏龍茶融合葡萄香氣，清新獨特。'),
(46, 16, 'en', 'Hot Lemon Tea', 'Hot lemon tea, tangy and comforting.'),
(47, 16, 'zh-CN', '热柠茶', '热柠檬茶，酸甜暖心。'),
(48, 16, 'zh-TW', '熱檸茶', '熱檸檬茶，酸甜暖心。'),
(49, 17, 'en', 'Iced Milk Tea', 'Classic Hong Kong-style milk tea, served chilled.'),
(50, 17, 'zh-CN', '冻奶茶', '经典港式奶茶，冰凉爽口。'),
(51, 17, 'zh-TW', '凍奶茶', '經典港式奶茶，冰涼爽口。'),
(52, 18, 'en', 'Iced Lemon Tea', 'Crisp iced tea with fresh lemon slices.'),
(53, 18, 'zh-CN', '冻柠茶', '冰镇柠檬茶，清爽解渴。'),
(54, 18, 'zh-TW', '凍檸茶', '冰鎮檸檬茶，清爽解渴。');

-- --------------------------------------------------------

--
-- 資料表結構 `menu_package`
--

CREATE TABLE `menu_package` (
  `package_id` int(11) NOT NULL,
  `package_name` varchar(255) NOT NULL,
  `num_of_type` int(11) NOT NULL,
  `package_image_url` varchar(255) DEFAULT NULL,
  `amounts` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `menu_package`
--

INSERT INTO `menu_package` (`package_id`, `package_name`, `num_of_type`, `package_image_url`, `amounts`) VALUES
(1, 'Double Set', 3, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/package/1.jpg', 180.00),
(2, 'Four Person Set', 4, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/package/2.jpg', 380.00),
(3, 'Business Set', 2, 'https://raw.githubusercontent.com/LEO7526/FYP/main/Image/package/3.jpg', 120.00);

-- --------------------------------------------------------

--
-- 資料表結構 `menu_tag`
--

CREATE TABLE `menu_tag` (
  `item_id` int(11) NOT NULL,
  `tag_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `menu_tag`
--

INSERT INTO `menu_tag` (`item_id`, `tag_id`) VALUES
(1, 1),
(1, 2),
(2, 1),
(2, 2),
(3, 3),
(3, 4),
(3, 5),
(4, 5),
(4, 6),
(4, 7),
(5, 5),
(5, 8),
(5, 9),
(6, 8),
(6, 9),
(6, 10),
(7, 5),
(7, 11),
(7, 12),
(8, 12),
(8, 13),
(8, 14),
(8, 15),
(9, 5),
(9, 8),
(9, 9),
(10, 1),
(10, 16),
(11, 16),
(11, 17),
(12, 2),
(12, 4),
(12, 18),
(12, 21),
(13, 4),
(13, 16),
(13, 22),
(14, 15),
(14, 20),
(15, 2),
(15, 4),
(15, 19),
(16, 7),
(16, 15),
(16, 18),
(17, 4),
(17, 15),
(17, 20),
(18, 2),
(18, 4),
(18, 18);

-- --------------------------------------------------------

--
-- 資料表結構 `orders`
--

CREATE TABLE `orders` (
  `oid` int(11) NOT NULL,
  `odate` datetime NOT NULL,
  `cid` int(11) NOT NULL,
  `ostatus` int(11) NOT NULL,
  `orderRef` varchar(100) NOT NULL,
  `coupon_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `orders`
--

INSERT INTO `orders` (`oid`, `odate`, `cid`, `ostatus`, `orderRef`, `coupon_id`) VALUES
(1, '2025-04-12 17:50:00', 1, 1, 'order_20250412A', NULL),
(2, '2025-04-13 12:01:00', 2, 3, 'order_20250413B', 1);

-- --------------------------------------------------------

--
-- 資料表結構 `order_coupons`
--

CREATE TABLE `order_coupons` (
  `id` int(11) NOT NULL,
  `oid` int(11) NOT NULL,
  `coupon_id` int(11) NOT NULL,
  `redemption_id` int(11) DEFAULT NULL,
  `discount_amount` decimal(10,2) DEFAULT NULL,
  `applied_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `order_coupons`
--

INSERT INTO `order_coupons` (`id`, `oid`, `coupon_id`, `redemption_id`, `discount_amount`, `applied_at`) VALUES
(1, 1, 1, NULL, 20.00, '2025-11-18 01:47:01'),
(2, 2, 2, NULL, 22.00, '2025-11-18 01:47:01');

-- --------------------------------------------------------

--
-- 資料表結構 `order_items`
--

CREATE TABLE `order_items` (
  `oid` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `qty` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `order_items`
--

INSERT INTO `order_items` (`oid`, `item_id`, `qty`) VALUES
(1, 1, 2),
(1, 3, 1),
(2, 4, 1),
(2, 6, 3);

-- --------------------------------------------------------

--
-- 資料表結構 `package_dish`
--

CREATE TABLE `package_dish` (
  `package_id` int(11) NOT NULL,
  `type_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `price_modifier` decimal(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `package_dish`
--

INSERT INTO `package_dish` (`package_id`, `type_id`, `item_id`, `price_modifier`) VALUES
(1, 1, 1, 0.00),
(1, 1, 2, 3.00),
(1, 1, 3, 8.00),
(1, 2, 5, 0.00),
(1, 2, 6, 8.00),
(1, 2, 7, 6.00),
(1, 2, 8, 12.00),
(1, 2, 9, 10.00),
(1, 2, 10, 7.00),
(1, 3, 12, 0.00),
(1, 3, 13, 2.00),
(1, 3, 14, 3.00),
(1, 3, 15, 4.00),
(1, 3, 16, 2.00),
(1, 3, 17, 3.00),
(1, 3, 18, 2.00),
(2, 4, 1, 0.00),
(2, 4, 2, 5.00),
(2, 4, 3, 12.00),
(2, 5, 4, 0.00),
(2, 6, 5, 0.00),
(2, 6, 6, 15.00),
(2, 6, 7, 12.00),
(2, 6, 8, 20.00),
(2, 6, 9, 18.00),
(2, 6, 10, 14.00),
(2, 7, 12, 0.00),
(2, 7, 13, 3.00),
(2, 7, 14, 4.00),
(2, 7, 15, 5.00),
(2, 7, 16, 3.00),
(2, 7, 17, 4.00),
(2, 7, 18, 3.00),
(3, 8, 5, 0.00),
(3, 8, 6, 12.00),
(3, 8, 7, 10.00),
(3, 8, 8, 18.00),
(3, 8, 9, 15.00),
(3, 8, 10, 12.00),
(3, 9, 14, 0.00),
(3, 9, 16, 3.00),
(3, 9, 17, 4.00),
(3, 9, 18, 3.00);

-- --------------------------------------------------------

--
-- 資料表結構 `package_type`
--

CREATE TABLE `package_type` (
  `type_id` int(11) NOT NULL,
  `package_id` int(11) NOT NULL,
  `optional_quantity` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `package_type`
--

INSERT INTO `package_type` (`type_id`, `package_id`, `optional_quantity`) VALUES
(1, 1, 1),
(2, 1, 2),
(3, 1, 2),
(4, 2, 2),
(5, 2, 1),
(6, 2, 3),
(7, 2, 4),
(8, 3, 1),
(9, 3, 1);

-- --------------------------------------------------------

--
-- 資料表結構 `package_type_translation`
--

CREATE TABLE `package_type_translation` (
  `type_translation_id` int(11) NOT NULL,
  `type_id` int(11) NOT NULL,
  `type_language_code` varchar(10) NOT NULL,
  `type_name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `package_type_translation`
--

INSERT INTO `package_type_translation` (`type_translation_id`, `type_id`, `type_language_code`, `type_name`) VALUES
(1, 1, 'en', 'Appetizer'),
(2, 2, 'en', 'Main Course'),
(3, 3, 'en', 'Drink'),
(4, 4, 'en', 'Appetizer'),
(5, 5, 'en', 'Soup'),
(6, 6, 'en', 'Main Course'),
(7, 7, 'en', 'Drink'),
(8, 8, 'en', 'Main Course'),
(9, 9, 'en', 'Drink'),
(10, 1, 'zh-CN', '前菜'),
(11, 2, 'zh-CN', '主菜'),
(12, 3, 'zh-CN', '饮料'),
(13, 4, 'zh-CN', '前菜'),
(14, 5, 'zh-CN', '汤品'),
(15, 6, 'zh-CN', '主菜'),
(16, 7, 'zh-CN', '饮料'),
(17, 8, 'zh-CN', '主菜'),
(18, 9, 'zh-CN', '饮料'),
(19, 1, 'zh-TW', '前菜'),
(20, 2, 'zh-TW', '主菜'),
(21, 3, 'zh-TW', '飲料'),
(22, 4, 'zh-TW', '前菜'),
(23, 5, 'zh-TW', '湯品'),
(24, 6, 'zh-TW', '主菜'),
(25, 7, 'zh-TW', '飲料'),
(26, 8, 'zh-TW', '主菜'),
(27, 9, 'zh-TW', '飲料');

-- --------------------------------------------------------

--
-- 資料表結構 `recipes`
--

CREATE TABLE `recipes` (
  `recipe_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `material_id` int(11) NOT NULL,
  `quantity_used` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `recipes`
--

INSERT INTO `recipes` (`recipe_id`, `item_id`, `material_id`, `quantity_used`) VALUES
(1, 6, 2, 1.00),
(2, 6, 1, 0.15),
(3, 6, 3, 0.02),
(4, 3, 5, 0.25),
(5, 3, 4, 0.01),
(6, 1, 6, 0.20);

-- --------------------------------------------------------

--
-- 資料表結構 `seatingchart`
--

CREATE TABLE `seatingchart` (
  `tid` int(11) NOT NULL,
  `capacity` int(11) NOT NULL COMMENT 'Table capacity',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'state'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `seatingchart`
--

INSERT INTO `seatingchart` (`tid`, `capacity`, `status`) VALUES
(1, 2, 0),
(2, 2, 0),
(3, 2, 0),
(4, 2, 0),
(5, 2, 0),
(6, 2, 0),
(7, 2, 0),
(8, 2, 0),
(9, 2, 0),
(10, 2, 0),
(11, 2, 0),
(12, 2, 0),
(13, 2, 0),
(14, 2, 0),
(15, 2, 0),
(16, 2, 0),
(17, 2, 0),
(18, 2, 0),
(19, 2, 0),
(20, 2, 0),
(21, 4, 0),
(22, 4, 0),
(23, 4, 0),
(24, 4, 0),
(25, 4, 0),
(26, 4, 0),
(27, 4, 0),
(28, 4, 0),
(29, 4, 0),
(30, 4, 0),
(31, 4, 0),
(32, 4, 0),
(33, 4, 0),
(34, 4, 0),
(35, 4, 0),
(36, 4, 0),
(37, 4, 0),
(38, 4, 0),
(39, 4, 0),
(40, 4, 0),
(41, 4, 0),
(42, 4, 0),
(43, 4, 0),
(44, 4, 0),
(45, 4, 0),
(46, 8, 0),
(47, 8, 0),
(48, 8, 0),
(49, 8, 0),
(50, 8, 0);

-- --------------------------------------------------------

--
-- 資料表結構 `staff`
--

CREATE TABLE `staff` (
  `sid` int(11) NOT NULL,
  `semail` varchar(191) NOT NULL,
  `spassword` varchar(255) NOT NULL,
  `sname` varchar(255) NOT NULL,
  `srole` varchar(45) DEFAULT NULL,
  `stel` int(11) DEFAULT NULL,
  `simageurl` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `staff`
--

INSERT INTO `staff` (`sid`, `semail`, `spassword`, `sname`, `srole`, `stel`, `simageurl`) VALUES
(1, 'peter.wong@example.com', 'password123', 'Peter Wong', 'staff', 25669197, NULL),
(2, 'tina.chan@example.com', 'letmein456', 'Tina Chan', 'Production Supervisor', 31233123, NULL),
(3, 'alex.lam@example.com', 'qwerty789', 'Alex Lam', 'Warehouse Clerk', 29881234, NULL),
(4, 'susan.leung@example.com', 'helloWorld1', 'Susan Leung', 'HR Officer', 28889999, NULL),
(5, 'john.ho@example.com', 'changeme2023', 'John Ho', 'Engineer', 29998888, NULL),
(6, 'maggie.tse@example.com', 'maggiePass!', 'Maggie Tse', 'Accountant', 23881211, NULL),
(7, 'kevin.ng@example.com', 'ngfamily', 'Kevin Ng', 'IT Support', 27889977, NULL),
(8, 'emily.tsui@example.com', 'emily2024', 'Emily Tsui', 'Marketing Lead', 26543210, NULL);

-- --------------------------------------------------------

--
-- 資料表結構 `table_orders`
--

CREATE TABLE `table_orders` (
  `toid` int(11) NOT NULL,
  `table_number` int(11) NOT NULL,
  `oid` int(11) DEFAULT NULL,
  `staff_id` int(11) DEFAULT NULL,
  `status` enum('available','reserved','seated','ordering','ready_to_pay','paid') NOT NULL DEFAULT 'available',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `table_orders`
--

INSERT INTO `table_orders` (`toid`, `table_number`, `oid`, `staff_id`, `status`, `created_at`, `updated_at`) VALUES
(1, 1, NULL, NULL, 'available', '2025-11-18 01:47:01', '2025-11-18 01:47:01'),
(2, 2, NULL, 1, 'reserved', '2025-11-18 01:47:01', '2025-11-18 01:47:01'),
(3, 3, NULL, 2, 'seated', '2025-11-18 01:47:01', '2025-11-18 01:47:01'),
(4, 4, 1, 3, 'ordering', '2025-11-18 01:47:01', '2025-11-18 01:47:01'),
(5, 5, 2, 4, 'ready_to_pay', '2025-11-18 01:47:01', '2025-11-18 01:47:01'),
(6, 6, 2, 5, 'paid', '2025-11-18 01:47:01', '2025-11-18 01:47:01');

-- --------------------------------------------------------

--
-- 資料表結構 `tag`
--

CREATE TABLE `tag` (
  `tag_id` int(11) NOT NULL,
  `tag_name` varchar(255) NOT NULL,
  `tag_category` varchar(255) NOT NULL,
  `tag_bg_color` varchar(7) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `tag`
--

INSERT INTO `tag` (`tag_id`, `tag_name`, `tag_category`, `tag_bg_color`) VALUES
(1, 'vegetarian', 'Dietary', '#4CAF50'),
(2, 'refreshing', 'Characteristic', '#2196F3'),
(3, 'chicken', 'Protein', '#FFC107'),
(4, 'cold', 'Temperature', '#03A9F4'),
(5, 'spicy', 'Flavor', '#F44336'),
(6, 'fish', 'Protein', '#3F51B5'),
(7, 'sour', 'Flavor', '#FF9800'),
(8, 'beef', 'Protein', '#E91E63'),
(9, 'numbing', 'Flavor', '#9C27B0'),
(10, 'tofu', 'Protein', '#009688'),
(11, 'noodles', 'Type', '#673AB7'),
(12, 'pork', 'Protein', '#FF5722'),
(13, 'streetfood', 'Type', '#795548'),
(14, 'stirfry', 'Cooking Method', '#8BC34A'),
(15, 'classic', 'Characteristic', '#00BCD4'),
(16, 'sweet', 'Flavor', '#FFEB3B'),
(17, 'glutinous', 'Type', '#607D8B'),
(18, 'lemon', 'Flavor', '#FFEB3B'),
(19, 'grape', 'Flavor', '#9C27B0'),
(20, 'milk', 'Ingredient', '#795548'),
(21, 'soda', 'Type', '#03A9F4'),
(22, 'traditional', 'Characteristic', '#607D8B');

--
-- 已傾印資料表的索引
--

--
-- 資料表索引 `booking`
--
ALTER TABLE `booking`
  ADD PRIMARY KEY (`bid`),
  ADD KEY `bkcname` (`bkcname`),
  ADD KEY `tid` (`tid`),
  ADD KEY `booking_ibfk_1` (`cid`);

--
-- 資料表索引 `consumption_history`
--
ALTER TABLE `consumption_history`
  ADD PRIMARY KEY (`log_id`);

--
-- 資料表索引 `coupons`
--
ALTER TABLE `coupons`
  ADD PRIMARY KEY (`coupon_id`);

--
-- 資料表索引 `coupon_applicable_categories`
--
ALTER TABLE `coupon_applicable_categories`
  ADD PRIMARY KEY (`id`),
  ADD KEY `coupon_id` (`coupon_id`),
  ADD KEY `category_id` (`category_id`);

--
-- 資料表索引 `coupon_applicable_items`
--
ALTER TABLE `coupon_applicable_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `coupon_id` (`coupon_id`),
  ADD KEY `item_id` (`item_id`);

--
-- 資料表索引 `coupon_applicable_package`
--
ALTER TABLE `coupon_applicable_package`
  ADD PRIMARY KEY (`id`),
  ADD KEY `coupon_id` (`coupon_id`),
  ADD KEY `package_id` (`package_id`);

--
-- 資料表索引 `coupon_point`
--
ALTER TABLE `coupon_point`
  ADD PRIMARY KEY (`cp_id`),
  ADD UNIQUE KEY `uq_coupon_point_cid` (`cid`);

--
-- 資料表索引 `coupon_point_history`
--
ALTER TABLE `coupon_point_history`
  ADD PRIMARY KEY (`cph_id`),
  ADD KEY `idx_cph_cp_id` (`cp_id`),
  ADD KEY `idx_cph_cid` (`cid`),
  ADD KEY `idx_cph_coupon_id` (`coupon_id`);

--
-- 資料表索引 `coupon_redemptions`
--
ALTER TABLE `coupon_redemptions`
  ADD PRIMARY KEY (`redemption_id`),
  ADD KEY `fk_redemption_coupon` (`coupon_id`),
  ADD KEY `fk_redemption_customer` (`cid`);

--
-- 資料表索引 `coupon_rules`
--
ALTER TABLE `coupon_rules`
  ADD PRIMARY KEY (`rule_id`),
  ADD KEY `coupon_id` (`coupon_id`);

--
-- 資料表索引 `coupon_terms`
--
ALTER TABLE `coupon_terms`
  ADD PRIMARY KEY (`term_id`),
  ADD KEY `coupon_id` (`coupon_id`);

--
-- 資料表索引 `coupon_translation`
--
ALTER TABLE `coupon_translation`
  ADD PRIMARY KEY (`translation_id`),
  ADD KEY `coupon_id` (`coupon_id`);

--
-- 資料表索引 `customer`
--
ALTER TABLE `customer`
  ADD PRIMARY KEY (`cid`),
  ADD UNIQUE KEY `cemail` (`cemail`);

--
-- 資料表索引 `materials`
--
ALTER TABLE `materials`
  ADD PRIMARY KEY (`mid`),
  ADD UNIQUE KEY `mname` (`mname`);

--
-- 資料表索引 `menu_category`
--
ALTER TABLE `menu_category`
  ADD PRIMARY KEY (`category_id`);

--
-- 資料表索引 `menu_item`
--
ALTER TABLE `menu_item`
  ADD PRIMARY KEY (`item_id`),
  ADD KEY `category_id` (`category_id`);

--
-- 資料表索引 `menu_item_translation`
--
ALTER TABLE `menu_item_translation`
  ADD PRIMARY KEY (`translation_id`),
  ADD KEY `item_id` (`item_id`);

--
-- 資料表索引 `menu_package`
--
ALTER TABLE `menu_package`
  ADD PRIMARY KEY (`package_id`);

--
-- 資料表索引 `menu_tag`
--
ALTER TABLE `menu_tag`
  ADD PRIMARY KEY (`item_id`,`tag_id`),
  ADD KEY `fk_menu_tag_tag_id` (`tag_id`);

--
-- 資料表索引 `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`oid`),
  ADD UNIQUE KEY `orderRef` (`orderRef`),
  ADD KEY `fk_orders_cid` (`cid`),
  ADD KEY `fk_orders_coupon` (`coupon_id`);

--
-- 資料表索引 `order_coupons`
--
ALTER TABLE `order_coupons`
  ADD PRIMARY KEY (`id`),
  ADD KEY `oid` (`oid`),
  ADD KEY `coupon_id` (`coupon_id`),
  ADD KEY `redemption_id` (`redemption_id`);

--
-- 資料表索引 `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`oid`,`item_id`),
  ADD KEY `item_id` (`item_id`);

--
-- 資料表索引 `package_dish`
--
ALTER TABLE `package_dish`
  ADD PRIMARY KEY (`package_id`,`type_id`,`item_id`),
  ADD KEY `fk_package_dish_type_id` (`type_id`),
  ADD KEY `fk_package_dish_item_id` (`item_id`);

--
-- 資料表索引 `package_type`
--
ALTER TABLE `package_type`
  ADD PRIMARY KEY (`type_id`),
  ADD KEY `fk_package_type_package_id` (`package_id`);

--
-- 資料表索引 `package_type_translation`
--
ALTER TABLE `package_type_translation`
  ADD PRIMARY KEY (`type_translation_id`),
  ADD KEY `fk_package_type_translation_type_id` (`type_id`);

--
-- 資料表索引 `recipes`
--
ALTER TABLE `recipes`
  ADD PRIMARY KEY (`recipe_id`),
  ADD KEY `item_id` (`item_id`),
  ADD KEY `material_id` (`material_id`);

--
-- 資料表索引 `seatingchart`
--
ALTER TABLE `seatingchart`
  ADD PRIMARY KEY (`tid`);

--
-- 資料表索引 `staff`
--
ALTER TABLE `staff`
  ADD PRIMARY KEY (`sid`),
  ADD UNIQUE KEY `semail_UNIQUE` (`semail`);

--
-- 資料表索引 `table_orders`
--
ALTER TABLE `table_orders`
  ADD PRIMARY KEY (`toid`),
  ADD KEY `fk_table_orders_oid` (`oid`),
  ADD KEY `fk_table_orders_staff` (`staff_id`);

--
-- 資料表索引 `tag`
--
ALTER TABLE `tag`
  ADD PRIMARY KEY (`tag_id`),
  ADD UNIQUE KEY `tag_name` (`tag_name`);

--
-- 在傾印的資料表使用自動遞增(AUTO_INCREMENT)
--

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `booking`
--
ALTER TABLE `booking`
  MODIFY `bid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `consumption_history`
--
ALTER TABLE `consumption_history`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `coupons`
--
ALTER TABLE `coupons`
  MODIFY `coupon_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `coupon_applicable_categories`
--
ALTER TABLE `coupon_applicable_categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `coupon_applicable_items`
--
ALTER TABLE `coupon_applicable_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `coupon_applicable_package`
--
ALTER TABLE `coupon_applicable_package`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `coupon_point`
--
ALTER TABLE `coupon_point`
  MODIFY `cp_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `coupon_point_history`
--
ALTER TABLE `coupon_point_history`
  MODIFY `cph_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `coupon_redemptions`
--
ALTER TABLE `coupon_redemptions`
  MODIFY `redemption_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `coupon_rules`
--
ALTER TABLE `coupon_rules`
  MODIFY `rule_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `coupon_terms`
--
ALTER TABLE `coupon_terms`
  MODIFY `term_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=121;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `coupon_translation`
--
ALTER TABLE `coupon_translation`
  MODIFY `translation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `customer`
--
ALTER TABLE `customer`
  MODIFY `cid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `materials`
--
ALTER TABLE `materials`
  MODIFY `mid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `menu_category`
--
ALTER TABLE `menu_category`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `menu_item`
--
ALTER TABLE `menu_item`
  MODIFY `item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `menu_item_translation`
--
ALTER TABLE `menu_item_translation`
  MODIFY `translation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=55;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `menu_package`
--
ALTER TABLE `menu_package`
  MODIFY `package_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `orders`
--
ALTER TABLE `orders`
  MODIFY `oid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `order_coupons`
--
ALTER TABLE `order_coupons`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `package_type`
--
ALTER TABLE `package_type`
  MODIFY `type_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `package_type_translation`
--
ALTER TABLE `package_type_translation`
  MODIFY `type_translation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `recipes`
--
ALTER TABLE `recipes`
  MODIFY `recipe_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `seatingchart`
--
ALTER TABLE `seatingchart`
  MODIFY `tid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=51;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `staff`
--
ALTER TABLE `staff`
  MODIFY `sid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `table_orders`
--
ALTER TABLE `table_orders`
  MODIFY `toid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `tag`
--
ALTER TABLE `tag`
  MODIFY `tag_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- 已傾印資料表的限制式
--

--
-- 資料表的限制式 `booking`
--
ALTER TABLE `booking`
  ADD CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`cid`) REFERENCES `customer` (`cid`),
  ADD CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`tid`) REFERENCES `seatingchart` (`tid`);

--
-- 資料表的限制式 `coupon_applicable_categories`
--
ALTER TABLE `coupon_applicable_categories`
  ADD CONSTRAINT `coupon_applicable_categories_ibfk_1` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `coupon_applicable_categories_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `menu_category` (`category_id`) ON DELETE CASCADE;

--
-- 資料表的限制式 `coupon_applicable_items`
--
ALTER TABLE `coupon_applicable_items`
  ADD CONSTRAINT `coupon_applicable_items_ibfk_1` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `coupon_applicable_items_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `menu_item` (`item_id`) ON DELETE CASCADE;

--
-- 資料表的限制式 `coupon_applicable_package`
--
ALTER TABLE `coupon_applicable_package`
  ADD CONSTRAINT `coupon_applicable_package_ibfk_1` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `coupon_applicable_package_ibfk_2` FOREIGN KEY (`package_id`) REFERENCES `menu_package` (`package_id`) ON DELETE CASCADE;

--
-- 資料表的限制式 `coupon_point`
--
ALTER TABLE `coupon_point`
  ADD CONSTRAINT `fk_coupon_point_cid` FOREIGN KEY (`cid`) REFERENCES `customer` (`cid`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- 資料表的限制式 `coupon_point_history`
--
ALTER TABLE `coupon_point_history`
  ADD CONSTRAINT `fk_cph_cid` FOREIGN KEY (`cid`) REFERENCES `customer` (`cid`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_cph_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`),
  ADD CONSTRAINT `fk_cph_cp_id` FOREIGN KEY (`cp_id`) REFERENCES `coupon_point` (`cp_id`) ON DELETE CASCADE;

--
-- 資料表的限制式 `coupon_redemptions`
--
ALTER TABLE `coupon_redemptions`
  ADD CONSTRAINT `fk_redemption_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`),
  ADD CONSTRAINT `fk_redemption_customer` FOREIGN KEY (`cid`) REFERENCES `customer` (`cid`);

--
-- 資料表的限制式 `coupon_rules`
--
ALTER TABLE `coupon_rules`
  ADD CONSTRAINT `coupon_rules_ibfk_1` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`) ON DELETE CASCADE;

--
-- 資料表的限制式 `coupon_terms`
--
ALTER TABLE `coupon_terms`
  ADD CONSTRAINT `coupon_terms_ibfk_1` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`) ON DELETE CASCADE;

--
-- 資料表的限制式 `coupon_translation`
--
ALTER TABLE `coupon_translation`
  ADD CONSTRAINT `coupon_translation_ibfk_1` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`) ON DELETE CASCADE;

--
-- 資料表的限制式 `menu_item`
--
ALTER TABLE `menu_item`
  ADD CONSTRAINT `menu_item_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `menu_category` (`category_id`);

--
-- 資料表的限制式 `menu_item_translation`
--
ALTER TABLE `menu_item_translation`
  ADD CONSTRAINT `menu_item_translation_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `menu_item` (`item_id`);

--
-- 資料表的限制式 `menu_tag`
--
ALTER TABLE `menu_tag`
  ADD CONSTRAINT `fk_menu_tag_item_id` FOREIGN KEY (`item_id`) REFERENCES `menu_item` (`item_id`),
  ADD CONSTRAINT `fk_menu_tag_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`);

--
-- 資料表的限制式 `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `fk_orders_cid` FOREIGN KEY (`cid`) REFERENCES `customer` (`cid`),
  ADD CONSTRAINT `fk_orders_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`);

--
-- 資料表的限制式 `order_coupons`
--
ALTER TABLE `order_coupons`
  ADD CONSTRAINT `order_coupons_ibfk_1` FOREIGN KEY (`oid`) REFERENCES `orders` (`oid`) ON DELETE CASCADE,
  ADD CONSTRAINT `order_coupons_ibfk_2` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `order_coupons_ibfk_3` FOREIGN KEY (`redemption_id`) REFERENCES `coupon_redemptions` (`redemption_id`) ON DELETE SET NULL;

--
-- 資料表的限制式 `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`oid`) REFERENCES `orders` (`oid`),
  ADD CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `menu_item` (`item_id`);

--
-- 資料表的限制式 `package_dish`
--
ALTER TABLE `package_dish`
  ADD CONSTRAINT `fk_package_dish_item_id` FOREIGN KEY (`item_id`) REFERENCES `menu_item` (`item_id`),
  ADD CONSTRAINT `fk_package_dish_package_id` FOREIGN KEY (`package_id`) REFERENCES `menu_package` (`package_id`),
  ADD CONSTRAINT `fk_package_dish_type_id` FOREIGN KEY (`type_id`) REFERENCES `package_type` (`type_id`);

--
-- 資料表的限制式 `package_type`
--
ALTER TABLE `package_type`
  ADD CONSTRAINT `fk_package_type_package_id` FOREIGN KEY (`package_id`) REFERENCES `menu_package` (`package_id`);

--
-- 資料表的限制式 `package_type_translation`
--
ALTER TABLE `package_type_translation`
  ADD CONSTRAINT `fk_package_type_translation_type_id` FOREIGN KEY (`type_id`) REFERENCES `package_type` (`type_id`);

--
-- 資料表的限制式 `recipes`
--
ALTER TABLE `recipes`
  ADD CONSTRAINT `recipes_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `menu_item` (`item_id`),
  ADD CONSTRAINT `recipes_ibfk_2` FOREIGN KEY (`material_id`) REFERENCES `materials` (`mid`);

--
-- 資料表的限制式 `table_orders`
--
ALTER TABLE `table_orders`
  ADD CONSTRAINT `fk_table_orders_oid` FOREIGN KEY (`oid`) REFERENCES `orders` (`oid`),
  ADD CONSTRAINT `fk_table_orders_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`sid`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
