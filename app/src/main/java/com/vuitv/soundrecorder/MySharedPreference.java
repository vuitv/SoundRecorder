package com.vuitv.soundrecorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by vuitv on 10/29/2018.
 */

public class MySharedPreference {
    private static String PREF_HIGHT_QUALITY = "pref_hight_quality";

    public static boolean getPrefHightQuality(Context context) {
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_HIGHT_QUALITY,false);
    }

    public static void setPrefHightQuality(Context context, boolean isEnabled) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_HIGHT_QUALITY, isEnabled);
        editor.apply();
    }

}
