package com.tjaklin.groupwakeclock.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tjaklin.groupwakeclock.AlarmGoingOff.AlarmGoingOffReceiver;
import com.tjaklin.groupwakeclock.Models.AlarmSQLite;
import com.tjaklin.groupwakeclock.Util.CustomSQLiteOpenHelper;

import java.util.ArrayList;

public class AlarmRegistrationManager {

    // This class is a singleton that does some work with AlarmClock.
    // Work like arming and disarming Alarms.

    private static final String TAG = AlarmRegistrationManager.class.getSimpleName();

    private Context context;
    private static AlarmRegistrationManager instance;
    // It needs to communicate with SQLite to read relevant information about alarms it needs to
    // register (arm) or unregister (disarm)
    private CustomSQLiteOpenHelper db;

    private AlarmRegistrationManager(Context c) {
        context = c;
        db = new CustomSQLiteOpenHelper(context);
    }

    public static AlarmRegistrationManager getInstance(Context c) {
        if (instance != null) {
            return instance;
        } else {
            instance = new AlarmRegistrationManager(c);
            return instance;
        }
    }


    public void registerAlarmClock(String email, long eventDatetime, final String eventname, final String eventChatID, final String membershipID) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmGoingOffReceiver.class);

        Bundle extras = new Bundle();
        extras.putLong("alarmID", eventDatetime);
        extras.putString("userEmail", email);
        extras.putString("eventname", eventname);
        extras.putString("eventChatID", eventChatID);
        extras.putString("membershipID", membershipID);
        intent.putExtra("svi_podaci", extras);

        Log.d("[registerAlarm]", "alarmID = " + eventDatetime);
        Log.d("[registerAlarm]", "userEmail = " + email);
        Log.d("[registerAlarm]", "eventname = " + eventname);
        Log.d("[registerAlarm]", "eventChatID = " + eventChatID);
        Log.d("[registerAlarm]", "membershipID = " + membershipID);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                Long.valueOf(eventDatetime).intValue(), intent, PendingIntent.FLAG_ONE_SHOT);

        if (alarmManager != null) {

            AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(eventDatetime, null);
            alarmManager.setAlarmClock(ac, pendingIntent);

            Log.d(TAG, "Alarm succesfully REGISTERED via AlarmManager!");

        } else {
            Log.e(TAG, "registerEvent(): alarmManager is null!");
        }
    }

    public void unregisterAlarmClock(String email, long eventDatetime, final String eventname, final String eventChatID, final String membershipID) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmGoingOffReceiver.class);

        Bundle extras = new Bundle();
        extras.putLong("alarmID", eventDatetime);
        extras.putString("userEmail", email);
        extras.putString("eventname", eventname);
        extras.putString("eventChatID", eventChatID);
        extras.putString("membershipID", membershipID);
        intent.putExtra("svi_podaci", extras);

        Log.d("[registerAlarm]", "alarmID = " + eventDatetime);
        Log.d("[registerAlarm]", "userEmail = " + email);
        Log.d("[registerAlarm]", "eventname = " + eventname);
        Log.d("[registerAlarm]", "eventChatID = " + eventChatID);
        Log.d("[registerAlarm]", "membershipID = " + membershipID);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                Long.valueOf(eventDatetime).intValue(), intent, PendingIntent.FLAG_ONE_SHOT);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Alarm succesfully UNREGISTERED via AlarmManager!");
        } else {
            Log.d(TAG, "unregisterEvent(): alarmManager is null!");
        }
    }

    // Arming and Disarming of AlarmClocks
    public void rearmAlarmClocksForUser(String email) {
        // This will automatically rearm all upcoming alarms saved in SQLite for a certain user
        Log.d(TAG, "rearmAlarmClocksForUser!");

        if (db.readAlarms(email) != null) {
            ArrayList<AlarmSQLite> alarms = new ArrayList<>(db.readAlarms(email));
            for (AlarmSQLite a : alarms) {
                registerAlarmClock(a.getEmail(), a.getDatetime(), a.getGroupname(), a.getGroupChatID(), a.getMembershipID());
            }
        } else {
            Log.d(TAG, "No Alarms found in SQLite!");
        }
    }

    public void disarmAlarmClocksForUser(String email) {
        // This will automatically disarm all upcoming alarms saved in SQLite for a certain user
        Log.d(TAG, "disarmAlarmClocksForUser!");

        if (db.readAlarms(email) != null) {
            ArrayList<AlarmSQLite> alarms = new ArrayList<>(db.readAlarms(email));
            for (AlarmSQLite a : alarms) {
                unregisterAlarmClock(a.getEmail(), a.getDatetime(), a.getGroupname(), a.getGroupChatID(), a.getMembershipID());
            }
        } else {
            Log.d(TAG, "No Alarms found in SQLite!");
        }
    }

}