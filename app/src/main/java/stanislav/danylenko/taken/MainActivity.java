package stanislav.danylenko.taken;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static int DELAY_MILLIS = 5_000;
    public static int SENSITIVITY = 5;

    private SeekBar seekBar;
    private TextView seekBarCurrentValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.seekBar = findViewById(R.id.seekBar);
        this.seekBarCurrentValue = findViewById(R.id.current_progress);

        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarCurrentValue.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isServiceRunning(CheckingService.class)) {
            startPinActivity();
        }
    }

    public void startChecking(View view) {
        if (AppPreferences.isPsswdExists(this)) {
            SENSITIVITY = this.seekBar.getProgress();

            startService();
            startPinActivity();
        } else {
            Toast.makeText(this, "You haven't specified your password, do it before tracking", Toast.LENGTH_LONG).show();
            startPinActivity();
        }
    }

    private void startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, CheckingService.class)
                    .putExtra(AppConstants.DELAY, DELAY_MILLIS)
                    .putExtra(AppConstants.SENSITIVITY, SENSITIVITY));
        } else {
            startService(new Intent(this, CheckingService.class)
                    .putExtra(AppConstants.DELAY, DELAY_MILLIS)
                    .putExtra(AppConstants.SENSITIVITY, SENSITIVITY));
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        if (checked) {
            switch (view.getId()) {
                case R.id.instantly:
                    DELAY_MILLIS = (int) (0.5 * 1000);
                    break;
                case R.id.fivesec:
                    DELAY_MILLIS = 5 * 1000;
                    break;
                case R.id.tensec:
                    DELAY_MILLIS = 10 * 1000;
                    break;
                case R.id.twentysec:
                    DELAY_MILLIS = 20 * 1000;
                    break;
                case R.id.thirtySec:
                    DELAY_MILLIS = 30 * 1000;
                    break;
            }
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startPinActivity() {
        Intent intent = new Intent(this, PinActivity.class);
        startActivity(intent);
    }

    public void startInfoActivity() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.settings:
                startPinActivity();
                break;
            case R.id.info:
                startInfoActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}