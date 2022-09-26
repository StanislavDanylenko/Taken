package stanislav.danylenko.taken.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    public static final String STORAGE_NAME = "TAKEN_DATA";
    public static final String PSSWD = "PSSWD";

    public static void putData(Context context, String name, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public static String getData(Context context, String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(name, "");
    }

    public static boolean isPsswdExists(Context context) {
        return isDataExists(context, PSSWD);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
    }

    private static boolean isDataExists(Context context, String name) {
        String data = getData(context, name);
        return data != null && !"".equals(data);
    }
}
