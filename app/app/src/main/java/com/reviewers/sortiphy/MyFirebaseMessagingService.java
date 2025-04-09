package com.reviewers.sortiphy;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String PREFS_NAME = "NotificationHistory";
    private static final int MAX_HISTORY = 50;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            sendNotification(title, body);
            saveNotificationHistory(title, body);
        }

        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            if (title != null && body != null) {
                sendNotification(title, body);
                saveNotificationHistory(title, body);
            }
        }
    }

    private void sendNotification(String title, String messageBody) {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "threshold_alerts")
                    .setSmallIcon(R.drawable.logo_sortiphy)
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            if (manager.areNotificationsEnabled()) { // Checks both permission and channel
                manager.notify((int) System.currentTimeMillis(), builder.build());
            } else {
                Log.w("FCM", "Notifications disabled by user");
            }
        } catch (Exception e) {
            Log.e("FCM", "Notification failed", e);
        }
    }

    private void saveNotificationHistory(String title, String message) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Create formatted entry
        String timestamp = new SimpleDateFormat("MM-dd-yy | hh:mm:ss a", Locale.getDefault())
                .format(new Date());
        String entry = timestamp + "###" + title + "###" + message;

        // Get existing history (thread-safe)
        Set<String> history = new LinkedHashSet<>(
                prefs.getStringSet("history", new LinkedHashSet<>())
        );

        // Enforce maximum size
        if (history.size() >= MAX_HISTORY) {
            Iterator<String> it = history.iterator();
            it.next(); // Remove oldest entry
            it.remove();
        }

        // Add new entry and save
        history.add(entry);
        editor.putStringSet("history", history);
        editor.apply(); // Use apply() for async saving
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "threshold_alerts",
                    "Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Bin alert notifications");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "threshold_alerts",
                    "Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Bin alerts");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}