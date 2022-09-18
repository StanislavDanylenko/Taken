package stanislav.danylenko.taken;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class CheckingService extends Service implements SensorEventListener {

    private int delayMillis;
    private int sensitivity;

    private boolean taken = false;
    private Context context;

    private float[] startValues = new float[3];
    private float[] currentValues;

    private long startTimestamp = Long.MAX_VALUE;
    private boolean started = false;

    private final Timer timer = new Timer();
    private TimerTask notifyWarningTask;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;

        registerSensorListener();

        notifyWarningTask = new TimerTask() {
            @Override
            public void run() {
                NotificationUtils.cancelAll(context);
                NotificationUtils.showWarningNotification(context);
            }
        };
    }

    private void registerSensorListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        } else {
            Toast.makeText(this, "Needed sensors not found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.delayMillis = intent.getIntExtra(AppConstants.DELAY, AppConstants.DELAY_MILLIS_DEFAULT);
        this.sensitivity = intent.getIntExtra(AppConstants.SENSITIVITY, AppConstants.DEFAULT_SENSITIVITY);
        int channelId = intent.getIntExtra(AppConstants.CHANNEL_ID, -1);

        showForegroundNotification(channelId);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            startValues = currentValues.clone();
            startTimestamp = System.currentTimeMillis();
            started = true;
            NotificationUtils.showPositionNotification(context);
        }, delayMillis);

        return super.onStartCommand(intent, flags, startId);
    }

    private void showForegroundNotification(int channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(channelId, NotificationUtils.getProgressNotification(context));
        } else {
            NotificationUtils.showProgressNotification(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopChecking();
    }

    private boolean taken() {
        float[] cloneOfCurrentValues = currentValues.clone();
        for (int i = 0; i < startValues.length; i++) {
            float startValue = startValues[i];
            if (Math.abs(startValue - cloneOfCurrentValues[i]) > sensitivity) {
                return true;
            }
        }
        return false;
    }

    public void stopChecking() {
        startValues = null;
        startTimestamp = Long.MAX_VALUE;
        started = false;
        taken = false;
        timer.cancel();
        NotificationUtils.cancelAll(this);
        NotificationUtils.showStoppedNotification(context);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        currentValues = sensorEvent.values;

        if (!taken) {
            long currentTimeMillis = System.currentTimeMillis();
            long diff = currentTimeMillis - startTimestamp;

            if (diff > delayMillis) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    if (started) {
                        if (taken()) {
                            taken = true;
                            NotificationUtils.cancelAll(this);
                            timer.scheduleAtFixedRate(notifyWarningTask, 0, 2_000);
                        }
                    }
                    startTimestamp = currentTimeMillis;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}