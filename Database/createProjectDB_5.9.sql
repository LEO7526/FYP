-- Fix Free Drink coupon applicability for all drink items.
-- This keeps coupon_id = 2 aligned with current drink menu data.

INSERT IGNORE INTO coupon_applicable_items (coupon_id, item_id)
VALUES
    (2, 39),
    (2, 40),
    (2, 41);

INSERT IGNORE INTO coupon_applicable_categories (coupon_id, category_id)
VALUES
    (2, 5);

UPDATE coupon_rules
SET applies_to = 'category'
WHERE coupon_id = 2;