package com.tjaklin.groupwakeclock.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.tjaklin.groupwakeclock.R;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.tjaklin.groupwakeclock.DailyReminder.DailyReminderReceiver;
import com.tjaklin.groupwakeclock.Models.User;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;
import com.tjaklin.groupwakeclock.Util.HelperMethods;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private TabItem tab_events, tab_friends, tab_alarms;

    private ViewPager vp_fragments;
    private ViewPagerFragmentsAdapter vpfa;

    // FBObjectManager is a custom class that manages all work done by the FireBase server.
    private FBObjectManager fbO;

    private String userID, userEmail;

    //todo: Spojiti sa StartTutorial!
    private boolean startTutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (!checkIntentExtras()) {
            Log.e(TAG, "[INTENT] Failed to retrieve Extras!");
        }

        fbO = FBObjectManager.getFbObjectManager(this);
        // FBObjectManager holds a 'thisUser' variable of type User that holds all information
        // about the currently logged-in user.
        fbO.setThisUser(new User(userID, userEmail));

        // Checks if we managed to save our user's info to RAM.
        if (fbO.getThisUser() != null) {
            Log.d(TAG, "[thisUser] .userID = " + fbO.getThisUser().getUserID()
                    + ", .userEmail = " + fbO.getThisUser().getEmail());
        } else {
            Log.e(TAG, "[thisUser] is NULL !");
        }

        // Inflates all Widgets.
        spawnGUI();

        // We want to let the user know if there's no Internet connection currently available.
        // This is because our data concerning the 'events' is stored in FireBase servers, and is
        // not stored in SQLite. Every time the user launches the App, we pull the relevant
        // 'events' data from the FireBase and store it to RAM via FBObjectManager's member variables.
        //
        // If there is no internet connection, the user cannot access their 'events' data.
        if (!HelperMethods.isNetworkAvailable(this)) {
            Toast.makeText(this, "Network connectivity unavailable!",
                    Toast.LENGTH_SHORT).show();
        }

        // Check comment at line 180
        checkIfDailyReminderSet();
    }

    private void spawnGUI() {

        // Displays the email of currently logged in user in the top left corner.
        ( (TextView) findViewById(R.id.toolbar_tv_userID) ).setText(userEmail);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        tabLayout = findViewById(R.id.tabLayout);
        tab_events = findViewById(R.id.tab_events);
        tab_friends = findViewById(R.id.tab_friends);
        tab_alarms = findViewById(R.id.tab_alarms);

        vp_fragments = findViewById(R.id.vp_fragments);
        vpfa = new ViewPagerFragmentsAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        vp_fragments.setAdapter(vpfa);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vp_fragments.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        vp_fragments.addOnPageChangeListener( new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    // Checks if any of the extras received via the intent are missing.
    private boolean checkIntentExtras() {

        boolean extrasCorrectlyReceived = true;

        // Get data from extras:
        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            if (extras.containsKey("userID")) {

                userID = extras.getString("userID", null);

                if (userID == null) {
                    extrasCorrectlyReceived = false;
                    Log.e(TAG, "Failed to retrieve userID from extras!");

                } else {
                    Log.d(TAG, "Succesfully retrieved userID from extras!");
                }

            } else {
                extrasCorrectlyReceived = false;
                Log.d(TAG, "extras.containsKey('userID') == FALSE!");
            }

            if (extras.containsKey("userEmail")) {

                userEmail = extras.getString("userEmail", null);

                if (userEmail == null) {
                    extrasCorrectlyReceived = false;
                    Log.e(TAG, "Failed to retrieve userEmail from extras!");

                } else {
                    Log.d(TAG, "Succesfully retrieved userEmail from extras!");
                }

            } else {
                extrasCorrectlyReceived = false;
                Log.d(TAG, "extras.containsKey('userEmail') == FALSE!");
            }

            if (extras.containsKey("startTutorial")) {

                startTutorial = extras.getBoolean("startTutorial");
                Log.d(TAG, "Succesfully retrieved userEmail from extras!");

            } else {
                extrasCorrectlyReceived = false;
                Log.d(TAG, "extras.containsKey('startTutorial') == FALSE!");
            }

        } else {
            extrasCorrectlyReceived = false;
            Log.d(TAG, "extras == NULL!");
        }

        return extrasCorrectlyReceived;
    }

    // This App uses 'Daily Reminders' which are just daily Notifications that are meant to
    // remind the user to launch the app.
    // Reminding the user to launch their App is necessary because the App itself doesn't
    // implement any kind of mechanism that would periodically query the server for relevant
    // new information.
    //
    // This method is the initial trigger that starts the 'Daily Reminder' notifications.
    // If a 'Daily Reminder' has not yet been scheduled (e.g. After a fresh installation),
    // this method will take care of scheduling it.
    private void checkIfDailyReminderSet() {

        Intent launchDailyReminderReceiver = new Intent(this, DailyReminderReceiver.class);

        // requestCode 314 is used for Daily Reminders.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 314,
                launchDailyReminderReceiver, PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent == null) {
            Log.d(TAG, "pendingIntent == null");
            sendBroadcast(launchDailyReminderReceiver);
        }
    }
}