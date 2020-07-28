package com.studypartner.fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.ReminderAdapter;
import com.studypartner.models.ReminderItem;
import com.studypartner.utils.AlertReceiver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class ReminderFragment extends Fragment implements ReminderAdapter.ReminderItemClickListener {
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
		
		View rootView = inflater.inflate(R.layout.fragment_reminder, container, false);
		
		final MainActivity activity = (MainActivity) requireActivity();
		mfab = rootView.findViewById(R.id.reminderFab);
		activity.fab.hide();
		activity.mBottomAppBar.performHide();
		activity.mBottomAppBar.setVisibility(View.GONE);
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				activity.mNavController.navigate(R.id.action_nav_reminder_to_nav_home);
			}
		});
		
		mRecyclerView = rootView.findViewById(R.id.recyclerview);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        mfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.mNavController.navigate(R.id.reminderDialogFragment);
                mfab.setVisibility(View.GONE);
            }
        });

        populateDataAndSetAdapter();
        //enableSwipe();
        return rootView;
    }

	private void populateDataAndSetAdapter() {

		SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
		Gson gson = new Gson();
		SharedPreferences.Editor reminderPreferenceEditor = reminderPreference.edit();
		
		if (reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
			String json = reminderPreference.getString("REMINDER_ITEMS", "");
			Type type = new TypeToken<ArrayList<ReminderItem>>() {}.getType();
			mReminderList = gson.fromJson(json, type);
		} else {
			mReminderList = new ArrayList<>();
		}
		
		Calendar calendar = Calendar.getInstance();
		Calendar today = Calendar.getInstance();

		for (int position = 0; position < mReminderList.size(); position++) {
			
			ReminderItem item = mReminderList.get(position);
			
			int year = Integer.parseInt(item.getDate().substring(6));
			int month = Integer.parseInt(item.getDate().substring(3, 5)) - 1;
			int day = Integer.parseInt(item.getDate().substring(0, 2));
			int hour = Integer.parseInt(item.getTime().substring(0, 2));
			int minute = Integer.parseInt(item.getTime().substring(3, 5));
			String amOrPm = item.getTime().substring(6);
			if (amOrPm.equals("PM") && hour != 12)
				hour = hour + 12;
			calendar.set(year, month, day, hour, minute);
			if (calendar.compareTo(today) < 0) {
				mReminderList.get(position).setActive(false);
			}
		}
		
		Log.d(TAG, "populateDataAndSetAdapter: " + mReminderList.toString());
		
		if (mReminderList.size() == 0) {
			reminderPreferenceEditor.putBoolean("REMINDER_ITEMS_EXISTS", false);
		}

        String json = gson.toJson(mReminderList);

        reminderPreferenceEditor.putString("REMINDER_ITEMS", json);
        reminderPreferenceEditor.apply();

        reminderAdapter = new ReminderAdapter(getContext(), mReminderList, this);

        mRecyclerView.setAdapter(reminderAdapter);
    }

    void enableSwipe() {
       /* final SwipeControl swipeController = new SwipeControl();
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);
		mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
			@Override
			public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
				swipeController.onDraw(c);
			}
		});*/
    }

    private void deleteReminder(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Delete Reminder");
        builder.setMessage("Are you sure you want to remove the reminder");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
				
				AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
				Intent intent = new Intent(requireContext(), AlertReceiver.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("BUNDLE_REMINDER_ITEM", mReminderList.get(position));
				intent.putExtra("EXTRA_REMINDER_ITEM", bundle);
				
				PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				
				alarmManager.cancel(pendingIntent);
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
		bundle.putString("REMINDER_POSITION", String.valueOf(position));
		((MainActivity) requireActivity()).mNavController.navigate(R.id.reminderDialogFragment, bundle);
		mfab.setVisibility(View.GONE);
		
	}

    @Override
    public void onClick(int position) {
        editReminder(position);
    }

    @Override
    public void onLongClick(int position) {
        deleteReminder(position);
    }

    @Override
    public void deleteView(int adapterPosition) {

        deleteReminder(adapterPosition);
    }
}