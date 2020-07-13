package com.example.studygroup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroup.R;
import com.example.studygroup.models.Event;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private Context mContext;
    private List<Event> mEventsList;

    public EventsAdapter(Context mContext, List<Event> mEventsList) {
        this.mContext = mContext;
        this.mEventsList = mEventsList;
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

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(Event event) {

        }
    }
}
