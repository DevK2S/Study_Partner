package com.studypartner.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.studypartner.R;
import com.studypartner.models.AttendanceItem;

import java.text.DecimalFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {
	private static final String TAG = "AttendanceAdapter";

	public interface AttendanceItemClickListener {
		
		void onAttendedPlusButtonClicked (int position);
		void onAttendedMinusButtonClicked (int position);
		void onMissedPlusButtonClicked (int position);
		void onMissedMinusButtonClicked (int position);
		void editButtonClicked (int position);
		void deleteButtonClicked (int position);
	}
	
	private Context mContext;
	private AttendanceItemClickListener mAttendanceItemClickListener;
	private ArrayList<AttendanceItem> mAttendanceItemArrayList;
	
	public AttendanceAdapter (Context context, ArrayList<AttendanceItem> attendanceItemArrayList, AttendanceItemClickListener attendanceItemClickListener) {
		mContext = context;
		mAttendanceItemArrayList = attendanceItemArrayList;
		mAttendanceItemClickListener = attendanceItemClickListener;
	}
	
	@NonNull
	@Override
	public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		Log.d(TAG, "onCreateViewHolder: starts");
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.attendance_item, parent, false);
		return new AttendanceViewHolder(itemView, mAttendanceItemClickListener);
	}
	
	@Override
	public void onBindViewHolder(@NonNull final AttendanceViewHolder holder, final int position) {
		Log.d(TAG, "onBindViewHolder: starts at position " + position);
		
		final AttendanceItem item = mAttendanceItemArrayList.get(position);
		
		DecimalFormat decimalFormat = new DecimalFormat("##.#");
		
		holder.subjectName.setText(item.getSubjectName());
		holder.classesAttended.setText(mContext.getString(R.string.attendance_item_attended, item.getAttendedClasses()));
		holder.classesMissed.setText(mContext.getString(R.string.attendance_item_missed, item.getMissedClasses()));
		holder.attendedProgressBar.setProgress((float) item.getAttendedPercentage());
		
		if (item.getTotalClasses() > 0) {
			Log.d(TAG, "onBindViewHolder: total classes > 0");
			holder.percentageAttended.setText(decimalFormat.format(item.getAttendedPercentage()) + "%");
			
			if (item.getAttendedPercentage() < item.getRequiredPercentage()) {
				Log.d(TAG, "onBindViewHolder: percentage less than required, setting colors red");
				
				holder.attendedProgressBar.setProgressBarColor(mContext.getColor(R.color.lowAttendanceColor));
				holder.percentageAttended.setTextColor(mContext.getColor(R.color.lowAttendanceColor));
				
				if (item.getClassesNeededToAttend() > 1) {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_need_to_attend, item.getClassesNeededToAttend(), "es"));
				} else if (item.getClassesNeededToAttend() == 1) {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_need_to_attend, item.getClassesNeededToAttend(), ""));
				} else {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_cannot_miss));
				}
				
			} else {
				Log.d(TAG, "onBindViewHolder: percentage more than required, setting colors green");
				
				holder.attendedProgressBar.setProgressBarColor(mContext.getColor(R.color.highAttendanceColor));
				holder.percentageAttended.setTextColor(mContext.getColor(R.color.highAttendanceColor));
				
				if (item.getClassesNeededToAttend() < -1) {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_you_can_miss, item.getClassesNeededToAttend() * (-1), "es"));
				} else if (item.getClassesNeededToAttend() == -1) {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_you_can_miss, item.getClassesNeededToAttend() * (-1), ""));
				} else {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_cannot_miss));
				}
			}
			
		} else {
			Log.d(TAG, "onBindViewHolder: no classes attended");
			holder.percentageAttended.setText(mContext.getString(R.string.attendance_item_empty_percentage));
			holder.classesText.setText("");
		}
	}
	
	@Override
	public int getItemCount() {
		return mAttendanceItemArrayList.size();
	}
	
	static class AttendanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		
		private TextView subjectName, percentageAttended, classesAttended, classesMissed, classesText;
		private Button attendedPlusButton, attendedMinusButton, missedPlusButton, missedMinusButton;
		private ImageButton editButton, deleteButton;
		private CircularProgressBar attendedProgressBar;
		
		private AttendanceItemClickListener mClickListener;
		
		public AttendanceViewHolder(@NonNull View itemView, AttendanceItemClickListener listener) {
			super(itemView);
			
			mClickListener = listener;
			
			subjectName = itemView.findViewById(R.id.attendanceItemSubjectName);
			percentageAttended = itemView.findViewById(R.id.attendanceItemPercentageAttended);
			classesText = itemView.findViewById(R.id.attendanceItemClassesText);
			classesAttended = itemView.findViewById(R.id.attendanceItemAttended);
			classesMissed = itemView.findViewById(R.id.attendanceItemMissed);
			
			attendedPlusButton = itemView.findViewById(R.id.attendanceItemAttendedPlusButton);
			attendedPlusButton.setOnClickListener(this);
			attendedMinusButton = itemView.findViewById(R.id.attendanceItemAttendedMinusButton);
			attendedMinusButton.setOnClickListener(this);
			missedPlusButton = itemView.findViewById(R.id.attendanceItemMissedPlusButton);
			missedPlusButton.setOnClickListener(this);
			missedMinusButton = itemView.findViewById(R.id.attendanceItemMissedMinusButton);
			missedMinusButton.setOnClickListener(this);
			editButton = itemView.findViewById(R.id.attendanceItemEditButton);
			editButton.setOnClickListener(this);
			deleteButton = itemView.findViewById(R.id.attendanceItemDeleteButton);
			deleteButton.setOnClickListener(this);
			
			attendedProgressBar = itemView.findViewById(R.id.attendanceItemAttendedPercentageProgressBar);
		}
		
		@Override
		public void onClick(View v) {

			if (v.getId() == attendedPlusButton.getId()) {

				Log.d(TAG, "onClick: attended plus clicked");
				mClickListener.onAttendedPlusButtonClicked(getAdapterPosition());

			} else if (v.getId() == attendedMinusButton.getId()) {

				Log.d(TAG, "onClick: attended minus clicked");
				mClickListener.onAttendedMinusButtonClicked(getAdapterPosition());

			} else if (v.getId() == missedPlusButton.getId()) {

				Log.d(TAG, "onClick: missed plus clicked");
				mClickListener.onMissedPlusButtonClicked(getAdapterPosition());

			} else if (v.getId() == missedMinusButton.getId()) {

				Log.d(TAG, "onClick: missed minus clicked");
				mClickListener.onMissedMinusButtonClicked(getAdapterPosition());

			} else if (v.getId() == editButton.getId()) {
				Log.d(TAG, "onClick: edit button clicked");
				mClickListener.editButtonClicked(getAdapterPosition());

			} else if (v.getId() == deleteButton.getId()) {
				Log.d(TAG, "onClick: delete button clicked");
				mClickListener.deleteButtonClicked(getAdapterPosition());

			}
		}
	}
}
