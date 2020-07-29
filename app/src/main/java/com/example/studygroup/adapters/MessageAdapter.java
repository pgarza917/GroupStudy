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
import com.example.studygroup.models.Message;
import com.example.studygroup.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Message> mMessageList;
    private String mImageUrl;

    private FirebaseUser mFirebaseUser;

    public MessageAdapter(Context mContext, List<Message> mMessageList, String imageUrl) {
        this.mContext = mContext;
        this.mMessageList = mMessageList;
        this.mImageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = mMessageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mMessageTextView;
        private ImageView mProfilePictureImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mMessageTextView = itemView.findViewById(R.id.chatItemMessageTextView);
            mProfilePictureImageView = itemView.findViewById(R.id.chatItemImageView);
        }

        public void bind(Message message) {
            mMessageTextView.setText(message.getMessage());

            if(mProfilePictureImageView.getVisibility() != View.GONE) {
                Glide.with(mContext).load(mImageUrl).circleCrop().into(mProfilePictureImageView);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(mMessageList.get(position).getSender().equals(mFirebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
