package com.reviewers.sortiphy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsPageViewFragment extends Fragment {

    private PieChart pieChart;
    private String lastKnownValue = null;  // Initial value is null

    private LineChart lineChart;
    private List<String> xValues;
    private FirebaseFirestore db;
    private int page;
    String userId; //replace this when authentication system is complete!

    public static StatisticsPageViewFragment newInstance(int page) {
        StatisticsPageViewFragment fragment = new StatisticsPageViewFragment();
        Bundle args = new Bundle();
        args.putInt("INPUT", page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            page = getArguments().getInt("INPUT", 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();
        View rootView = (ViewGroup) inflater.inflate(R.layout.viewpager_statistics_fragment, container, false);
        pieChart = rootView.findViewById(R.id.pie_chart);
        TextView date = rootView.findViewById(R.id.date);

        Calendar calendarDisplay = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        Calendar calendarOne = Calendar.getInstance();
        calendarDisplay.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendarOne.setFirstDayOfWeek(Calendar.MONDAY);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

        switch (page) {
            case 0:
                date.setText(dateFormat.format(calendar.getTime()));
                userId = "dailyTrashStatistics";
                break;
            case 1:
                calendarDisplay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                calendarOne.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                date.setText(dateFormat.format(calendar.getTime()) + " - " + dateFormat.format(calendarOne.getTime()));
                userId = "weeklyTrashStatistics";
                break;
            case 2:
                dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                date.setText(dateFormat.format(calendar.getTime()));
                userId = "monthlyTrashStatistics";
                break;
            default:
                date.setText(dateFormat.format(calendar.getTime()));
                userId = "dailyTrashStatistics";
                break;
        }

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

                        long totalValue = categoryOne + categoryTwo + categoryThree + categoryFour;

                        setupGarbageInfo(rootView, R.id.type_one, "Glass", categoryOne, totalValue, 0, colors);
                        setupGarbageInfo(rootView, R.id.type_two, "Non-Recyclable", categoryTwo, totalValue, 1, colors);
                        setupGarbageInfo(rootView, R.id.type_three, "Paper", categoryThree, totalValue, 2, colors);
                        setupGarbageInfo(rootView, R.id.type_four, "Recyclable", categoryFour, totalValue, 3, colors);

                        entries.clear();
                        entries.add(new PieEntry(categoryOne, ""));
                        entries.add(new PieEntry(categoryTwo, ""));
                        entries.add(new PieEntry(categoryThree, ""));
                        entries.add(new PieEntry(categoryFour, ""));

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
                    }
        });

        lineChart = rootView.findViewById(R.id.line_chart);

        Description description = new Description();
        description.setText("Per Hour");
        description.setPosition(150f,15f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);

        xValues = Arrays.asList("6AM", "8AM", "10AM", "12PM",
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

        List<Entry> lineCategoryOne = new ArrayList<>();
        String timeStampDocumentPath = "";

        switch (page) {
            case 0:
                timeStampDocumentPath = "daily";
                break;
            case 1:
                timeStampDocumentPath = "weekly";
                break;
            case 2:
                timeStampDocumentPath = "monthly";
                break;
            default:
                timeStampDocumentPath = "daily";
        }

        db.collection("timeStamp").document(timeStampDocumentPath)
                .addSnapshotListener(((snapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen Failed.", error);
                    }

                    if (snapshot != null && snapshot.exists()) {
                        SharedPreferences prefs = requireContext().getSharedPreferences("StatsPrefs", Context.MODE_PRIVATE);

                        long adjustedDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1;
                        long currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);  // 0 to 29 (for 30-day month)

                        long divisor = 1;

                        switch (page) {
                            case 0:
                                divisor = 1;
                                break;
                            case 1:
                                divisor = adjustedDayOfWeek;
                                break;
                            case 2:
                                divisor = currentDayOfMonth;
                                break;
                            default:
                                divisor = adjustedDayOfWeek;
                        }

                        long sixAm = snapshot.getLong("6am");
                        long eightAm = snapshot.getLong("8am");
                        long tenAm = snapshot.getLong("10am");
                        long twelvePm = snapshot.getLong("12pm");
                        long twoPm = snapshot.getLong("2pm");
                        long fourPm = snapshot.getLong("4pm");
                        long sixPm = snapshot.getLong("6pm");

                        setupLineGraph(lineCategoryOne, sixAm, eightAm, tenAm, twelvePm, twoPm, fourPm, sixPm, divisor);

                        LineDataSet dataSet1 = new LineDataSet(lineCategoryOne, "Trash");
                        dataSet1.setColor(Color.BLUE);
                        LineData lineData = new LineData(dataSet1);
                        lineChart.setData(lineData);
                        lineChart.invalidate();
                    }
                }));
        return rootView;
    }

    private void setupLineGraph (List<Entry> lineCategory, long sixAm, long eightAm, long tenAm, long twelvePm, long twoPm, long fourPm, long sixPm, long divisor) {
        lineCategory.clear();
        lineCategory.add(new Entry(0, (sixAm/divisor)));
        lineCategory.add(new Entry(1, (eightAm/divisor)));
        lineCategory.add(new Entry(2, (tenAm/divisor)));
        lineCategory.add(new Entry(3, (twelvePm/divisor)));
        lineCategory.add(new Entry(4, (twoPm/divisor)));
        lineCategory.add(new Entry(5, (fourPm/divisor)));
        lineCategory.add(new Entry(6, (sixPm/divisor)));
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
