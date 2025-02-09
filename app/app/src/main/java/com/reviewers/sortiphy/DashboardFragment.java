package com.reviewers.sortiphy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class DashboardFragment extends Fragment {
/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard,container,false);
    }
*/
    private FirebaseFirestore db;
    private static final int NUM_PAGES = 3;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TabLayout tabLayout;
    String userId = "SqkFypben8amVK4o4RED"; //replace this when authentication system is complete!

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        View rootView = inflater.inflate(R.layout.fragment_dashboard,container,false);
        TextView usernameDisplay = rootView.findViewById(R.id.greetings_text);

        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        usernameDisplay.setText("Welcome, " + username + "!");
                    }
                });


        viewPager = rootView.findViewById(R.id.bin_dashboard_levels_pager);
        pagerAdapter = new DashboardFragmentPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        tabLayout = rootView.findViewById(R.id.bin_page_indicator);

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                // Configure the tab, e.g., set text or icon
                tab.setText("Tab " + (position + 1));
            }
        }).attach();

        return rootView;
    }

    private class DashboardFragmentPagerAdapter extends FragmentStateAdapter {
        public DashboardFragmentPagerAdapter(@NotNull Fragment fragment) {
            super(fragment);
        }
        @NotNull
        @Override
        public Fragment createFragment(int position) {
            return DashboardPageViewFragment.newInstance(position, position + 2);
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}
