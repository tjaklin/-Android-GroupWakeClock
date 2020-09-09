package com.tjaklin.groupwakeclock.Util;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.util.Log;

import com.tjaklin.groupwakeclock.Alarm.AlarmRegistrationManager;
import com.tjaklin.groupwakeclock.Models.AlarmFB;
import com.tjaklin.groupwakeclock.Models.AlarmSQLite;
import com.tjaklin.groupwakeclock.Models.Event;
import com.tjaklin.groupwakeclock.Models.Membership;
import com.tjaklin.groupwakeclock.Models.User;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class DownloadUpcomingAlarmThread extends Thread {

    private final String TAG = DownloadUpcomingAlarmThread.class.getSimpleName();

    private Context context;
    private User thisUser;
    private Membership thisMembership;
    private Event thisEvent;

    private FBObjectManager fbO;
    private CustomSQLiteOpenHelper sqliteDB;

    // Current time + 24h. Used to determine if some event is happening within next 24h.
    private Calendar tomorrowDatetime;

    private int activeAsyncMethodsCounter = 0;
    private boolean shouldThreadExecute = true;

    DownloadUpcomingAlarmThread(Context c, User u, Membership m, Event g) {
        Log.d(TAG, "[Constructor()] !");

        context = c;
        thisUser = u;
        thisMembership = m;
        thisEvent = g;

        fbO = FBObjectManager.getFbObjectManager(context);
        sqliteDB = new CustomSQLiteOpenHelper(context);

        tomorrowDatetime = Calendar.getInstance();
        tomorrowDatetime.add(Calendar.HOUR_OF_DAY, 24);
    }

    @Override
    public void run() {
        Log.e(TAG, "[run()] ENTERED!");

        if (isEventUpcoming(thisMembership.getDatetimeInMillis())) {
            Log.d(TAG, "[run()] It is UPCOMING !");
            processUpcomingEvent(thisMembership, thisEvent.getEventname(), thisEvent.getEventChatID());
        } else {
            Log.d(TAG, "[run()] It is NOT UPCOMING !");
            shouldThreadExecute = false;
        }

        while (shouldThreadExecute) {
            // I don't know what to do with this. Looks bad but it works. I'm afraid to touch it.
        }
        Log.d(TAG, "Thread exited the While loop.");

        Log.e(TAG, "[run()] EXITED!");
    }

    // We only download Alarm data from the FireBase if the Event is upcoming (within 24 hrs).
    // This decision was made in order to minimise wasting the user's Internet traffic.
    // Now that i think about it, it's not really needed here. We could just download all alarms
    // that have not yet passed.
    private boolean isEventUpcoming(long event) {
        // Current time
        Calendar currentDatetime = Calendar.getInstance();

        // Time of our Event
        Calendar eventDatetime = Calendar.getInstance();
        eventDatetime.setTimeInMillis(event);

        if ((eventDatetime.after(currentDatetime)) && (eventDatetime.before(tomorrowDatetime))) {
            SimpleDateFormat sf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            String humanReadable = sf.format(eventDatetime.getTime());
            Log.d(TAG, "Event IS in the next 24h : " + humanReadable);
            return true;

        } else {
            Log.d(TAG, "Event IS NOT in the next 24h.");
            return false;
        }
    }

    private void processUpcomingEvent(Membership membership, String eventname, String eventChatID) {

        long datetime = membership.getDatetimeInMillis();

        AlarmSQLite upcomingAlarm = sqliteDB.readAlarm(datetime, thisUser.getEmail());

        if (upcomingAlarm != null) {

            // If the Alarm is already present in SQLite we check it's Type and Complexity.
            // If Type or Complexity read from SQLite are different from those read from the FireBase
            // server, we are certain that this Alarm's information in SQLite is outdated.
            // What we need to do then is to remove the Alarm from SQLite, download updated
            // information from the FireBase and then save that info to the SQLite.
            boolean matchingType = upcomingAlarm.getType().equals(membership.getAlarmTemplate().getType());
            boolean matchingComplexity = upcomingAlarm.getComplexity() == membership.getAlarmTemplate().getComplexity();

            if (!matchingType || !matchingComplexity) {
                // Type and / or Complexity are outdated. We should download the info from FB.
                Log.d(TAG, "Alarm is present in SQLite but is outdated!");
                // First we have to disarm that Alarm.
                AlarmRegistrationManager.getInstance(context).unregisterAlarmClock(thisUser.getEmail(), datetime, eventname, eventChatID, membership.getMembershipID());
                // Now we delete it.
                sqliteDB.deleteAlarm(datetime, thisUser.getEmail());

                // Here we download all the information about that specific Alarm from the FireBase.
                // We write it to SQLite after the download.
                fbO.downloadAlarmFBs(membership.getAlarmTemplate(), new FBObjectManager.AsyncListenerAlarmFBs() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "onStart()!");
                        activeAsyncMethodsCounter++;
                    }

                    @Override
                    public void onSuccess(ArrayList<AlarmFB> alarmFBS) {
                        Log.d(TAG, "onSuccess()!");
                        // This random number is used to download a random Alarm that fits our AlarmTemplate
                        // because there can be multiple Alarms of Type = x and Complexity = y.
                        int upperLimit = alarmFBS.size();
                        int randomNumber = new Random().ints(1, 0, upperLimit).findFirst().getAsInt();

                        Log.d(TAG, "[image->randomNumber] randomNumber = " + randomNumber);

                        // Here we download the Alarm's question, which is in fact just an Image.
                        fbO.downloadImage(membership.getAlarmTemplate(), alarmFBS.get(randomNumber), new FBObjectManager.AsyncListenerImage() {
                            @Override
                            public void onStart() {
                                Log.d(TAG, "onStart()!");
                                activeAsyncMethodsCounter++;
                            }

                            @Override
                            public void onSuccess(byte[] image) {
                                Log.d(TAG, "onSuccess()!");
                                AlarmSQLite newAlarmSQLite = new AlarmSQLite(datetime, eventname, thisUser.getEmail(),
                                        membership.getAlarmTemplate().getType(), membership.getAlarmTemplate().getComplexity(),
                                        image, alarmFBS.get(randomNumber).getAnswer(), eventChatID, membership.getMembershipID());
                                sqliteDB.insertAlarm(newAlarmSQLite);
                                AlarmRegistrationManager.getInstance(context).registerAlarmClock(thisUser.getEmail(), datetime, eventname, eventChatID, membership.getMembershipID());

                                activeAsyncMethodsCounter--;
                                checkIfCounterIsZero();
                            }

                            @Override
                            public void onFailed() {
                                Log.d(TAG, "onFailed()!");
                                activeAsyncMethodsCounter--;
                                checkIfCounterIsZero();
                            }
                        });

                        activeAsyncMethodsCounter--;
                        checkIfCounterIsZero();
                    }

                    @Override
                    public void onFailed() {
                        Log.d(TAG, "onFailed()!");
                        activeAsyncMethodsCounter--;
                        checkIfCounterIsZero();
                    }
                });

            } else {
                // This means that Type and Complexity are not outdated.
                Log.d(TAG, "Alarm is present in SQLite and is up to date.");
                AlarmRegistrationManager.getInstance(context).registerAlarmClock(thisUser.getEmail(), datetime, eventname, eventChatID, membership.getMembershipID());

            }
        } else {
            // We have to download this Alarm's info from the FB.
            Log.d(TAG, "Alarm is not present in SQLite!");
            // We download it using this line.
            // Then we save it to SQLite.
            fbO.downloadAlarmFBs(membership.getAlarmTemplate(), new FBObjectManager.AsyncListenerAlarmFBs() {
                @Override
                public void onStart() {
                    Log.d(TAG, "[downloadAlarmFBs] onStart()!");
                    activeAsyncMethodsCounter++;
                }

                @Override
                public void onSuccess(ArrayList<AlarmFB> alarmFBs) {
                    Log.d(TAG, "[downloadAlarmFBs] onSuccess()!");

                    if (alarmFBs.size() > 0) {

                        int upperLimit = alarmFBs.size();
                        Log.d(TAG, "alarmFBs.size = " + upperLimit);
                        for (int i = 0; i < upperLimit; i++) {
                            Log.d(TAG, "alarmFB.questionID = " + alarmFBs.get(i).getQuestionID());
                        }

                        int randomNumber = new Random().ints(1, 0, upperLimit).findFirst().getAsInt();

                        Log.d(TAG, "[image->randomNumber] randomNumber = " + randomNumber);

                        fbO.downloadImage(membership.getAlarmTemplate(), alarmFBs.get(randomNumber), new FBObjectManager.AsyncListenerImage() {
                            @Override
                            public void onStart() {
                                Log.d(TAG, "[downloadImage] onStart()!");
                                activeAsyncMethodsCounter++;
                            }

                            @Override
                            public void onSuccess(byte[] image) {
                                Log.d(TAG, "[downloadImage] onSuccess()!");
                                AlarmSQLite newAlarmSQLite = new AlarmSQLite(datetime, eventname, thisUser.getEmail(),
                                        membership.getAlarmTemplate().getType(), membership.getAlarmTemplate().getComplexity(),
                                        image, alarmFBs.get(randomNumber).getAnswer(), eventChatID, membership.getMembershipID());
                                sqliteDB.insertAlarm(newAlarmSQLite);
                                AlarmRegistrationManager.getInstance(context).registerAlarmClock(thisUser.getEmail(), datetime, eventname, eventChatID, membership.getMembershipID());


                                activeAsyncMethodsCounter--;
                                checkIfCounterIsZero();

                            }

                            @Override
                            public void onFailed() {
                                Log.d(TAG, "[downloadImage] onFailed()!");
                                activeAsyncMethodsCounter--;
                                checkIfCounterIsZero();
                            }
                        });

                        activeAsyncMethodsCounter--;
                        checkIfCounterIsZero();

                    } else {
                        Log.d(TAG, "alarmFBs.size() == EMPTY!");
                    }
                }

                @Override
                public void onFailed() {
                    Log.d(TAG, "[downloadAlarmFBs] onFailed()!");
                    activeAsyncMethodsCounter--;
                    checkIfCounterIsZero();
                }
            });
        }
    }

    private void checkIfCounterIsZero() {
        if (activeAsyncMethodsCounter == 0) {
            Log.e(TAG, "[checkIfCounterIsZero]  activeAsyncMethodsCounter == 0 !");
            shouldThreadExecute = false;
        }
    }
}
