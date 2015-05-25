package net.linnovate.NeuraNest;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import com.ionicframework.neuranest118899.R;
import com.neura.sdk.config.NeuraConsts;

public class NeuraReceiver extends BroadcastReceiver {

    // Determine whether the broadcast is in response to the app registering an
    // event or whether Neura is sending an event notification
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        String eventName = intent.getStringExtra(NeuraConsts.EXTRA_EVENT_NAME);

        if (action.equalsIgnoreCase(NeuraConsts.ACTION_NEURA_EVENT)) {
            handleNeuraEvent(context, intent, eventName);
        }
    }

    // This event handler executes when Neura sends an event broadcast
    // For the demo app, we simply have a notification pop up on the user's
    // phone
    // In your app, we hope that you're much more creative. Turn on a light,
    // lock a door, let the magic flow
    private void handleNeuraEvent(Context context, Intent intent, String eventName) {

        /**
         * the intent Bundle will contain key value pairs of additional
         * parameters related to the event, according to the documentation
         */

        String title = "Neura Event";
        String message = "Event Accured: " + eventName + ", ";

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context).setSound(uri)
                .setSmallIcon(R.drawable.icon).setContentTitle(title)
                .setContentText(message).build();
        notificationManager.notify(45, notification);
    }
}
