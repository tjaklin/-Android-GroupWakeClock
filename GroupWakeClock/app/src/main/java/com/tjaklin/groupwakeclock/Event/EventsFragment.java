package com.tjaklin.groupwakeclock.Event;

import android.content.Intent;
import android.graphics.Color;
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
import com.tjaklin.groupwakeclock.Alarm.AlarmRegistrationManager;
import com.tjaklin.groupwakeclock.Home.LoginActivity;
import com.tjaklin.groupwakeclock.Models.EventRVAdapter;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;

public class EventsFragment extends Fragment {

    private static final String TAG = EventsFragment.class.getSimpleName();

    // Holds an ArrayList of type Models.Event.
    private RecyclerView rv_events;
    private EventRVAdapter rva;

    // This TextView is displayed if our RecyclerView is empty. It says that "No data is found".
    private TextView tv_noData;

    // This is a FrameLayout that holds a ProgressBar. It works the same way as the FrameLayout
    // in LoginActivity. (explained there)
    private FrameLayout fl_progress;
    // Checks if we should show or hide the ProgressBar.
    private boolean hideProgress;

    // Used for managing the communication with our FireBase server.
    private FBObjectManager fbO;

    public EventsFragment() {
        // Show ProgressBar initially.
        hideProgress = false;

        fbO = FBObjectManager.getFbObjectManager(getActivity());

        // This method sets up a 'Listener' that listens for any changes in user's 'event membership'
        // status in our FireBase server. Upon certain changes it does some predetermined work.
        // (e.g. When a new Event is created and our user is added to that Event, a new Membership
        // entry will be added to our user's Memberships in FireBase. When that happens we
        // download relevant information about that Event. It will also download an Alarm that is
        // unique to this Event.)
        //
        // In short, this method goes through each of our user's event memberships and downloads
        // information about that event and also information about that membership.
        // (Event information is more general = Event name, date, time, default alarm type and complexity)
        // (Membership information is specific to each user / member = Alarm type and complexity for that
        // user, Has User woken up)
        // For every Membership and Event info, it will also download an Alarm that will be used
        // when the Event goes off.
        fbO.listenToMyOwnMembershipsByUserID(fbO.getThisUser().getUserID(), new FBObjectManager.AsyncListenerVoid() {
            @Override
            public void onStart() {
                Log.d(TAG, "[listenToMyOwnMembershipsByUserID] onStart()");
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "[listenToMyOwnMembershipsByUserID] onSuccess()");
                showHideRecyclerView();
                hideProgressBar();
            }

            @Override
            public void onFailed() {
                Log.e(TAG, "[listenToMyOwnMembershipsByUserID] onFailed()");
                hideProgressBar();
            }
        });
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
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        showHideRecyclerView();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spawnGUI(view);
    }

    private void spawnGUI(View view) {
        fl_progress = view.findViewById(R.id.fl_progress);
        if (hideProgress) {
            hideProgressBar();
        }

        tv_noData = view.findViewById(R.id.tv_noData);

        rv_events = view.findViewById(R.id.rv_groups);
        rv_events.setNestedScrollingEnabled(false);
        rv_events.setHasFixedSize(false);

        rv_events.setLayoutManager(new LinearLayoutManager( getActivity().getApplicationContext(), RecyclerView.VERTICAL, false));

        rva = new EventRVAdapter(getActivity(), true, fbO.getMyOwnEvents());
        rv_events.setAdapter(rva);
        showHideRecyclerView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_events, menu);

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
            case R.id.option_addEvent:
                Log.d(TAG, "+ Add Group clicked!");
                Intent launchGroupCreationActivityIntent = new Intent(getActivity().getApplicationContext(), EventCreationActivity.class);
                startActivity(launchGroupCreationActivityIntent);
                return true;

            case R.id.option_logout:
                Toast.makeText(getActivity(), "Logging out", Toast.LENGTH_SHORT).show();
                logOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[onDestroy()] !");

        // Remove the listener.
        fbO.stopListeningToMyOwnMembershipsByUserID(fbO.getThisUser().getUserID(), new FBObjectManager.AsyncListenerString() {
            @Override
            public void onStart() {
                Log.d(TAG, "[stopListeningToMyOwnMembershipsByUserID] onStart()");
            }

            @Override
            public void onSuccess(String id) {
                Log.d(TAG, "[stopListeningToMyOwnMembershipsByUserID] onSuccess()");
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[stopListeningToMyOwnMembershipsByUserID] onSuccess()");
            }
        });
    }

    private void logOut() {

        // First, disarm all armed AlarmClock alarms.
        AlarmRegistrationManager.getInstance(getActivity()).disarmAlarmClocksForUser(fbO.getThisUser().getEmail());

        // Then we signOut from FireBase and go back to LoginActivity
        FirebaseAuth.getInstance().signOut();
        Intent launchLoginActivityIntent = new Intent(getActivity(), LoginActivity.class);
        ContextCompat.startActivity(getActivity(), launchLoginActivityIntent, null);

        getActivity().finish();
    }

    private void hideProgressBar() {
        if (fl_progress != null) {
            fl_progress.setVisibility(View.GONE);
        }
        hideProgress = true;
    }

    private void showHideRecyclerView() {
        if (rva != null) {
            rva.notifyDataSetChanged();
            if (rva.getItemCount() == 0) {
                rv_events.setVisibility(View.GONE);
                tv_noData.setVisibility(View.VISIBLE);
            } else {
                rv_events.setVisibility(View.VISIBLE);
                tv_noData.setVisibility(View.INVISIBLE);
            }
        }
    }
}
