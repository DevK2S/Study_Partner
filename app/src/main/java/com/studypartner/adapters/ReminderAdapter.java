package com.studypartner.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.studypartner.R;
import com.studypartner.models.ReminderItem;
import com.zerobranch.layout.SwipeLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
	
	private final Context context;
	private final ReminderItemClickListener mReminderItemClickListener;
	private final ArrayList<ReminderItem> mReminderList;
	
	public ReminderAdapter(Context context, ArrayList<ReminderItem> mReminderList, ReminderItemClickListener mReminderItemClickListener) {
		this.context = context;
		this.mReminderItemClickListener = mReminderItemClickListener;
		this.mReminderList = mReminderList;
	}
	
	@NotNull
	@Override
	public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
		if (item.isActive()) {
			
			holder.activeIcon.setImageResource(R.drawable.alarm_icon);
			holder.activeIcon.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			holder.reminderCalendar.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			holder.reminderClock.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			holder.title.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			holder.date.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			holder.time.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			
		} else {
			
			holder.activeIcon.setImageResource(R.drawable.alarm_off_icon);
			holder.activeIcon.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			holder.reminderCalendar.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			holder.reminderClock.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			holder.title.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			holder.date.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			holder.time.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			
		}
		
		applyClickEvents(holder);
	}
	
	private void applyClickEvents(final ReminderViewHolder holder) {
		holder.reminderLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mReminderItemClickListener.onClick(holder.getAdapterPosition());
			}
		});
		holder.delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				holder.swipeLayout.close();
				mReminderItemClickListener.deleteView(holder.getAdapterPosition());
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return mReminderList.size();
	}
	
	public interface ReminderItemClickListener {
		void onClick(int position);
		
		void deleteView(int position);
	}
	
	public class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		
		private final TextView title;
		private final TextView date;
		private final TextView time;
		private final CardView reminderLayout;
		private final SwipeLayout swipeLayout;
		private final ImageView activeIcon;
		private final ImageView reminderClock;
		private final ImageView reminderCalendar;
		private final ImageView delete;
		
		public ReminderViewHolder(@NonNull View itemView) {
			super(itemView);
			
			title = itemView.findViewById(R.id.reminderItemTitle);
			date = itemView.findViewById(R.id.reminderItemDate);
			time = itemView.findViewById(R.id.reminderItemTime);
			swipeLayout = itemView.findViewById(R.id.swipeLayout);
			activeIcon = itemView.findViewById(R.id.reminderActiveIcon);
			reminderCalendar = itemView.findViewById(R.id.reminderCalendar);
			reminderClock = itemView.findViewById(R.id.reminderClock);
			reminderLayout = itemView.findViewById(R.id.reminderItemCard);
			delete = itemView.findViewById(R.id.reminderItemDeleteIcon);
			
			itemView.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View view) {
			mReminderItemClickListener.onClick(getAdapterPosition());
		}
		
	}
}
