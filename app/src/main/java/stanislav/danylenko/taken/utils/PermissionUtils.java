package stanislav.danylenko.taken.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import stanislav.danylenko.taken.R;

public final class PermissionUtils {

    public static final int PERMISSION_REQUEST_CODE = 112;
    public static final String PERMISSION_REQUEST_CODE_STR = "112";

    private PermissionUtils() {}

    public static void getNotificationPermission(Activity activity){
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(activity,
                        new String[] {Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        } catch (Exception e){
            Log.i("s.d.Taken", "GetNotificationPermission failed");
        }
    }

    public static void processAnswer(Activity activity, int requestCode, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("s.d.Taken", "GetNotificationPermission granted");
            } else {
                Toast.makeText(activity, R.string.permission_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
