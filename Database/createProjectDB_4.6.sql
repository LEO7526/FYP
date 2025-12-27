-- MySQL dump - Version 4.6
-- Project Database Schema Update for Package Customizations
-- Database: ProjectDB

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+08:00";

USE ProjectDB;

-- =================================================================
-- PACKAGE ITEM CUSTOMIZATIONS (NEW TABLE)
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
  KEY idx_group_option (group_id, option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Create index for efficient querying
CREATE INDEX idx_package_customizations ON order_package_item_customizations(oid, package_id);

COMMIT;
