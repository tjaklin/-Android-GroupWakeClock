package com.tjaklin.groupwakeclock.onBootCompleted;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.tjaklin.groupwakeclock.DailyReminder.DailyReminderReceiver;

public class BootCompletionReceiver extends BroadcastReceiver {

    // This Receiver is launched upon BOOT_COMPLETED.
    // It then triggers RearmAlarmService and DailyReminderReceiver.

    @Override
    public void onReceive(Context context, Intent oldIntent) {
        Intent launchRearmAlarmsService = new Intent(context, RearmAlarmsService.class);
        Intent launchDailyReminderReceiver = new Intent(context, DailyReminderReceiver.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(launchRearmAlarmsService);
        } else {
            context.startService(launchRearmAlarmsService);
        }

        context.sendBroadcast(launchDailyReminderReceiver);

        Log.i("[BootCompletionReceiver]", "onReceive()");
    }
}
