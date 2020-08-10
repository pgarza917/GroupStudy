package com.example.studygroup.eventFeed;

import android.content.Context;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroup.MainActivity;
import com.example.studygroup.R;
import com.example.studygroup.models.Event;
import com.example.studygroup.profile.ProfileFragment;

import org.parceler.Parcels;

import java.util.Calendar;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private Context mContext;
    private List<Event> mEventsList;
    private OnClickListener mClickListener;

    public EventsAdapter(Context mContext, List<Event> mEventsList, OnClickListener clickListener) {
        this.mContext = mContext;
        this.mEventsList = mEventsList;
        this.mClickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = mEventsList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return mEventsList.size();
    }

    // Clear all items from Recycler View
    public void clear() {
        mEventsList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items (events)
    public void addAll(List<Event> events) {
        mEventsList.addAll(events);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mDescriptionTextView;
        private ImageButton mShareImageButton;
        private ImageButton mLocationButton;
        private TextView mLocationTextView;
        private ImageButton mTimeImageButton;
        private TextView mTimeTextView;
        private TextView mSuggestedTextView;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            mTitleTextView = itemView.findViewById(R.id.titleTextView);
            mDescriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            mShareImageButton = itemView.findViewById(R.id.shareImageButton);
            mLocationButton = itemView.findViewById(R.id.locationPickerImageButton);
            mLocationTextView = itemView.findViewById(R.id.locationTextView);
            mTimeImageButton = itemView.findViewById(R.id.timeImageButton);
            mTimeTextView = itemView.findViewById(R.id.timeTextView);
            mSuggestedTextView = itemView.findViewById(R.id.suggestionTextView);


            itemView.setOnClickListener(this);
        }

        // Method to help bind the data retrieved and stored in an Event object to the views in
        // each item in the Recycler View
        public void bind(Event event) {
            if(event.getSuggestion()) {
                mSuggestedTextView.setVisibility(View.VISIBLE);
            } else {
                mSuggestedTextView.setVisibility(View.GONE);
            }

            mTitleTextView.setText(event.getTitle());
            mDescriptionTextView.setText(event.getDescription());

            Calendar eventDate = Calendar.getInstance();
            eventDate.setTime(event.getTime());
            String strEventDate = eventDate.get(Calendar.MONTH) + "/" + eventDate.get(Calendar.DAY_OF_MONTH) + "/" + eventDate.get(Calendar.YEAR);
            mTimeTextView.setText(strEventDate);

            mLocationTextView.setText(event.getLocationName());
        }

        @Override
        public void onClick(View view) {
            if(mClickListener != null) {
                final int position = getAdapterPosition();
                mClickListener.onClick(position);
            }
        }
    }

    public interface OnClickListener {
        void onClick(int position);
    }
}
