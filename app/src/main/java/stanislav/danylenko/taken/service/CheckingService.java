package stanislav.danylenko.taken.service;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

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

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import stanislav.danylenko.taken.utils.AppUtils;
import stanislav.danylenko.taken.utils.NotificationUtils;
import stanislav.danylenko.taken.utils.ServiceUtils;

public class CheckingService extends Service implements SensorEventListener {

    private int sensitivity;

    private boolean taken = false;
    private boolean cancelled = false;
    private Context context;

    private float[] startValues = new float[3];
    private float[] currentValues;

    private long startTimestamp = Long.MAX_VALUE;
    private boolean started = false;
    private boolean unlocked = false;
    private boolean receiverRegistered = false;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private final BroadcastReceiver screenUnlockActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                unlocked = true;
                sendServiceStoppedMessage();
                stopSelf();
            }
        }
    };

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationUtils.cancelAll(this.context);

        if (flags == 0) {
            startInNewMode(intent);
        } else {
            startInRetryMode(intent);
        }

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopChecking();
        ServiceUtils.cleanAllServiceData(this.context);
    }


    private void registerSensorListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        } else {
            NotificationUtils.showErrorNotification(this.context);
            stopSelf();
        }
    }

    private void sendServiceStoppedMessage() {
        Intent intent = new Intent(AppUtils.BROADCAST_MESSAGE);
        intent.putExtra(AppUtils.BROADCAST_MESSAGE_PARAM, AppUtils.BROADCAST_MESSAGE_SERVICE_STOPPED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void startInNewMode(Intent intent) {
        ServiceUtils.cleanAllServiceData(this.context);
        ServiceUtils.saveStartTimestamp(this.context);

        int delayMillis = intent.getIntExtra(AppUtils.DELAY, AppUtils.DELAY_MILLIS_DEFAULT);
        this.sensitivity = intent.getIntExtra(AppUtils.SENSITIVITY, AppUtils.DEFAULT_SENSITIVITY);
        boolean stopOnUnlockScreen = intent.getBooleanExtra(AppUtils.STOP_ON_UNLOCK, false);

        if (stopOnUnlockScreen) {
            addOnUnlockScreenListener();
        }

        showForegroundNotification();

        Handler handler = new Handler();
        handler.postDelayed(this::delayedStartExecutionTask, delayMillis);
    }

    private void startInRetryMode(Intent intent) {
        this.sensitivity = intent.getIntExtra(AppUtils.SENSITIVITY, AppUtils.DEFAULT_SENSITIVITY);

        boolean wasTaken = ServiceUtils.loadIsTaken(this.context);
        boolean wasCancelled = ServiceUtils.loadIsCancelled(this.context);

        if (wasCancelled) {
            stopSelf();
        }

        if (wasTaken) {
            showTakenNotification();
            return;
        }

        boolean stopOnUnlockScreen = intent.getBooleanExtra(AppUtils.STOP_ON_UNLOCK, false);
        if (stopOnUnlockScreen) {
            addOnUnlockScreenListener();
        }

        showForegroundNotification();

        boolean wasStarted = ServiceUtils.loadTrackingStarted(this.context);

        if (!wasStarted) {
            int delayMillis = intent.getIntExtra(AppUtils.DELAY, AppUtils.DELAY_MILLIS_DEFAULT);
            long delayMillisNew = getNewDelayMillis(delayMillis);

            Handler handler = new Handler();
            handler.postDelayed(this::delayedStartExecutionTask, delayMillisNew);
        } else {
            float[] wasStartValues = ServiceUtils.loadStartPosition(context);
            if (wasStartValues != null) {
                this.startValues = wasStartValues;
            }

            if (!this.cancelled) {
                if (this.startValues == null && this.currentValues != null) {
                    this.startValues = this.currentValues.clone();
                    ServiceUtils.saveStartPosition(this.context, this.startValues);
                }
                this.startTimestamp = System.currentTimeMillis();
                this.started = true;

                NotificationUtils.showPositionNotification(this.context);
            }
        }
    }

    private void addOnUnlockScreenListener() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        registerReceiver(this.screenUnlockActionReceiver, filter);
        this.receiverRegistered = true;
    }

    private void showForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NotificationUtils.getRandomId(),
                NotificationUtils.getProgressNotification(context),
                FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            );
        } else {
            startForeground(
                NotificationUtils.getRandomId(),
                NotificationUtils.getProgressNotification(context)
            );
        }
    }

    private void delayedStartExecutionTask() {
        if (!this.cancelled) {
            if (this.currentValues != null) {
                this.startValues = this.currentValues.clone();
                ServiceUtils.saveStartPosition(this.context, this.startValues);
            }
            this.startTimestamp = System.currentTimeMillis();
            this.started = true;
            ServiceUtils.saveTrackingStarted(this.context, true);

            NotificationUtils.showPositionNotification(this.context);
        }
    }

    private long getNewDelayMillis(int delayMillis) {
        long wasStartTimestamp = ServiceUtils.loadStartTimestamp(this.context);
        long currentTimeMillis = System.currentTimeMillis();

        return delayMillis - (currentTimeMillis - wasStartTimestamp);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            this.currentValues = sensorEvent.values;

            if (!this.taken && !this.cancelled) {
                long currentTimeMillis = System.currentTimeMillis();
                long diff = currentTimeMillis - this.startTimestamp;

                if (this.started && diff > AppUtils.ONE_SECOND_MILLIS) {
                    if (this.startValues == null) {
                        this.startValues = this.currentValues.clone();
                        ServiceUtils.saveStartPosition(this.context, this.startValues);
                    }
                    if (taken()) {
                        showTakenNotification();
                    } else {
                        this.startTimestamp = currentTimeMillis;
                    }
                }
            }
        }
    }

    private boolean taken() {
        float[] cloneOfCurrentValues = this.currentValues.clone();
        for (int i = 0; i < this.startValues.length; i++) {
            float startValue = this.startValues[i];
            if (Math.abs(startValue - cloneOfCurrentValues[i]) > this.sensitivity) {
                return true;
            }
        }
        return false;
    }

    private void stopChecking() {
        this.cancelled = true;
        ServiceUtils.saveIsCancelled(context, true);
        this.startValues = null;
        this.startTimestamp = Long.MAX_VALUE;
        this.started = false;
        this.taken = false;
        this.executor.shutdown();
        this.executor.shutdownNow();
        NotificationUtils.cancelAll(this);

        if (this.unlocked) {
            NotificationUtils.showUnlockNotification(this.context);
        } else {
            NotificationUtils.showStoppedNotification(this.context);
        }

        if (this.receiverRegistered) {
            unregisterReceiver(this.screenUnlockActionReceiver);
        }
    }

    private void showTakenNotification() {
        this.taken = true;
        ServiceUtils.saveIsTaken(this.context, true);
        this.executor.scheduleWithFixedDelay(() -> {
            NotificationUtils.cancelAll(this.context);
            NotificationUtils.showWarningNotification(this.context);
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}