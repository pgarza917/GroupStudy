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
import com.example.studygroup.models.User;

import java.util.List;

public class FirebaseUserAdapter extends RecyclerView.Adapter<FirebaseUserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUserList;

    public FirebaseUserAdapter(Context mContext, List<User> mUserList) {
        this.mContext = mContext;
        this.mUserList = mUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUserList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

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

            mAddUserCheckBox.setVisibility(View.GONE);
            mEmailTextView.setVisibility(View.GONE);
        }

        public void bind(User user) {
            mDisplayNameTextView.setText(user.getUsername());

            Glide.with(mContext)
                    .load(user.getImageUrl())
                    .circleCrop()
                    .into(mProfilePictureImageView);
        }
    }
}
