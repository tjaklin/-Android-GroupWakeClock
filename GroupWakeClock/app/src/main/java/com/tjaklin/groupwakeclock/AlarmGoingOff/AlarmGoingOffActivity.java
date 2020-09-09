package com.tjaklin.groupwakeclock.AlarmGoingOff;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjaklin.groupwakeclock.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.tjaklin.groupwakeclock.Models.AlarmSQLite;
import com.tjaklin.groupwakeclock.Models.AlarmTemplate;
import com.tjaklin.groupwakeclock.Models.Membership;
import com.tjaklin.groupwakeclock.Models.Message;
import com.tjaklin.groupwakeclock.Util.CustomSQLiteOpenHelper;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;
import com.tjaklin.groupwakeclock.Util.HelperMethods;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class AlarmGoingOffActivity extends AppCompatActivity {

    // This Activity is launched when user clicks on AlarmGoingOffNotification.
    // This Activity displays the Alarm's question to the user, and it's up to the user
    // to correctly answer that question by typing the answer into the EditText

    // (Almost) All widgets used here are created dynamically from setupGUI() or setupDefaultGUI()
    // methods.

    private static final String TAG = AlarmGoingOffActivity.class.getSimpleName();
    private static final String ALARM_ID = "alarmID";
    private static final String EVENT_CHAT_ID = "eventChatID";
    private static final String EVENTNAME = "eventname";
    private static final String USER_EMAIL = "userEmail";
    private static final String MEMBERSHIP_ID = "membershipID";

    // We need to communicate with SQLite to read the relevant Alarm info.
    private CustomSQLiteOpenHelper db;
    // That's the Alarm object that we read from SQLite.
    private AlarmSQLite alarm;
    // These variables store the information from Extras we received via the Intent.
    private String eventname;
    private String eventChatID;
    private String userEmail;
    private String membershipID;

    // These variables hold references to dynamically created Widgets.
    private LinearLayout ll_main;
    private EditText et_answer;
    private TextWatcher et_answer_tw;
    private ImageView iv_answerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_going_off);
        Log.d(TAG, "Alarm's going off!");

        Bundle extras = getIntent().getExtras();

        // Check if any extras are missing.
        if (extras != null) {
            if (extras.containsKey(ALARM_ID)) {
                long alarmID = extras.getLong(ALARM_ID, -1);
                if (alarmID != -1) {
                    Log.e(TAG, "alarmID = " + alarmID);

                    if (extras.containsKey(USER_EMAIL)) {
                        userEmail = extras.getString(USER_EMAIL);
                        if (userEmail != null) {
                            Log.e(TAG, "userEmail = " + userEmail);

                            if (extras.containsKey(EVENTNAME)) {
                                eventname = extras.getString(EVENTNAME);
                                if (eventname != null) {
                                    Log.e(TAG, "eventname = " + eventname);


                                    if (extras.containsKey(EVENT_CHAT_ID)) {
                                        eventChatID = extras.getString(EVENT_CHAT_ID, null);
                                        if (eventChatID != null) {
                                            Log.e(TAG, "eventChatID = " + eventChatID);

                                            if (extras.containsKey(MEMBERSHIP_ID)) {
                                                membershipID = extras.getString(MEMBERSHIP_ID);
                                                if (membershipID != null) {
                                                    Log.e(TAG, "membershipID = " + membershipID);
                                                    db = new CustomSQLiteOpenHelper(this);
                                                    alarm = db.readAlarm(alarmID, userEmail);
                                                    if (alarm != null) {
                                                        Log.d(TAG, "Succesfully retrieved an Alarm object from sqlite!");
                                                        setupGUI();
                                                    } else {
                                                        Log.d(TAG, "Sqlite returned a NULL Alarm object!");
                                                        setupDefaultGUI();
                                                    }
                                                }
                                            } else {
                                                Log.d(TAG, "Intent.extras.MEMBERSHIP_ID NOT RECEIVED!");
                                                setupDefaultGUI();
                                            }
                                        }
                                    } else {
                                        Log.d(TAG, "Intent.extras.EVENT_CHAT_ID NOT RECEIVED!");
                                        setupDefaultGUI();
                                    }
                                }
                            } else {
                                Log.d(TAG, "Intent.extras.EVENTNAME NOT RECEIVED!");
                                setupDefaultGUI();
                            }
                        }
                    } else {
                        Log.d(TAG, "Intent.extras.USER_EMAIL NOT RECEIVED!");
                        setupDefaultGUI();
                    }
                }
            } else {
                Log.d(TAG, "Intent.extras.ALARM_ID NOT RECEIVED!");
                setupDefaultGUI();
            }

        } else {
            Log.d(TAG, "Intent.extras == NULL");
            setupDefaultGUI();
        }
    }

    // If we successfully received the information we needed to find our Alarm in SQLite
    // we will use this method to inflate our Widgets.
    private void setupGUI() {

        et_answer_tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged()!");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged()!");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged()!");
                String answer = s.toString();
                if (!answer.isEmpty()) {
                    iv_answerStatus.setVisibility(View.VISIBLE);
                    submitAnswer(s.toString());
                } else {
                    iv_answerStatus.setVisibility(View.INVISIBLE);
                }
            }
        };

        ll_main = findViewById(R.id.ll_main);

        LinearLayout.LayoutParams lp_MW = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams lp_MM = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout.LayoutParams lp_WW = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp_WW.gravity = Gravity.CENTER;

        ll_main.setLayoutParams(lp_MM);

        String title = eventname + "\n" + alarm.getDatetimeFormatted();
        TextView tv_title = new TextView(this);
        tv_title.setLayoutParams(lp_MW);
        tv_title.setGravity(Gravity.CENTER);
        tv_title.setText(title);

        ImageView iv_question = new ImageView(this);
        iv_question.setLayoutParams(lp_MW);
        iv_question.setBackgroundColor(Color.parseColor("#603D5AFE"));
        iv_question.setImageBitmap(byteArrayToBitmap(alarm.getQuestion()));

        et_answer = new EditText(this);
        et_answer.setGravity(Gravity.CENTER);
        et_answer.setLayoutParams(lp_WW);
        et_answer.setHint("Enter your answer");
        et_answer.addTextChangedListener(et_answer_tw);

        iv_answerStatus = new ImageView(this);
        iv_answerStatus.setLayoutParams(lp_WW);
        iv_answerStatus.setPadding(50, 50, 50, 50);

        ll_main.addView(tv_title);
        ll_main.addView(iv_question);
        ll_main.addView(et_answer);
        ll_main.addView(iv_answerStatus);
    }

    // If we failed to retrieve some of the needed information from extras, we will inflate our
    // Widgets using this method as we don't know exactly which Alarm to use.
    //
    // This is just a basic replacement for our, otherwise more complicated, Alarm. The user
    // just has to click a button and Alarm turns off.
    private void setupDefaultGUI() {

        // Programatically Populate and inflate a LinearLayout. Add an OnClickListener to a Button
        // that stops the service!
        ll_main = findViewById(R.id.ll_main);

        TextView textView = new TextView(this);
        textView.setLayoutParams( new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        textView.setFocusable(false);
        textView.setBackgroundColor(Color.GREEN);
        textView.setText("This is the default Alarm! Just click the button to stop the Alarm.");
        textView.setOnClickListener(v -> stopService());

        ll_main.addView(textView);
    }

    // Checks if answer is correct
    private boolean isAnswerCorrect(String possibleAnswer) {
        String[] legalAnswers = alarm.getAnswerAsStringArray();
        if (legalAnswers == null) {
            Log.d(TAG, "[isAnswerCorrect] legalAnswers is NULL!");
            return false;
        }

        for (String legalAnswer : legalAnswers) {
            if (legalAnswer.equals(possibleAnswer))
                return true;
        }

        return false;
    }

    // Takes care of stopping the Alarm, notifies FireBase that the user is awake, tries to
    // notify the Event members/participants that the user is awake.
    private void submitAnswer(String answer) {
        Log.d(TAG, "Answer submitted!");

        if (isAnswerCorrect(answer)) {
            Log.d(TAG, "Answer is correct!");

            // This updates the 'AnswerStatus' icon that we use to communicate to the user whether
            // their input is correct or not.
            changeAnswerStatus(true);
            // Stops AlarmGoingOffService which implicitly stops the phone vibration.
            stopService();

            // Another way of communicating that the answer is correct.
            Snackbar.make(ll_main, "Answer Correct!", BaseTransientBottomBar.LENGTH_LONG).show();

            // Disable the EditText Widget because we don't want the user to keep on typing.
            if (et_answer != null) {
                et_answer.removeTextChangedListener(et_answer_tw);
                et_answer.clearFocus();
                et_answer.setEnabled(false);
            }

            // If there is an internet connection, we would like to notify our FireBase server
            // that the user is awake. Also, we would like to notify the event participants that
            // this user is now awake.
            //
            // If there is no connection, we will spawn a Dialog that asks the user to connect
            // to the internet so we could take care of notifying our server and event participants.
            if (HelperMethods.isNetworkAvailable(this)) {
                publishUserAwake();
            } else {
                spawnNoConnectionDialog().show();
            }

        } else {
            Log.d(TAG, "Answer is incorrect!");
            changeAnswerStatus(false);
        }
    }

    // This manipulates the Answer Status icon which is used to communicate to the user whether the
    // answer submitted is correct or false. If it's correct we'll display a tick/ check mark.
    // If it's incorrect we'll display an X sign.
    private void changeAnswerStatus(boolean isCorrect) {
        if (iv_answerStatus == null) {
            Log.d(TAG, "iv_answerStatus == NULL!");
            return;
        }

        if (!isCorrect) {
            iv_answerStatus.setImageResource(R.drawable.ic_status_incorrect_24dp);
            iv_answerStatus.setBackgroundColor(Color.RED);
        } else {
            iv_answerStatus.setImageResource(R.drawable.ic_status_correct_24dp);
            iv_answerStatus.setBackgroundColor(Color.GREEN);
        }
    }

    // Stops the Service
    private void stopService() {
        Log.d(TAG, "Stopping Service!");
        Intent stopVibrating = new Intent(getApplicationContext(), AlarmGoingOffService.class);
        stopService(stopVibrating);
    }

    // Notify the FireBase server and the Event's chat that the user is awake.
    private void publishUserAwake() {
        Log.e(TAG, "[publishUserAwakeStatusToGroup] publishing Msg to Event!");

        FBObjectManager fbO = FBObjectManager.getFbObjectManager(AlarmGoingOffActivity.this);

        String msgContent = "User " + userEmail + " is awake!";
        Message userIsAwakeMsg = new Message("system", msgContent);

        // This method sends a Message to our Event which notifies the members that our user
        // is now awake.
        fbO.pushMessageIntoChat(userIsAwakeMsg, eventChatID, new FBObjectManager.AsyncListenerString() {
            @Override
            public void onStart() {
                Log.d(TAG, "[publishResponseTime] onStart()");
            }

            @Override
            public void onSuccess(String id) {
                Log.d(TAG, "[publishResponseTime] onSuccess()");
                Snackbar.make(ll_main, "Your group members now know that you're awake!", BaseTransientBottomBar.LENGTH_LONG).show();
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[publishResponseTime] onFailed()");
            }
        });

        // This method modifies the corresponding Membership entry in the FireBase server to indicate
        // that our user is awake.
        Membership thisMembership = new Membership(membershipID, new AlarmTemplate(), true);
        fbO.updateMembership(thisMembership, new FBObjectManager.AsyncListenerString() {
            @Override
            public void onStart() {
                Log.d(TAG, "[publishUserAwake] onStart()!");
            }

            @Override
            public void onSuccess(String id) {
                Log.d(TAG, "[publishUserAwake] onSuccess()!");
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[publishUserAwake] onFailed()!");
            }
        });
    }

    /**
     * Bitmap <-> Byte[] Conversion!
     */

    private byte[] bitmapToByteArray(Bitmap img) {

        if (img == null) {
            Log.d(TAG, "bitmapToByteArray().img == NULL!");
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, out);

        // Tu se nadam da mi nije NULL!
        return out.toByteArray();
    }

    // This converts the Alarm's variable 'Question' of type byte[] to a Bitmap
    // (Question is just an image that contains the question. We need to convert it to Bitmap
    // to display it in a ImageView Widget).
    private Bitmap byteArrayToBitmap(byte[] img ) {

        if (img == null) {
            Log.d(TAG, "byteArrayToBitmap().img == NULL!");
            return null;
        }

        ByteArrayInputStream imageStream = new ByteArrayInputStream(img);
        return BitmapFactory.decodeStream(imageStream);
    }

    // This methods spawns a Dialog that basically bullies our user to turn on the
    // internet connection so that we could call publishUserAwake().
    private AlertDialog spawnNoConnectionDialog() {

        String dialogTitle = "Not Connected to Internet!";
        String dialogMsg = "Your answer is CORRECT,\n" +
                "but we want to let your fellow group members know that you're awake.\n" +
                "Please establish a connection to the internet and click on 'RETRY'" +
                " so that we can notify other event participants that you're awake.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder .setTitle(dialogTitle)
                .setMessage(dialogMsg)
                .setPositiveButton("RETRY", (dialog, which) -> {
                    if (HelperMethods.isNetworkAvailable(this)) {
                        publishUserAwake();
                    } else {
                        Toast.makeText(AlarmGoingOffActivity.this,
                                "Not connected to the internet!", Toast.LENGTH_SHORT).show();
                        // spawn another?
                        spawnNoConnectionDialog().show();
                    }
                });

        // Create the AlertDialog object and return it
        AlertDialog result = builder.create();

        result.setCanceledOnTouchOutside(true);
        result.setOnCancelListener(dialog -> spawnNoConnectionDialog().show());

        return result;
    }
}
