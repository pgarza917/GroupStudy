package com.example.studygroup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check to see if there is a cached user so we don't make the user have to sign
        // in again if they already have before. Persistence of user login across app
        // restarts
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null) {
            goToMainActivity();
        }

        mUsernameEditText = findViewById(R.id.usernameEditText);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        mLoginButton = findViewById(R.id.loginButton);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                loginUser(username, password);
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
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_LONG).show();

                    goToMainActivity();
                } else {
                    Log.e(TAG, "Error with user login", e);
                    Toast.makeText(LoginActivity.this, "Login Unsuccessful!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Handles the flow and logic for moving to the Main Activity from Login Activity
    protected void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}