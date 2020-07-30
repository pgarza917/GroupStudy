package com.example.studygroup.messaging;

import android.animation.FloatEvaluator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.studygroup.R;
import com.example.studygroup.adapters.FirebaseUserAdapter;
import com.example.studygroup.adapters.UsersAdapter;
import com.example.studygroup.models.Message;
import com.example.studygroup.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageTabFragment extends Fragment {

    public static final String TAG = MessageTabFragment.class.getSimpleName();
    public static final String ARGS_NAME = "position";

    private RecyclerView mMessagesRecyclerView;

    private int mTabPosition;
    private List<User> mUsersList;
    private List<String> mUserIdList;
    private FirebaseUserAdapter mFirebaseUsersAdapter;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseReference;

    public MessageTabFragment() {
        // Required empty public constructor
    }


    public static MessageTabFragment newInstance(int position) {
        MessageTabFragment fragment = new MessageTabFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_NAME, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            mTabPosition = args.getInt(ARGS_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_tab, container, false);

        mMessagesRecyclerView = view.findViewById(R.id.messagesListRecyclerView);
        mMessagesRecyclerView.setHasFixedSize(true);
        mMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mUserIdList = new ArrayList<>();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserIdList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);

                    if(message.getSender().equals(mFirebaseUser.getUid())) {
                        mUserIdList.add(message.getReceiver());
                    }
                    if(message.getReceiver().equals(mFirebaseUser.getUid())) {
                        mUserIdList.add(message.getSender());
                    }
                }

                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error reading Firebase for chats: ", error.toException());
            }
        });

        return view;
    }

    private void readChats() {
        mUsersList = new ArrayList<>();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsersList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    for(String id : mUserIdList) {
                        if(user.getId().equals(id)) {
                            if(!mUsersList.isEmpty()) {
                                for(User user1 : mUsersList) {
                                    if(!user.getId().equals(user1.getId())) {
                                        mUsersList.add(user);
                                    }
                                }
                            } else {
                                mUsersList.add(user);
                            }
                        }
                    }
                }

                mFirebaseUsersAdapter = new FirebaseUserAdapter(getContext(), mUsersList);
                mMessagesRecyclerView.setAdapter(mFirebaseUsersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error reading users from Firebase: ", error.toException());
            }
        });
    }
}