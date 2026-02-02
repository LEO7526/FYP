# Quick Reference: Payment Method Implementation

## Payment Methods Available

### 1. Card Payment (Stripe)
```
Selection: RadioButton "💳 Credit/Debit Card"
Flow: PaymentActivity → createPaymentIntent() → Stripe PaymentSheet
Result: ostatus = 3 (PAID), payment_method = "card"
Database: Stores actual Stripe payment_intent_id
```

### 2. Cash Payment (Front Desk)
```
Selection: RadioButton "💰 Pay by Cash at Front Desk"
Flow: PaymentActivity → onCashPaymentSelected() → Direct save
Result: ostatus = 2 (DONE/UNPAID), payment_method = "cash"
Database: Stores pseudo payment_intent_id (cash_<timestamp>)
```

---

## ostatus (Order Status) Values

| Value | Status | Payment | When Set | Kitchen Action |
|-------|--------|---------|----------|-----------------|
| **1** | Pending | Unknown | Initial order creation | Prep & queue |
| **2** | Done | Unpaid | Cash payment selected | Prep & queue, wait for payment |
| **3** | Paid | Verified | Card payment successful | Prep & queue |
| **4** | Cancelled | N/A | Order cancelled | Discard |

---

## Android Implementation Details

### File: `PaymentActivity.java`

**Key Methods:**
```java
onCreate()                    // Initialize UI, setup payment method listener
onCashPaymentSelected()       // Handle cash payment flow
createPaymentIntent()         // Handle card payment (Stripe API)
saveOrderToBackend()          // Save order with correct ostatus & payment_method
```

**Key Logic:**
```java
// Determine which payment flow
if ("cash".equals(selectedPaymentMethod)) {
    onCashPaymentSelected();      // Direct save, ostatus=2
} else {
    createPaymentIntent();         // Stripe flow, ostatus=3
}

// Set correct status based on payment method
int ostatus = ("cash".equals(selectedPaymentMethod)) ? 2 : 3;
orderData.put("payment_method", selectedPaymentMethod);
```

**UI Elements Added:**
```xml
<RadioButton android:id="@+id/rbCash"
    android:text="💰 Pay by Cash at Front Desk"
/>
```

---

## Backend Implementation Details

### File: `save_order.php`

**Payment Information Extraction:**
```php
$payment_method = $input['payment_method'] ?? 'card';
$payment_intent_id = $input['payment_intent_id'] ?? null;
$ostatus = $input['ostatus'] ?? 0;

// Auto-determine ostatus if not provided
if (empty($ostatus) || $ostatus < 1 || $ostatus > 4) {
    $ostatus = ("cash" === $payment_method) ? 2 : 3;
}
```

**Database Insert:**
```php
INSERT INTO orders (
    odate, cid, ostatus, orderRef, order_type, 
    table_number, payment_method, payment_intent_id
) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
```

---

## Database Schema

### Added Columns to `orders` Table

```sql
payment_method VARCHAR(50) DEFAULT 'card' COMMENT 'card, cash'
payment_intent_id VARCHAR(255) DEFAULT NULL COMMENT 'Stripe ID or pseudo ID'
```

### Updated Column Comment

```sql
ostatus INT NOT NULL DEFAULT 1 COMMENT '1=Pending, 2=Done/Unpaid, 3=Paid, 4=Cancelled'
```

---

## Sample Order Data Sent to Backend

### Card Payment:
```json
{
  "cid": 1,
  "ostatus": 3,
  "payment_method": "card",
  "payment_intent_id": "pi_1234567890ABCDEF",
  "order_type": "dine_in",
  "table_number": 5,
  "items": [...]
}
```

### Cash Payment:
```json
{
  "cid": 1,
  "ostatus": 2,
  "payment_method": "cash",
  "payment_intent_id": "cash_1706602800123",
  "order_type": "takeaway",
  "table_number": null,
  "items": [...]
}
```

---

## API Endpoints

### Card Payment Flow
```
1. POST /create_payment_intent
   Request: {amount, cid, currency, paymentMethod}
   Response: {clientSecret, paymentIntentId}

2. POST /save_order
   Request: {cid, items, payment_method, payment_intent_id, ostatus=3}
   Response: {success: true, oid: 123}
```

### Cash Payment Flow
```
1. POST /save_order (direct, no Stripe)
   Request: {cid, items, payment_method, payment_intent_id, ostatus=2}
   Response: {success: true, oid: 124}
```

---

## Testing Quick Commands

### Verify Card Payment Order
```sql
SELECT oid, ostatus, payment_method, payment_intent_id, order_type, table_number
FROM orders
WHERE payment_method = 'card' AND ostatus = 3;
```

### Verify Cash Payment Order
```sql
SELECT oid, ostatus, payment_method, payment_intent_id, order_type
FROM orders
WHERE payment_method = 'cash' AND ostatus = 2;
```

### Find Unpaid Orders (Cash)
```sql
SELECT * FROM orders WHERE payment_method = 'cash' AND ostatus = 2;
```

### Update Cash Order to Paid
```sql
UPDATE orders SET ostatus = 3 WHERE oid = 100 AND payment_method = 'cash';
```

---

## Common Workflows

### Kitchen Staff View
```
Filter orders by ostatus:
- ostatus = 1: Not yet confirmed (rare)
- ostatus = 2: Ready to prep (cash pending payment)
- ostatus = 3: Ready to prep (payment confirmed)
- ostatus = 4: Cancelled (skip)

Priority: Handle ostatus=2 and ostatus=3 equally
Note: When cash payment received, update ostatus to 3
```

### Cashier/Payment Staff View
```
Find pending cash payments:
SELECT * FROM orders WHERE payment_method='cash' AND ostatus=2;

After customer pays cash:
UPDATE orders SET ostatus=3 WHERE oid=<order_id>;

Generate report:
SELECT COUNT(*), SUM(amount) FROM orders 
WHERE DATE(odate) = CURDATE() AND payment_method='cash';
```

### Customer View
```
Card payment: "Your order is confirmed and paid ✓"
Cash payment: "Your order is confirmed. Please pay at the front desk ✓"
```

---

## Migration from Old System

### If Upgrading Existing Database:

```sql
-- Step 1: Add new columns
ALTER TABLE orders 
ADD COLUMN payment_method VARCHAR(50) DEFAULT 'card',
ADD COLUMN payment_intent_id VARCHAR(255) DEFAULT NULL;

-- Step 2: Update existing records (assume all were card paid)
UPDATE orders SET payment_method = 'card' 
WHERE payment_method IS NULL OR payment_method = '';

-- Step 3: Update ostatus for existing records
UPDATE orders SET ostatus = 3 WHERE ostatus = 1 AND odate < DATE_SUB(NOW(), INTERVAL 7 DAY);
-- Keep recent ostatus=1 orders as is (might still be pending)

-- Step 4: Verify
SELECT COUNT(*), payment_method, ostatus FROM orders GROUP BY payment_method, ostatus;
```

---

## Debugging Tips

### Check Logs
```bash
# Android Studio Logcat
PaymentActivity: >>> Payment method: CARD
PaymentActivity: ostatus=3 (payment_method=card)

# PHP Error Log
[save_order.php] Auto-determined ostatus=3 based on payment_method=card
[save_order.php] ✅ Order header saved with ID: 25 (ostatus: 3, payment_method: card)
```

### Common Issues

**Issue:** Order saved with wrong ostatus
```
Solution: Check selectedPaymentMethod in PaymentActivity
Ensure payment_method is sent correctly to backend
```

**Issue:** Cash orders showing ostatus=3
```
Solution: Check if app version is updated
Clear app cache and reinstall
```

**Issue:** payment_intent_id is NULL
```
Solution: Check if payment_intent_id was sent in request
For cash: Should be "cash_<timestamp>"
For card: Should be "pi_<stripe_id>"
```

---

## File Summary

| File | Changes |
|------|---------|
| `activity_payment.xml` | Added RadioButton for cash payment |
| `PaymentActivity.java` | Added rbCash, onCashPaymentSelected(), updated logic |
| `save_order.php` | Extract payment method, validate ostatus, save to DB |
| `createProjectDB_5.7.sql` | Added payment_method, payment_intent_id columns |

---

## Next Steps

1. **Database Migration**
   - Run ALTER TABLE to add new columns
   - Update existing records

2. **Testing**
   - Test card payment flow
   - Test cash payment flow
   - Verify ostatus values in database

3. **Deployment**
   - Update Android app
   - Update backend PHP
   - Update database
   - Train staff

4. **Monitoring**
   - Check logs for payment method tracking
   - Monitor cash vs card payment ratio
   - Ensure ostatus updates when payment received
