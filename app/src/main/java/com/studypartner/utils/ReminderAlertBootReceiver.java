package com.studypartner.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studypartner.models.ReminderItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class ReminderAlertBootReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction() != null && (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("android.intent.action.TIME_SET") || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON"))) {
			Log.d("TAG", "onReceive: " + intent.getAction());
			
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			
			ArrayList<ReminderItem> mReminderList = null;
			
			SharedPreferences reminderPreference = context.getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
			Gson gson = new Gson();
			
			if (reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
				String json = reminderPreference.getString("REMINDER_ITEMS", "");
				Type type = new TypeToken<ArrayList<ReminderItem>>() {
				}.getType();
				mReminderList = gson.fromJson(json, type);
			}
			
			if (mReminderList == null) {
				mReminderList = new ArrayList<>();
			}
			
			for (ReminderItem item: mReminderList) {
				if (item.isActive()) {
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
					
					Intent reminderIntent = new Intent(context, ReminderAlertReceiver.class);
					
					Bundle bundle = new Bundle();
					bundle.putParcelable("BUNDLE_REMINDER_ITEM", item);
					
					reminderIntent.putExtra("EXTRA_REMINDER_ITEM", bundle);
					
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context, item.getNotifyId(), reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
				}
			}
		}
		
	}
}
