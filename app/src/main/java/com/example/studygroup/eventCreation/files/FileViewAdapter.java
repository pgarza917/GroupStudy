package com.example.studygroup.eventCreation.files;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.models.FileExtended;

import java.util.List;

public class FileViewAdapter extends RecyclerView.Adapter<FileViewAdapter.ViewHolder> {

    public static final int WRITE_PERMISSION = 2390;
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
        private TextView mFileSizeTextView;
        private ImageButton mDownloadFileButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mFileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            mFileSizeTextView = itemView.findViewById(R.id.fileSizeTextView);
            mDownloadFileButton = itemView.findViewById(R.id.downloadFileImageButton);

        }

        public void bind(FileExtended file) {
            String filename = file.getFileName();
            mFileNameTextView.setText(filename);
            long fileSize = file.getFileSize();
            String fileSizeString;
            if(fileSize >= 0) {
                fileSizeString = getSize(fileSize);
            } else {
                fileSizeString = "Google Document";
            }
            mFileSizeTextView.setText(fileSizeString);

            mDownloadFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Would you like to download " + filename + "?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((MainActivity) mContext).checkDownloadPermissions(file);
                        }
                    });
                    builder.setNegativeButton("No", null);

                    Dialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
    }



    // This method is for formatting the size of files, which is in bytes, into a more
    // user-friendly and readable form, e.g. 503296 --> "503.00 KB"
    public static String getSize(long size) {
        long n = 1000;
        String s = "";
        double kb = size / n;
        double mb = kb / n;
        double gb = mb / n;
        double tb = gb / n;
        if(size < n) {
            s = size + " Bytes";
        } else if(size >= n && size < (n * n)) {
            s =  String.format("%.2f", kb) + " KB";
        } else if(size >= (n * n) && size < (n * n * n)) {
            s = String.format("%.2f", mb) + " MB";
        } else if(size >= (n * n * n) && size < (n * n * n * n)) {
            s = String.format("%.2f", gb) + " GB";
        } else if(size >= (n * n * n * n)) {
            s = String.format("%.2f", tb) + " TB";
        }
        return s;
    }

}
