package stanislav.danylenko.taken.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.util.Random;

import stanislav.danylenko.taken.R;
import stanislav.danylenko.taken.activity.MainActivity;

public final class NotificationUtils {

    public static final String CHANNEL_ID = "TakenNotificationChannel";
    public static final String CHANNEL_NAME = "TakenNotificationChannel";

    private static final Random RANDOM = new Random();

    private NotificationUtils() {}

    public static void showWarningNotification(Context context) {
        showNotification(context, context.getString(R.string.attention), context.getString(R.string.someone_taken), false);
    }

    public static void showErrorNotification(Context context) {
        showNotification(context, context.getString(R.string.tracking_error), context.getString(R.string.sensor_not_found), true);
    }

    public static void showPositionNotification(Context context) {
        showNotification(context, context.getString(R.string.position_saved), context.getString(R.string.dont_worry), true);
    }

    public static void showStoppedNotification(Context context) {
        showNotification(context, context.getString(R.string.tracking_stopped), context.getString(R.string.password_accepted), true);
    }

    public static void showUnlockNotification(Context context) {
        showNotification(context, context.getString(R.string.tracking_stopped), context.getString(R.string.screen_unlocked), true);
    }

    public static Notification getProgressNotification(Context context) {
        return buildNotification(context, null, null, false);
    }

    public static Notification buildNotification(Context context, String title, String body, boolean hidable) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(context.getString(R.string.channed_description));
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = getDefaultBuilder(context, title, body, hidable);
        if (body == null) {
            mBuilder = getProgressBuilder(context);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(new Intent(context, MainActivity.class));
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );
        mBuilder.setContentIntent(resultPendingIntent);

        return mBuilder.build();
    }

    public static void showNotification(Context context, String title, String body, boolean hidable) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = buildNotification(context, title, body, hidable);
        notificationManager.notify(getRandomId(),notification);
    }

    @NonNull
    private static NotificationCompat.Builder getDefaultBuilder(Context context, String title, String body, boolean hidable) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_warning_24)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon))
                .setContentTitle(title)
                .setContentText(body)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(!hidable)
                .setAutoCancel(hidable);
    }

    @NonNull
    private static NotificationCompat.Builder getProgressBuilder(Context context) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_sync_24)
                .setContentTitle(context.getString(R.string.tarcking_state_progress))
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setProgress(0, 0, true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setAutoCancel(false);
    }

    public static void cancelAll(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static int getRandomId() {
        return RANDOM.nextInt();
    }

}
