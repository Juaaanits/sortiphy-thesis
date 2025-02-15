package com.reviewers.sortiphy;

import android.content.Context;
import android.content.SharedPreferences;
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
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        View rootView = inflater.inflate(R.layout.fragment_dashboard,container,false);
        TextView usernameDisplay = rootView.findViewById(R.id.greetings_text);

        if (getActivity() != null) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            userId = sharedPreferences.getString("USER_ID", null);
        }


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

        View dailyStats = rootView.findViewById(R.id.daily_card);
        View weeklyStats = rootView.findViewById(R.id.weekly_card);

        setupCards("dailyTrashStatistics", dailyStats);
        setupCards("weeklyTrashStatistics", weeklyStats);

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

    private void setupCards (String statisticsId, View stats) {
        db.collection("statistics").document(statisticsId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        long categoryOne = documentSnapshot.getLong("categoryOneCount");
                        long categoryTwo = documentSnapshot.getLong("categoryTwoCount");
                        long categoryThree = documentSnapshot.getLong("categoryThreeCount");
                        long categoryFour = documentSnapshot.getLong("categoryFourCount");
                        long categoryFive = documentSnapshot.getLong("categoryFiveCount");

                        String categoryOneName = documentSnapshot.getString("categoryNameOne");
                        String categoryTwoName = documentSnapshot.getString("categoryNameTwo");
                        String categoryThreeName = documentSnapshot.getString("categoryNameThree");
                        String categoryFourName = documentSnapshot.getString("categoryNameFour");
                        String categoryFiveName = documentSnapshot.getString("categoryNameFive");

                        String type = documentSnapshot.getString("type");

                        TextView textOne = stats.findViewById(R.id.type_one_button);
                        TextView textTwo = stats.findViewById(R.id.type_two_button);
                        TextView textThree = stats.findViewById(R.id.type_three_button);
                        TextView textFour = stats.findViewById(R.id.type_four_button);
                        TextView textFive = stats.findViewById(R.id.type_five_button);
                        TextView cardType = stats.findViewById(R.id.card_name);

                        long totalValue = categoryOne + categoryTwo + categoryThree + categoryFour + categoryFive;

                        textOne.setText(String.format("%.2f%%", (categoryOne * 100.0) / totalValue) + " " +  categoryOneName);
                        textTwo.setText(String.format("%.2f%%", (categoryTwo * 100.0) / totalValue) + " " +  categoryTwoName);
                        textThree.setText(String.format("%.2f%%", (categoryThree * 100.0) / totalValue) + " " +  categoryThreeName);
                        textFour.setText(String.format("%.2f%%", (categoryFour * 100.0) / totalValue) + " " +  categoryFourName);
                        textFive.setText(String.format("%.2f%%", (categoryFive * 100.0) / totalValue) + " " + categoryFiveName);

                        cardType.setText(type + " Stats");
                    }
                });
    }
}


