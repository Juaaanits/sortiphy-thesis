package com.reviewers.sortiphy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import org.jetbrains.annotations.NotNull;

public class DashboardFragment extends Fragment {
/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard,container,false);
    }
*/

    private static final int NUM_PAGES = 3;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard,container,false);
        viewPager = rootView.findViewById(R.id.bin_dashboard_levels_pager);
        pagerAdapter = new DashboardFragmentPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        return rootView;
    }

    private class DashboardFragmentPagerAdapter extends FragmentStateAdapter {
        public DashboardFragmentPagerAdapter(@NotNull Fragment fragment) {
            super(fragment);
        }
        @NotNull
        @Override
        public Fragment createFragment(int position) {
            return new DashboardPageViewFragment();
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}
