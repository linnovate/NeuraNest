package net.linnovate.NeuraNest.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utils {

  private static String KEY_NEURA_ACCESS_TOKEN = "net.linnovate.NeuraNest.KEY_NEURA_ACCESS_TOKEN";

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
}
