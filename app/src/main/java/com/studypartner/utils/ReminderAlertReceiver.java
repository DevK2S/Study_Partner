package com.studypartner.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;

import com.studypartner.models.ReminderItem;

import androidx.core.app.NotificationCompat;

public class ReminderAlertReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle bundle = intent.getBundleExtra("EXTRA_REMINDER_ITEM");
		assert bundle != null;
		ReminderItem item = bundle.getParcelable("BUNDLE_REMINDER_ITEM");
		assert item != null;
		boolean cancel = intent.getBooleanExtra("CANCEL", false);
		
		NotificationHelper notificationHelper = new NotificationHelper(context);
		
		Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (notificationUri == null) {
			notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		}
		Ringtone ringtone = RingtoneManager.getRingtone(context, notificationUri);
		ringtone.play();
		
		if (cancel) {
			notificationHelper.getManager().cancel(item.getNotifyId());
		} else {
			NotificationCompat.Builder builder = notificationHelper.getChannelNotification(context, item);
			notificationHelper.getManager().notify(item.getNotifyId(), builder.build());
		}
		
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock, wakeLock_cpu;
		boolean isScreenOn = powerManager.isInteractive();
		if (!isScreenOn) {
			wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "StudyPartner:WakeLockTag");
			wakeLock.acquire(2000);
			wakeLock_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StudyPartner:CPUWakeLockTag");
			wakeLock_cpu.acquire(2000);
		}
	}
}
