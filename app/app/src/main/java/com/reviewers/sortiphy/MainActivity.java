package com.reviewers.sortiphy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;
import android.Manifest;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private DrawerLayout drawerLayout;
    private FirebaseFirestore db;
    String userId = "SqkFypben8amVK4o4RED"; //replace this when authentication system is complete!

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }


        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean loginCheck = sharedPreferences.getBoolean("isLoggedIn", false);
        userId = sharedPreferences.getString("USER_ID", null);

        if (!loginCheck) {
            startActivity(new Intent(MainActivity.this, LoginScreen.class));
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //scheduleFirestoreWorkerDebug();
        scheduleFirestoreWorker();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        View headerView = navigationView.getHeaderView(0);
        TextView userNameText = headerView.findViewById(R.id.user_username_display);
        TextView userDescriptionDisplay = headerView.findViewById(R.id.user_description_display);
        ImageView userProfileImage = headerView.findViewById(R.id.profile_picture);

        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String position = documentSnapshot.getString("position");
                        String profilePicture = documentSnapshot.getString("displayPhoto");

                        userNameText.setText(username);
                        userDescriptionDisplay.setText(position);

                        // Load image using Glide if the profile picture exists
                        if (profilePicture != null && !profilePicture.isEmpty()) {
                            Glide.with(headerView.getContext())
                                    .load(profilePicture)
                                    //.placeholder(R.drawable.default_profile) // Optional: Placeholder image
                                    //.error(R.drawable.error_profile) // Optional: Error image if URL fails
                                    .into(userProfileImage);
                        } else {
                           // userProfileImage.setImageResource(R.drawable.default_profile);
                        }
                    }
        });

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==  R.id.nav_dashboard) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
        } else if (item.getItemId() ==  R.id.nav_history) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HistoryFragment()).commit();
        } else if (item.getItemId() ==  R.id.nav_statistics) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StatisticsFragment()).commit();
        } else if (item.getItemId() ==  R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit(); // no settings for now
        } else if (item.getItemId() ==  R.id.nav_logout) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("isLoggedIn");
            editor.remove("USER_ID");
            editor.apply();

            startActivity(new Intent(MainActivity.this, LoginScreen.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void scheduleFirestoreWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest firestoreWorkRequest = new PeriodicWorkRequest.Builder(
                FirestoreWorker.class,
                15,
                TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "FirestoreWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                firestoreWorkRequest
        );
    }

    private void scheduleFirestoreWorkerDebug() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(FirestoreWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniqueWork(
                "FirestoreWorkerDebug",
                ExistingWorkPolicy.REPLACE,
                workRequest
        );
        new Handler(Looper.getMainLooper()).postDelayed(this::scheduleFirestoreWorkerDebug, 10000);
    }
}