package com.reviewers.sortiphy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HistoryFragment extends Fragment {

    private LinearLayout linearLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history,container,false);
        linearLayout = rootView.findViewById(R.id.linear_layout_history);

        loadNotificationHistory();

        return rootView;
    }

    private void loadNotificationHistory() {
        SharedPreferences prefs = requireContext().getSharedPreferences("NotificationHistory", Context.MODE_PRIVATE);
        Set<String> historySet = prefs.getStringSet("history", new LinkedHashSet<>());

        List<String> historyList = new ArrayList<>(historySet);
        Collections.reverse(historyList);

        for (int i = 0; i < historyList.size(); i++) {
            String[] parts = historyList.get(i).split("###");
            if (parts.length < 3) continue;

            View historyItem = getLayoutInflater().inflate(R.layout.history_item, null);
            TextView dateAndTime = historyItem.findViewById(R.id.date_and_time);
            TextView content = historyItem.findViewById(R.id.message);

            dateAndTime.setText(parts[0]);
            content.setText(parts[2]);

            if ((i % 2) == 1) {
                historyItem.findViewById(R.id.item_layout).setBackgroundColor(Color.parseColor("#C6C6C6"));
            }

            linearLayout.addView(historyItem);
        }
    }
}
