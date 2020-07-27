package com.studypartner.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.ReminderAdapter;
import com.studypartner.models.AttendanceItem;
import com.studypartner.models.ReminderItem;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ReminderFragment extends Fragment {
	private static final String TAG = "ReminderFragment";
	private FloatingActionButton mfab;
	private RecyclerView mRecyclerView;
	private ArrayList<ReminderItem> mReminderList;
	private ReminderAdapter reminderAdapter;

	public ReminderFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		View rootView = inflater.inflate(R.layout.fragment_reminder, container, false);
		final MainActivity activity = (MainActivity) requireActivity();
		mfab = activity.fab;
		mRecyclerView = rootView.findViewById(R.id.recyclerview);
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				MainActivity activity = (MainActivity) requireActivity();
				activity.mBottomAppBar.setVisibility(View.VISIBLE);
				activity.mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
				activity.fab.setVisibility(View.VISIBLE);
				activity.mNavController.navigate(R.id.action_nav_reminder_to_nav_home);
			}
		});
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		mRecyclerView.setAdapter(reminderAdapter);
		mfab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				activity.mNavController.navigate(R.id.reminderDialogFragment);
				//attendanceAdapter.notifyItemInserted(mAttendanceItemArrayList.size());
				mfab.setVisibility(View.GONE);
			}
		});

		populateDataAndSetAdapter();
		return rootView;
	}

	private void populateDataAndSetAdapter() {

		final SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
		final Gson gson = new Gson();
		final SharedPreferences.Editor reminderPreferenceEditor = reminderPreference.edit();

		if (reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
			String json = reminderPreference.getString("REMINDER_ITEMS", "");
			Type type = new TypeToken<ArrayList<ReminderItem>>() {
			}.getType();
			mReminderList = gson.fromJson(json, type);
			Log.d("TEST", "Entered");
		} else {
			mReminderList = new ArrayList<>();
			Log.d("TEST", "NotEntered");
		}
		int c = mReminderList.size();
		Log.d("Hello", String.valueOf(c));
		reminderAdapter = new ReminderAdapter(getContext(), mReminderList, new ReminderAdapter.ReminderItemClickListener() {
			@Override
			public void editButtonClicked(int position) {
				editReminder(position);
			}

			@Override
			public void deleteButtonClicked(int position) {
				deleteReminder(position);
			}
		});
		mRecyclerView.setAdapter(reminderAdapter);
	}

	private void deleteReminder(final int position) {
		Log.d(TAG, "deleteSubject: delete button clicked");

		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Delete Reminder");
		builder.setMessage("Are you sure you want to the reminder");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mReminderList.remove(position);
				reminderAdapter.notifyItemRemoved(position);

				SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
				SharedPreferences.Editor reminderPreferenceEditor = reminderPreference.edit();
				Gson gson = new Gson();

				String json = gson.toJson(mReminderList);

				if (mReminderList.size() == 0) {
					reminderPreferenceEditor.putBoolean("REMINDER_ITEMS_EXISTS", false);
				}

				reminderPreferenceEditor.putString("REMINDER_ITEMS", json);
				reminderPreferenceEditor.apply();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.show();
	}

	private void editReminder(int position) {
		Bundle bundle = new Bundle();
		bundle.putString("ItemPosition", String.valueOf(position));
		((MainActivity) requireActivity()).mNavController.navigate(R.id.reminderDialogFragment, bundle);
		mfab.setVisibility(View.GONE);
	}

}