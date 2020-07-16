package com.example.studygroup.loginAndRegister;

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

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();


    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private Button mRegisterButton;

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

        // Set click listener to handle registration when 'Register Account' button is tapped
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                loginUser(username, password);
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
}