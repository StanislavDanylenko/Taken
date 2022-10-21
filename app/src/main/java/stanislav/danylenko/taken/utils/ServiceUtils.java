package stanislav.danylenko.taken.utils;

import android.content.Context;
import android.util.Log;

public class ServiceUtils {

    public static void readAllPrefs(Context context) {
        boolean hTaken = AppPreferences.getBooleanData(context, AppUtils.S_TAKEN);
        Log.w("TAKEN INFO", "S_TAKEN = " + hTaken);

        boolean hCancelled = AppPreferences.getBooleanData(context, AppUtils.S_CANCELLED);
        Log.w("TAKEN INFO", "S_CANCELLED = " + hCancelled);

        boolean hStarted = AppPreferences.getBooleanData(context, AppUtils.S_TRACKING_STARTED);
        Log.w("TAKEN INFO", "S_TRACKING_STARTED = " + hStarted);

        long hStartTimestamp = AppPreferences.getLongData(context, AppUtils.S_START_TIMESTAMP);
        Log.w("TAKEN INFO", "S_START_TIMESTAMP = " + hStartTimestamp);

        float hx = AppPreferences.getFloatData(context, AppUtils.S_START_VALUE_X);
        float hy = AppPreferences.getFloatData(context, AppUtils.S_START_VALUE_Y);
        float hz = AppPreferences.getFloatData(context, AppUtils.S_START_VALUE_Z);
        Log.w("TAKEN INFO", "S_START_VALUE_X = " + hx);
        Log.w("TAKEN INFO", "S_START_VALUE_Y = " + hy);
        Log.w("TAKEN INFO", "S_START_VALUE_Z = " + hz);
    }


    public static void saveStartTimestamp(Context context) {
        AppPreferences.putLongData(context, AppUtils.S_START_TIMESTAMP, System.currentTimeMillis());
    }

    public static long loadStartTimestamp(Context context) {
        return AppPreferences.getLongData(context, AppUtils.S_START_TIMESTAMP);
    }


    public static void saveTrackingStarted(Context context, boolean isStarted) {
        AppPreferences.putBooleanData(context, AppUtils.S_TRACKING_STARTED, isStarted);
    }

    public static boolean loadTrackingStarted(Context context) {
        return AppPreferences.getBooleanData(context, AppUtils.S_TRACKING_STARTED);
    }


    public static void saveIsTaken(Context context, boolean isTaken) {
        AppPreferences.putBooleanData(context, AppUtils.S_TAKEN, isTaken);
    }

    public static boolean loadIsTaken(Context context) {
        return AppPreferences.getBooleanData(context, AppUtils.S_TAKEN);
    }


    public static void saveIsCancelled(Context context, boolean IsCancelled) {
        AppPreferences.putBooleanData(context, AppUtils.S_CANCELLED, IsCancelled);
    }

    public static boolean loadIsCancelled(Context context) {
        return AppPreferences.getBooleanData(context, AppUtils.S_CANCELLED);
    }


    public static void saveStartPosition(Context context, float[] startValues) {
        AppPreferences.putFloatData(context, AppUtils.S_START_VALUE_X, startValues[0]);
        AppPreferences.putFloatData(context, AppUtils.S_START_VALUE_Y, startValues[1]);
        AppPreferences.putFloatData(context, AppUtils.S_START_VALUE_Z, startValues[2]);
    }

    public static float[] loadStartPosition(Context context) {
        float hx = AppPreferences.getFloatData(context, AppUtils.S_START_VALUE_X);
        float hy = AppPreferences.getFloatData(context, AppUtils.S_START_VALUE_Y);
        float hz = AppPreferences.getFloatData(context, AppUtils.S_START_VALUE_Z);

        if (hx != Float.MIN_VALUE) {
            return new float[]{hx, hy, hz};
        } else {
            return null;
        }
    }


    public static void cleanAllServiceData(Context context) {
        AppPreferences.cleanAllDataByKeys(context, AppUtils.SERVICE_PREFS_LIST);
    }

}
