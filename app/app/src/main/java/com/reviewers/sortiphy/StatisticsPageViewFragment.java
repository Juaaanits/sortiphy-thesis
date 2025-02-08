package com.reviewers.sortiphy;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatisticsPageViewFragment extends Fragment {

    private PieChart pieChart;
    private LineChart lineChart;
    private List<String> xValues;
    private FirebaseFirestore db;
    String userId = "dailyTrashStatistics"; //replace this when authentication system is complete!

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        View rootView = (ViewGroup) inflater.inflate(R.layout.viewpager_statistics_fragment, container, false);
        pieChart = rootView.findViewById(R.id.pie_chart);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#FF6347"));  // Tomato Red
        colors.add(Color.parseColor("#4682B4"));  // Steel Blue
        colors.add(Color.parseColor("#32CD32"));  // Lime Green
        colors.add(Color.parseColor("#FFD700"));  // Gold
        colors.add(Color.parseColor("#8A2BE2"));  // Blue Violet

        ArrayList<PieEntry> entries = new ArrayList<>();

        db.collection("statistics").document(userId)
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

                        entries.clear();
                        entries.add(new PieEntry(categoryOne, ""));
                        entries.add(new PieEntry(categoryTwo, ""));
                        entries.add(new PieEntry(categoryThree, ""));
                        entries.add(new PieEntry(categoryFour, ""));
                        entries.add(new PieEntry(categoryFive, ""));

                        PieDataSet pieDataSet = new PieDataSet(entries, "Garbage Classes");
                        pieDataSet.setColors(colors);

                        pieDataSet.setDrawValues(false);

                        PieData pieData = new PieData(pieDataSet);
                        pieChart.setData(pieData);

                        pieChart.getDescription().setEnabled(false);
                        pieChart.setUsePercentValues(true);
                        pieChart.setDrawHoleEnabled(false);
                        pieChart.animateY(1000);
                        pieChart.invalidate();
                        pieChart.getLegend().setEnabled(false);

                        long totalValue = categoryOne + categoryTwo + categoryThree + categoryFour + categoryFive;

                        setupGarbageInfo(rootView, R.id.type_one, "Paper", categoryOne, totalValue, 0, colors);
                        setupGarbageInfo(rootView, R.id.type_two, "Plastic", categoryTwo, totalValue, 1, colors);
                        setupGarbageInfo(rootView, R.id.type_three, "Glass", categoryThree, totalValue, 2, colors);
                        setupGarbageInfo(rootView, R.id.type_four, "Metal", categoryFour, totalValue, 3, colors);
                        setupGarbageInfo(rootView, R.id.type_five, "Organic", categoryFive, totalValue, 4, colors);
                    }
        });

        lineChart = rootView.findViewById(R.id.line_chart);

        Description description = new Description();
        description.setText("Per Hour");
        description.setPosition(150f,15f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);

        xValues = Arrays.asList("6AM", "8AM", "10AM", "12AM",
                "2PM", "4PM", "6PM" );

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(7);
        xAxis.setGranularity(1f);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);


        List<Entry> entries1 = new ArrayList<>(); // MAKE A HELPER FUNCTION FOR THIS PLEASE
        entries1.add(new Entry(0, 60f));
        entries1.add(new Entry(1, 70f));
        entries1.add(new Entry(2, 85f));
        entries1.add(new Entry(3, 95f));
        entries1.add(new Entry(4, 60f));
        entries1.add(new Entry(5, 70f));
        entries1.add(new Entry(6, 78f));

        List<Entry> entries2 = new ArrayList<>();
        entries2.add(new Entry(0, 20f));
        entries2.add(new Entry(1, 34f));
        entries2.add(new Entry(2, 26f));
        entries2.add(new Entry(3, 74f));
        entries2.add(new Entry(4, 23f));
        entries2.add(new Entry(5, 70f));
        entries2.add(new Entry(6, 65f));


        LineDataSet dataSet1 = new LineDataSet(entries1, "Paper");
        dataSet1.setColor(Color.BLUE);

        LineDataSet dataSet2 = new LineDataSet(entries2, "Plastic");
        dataSet2.setColor(Color.RED);

        LineData lineData = new LineData(dataSet1, dataSet2);

        lineChart.setData(lineData);

        lineChart.invalidate();

        return rootView;
    }

    private void setupGarbageInfo(View rootView, int viewId, String typeName, double categoryValue, double totalValue, int colorIndex, ArrayList<Integer> colors) {
        View garbageInformation = rootView.findViewById(viewId);
        TextView name = garbageInformation.findViewById(R.id.garbage_type);
        TextView textView = garbageInformation.findViewById(R.id.garbage_percent);
        ImageView color = garbageInformation.findViewById(R.id.marker_color);

        color.setColorFilter(colors.get(colorIndex), PorterDuff.Mode.SRC_IN);
        name.setText(typeName);

        if (totalValue > 0) {
            textView.setText(String.format("%.2f %%", (categoryValue * 100.0) / totalValue));
        } else {
            textView.setText("0.00 %");
        }
    }
}
