# Payment Flow Update - Cash Payment & Status Fixes

## Summary of Changes

### 1. **Added "Pay by Cash at Front Desk" Payment Method** ✅

**Frontend (Android):**
- Added new `RadioButton` (rbCash) in `activity_payment.xml`
- Updated UI to show three payment options:
  - 💳 Credit/Debit Card
  - 🔐 Alipay HK (hidden)
  - 💰 Pay by Cash at Front Desk (NEW)

**Logic:**
- When "Cash at Front Desk" is selected:
  - Skips Stripe payment intent creation
  - Directly saves order to backend
  - Sets `ostatus = 2` (Done/Unpaid)
  - Sets `payment_method = "cash"`
  - Generates pseudo payment_intent_id: `cash_<timestamp>`

---

### 2. **Fixed Order Status (ostatus) Logic** ✅

**Status Mapping:**
```
1 = Pending       (Initial order creation)
2 = Done/Unpaid   (Order complete, waiting for cash payment at desk)
3 = Paid          (Card payment successful via Stripe)
4 = Cancelled     (Order cancelled)
```

**Auto-determination of ostatus:**
- **Card Payment:** `ostatus = 3` (Paid immediately after Stripe confirmation)
- **Cash Payment:** `ostatus = 2` (Done, but payment pending at front desk)

---

### 3. **Database Schema Updates** ✅

**Added to `orders` table:**
```sql
payment_method VARCHAR(50) DEFAULT 'card' COMMENT 'card, cash'
payment_intent_id VARCHAR(255) DEFAULT NULL COMMENT 'Stripe payment intent ID or pseudo ID for cash'
```

**Updated ostatus comment:**
```sql
ostatus INT NOT NULL DEFAULT 1 COMMENT '1=Pending, 2=Done/Unpaid, 3=Paid, 4=Cancelled'
```

---

### 4. **Backend Changes** ✅

**save_order.php:**
- Extracts `payment_method` from request
- Extracts `ostatus` from request
- Auto-determines `ostatus` based on `payment_method` if not provided
- Validates ostatus is within range (1-4)
- Saves `payment_method` and `payment_intent_id` to database
- Enhanced logging with payment method information

---

## Payment Flow Diagrams

### **Card Payment Flow:**
```
PaymentActivity
    ↓
Pay Button (Card selected)
    ↓
createPaymentIntent() → Backend API
    ↓
Stripe Payment Intent Created
    ↓
PaymentSheet UI (Stripe)
    ↓
Payment Successful
    ↓
onPaymentSuccess()
    ↓
saveOrderToBackend(ostatus=3, payment_method="card")
    ↓
Order Saved (PAID)
    ↓
OrderConfirmationActivity
```

### **Cash Payment Flow:**
```
PaymentActivity
    ↓
Pay Button (Cash selected)
    ↓
onCashPaymentSelected()
    ↓
saveOrderToBackend(ostatus=2, payment_method="cash")
    ↓
Order Saved (UNPAID, PENDING CASH PAYMENT)
    ↓
OrderConfirmationActivity
    ↓
Show message: "Please pay at front desk"
```

---

## Issues Fixed

### **Issue 1: hardcoded ostatus ❌ → Fixed ✅**
- **Before:** `ostatus` always set to `1` (Pending) regardless of payment method
- **After:** Dynamic status:
  - Card → `3` (Paid)
  - Cash → `2` (Done/Unpaid)

### **Issue 2: No cash payment option ❌ → Fixed ✅**
- **Before:** Only card payment via Stripe
- **After:** Two payment methods available:
  - Card (Stripe)
  - Cash at Front Desk (direct order creation)

### **Issue 3: Payment method not saved ❌ → Fixed ✅**
- **Before:** `payment_method` hardcoded to "stripe"
- **After:** Actually saved to database:
  - `payment_method` column stores "card" or "cash"
  - `payment_intent_id` column stores Stripe ID or pseudo ID

### **Issue 4: No validation of ostatus ❌ → Fixed ✅**
- **Before:** Frontend sent arbitrary status values
- **After:** Backend validates and auto-determines if invalid

---

## Files Modified

### Android
1. **`activity_payment.xml`** - Added Cash payment RadioButton
2. **`PaymentActivity.java`** - Complete payment logic restructure:
   - Added `rbCash` RadioButton reference
   - Added `onCashPaymentSelected()` method
   - Updated `onCreate()` to handle cash selection
   - Updated `saveOrderToBackend()` to set correct `ostatus` and `payment_method`
   - Separated card vs. cash payment flows

### Backend
1. **`save_order.php`**
   - Extract `payment_method`, `payment_intent_id`, `ostatus`
   - Auto-determine `ostatus` based on payment method
   - Insert payment information into database
   - Enhanced logging

### Database
1. **`createProjectDB_5.7.sql`**
   - Added `payment_method` column to `orders` table
   - Added `payment_intent_id` column to `orders` table
   - Added comments explaining ostatus values

---

## Testing Checklist

- [ ] **Card Payment:**
  - [ ] Select "Credit/Debit Card"
  - [ ] Complete Stripe payment
  - [ ] Verify order saved with `ostatus = 3` (Paid)
  - [ ] Verify `payment_method = "card"`
  - [ ] Verify `payment_intent_id` contains Stripe ID

- [ ] **Cash Payment:**
  - [ ] Select "Pay by Cash at Front Desk"
  - [ ] Skip Stripe (no payment sheet)
  - [ ] Verify order saved with `ostatus = 2` (Done/Unpaid)
  - [ ] Verify `payment_method = "cash"`
  - [ ] Verify `payment_intent_id` contains `cash_<timestamp>`

- [ ] **Edge Cases:**
  - [ ] Verify ostatus=1 used only for initial/pending orders
  - [ ] Verify ostatus=4 used only for cancelled orders
  - [ ] Test with different order types (dine_in, takeaway)
  - [ ] Verify table_number only saved for dine_in orders

---

## Future Enhancements

1. **UI Enhancement:**
   - Display "Payment pending at front desk" message for cash orders
   - Show estimated payment time

2. **Kitchen Display:**
   - Show different order status colors based on ostatus
   - Priority different handling for paid (3) vs. unpaid (2) orders

3. **Payment Tracking:**
   - Add button for staff to mark cash payments as received (ostatus 3)
   - Generate receipt with payment status

4. **Additional Payment Methods:**
   - Apple Pay
   - Google Pay
   - Bank Transfer
   - QR Code payment (WeChat Pay, etc.)

---

## Status Code Reference

| Code | Status | Meaning | Payment Status |
|------|--------|---------|-----------------|
| 1 | Pending | Order created but not finalized | Unknown |
| 2 | Done | Order complete, awaiting payment | Unpaid (Cash) |
| 3 | Paid | Order complete and paid | Paid (Card) |
| 4 | Cancelled | Order cancelled | N/A |

---

## Logging Examples

### **Card Payment:**
```
saveOrderToBackend: ostatus=3 (payment_method=card)
saveOrderToBackend: card payment, payment_intent_id=pi_1234567890
✅ Order header saved with ID: 25 (type: dine_in, table: 5, ostatus: 3, payment_method: card)
```

### **Cash Payment:**
```
saveOrderToBackend: ostatus=2 (payment_method=cash)
saveOrderToBackend: cash payment, generated pseudo payment_intent_id
✅ Order header saved with ID: 26 (type: takeaway, table: null, ostatus: 2, payment_method: cash)
```

---

## Migration Notes

If upgrading existing database, add columns:
```sql
ALTER TABLE orders ADD COLUMN payment_method VARCHAR(50) DEFAULT 'card';
ALTER TABLE orders ADD COLUMN payment_intent_id VARCHAR(255) DEFAULT NULL;

-- Update existing records
UPDATE orders SET ostatus = 3 WHERE ostatus = 1 OR ostatus = 0; -- Assume old orders were paid
```

---

## Conclusion

✅ **Payment flow is now complete with:**
- Multiple payment methods (Card, Cash)
- Proper order status tracking
- Payment information persisted to database
- Clear separation of payment logic
- Better logging for debugging
