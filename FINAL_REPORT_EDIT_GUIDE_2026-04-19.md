# Final Report Edit Guide (2026-04-19)

This guide is based on:
- project-wide scan (Android + Website + FYP + FYP CCK + Database/projectapi)
- the current report draft content in `temp.txt`

## 1) High-priority edits (must fix before submission)

1. Replace outdated AI description in Abstract and Architecture section.
- Current draft says chatbot uses Qwen2.5 via Ollama as main system capability.
- Code evidence shows two separate AI tracks:
  - Android demo app `Android/MyRestaurantAI` calls model `qwen2.5:7b`.
  - Main restaurant staff recommendation flow uses Gemini API in `FYP/staff/generatePackageRecommendation.php` with key from `FYP/gemini_config.php`.
- Suggested report wording:
  - "The production restaurant workflow currently uses Gemini API for staff-side package recommendation. A separate Android AI prototype module (`MyRestaurantAI`) demonstrates Qwen2.5 integration for conversational experiments."

2. Remove "microservices architecture" claim unless you can prove real service separation/deployment.
- Evidence indicates mostly monolithic PHP endpoints under `Database/projectapi` + Android clients + website modules.
- Suggested wording:
  - "The system follows a modular API-based architecture with domain-separated PHP endpoints and Android/Web clients."

3. Remove "TensorFlow Lite recommendation" claim for main YummyRestaurant app unless implemented in that app.
- `TensorFlow Lite` dependency is in `Android/MyRestaurantAI/app/build.gradle.kts`, not in core YummyRestaurant production logic.
- Suggested wording:
  - "ML/TFLite is currently explored in prototype modules; production recommendation is API-driven."

4. Correct business-hour statements everywhere.
- Current runtime rule reverted to ordering/payment window `11:00-21:29`.
- Evidence:
  - `Database/projectapi/create_payment_intent.php`
  - `Database/projectapi/save_order.php`
  - `Database/projectapi/place_order.php`
  - `Database/projectapi/payMoneyUrl.php`
  - Android strings in `Android/YummyRestaurant/app/src/main/res/values*/strings.xml`
- Ensure report does not mention 22:59.

5. Correct profile image architecture description.
- Old flow (GitHub token upload) is no longer accurate.
- Current flow stores image to local XAMPP file system and saves URL to DB:
  - `Database/projectapi/save_customerProfileImage.php`
  - `Database/projectapi/save_staffProfileImage.php`
  - Android uses `UploadResponse.imageUrl` in `Android/YummyRestaurant/.../UploadResponse.java` and `EditProfileActivity.java`.
- Suggested wording:
  - "Profile images are uploaded to local server storage (`Image/Profile_image/...`) and persisted as URL in database."

6. Clarify Website module is legacy/mixed-schema in some pages.
- `Website/FYP/order/payment.php` references `orders.total_price` and `coupon_point` table style that may not align with latest schema APIs.
- Production-grade order/payment/coupon flow is centered in `Database/projectapi/*.php` and Android app.
- Suggested wording:
  - "Website ordering modules include legacy pages retained for compatibility/testing, while core production flows use unified project API endpoints."

7. Fix security language in report.
- Draft overstates enterprise security posture while code has plain credentials and no full enterprise auth stack.
- Also remove any exposed secrets from report screenshots/snippets.
- Suggested wording:
  - "Security controls currently include role-based access checks, API input validation, and payment provider SDK integration; enterprise hardening (secret vault, full audit, centralized IAM) remains future work."

## 2) Strongly recommended edits (quality and credibility)

1. Add "What was actually completed" timeline table.
- Include these milestones:
  - 2026-03-10: order save/order history package fixes
  - 2026-03-17: timezone/coupon applicability fixes
  - 2026-03-24: bottom nav + browse UX fixes
  - 2026-03-31: HK locale rollout + label migration
  - 2026-04-13: resource/linking fix batch
  - 2026-04-19: networking regression + profile upload fix

2. Add one architecture diagram caption update.
- Describe current stack as:
  - Android client (`YummyRestaurant`) + Website/FYP/FYP CCK modules
  - PHP API layer (`Database/projectapi`)
  - MySQL schema (`createProjectDB_5.8/5.9`)
  - Stripe integration for card payments
  - Gemini-based staff recommendation endpoint

3. Add "Known limitations" subsection with honest statements.
- Recommended points:
  - Legacy website pages still use older schema assumptions.
  - Duplicate paths (`Website`, `FYP`, `FYP CCK`) increase maintenance overhead.
  - Need further secret-management hardening.

4. Update test/evidence section with runtime proof items.
- Mention:
  - physical-device API base URL auto-resolution (`ApiConfig` + `get_computer_address.php`)
  - coupon validation and deferred coupon usage until payment success
  - zero-amount coupon order path (bypass Stripe)
  - order history total uses backend-provided total fields

## 3) Suggested replacement paragraph (Abstract)

"This project delivers an AI-assisted restaurant management platform for a single Hong Kong restaurant, including an Android customer app, staff-facing web modules, and a PHP/MySQL backend API. Core implemented functions include multilingual menu browsing (EN/zh-TW/zh-CN), booking, cart and coupon validation, Stripe card payment, order tracking, inventory/material checks, and order history reconstruction with customization support. During iterative development, major fixes were completed for device networking, timezone-aware order windows, coupon consistency, order package handling, and local profile-image upload reliability. AI support is currently applied in staff-side recommendation workflows via Gemini API, with separate prototype exploration modules for additional LLM integrations."

## 4) Suggested replacement paragraph (Architecture description)

"The implemented architecture is a modular API-based system. Android and website clients consume domain endpoints under `Database/projectapi` (orders, coupons, booking, payment, profile, materials), backed by MySQL schema v5.8/v5.9. Payment intent creation and order submission enforce business-hour and timezone checks server-side. Image handling uses local server storage with URL persistence in customer/staff profiles. Label/categorization content is increasingly database-driven through translation tables and label APIs."

## 5) Section-by-section edit checklist against current draft

1. Section 1 Introduction
- Keep problem statement, but remove unverified claims: "microservices", "fully deployed AI forecasting".

2. Section 2 Driving Question
- Replace generic methodology statements with concrete implemented items and files.

3. Section 4.3.1 Software
- Reduce unrealistic enterprise procurement list unless required by course template.
- Distinguish "development tools used" vs "hypothetical enterprise procurement".

4. Section 4.8 Constraints and limitations
- Add practical technical debt from codebase (legacy website schema mismatch, secret handling, duplicated modules).

5. Section 7 User Guide / Test Case
- Ensure screenshots and steps match current flows:
  - profile upload via local server path
  - payment window 11:00-21:29
  - coupon used only after successful payment

6. Section 8 Critical Evaluation
- Add candid reflection on regressions fixed and why they happened (e.g., API base URL regression, legacy website drift).

## 6) Evidence file list you can cite in report

- Android core app:
  - `Android/YummyRestaurant/app/src/main/java/com/example/yummyrestaurant/api/ApiConfig.java`
  - `Android/YummyRestaurant/app/src/main/java/com/example/yummyrestaurant/activities/PaymentActivity.java`
  - `Android/YummyRestaurant/app/src/main/java/com/example/yummyrestaurant/utils/CouponValidator.java`
  - `Android/YummyRestaurant/app/src/main/java/com/example/yummyrestaurant/activities/EditProfileActivity.java`
  - `Android/YummyRestaurant/app/src/main/java/com/example/yummyrestaurant/models/UploadResponse.java`
  - `Android/YummyRestaurant/app/src/main/java/com/example/yummyrestaurant/adapters/OrderAdapter.java`

- Backend API:
  - `Database/projectapi/create_payment_intent.php`
  - `Database/projectapi/save_order.php`
  - `Database/projectapi/get_orders.php`
  - `Database/projectapi/getMyCoupons.php`
  - `Database/projectapi/useCoupon.php`
  - `Database/projectapi/save_customerProfileImage.php`
  - `Database/projectapi/save_staffProfileImage.php`
  - `Database/projectapi/get_computer_address.php`

- Website modules (legacy + active):
  - `Website/FYP/order/*.php`
  - `FYP CCK/api/labels.php`
  - `FYP/staff/generatePackageRecommendation.php`

## 7) Final recommendation for submission strategy

- Position your report as "implemented core platform + ongoing consolidation".
- Do not present all old website flows as production-current if they conflict with latest API/schema.
- Prefer evidence-backed statements over aspirational claims.
