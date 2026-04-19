package com.example.yummyrestaurant.inventory;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final MaterialsFragment materialsFragment = new MaterialsFragment();
    private final RecipesFragment recipesFragment = new RecipesFragment();
    private final MaterialAnalysisFragment materialAnalysisFragment = new MaterialAnalysisFragment();
    private final ShortageImpactFragment shortageImpactFragment = new ShortageImpactFragment();
    private final RestockFragment restockFragment = new RestockFragment();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return materialsFragment;
            case 1:
                return recipesFragment;
            case 2:
                return materialAnalysisFragment;
            case 3:
                return shortageImpactFragment;
            case 4:
                return restockFragment;
            default:
                return materialsFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public Fragment getFragmentAt(int position) {
        switch (position) {
            case 0:
                return materialsFragment;
            case 1:
                return recipesFragment;
            case 2:
                return materialAnalysisFragment;
            case 3:
                return shortageImpactFragment;
            case 4:
                return restockFragment;
            default:
                return materialsFragment;
        }
    }
}