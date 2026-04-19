-- Idempotent member customer import (safe to run multiple times)
-- It updates existing customers by unique email instead of throwing #1062.

INSERT INTO customer (cname, cpassword, ctel, caddr, company, cemail, cbirthday, crole, cimageurl, coupon_point) VALUES
('David Chan', 'password123', 91234567, '30 Canton Road, Tsim Sha Tsui, Kowloon, Hong Kong', 'Eastern Trading Company', 'david.chan@example.com', '05-18', 'customer', NULL, 0),
('Mary Li', 'meiling2025', 92345678, '12/F, Tower 1, Times Square, Causeway Bay, Hong Kong', 'MayLin Design Studio', 'mary.li@example.com', '08-22', 'customer', NULL, 0),
('John Zhang', 'zhangwq789', 93456789, '88 Des Voeux Road Central, Central, Hong Kong', 'StrongTech Solutions', 'john.zhang@example.com', '11-05', 'customer', NULL, 0),
('Sarah Wang', 'xiaowen888', 94567890, '200 Hennessy Road, Wan Chai, Hong Kong', 'Creative Culture Media', 'sarah.wang@example.com', '02-14', 'customer', NULL, 0),
('Kevin Liu', 'liujiahui66', 95678901, '55 Hoi Yuen Road, Kwun Tong, Hong Kong', 'Kevin Logistics Ltd.', 'kevin.liu@example.com', '09-09', 'customer', NULL, 0),
('Michael Wong', 'michael2025', 96789012, '9 Queen''s Road East, Admiralty, Hong Kong', 'Harbour Finance Group', 'michael.wong@example.com', '12-03', 'customer', NULL, 0),
('Susan Lam', 'susanlam88', 97890123, '28 Johnston Road, Wan Chai, Hong Kong', 'Blue Peak Consulting', 'susan.lam@example.com', '04-27', 'customer', NULL, 0)
ON DUPLICATE KEY UPDATE
cname = VALUES(cname),
cpassword = VALUES(cpassword),
ctel = VALUES(ctel),
caddr = VALUES(caddr),
company = VALUES(company),
cbirthday = VALUES(cbirthday),
crole = VALUES(crole),
cimageurl = VALUES(cimageurl),
coupon_point = VALUES(coupon_point);
