package stanislav.danylenko.taken;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationUtils {

    public static final String CHANNEL_ID = "CHANGE_ME";
    public static final String NOTE_APPLICATION_CHANNEL = "CHANGE_ME";

    private NotificationUtils() {
    }

    public static void showNotification(Context context, String body, Intent intent, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH; // check this
            NotificationChannel mChannel = new NotificationChannel(
                    CHANNEL_ID, NOTE_APPLICATION_CHANNEL, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Attention!")
                .setContentText(body)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(id, mBuilder.build());
    }

}
