package com.studypartner.fragments;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studypartner.R;
import com.studypartner.models.ReminderItem;
import com.studypartner.utils.ReminderAlertReceiver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import static android.content.Context.MODE_PRIVATE;

public class ReminderDialogFragment extends DialogFragment {
	
	private TextInputEditText mTitleEditText;
	private TextInputEditText mContentEditText;
	private TextView mDateEditText;
	private TextView mTimeEditText;
	
	private NavController mNavController;
	
	private DatePickerDialog mDatePicker;
	private TimePickerDialog mTimePicker;
	
	private FloatingActionButton okFab;
	
	private String date, time;
	
	private boolean inEditMode = false;
	
	private String currentDate, currentTime;
	private int hourSelected, minuteSelected;
	
	private ArrayList<ReminderItem> mReminderList = new ArrayList<>();
	
	public ReminderDialogFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_reminder_dialog, container, false);
		
		int positionToEdit = -1;
		
		if (getArguments() != null) {
			inEditMode = true;
			positionToEdit = Integer.parseInt(String.valueOf(getArguments().getString("REMINDER_POSITION")));
		}
		
		mTitleEditText = rootView.findViewById(R.id.titleEditText);
		mContentEditText = rootView.findViewById(R.id.descriptionEditText);
		mDateEditText = rootView.findViewById(R.id.dateTextView);
		mTimeEditText = rootView.findViewById(R.id.timeTextView);
		
		okFab = rootView.findViewById(R.id.okButton);
		
		mNavController = NavHostFragment.findNavController(this);
		
		currentDate = getCurrentDate();
		currentTime = getCurrentTime();
		
		if (inEditMode) {
			populateEditText(positionToEdit);
		} else {
			date = currentDate;
			mDateEditText.setText(currentDate);
			
			time = currentTime;
			mTimeEditText.setText(currentTime);
		}
		
		final SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
		final Gson gson = new Gson();
		final SharedPreferences.Editor reminderPreferenceEditor = reminderPreference.edit();
		
		if (reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
			
			String json = reminderPreference.getString("REMINDER_ITEMS", "");
			Type type = new TypeToken<ArrayList<ReminderItem>>() {
			}.getType();
			mReminderList = gson.fromJson(json, type);
		}
		
		if (mReminderList == null) {
			mReminderList = new ArrayList<>();
		}
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				dismiss();
				mNavController.navigateUp();
			}
		});
		
		mDateEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				
				int year = Integer.parseInt(date.substring(6));
				int month = Integer.parseInt(date.substring(3, 5)) - 1;
				int day = Integer.parseInt(date.substring(0, 2));
				
				mDatePicker = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
						
						String selectedYear = String.valueOf(year);
						String selectedMonth = String.valueOf(month + 1); // starts with 0
						String selectedDay = String.valueOf(dayOfMonth);
						
						if (month < 9) { // to make 01 - 09
							selectedMonth = "0" + selectedMonth;
						}
						
						if (dayOfMonth < 10) { // to make 01 - 09
							selectedDay = "0" + selectedDay;
						}
						
						date = selectedDay + "-" + selectedMonth + "-" + selectedYear;
						
						mDateEditText.setText(date);
						
					}
				}, year, month, day);
				
				mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
				
				mDatePicker.show();
			}
		});
		
		mTimeEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				
				int hour = Integer.parseInt(time.substring(0, 2));
				int minute = Integer.parseInt(time.substring(3, 5));
				
				String am_pm = time.substring(6);
				
				if (am_pm.equals("PM") && hour != 12)
					hour = hour + 12;
				
				mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
						
						hourSelected = hourOfDay;
						minuteSelected = minute;
						
						String am_pm = "AM";
						
						if (hourOfDay >= 12) {
							am_pm = "PM";
						}
						
						if (hourOfDay > 12) {
							hourOfDay = hourOfDay - 12;
						}
						
						String selectedHour = String.valueOf(hourOfDay);
						String selectedMinute = String.valueOf(minute);
						
						if (hourOfDay < 10)
							selectedHour = "0" + selectedHour;
						
						if (minute < 10)
							selectedMinute = "0" + selectedMinute;
						
						time = selectedHour + ":" + selectedMinute + " " + am_pm;
						
						mTimeEditText.setText(time);
						
					}
				}, hour, minute, false);
				
				mTimePicker.show();
			}
		});
		
		final int finalPositionToEdit = positionToEdit;
		
		okFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Calendar cal = Calendar.getInstance();
				
				if (date.equals(currentDate) && time.equals(currentTime)) {
					Toast.makeText(requireContext(), "Cannot set reminder for now", Toast.LENGTH_SHORT).show();
				} else if (date.equals(currentDate) && (hourSelected < cal.get(Calendar.HOUR_OF_DAY) || (hourSelected == cal.get(Calendar.HOUR_OF_DAY) && minuteSelected < cal.get(Calendar.MINUTE)))) {
					Toast.makeText(requireContext(), "Cannot set reminder for previous times", Toast.LENGTH_SHORT).show();
				} else {
					String title = mTitleEditText.getText().toString().trim();
					String content = mContentEditText.getText().toString().trim();
					if (title.isEmpty()) title = "Reminder from Study Partner";
					
					if (inEditMode) {
						
						AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
						
						Intent intent = new Intent(requireContext(), ReminderAlertReceiver.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable("BUNDLE_REMINDER_ITEM", mReminderList.get(finalPositionToEdit));
						
						intent.putExtra("EXTRA_REMINDER_ITEM", bundle);
						
						PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), mReminderList.get(finalPositionToEdit).getNotifyId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
						
						alarmManager.cancel(pendingIntent);
						
						mReminderList.get(finalPositionToEdit).edit(title, content, time, date);
						mReminderList.get(finalPositionToEdit).setActive(true);
						
						if (mReminderList.get(finalPositionToEdit).isActive()) {
							ReminderItem newItem = mReminderList.get(finalPositionToEdit);
							mReminderList.remove(finalPositionToEdit);
							mReminderList.add(0, newItem);
						}
						
						createNotification(mReminderList.get(0));
					} else {
						ReminderItem item = new ReminderItem(title, content, time, date);
						
						mReminderList.add(0, item);
						createNotification(item);
					}
					
					String json = gson.toJson(mReminderList);
					
					if (!reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
						reminderPreferenceEditor.putBoolean("REMINDER_ITEMS_EXISTS", true);
					}
					
					reminderPreferenceEditor.putString("REMINDER_ITEMS", json);
					reminderPreferenceEditor.apply();
					
					mNavController.navigateUp();
				}
			}
		});
		return rootView;
	}
	
	private void populateEditText(int position) {
		
		SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
		Gson gson = new Gson();
		
		String json = reminderPreference.getString("REMINDER_ITEMS", "");
		Type type = new TypeToken<ArrayList<ReminderItem>>() {
		}.getType();
		mReminderList = gson.fromJson(json, type);
		
		if (mReminderList != null && mReminderList.size() > position) {
			ReminderItem editItem = mReminderList.get(position);
			mTitleEditText.setText(editItem.getTitle());
			mContentEditText.setText(editItem.getDescription());
			mDateEditText.setText(editItem.getDate());
			date = editItem.getDate();
			mTimeEditText.setText(editItem.getTime());
			time = editItem.getTime();
			hourSelected = Integer.parseInt(time.substring(0, 2));
			minuteSelected = Integer.parseInt(time.substring(3, 5));
		}
		
	}
	
	private void createNotification(ReminderItem item) {
		
		Calendar calendar = Calendar.getInstance();
		
		int year = Integer.parseInt(item.getDate().substring(6));
		int month = Integer.parseInt(item.getDate().substring(3, 5)) - 1;
		int day = Integer.parseInt(item.getDate().substring(0, 2));
		
		int hour = Integer.parseInt(item.getTime().substring(0, 2));
		int minute = Integer.parseInt(item.getTime().substring(3, 5));
		
		String am_pm = item.getTime().substring(6);
		
		if (am_pm.equals("PM") && hour != 12)
			hour = hour + 12;
		
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		
		AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(requireContext(), ReminderAlertReceiver.class);
		
		Bundle bundle = new Bundle();
		bundle.putParcelable("BUNDLE_REMINDER_ITEM", item);
		
		intent.putExtra("EXTRA_REMINDER_ITEM", bundle);
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), item.getNotifyId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
		
	}
	
	private String getCurrentDate() {
		
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		
		String selectedYear = String.valueOf(year);
		String selectedMonth = String.valueOf(month + 1); // starts with 0
		String selectedDay = String.valueOf(dayOfMonth);
		
		if (month < 9) { // to make 01 - 09
			selectedMonth = "0" + selectedMonth;
		}
		
		if (dayOfMonth < 10) { // to make 01 - 09
			selectedDay = "0" + selectedDay;
		}
		
		return selectedDay + "-" + selectedMonth + "-" + selectedYear;
		
	}
	
	private String getCurrentTime() {
		
		Calendar calendar = Calendar.getInstance();
		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		hourSelected = hourOfDay;
		minuteSelected = minute;
		String am_pm = "AM";
		
		if (hourOfDay >= 12) {
			am_pm = "PM";
		}
		
		if (hourOfDay > 12) {
			hourOfDay = hourOfDay - 12;
		}
		
		String selectedHour = String.valueOf(hourOfDay);
		String selectedMinute = String.valueOf(minute);
		
		if (hourOfDay < 10)
			selectedHour = "0" + selectedHour;
		
		if (minute < 10)
			selectedMinute = "0" + selectedMinute;
		
		return selectedHour + ":" + selectedMinute + " " + am_pm;
		
	}
}