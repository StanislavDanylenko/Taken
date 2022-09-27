package stanislav.danylenko.taken.utils;

import android.app.ActivityManager;

public final class AppUtils {

    private AppUtils() {}

    public static final String DELAY = "delay";
    public static final String SENSITIVITY = "sensitivity";

    public static final int DELAY_MILLIS_DEFAULT = 5_000;
    public static final int DEFAULT_SENSITIVITY = 5;

    public static final int SENSITIVITY_MAX_VALUE = 11;

    public static boolean isServiceRunning(ActivityManager manager, Class<?> serviceClass) {
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
