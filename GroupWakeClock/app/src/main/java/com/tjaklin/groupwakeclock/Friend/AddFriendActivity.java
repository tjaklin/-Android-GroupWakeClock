package com.tjaklin.groupwakeclock.Friend;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tjaklin.groupwakeclock.R;
import com.tjaklin.groupwakeclock.Models.Friendship;
import com.tjaklin.groupwakeclock.Models.User;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;

public class AddFriendActivity extends AppCompatActivity {

    private static final String TAG = AddFriendActivity.class.getSimpleName();

    // Used to communicate with the FireBase server.
    private FBObjectManager fbO;

    // We use et_email to get input from user. That's where users write the email of their friend.
    private EditText et_email;
    // Tries to add the user using et_email's email as our, currently logged in, user's friend.
    private Button btn_addFriend;

    // That's the email entered in et_email.
    private String friendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        fbO = FBObjectManager.getFbObjectManager(this);
        // Inflate Widgets.
        spawnGUI();
    }

    private void spawnGUI() {
        et_email = findViewById(R.id.et_email);

        btn_addFriend = findViewById(R.id.btn_addFriend);
        btn_addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btn_addFriend clicked!");
                if (isInputLegal()) {

                    // Proceed only if we're not trying to add self as friend AND if
                    // users and friends already.
                    if ( (!fbO.getThisUser().getEmail().equals(friendEmail)) && (!areFriendsAlready()) )
                        // Download potential friend's info.
                        fbO.downloadUserByEmail(friendEmail, new FBObjectManager.AsyncListenerUser() {
                            @Override
                            public void onStart() {
                                Log.d(TAG, "onStart()!");
                            }

                            @Override
                            public void onSuccess(User user) {
                                Log.d(TAG, "User / Friend succesfully retrieved!");
                                // I don't think this line is needed.
                                // Add potential friend to list of friends inside FBObjectManager
//                                fbO.addToMyOwnFriends(user);

                                // Upload that Friendship to FireBase server.
                                Friendship newFriendship = new Friendship(fbO.getThisUser().getUserID(), user.getUserID());
                                fbO.uploadFriendship(newFriendship, new FBObjectManager.AsyncListenerFriendship() {
                                    @Override
                                    public void onStart() {
                                        Log.d(TAG, "onStart()!");
                                    }

                                    @Override
                                    public void onSuccess(Friendship friendship) {
                                        Log.d(TAG, "Friendship succesfully added!");
                                        // Is this line needed?
//                                        fbO.addToMyOwnFriendships(friendship);

                                        Toast.makeText(AddFriendActivity.this, "Friend " + friendEmail + " succesfully added!", Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onFailed() {
                                        Log.e(TAG, "onFailed!");
                                    }
                                });
                            }

                            @Override
                            public void onFailed() {
                                Log.e(TAG, "onFailed!");
                                Toast.makeText(AddFriendActivity.this, "Failed to find user!", Toast.LENGTH_LONG).show();
                            }
                        });
                }
            }
        });
    }

    private boolean isInputLegal() {
        // Check et_email's content.
        // Trims all whitespace from our string.
        friendEmail = et_email.getText().toString().trim();

        if (friendEmail != null && !friendEmail.isEmpty()) {
            String[] emailSplit = friendEmail.split("@");
            if (emailSplit.length == 2) {
                Log.d(TAG, "emailSplit.length == 2");
                return true;
            }
            else {
                Log.d(TAG, "emailSplit.length != 2");
            }
        } else {
            Log.d(TAG, "email == null or .isEmpty !");
        }

        Toast.makeText(AddFriendActivity.this, "Illegal input!", Toast.LENGTH_LONG).show();
        return false;
    }

    private boolean areFriendsAlready() {

        for (User friend : fbO.getMyOwnFriends()) {
            if (friend.getEmail().equals(friendEmail)) {
                Log.d(TAG, "areFriendsAlready == True !");
                Toast.makeText(this, "You are already friends!", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        Log.d(TAG, "areFriendsAlready == False !");
        return false;
    }
}
