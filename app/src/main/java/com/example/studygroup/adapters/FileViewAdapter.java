package com.example.studygroup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroup.R;
import com.example.studygroup.models.FileExtended;

import java.util.List;

public class FileViewAdapter extends RecyclerView.Adapter<FileViewAdapter.ViewHolder> {

    private Context mContext;
    private List<FileExtended> mFilesList;

    public FileViewAdapter(Context mContext, List<FileExtended> mFilesList) {
        this.mContext = mContext;
        this.mFilesList = mFilesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileExtended file = mFilesList.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return mFilesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mFileNameTextView;
        private TextView mFileTimestampTextView;
        private TextView mFileSizeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mFileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            mFileTimestampTextView = itemView.findViewById(R.id.fileTimestampTextView);
            mFileSizeTextView = itemView.findViewById(R.id.fileSizeTextView);

        }

        public void bind(FileExtended file) {
            mFileNameTextView.setText(file.getFileName());

            String createdAt = file.getCreatedAt().toString();

            long fileSize = file.getFileSize();


        }
    }

}
