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
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

    public static final String TAG = RegisterFragment.class.getSimpleName();

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mEmailEditText;
    private EditText mBioEditText;
    private Button mRegisterAccountButton;
    private Button mCancelRegisterButton;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUsernameEditText = view.findViewById(R.id.registerUsernameEditText);
        mPasswordEditText = view.findViewById(R.id.registerPasswordEditText);
        mEmailEditText = view.findViewById(R.id.registerEmailEditText);
        mBioEditText = view.findViewById(R.id.registerBioEditText);
        mRegisterAccountButton = view.findViewById(R.id.registerButton);
        mCancelRegisterButton = view.findViewById(R.id.registerCancelButton);

        mRegisterAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String email = mEmailEditText.getText().toString();
                String bio = mBioEditText.getText().toString();

                if(username.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_LONG).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a password", Toast.LENGTH_LONG).show();
                    return;
                }
                if(email.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter an email", Toast.LENGTH_LONG).show();
                }

                registerUser(username, password, email, bio);
            }
        });

        mCancelRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Cancel Button clicked!");
                Fragment fragment = new LoginFragment();
                ((LoginActivity) getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
            }
        });

    }

    // Method to handle registering a new user in the Parse database
    private void registerUser(String username, String password, String email, String bio) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("bio", bio);
        user.put("displayName", username);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Toast.makeText(getContext(), "Registration Unsuccessful!", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getContext(), "Successful Registration!", Toast.LENGTH_LONG).show();
                // Navigate to Main Activity if user registered successfully
                goToMainActivity();
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