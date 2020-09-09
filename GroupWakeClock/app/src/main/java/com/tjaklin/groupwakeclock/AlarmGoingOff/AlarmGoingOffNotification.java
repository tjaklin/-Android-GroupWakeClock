package com.tjaklin.groupwakeclock.AlarmGoingOff;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.tjaklin.groupwakeclock.R;

public class AlarmGoingOffNotification {

    private static final String TAG = AlarmGoingOffNotification.class.getSimpleName();
    private static final String CHANNEL_ID = "Alarm_Going_Off";

    private Context context;
    private long alarmID;
    private String eventname;
    private String eventChatID;
    private String membershipID;
    private String userEmail;

    AlarmGoingOffNotification(Context c, long aID, String ename, String cID, String mID, String uEM) {
        context = c;
        alarmID = aID;
        eventname = ename;
        eventChatID = cID;
        membershipID = mID;
        userEmail = uEM;
        createNotificationChannel();
    }

    public NotificationCompat.Builder buildNotification(String title, String content) {
        if(context == null) {
            Log.d(TAG, "Error! Context not set!");
            return null;
        }

        // This intent will launch AlarmGoingOffActivity when user clicks the Notification.
        Intent launchAlarmGoingOffActivityIntent = new Intent(context, AlarmGoingOffActivity.class);
        launchAlarmGoingOffActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Bundle extras = new Bundle();
        extras.putLong("alarmID", alarmID);
        extras.putString("userEmail", userEmail);
        extras.putString("eventname", eventname);
        extras.putString("eventChatID", eventChatID);
        extras.putString("membershipID", membershipID);
        launchAlarmGoingOffActivityIntent.putExtras(extras);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, launchAlarmGoingOffActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_access_alarm_black_24dp)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm";
            String description = "Alarm Going Off Notification Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }
}
