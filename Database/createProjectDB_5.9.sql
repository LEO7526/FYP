-- =================================================================
-- VERSION 5.9 – Add translation tables for tags, customization option
--               groups and customization option values.
--               Supports zh-CN (Simplified Chinese) and zh-TW
--               (Traditional Chinese) in addition to the default
--               English values stored in the base tables.
-- =================================================================

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
