package com.reviewers.sortiphy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import android.graphics.Color;

public class DashboardPageViewFragment extends Fragment {

    private FirebaseFirestore db;
    private String classOne;
    private String classTwo;
    private int inputOne;
    private int inputTwo;

    public static DashboardPageViewFragment newInstance(int inputOne, int inputTwo) {
        DashboardPageViewFragment fragment = new DashboardPageViewFragment();
        Bundle args = new Bundle();
        args.putInt("INPUT_ONE", inputOne);
        args.putInt("INPUT_TWO", inputTwo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            inputOne = getArguments().getInt("INPUT_ONE", 0);
            inputTwo = getArguments().getInt("INPUT_TWO", 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        switch (inputOne) {
            case 0:
                classOne = "trashClassificationOne";
                break;
            case 1:
                classOne = "trashClassificationTwo";
                break;
            case 2:
                classOne = "trashClassificationThree";
                break;
            case 3:
                classOne = "trashClassificationFour";
                break;
            default:
                classOne = "trashClassificationOne";
        }

        switch (inputTwo) {
            case 1:
                classTwo = "trashClassificationTwo";
                break;
            case 2:
                classTwo = "trashClassificationThree";
                break;
            case 3:
                classTwo = "trashClassificationFour";
                break;
            case 4:
                classTwo = "trashClassificationFive";
                break;
            default:
                classTwo = "trashClassificationOne";
        }
        View rootView = inflater.inflate(R.layout.viewpager_item, container, false);
        TextView fillLevelOne = rootView.findViewById(R.id.text_num_percent_one);
        TextView fillLevelTwo = rootView.findViewById(R.id.text_num_percent_two);
        TextView binLevelOne = rootView.findViewById(R.id.bin_desc_one);
        TextView binLevelTwo = rootView.findViewById(R.id.bin_desc_two);
        TextView bottomText = rootView.findViewById(R.id.text_percent_one);
        TextView bottomTextTwo = rootView.findViewById(R.id.text_percent_two);

        db.collection("binData").document(classOne)
            .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        long percentageValue = documentSnapshot.getLong("fillLevel");
                        String binDesc = documentSnapshot.getString("className");
                        if (percentageValue > 90) {
                            fillLevelOne.setText("Replace");
                            fillLevelOne.setTextColor(Color.RED);
                            bottomText.setText("Bag Now");
                            bottomText.setTextColor(Color.RED);
                        } else {
                            fillLevelOne.setText(String.valueOf(percentageValue));
                            bottomText.setText("Percent");
                            fillLevelOne.setTextColor(Color.parseColor("#011638"));
                            bottomText.setTextColor(Color.parseColor("#011638"));
                        }
                        binLevelOne.setText(binDesc);
                    }
                 });

        db.collection("binData").document(classTwo)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        long percentageValue = documentSnapshot.getLong("fillLevel");
                        String binDesc = documentSnapshot.getString("className");
                        if (percentageValue > 90) {
                            fillLevelTwo.setText("Replace");
                            fillLevelTwo.setTextColor(Color.RED);
                            bottomTextTwo.setText("Bag Now");
                            bottomTextTwo.setTextColor(Color.RED);
                        } else {
                            fillLevelTwo.setText(String.valueOf(percentageValue));
                            bottomTextTwo.setText("Percent");
                            fillLevelTwo.setTextColor(Color.parseColor("#011638"));
                            bottomTextTwo.setTextColor(Color.parseColor("#011638"));
                        }
                        binLevelTwo.setText(binDesc);
                    }
                });
        return rootView;
    }
}
