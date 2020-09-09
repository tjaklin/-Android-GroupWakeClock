package com.tjaklin.groupwakeclock.Event;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tjaklin.groupwakeclock.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tjaklin.groupwakeclock.Models.AlarmTemplate;
import com.tjaklin.groupwakeclock.Models.Member;
import com.tjaklin.groupwakeclock.Models.Membership;
import com.tjaklin.groupwakeclock.Models.NewMemberRVAdapter;
import com.tjaklin.groupwakeclock.Models.User;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;

import java.util.ArrayList;

public class AddMemberActivity extends AppCompatActivity {

    // This activity displays a list of our user's friends. The user can select those friends
    // they want to add to some Event that launched this Activity. We get that Event's information
    // via the Intent extras.

    private static final String TAG = AddMemberActivity.class.getSimpleName();

    // FBObjectManager manages calls towards the FireBase server.
    private FBObjectManager fbO;

    // Holds an ArrayList of type Models.User.
    private RecyclerView rv_friends;
    private NewMemberRVAdapter rva;

    // This TextView is displayed if our RecyclerView is empty. It says that "No data is found".
    private TextView tv_noData;
    // Pressing this fab results in adding all of the selected Friends from rv_friends to the Event.
    FloatingActionButton fab_addToEvent;

    // We use these variables to hold the information we received through Intent Extras.
    private String eventID, defaultAlarmType;
    private long datetime;
    private int defaultAlarmComplexity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        Bundle extras = getIntent().getExtras();

        // Check if any extras are missing.
        if (extras != null) {
            if (extras.containsKey("eventID") ) {
                eventID = extras.getString("eventID", null);
                if (eventID != null) {
                    Log.d(TAG, "eventID succesfully retrieved from extras");
                } else {
                    Log.d(TAG, "Failed to retrieve eventID from extras");
                }
            } else {
                Log.d(TAG, "extras doesn't contain eventID");
            }

            if (extras.containsKey("defaultAlarmType") ) {
                defaultAlarmType = extras.getString("defaultAlarmType", null);
                if (defaultAlarmType != null) {
                    Log.d(TAG, "defaultAlarmType succesfully retrieved from extras");
                } else {
                    Log.d(TAG, "Failed to retrieve defaultAlarmType from extras");
                }
            } else {
                Log.d(TAG, "extras doesn't contain defaultAlarmType");
            }

            if (extras.containsKey("datetime") ) {
                datetime = extras.getLong("datetime", -1);
                if (datetime != -1) {
                    Log.d(TAG, "datetime succesfully retrieved from extras");
                } else {
                    Log.d(TAG, "Failed to retrieve datetime from extras");
                }
            } else {
                Log.d(TAG, "extras doesn't contain datetime");
            }

            if (extras.containsKey("defaultAlarmComplexity") ) {
                defaultAlarmComplexity = extras.getInt("defaultAlarmComplexity", -1);
                if (defaultAlarmComplexity != -1) {
                    Log.d(TAG, "defaultAlarmComplexity succesfully retrieved from extras");
                } else {
                    Log.d(TAG, "Failed to retrieve defaultAlarmComplexity from extras");
                }
            } else {
                Log.d(TAG, "extras doesn't contain defaultAlarmComplexity");
            }

        } else {
            Log.d(TAG, "extras == NULL");
        }

        fbO = FBObjectManager.getFbObjectManager(this);
        // Inflate widgets.
        spawnGUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        rva.replaceData(spawnPossibleNewMembers());
        showHideRecyclerView();
    }

    private void spawnGUI() {
        tv_noData = findViewById(R.id.tv_noData);

        rv_friends = findViewById(R.id.rv_members);
        rv_friends.setNestedScrollingEnabled(false);
        rv_friends.setHasFixedSize(false);

        rv_friends.setLayoutManager(new LinearLayoutManager( getApplicationContext(), RecyclerView.VERTICAL, false));

        rva = new NewMemberRVAdapter(this, spawnPossibleNewMembers());
        rv_friends.setAdapter(rva);
        showHideRecyclerView();

        fab_addToEvent = findViewById(R.id.fab_addToGroup);
        fab_addToEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "fab_addMembers.Clicked!");
                Log.d(TAG, "[BEFORE] currentGroupMembers.size = " + fbO.getCurrentEventMembers().size());

                for (final Member u : rva.returnSelectedItems()) {
                    // Creates new entries in the FireBase server - adds new participants.
                    // It also sets their AlarmTemplate using the default Event's default
                    // Alarm Type and Complecxity.
                    fbO.uploadMembership(new Membership(eventID, u.getUserID(), datetime,
                            new AlarmTemplate(defaultAlarmType, defaultAlarmComplexity)),
                            new FBObjectManager.AsyncListenerString() {
                        @Override
                        public void onStart() {
                            Log.d(TAG, "Beginning upload of some mebership");
                        }

                        @Override
                        public void onSuccess(String membershipID) {
                            Log.d(TAG, "Upload of mebership " + membershipID + " finished successfully");
                        }

                        @Override
                        public void onFailed() {
                            Log.e(TAG, "Upload of some mebership failed");
                        }
                    });
                }
                Log.d(TAG, "[AFTER] currentGroupMembers.size = " + fbO.getCurrentEventMembers().size());

                // Close the Activity.
                finish();
            }
        });
    }

    // This method gets a list of the User's current Friends. It then filters out the Friends that
    // are already Participating in the Event.
    private ArrayList<Member> spawnPossibleNewMembers() {
        ArrayList<Member> possibleNewMembers = new ArrayList<>();

        for (User u : fbO.getMyOwnFriends())
            possibleNewMembers.add( new Member(u.getUserID(), u.getEmail()) );

        Log.d(TAG, "possibleNewMembers.size = " + possibleNewMembers.size());

        if (possibleNewMembers.removeAll(fbO.getCurrentEventMembers()) ) {
            Log.d(TAG, "Removal done. possibleNewMembers.size = " + possibleNewMembers.size());
        } else {
            Log.d(TAG, "Removal failed. possibleNewMembers.size = " + possibleNewMembers.size());
        }
        return possibleNewMembers;
    }

    private void showHideRecyclerView() {
        if (rva != null) {
            rva.notifyDataSetChanged();
            if (rva.getItemCount() == 0) {
                rv_friends.setVisibility(View.GONE);
                tv_noData.setVisibility(View.VISIBLE);
            } else {
                rv_friends.setVisibility(View.VISIBLE);
                tv_noData.setVisibility(View.INVISIBLE);
            }
        }
    }
}
