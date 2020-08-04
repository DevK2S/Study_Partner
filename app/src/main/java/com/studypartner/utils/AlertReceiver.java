package com.studypartner.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import com.studypartner.models.ReminderItem;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle bundle = intent.getBundleExtra("EXTRA_REMINDER_ITEM");
		assert bundle != null;
		ReminderItem item = bundle.getParcelable("BUNDLE_REMINDER_ITEM");
		assert item != null;
		boolean cancel = intent.getBooleanExtra("CANCEL", false);
		
		NotificationHelper notificationHelper = new NotificationHelper(context);
		PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = powerManager.isInteractive();
		if(!isScreenOn) {
			PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,"StudyPartner:WakeLockTag");
			wakeLock.acquire(1000);
			PowerManager.WakeLock wakeLock_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"StudyPartner:CPUWakeLockTag");
			wakeLock_cpu.acquire(1000);
		}
		if (cancel) {
			notificationHelper.getManager().cancel(item.getNotifyId());
		} else {
			NotificationCompat.Builder builder = notificationHelper.getChannelNotification(context, item);
			notificationHelper.getManager().notify(item.getNotifyId(), builder.build());
		}
	}
}
