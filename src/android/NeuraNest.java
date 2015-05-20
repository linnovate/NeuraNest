package net.linnovate.NeuraNest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.neura.sdk.config.NeuraConsts;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.object.Permission;
import com.neura.sdk.service.NeuraApiClient;
import com.neura.sdk.util.NeuraAuthUtil;
import com.neura.sdk.util.NeuraUtil;

import net.linnovate.NeuraNest.util.Utils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.Logger;

public class NeuraNest extends CordovaPlugin {
    /**
     * The APP_REFERRER let's Neura know that your app directed the user to get
     * Neura Please replace "sample_app" below your app name without spaces so
     * that we know that you're driving lots of downloads. Thanks! This helps a
     * lot for business purposes!
     */
    private static final String APP_REFERRER = "NeuraNest";
    private static final int NEURA_AUTHENTICATION_REQUEST_CODE = 0;

    private NeuraApiClient mNeuraApiClient;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        // your init code here

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
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        LOG.d("action", action);
        LOG.d("data", data.toString());

        if (action.equals("authenticate")) {
            String appId = data.getString(0);
            String appSecret = data.getString(1);
            JSONArray appPermissionsArr = data.getJSONArray(2);
            String[] appPermissions = new String[appPermissionsArr.length()];
            for(int i=0;i<appPermissionsArr.length();i++)
                appPermissions[i] = appPermissionsArr.getString(i);

            performNeuraAuthentication(appId, appSecret, appPermissions);

            return true;
        }

        return false;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Activity MainActivity = cordova.getActivity();

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEURA_AUTHENTICATION_REQUEST_CODE) {
            //if (resultCode == RESULT_OK) {
            if (resultCode != 0) {
                String accessToken = NeuraAuthUtil.extractToken(data);
                Utils.saveAccessTokenPersistent(MainActivity, accessToken);
                Toast.makeText(MainActivity, "Authenticate Success!", Toast.LENGTH_SHORT)
                        .show();

                //refreshUi();
            } else {
                int errorCode = data.getIntExtra(NeuraConsts.EXTRA_ERROR_CODE, -1);

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

}
