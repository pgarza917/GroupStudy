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
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();
    public static final int GOOGLE_SIGN_IN_REQUEST_CODE = 5456;

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
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());

        if(account != null) {
            goToMainActivity();
        }

        // Check to see if there is a cached user so we don't make the user have to sign
        // in again if they already have before. Persistence of user login across app
        // restarts
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null) {
            goToMainActivity();
        }

        mUsernameEditText = view.findViewById(R.id.usernameEditText);
        mPasswordEditText = view.findViewById(R.id.passwordEditText);
        mLoginButton = view.findViewById(R.id.loginButton);
        mRegisterButton = view.findViewById(R.id.goToRegisterButton);
        //mGoogleSignInButton = view.findViewById(R.id.googleSignInButton);

        // Set click listener to handle registration when 'Register Account' button is tapped
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                loginUser(username, password);
            }
        });

        /*mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchGoogleSignIn();
            }
        });*/

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
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Signed in successfully, show authenticated UI.
                goToMainActivity();
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