package com.example.studygroup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studygroup.R;
import com.example.studygroup.models.Event;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private Context mContext;
    private List<ParseUser> mUserList;
    private final CheckBoxListener listener;

    public UsersAdapter(Context mContext, List<ParseUser> mUserList, CheckBoxListener listener) {
        this.mContext = mContext;
        this.mUserList = mUserList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser user = mUserList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    // Clear all items from Recycler View
    public void clear() {
        mUserList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items (users)
    public void addAll(List<ParseUser> users) {
        mUserList.addAll(users);
        notifyDataSetChanged();
    }

    public void remove(ParseUser user) {
        mUserList.remove(user);
        notifyDataSetChanged();
    }

    public void addUnique(List<ParseUser> users) {
        for(int i = 0; i < users.size(); i++) {
            String currId = users.get(i).getObjectId();
            int j = 0;
            while(j < mUserList.size() && !currId.equals(mUserList.get(j).getObjectId())) {
                j++;
            }
            if(j >= mUserList.size()) {
                mUserList.add(users.get(i));
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mProfilePictureImageView;
        private TextView mDisplayNameTextView;
        private TextView mEmailTextView;
        private CheckBox mAddUserCheckBox;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mProfilePictureImageView = itemView.findViewById(R.id.userItemProfilePictureImageView);
            mDisplayNameTextView = itemView.findViewById(R.id.userDisplayNameTextView);
            mEmailTextView = itemView.findViewById(R.id.userItemEmailTextView);
            mAddUserCheckBox = itemView.findViewById(R.id.addItemUserCheckBox);

            mAddUserCheckBox.setOnClickListener(this);
        }

        public void bind(ParseUser user) {
            String displayName = null;
            try {
                displayName = user.fetchIfNeeded().getString("displayName");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mDisplayNameTextView.setText(displayName);
            String email = user.getString("openEmail");
            mEmailTextView.setText(email);
            mAddUserCheckBox.setChecked(true);

            ParseFile profilePicture = user.getParseFile("profileImage");
            Glide.with(mContext).load(profilePicture.getUrl()).circleCrop().into(mProfilePictureImageView);
        }

        @Override
        public void onClick(View view) {

            int id = view.getId();
            int position = getAdapterPosition();

            if(id == R.id.addItemUserCheckBox) {
                if(mAddUserCheckBox.isChecked()) {
                    listener.onBoxChecked(position);
                } else {
                    listener.onBoxUnchecked(position);
                }
            }
        }
    }

    public interface CheckBoxListener {
        void onBoxChecked(int position);
        void onBoxUnchecked(int position);
    }
}
