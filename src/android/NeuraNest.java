package net.linnovate.NeuraNest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import com.ionicframework.neuranest118899.MainActivity;
import com.neura.sdk.config.NeuraConsts;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.object.Permission;
import com.neura.sdk.object.SubscriptionRequest;
import com.neura.sdk.service.NeuraApiClient;
import com.neura.sdk.service.NeuraServices;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.sdk.util.NeuraAuthUtil;
import com.neura.sdk.util.NeuraUtil;

import net.linnovate.NeuraNest.util.Utils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;


public class NeuraNest extends CordovaPlugin {
    /**
     * The APP_REFERRER let's Neura know that your app directed the user to get
     * Neura Please replace "sample_app" below your app name without spaces so
     * that we know that you're driving lots of downloads. Thanks! This helps a
     * lot for business purposes!
     */
    private static final String APP_REFERRER = "NeuraNest";
    private static final int NEURA_AUTHENTICATION_REQUEST_CODE = 0;

    private static final String TAG = "NeuraNest";
    private NeuraApiClient mNeuraApiClient;

    private CallbackContext callbackContext;

    private BroadcastReceiver receiver;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        /**
         * The Neura app has certain requirements, including: (1) the Neura app
         * does not run on tablets, (2) the operating system must be Android 4.0
         * or higher, and (3) a few other constraints. The isNeuraAppSupported
         * method validates whether the Neura app is capable of running on the
         * device. However, this method does not validate whether the app is
         * currently installed.
         */

        final Activity MainActivity = cordova.getActivity();

        boolean isNeuraAppSupported = NeuraUtil.isNeuraAppSupported(MainActivity);

        if (!isNeuraAppSupported) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity);

            dialogBuilder.setMessage("Error: This device cannot support the Neura app");
            dialogBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.finish();
                }
            });

            dialogBuilder.create().show();
        }
        else {
            NeuraApiClient.Builder builder = new NeuraApiClient.Builder(MainActivity);
            builder.addConnectionCallbacks(mNeuraServiceConnectionCallbacks);
            mNeuraApiClient = builder.build();

            this.cordova.setActivityResultCallback(this);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.neura.android.ACTION_NEURA_EVENT");

        BroadcastReceiver r = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String eventName = intent.getStringExtra(NeuraConsts.EXTRA_EVENT_NAME);

                if (action.equalsIgnoreCase(NeuraConsts.ACTION_NEURA_EVENT)) {
                    try {
                        JSONObject result = new JSONObject();
                        result.put("event", eventName);
                        Utils.sendData(result, NeuraNest.this.callbackContext);
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                }

            }
        };
        webView.getContext().registerReceiver(r, intentFilter);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        LOG.d("action", action);
        LOG.d("data", data.toString());

        final JSONArray localData = data;
        this.callbackContext = callbackContext;

        if (action.equals("authenticate")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        authenticate(localData);
                    } catch (JSONException e) {
                        Utils.sendActionError("authenticate", e.toString(), NeuraNest.this.callbackContext);
                        Log.e(TAG, e.toString());
                    }
                }
            });

            return true;
        } else if (action.equals("subscribe")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        registerEvents(localData);
                    } catch (JSONException e) {
                        Utils.sendActionError("subscribe", e.toString(), NeuraNest.this.callbackContext);
                        Log.e(TAG, e.toString());
                    }
                }
            });

            return true;
        }

        return false;
    }

    private void authenticate(JSONArray data) throws JSONException {
        String appId = data.getString(0);
        String appSecret = data.getString(1);
        JSONArray appPermissionsArr = data.getJSONArray(2);
        String[] appPermissions = new String[appPermissionsArr.length()];
        for (int i = 0; i < appPermissionsArr.length(); i++)
            appPermissions[i] = appPermissionsArr.getString(i);

        performNeuraAuthentication(appId, appSecret, appPermissions);

        mNeuraApiClient.connect();

    }

    private void registerEvents(JSONArray data) throws JSONException{
        final Activity MainActivity = cordova.getActivity();
        Context ctx = MainActivity.getApplication().getApplicationContext();

        String accessToken = Utils.getAccessToken(ctx);

        Log.d(TAG, "accessToken: " + accessToken);

        JSONArray eventsArr = data.getJSONArray(0);
        for (int i = 0; i < eventsArr.length(); i++) {
            registerToNeuraSpecificEvents(accessToken, ctx, eventsArr.getString(i));
            Log.d(TAG, "Registered for event: " + eventsArr.getString(i));
        }
    }

    /**
     * Authenticate with Neura, where this demo app launches authorization in
     * the Neura app -- the user will see a Neura screen Request from Neura an
     * accessToken for this user for the requested permissions; the callback is
     * onActivityResult These permissions must be a subset of permissions you
     * declared on Neura's developer website, https://dev.theneura.com/#/manage
     */
    private void performNeuraAuthentication(String appId, String appSecret, String[] appPermissions) {
        final Activity MainActivity = cordova.getActivity();

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setAppId(appId);
        authenticationRequest.setAppSecret(appSecret);

        ArrayList<Permission> permissionsList = Permission.list(appPermissions);

        authenticationRequest.setPermissions(permissionsList);

        boolean neuraInstalled = NeuraAuthUtil.authenticate(MainActivity,
                NEURA_AUTHENTICATION_REQUEST_CODE, authenticationRequest);

        /**
         * check whether the user has installed the Neura app. If not, we
         * created a method for you in the Neura SDK to easily direct the user
         * to the Play Store to get the app
         */
        if (!neuraInstalled) {
            NeuraUtil.redirectToGooglePlayNeuraMeDownloadPage(MainActivity.getApplicationContext(), APP_REFERRER);
        }
    }

    /**
     * Subscribe to receive events from Neura In order to receive events, the
     * user must have first granted permission Once the app subscribes to an
     * event, Neura will continue notify the app until it calls
     * NeuraUtil.unregisterEvent() -- reseting the app won't stop event
     * notifications
     */
    private void registerToNeuraSpecificEvents(String accessToken, Context context, String eventName) {
        final Activity MainActivity = cordova.getActivity();
        final BroadcastReceiver receiver = this.receiver;

        if (!mNeuraApiClient.isConnected()) {
            Toast.makeText(
                    MainActivity,
                    "Error: You attempted to register to receive an event without being connected to Neura's service.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest.Builder(context)
                .setAccessToken(accessToken).setAction(NeuraConsts.ACTION_SUBSCRIBE)
                .setEventName(eventName).build();

        NeuraServices.SubscriptionsAPI.executeSubscriptionRequest(mNeuraApiClient,
                subscriptionRequest, new SubscriptionRequestCallbacks() {

                    @Override
                    public void onSuccess(String eventName, Bundle resultData, String identifier) {

                        Toast.makeText(MainActivity,
                                "Success: You subscribed to the event " + eventName,
                                Toast.LENGTH_LONG).show();
                        Utils.sendActionSuccess("subscribe", NeuraNest.this.callbackContext);

                    }

                    @Override
                    public void onFailure(String eventName, Bundle resultData, int errorCode) {
                        Toast.makeText(
                                MainActivity,
                                "Error: Failed to subscribe to event " + eventName
                                        + ". Error code: " + NeuraUtil.errorCodeToString(errorCode),
                                Toast.LENGTH_LONG).show();
                        Utils.sendActionError("subscribe", NeuraUtil.errorCodeToString(errorCode), NeuraNest.this.callbackContext);
                    }
                });
    }

    /**
     * Refresh the UI when the app launches
     */
    @Override
    public void onStart() {
        super.onStart();

        LOG.d(TAG, "on start");

        mNeuraApiClient.connect();
    }

    @Override
    public void onStop() {
        mNeuraApiClient.disconnect();

        LOG.d(TAG, "on stop");

        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOG.d(TAG, "on activity result");

        final Activity MainActivity = cordova.getActivity();

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEURA_AUTHENTICATION_REQUEST_CODE) {
            //if (resultCode == RESULT_OK) {
            if (resultCode != 0) {
                String accessToken = NeuraAuthUtil.extractToken(data);
                Utils.saveAccessTokenPersistent(MainActivity, accessToken);
                Utils.sendActionSuccess("authenticate", NeuraNest.this.callbackContext);
                Toast.makeText(
                        MainActivity,
                        "Authentication Succeded ", Toast.LENGTH_SHORT)
                        .show();
            } else {
                int errorCode = data.getIntExtra(NeuraConsts.EXTRA_ERROR_CODE, -1);
                Utils.sendActionError("authenticate", NeuraUtil.errorCodeToString(errorCode), NeuraNest.this.callbackContext);

                Toast.makeText(
                        MainActivity,
                        "Error: Authentication failed. Error code: "
                                + NeuraUtil.errorCodeToString(errorCode), Toast.LENGTH_SHORT)
                        .show();

                // You should now add to this method to address the errorCode
                // You can find details on the error codes in the API
                // documentation bit.ly/NeuraAPIdocs
                // For instance, if there is no wifi, suggest that the user turn
                // on the wifi.

            }
        }
    }

    private NeuraApiClient.ConnectionCallbacks mNeuraServiceConnectionCallbacks = new NeuraApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected() {
            LOG.d(TAG, "on neura connected");
            final Activity MainActivity = cordova.getActivity();


            Toast.makeText(MainActivity, "Success: Connected to Neura's service",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailedToConnect(int errorCode) {
            LOG.d(TAG, "on neura failed to connect");
            final Activity MainActivity = cordova.getActivity();
            Toast.makeText(
                    MainActivity,

                    "Error: Failed to connect to Neura's service. Error code: "
                            + NeuraUtil.errorCodeToString(errorCode), Toast.LENGTH_SHORT).show();
        }
    };

}
