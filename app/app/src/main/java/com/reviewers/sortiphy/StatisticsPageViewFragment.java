package com.reviewers.sortiphy;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Toast;

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
import com.google.gson.Gson;

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

        Calendar calendar = Calendar.getInstance();
        Calendar calendarOne = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendarOne.setFirstDayOfWeek(Calendar.MONDAY);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

        switch (page) {
            case 0:
                date.setText(dateFormat.format(calendar.getTime()));
                userId = "dailyTrashStatistics";
                break;
            case 1:
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
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
                        long categoryFive = documentSnapshot.getLong("categoryFiveCount");

                        long totalValue = categoryOne + categoryTwo + categoryThree + categoryFour + categoryFive;

                        setupGarbageInfo(rootView, R.id.type_one, "Paper", categoryOne, totalValue, 0, colors);
                        setupGarbageInfo(rootView, R.id.type_two, "Plastic", categoryTwo, totalValue, 1, colors);
                        setupGarbageInfo(rootView, R.id.type_three, "Glass", categoryThree, totalValue, 2, colors);
                        setupGarbageInfo(rootView, R.id.type_four, "Metal", categoryFour, totalValue, 3, colors);
                        setupGarbageInfo(rootView, R.id.type_five, "Organic", categoryFive, totalValue, 4, colors);

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

        db.collection("statistics").document("dailyTrashStatistics")
                .addSnapshotListener(((snapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen Failed.", error);
                    }

                    if (snapshot != null && snapshot.exists()) {
                        SharedPreferences prefs = requireContext().getSharedPreferences("StatsPrefs", Context.MODE_PRIVATE);

                        int adjustedDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1;

                        int[] dailyStats = loadArrayFromPrefs(prefs, "dailyStats", 7);
                        int[][] weeklyStats = load2DArrayFromPrefs(prefs, "weeklyStats", 7, 7);
                        int[][] monthlyStats = load2DArrayFromPrefs(prefs, "monthlyStats", 31, 7);

                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY) - 6;  // 0-23
                        int currentDayOfWeek = adjustedDayOfWeek - 1;  // 0 (Sunday) to 6 (Saturday)
                        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH) - 1;  // 0 to 29 (for 30-day month)

                        if (currentHour >= 0 && currentHour < 13 && currentHour % 2 == 0) {
                            dailyStats[currentHour/2]++;
                        }

                        weeklyStats[currentDayOfWeek] = dailyStats.clone();
                        monthlyStats[currentDayOfMonth] = dailyStats.clone();

                        saveArrayToPrefs(prefs, "dailyStats", dailyStats);
                        save2DArrayToPrefs(prefs, "weeklyStats", weeklyStats);
                        save2DArrayToPrefs(prefs, "monthlyStats", monthlyStats);

                        Log.d("Firestore", "Statistics updated and saved.");

                        if (userId.equals("dailyTrashStatistics")) {
                            for (int i = 0; i < 7; i++) {
                                float value = dailyStats[i];
                                lineCategoryOne.add(new Entry(i, dailyStats[i]));
                            }

                        } else if (userId.equals("weeklyTrashStatistics")) {
                            for (int i = 0; i < 7; i++) {
                                int sum = 0;
                                for (int j = 0; j < adjustedDayOfWeek; j++) {
                                    sum += weeklyStats[j][i];
                                }
                                lineCategoryOne.add(new Entry(i, sum / adjustedDayOfWeek));
                            }
                        }  else if (userId.equals("monthlyTrashStatistics")) {
                            for (int i = 0; i < 7; i++) {
                                int sum = 0;
                                for (int j = 0; j < calendar.get(Calendar.DAY_OF_MONTH); j++) {
                                    sum += monthlyStats[j][i];
                                }
                                lineCategoryOne.add(new Entry(i, sum / calendar.get(Calendar.DAY_OF_MONTH)));
                            }
                        }
                        LineDataSet dataSet1 = new LineDataSet(lineCategoryOne, "Trash");
                        dataSet1.setColor(Color.BLUE);
                        LineData lineData = new LineData(dataSet1);
                        lineChart.setData(lineData);
                        lineChart.invalidate();
                    }
                }));


        LineDataSet dataSet1 = new LineDataSet(lineCategoryOne, "Paper");
        dataSet1.setColor(Color.BLUE);

        LineData lineData = new LineData(dataSet1);

        lineCategoryOne.clear();

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

    private void saveArrayToPrefs(SharedPreferences prefs, String key, int[] array) {
        Gson gson = new Gson();
        String json = gson.toJson(array);
        prefs.edit().putString(key, json).apply();
    }

    private int[] loadArrayFromPrefs(SharedPreferences prefs, String key, int size) {
        Gson gson = new Gson();
        String json = prefs.getString(key, null);

        if (json != null) {
            return gson.fromJson(json, int[].class);
        } else {
            return new int[size];  // Return a new array with default values if not found
        }
    }

    private void save2DArrayToPrefs(SharedPreferences prefs, String key, int[][] array) {
        Gson gson = new Gson();
        String json = gson.toJson(array);
        prefs.edit().putString(key, json).apply();
    }

    private int[][] load2DArrayFromPrefs(SharedPreferences prefs, String key, int rows, int cols) {
        Gson gson = new Gson();
        String json = prefs.getString(key, null);

        if (json != null) {
            return gson.fromJson(json, int[][].class);
        } else {
            return new int[rows][cols];  // Return a new empty 2D array if not found
        }
    }
}
