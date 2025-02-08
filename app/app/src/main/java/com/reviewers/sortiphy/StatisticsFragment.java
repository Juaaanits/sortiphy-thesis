package com.reviewers.sortiphy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class StatisticsFragment extends Fragment {
/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard,container,false);
    }
*/

    private static final int NUM_PAGES = 3;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TabLayout tabLayout;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics,container,false);
        viewPager = rootView.findViewById(R.id.statistics_viewpager);
        pagerAdapter = new FragmentPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        tabLayout = rootView.findViewById(R.id.statistics_tabLayout);

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Today");
                        break;
                    case 1:
                        tab.setText("This Week");
                        break;
                    case 2:
                        tab.setText("All Time");
                        break;
                }
            }
        }).attach();

        return rootView;
    }

    private class FragmentPagerAdapter extends FragmentStateAdapter {
        public FragmentPagerAdapter(@NotNull Fragment fragment) {
            super(fragment);
        }
        @NotNull
        @Override
        public Fragment createFragment(int position) {
            return new StatisticsPageViewFragment();
        } // This part is responsible for deciding which class will show what fragment

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}
