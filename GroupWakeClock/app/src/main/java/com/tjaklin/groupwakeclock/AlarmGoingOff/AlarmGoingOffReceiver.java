package com.tjaklin.groupwakeclock.AlarmGoingOff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.Set;

public class AlarmGoingOffReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // This receiver triggers AlarmGoingOffService which is a service that is active as long
        // as the user doesn't turn the alarm off.
        Intent alarmGoingOffServiceIntent = new Intent(context, AlarmGoingOffService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            alarmGoingOffServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // This just checks if any extras are missing.
        Bundle receivedExtras = intent.getBundleExtra("svi_podaci");
        if (receivedExtras != null) {

            Log.e("[AlarmGoingOffReceiver]", "receivedExtras.size = " + receivedExtras.size());

            Set<String> keys = receivedExtras.keySet();

            for (String k : keys) {
                Log.e("[AlarmGoingOffReceiver]", "key = " + k);
            }


            Bundle newExtras = new Bundle();

            if (receivedExtras.containsKey("alarmID")) {
                long alarmID = receivedExtras.getLong("alarmID", -1);
                Log.e("[AlarmGoingOffReceiver]", "alarmID = " + alarmID);
                if (alarmID != -1) {
                    newExtras.putLong("alarmID", alarmID);
                }
            } else {
                Log.e("[AlarmGoingOffReceiver]", "extras don't contain alarmID!");
            }

            if (receivedExtras.containsKey("userEmail")) {
                String userEmail = receivedExtras.getString("userEmail", null);
                Log.e("[AlarmGoingOffReceiver]", "userEmail = " + userEmail);
                if (userEmail != null) {
                    newExtras.putString("userEmail", userEmail);
                }
            } else {
                Log.e("[AlarmGoingOffReceiver]", "extras don't contain userEmail!");
            }

            if (receivedExtras.containsKey("eventname")) {
                String eventname = receivedExtras.getString("eventname", null);
                if (eventname != null) {
                    Log.e("[AlarmGoingOffReceiver]", "eventname = " + eventname);
                    newExtras.putString("eventname", eventname);
                }
            } else {
            Log.e("[AlarmGoingOffReceiver]", "extras don't contain eventname!");
            }

            if (receivedExtras.containsKey("eventChatID")) {
                String eventChatID = receivedExtras.getString("eventChatID", null);
                if (eventChatID != null) {
                    Log.e("[AlarmGoingOffReceiver]", "eventChatID = " + eventChatID);
                    newExtras.putString("eventChatID", eventChatID);
                }
            } else {
                Log.e("[AlarmGoingOffReceiver]", "extras don't contain eventChatID!");
            }

            if (receivedExtras.containsKey("membershipID")) {
                String membershipID = receivedExtras.getString("membershipID", null);
                if (membershipID != null) {
                    Log.e("[AlarmGoingOffReceiver]", "membershipID = " + membershipID);
                    newExtras.putString("membershipID", membershipID);
                }
            } else {
                Log.e("[AlarmGoingOffReceiver]", "extras don't contain membershipID!");
            }
        alarmGoingOffServiceIntent.putExtras(newExtras);
        }

        ContextCompat.startForegroundService(context, alarmGoingOffServiceIntent);

    }
}