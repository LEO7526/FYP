package com.example.yummyrestaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yummyrestaurant.LoginBottomSheetFragment;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCustomerActivity extends AppCompatActivity {

    protected List<ImageView> functionIcons = new ArrayList<>();
    protected Map<ImageView, String> iconBaseNames = new HashMap<>();

    private boolean login;

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh login state whenever the user returns
        login = BrowseMenuActivity.getLogin();

        // Highlight whichever icon was passed in the Intent
        int selectedIconId = getIntent().getIntExtra("selectedIcon", 0);
        if (selectedIconId != 0) {
            ImageView selectedIcon = findViewById(selectedIconId);
            if (selectedIcon != null) {
                highlightIcon(selectedIcon);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected void setupBottomFunctionBar() {
        ImageView orderBellIcon     = findViewById(R.id.orderBellIcon);
        ImageView couponIcon        = findViewById(R.id.couponIcon);
        ImageView membershipIcon    = findViewById(R.id.membershipIcon);
        ImageView orderRecordIcon   = findViewById(R.id.orderRecordIcon);
        ImageView profileIcon       = findViewById(R.id.profileIcon);

        functionIcons.clear();
        functionIcons.add(orderBellIcon);
        functionIcons.add(couponIcon);
        functionIcons.add(membershipIcon);
        functionIcons.add(orderRecordIcon);
        functionIcons.add(profileIcon);

        iconBaseNames.put(orderBellIcon,   "customer_main_page_function_item_background");
        iconBaseNames.put(couponIcon,      "customer_main_page_function_item_background");
        iconBaseNames.put(membershipIcon,  "customer_main_page_function_item_background_unique");
        iconBaseNames.put(orderRecordIcon, "customer_main_page_function_item_background");
        iconBaseNames.put(profileIcon,     "customer_main_page_function_item_background");

        orderBellIcon.setOnClickListener(v ->
                navigateProtected(R.id.orderBellIcon, BrowseMenuActivity.class, null, 0, null, null)
        );
        couponIcon.setOnClickListener(v ->
                navigateProtected(R.id.couponIcon, CouponActivity.class, null, 0, null, null)
        );
        membershipIcon.setOnClickListener(v ->
                navigateProtected(R.id.membershipIcon, MembershipActivity.class, null, 0, null, null)
        );
        orderRecordIcon.setOnClickListener(v ->
                navigateProtected(R.id.orderRecordIcon, OrderHistoryActivity.class, null, 0, null, null)
        );
        profileIcon.setOnClickListener(v ->
                navigateProtected(R.id.profileIcon, ProfileActivity.class, null, 0, null, null)
        );
    }

    /**
     * Checks login state; BrowseMenuActivity and CouponActivity are always allowed.
     * Other activities require login. Optionally passes pending cart extras.
     */
    protected void navigateProtected(int iconId,
                                     Class<? extends AppCompatActivity> target,
                                     MenuItem pendingItem,
                                     int pendingQuantity,
                                     String pendingSpice,
                                     String pendingNotes) {
        // Always allow BrowseMenuActivity and CouponActivity
        if (target == BrowseMenuActivity.class || target == CouponActivity.class) {
            launchScreen(iconId, target, null, 0, null, null);
            return;
        }

        // For other activities, check login
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
                              Class<? extends AppCompatActivity> cls,
                              MenuItem pendingItem,
                              int pendingQuantity,
                              String pendingSpice,
                              String pendingNotes) {
        Intent intent = new Intent(this, cls);
        intent.putExtra("selectedIcon", iconId);

        // Forward dish context if customizing
        if (cls == CustomizeDishActivity.class) {
            // Always forward the dish context if DishDetailActivity set it
            MenuItem dish = (MenuItem) getIntent().getSerializableExtra("menuItem");
            int qty = getIntent().getIntExtra("quantity", 1);
            if (dish != null) {
                intent.putExtra(CustomizeDishActivity.EXTRA_MENU_ITEM, dish);
                intent.putExtra(CustomizeDishActivity.EXTRA_QUANTITY, qty);
            }
        }

        // Forward pending cart extras for cart-related actions
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

    /**
     * Highlights the tapped icon by switching its background drawable
     * to the "_current" variant, and resets all others.
     */
    protected void highlightIcon(ImageView selectedIcon) {
        for (ImageView icon : functionIcons) {
            String baseName = iconBaseNames.get(icon);
            if (baseName == null) continue;

            String drawableName = icon == selectedIcon
                    ? baseName + "_current"
                    : baseName;

            int drawableId = getResources()
                    .getIdentifier(drawableName, "drawable", getPackageName());

            if (drawableId != 0) {
                icon.setBackgroundResource(drawableId);
            }
        }
    }
}