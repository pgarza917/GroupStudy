package com.example.studygroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.studygroup.eventCreation.CreateFragment;
import com.example.studygroup.eventCreation.files.FileViewAdapter;
import com.example.studygroup.eventCreation.CreateEventFragment;
import com.example.studygroup.eventFeed.FeedFragment;
import com.example.studygroup.groups.GroupListFragment;
import com.example.studygroup.messaging.MessagesFragment;
import com.example.studygroup.models.FileExtended;
import com.example.studygroup.profile.ProfileFragment;
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
import com.parse.ParseFile;
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
    private FileExtended mDownloadFile;
    private int mCurrentTabPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Account currentGoogleAccount = GoogleSignIn.getLastSignedInAccount(this).getAccount();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                int id = menuItem.getItemId();
                if(id == R.id.action_messages){
                    firebaseAuthentication();
                    return true;
                }
                setSupportActionBar(toolbar);
                getSupportActionBar().show();
                int newPosition;
                switch (id) {
                    case R.id.action_groups:
                        fragment = new GroupListFragment();
                        newPosition = 1;
                        break;
                    case R.id.action_create_event:
                        fragment = new CreateFragment();
                        newPosition = 2;
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        newPosition = 4;
                        break;
                    default:
                        fragment = new FeedFragment();
                        newPosition = 0;
                        break;
                }
                loadFragment(fragment, newPosition);
                return true;
            }
        });

        // Set default selection as home action icon
        mBottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == FileViewAdapter.WRITE_PERMISSION) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadFile(mDownloadFile);
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        FeedFragment feedFragment = new FeedFragment();
        loadFragment(feedFragment, 0);
        mBottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    private boolean loadFragment(Fragment fragment, int newPosition) {
        if(fragment != null) {
            if(newPosition == mCurrentTabPosition) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutContainer, fragment).commit();

            }
            if(mCurrentTabPosition > newPosition) {
                mFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.frameLayoutContainer, fragment).commit();

            }
            if(mCurrentTabPosition < newPosition) {
                mFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.frameLayoutContainer, fragment).commit();

            }
            mCurrentTabPosition = newPosition;
            return true;
        }

        return false;
    }

    public void checkDownloadPermissions(FileExtended file) {
        mDownloadFile = file;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadFile(file);
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, FileViewAdapter.WRITE_PERMISSION);
            }
        } else {
            downloadFile(file);
        }
    }

    public void downloadFile(FileExtended file) {
        ParseFile parseFile = file.getParseFile("fileData");
        Uri downloadUri = Uri.parse(parseFile.getUrl());
        String filename = file.getFileName();

        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        try {
            if(manager != null) {
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                        .setTitle(filename)
                        .setDescription("Downloading: " + filename)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                        .setMimeType(getMimeType(downloadUri));
                manager.enqueue(request);
            } else {
                // In case of a null download manager, the user will be able to open the link
                // in a browser
                Intent intent = new Intent(Intent.ACTION_VIEW, downloadUri);
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error with downloading file", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error downloading file: ", e);
        }
    }

    public String getMimeType(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }

    public void firebaseAuthentication() {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if(mFirebaseUser != null) {
            Log.i(TAG, "Silent Firebase auto-login successful");
            Fragment fragment = new MessagesFragment();
            loadFragment(fragment, 2);
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
                    Fragment fragment = new MessagesFragment();
                    loadFragment(fragment, 3);
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
                            hashMap.put("email", email);

                            mDatabaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Log.i(TAG, "Successfully set values on Firebase reference");
                                        Fragment fragment = new MessagesFragment();
                                        mFragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, fragment).commit();
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

    public static HashMap<String, Integer> createColorMap() {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("Physics", R.color.aqua);
        map.put("History", R.color.red);
        map.put("Psychology", R.color.blue);
        map.put("Economics", R.color.fuchsia);
        map.put("Geology", R.color.grey);
        map.put("Math", R.color.maroon);
        map.put("Political Science", R.color.olive);
        map.put("Literature", R.color.purple);
        map.put("Art", R.color.warmPink);
        map.put("Chemistry", R.color.seaGreen);
        map.put("Law", R.color.goldenRod);
        map.put("Biology", R.color.darkGreen);
        map.put("Language", R.color.darkGrey);
        map.put("Philosophy", R.color.orange);
        map.put("Statistics", R.color.navyBlue);
        map.put("Engineering", R.color.lushLava);
        map.put("Computer Science", R.color.limeGreen);

        return map;
    }

}