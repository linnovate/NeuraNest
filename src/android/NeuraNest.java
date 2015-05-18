package net.linnovate.NeuraNest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.neura.sdk.config.NeuraConsts;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.object.Permission;
import com.neura.sdk.util.NeuraAuthUtil;
import com.neura.sdk.util.NeuraUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class NeuraNest extends CordovaPlugin {
    private static final String appId = "12abac932242f5d71f16560aadebf9f97c23cc4be9d71e43d6748792d763a849";
    private static final String appSecret = "1ec478d9ceabe452070bfd0e868fcd4289b891225fd9e5198f5506c2bedbc34b";
    private static final String[] appPermissions = { "userWokeUp", "userArrivedHome" };
    private static final int NEURA_AUTHENTICATION_REQUEST_CODE = 1;

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
        else {
            performNeuraAuthentication();
        }
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("greet")) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;

        } else {

            return false;

        }
    }

    /**
     * Authenticate with Neura, where this demo app launches authorization in
     * the Neura app -- the user will see a Neura screen Request from Neura an
     * accessToken for this user for the requested permissions; the callback is
     * onActivityResult These permissions must be a subset of permissions you
     * declared on Neura's developer website, https://dev.theneura.com/#/manage
     */
    private void performNeuraAuthentication() {
        final Activity MainActivity = cordova.getActivity();

        String appId = this.appId;
        String appSecret = this.appSecret;

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setAppId(appId);
        authenticationRequest.setAppSecret(appSecret);

        String[] permissions = this.appPermissions;

        ArrayList<Permission> permissionsList = Permission.list(permissions);

        authenticationRequest.setPermissions(permissionsList);

        boolean neuraInstalled = NeuraAuthUtil.authenticate(MainActivity,
                NEURA_AUTHENTICATION_REQUEST_CODE, authenticationRequest);

        /**
         * check whether the user has installed the Neura app. If not, we
         * created a method for you in the Neura SDK to easily direct the user
         * to the Play Store to get the app
         */
        if (!neuraInstalled) {
            NeuraUtil.redirectToGooglePlayNeuraMeDownloadPage(MainActivity.getApplicationContext(), MainActivity.getApplicationContext().getPackageName());
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
                //Utils.saveAccessTokenPersistent(MainActivity.this, accessToken);
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
