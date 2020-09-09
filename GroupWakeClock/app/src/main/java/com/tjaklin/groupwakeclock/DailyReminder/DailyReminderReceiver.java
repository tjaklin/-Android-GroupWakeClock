package com.tjaklin.groupwakeclock.DailyReminder;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class DailyReminderReceiver extends BroadcastReceiver {

    private static final String TAG = DailyReminderReceiver.class.getSimpleName();
    // We don't want to launch the Daily Reminder too early in the morning or too late in the
    // evening, so we use lowerTimeLimit and upperTimeLimit to set the "acceptable time period"
    // for launching the reminder.
    private int lowerTimeLimit = 11;
    private int upperTimeLimit = 18;
    private Context context;

    // This method checks if now is the acceptable time of day to spawn a Daily Reminder.
    // I think that the acceptable time period is between 11h and 18h.
    private boolean isCorrectTimeOfDay() {

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= lowerTimeLimit && timeOfDay <= upperTimeLimit) {
            // We're good to proceed.
            return true;

        } else {
            // We're not good.
            return false;
        }
    }

    // Calculate when to trigger the next Daily Reminder!
    private long getNextTriggerTime() {

        // Trigger time is right in the middle of our "acceptable time period".
        int triggerHour = (int) Math.round((lowerTimeLimit + upperTimeLimit) / 2.0);

        Calendar triggerTime = Calendar.getInstance();
        triggerTime.set(Calendar.HOUR_OF_DAY, triggerHour);

        Calendar currentTime = Calendar.getInstance();

        if (currentTime.before(triggerTime)) {
            // Set the alarm to trigger at triggerTime today.
            // That's already set.

        } else {
            // Set the alarm to trigger at triggerTime tomorrow.
            triggerTime.set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH) + 1);
        }

        return triggerTime.getTimeInMillis();
    }

    private void scheduleNextDailyReminder(long millis) {
        Log.d(TAG, "scheduleNextServiceRevival()!");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, DailyReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                314, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
    }

    private void spawnNotification() {
        DailyReminderNotification notification = new DailyReminderNotification(context);

        NotificationCompat.Builder notificationBuilder = notification.
                buildNotification( "Daily Reminder to open your App", "You might have some new upcoming Events.");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(315, notificationBuilder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("[DailyReminderReceiver]", "onReceive()");

        this.context = context;

        if (isCorrectTimeOfDay()) {
            // Display our Notification
            Log.d(TAG, "Showing our Notification!");

            spawnNotification();

        } else {
            // Do nothing i think . . .
            Log.e(TAG, "Not showing our Notification!");
        }

        scheduleNextDailyReminder(getNextTriggerTime());
    }

}
