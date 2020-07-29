package com.example.studygroup.messaging;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.studygroup.R;
import com.example.studygroup.models.User;

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
        return inflater.inflate(R.layout.fragment_message_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesRecyclerView = view.findViewById(R.id.messagesListRecyclerView);

        if(mTabPosition == 1) {

        } else {

        }
    }
}