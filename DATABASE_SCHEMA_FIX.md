# 🔧 Database Schema Fix - Add Payment Columns

**Issue:** Order History not displaying unpaid orders due to database schema mismatch  
**Root Cause:** The `payment_method` and `payment_intent_id` columns are missing from the `orders` table  
**Status:** 🔄 IN PROGRESS - Schema update needed

---

## 🐛 Problem Explanation

When a new cash order is placed, the app tries to save it with the following error:

```
Fatal error: Uncaught mysqli_sql_exception: Unknown column 'payment_method' in 'field list'
```

This error occurs because:
1. ✅ The `save_order.php` backend code is correct (tries to save payment_method)
2. ✅ The database schema file includes these columns (createProjectDB_5.7.sql)
3. ❌ But the **actual running database** hasn't been updated with these new columns

---

## 📋 What's Missing

The `orders` table needs these two columns:

```sql
payment_method VARCHAR(50) DEFAULT 'card' COMMENT 'card, cash'
payment_intent_id VARCHAR(255) DEFAULT NULL COMMENT 'Stripe payment intent ID or pseudo ID for cash'
```

---

## ✅ Solution - Run Migration

### Option 1: Using phpMyAdmin (GUI - Easiest)

1. Open phpMyAdmin
2. Navigate to `ProjectDB` → `orders` table
3. Click "Structure"
4. Click "Add" (to add new column)
5. Add first column:
   - **Name:** `payment_method`
   - **Type:** `VARCHAR`
   - **Length:** `50`
   - **Default:** `card`
   - **Comment:** `card, cash`
6. Add second column:
   - **Name:** `payment_intent_id`
   - **Type:** `VARCHAR`
   - **Length:** `255`
   - **Default:** NULL
   - **Comment:** `Stripe payment intent ID or pseudo ID for cash`
7. Click "Save"

### Option 2: Using SQL Query (Recommended)

Run this SQL in phpMyAdmin's SQL tab or MySQL console:

```sql
USE ProjectDB;

ALTER TABLE orders 
ADD COLUMN payment_method VARCHAR(50) DEFAULT 'card' COMMENT 'card, cash';

ALTER TABLE orders 
ADD COLUMN payment_intent_id VARCHAR(255) DEFAULT NULL COMMENT 'Stripe payment intent ID or pseudo ID for cash';
```

### Option 3: Using Migration File

Run the migration file created:
```bash
mysql -u root ProjectDB < add_payment_columns.sql
```

---

## 📊 Current Table Structure (Before Fix)

```
oid            INT (PK)
odate          DATETIME
cid            INT (FK to customer)
ostatus        INT (1=Pending, 2=Done/Unpaid, 3=Paid, 4=Cancelled)
note           TEXT
orderRef       VARCHAR(100) UNIQUE
coupon_id      INT (FK to coupons)
order_type     ENUM('dine_in', 'takeaway')
table_number   INT
❌ payment_method       (MISSING)
❌ payment_intent_id    (MISSING)
```

## 📊 Updated Table Structure (After Fix)

```
oid            INT (PK)
odate          DATETIME
cid            INT (FK to customer)
ostatus        INT (1=Pending, 2=Done/Unpaid, 3=Paid, 4=Cancelled)
note           TEXT
orderRef       VARCHAR(100) UNIQUE
coupon_id      INT (FK to coupons)
order_type     ENUM('dine_in', 'takeaway')
table_number   INT
✅ payment_method       VARCHAR(50) DEFAULT 'card'
✅ payment_intent_id    VARCHAR(255) DEFAULT NULL
```

---

## 🔄 Why Orders Are Missing From History

**Current Flow (with missing columns):**

```
1. Customer places cash order
2. App sends order with payment_method='cash'
3. Backend save_order.php tries to INSERT into orders table
4. ❌ Database error: Unknown column 'payment_method'
5. ❌ Order is NOT saved to database
6. ❌ Order History query returns empty result for that order
7. ❌ Customer sees old orders but not the new one
```

**Expected Flow (after fix):**

```
1. Customer places cash order
2. App sends order with payment_method='cash'
3. Backend save_order.php INSERTs into orders table
4. ✅ Order saved successfully with ostatus=2
5. ✅ Order History query retrieves all orders
6. ✅ Customer sees new unpaid order in history
7. ✅ Shows "Ready for Pickup - Pay at Desk"
```

---

## 🧪 Testing After Fix

### Step 1: Verify Columns Added
```sql
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'orders' AND TABLE_SCHEMA = 'ProjectDB'
ORDER BY ORDINAL_POSITION;
```

Expected result: Should show `payment_method` and `payment_intent_id` columns

### Step 2: Place New Cash Order
1. Open app as customer
2. Order items
3. Select "Pay by Cash at Front Desk"
4. Complete order
5. ✅ Verify no error in app logs

### Step 3: Check Order History
1. Navigate to "Order History"
2. ✅ Verify new cash order appears in list
3. ✅ Verify status shows as "Done/Unpaid" or similar
4. ✅ Verify order details are complete

### Step 4: Check Database
```sql
SELECT oid, odate, cid, ostatus, payment_method, payment_intent_id
FROM orders
WHERE cid = 1
ORDER BY odate DESC
LIMIT 5;
```

Expected: Should show the new order with:
- `ostatus = 2` (Done/Unpaid)
- `payment_method = 'cash'`
- `payment_intent_id = 'cash_...'`

---

## 📝 Related Files

### Backend
- `Database/projectapi/save_order.php` - Already correctly uses these columns ✅
- `Database/projectapi/get_orders.php` - Already retrieves data correctly ✅

### Database
- `Database/createProjectDB_5.7.sql` - Schema file has correct structure ✅
- `Database/add_payment_columns.sql` - Migration script (NEW)

### Frontend
- Android app - Already sends payment_method correctly ✅

---

## 🎯 Impact Summary

| Aspect | Before Fix | After Fix |
|--------|-----------|-----------|
| **Save Cash Order** | ❌ Database error | ✅ Success |
| **Order in DB** | ❌ Missing | ✅ Saved with payment_method |
| **Order History Query** | ❌ Doesn't show order | ✅ Shows unpaid order |
| **Customer Sees Order** | ❌ No | ✅ Yes |
| **Payment Tracking** | ❌ Not stored | ✅ Stored in DB |

---

## ⚠️ Important Notes

1. **This is a ONE-TIME database schema update** - Only needs to be done once
2. **No app rebuild required** - Backend code already correct
3. **Existing orders will remain intact** - Only adds new columns with defaults
4. **Backward compatible** - Default values ensure existing queries still work

---

## 🚀 Quick Action Items

- [ ] Step 1: Run SQL migration to add columns
- [ ] Step 2: Verify columns exist in database
- [ ] Step 3: Place test cash order
- [ ] Step 4: Check Order History displays new order
- [ ] Step 5: Verify database record has payment info

---

## 📞 Troubleshooting

**Error: "Unknown column 'payment_method'"**
- Solution: Columns haven't been added yet. Run the migration SQL.

**Error: "Duplicate column name"**
- Solution: Columns already exist. This is good! Proceed to testing.

**New orders still not showing in history:**
- Solution: Clear app cache and refresh Order History
- Or: Restart app to clear any cached API responses

---

**Status:** ⏳ **ACTION REQUIRED - Run Database Migration**  
**Affected:** Orders with payment_method (card/cash)  
**Impact:** New cash orders will be saved correctly  
**Timeline:** 5 minutes to apply fix
