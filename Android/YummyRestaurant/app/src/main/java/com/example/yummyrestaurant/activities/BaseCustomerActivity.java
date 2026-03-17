package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.yummyrestaurant.LoginBottomSheetFragment;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.MenuItem;

public abstract class BaseCustomerActivity extends ThemeBaseActivity {

    protected BottomNavigationView bottomNavigationView;
    private boolean suppressNavigation = false;
    private boolean login;

    @Override
    protected void onResume() {
        super.onResume();
        login = BrowseMenuActivity.getLogin();

        int selectedIconId = getIntent().getIntExtra("selectedIcon", 0);
        if (selectedIconId != 0 && bottomNavigationView != null) {
            suppressNavigation = true;
            try {
                bottomNavigationView.setSelectedItemId(selectedIconId);
            } catch (Exception ignored) {
                // selectedIconId may not be a nav item (e.g. R.id.addToCartBtn)
            }
            suppressNavigation = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected void setupBottomFunctionBar() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView == null) return;

        // Highlight current tab without triggering navigation
        int selectedIconId = getIntent().getIntExtra("selectedIcon", 0);
        if (selectedIconId != 0) {
            suppressNavigation = true;
            try {
                bottomNavigationView.setSelectedItemId(selectedIconId);
            } catch (Exception ignored) {}
            suppressNavigation = false;
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (suppressNavigation) return true;
            int id = item.getItemId();
            if (id == R.id.orderBellIcon) {
                navigateProtected(R.id.orderBellIcon, BrowseMenuActivity.class, null, 0, null, null);
            } else if (id == R.id.couponIcon) {
                navigateProtected(R.id.couponIcon, CouponActivity.class, null, 0, null, null);
            } else if (id == R.id.membershipIcon) {
                navigateProtected(R.id.membershipIcon, MembershipActivity.class, null, 0, null, null);
            } else if (id == R.id.orderRecordIcon) {
                navigateProtected(R.id.orderRecordIcon, OrderHistoryActivity.class, null, 0, null, null);
            } else if (id == R.id.profileIcon) {
                navigateProtected(R.id.profileIcon, ProfileActivity.class, null, 0, null, null);
            }
            return true;
        });

        // Suppress reselection (no re-launch if already on this screen)
        bottomNavigationView.setOnItemReselectedListener(item -> {});
    }

    /**
     * Checks login state; BrowseMenuActivity and CouponActivity are always allowed.
     * Other activities require login. Optionally passes pending cart extras.
     */
    protected void navigateProtected(int iconId,
                                     Class<? extends ThemeBaseActivity> target,
                                     MenuItem pendingItem,
                                     int pendingQuantity,
                                     String pendingSpice,
                                     String pendingNotes) {
        if (target == BrowseMenuActivity.class || target == CouponActivity.class) {
            launchScreen(iconId, target, null, 0, null, null);
            return;
        }

        if (login) {
            launchScreen(iconId, target, pendingItem, pendingQuantity, pendingSpice, pendingNotes);
        } else {
            showInlineLogin(
                    () -> launchScreen(iconId, target, pendingItem, pendingQuantity, pendingSpice, pendingNotes),
                    pendingItem, pendingQuantity, pendingSpice, pendingNotes
            );
        }
    }

    /**
     * Actually starts the target Activity and passes along the selected icon ID.
     */
    private void launchScreen(int iconId,
                              Class<? extends ThemeBaseActivity> cls,
                              MenuItem pendingItem,
                              int pendingQuantity,
                              String pendingSpice,
                              String pendingNotes) {
        Intent intent = new Intent(this, cls);
        intent.putExtra("selectedIcon", iconId);

        if (cls == CustomizeDishActivity.class) {
            MenuItem dish = (MenuItem) getIntent().getSerializableExtra("menuItem");
            int qty = getIntent().getIntExtra("quantity", 1);
            if (dish != null) {
                intent.putExtra(CustomizeDishActivity.EXTRA_MENU_ITEM, dish);
                intent.putExtra(CustomizeDishActivity.EXTRA_QUANTITY, qty);
            }
        }

        if (pendingItem != null && cls != CustomizeDishActivity.class) {
            intent.putExtra("pendingMenuItem", pendingItem);
            intent.putExtra("pendingQuantity", pendingQuantity);
            intent.putExtra("pendingSpice", pendingSpice);
            intent.putExtra("pendingNotes", pendingNotes);
        }

        startActivity(intent);
    }

    /**
     * Shows the inline login bottom sheet, passing pending cart extras.
     */
    protected void showInlineLogin(Runnable onSuccess,
                                 MenuItem pendingItem,
                                 int pendingQuantity,
                                 String pendingSpice,
                                 String pendingNotes) {

        Bundle args = new Bundle();
        if (pendingItem != null) {
            args.putSerializable("pendingMenuItem", pendingItem);
            args.putInt("pendingQuantity", pendingQuantity);
            args.putString("pendingSpice", pendingSpice);
            args.putString("pendingNotes", pendingNotes);
        }

        LoginBottomSheetFragment sheet = new LoginBottomSheetFragment();
        sheet.setArguments(args);

        sheet.setLoginListener(success -> {
            if (success) {
                login = true;
                if (onSuccess != null) onSuccess.run();
            }
        });

        sheet.show(getSupportFragmentManager(), "login_sheet");
    }
}
