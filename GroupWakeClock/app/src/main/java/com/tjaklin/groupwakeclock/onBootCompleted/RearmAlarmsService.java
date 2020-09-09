package com.tjaklin.groupwakeclock.onBootCompleted;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tjaklin.groupwakeclock.Alarm.AlarmRegistrationManager;

import java.util.ArrayList;

public class RearmAlarmsService extends Service {

    private static final String TAG = RearmAlarmsService.class.getSimpleName();
    private FirebaseUser fbUser;
    // This thread takes care of rearming Alarms (AlarmClocks).
    private ReregisterAlarmsFromSQLiteThread thread;

    public RearmAlarmsService() {
        // If no user's logged in we have nothing to do as we don't know which alarms to rearm.
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            Log.e(TAG, "[fbUser] NULL!");
            endService();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()!");

        thread = new ReregisterAlarmsFromSQLiteThread();

    }

    private void moveToForeGround() {
        // Spawn a Notification
        RearmAlarmsNotification notification = new RearmAlarmsNotification(getApplicationContext());
        NotificationCompat.Builder notificationBuilder = notification.buildNotification(
                "Rearming upcoming Alarms", "Rearming upon phone reboot");
        startForeground(1, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand()!");

        moveToForeGround();

        thread.start();

        return START_STICKY;
    }

    private void endService() {
        Log.d(TAG, "Job is done. Ending Service now!");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy()!");

        thread.interrupt();
        thread = null;
    }

    // Rearms alarms using AlarmRegistrationManager's methods.
    private class ReregisterAlarmsFromSQLiteThread extends Thread {

        private AlarmRegistrationManager arManager;

        ReregisterAlarmsFromSQLiteThread() {
            Log.d(TAG, "Constructed!");
            arManager = AlarmRegistrationManager.getInstance(getApplication());
        }

        @Override
        public void run() {
            Log.d(TAG, "Ran!");

            arManager.rearmAlarmClocksForUser(fbUser.getEmail());

            endService();
        }

    }

}
