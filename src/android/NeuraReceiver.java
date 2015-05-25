package net.linnovate.NeuraNest;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import com.ionicframework.neuranest118899.R;
import com.neura.sdk.config.NeuraConsts;

import net.linnovate.NeuraNest.util.Utils;

import org.apache.cordova.LOG;
import org.json.JSONException;
import org.json.JSONObject;

public class NeuraReceiver extends BroadcastReceiver {
    private final static String TAG = "BroadcastReceiver";

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
                .setSmallIcon(R.drawable.icon).setAutoCancel(true).setContentTitle(title)
                .setContentText(message).build();

        /*Intent notificationIntent = new Intent(context, HomeActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        notification.setLatestEventInfo(context, title, message, pendingIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;*/


        notificationManager.notify(45, notification);

        //try {
            JSONObject data = new JSONObject();
            //Utils.sendData(data, );
        //}
        //catch(JSONException e) {
        //    LOG.d(TAG, e.toString());
        //}
    }
}
