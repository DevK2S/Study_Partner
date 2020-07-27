package com.studypartner.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.studypartner.R;
import com.studypartner.models.ReminderItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private static final String TAG = "ReminderAdapter";

    public interface ReminderItemClickListener {
        void editButtonClicked(int position);
        void deleteButtonClicked(int position);
    }

    private Context context;
    private ReminderItemClickListener mReminderItemClickListener;
    private ArrayList<ReminderItem> mReminderList;

    public ReminderAdapter(Context context, ArrayList<ReminderItem> mReminderList, ReminderItemClickListener mReminderItemClickListener) {
        this.context = context;
        this.mReminderItemClickListener = mReminderItemClickListener;
        this.mReminderList = mReminderList;
    }


    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: starts");
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(itemView, mReminderItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderAdapter.ReminderViewHolder holder, int position) {
        final ReminderItem item = mReminderList.get(position);
        holder.title.setText(item.getTitle());
    }


    @Override
    public int getItemCount() {
        return mReminderList.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final String TAG = "ReminderAadpter";
        private ImageButton editButton, deleteButton;
        private TextView title, date;
        private ReminderItemClickListener ClickListener;

        public ReminderViewHolder(@NonNull View itemView, ReminderItemClickListener mReminderItemClickListener) {
            super(itemView);
            ClickListener = mReminderItemClickListener;
            editButton = itemView.findViewById(R.id.reminderItemEditButton);
            editButton.setOnClickListener(this);
            deleteButton = itemView.findViewById(R.id.reminderItemDeleteButton);
            deleteButton.setOnClickListener(this);
            title = itemView.findViewById(R.id.reminderItemTitle);
        }

        @Override
        public void onClick(View v) {

            if (v.getId() == editButton.getId()) {
                Log.d(TAG, "onClick: edit button clicked");
                ClickListener.editButtonClicked(getAdapterPosition());

            } else if (v.getId() == deleteButton.getId()) {
                Log.d(TAG, "onClick: delete button clicked");
                ClickListener.deleteButtonClicked(getAdapterPosition());

            }
        }
    }
}
