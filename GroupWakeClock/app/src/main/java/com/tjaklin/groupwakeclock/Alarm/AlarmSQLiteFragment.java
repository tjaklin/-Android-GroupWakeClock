package com.tjaklin.groupwakeclock.Alarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjaklin.groupwakeclock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.tjaklin.groupwakeclock.Home.LoginActivity;
import com.tjaklin.groupwakeclock.Models.AlarmSQLite;
import com.tjaklin.groupwakeclock.Models.AlarmSQLiteRVAdapter;
import com.tjaklin.groupwakeclock.Util.CustomSQLiteOpenHelper;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;

import java.util.ArrayList;

public class AlarmSQLiteFragment extends Fragment {

    // This Fragment holds information about user's upcoming alarms.
    // That information is displayed in a RecyclerView where a single item
    // holds information about Alarm's Date, Time, Name of the Event that it's tied to.
    // It also presents information about alarm's Type and Complexity.

    // Alarm can be of type "Math" or "Image". Type determines the type of Question user will
    // have to answer in order to turn off the Alarm.
    // (math = solve a problem, image = recognize the object)
    // Alarm's Complexity can be between 0 and 5, where 5 is the most complex. Complexity
    // helps split different tasks into categories by difficulty.

    private static final String TAG = AlarmSQLiteFragment.class.getSimpleName();

    // In this RecyclerView
    private RecyclerView rv_alarms;
    private RecyclerView.Adapter rva;
    private TextView tv_noData;

    // Once again, we use a FrameLayout which hosts a ProgressBar to communicate to the user that
    // a job is currently being executed (that job is getting data from FireBase).
    private FrameLayout fl_progress;
    private boolean hideProgress;

    private ArrayList<AlarmSQLite> upcomingAlarms;
    // CustomSQLiteOpenHelper is a class that manages all calls to SQLite.
    private CustomSQLiteOpenHelper db;
    private FBObjectManager fbO;

    public AlarmSQLiteFragment() {
        upcomingAlarms = new ArrayList<>();
        fbO = FBObjectManager.getFbObjectManager(getActivity());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttachFragment!");

        if (getActivity() == null) {
            Log.e(TAG, "ACTIVITY NULL!");
        }

        db = new CustomSQLiteOpenHelper(getActivity());

        // Reads data from SQLite.
        readFromSQLite();

        if (upcomingAlarms.isEmpty()) {
            Log.e(TAG, "upcomingAlarms is empty!");
        } else {
            Log.e(TAG, "upcomingAlarms is not empty!");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null)
            savedInstanceState = new Bundle();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_alarms, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (db != null) {
            readFromSQLite();
            showHideRecyclerView();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spawnGUI(view);
    }

    private void spawnGUI(View view) {
        fl_progress = view.findViewById(R.id.fl_progress);
        if (hideProgress) {
            hideProgressBar(true);
        }

        tv_noData = view.findViewById(R.id.tv_noData);

        rv_alarms = view.findViewById(R.id.rv_alarms);
        rv_alarms.setNestedScrollingEnabled(false);
        rv_alarms.setHasFixedSize(false);

        rv_alarms.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), RecyclerView.VERTICAL, false));

        rva = new AlarmSQLiteRVAdapter(upcomingAlarms);
        rv_alarms.setAdapter(rva);
        showHideRecyclerView();

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_alarms, menu);

        // Manipulate logout item's appearance
        MenuItem item = menu.getItem(1);
        SpannableString s = new SpannableString(item.getTitle());
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        s.setSpan(new RelativeSizeSpan(0.9f),0,s.length(),0);
        item.setTitle(s);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.option_refreshSQL:
                Toast.makeText(getActivity(), "Looking for changes in the database.", Toast.LENGTH_SHORT).show();
                readFromSQLite();
                return true;

            case R.id.option_logout:
                Toast.makeText(getActivity(), "Logging out.", Toast.LENGTH_SHORT).show();
                logOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void readFromSQLite() {
        upcomingAlarms.clear();
        hideProgressBar(false);

        // Read all alarms for our user
        if (db.readAlarms(fbO.getThisUser().getEmail()) != null) {
            Log.d(TAG, "[readFromSQLite] Found data in SQLite");

            // Save read alarms into an ArrayList
            ArrayList<AlarmSQLite> tempList = new ArrayList<>(db.readAlarms(fbO.getThisUser().getEmail()));

            if (!tempList.isEmpty()) {

                // Go through the list and delete old alarms
                for (int i = 0; i < tempList.size(); i++) {

                    if (isAlarmTooOld(tempList.get(i).getDatetime())) {
                        Log.d(TAG, "[readFromSQLite] I'm deleting it from SQLite.");
                        db.deleteAlarm(tempList.get(i).getDatetime(), fbO.getThisUser().getEmail());

                    } else {
                        // If alarm is upcoming - save it to our List instead. Alarms from this List
                        // will be shown in our RecyclerView.
                        Log.d(TAG, "[readFromSQLite] I'm leaving it as is.");
                        upcomingAlarms.add(tempList.get(i));
                    }
                }

                // Job is done, hide the ProgressBar
                hideProgressBar(true);

            } else {
                Log.e(TAG, "[readFromSQLite] No data found in SQLite");
                hideProgressBar(true);

            }
        } else {
            Log.e(TAG, "[readFromSQLite] SQLite returned NULL");
            hideProgressBar(true);

        }

        showHideRecyclerView();
    }

    // Checks if some alarm from the SQLite database is too old. We'd like to delete all
    // old alarms.
    private boolean isAlarmTooOld(long datetime) {
        // Alarm is considered to be old if it's 'Trigger Time' had passed 12 hours ago.
        // If the Alarm was triggered less than 12 hours ago, it's not too old yet.
        // (I can't remember why we wouldn't just delete all alarms that have already passed.
        //  Possibly because of a bug.)
        Calendar past12hours = Calendar.getInstance();
        past12hours.add(Calendar.HOUR_OF_DAY, -12);

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(datetime);

        if (alarmTime.before(past12hours)) {
            Log.d(TAG, "[hasAlarmAlreadyLongPassed] alarm is really old. I'm deleting it from SQLite.");
            return true;
        }

        return false;
    }

    // Logs the user out of FireBase.
    private void logOut() {

        // First we have to disarm any active alarms.
        AlarmRegistrationManager.getInstance(getActivity()).disarmAlarmClocksForUser(fbO.getThisUser().getEmail());

        FirebaseAuth.getInstance().signOut();
        Intent launchLoginActivityIntent = new Intent(getActivity(), LoginActivity.class);
        ContextCompat.startActivity(getActivity(), launchLoginActivityIntent, null);

        getActivity().finish();
    }

    private void hideProgressBar(boolean flag) {
        if (fl_progress != null) {
            if (flag)
                fl_progress.setVisibility(View.GONE);
            else
                fl_progress.setVisibility(View.VISIBLE);
        }
        hideProgress = true;
    }

    private void showHideRecyclerView() {
        if (rva != null) {
            rva.notifyDataSetChanged();
            if (rva.getItemCount() == 0) {
                rv_alarms.setVisibility(View.GONE);
                tv_noData.setVisibility(View.VISIBLE);
            } else {
                rv_alarms.setVisibility(View.VISIBLE);
                tv_noData.setVisibility(View.INVISIBLE);
            }
        }
    }
}
