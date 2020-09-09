package com.tjaklin.groupwakeclock.AlarmGoingOff;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmGoingOffService extends Service {

    // This Service is active until the user succesfully turns off the alarm.
    // If the user doesn't turn off the alarm, it will automatically stop 2 minutes after it's
    // "launch".

    // The Service also holds a Thread object which vibrates the mobile device to let the user know
    // that some alarm is going off.

    private static final String TAG = AlarmGoingOffService.class.getSimpleName();
    private boolean shouldVibrate;
    private VibratorThread vibratorThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()!");

        shouldVibrate = false;
        vibratorThread = new VibratorThread();
    }

    private void moveToForeGround(long aID, String ename, String cID, String mID, String uEM) {
        // Spawn a notification.
        AlarmGoingOffNotification notification = new AlarmGoingOffNotification(getApplicationContext(), aID, ename, cID, mID, uEM);
        NotificationCompat.Builder notificationBuilder = notification.buildNotification("Alarm going off for event:", ename);
        startForeground(1, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand()!");

        // Check if any extras are missing.
        Bundle receivedExtras = intent.getExtras();
        if (receivedExtras != null) {
            if (receivedExtras.containsKey("alarmID")) {
                long alarmID = receivedExtras.getLong("alarmID", -1);

                if (alarmID != -1) {

                    if (receivedExtras.containsKey("userEmail")) {
                        String userEmail = receivedExtras.getString("userEmail", null);

                        if (userEmail != null) {

                            if (receivedExtras.containsKey("eventname")) {
                                String eventname = receivedExtras.getString("eventname", null);

                                if (eventname != null) {

                                    if (receivedExtras.containsKey("eventChatID")) {
                                        String eventChatID = receivedExtras.getString("eventChatID", null);

                                        if (eventChatID != null) {

                                            if (receivedExtras.containsKey("membershipID")) {
                                                String membershipID = receivedExtras.getString("membershipID", null);

                                                if (membershipID != null) {
                                                    moveToForeGround(alarmID, eventname, eventChatID, membershipID, userEmail);
                                                }
                                            }
                                        }
                                    } else {
                                        Log.e("[onStartCommand]", "extras don't contain eventChatID!");
                                    }
                                }
                            } else {
                                Log.e("[onStartCommand]", "extras don't contain eventname!");
                            }
                        }
                    } else {
                        Log.e("[onStartCommand]", "extras don't contain userEmail!");
                    }
                }
            } else {
                Log.e("[onStartCommand]", "extras don't contain alarmID!");
            }


        } else {
            moveToForeGround(-1, null, null,null, null);
        }

        // Start the vibrating thread.
        shouldVibrate = true;
        vibratorThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy()!");

        shouldVibrate = false;
        vibratorThread.interrupt();
        vibratorThread = null;
    }

    private class VibratorThread extends Thread {

        // This Thread takes care of vibrating the mobile device.
        // It also keeps track of number of vibrations passed since the launch.
        // After a certain number of vibrations (around 2 minutes worth of vibrations) it will
        // turn itself and it's parent service off.

        private final String TAG = VibratorThread.class.getSimpleName();

        private Vibrator v;
        // One vibration lasts for 0.375 seconds.
        private final long vibrateDuration = 375;
        // This is how long we wait between 2 vibrations.
        private final long sleepDuration = 2 * vibrateDuration;

        // We turn it off after the 106th vibration.
        private long vibrationsPassedCounter = 0;

        VibratorThread() {
            Log.d(TAG, "Constructed!");
            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        @Override
        public void run() {
            Log.d(TAG, "Ran!");

            while (shouldVibrate) {
                try {
                    ++vibrationsPassedCounter;
                    if (vibrationsPassedCounter > 106)
                        stopSelf();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        v.vibrate(VibrationEffect.createOneShot(vibrateDuration, VibrationEffect.DEFAULT_AMPLITUDE));
                    else
                        v.vibrate(vibrateDuration);

                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    shouldVibrate = false;
                    e.printStackTrace();
                }
            }

        }
    }
}