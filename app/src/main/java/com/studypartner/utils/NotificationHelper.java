package com.studypartner.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.models.ReminderItem;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

class NotificationHelper extends ContextWrapper {
	public static final String channelID = "channelID";
	public static final String channelName = "Reminder";
	private NotificationManager mManager;
	
	public NotificationHelper(Context base) {
		super(base);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createChannel();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.O)
	private void createChannel() {
		NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
		channel.enableLights(true);
		AudioAttributes audioAttributes = new AudioAttributes.Builder()
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
				.setUsage(AudioAttributes.USAGE_NOTIFICATION)
				.build();
		channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes);
		getManager().createNotificationChannel(channel);
	}
	
	public NotificationManager getManager() {
		if (mManager == null) {
			mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mManager;
	}
	
	public NotificationCompat.Builder getChannelNotification(Context context, ReminderItem item) {
		
		Bundle bundle = new Bundle();
		bundle.putParcelable("BUNDLE_REMINDER_ITEM", item);
		
//		PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
//				.setComponentName(MainActivity.class)
//				.setGraph(R.navigation.main_nav_graph)
//				.setDestination(R.id.nav_reminder)
//				.createPendingIntent();
		
		Intent openIntent = new Intent(context, MainActivity.class);
		openIntent.putExtra("EXTRA_REMINDER_ITEM", bundle);
		openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent openPendingIntent = PendingIntent.getActivity(context,1,openIntent,PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent dismissIntent = new Intent(context, AlertReceiver.class);
		dismissIntent.putExtra("EXTRA_REMINDER_ITEM", bundle);
		dismissIntent.putExtra("CANCEL", true);
		PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 1, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		return new NotificationCompat.Builder(context, channelID)
				.setAutoCancel(true)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentTitle(item.getTitle())
				.setContentText(item.getDescription())
				.setContentIntent(openPendingIntent)
				.addAction(android.R.drawable.ic_menu_view, "OPEN IN APP", openPendingIntent)
				.addAction(android.R.drawable.ic_delete, "DISMISS", dismissPendingIntent)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.alarm_icon))
				.setSmallIcon(R.drawable.app_logo_transparent)
				.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
				.setCategory(NotificationCompat.CATEGORY_REMINDER)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setDefaults(Notification.DEFAULT_ALL);
	}
}
