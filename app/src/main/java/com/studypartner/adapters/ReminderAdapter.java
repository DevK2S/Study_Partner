package com.studypartner.adapters;

import android.content.Context;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.studypartner.R;
import com.studypartner.models.ReminderItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private static final String TAG = "ReminderAdapter";

    public interface ReminderItemClickListener {
        void onClick(int position);
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
        return new ReminderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderAdapter.ReminderViewHolder holder, int position) {
        final ReminderItem item = mReminderList.get(position);
        holder.title.setText(item.getTitle());
        holder.date.setText(item.getDate());
        holder.time.setText(item.getTime());
        applyClickEvents(holder);
    }

    private void applyClickEvents(final ReminderViewHolder holder) {
        holder.reminderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReminderItemClickListener.onClick(holder.getAdapterPosition());
            }
        });

    }


    @Override
    public int getItemCount() {
        return mReminderList.size();
    }

    public class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final String TAG = "ReminderAadpter";
        private TextView title, date, time;
        CardView reminderLayout;
        private ReminderItemClickListener ClickListener;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reminderItemTitle);
            date = itemView.findViewById(R.id.reminderItemDate);
            time = itemView.findViewById(R.id.reminderItemTime);
            reminderLayout = itemView.findViewById(R.id.ReminderItemCard);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            mReminderItemClickListener.onClick(getAdapterPosition());
        }
    }
}
