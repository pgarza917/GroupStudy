package com.example.studygroup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroup.R;
import com.example.studygroup.models.Event;
import com.example.studygroup.models.FileExtended;
import com.example.studygroup.models.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class EventPostsAdapter extends RecyclerView.Adapter<EventPostsAdapter.ViewHolder> {

    private List<Post> mPosts;
    private Context mContext;

    public EventPostsAdapter(List<Post> mPosts, Context mContext) {
        this.mPosts = mPosts;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> posts) {
        mPosts.addAll(posts);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mUsernameTextView;
        private TextView mDateTextView;
        private TextView mContentTextView;
        private TextView mEditedEventTextView;

        private FileViewAdapter mFilesAdapter;
        private List<FileExtended> mFilesList;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mUsernameTextView = itemView.findViewById(R.id.postUserNameTextView);
            mDateTextView = itemView.findViewById(R.id.postDateTextView);
            mContentTextView = itemView.findViewById(R.id.postContentTextView);
            RecyclerView mFilesRecyclerView = itemView.findViewById(R.id.postFilesRecyclerView);
            mEditedEventTextView = itemView.findViewById(R.id.eventEditedTextView);

            mFilesList = new ArrayList<>();
            mFilesAdapter = new FileViewAdapter(mContext, mFilesList);
            mFilesRecyclerView.setAdapter(mFilesAdapter);
            mFilesRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

            DividerItemDecoration divider = new DividerItemDecoration(mFilesRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
            mFilesRecyclerView.addItemDecoration(divider);

        }

        public void bind(Post post) {
            mUsernameTextView.setText(post.getCreator().getString("displayName"));
            mContentTextView.setText(post.getText());
            mDateTextView.setText(getDateText(post.getEvent()));

            if(post.getEdited()) {
                mEditedEventTextView.setVisibility(View.VISIBLE);
            } else {
                mEditedEventTextView.setVisibility(View.GONE);
            }

            List<FileExtended> filesToAdd = post.getFiles();
            if(filesToAdd.size() > 0) {
                mFilesList.addAll(filesToAdd);
                mFilesAdapter.notifyDataSetChanged();
            }
        }
    }

    public String getDateText(Event event) {
        String timeStamp = event.getTime().toString();
        StringTokenizer tokenizer = new StringTokenizer(timeStamp);

        String weekday = tokenizer.nextToken();
        String month = tokenizer.nextToken();
        String day = tokenizer.nextToken();

        String timeInDay = tokenizer.nextToken();
        String timezone = tokenizer.nextToken();
        String year = tokenizer.nextToken();

        return day + " " + month + " " + year;
    }

}
