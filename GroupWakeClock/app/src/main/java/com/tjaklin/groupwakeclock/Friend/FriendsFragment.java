package com.tjaklin.groupwakeclock.Friend;

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
import com.tjaklin.groupwakeclock.Models.UserRVAdapter;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;

public class FriendsFragment extends Fragment {

    private static final String TAG = FriendsFragment.class.getSimpleName();

    // Holds an ArrayList of type Models.User.
    private RecyclerView rv_friends;
    private RecyclerView.Adapter rva;
    // This TextView is displayed if our RecyclerView is empty. It says that "No data is found".
    private TextView tv_noData;

    // This is a FrameLayout that holds a ProgressBar. It works the same way as the FrameLayout
    // in LoginActivity. (explained there)
    private FrameLayout fl_progress;
    // Checks if we should show or hide the ProgressBar.
    private boolean hideProgress;

    // Used for managing the communication with our FireBase server.
    private FBObjectManager fbO;

    public FriendsFragment() {
        // Show ProgressBar initially.
        hideProgress = false;

        fbO = FBObjectManager.getFbObjectManager(getActivity());

        // This method sets up a 'Listener' that listens for any changes in user's friendships
        // status in our FireBase server. Upon certain changes it does some predetermined work.
        // (e.g. When a new friendship is added, download relevant information about user's
        // new friend.)
        //
        // In short, this method downloads a list of friend so we can display them in a RecyclerView.
        fbO.listenToMyOwnFriendships(fbO.getThisUser().getUserID(), new FBObjectManager.AsyncListenerVoid() {
            @Override
            public void onStart() {
                Log.d(TAG, "[listenToMyOwnFriendships] onStart()");
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "[listenToMyOwnFriendships] onSuccess()");

                // Once some data is succesfully downloaded, display it in our RecyclerView.
                // Hide tv_noData which states that user has no friends, and display our RecyclerView.
                // If a change occurs that deletes all of our user's friendships - we will hide the
                // now empty RecyclerView and show tv_noData again.
                showHideRecyclerView();
                hideProgressBar();
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[listenToMyOwnFriendships] onFailed()");
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
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        showHideRecyclerView();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inflates all Widgets
        spawnGUI(view);
    }

    private void spawnGUI(View view) {
        fl_progress = view.findViewById(R.id.fl_progress);

        if (hideProgress) {
            hideProgressBar();
        }

        tv_noData = view.findViewById(R.id.tv_noData);

        rv_friends = view.findViewById(R.id.rv_friends);
        rv_friends.setNestedScrollingEnabled(false);
        rv_friends.setHasFixedSize(false);

        rv_friends.setLayoutManager(new LinearLayoutManager( getActivity().getApplicationContext(), RecyclerView.VERTICAL, false));

        rva = new UserRVAdapter(getActivity(), fbO.getMyOwnFriends());
        rv_friends.setAdapter(rva);
        showHideRecyclerView();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_friends, menu);

        // Manipulate 'logout' item's appearance
        MenuItem item = menu.getItem(1);
        SpannableString s = new SpannableString(item.getTitle());
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        s.setSpan(new RelativeSizeSpan(0.9f),0,s.length(),0);
        item.setTitle(s);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.option_addFriend:
                Log.d(TAG, "+ Add Friend clicked!");
                Intent launchAddFriendAcIntent = new Intent(getActivity(), AddFriendActivity.class);
                startActivity(launchAddFriendAcIntent);
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
        fbO.stopListeningToMyOwnFriendships(fbO.getThisUser().getUserID(), new FBObjectManager.AsyncListenerString() {
            @Override
            public void onStart() {
                Log.d(TAG, "[stopListeningToMyOwnFriendships] onStart()");
            }

            @Override
            public void onSuccess(String id) {
                Log.d(TAG, "[stopListeningToMyOwnFriendships] onSuccess()");
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "[stopListeningToMyOwnFriendships] onSuccess()");
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
                rv_friends.setVisibility(View.GONE);
                tv_noData.setVisibility(View.VISIBLE);
            } else {
                rv_friends.setVisibility(View.VISIBLE);
                tv_noData.setVisibility(View.INVISIBLE);
            }
        }
    }

}
