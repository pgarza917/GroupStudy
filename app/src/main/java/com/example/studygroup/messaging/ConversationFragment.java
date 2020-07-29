package com.example.studygroup.messaging;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studygroup.R;
import com.example.studygroup.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationFragment extends Fragment {

    public static final String TAG = ConversationFragment.class.getSimpleName();

    private ImageView mToolbarImageView;
    private TextView mToolbarTextView;
    private ImageButton mSendMessageImageButton;
    private EditText mComposeMessageEditText;
    private RecyclerView mConversationMessagesRecyclerView;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseReference;

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar_messages);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mToolbarImageView = view.findViewById(R.id.toolbarProfilePictureImageView);
        mToolbarTextView = view.findViewById(R.id.toolbarUserNameTextView);
        mSendMessageImageButton = view.findViewById(R.id.messageSendButton);
        mComposeMessageEditText = view.findViewById(R.id.composeMessageEditText);
        mConversationMessagesRecyclerView = view.findViewById(R.id.conversationMessagesRecyclerView);

        Bundle data = getArguments();
        String userId = data.getString("userId");

        mSendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mComposeMessageEditText.getText().toString();
                if(!message.isEmpty()) {
                    sendMessage(mFirebaseUser.getUid(), userId, message);
                    mComposeMessageEditText.setText("");
                }
            }
        });

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                mToolbarTextView.setText(user.getUsername());
                if(user.getImageUrl().equals("default")) {
                    mToolbarImageView.setVisibility(View.INVISIBLE);
                } else {
                    Glide.with(getContext()).load(user.getImageUrl()).into(mToolbarImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error with database value event listener ", error.toException());
            }
        });

    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("Chats").push().setValue(hashMap);
    }
}