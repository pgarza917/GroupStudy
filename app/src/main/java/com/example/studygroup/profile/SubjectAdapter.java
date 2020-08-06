package com.example.studygroup.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroup.R;
import com.example.studygroup.models.Subject;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {
    public static final String TAG = SubjectAdapter.class.getSimpleName();

    private List<Subject> mSubjectsList;
    private Context mContext;
    private HashMap<String, Integer> mColorMap;

    public SubjectAdapter(Context mContext, List<Subject> mSubjectsList) {
        this.mSubjectsList = mSubjectsList;
        this.mContext = mContext;
        createColorMap();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_subject, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = mSubjectsList.get(position);
        holder.bind(subject);
    }

    @Override
    public int getItemCount() {
        return mSubjectsList.size();
    }

    private void createColorMap() {
        mColorMap = new HashMap<String, Integer>();
        mColorMap.put("Physics", R.color.aqua);
        mColorMap.put("History", R.color.red);
        mColorMap.put("Psychology", R.color.blue);
        mColorMap.put("Economics", R.color.fuchsia);
        mColorMap.put("Geography", R.color.grey);
        mColorMap.put("Math", R.color.maroon);
        mColorMap.put("Political Science", R.color.olive);
        mColorMap.put("Literature", R.color.purple);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mSubjectNameTextView;
        private String mSubjectName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mSubjectNameTextView = itemView.findViewById(R.id.subjectNameTextView);

        }

        public void bind(Subject subject) {
            mSubjectName = subject.getSubjectName();
            mSubjectNameTextView.setText(mSubjectName);

            Integer color = mColorMap.get(mSubjectName);
            Drawable background = ContextCompat.getDrawable(mContext, R.drawable.subject_tag);
            background.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(mContext, color), PorterDuff.Mode.SRC_IN));
            mSubjectNameTextView.setBackground(background);

            if(subject.isEditMode()) {
                mSubjectNameTextView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Would you like to remove " + mSubjectName + " from your subject tags?") ;

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int position = getAdapterPosition();
                    Subject subject = mSubjectsList.get(position);
                    mSubjectsList.remove(position);

                    ParseRelation<Subject> relation = ParseUser.getCurrentUser().getRelation("subjectInterests");
                    relation.remove(subject);
                    ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null) {
                                Log.e(TAG, "Error saving edited user-subject relation to Parse: ", e);
                                return;
                            }
                            Toast.makeText(mContext, "Subject Tag Removed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            builder.setNegativeButton("No", null);

            Dialog dialog = builder.create();
            dialog.show();
        }
    }
}
