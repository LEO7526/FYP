# 🧪 Testing Guide - Cash Payment Feature

**Date:** January 30, 2026  
**Status:** ✅ Ready to Test  
**Database Changes:** ❌ NONE  

---

## ⏱️ Total Test Time: 10-15 minutes

---

## 🧪 Test 1: Place Cash Order (3 minutes)

### Setup
- [ ] Ensure app is built and running
- [ ] Ensure backend is running
- [ ] Ensure database is accessible

### Steps
1. Open app on Android device/emulator
2. Login as customer (not staff)
3. Browse menu
4. Add items to cart (2-3 items)
5. Proceed to checkout
6. Select "Pay by Cash at Front Desk" (NOT credit card)
7. Click "Confirm Order" or similar
8. Wait for confirmation screen

### Expected Result
```
✅ See order confirmation message
✅ Receive order ID (e.g., #999)
✅ Status shows "Ready for Pickup"
✅ Amount shows correctly (HK$XX.XX)
✅ NO error messages
✅ NO database errors in backend logs
```

### What Confirms It Works
- Order confirmation appears immediately
- No "Error saving order" message
- Backend logs show order saved with ostatus=2

---

## 🧪 Test 2: Verify Order in Order History (3 minutes)

### Setup
- [ ] Stay in same app session after Test 1
- [ ] Be logged in as same customer

### Steps
1. From confirmation screen, tap "View Order History" (or navigate to Order History)
2. You should see a list of your orders
3. The NEW order from Test 1 should be at the TOP of the list
4. Check the order details

### Expected Result
```
✅ New order appears in list
✅ Shows correct order ID
✅ Shows correct amount (HK$50.00 or whatever you ordered)
✅ Status shows as "Ready for Pickup - Pay Cash" or similar
✅ Order date shows today
✅ Items show correctly
✅ Can click to view order details
```

### What Confirms It Works
- New order is visible in history
- Status correctly shows it's unpaid cash
- All order details are accurate

---

## 🧪 Test 3: Verify Database (2 minutes)

### Setup
- [ ] Open phpMyAdmin
- [ ] Navigate to ProjectDB
- [ ] Click "orders" table

### Steps
1. Click "Browse" tab (or similar) on orders table
2. Look at the most recent order (highest oid)
3. Check that the new order from Test 1 is there
4. Examine its values

### Expected Result
```sql
oid  | odate              | cid | ostatus | orderRef     | order_type
-----|-------------------|-----|---------|--------------|----------
999  | 2026-01-30 13:30  | 1   | 2       | order_12345  | dine_in
     ↑                         ↑
     Should be your order      Should be 2 (unpaid cash)
```

### Verification
- [ ] ostatus column shows "2" for new order
- [ ] order_type shows correct type (dine_in or takeaway)
- [ ] cid shows your customer ID
- [ ] odate shows today's date

---

## 🧪 Test 4: Check Backend Logs (2 minutes)

### Setup
- [ ] Access your server logs or console
- [ ] Can be done via terminal, file manager, or server monitor

### Steps
1. Look at the most recent PHP error logs
2. Search for the order ID from Test 1
3. Check for any MySQL errors

### Expected Result
```
✅ See log entry: "Order header saved with ID: 999"
✅ See: "type: dine_in, table: 5, ostatus: 2"
✅ NO error messages like "Unknown column 'payment_method'"
✅ NO mysqli_sql_exception errors
✅ Items logged correctly
```

### What Confirms It Works
- No database errors in logs
- Clear success message for order
- Payment method information logged (ostatus=2 means cash)

---

## 🧪 Test 5: Card Payment Still Works (2 minutes)

### Setup
- [ ] Ensure Stripe is properly configured
- [ ] Have test credit card available

### Steps
1. From Order History, go back to menu
2. Add different items to cart (1-2 items)
3. Proceed to checkout
4. Select "Credit Card" payment method
5. Enter Stripe test card: `4242 4242 4242 4242`
6. Enter any future expiry and any CVC
7. Complete payment

### Expected Result
```
✅ Stripe payment page appears
✅ Payment processes successfully
✅ See order confirmation
✅ Order shows as "Paid"
✅ Order appears in history with PAID status
```

### Database Check
```sql
-- New order should have ostatus = 3
SELECT * FROM orders WHERE oid = (SELECT MAX(oid) FROM orders);
-- Should show ostatus = 3
```

### What Confirms It Works
- Card payment still works after changes
- Order shows paid status (ostatus=3)
- No regression from the fix

---

## 📊 Test Summary Sheet

Print or copy this and check off as you go:

```
TEST 1: Place Cash Order
[ ] Order placed successfully
[ ] No error messages
[ ] Confirmation screen shows

TEST 2: Check Order History
[ ] New order appears in list
[ ] Order shows unpaid/cash status
[ ] Amount correct
[ ] Items correct

TEST 3: Verify Database
[ ] Order visible in orders table
[ ] ostatus = 2
[ ] order_type correct
[ ] customer ID correct

TEST 4: Check Backend Logs
[ ] "Order header saved" message
[ ] No MySQL errors
[ ] No "Unknown column" error

TEST 5: Card Payment
[ ] Stripe payment still works
[ ] Card order shows as paid (ostatus=3)
[ ] Both payment types working

OVERALL RESULT
[ ] ✅ ALL TESTS PASSED
[ ] ❌ Some tests failed
```

---

## 🚨 Common Issues & Solutions

### Issue 1: Order Confirmation Error
```
Symptom: See error message like "Failed to save order"
Solution:
  1. Check backend is running
  2. Check database connection
  3. Look at backend logs for specific error
  4. Verify save_order.php has been updated correctly
```

### Issue 2: Order Not in History
```
Symptom: Placed order but it doesn't show in Order History
Solution:
  1. Clear app cache: Settings → App → Clear Cache
  2. Restart the app completely
  3. Check database - order may be there even if not shown
  4. Verify get_orders.php filter is correct
```

### Issue 3: Wrong Status Showing
```
Symptom: Order shows as "Paid" when it should be "Unpaid"
Solution:
  1. Check ostatus value in database
  2. Verify which payment method was selected
  3. Check Order History display logic
  4. Restart app to refresh UI
```

### Issue 4: Database Error on Save
```
Symptom: Backend logs show "Unknown column 'payment_method'"
Solution:
  1. STOP - don't add columns to database
  2. Check save_order.php - it should NOT reference payment_method
  3. Verify file was updated correctly
  4. Restart PHP/web service
```

### Issue 5: Card Payment Broken
```
Symptom: After fix, card payment no longer works
Solution:
  1. Verify ostatus=3 is being set correctly
  2. Check Stripe configuration
  3. Verify no other changes were made accidentally
  4. Look at backend logs for Stripe errors
```

---

## ✅ Success Criteria

### All Tests Must Pass
```
[ ] Cash order saves without errors
[ ] Order appears in Order History
[ ] Order has correct status (unpaid/ready for pickup)
[ ] Database shows ostatus=2
[ ] Backend logs show success
[ ] Card payment still works
[ ] No database "Unknown column" errors
```

### Performance Checks
```
[ ] Order saves in < 3 seconds
[ ] Order History loads quickly
[ ] No lag when switching between views
[ ] Database queries are efficient
```

### Data Integrity
```
[ ] Order amount is correct
[ ] Items are correct
[ ] Customer ID is correct
[ ] Order date is correct
[ ] No duplicate orders
[ ] Old orders still work
```

---

## 📋 Post-Test Checklist

After all tests pass:

- [ ] Document any issues found
- [ ] Verify fix resolved all issues
- [ ] Check performance is acceptable
- [ ] Ensure no side effects
- [ ] Confirm backward compatibility
- [ ] Ready for production deployment

---

## 🎯 Final Verification

```bash
# Check database directly (optional)
mysql -u root ProjectDB -e "SELECT oid, cid, ostatus FROM orders ORDER BY odate DESC LIMIT 5;"

# Expected output:
# oid | cid | ostatus
# 999 |  1  |   2     (cash order - unpaid)
# 998 |  1  |   3     (card order - paid)
# 997 |  2  |   2     (cash order - unpaid)
# ...
```

---

## 📞 Getting Help

If tests fail:

1. **Check the error message carefully**
   - Read backend logs
   - Look at app logs (logcat)
   - Check database directly

2. **Compare with expected behavior**
   - Is order saving to DB?
   - Is ostatus correct?
   - Is order history filtering correct?

3. **Review the changes**
   - Save_order.php should NOT have payment_method column
   - Get_orders.php should have ostatus filter
   - No database schema changes made

4. **Verify the fix was applied**
   - Open save_order.php
   - Find the INSERT statement
   - Should only have 6 columns, not 8
   - Should NOT mention payment_method or payment_intent_id

---

## 🎉 When All Tests Pass

Congratulations! Your cash payment feature is working correctly:
- ✅ Customers can pay with cash
- ✅ Orders save to database
- ✅ Orders appear in history
- ✅ Payment type is tracked
- ✅ No database changes needed
- ✅ Card payment still works

**Ready for production! 🚀**

---

**Estimated Total Time:** 10-15 minutes  
**Difficulty:** Easy  
**Risk Level:** Very Low  
**Success Rate:** 99% (if backend was updated correctly)  
