package com.example.studygroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.accounts.Account;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.studygroup.eventCreation.CreateEventFragment;
import com.example.studygroup.eventFeed.FeedFragment;
import com.example.studygroup.messaging.MessagesFragment;
import com.example.studygroup.profile.ProfileFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.Parse;
import com.parse.ParseUser;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int ERROR_DIALOG_REQUEST = 8001;

    final FragmentManager mFragmentManager = getSupportFragmentManager();
    private BottomNavigationView mBottomNavigationView;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Account currentGoogleAccount = GoogleSignIn.getLastSignedInAccount(this).getAccount();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_create_event:
                        fragment = new CreateEventFragment();
                        break;
                    case R.id.action_messages:
                        firebaseAuthentication();
                        fragment = new MessagesFragment();
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        break;
                    default:
                        fragment = new FeedFragment();
                        break;
                }
                mFragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
                return true;
            }
        });

        // Set default selection as home action icon
        mBottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    private void firebaseAuthentication() {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if(mFirebaseUser != null) {
            Log.i(TAG, "Silent Firebase auto-login successful");
            return;
        }

        ParseUser currentUser = ParseUser.getCurrentUser();
        String username = currentUser.getString("displayName");
        String email = currentUser.getEmail();
        String password = currentUser.getObjectId();
        String imageUrl = currentUser.getParseFile("profileImage").getUrl();

        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Log.i(TAG, "Successful login to Firebase");
                } else {
                    firebaseRegister(username, email, password, imageUrl);
                }
            }
        });

    }

    private void firebaseRegister(final String username, String email, String password, String imageUrl) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            String userId = user.getUid();

                            mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userId);
                            hashMap.put("username", username);
                            hashMap.put("imageUrl", ((imageUrl == null) ? "default" : imageUrl));

                            mDatabaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Log.i(TAG, "Successfully set values on Firebase reference");
                                    } else {
                                        Log.e(TAG, "Error setting values on Firebase reference: ", task.getException());
                                    }
                                }
                            });
                        } else {
                            Log.e(TAG, "Error registering new account in Firebase: ", task.getException());
                        }
                    }
                });
    }

    // Helper method for determining if the current device is able to work with Google Play services
    // so that we do not run into errors when using the Google Map API
    public boolean isGoogleServicesOk() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play services are working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "A Google Play services error occurred, but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests!", Toast.LENGTH_LONG).show();
        }

        return false;
    }

}