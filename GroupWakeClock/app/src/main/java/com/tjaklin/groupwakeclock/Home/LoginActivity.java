package com.tjaklin.groupwakeclock.Home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tjaklin.groupwakeclock.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tjaklin.groupwakeclock.Util.FBObjectManager;
import com.tjaklin.groupwakeclock.Util.HelperMethods;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText et_email, et_password;
    private FrameLayout fl_progress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Skips this Activity if user has a FireBase Token.
        tryProceeding(false);

        et_email = findViewById( R.id.et_email );
        et_password = findViewById( R.id.et_password );
        // This is the Login button
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEmailFormat()) {
                    Toast.makeText(LoginActivity.this, "Invalid Email format!", Toast.LENGTH_SHORT).show();
                }
                else if (!checkPasswordFormat()) {
                    Toast.makeText(LoginActivity.this, "Password lenght must be between 6 and 20!", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser();
                }
            }
        });
        // This is the Register button
        findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEmailFormat()) {
                    Toast.makeText(LoginActivity.this, "Invalid Email format!", Toast.LENGTH_SHORT).show();
                }
                else if (!checkPasswordFormat()) {
                    Toast.makeText(LoginActivity.this, "Password lenght must be between 6 and 20!", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser();
                }
            }
        });

        // This is a FrameLayout that contains a ProgresBar. We will show and hide the ProgressBar
        // by showing/hiding it's FrameLayout.
        fl_progress = findViewById(R.id.fl_progress);
    }

    // Our email format is invalid if it doesn't contain a '@' character.
    // We're not checking anything past that.
    private boolean checkEmailFormat() {
        // Trim() gets rid of all the whitespace
        String email = et_email.getText().toString().trim();

        String[] parts = email.split("@");

        // There has to be at least one char preceding and succeeding the '@' char.
        if (parts.length < 2)
            return false;

        else return (parts[0].length() != 0) && (parts[1].length() != 0);

    }

    // Our password has to be between 6 and 20 chars long.
    private boolean checkPasswordFormat() {
        String password = et_password.getText().toString();
        return (password.length() > 5) && (password.length() < 21);
    }

    private void registerUser() {

        if (!HelperMethods.isNetworkAvailable(this)) {
            Log.d(TAG, "No internet connection!");
            Toast.makeText(getApplicationContext(), "No internet connection.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBar();

        // Try to register our user with FireBase.
        mAuth.createUserWithEmailAndPassword(
                et_email.getText().toString(), et_password.getText().toString() )
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User successfully registered !
                            Log.d(TAG, "[registration] Succeeded!");

                            // Unfortunately we have to take an extra step here to insert our user's
                            // information to the FireBase's RealtimeDatabase.
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            String userID = currentUser.getUid();
                            String userEmail = currentUser.getEmail();

                            FBObjectManager.getFbObjectManager(getApplication())
                                    .uploadUser(userID, userEmail, new FBObjectManager.AsyncListenerVoid() {
                                @Override
                                public void onStart() {
                                    Log.d(TAG, "[uploadUser] onStart()!");
                                }

                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "[uploadUser] onSuccess()!");
                                    hideProgressBar();

                                    // Proceed with App's flow
                                    tryProceeding(true);
                                }

                                @Override
                                public void onFailed() {
                                    Log.d(TAG, "[uploadUser] onFailed()!");
                                    hideProgressBar();
                                    Toast.makeText(getApplicationContext(),
                                            "Registration failed - Experiencing a server issue.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            // Failed to write our user's info to FireBase's Authentication service.
                            Log.w(TAG, "[registration] Failed!", task.getException());
                            // todo: Process that exception? No . . .

                            hideProgressBar();

                            Toast.makeText(getApplicationContext(),
                                    "Registration failed - Email already in use.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loginUser() {

        if (!HelperMethods.isNetworkAvailable(this)) {
            Log.d(TAG, "No internet connection!");
            Toast.makeText(getApplicationContext(), "No internet connection.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBar();

        // Try logging in.
        mAuth.signInWithEmailAndPassword(
                et_email.getText().toString(), et_password.getText().toString() )
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressBar();

                        if (task.isSuccessful()) {
                            // User successfully logged in !
                            Log.d(TAG, "[login] Succeeded!");

                            // Proceed with App's flow.
                            tryProceeding(false);
                        } else {
                            // Failed to find our user's info in FireBase's Authentication service.
                            // This means that the entered email is not in use.
                            Log.w(TAG, "[login] Failed!", task.getException());

                            Toast.makeText(getApplicationContext(),
                                    "Login failed - Email not in use.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // If there's already a FireBase token that's tied to our user, we can just skip this Activity
    // and go straight into HomeActivity.
    // (Token is present only if our user had previously explicitly logged in.)
    // Otherwise we would have to ask our user to register or login from within this Activity's
    // registerUser() or loginUser() methods.
    private void tryProceeding(boolean userHasJustRegistered) {
        // Use the token to get user's info
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if( currentUser != null ) {

            String userID = currentUser.getUid();
            String userEmail = currentUser.getEmail();

            Intent launchHomeActivityIntent = new Intent(getApplicationContext(), HomeActivity.class);

            Bundle extras = new Bundle();
            extras.putString("userID", userID);
            extras.putString("userEmail", userEmail);
            // If our user has just registered, we assume it's their first time using this App.
            // We want to spawn our tutorial for them, in order for them to get familiar with how
            // this App works. The tutorial is skippable and can be launched on demand at any time
            // through TaskBar options.
            if (userHasJustRegistered) {
                extras.putBoolean("startTutorial", true);
            } else {
                extras.putBoolean("startTutorial", false);
            }
            launchHomeActivityIntent.putExtras(extras);

            startActivity(launchHomeActivityIntent);
            finish();
        }
    }

    // We will show a ProgressBar when user clicks on register / login and hide
    // it once the App is done processing the request.
    public void hideProgressBar() {
        if (fl_progress != null) {
            fl_progress.setVisibility(View.GONE);
        }
    }

    public void showProgressBar() {
        if (fl_progress != null) {
            fl_progress.setVisibility(View.VISIBLE);
        }
    }

}