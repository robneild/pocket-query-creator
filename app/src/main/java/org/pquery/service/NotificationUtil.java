package org.pquery.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import org.pquery.Main;
import org.pquery.R;
import org.pquery.util.Logger;

import java.io.File;

public class NotificationUtil {

    private static int notificationId = 1;
    private NotificationManager notifManager;
    private Service cxt;

    public NotificationUtil(Service cxt) {
        this.cxt = cxt;
        notifManager = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void startInProgressNotification(String title, String message, PendingIntent intent) {
        Logger.d("[title=" + title + ",message=" + message + "]");
        int notificationId = getNextNotificationId();

        Notification notification = new Notification(R.drawable.status_bar2, title, System.currentTimeMillis());
        notification.setLatestEventInfo(cxt, title, message, intent);

        //notifManager.notify(notificationId, notification);
        cxt.startForeground(notificationId, notification);
        return;
    }

    public int showEndNotification(String title, String message) {
        Logger.d("[title=" + title + ",message=" + message + "]");
        cxt.stopForeground(true);

        int notificationId = getNextNotificationId();
        PendingIntent intent = getPendingIntent(title, message, notificationId);

        Notification notification = new Notification(R.drawable.status_bar2, title, System.currentTimeMillis());
        notification.setLatestEventInfo(cxt, title, message, intent);
        notification.defaults = Notification.DEFAULT_ALL;      // vibrate etc
        notifManager.notify(notificationId, notification);

        return notificationId;
    }

    public void closeInProgressNotification() {
        Logger.d("close");
        cxt.stopForeground(true);
    }

    private int getNextNotificationId() {
        return notificationId++;
    }

    private PendingIntent getPendingIntent(String title, String message, int notificationId) {
        return getPendingIntent(title , message, notificationId, null);
    }

    private PendingIntent getPendingIntent(String title, String message, int notificationId, File fileNameDownloaded) {
        Intent intent = new Intent(cxt, Main.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("notificationId", notificationId);
        intent.putExtra("fileNameDownloaded", fileNameDownloaded);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(cxt, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
