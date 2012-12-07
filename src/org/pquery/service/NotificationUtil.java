package org.pquery.service;

import org.pquery.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

public class NotificationUtil {

    private static int notificationId = 0;
    private NotificationManager notifManager;
    private Context cxt;
    
    public NotificationUtil(Context cxt) {
        this.cxt = cxt;
        notifManager = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
    public int createNotification(String title, String message, PendingIntent intent) {
        
        int notificationId = getNextNotificationId();
        changeNotification(notificationId, title, message, intent);
        return notificationId;
    }
    
    public void changeNotification(int notificationId, String title, String message, PendingIntent intent) {
        Notification notification = new Notification(R.drawable.status_bar2, title, System.currentTimeMillis());
        notification.setLatestEventInfo(cxt, title, message, intent);
        notification.defaults = Notification.DEFAULT_ALL;      // vibrate etc
        notifManager.notify(notificationId, notification);
    }
    
    public void dismissNotification(int notificationId) {
        notifManager.cancel(notificationId);
    }
    
    private int getNextNotificationId() {
        return notificationId++;
    }
}
