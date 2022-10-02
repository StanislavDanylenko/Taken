package stanislav.danylenko.taken.utils;

import android.app.ActivityManager;

import java.util.Arrays;
import java.util.List;

public final class AppUtils {

    private AppUtils() {}

    public static final String DELAY = "delay";
    public static final String SENSITIVITY = "sensitivity";
    public static final String STOP_ON_UNLOCK = "stop_on_unlock";

    public static final String BROADCAST_MESSAGE = "taken-service-action";
    public static final String BROADCAST_MESSAGE_PARAM = "action";
    public static final int BROADCAST_MESSAGE_SERVICE_STOPPED = 1;

    public static final int DELAY_MILLIS_DEFAULT = 5_000;
    public static final int DEFAULT_SENSITIVITY = 5;

    public static final int SENSITIVITY_MAX_VALUE = 11;

    public static final int ONE_SECOND_MILLIS = 1000;

    public static final String S_TRACKING_STARTED = "S_TRACKING_STARTED";
    public static final String S_START_TIMESTAMP = "S_START_TIMESTAMP";
    public static final String S_CANCELLED = "S_CANCELLED";
    public static final String S_TAKEN = "S_TAKEN";
    public static final String S_START_VALUE_X = "S_START_VALUE_X";
    public static final String S_START_VALUE_Y = "S_START_VALUE_Y";
    public static final String S_START_VALUE_Z = "S_START_VALUE_Z";

    public static final List<String> SERVICE_PREFS_LIST = Arrays.asList(
            S_TRACKING_STARTED, S_START_TIMESTAMP, S_CANCELLED, S_TAKEN,
            S_START_VALUE_X, S_START_VALUE_Y, S_START_VALUE_Z);

    public static boolean isServiceRunning(ActivityManager manager, Class<?> serviceClass) {
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
