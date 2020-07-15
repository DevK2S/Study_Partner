package com.studypartner.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.AttendanceAdapter;
import com.studypartner.models.AttendanceItem;

import java.util.ArrayList;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AttendanceFragment extends Fragment {
	private static final String TAG = "AttendanceFragment";
	
	private RecyclerView mRecyclerView;
	
	private ArrayList<AttendanceItem> mAttendanceItemArrayList;
	private AttendanceAdapter attendanceAdapter;
	
	public AttendanceFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		View rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
		
		mRecyclerView = rootView.findViewById(R.id.attendanceRecyclerView);
		
		mAttendanceItemArrayList = new ArrayList<>();
		
		final MainActivity activity = (MainActivity) requireActivity();
		activity.mBottomAppBar.bringToFront();
		activity.fab.bringToFront();
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				activity.mNavController.navigate(R.id.action_nav_attendance_to_nav_home);
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
		});
		
		mAttendanceItemArrayList.add(new AttendanceItem("Java", 60.0, 3, 3));
		
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		mRecyclerView.setAdapter(attendanceAdapter);
		
		return rootView;
	}
}