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
        Bundle bundle = intent.getBundleExtra("Item");
        assert bundle != null;
        ReminderItem item = (ReminderItem) bundle.getParcelable("Item");
        assert item != null;
        int test = item.getnotifyId();
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(item);
        notificationHelper.getManager().notify(test, nb.build());
    }
}
