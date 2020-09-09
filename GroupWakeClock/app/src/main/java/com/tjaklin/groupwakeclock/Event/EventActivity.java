package com.tjaklin.groupwakeclock.Event;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjaklin.groupwakeclock.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tjaklin.groupwakeclock.Models.Event;
import com.tjaklin.groupwakeclock.Models.MemberRVAdapter;
import com.tjaklin.groupwakeclock.Models.Message;
import com.tjaklin.groupwakeclock.Models.MessageRVAdapter;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;

import java.util.Locale;

public class EventActivity extends AppCompatActivity {

    // This Activity displays all the important information about an Event.
    // Including Event's Name, Description, Date, Time, Chat, List of members/participants,
    // Whether those participants are awake or not, Number of members that are still asleep.

    private static final String TAG = EventActivity.class.getSimpleName();

    private FBObjectManager fbO;

    // We use this to show current event members.
    private RecyclerView rv_members;
    private MemberRVAdapter rva_members;
    private TextView tv_noData;
    // This fab launches AddMemberActivity.
    private FloatingActionButton fab_addMembers;

    // We use this to display messages in event's chat.
    private RecyclerView rv_chat;
    private MessageRVAdapter rva_chat;
    // This EditText is used to type messages.
    private EditText et_messageContent;
    // This is a button that sends et_messageContent's content as a message.
    private ImageButton ib_messageSend;

    // Regular views
    private Toolbar toolbar;
    // These ImageButtons spawn Dialogs that present some crucial Event's information.
    // We use the Dialogs to save on Screen Space. (Dialogs take up space only when they're clicked).
    private ImageButton ib_eventInfo, ib_timeInfo, ib_memberInfo;
    // This LinearLayout and TextView are hidden, and will be made visible once the Event becomes
    // triggered (When current time == event's time). It displays the number of members that are
    // awake.
    private LinearLayout ll_awake;
    private TextView awake_tv_counter;

    private Event thisEvent;
    private String userEmail;

    // If user is admin give them special permissions (to manipulate AlarmTemplates).
    // hasDatetimePassed determines if we should show ll_awake.
    private boolean isAdmin, hasDatetimePassed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Bundle extras = getIntent().getExtras();

        // Check if any extras are missing.
        if (extras != null) {
            if (extras.containsKey("event")) {
                thisEvent = extras.getParcelable("event");
                if (thisEvent != null) {
                    Log.d(TAG, "thisEvent succesfully read from extras!");
                } else {
                    Log.e(TAG, "Failed to read thisEvent from extras!");
                }
            } else {
                Log.e(TAG, "Extras don't contain thisEvent key!");
            }
        } else {
            Log.d(TAG, "Extras == NULL!");
        }

        fbO = FBObjectManager.getFbObjectManager(this);

        Log.d(TAG, "thisEvent.getEventID = " + thisEvent.getEventID());

        Calendar currentTime = Calendar.getInstance();

        // Check if Event has been triggered.
        if (currentTime.after(thisEvent.getDatetime())) {
            hasDatetimePassed = true;
        }

        // We add a 'Listener' that downloads all relevant Memberships.
        // Memberships store information about whether the members are awake or not.
        fbO.listenToCurrentEventMembershipsByEventID(thisEvent.getEventID(), new FBObjectManager.AsyncListenerVoid() {
            @Override
            public void onStart() {
                Log.d(TAG, "[listenToCurrentGroupMembershipsByGroupID]. onStart!");
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "[listenToCurrentGroupMembershipsByGroupID]. onSuccess!");
                if (rva_members != null ) {
                    rva_members.notifyDataSetChanged();
                    if (rv_members != null) {
                        if (rva_members.getItemCount() == 0) {
                            rv_members.setVisibility(View.GONE);
                            tv_noData.setVisibility(View.VISIBLE);
                        } else {
                            rv_members.setVisibility(View.VISIBLE);
                            tv_noData.setVisibility(View.INVISIBLE);
                        }
                    }
                    // When the Event triggers we display how many users are awake.
                    if ( hasDatetimePassed && awake_tv_counter != null) {
                        awake_tv_counter.setText(rva_members.getAwakeToSleepingRatio());
                    }
                }
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[listenToCurrentGroupMembershipsByGroupID]. onFailed!");

            }
        });

        // We add a 'Listener' that downloads all Chat messages for this Event.
        fbO.listenToCurrentEventChat(thisEvent.getEventChatID(), new FBObjectManager.AsyncListenerVoid() {
            @Override
            public void onStart() {
                Log.d(TAG, "[listenToCurrentGroupChat] onStart()");
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "[listenToCurrentGroupChat] onSuccess()");
                if (rva_chat != null ) {
                    rva_chat.notifyDataSetChanged();
                    // This line forces rv_chat to scroll to the most recent message.
                    rv_chat.smoothScrollToPosition(fbO.getCurrentEventChat().size() - 1);
                }
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[listenToCurrentGroupChat] onFailed()");
            }
        });

        userEmail = fbO.getThisUser().getEmail();

        if (fbO.getThisUser().getUserID().equals(thisEvent.getAdminUserID()))
            isAdmin = true;
        else
            isAdmin = false;

        // Inflate Widgets.
        spawnRegularGUI();

    }

    // "Regular GUI" refers to Toolbar, toolbar's buttons.
    private void spawnRegularGUI() {
        ( (TextView) findViewById(R.id.toolbar_tv_eventname) ).setText(thisEvent.getEventname());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        ib_memberInfo = findViewById(R.id.ib_memberInfo);
        ib_memberInfo.setOnClickListener(v -> {
            Log.d(TAG, "ib_memberInfo.onClicked!");
            spawnMemberListDialog();
        });

        ib_eventInfo = findViewById(R.id.ib_eventInfo);
        ib_eventInfo.setOnClickListener(v -> {
            Log.d(TAG, "ib_eventInfo clicked()!");
            spawnEventInfoDialog();
        });

        ib_timeInfo = findViewById(R.id.ib_timeInfo);
        ib_timeInfo.setOnClickListener(v -> {
            Log.d(TAG, "ib_timeInfo clicked()!");
            spawnEventTimeDialog();
        });

        rva_members = new MemberRVAdapter(this, hasDatetimePassed , fbO.getCurrentEventMembers());
        rva_members.setIsUserAdmin(isAdmin);
        rva_members.notifyDataSetChanged();

        ll_awake = findViewById(R.id.ll_awake);
        awake_tv_counter = findViewById(R.id.awake_tv_counter);

        if (hasDatetimePassed) {
            ll_awake.setVisibility(View.VISIBLE);
            awake_tv_counter.setText(rva_members.getAwakeToSleepingRatio());
        }

        spawnChat();
    }

    // Inflates Views that make up the Chat component of this Activity.
    private void spawnChat() {
        et_messageContent = findViewById(R.id.et_messageContent);

        ib_messageSend = findViewById(R.id.ib_messageSend);
        ib_messageSend.setOnClickListener(v -> {
            Log.d(TAG, "ib_messageSend. clicked!");
            if (!et_messageContent.getText().toString().isEmpty()) {
                Message newMessage = new Message(userEmail, et_messageContent.getText().toString());
                et_messageContent.setText("");
                et_messageContent.clearFocus();
                // This method pushes the message to our FireBase server.
                fbO.pushMessageIntoChat(newMessage, thisEvent.getEventChatID(), new FBObjectManager.AsyncListenerString() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "[pushMessageIntoChat] onStart()");
                    }

                    @Override
                    public void onSuccess(String msgID) {
                        Log.d(TAG, "[pushMessageIntoChat] onSuccess()");
                        Log.d(TAG, "Pushed new message with id = " + msgID + " to chat!");
                    }

                    @Override
                    public void onFailed() {
                        Log.d(TAG, "[pushMessageIntoChat] onFailed()");
                    }
                });
            }
        });

        rv_chat = findViewById(R.id.rv_chat);
        rv_chat.setNestedScrollingEnabled(false);
        rv_chat.setHasFixedSize(false);

        rv_chat.setLayoutManager(new LinearLayoutManager( getApplicationContext(), RecyclerView.VERTICAL, false));

        rva_chat = new MessageRVAdapter(this, fbO.getThisUser().getEmail(), fbO.getCurrentEventChat());
        rv_chat.setAdapter(rva_chat);
        rva_chat.notifyDataSetChanged();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
    }

    // Inflates Views that make up the MemberList component.
    // This Dialog is spawned by clicking on the top right icon in the toolbar.
    private void spawnMemberListDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_member_list, null);

        builder .setView(layoutView);
        AlertDialog ad = builder.create();
        ad.show();

        int w = getResources().getDisplayMetrics().widthPixels;
        int h = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);

        ad.getWindow().setLayout(w, h);

        // This fab launches the AddMemberActivity
        fab_addMembers = layoutView.findViewById(R.id.fab_addMembers);
        fab_addMembers.setOnClickListener(v -> {
            Intent addMemberActIntent = new Intent(EventActivity.this, AddMemberActivity.class);

            Bundle extras = new Bundle();

            extras.putString("eventID", thisEvent.getEventID());
            extras.putString("defaultAlarmType", thisEvent.getDefaultAlarmTemplate().getType());
            extras.putLong("datetime", thisEvent.getDatetimeInMillis());
            extras.putInt("defaultAlarmComplexity", thisEvent.getDefaultAlarmTemplate().getComplexity());
            addMemberActIntent.putExtras(extras);

            startActivity(addMemberActIntent);
        });

        tv_noData = layoutView.findViewById(R.id.tv_noData);

        rv_members = layoutView.findViewById(R.id.rv_members);
        rv_members.setNestedScrollingEnabled(false);
        rv_members.setHasFixedSize(false);

        rv_members.setLayoutManager(new LinearLayoutManager( getApplicationContext(), RecyclerView.VERTICAL, false));

        if (rva_members != null)
            rv_members.setAdapter(rva_members);
        else
            Log.e(TAG, "rva_members == NULL!");
        if (rva_members.getItemCount() == 0) {
            rv_members.setVisibility(View.GONE);
            fab_addMembers.setVisibility(View.INVISIBLE);
            tv_noData.setVisibility(View.VISIBLE);
        } else {
            rv_members.setVisibility(View.VISIBLE);
            fab_addMembers.setVisibility(View.VISIBLE);
            tv_noData.setVisibility(View.INVISIBLE);
        }
    }

    // Inflates Views that make up the EventInfo component.
    // This Dialog is spawned by clicking on the top left icon in the toolbar.
    private void spawnEventInfoDialog() {

        // Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Custom View za moj Dialog
        View layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_event_info, null);

        builder .setView(layoutView);
        AlertDialog ad = builder.create();
        ad.show();

        int w = getResources().getDisplayMetrics().widthPixels;
        int h = (int) (getResources().getDisplayMetrics().heightPixels * 0.3);

        ad.getWindow().setLayout(w, h);

        ( (TextView) layoutView.findViewById(R.id.tv_eventname) ) .setText(thisEvent.getEventname());
        ( (TextView) layoutView.findViewById(R.id.tv_description) ) .setText(thisEvent.getDescription());
    }

    // Inflates Views that make up the EventTime component.
    // This Dialog is spawned by clicking on the middle icon in the toolbar.
    private void spawnEventTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_time_info, null);

        builder .setView(layoutView);
        AlertDialog ad = builder.create();
        ad.show();

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(thisEvent.getDatetimeInMillis());

        SimpleDateFormat sf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

        String datetimeReadable = sf.format(c.getTime());

        ( (TextView) layoutView.findViewById(R.id.tv_datetime) ) .setText(datetimeReadable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the Listeners that we set up.
        fbO.stopListeningToCurrentEventChat(thisEvent.getEventChatID(), new FBObjectManager.AsyncListenerString() {
            @Override
            public void onStart() {
                Log.d(TAG, "[stopListeningToCurrentGroupChat] onStart()");
            }

            @Override
            public void onSuccess(String id) {
                Log.d(TAG, "[stopListeningToCurrentGroupChat] onSuccess()");
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[stopListeningToCurrentGroupChat] onFailed()");
            }
        });
        fbO.stopListeningToCurrentEventMembershipsByEventID(thisEvent.getEventID(), new FBObjectManager.AsyncListenerString() {
            @Override
            public void onStart() {
                Log.d(TAG, "[stopListeningToCurrentGroupMemberships] onStart()");
            }

            @Override
            public void onSuccess(String id) {
                Log.d(TAG, "[stopListeningToCurrentGroupMemberships] onSuccess()");
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[stopListeningToCurrentGroupMemberships] onFailed()");
            }
        });
    }

    // We don't use this.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
        }

        return super.onOptionsItemSelected(item);
    }
}
