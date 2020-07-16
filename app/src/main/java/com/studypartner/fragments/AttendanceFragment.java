package com.studypartner.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.AttendanceAdapter;
import com.studypartner.models.AttendanceItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AttendanceFragment extends Fragment {
	private static final String TAG = "AttendanceFragment";
	
	private RecyclerView mRecyclerView;
	private BottomAppBar mBottomAppBar;
	private FloatingActionButton mfab;
	private NavController mNavController;
	private Button addButton;
	private TextView dateText, dayText, percentageAttended;
	private CircularProgressBar attendedProgressBar, requiredProgressBar;
	
	private ArrayList<AttendanceItem> mAttendanceItemArrayList;
	private AttendanceAdapter attendanceAdapter;
	
	private double requiredPercentage, totalPercentageAttended;
	
	public AttendanceFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		View rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
		
		mRecyclerView = rootView.findViewById(R.id.attendanceRecyclerView);
		addButton = rootView.findViewById(R.id.attendanceAddButton);
		dateText = rootView.findViewById(R.id.attendanceDate);
		dayText = rootView.findViewById(R.id.attendanceDay);
		percentageAttended = rootView.findViewById(R.id.attendancePercentageAttended);
		attendedProgressBar = rootView.findViewById(R.id.attendanceAttendedTotalProgressBar);
		requiredProgressBar = rootView.findViewById(R.id.attendanceRequiredProgressBar);
		
		initializeViews();
		
		mAttendanceItemArrayList = new ArrayList<>();
		
		MainActivity activity = (MainActivity) requireActivity();
		mBottomAppBar = activity.mBottomAppBar;
		mfab = activity.fab;
		mNavController = NavHostFragment.findNavController(this);
		
		mBottomAppBar.bringToFront();
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				mNavController.navigate(R.id.action_nav_attendance_to_nav_home);
				mBottomAppBar.performShow();
				mfab.show();
			}
		});
		
		attendanceAdapter = new AttendanceAdapter(getContext(), mAttendanceItemArrayList, new AttendanceAdapter.AttendanceItemClickListener() {
			@Override
			public void onAttendedPlusButtonClicked(int position) {
				mAttendanceItemArrayList.get(position).increaseAttendedClasses();
				attendanceAdapter.notifyItemChanged(position);
			}
			
			@Override
			public void onAttendedMinusButtonClicked(int position) {
				mAttendanceItemArrayList.get(position).decreaseAttendedClasses();
				attendanceAdapter.notifyItemChanged(position);
			}
			
			@Override
			public void onMissedPlusButtonClicked(int position) {
				mAttendanceItemArrayList.get(position).increaseMissedClasses();
				attendanceAdapter.notifyItemChanged(position);
			}
			
			@Override
			public void onMissedMinusButtonClicked(int position) {
				mAttendanceItemArrayList.get(position).decreaseMissedClasses();
				attendanceAdapter.notifyItemChanged(position);
			}
			
			@Override
			public void editButtonClicked(int position) {
				editSubjectName(position);
			}
			
			@Override
			public void deleteButtonClicked(int position) {
				deleteSubject(position);
			}
		});
		
		mAttendanceItemArrayList.add(new AttendanceItem("Java", 60.0, 3, 3));
		mAttendanceItemArrayList.add(new AttendanceItem("C", 60.0, 1, 2));
		mAttendanceItemArrayList.add(new AttendanceItem("C++", 60.0, 2, 1));
		mAttendanceItemArrayList.add(new AttendanceItem("Python", 60.0, 4, 5));
		
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		mRecyclerView.setAdapter(attendanceAdapter);
		
		return rootView;
	}
	
	private void initializeViews() {
		
		Date date = new Date();
		SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'th' MMMM, yyyy", Locale.getDefault());
		dayText.setText(dayFormat.format(date));
		dateText.setText(dateFormat.format(date));
		
	}
	
	private void editSubjectName(final int position) {
		Log.d(TAG, "editSubjectName: edit button clicked");
		
		final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.attendance_item_subject_name_dialog, null);
		
		Button okButton = dialogView.findViewById(R.id.attendanceItemSubjectOkButton);
		Button cancelButton = dialogView.findViewById(R.id.attendanceItemSubjectCancelButton);
		final TextInputLayout subjectNameTextInput = dialogView.findViewById(R.id.attendanceItemSubjectNameTextInput);
		subjectNameTextInput.getEditText().setText(mAttendanceItemArrayList.get(position).getSubjectName(), TextView.BufferType.EDITABLE);
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: cancel pressed while changing subject");
				builder.dismiss();
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: ok pressed while changing subject");
				mAttendanceItemArrayList.get(position).setSubjectName(subjectNameTextInput.getEditText().getText().toString());
				builder.dismiss();
				attendanceAdapter.notifyItemChanged(position);
			}
		});
		
		builder.setView(dialogView);
		builder.show();
	}
	
	private void deleteSubject (int position) {
		Log.d(TAG, "deleteSubject: delete button clicked");
		mBottomAppBar.performShow();
		
		mAttendanceItemArrayList.remove(position);
		attendanceAdapter.notifyItemRemoved(position);
		attendanceAdapter.notifyItemRangeChanged(0, mAttendanceItemArrayList.size());
	}
}