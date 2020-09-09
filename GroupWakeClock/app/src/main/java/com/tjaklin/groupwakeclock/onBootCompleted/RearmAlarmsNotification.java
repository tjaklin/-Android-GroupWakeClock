package com.tjaklin.groupwakeclock.onBootCompleted;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.tjaklin.groupwakeclock.R;

public class RearmAlarmsNotification {

    private static final String TAG = RearmAlarmsNotification.class.getSimpleName();
    private static final String CHANNEL_ID = "313";

    private Context context;

    RearmAlarmsNotification(Context c) {
        context = c;
        createNotificationChannel();
    }

    public NotificationCompat.Builder buildNotification(String title, String content) {
        if(context == null) {
            Log.d(TAG, "Error! Context not set!");
            return null;
        }

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_rearm_24px)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "BOOT_COMPLETED_ALARM_REGISTRATION";
            String description = "Notifies that boot has succesfully completed. It is time to reregister all alarms present in SQLite";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }
}
