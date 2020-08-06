package com.example.studygroup.loginAndRegister;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.studygroup.BuildConfig;
import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.boltsinternal.Continuation;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();
    public static final int GOOGLE_SIGN_IN_REQUEST_CODE = 5456;
    public static final String OAUTH_CLIENT_ID = BuildConfig.CONSUMER_KEY;

    private GoogleSignInClient mGoogleSignInClient;

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private Button mRegisterButton;
    private SignInButton mGoogleSignInButton;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(OAUTH_CLIENT_ID)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);


        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        /*GoogleSignIn.silentSignIn()
                .addOnCompleteListener(
        this,
        new OnCompleteListener<GoogleSignInAccount>() {
          @Override
          public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
            handleSignInResult(task);
          }
        });*/

        // Check to see if there is a cached user so we don't make the user have to sign
        // in again if they already have before. Persistence of user login across app
        // restarts
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null) {
            goToMainActivity();
        }

        mUsernameEditText = view.findViewById(R.id.editDisplayNameEditText);
        mPasswordEditText = view.findViewById(R.id.passwordEditText);
        mLoginButton = view.findViewById(R.id.loginButton);
        mRegisterButton = view.findViewById(R.id.goToRegisterButton);
        mGoogleSignInButton = view.findViewById(R.id.googleSignInButton);

        // Set click listener to handle registration when 'Register Account' button is tapped
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                loginUser(username, password);
            }
        });

        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchGoogleSignIn();
            }
        });

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prepare the launch of a new Register Fragment
                Fragment fragment = new RegisterFragment();
                // Launch the register fragment
                ((LoginActivity) getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK) {
            Log.i(TAG, "Error on returning to login fragment from launched activity!");
            return;
        }
        if(requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                String userId = account.getId();
                // Pass the relevant info to Parse for third-party authentication with Google
                Map<String, String> authData = new HashMap<String, String>();
                authData.put("id", userId);
                authData.put("id_token", idToken);
                com.parse.boltsinternal.Task<ParseUser> parseUserTask = ParseUser.logInWithInBackground("google", authData);
                parseUserTask.continueWith(new Continuation<ParseUser, Void>() {
                    @Override
                    public Void then(com.parse.boltsinternal.Task<ParseUser> task) throws Exception {
                        if(task.isCancelled()) {
                            Log.i(TAG, "then: task cancelled");
                            return null;
                        }
                        if(task.isFaulted()) {
                            Log.i(TAG, "then: tag faulted");
                            return null;
                        }
                        final ParseUser user = task.getResult();
                        user.setEmail(account.getEmail());
                        user.setUsername(account.getId());
                        user.put("displayName", account.getDisplayName());
                        user.put("openEmail", account.getEmail());
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null) {
                                    Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                                    //mGoogleSignInClient.signOut();
                                    goToMainActivity();
                                } else {
                                    user.deleteInBackground();
                                    ParseUser.logOutInBackground();
                                }
                            }
                        });
                        return null;
                    }
                });

            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    // This method handles the logging in of a user with the user-provided credentials
    protected void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e == null) {
                    Log.i(TAG, "User successfully logged in!");
                    Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_LONG).show();

                    goToMainActivity();
                } else {
                    Log.e(TAG, "Error with user login", e);
                    Toast.makeText(getContext(), "Login Unsuccessful!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Handles the flow and logic for moving to the Main Activity from Login Activity
    protected void goToMainActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    // Method to initiate signing-in with a Google account
    private void launchGoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE);
    }
}