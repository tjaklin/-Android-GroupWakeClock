package com.tjaklin.groupwakeclock.DailyReminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.tjaklin.groupwakeclock.R;
import com.tjaklin.groupwakeclock.Home.LoginActivity;

public class DailyReminderNotification {

    private static final String TAG = DailyReminderNotification.class.getSimpleName();
    private static final String CHANNEL_ID = "314";

    private Context context;

    DailyReminderNotification(Context c) {
        context = c;
        createNotificationChannel();
    }

    public NotificationCompat.Builder buildNotification(String title, String content) {
        if(context == null) {
            Log.d(TAG, "Error! Context not set!");
            return null;
        }

        // When user clicks on the Notification we want to launch LoginActivity (which redirects
        // them to HomeActivity)
        Intent launchAppIntent = new Intent(context, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_member_asleep_24px)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(null)
                .setAutoCancel(true);
    }

    // We just want to remind the user to launch the App so it can download any new upcoming Events.
    // This approach has some disadvantages, but i don't think it's that bad.
    //
    // An alternative solution could be to have a BroadcastReceiver listen for when network connection
    // is established and the automatically download new data. This aproach is a bit too agressive in
    // my opinion so i went with the more passive one.
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "DAILY_REMINDER";
            String description = "Reminds user to open the app at least once a day to check if any events are nearing.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }


}
