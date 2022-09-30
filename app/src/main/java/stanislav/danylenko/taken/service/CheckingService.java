package stanislav.danylenko.taken.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import stanislav.danylenko.taken.R;
import stanislav.danylenko.taken.utils.AppUtils;
import stanislav.danylenko.taken.utils.NotificationUtils;

public class CheckingService extends Service implements SensorEventListener {

    private int delayMillis;
    private int sensitivity;

    private boolean taken = false;
    private boolean cancelled = false;
    private Context context;

    private float[] startValues = new float[3];
    private float[] currentValues;

    private long startTimestamp = Long.MAX_VALUE;
    private boolean started = false;
    private boolean unlocked = false;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

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
    }

    private void addOnUnlockScreenListener() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        BroadcastReceiver actionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    unlocked = true;
                    stopSelf();
                }
            }
        };
        registerReceiver(actionReceiver, filter);
    }

    private void registerSensorListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        } else {
            NotificationUtils.showErrorNotification(context);
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.delayMillis = intent.getIntExtra(AppUtils.DELAY, AppUtils.DELAY_MILLIS_DEFAULT);
        this.sensitivity = intent.getIntExtra(AppUtils.SENSITIVITY, AppUtils.DEFAULT_SENSITIVITY);
        boolean stopOnUnlockScreen = intent.getBooleanExtra(AppUtils.STOP_ON_UNLOCK, false);

        if (stopOnUnlockScreen) {
            addOnUnlockScreenListener();
        }

        showForegroundNotification();

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (!cancelled) {
                startValues = currentValues.clone();
                startTimestamp = System.currentTimeMillis();
                started = true;
                NotificationUtils.showPositionNotification(context);
            }
        }, delayMillis);

        return super.onStartCommand(intent, flags, startId);
    }

    private void showForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(
                    NotificationUtils.getRandomId(),
                    NotificationUtils.getProgressNotification(context));
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
        cancelled = true;
        executor.shutdown();
        executor.shutdownNow();
        NotificationUtils.cancelAll(this);
        if (unlocked) {
            NotificationUtils.showUnlockNotification(context);
        } else {
            NotificationUtils.showStoppedNotification(context);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        currentValues = sensorEvent.values;

        if (!taken && !cancelled) {
            long currentTimeMillis = System.currentTimeMillis();
            long diff = currentTimeMillis - startTimestamp;

            if (diff > delayMillis) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    if (started) {
                        if (taken()) {
                            taken = true;
                            NotificationUtils.cancelAll(this);
                            executor.scheduleAtFixedRate(() -> {
                                NotificationUtils.cancelAll(context);
                                NotificationUtils.showWarningNotification(context);
                            }, 0, 2, TimeUnit.SECONDS);
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