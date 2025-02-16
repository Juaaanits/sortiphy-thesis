package com.reviewers.sortiphy;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class FirestoreWorker extends Worker {

    private static final String CHANNEL_ID = "firestore_background_channel";
    private static final int THRESHOLD = 90;
    private static final String[] CLASS_IDS = {"trashClassificationOne",
            "trashClassificationTwo", "trashClassificationThree",
            "trashClassificationFour", "trashClassificationFive"};

    public FirestoreWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (String classId : CLASS_IDS) {
            db.collection("binData").document(classId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            Double value = snapshot.getDouble("fillLevel");
                            String type = snapshot.getString("className");
                            if (value != null && value >= THRESHOLD) {
                                sendNotification("Trash Bag almost Full!", "The " + type + " trash bag is " + value + "% full! Please replace now!"
                                        , type + " level currently at " + value + "%.");
                            }
                        }
                    });
        }

        return Result.success();
    }

    private void sendNotification(String title, String message, String notificationMessage) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Firestore Background Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Firestore Background Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());

        /*new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show()
        ); //for debugging*/

        saveNotificationHistory(title, notificationMessage);
    }

    private void saveNotificationHistory(String title, String message) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("NotificationHistory", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String timestamp = new SimpleDateFormat("MM-dd-yy | hh:mm:ss a", Locale.getDefault()).format(new Date());
        String entry = timestamp + "###" + title + "###" + message;

        Set<String> historySet = prefs.getStringSet("history", new LinkedHashSet<>());
        Set<String> history = new LinkedHashSet<>(historySet);

        if (history.size() >= 50) {
            history.remove(history.iterator().next());
        }
        history.add(entry);

        editor.putStringSet("history", history);
        editor.apply();
    }
}
