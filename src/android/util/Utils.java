package net.linnovate.NeuraNest.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

  private static String KEY_NEURA_ACCESS_TOKEN = "net.linnovate.NeuraNest.KEY_NEURA_ACCESS_TOKEN";
  private final static String TAG = "Utils";

  /**
   * The app will clear the token locally
   *
   * @param context
   */
  public static void clearToken(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs.edit().clear().commit();
  }

  /**
   * Save the accessToken persistently
   *
   * @param context
   * @param accessToken
   */
  public static void saveAccessTokenPersistent(Context context, String accessToken) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs.edit().putString(KEY_NEURA_ACCESS_TOKEN, accessToken).apply();
  }

  public static String getAccessToken(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getString(KEY_NEURA_ACCESS_TOKEN, null);
  }

  public static void sendData(JSONObject args, CallbackContext callbackContext) {
    try {
      JSONArray names = args.names();
      String name;
      JSONObject parameter = new JSONObject();
      for (int i = 0; i < args.length(); i++) {
        name = names.getString(i);
        parameter.put(name, args.getString(name));
      }


      // callback.success(parameter);
      PluginResult result = new PluginResult(PluginResult.Status.OK, parameter);
      result.setKeepCallback(true);
      callbackContext.sendPluginResult(result);

    } catch (JSONException e) {
      Log.e(TAG, e.toString());
    }
  }
}
