package stanislav.danylenko.taken.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

public class AppPreferences {

    public static final String STORAGE_NAME = "TAKEN_DATA";
    public static final String PSSWD = "PSSWD";

    public static void putStringData(Context context, String name, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public static String getStringData(Context context, String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(name, "");
    }

    public static void putLongData(Context context, String name, Long value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(name, value);
        editor.apply();
    }

    public static Long getLongData(Context context, String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getLong(name, Long.MIN_VALUE);
    }

    public static void putFloatData(Context context, String name, Float value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(name, value);
        editor.apply();
    }

    public static Float getFloatData(Context context, String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getFloat(name, Float.MIN_VALUE);
    }

    public static void putBooleanData(Context context, String name, Boolean value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    public static Boolean getBooleanData(Context context, String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getBoolean(name, false);
    }

    public static boolean isPsswdExists(Context context) {
        String data = getStringData(context, PSSWD);
        return data != null && !"".equals(data);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
    }

    public static void cleanAllDataByKeys(Context context, List<String> keys) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.apply();
    }
}
