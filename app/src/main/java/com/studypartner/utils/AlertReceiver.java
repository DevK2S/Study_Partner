package com.studypartner.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.studypartner.models.ReminderItem;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        
        Bundle bundle = intent.getBundleExtra("EXTRA_REMINDER_ITEM");
        assert bundle != null;
        ReminderItem item = bundle.getParcelable("BUNDLE_REMINDER_ITEM");
        assert item != null;
      
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder builder = notificationHelper.getChannelNotification(item);
        notificationHelper.getManager().notify(item.getnotifyId(), builder.build());
        
    }
}
