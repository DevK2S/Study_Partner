package com.studypartner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.studypartner.R;
import com.studypartner.models.AttendanceItem;

import java.text.DecimalFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HomeAttendanceAdapter extends RecyclerView.Adapter<HomeAttendanceAdapter.HomeAttendanceViewHolder> {
	
	private final Context mContext;
	private final ArrayList<AttendanceItem> mAttendanceItems;
	
	public HomeAttendanceAdapter(Context context, ArrayList<AttendanceItem> attendanceItems) {
		mContext = context;
		mAttendanceItems = attendanceItems;
	}
	
	@NonNull
	@Override
	public HomeAttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.home_carousel_attendance_item, parent, false);
		return new HomeAttendanceViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull HomeAttendanceViewHolder holder, int position) {
		
		final AttendanceItem item = mAttendanceItems.get(position);
		
		DecimalFormat decimalFormat = new DecimalFormat("##.#");
		
		holder.subjectName.setText(item.getSubjectName());
		holder.percentageAttended.setText(mContext.getString(R.string.attendance_percentage, decimalFormat.format(item.getAttendedPercentage())));
		
		if (item.getClassesNeededToAttend() > 1) {
			holder.classesText.setText(mContext.getString(R.string.attendance_item_need_to_attend, item.getClassesNeededToAttend(), "es"));
		} else if (item.getClassesNeededToAttend() == 1) {
			holder.classesText.setText(mContext.getString(R.string.attendance_item_need_to_attend, item.getClassesNeededToAttend(), ""));
		}
		
		holder.attendedProgressBar.setProgress((float) item.getAttendedPercentage());
		holder.requiredProgressBar.setProgress((float) item.getRequiredPercentage());
		
	}
	
	@Override
	public int getItemCount() {
		return mAttendanceItems.size();
	}
	
	public static class HomeAttendanceViewHolder extends RecyclerView.ViewHolder {
		
		private final TextView subjectName;
		private final TextView classesText;
		private final TextView percentageAttended;
		private final CircularProgressBar attendedProgressBar;
		private final CircularProgressBar requiredProgressBar;
		
		public HomeAttendanceViewHolder(@NonNull View itemView) {
			super(itemView);
			
			subjectName = itemView.findViewById(R.id.homeCarouselAttendanceSubjectName);
			classesText = itemView.findViewById(R.id.homeCarouselAttendanceClassesNeededToAttend);
			percentageAttended = itemView.findViewById(R.id.homeCarouselAttendancePercentageAttended);
			
			attendedProgressBar = itemView.findViewById(R.id.homeCarouselAttendanceAttendedProgressBar);
			requiredProgressBar = itemView.findViewById(R.id.homeCarouselAttendanceRequiredProgressBar);
		}
	}
}
