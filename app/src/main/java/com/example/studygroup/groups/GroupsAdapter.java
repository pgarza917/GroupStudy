package com.example.studygroup.groups;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studygroup.R;
import com.example.studygroup.models.Group;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    public static final String TAG = GroupsAdapter.class.getSimpleName();

    private Context mContext;
    private List<Group> mGroupsList;
    private OnClickListener mClickListener;

    public GroupsAdapter(Context mContext, List<Group> mGroupsList, OnClickListener listener) {
        this.mContext = mContext;
        this.mGroupsList = mGroupsList;
        this.mClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = mGroupsList.get(position);
        holder.bind(group);
    }

    public void setIsCurrentMember(List<Group> groups){
        for(Group group : groups) {
            group.setCurrentUserMember(true);
        }
    }

    @Override
    public int getItemCount() {
        return mGroupsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mGroupPictureImageView;
        private TextView mGroupNameTextView;
        private TextView mNumberOfUsersTextView;
        private Button mJoinButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mGroupPictureImageView = itemView.findViewById(R.id.groupPictureImageView);
            mGroupNameTextView = itemView.findViewById(R.id.groupNameTextView);
            mNumberOfUsersTextView = itemView.findViewById(R.id.numberOfUsersTextView);
            mJoinButton = itemView.findViewById(R.id.joinGroupButton);

            itemView.setOnClickListener(this);
        }

        public void bind(Group group) {
            mGroupNameTextView.setText(group.getGroupName());

            int numberOfUsers = group.getNumberOfUsers();
            String numberOfUsersStr = numberOfUsers + ((numberOfUsers == 1) ? " User" : " Users");
            mNumberOfUsersTextView.setText(numberOfUsersStr);

            ParseFile groupPicture = group.getGroupImage();
            if(groupPicture != null) {
                Glide.with(mContext).load(groupPicture.getUrl()).circleCrop().into(mGroupPictureImageView);
            }

            if(group.isCurrentUserMember) {
                mJoinButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            if(mClickListener != null) {
                mClickListener.onClick(getAdapterPosition());
            }
        }
    }

    public interface OnClickListener {
        void onClick(int position);
    }
}
