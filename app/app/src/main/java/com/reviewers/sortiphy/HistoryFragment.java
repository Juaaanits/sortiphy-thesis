package com.reviewers.sortiphy;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class HistoryFragment extends Fragment {

    private LinearLayout linearLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history,container,false);
        linearLayout = rootView.findViewById(R.id.linear_layout_history);
        for (int i = 0; i < 17; i++) {
            View historyItem = getLayoutInflater().inflate(R.layout.history_item, null);
            TextView dateAndTime = historyItem.findViewById(R.id.date_and_time);
            TextView content = historyItem.findViewById(R.id.message);

            String dateTime = "1-20-25 | 4:00PM";
            String message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";

            dateAndTime.setText(dateTime);
            content.setText(message);

            if ((i % 2) == 1) {
                historyItem.findViewById(R.id.item_layout).setBackgroundColor(Color.parseColor("#C6C6C6"));
            }

            linearLayout.addView(historyItem);
        }
        return rootView;
    }
}
