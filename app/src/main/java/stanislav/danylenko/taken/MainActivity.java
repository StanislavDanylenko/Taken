package stanislav.danylenko.taken;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static int DELAY_MILLIS = 5_000;
    private TextView x;
    private TextView y;
    private TextView z;

    private float[] startValues = new float[3];
    private float[] currentValues;

    private long startTimestamp;
    private boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        x = findViewById(R.id.x);
        y = findViewById(R.id.y);
        z = findViewById(R.id.z);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        } else {
            Toast.makeText(this, "Service not found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long currentTimeMillis = System.currentTimeMillis();
        long diff = currentTimeMillis - startTimestamp;
        if (diff > 1_000) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = sensorEvent.values;
                currentValues = values;
                x.setText("X = " + values[0]);
                y.setText("Y = " + values[1]);
                z.setText("Z = " + values[2]);
                if (started) {
                    if (taken()) {
                        Toast.makeText(this, "TAKEN", Toast.LENGTH_SHORT).show();
                    }
                }
                startTimestamp = currentTimeMillis;
            }
        }
    }

    private boolean taken() {
        float[] cloneOfCurrentValues = currentValues.clone();
        for (int i = 0; i < startValues.length; i++) {
            float startValue = startValues[i];
            if (Math.abs(startValue - cloneOfCurrentValues[i]) > 5) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    public void startChecking(View view) {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            startValues = currentValues.clone();
            startTimestamp = System.currentTimeMillis();
            started = true;
        }, DELAY_MILLIS);
        Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
    }

    public void stopChecking(View view) {
        startValues = null;
        startTimestamp = Long.MIN_VALUE;
        started = false;
        Toast.makeText(this, "Stoped", Toast.LENGTH_SHORT).show();
    }

    public void onRadioButtonClicked(View view) {
        // если переключатель отмечен
        boolean checked = ((RadioButton) view).isChecked();
        // Получаем нажатый переключатель
        switch(view.getId()) {
            case R.id.fivesec:
                if (checked){
                    DELAY_MILLIS = 5 * 1000;
                }
                break;
            case R.id.tensec:
                if (checked){
                    DELAY_MILLIS = 10 * 1000;
                }
                break;
            case R.id.twentysec:
                if (checked){
                    DELAY_MILLIS = 20 * 1000;
                }
                break;
            case R.id.thirtySec:
                if (checked){
                    DELAY_MILLIS = 30 * 1000;
                }
                break;
        }
    }


}