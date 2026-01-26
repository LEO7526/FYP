# Alipay Payment Setup Guide

## Issue Fixed
The application was experiencing fallback from Alipay to Card payment due to incorrect payment method configuration.

### Root Cause
1. **Wrong Payment Method Type**: The code was using `alipay_hk` as the payment method type, but Stripe's correct payment method type for Alipay (including Hong Kong) is simply `alipay`.
2. **Missing Configuration**: The payment method type needs to match Stripe's API specification.

### Solution Applied
Updated `create_payment_intent.php` to:
1. Map `alipay_hk` requests to Stripe's `alipay` payment method type
2. Removed unnecessary `billing_details` parameter from PaymentIntent creation
3. Let Stripe's Payment Sheet SDK handle return URLs automatically for mobile apps

## Stripe Dashboard Configuration

To enable Alipay in your Stripe account:

1. **Enable Alipay Payment Method**:
   - Go to https://dashboard.stripe.com/settings/payment_methods
   - Enable "Alipay" under payment methods
   - Make sure it's enabled for both Test and Live modes

2. **Test Mode Setup**:
   - Ensure you're in Test mode (toggle in top-left of dashboard)
   - Test mode is required during development
   - Use test API keys (starting with `pk_test_` and `sk_test_`)

3. **Currency Support**:
   - Alipay supports HKD (Hong Kong Dollar)
   - The minimum charge amount for HKD is HK$5.00 (500 cents)

## Testing Alipay

### Test Card Numbers (for fallback to Card):
- Success: `4242 4242 4242 4242`
- Declined: `4000 0000 0000 0002`

### Alipay Test Flow:
1. Select AlipayHK payment method in the app
2. Click Pay
3. Stripe will present the Alipay payment flow
4. In test mode, you'll see a test authentication page
5. Click "Authorize Test Payment" to complete

## Code Changes Summary

### Backend (create_payment_intent.php)
```php
// Before:
if ($paymentMethod === 'alipay_hk') {
    $paymentMethodTypes = ['alipay_hk'];  // ❌ Incorrect
}

// After:
if ($paymentMethod === 'alipay_hk' || $paymentMethod === 'alipay') {
    $paymentMethodTypes = ['alipay'];  // ✅ Correct Stripe payment method type
}
```

### Why This Works
- Stripe's Payment Sheet SDK for mobile automatically handles:
  - Return URLs for redirect-based payment methods
  - Authentication flows
  - Payment confirmation
- By using the correct payment method type (`alipay`), Stripe can properly create the PaymentIntent
- The SDK manages the entire user flow without requiring manual return URL configuration

## Troubleshooting

If Alipay still doesn't work:

1. **Check Stripe Dashboard**:
   - Verify Alipay is enabled
   - Check you're using test mode
   - Confirm the currency (HKD) is supported

2. **Verify API Keys**:
   - Ensure you're using the correct test API keys
   - Keys should match the mode (test/live) in the dashboard

3. **Check Logs**:
   - Backend logs: `create_payment_intent: Using Alipay payment method`
   - Stripe Dashboard: Check payment intents created

4. **Common Issues**:
   - "Payment method not available": Alipay not enabled in dashboard
   - "Invalid currency": Using unsupported currency for Alipay
   - "Amount too low": Minimum is HK$5.00 (500 cents) for HKD

## Additional Resources
- [Stripe Alipay Documentation](https://stripe.com/docs/payments/alipay)
- [Stripe Payment Sheet (Android)](https://stripe.com/docs/payments/accept-a-payment?platform=android&ui=payment-sheet)
- [Stripe Test Mode](https://stripe.com/docs/testing)
