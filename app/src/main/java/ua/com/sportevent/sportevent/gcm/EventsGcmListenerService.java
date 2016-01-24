package ua.com.sportevent.sportevent.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.gcm.GcmListenerService;

import ua.com.sportevent.sportevent.DetailActivity;
import ua.com.sportevent.sportevent.R;
import ua.com.sportevent.sportevent.helpers.StorageHelper;

/**
 * Enables various aspects of handling messages such as detecting different downstream message
 * types, determining upstream send status, and automatically displaying simple notifications
 * on the app’s behalf.
 */
public class EventsGcmListenerService extends GcmListenerService {

    public static final int NOTIFICATION_ID = 0;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        // TODO: 27.11.2015 add key
        // TODO: 27.11.2015 bundle data - should be json msg
        // Check that the message is coming from your server.
        if (getString(R.string.gcm_defaultSenderId).equals(from)) {
            String message = data.getString("m");
            // TODO: 03.12.2015 signal to reload the data or update the data
            StorageHelper.setEventValues(null);
            // TODO: 13.01.2016 Don't create a notification if the relevant new information is currently on screen.
            sendNotification(message);
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent activityIntent = new Intent(this, DetailActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(DetailActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(activityIntent);
        // Gets a PendingIntent containing the entire back stack
        activityIntent.putExtra("EXTRA_MESSAGE", "new");
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0 /* code */, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO: 27.11.2015 show notification
        // TODO: 13.01.2016 add sound preference
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // TODO: 29.11.2015 edit icon, title and text
//        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.art_storm);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_arrow_downward_white_24dp)
//                .setLargeIcon(bitmap here)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent)) // in 5.0+
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setCategory(NotificationCompat.CATEGORY_EMAIL)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                /*
                 * Sets the big view "big text" style and supplies the
                 * text (the user's reminder message) that will be displayed
                 * in the detail area of the expanded notification.
                 * These calls are ignored by the support library for
                 * pre-4.1 devices.
                 */
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message)) // multiple lines of code
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

//        Notification notification = notificationBuilder.build();
//        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    }
}
