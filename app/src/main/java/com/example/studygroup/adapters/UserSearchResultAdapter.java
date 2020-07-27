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
import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class UserSearchResultAdapter extends RecyclerView.Adapter<UserSearchResultAdapter.ViewHolder> {

    private Context mContext;
    private List<ParseUser> mUserList;
    private ResultClickListener listener;

    public UserSearchResultAdapter(Context mContext, List<ParseUser> mUserList, ResultClickListener listener) {
        this.mContext = mContext;
        this.mUserList = mUserList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
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
            itemView.setBackgroundColor(mContext.getResources().getColor(R.color.white));

            itemView.setOnClickListener(this);

            mAddUserCheckBox.setVisibility(View.GONE);
        }

        public void bind(ParseUser user) {
            mDisplayNameTextView.setText(user.getString("displayName"));
            mEmailTextView.setText(user.getEmail());

            ParseFile profilePicture = user.getParseFile("profileImage");
            Glide.with(mContext).load(profilePicture.getUrl()).circleCrop().into(mProfilePictureImageView);
        }

        @Override
        public void onClick(View view) {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_green));
            listener.onResultClicked(getAdapterPosition());
        }
    }

    public interface ResultClickListener {
        void onResultClicked(int position);
    }
}
