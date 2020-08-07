package com.example.studygroup.messaging;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.search.UserSearchResultAdapter;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.User;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.common.collect.Iterables;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    public static final String TAG = MessagesFragment.class.getSimpleName();

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;

    private List<ParseUser> mUserSearchResultsList;
    private UserSearchResultAdapter mUserResultsAdapter;
    private ParseUser mSelectedUser;

    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.new_message_menu, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((MainActivity) getContext()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().show();

        ((MainActivity) getContext()).getSupportActionBar().setTitle("Messages");
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mViewPager = view.findViewById(R.id.viewPager);
        mTabLayout = view.findViewById(R.id.tabLayout);

        mViewPager.setAdapter(new MessagesViewPagerAdapter(getActivity()));
        new TabLayoutMediator(mTabLayout, mViewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if(position == 0) {
                    tab.setText("Direct Messages");
                } else {
                    tab.setText("Group Messages");
                }
            }
        }).attach();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_new_message) {
            showNewConversationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showNewConversationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Conversation Type");

        String[] types = {"Direct Message", "Group Message"};
        final int[] optionChosen = {0};
        builder.setSingleChoiceItems(types, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                optionChosen[0] = which;
            }
        });

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(optionChosen[0] == 0) {
                    showUserSelectDialog();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showUserSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Search and Select User");

        final View view = ((MainActivity) getContext()).getLayoutInflater().inflate(R.layout.search_user_dialog_layout, null);
        builder.setView(view);

        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(mSelectedUser != null) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    String email = mSelectedUser.getString("openEmail");
                    Query query = reference.orderByChild("email").equalTo(email);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DataSnapshot snapshot = Iterables.get(dataSnapshot.getChildren(), 0);

                            User user = snapshot.getValue(User.class);
                            String uid = user.getId();

                            Fragment fragment = new ConversationFragment();
                            Bundle data = new Bundle();
                            data.putString("userId", uid);
                            fragment.setArguments(data);
                            ((MainActivity) getContext()).getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.frameLayoutContainer, fragment, ConversationFragment.class.getSimpleName())
                                    .commit();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error querying selected user: ", error.toException());
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Please Search and Select a User to Message!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Dialog dialog = builder.create();
        dialog.show();

        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        SearchView searchView = view.findViewById(R.id.searchUserDialogSearchView);
        RecyclerView searchResultsRecyclerView = view.findViewById(R.id.dialogSearchResultsRecyclerView);
        RelativeLayout userItemRelativeLayout = view.findViewById(R.id.selectedUserRelativeLayout);

        userItemRelativeLayout.setVisibility(View.GONE);

        mUserSearchResultsList = new ArrayList<>();
        mUserResultsAdapter = new UserSearchResultAdapter(getContext(), mUserSearchResultsList, new UserSearchResultAdapter.ResultClickListener() {
            @Override
            public void onResultClicked(int position) {
                mSelectedUser = mUserSearchResultsList.get(position);
                userItemRelativeLayout.setVisibility(View.VISIBLE);

                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                ((TextView) view.findViewById(R.id.selectedUserNameTextView)).setText(mSelectedUser.getString("displayName"));
                ((TextView) view.findViewById(R.id.selectedUserEmailTextView)).setText(mSelectedUser.getString("openEmail"));
                ((CheckBox) view.findViewById(R.id.selectedUserCheckBox)).setChecked(true);
                ((CheckBox) view.findViewById(R.id.selectedUserCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(!b) {
                            userItemRelativeLayout.setVisibility(View.GONE);
                            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                    }
                });

                Glide.with(getContext()).load(mSelectedUser.getParseFile("profileImage").getUrl())
                        .circleCrop()
                        .into((ImageView) view.findViewById(R.id.selectedUserPictureImageView));
            }
        });

        searchResultsRecyclerView.setAdapter(mUserResultsAdapter);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration itemDecoration = new DividerItemDecoration(searchResultsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        searchResultsRecyclerView.addItemDecoration(itemDecoration);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!query.isEmpty()) {
                    searchUsers(query);
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(!query.isEmpty()) {
                    searchUsers(query);
                }
                return true;
            }
        });
    }

    private void searchUsers(String query) {
        ParseQuery<ParseUser> nameQuery = ParseUser.getQuery();
        nameQuery.whereContains("lowerDisplayName", query.toLowerCase());

        ParseQuery<ParseUser> emailQuery = ParseUser.getQuery();
        emailQuery.whereContains("email", query.toLowerCase());

        List<ParseQuery<ParseUser>> userQueries = new ArrayList<ParseQuery<ParseUser>>();
        userQueries.add(nameQuery);
        userQueries.add(emailQuery);

        ParseQuery<ParseUser> usersFullQuery = ParseQuery.or(userQueries);
        usersFullQuery.orderByDescending(Event.KEY_CREATED_AT);
        usersFullQuery.setLimit(4);

        usersFullQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error executing user search query: ", e);
                    return;
                }
                Log.i(TAG, "Success searching for users");
                mUserResultsAdapter.clear();
                mUserResultsAdapter.addAll(users);
                //mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

}