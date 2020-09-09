package com.tjaklin.groupwakeclock.Event;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.tjaklin.groupwakeclock.R;
import com.tjaklin.groupwakeclock.Models.AlarmTemplate;
import com.tjaklin.groupwakeclock.Models.Event;
import com.tjaklin.groupwakeclock.Models.Membership;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;

public class EventCreationActivity extends AppCompatActivity {

    // This Activity is used for event creation. It consists of several EditText views
    // that we use to get information from the user.

    // An Event is defined by it's Name, Description, Date, Time, and a default AlarmTemplate.
    //
    // What is an AlarmTemplate? Well, each Participant of some Event will have their own Alarm
    // that is defined by it's Type and Complexity. Alarms need to have a Type and Complexity
    // because they're not just ordinary Alarms. Each Alarm, upon triggering, will present it's
    // user with a Question they will have to solve in order to turn off the Alarm.
    //
    // Alarm's Type and Complexity combined form an AlarmTemplate.
    // Every Event will have it's own default AlarmTemplate. We use this default AlarmTemplate
    // to automatically assign Alarms to newly added Participants.

    private static final String TAG = EventCreationActivity.class.getSimpleName();

    // Possible alarm types and complexities
    private static final String[] ALARM_TYPES = {"math", "image"};
    private static final Integer[] ALARM_COMPLEXITIES = {0, 1, 2, 3, 4, 5};

    // Widgets we use to get input from user.
    private EditText et_groupname, et_description, et_date, et_time;
    private Spinner typeSpinner, complexitySpinner;

    // Needed for communication with FireBase.
    private FBObjectManager fbO;

    private Event thisEvent;
    private String adminID;

    private Calendar groupCalendar;
    private AlarmTemplate defaultAlarmTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        fbO = FBObjectManager.getFbObjectManager(this);
        groupCalendar = Calendar.getInstance();

        // Initially we set this to 'Math', 0.
        defaultAlarmTemplate = new AlarmTemplate(ALARM_TYPES[0], ALARM_COMPLEXITIES[0]);

        // The user creating the Event will become the Event's admin. Admin can alter
        // the AlarmTemplate of Event's participants.
        adminID = fbO.getThisUser().getUserID();

        // Inflate Widgets
        spawnGUI();
    }

    private void spawnGUI() {

        et_groupname = findViewById(R.id.et_groupname);
        et_description = findViewById(R.id.et_description);

        et_date = findViewById(R.id.et_date);
        et_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spawnDatePicker();
            }
        });

        et_time = findViewById(R.id.et_time);
        et_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spawnTimePicker();
            }
        });

        findViewById(R.id.fab_createGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkIfInputLegal()) {
                    insertGroupIntoFB();
                } else {
                    Log.d(TAG, "fab_createGroup.Onclick(): checkIfInputLegal == FALSE");
                }
            }
        });

        // This manages EditText focus
        findViewById(R.id.cl_main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                et_groupname.clearFocus();
                et_description.clearFocus();
                return false;
            }
        });

        typeSpinner = findViewById(R.id.spnr_type);
        complexitySpinner = findViewById(R.id.spnr_complexity);
        spawnSpinners();
    }

    // Spinners are used to set the default Alarm Type and Complexity
    private void spawnSpinners() {
        // type
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ALARM_TYPES);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "typeSpinner.onItemSelected!");

                defaultAlarmTemplate.setType((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "typeSpinner.onNothingSelected!");
            }
        });
        // complexity
        ArrayAdapter<Integer> complexityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ALARM_COMPLEXITIES);
        complexityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complexitySpinner.setAdapter(complexityAdapter);
        complexitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "complexitySpinner.onItemSelected!");

                defaultAlarmTemplate.setComplexity(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "complexitySpinner.onNothingSelected!");
            }
        });
    }

    private boolean checkIfInputLegal() {
        String groupname = et_groupname.getText().toString();
        String description = et_description.getText().toString();

        Calendar currentDateTime = Calendar.getInstance();
        // We check if the selected Date and Time have already passed.
        if (groupCalendar.after(currentDateTime)) {
            Log.d(TAG, "Entered DateTime is OK");
            if (!groupname.isEmpty() && !description.isEmpty()) {
                Log.d(TAG, "Entered groupname is OK");
                Log.d(TAG, "Entered description is OK");
                if ( defaultAlarmTemplate != null) {
                    thisEvent = new Event(groupname, description, groupCalendar.getTimeInMillis(), defaultAlarmTemplate);
                    thisEvent.setAdminUserID(adminID);
                    Log.d(TAG, "Group object creation is OK");
                } else {
                    Log.d(TAG, "type and / or complexity are NULL");
                    return false;
                }
            } else {
                Log.d(TAG, "Entered groupname or description is NOT OK");
                return false;
            }
        } else {
            Log.d(TAG, "Entered DateTime is NOT OK");
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // This clears the focus.
        View view = getCurrentFocus();
        if (view != null) {
            view.clearFocus();
        }
    }

    /**
     * QoL metode
     */

    private void spawnDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(EventCreationActivity.this,
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                groupCalendar.set(Calendar.YEAR, year);
                groupCalendar.set(Calendar.MONTH, month);
                groupCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Months begin at 0.
                month++;

                String chosenDate = dayOfMonth + "." + month + "." + year;
                et_date.setText(chosenDate);

                Toast.makeText(getApplicationContext(),
                        "Date succesfully picked: " + chosenDate,
                        Toast.LENGTH_SHORT).show();

            }
        },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH) );

        datePickerDialog.show();
    }

    private void spawnTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(EventCreationActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int min) {
                groupCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                groupCalendar.set(Calendar.MINUTE, min);
                groupCalendar.set(Calendar.SECOND, 0);

                String chosenTime = hourOfDay + ":" + min;
                et_time.setText(chosenTime);

                Toast.makeText(getApplicationContext(),
                        "Time succesfully picked: "+ chosenTime,
                        Toast.LENGTH_SHORT).show();
            }

        },
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                true);

        timePickerDialog.show();
    }

    /**
     * FB metode
     */
    private void insertGroupIntoFB() {
        // First we create a chat for the Event in our FireBase server.

        String msg = "initial / placeholder groupChat msg!";
        fbO.uploadNewChat(msg, new FBObjectManager.AsyncListenerString() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart");
            }

            @Override
            public void onSuccess(String chatID) {
                Log.d(TAG, "onSuccess");
                thisEvent.setEventChatID(chatID);

                // Then we upload the Group to the FireBase server.
                fbO.uploadEvent(thisEvent, new FBObjectManager.AsyncListenerString() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "onStart");
                    }

                    @Override
                    public void onSuccess(String groupID) {
                        Log.d(TAG, "onSuccess");
                        thisEvent.setEventID(groupID);
                        final Membership newMembership = new Membership(groupID, adminID, groupCalendar.getTimeInMillis(), defaultAlarmTemplate);

                        // Next, we add our user (admin) to the Event.
                        fbO.uploadMembership(newMembership, new FBObjectManager.AsyncListenerString() {
                            @Override
                            public void onStart() {
                                Log.d(TAG, "onStart");
                            }

                            @Override
                            public void onSuccess(String membershipID) {
                                Log.d(TAG, "onSuccess");
                                newMembership.setMembershipID(membershipID);
                                thisEvent.setCurrentUserMembershipID(membershipID);

                                // We add this Event and our user's Membership to FBObjectManager's
                                // variables.
                                fbO.addToMyOwnMemberships(newMembership);
                                fbO.addToMyOwnEvents(thisEvent);

                                // Finally, we add the created Event to Intent's extras and launch the EventActivity.
                                Intent launchGroupActivityIntent = new Intent(EventCreationActivity.this, EventActivity.class);

                                Bundle extras = new Bundle();
                                extras.putParcelable("event", thisEvent);
                                launchGroupActivityIntent.putExtras(extras);

                                startActivity(launchGroupActivityIntent);
                                finish();
                            }

                            @Override
                            public void onFailed() {
                                Log.d(TAG, "onFailed");
                            }
                        });
                    }

                    @Override
                    public void onFailed() {
                        Log.d(TAG, "onFailed");
                    }

                });
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "onFailed");
            }
        });
    }
}
