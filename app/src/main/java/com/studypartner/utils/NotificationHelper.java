package com.studypartner.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.media.RingtoneManager;
import android.os.Build;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.models.ReminderItem;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;

class NotificationHelper extends ContextWrapper {
	public static final String channelID = "channelID";
	public static final String channelName = "Channel Name";
	private NotificationManager mManager;
	
	public NotificationHelper(Context base) {
		super(base);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createChannel();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.O)
	private void createChannel() {
		NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
		getManager().createNotificationChannel(channel);
	}
	
	public NotificationManager getManager() {
		if (mManager == null) {
			mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mManager;
	}
	
	public NotificationCompat.Builder getChannelNotification(ReminderItem item) {
		
		PendingIntent pendingIntent = new NavDeepLinkBuilder(getApplicationContext())
				.setComponentName(MainActivity.class)
				.setGraph(R.navigation.main_nav_graph)
				.setDestination(R.id.nav_reminder)
				.createPendingIntent();
		
		return new NotificationCompat.Builder(getApplicationContext(), channelID)
				.setAllowSystemGeneratedContextualActions(true)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setCategory(Notification.CATEGORY_REMINDER)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentTitle(item.getTitle())
				.setContentText(item.getDescription())
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.drawable.app_logo);
	}
}
