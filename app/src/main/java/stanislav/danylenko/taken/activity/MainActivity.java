package stanislav.danylenko.taken.activity;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import stanislav.danylenko.taken.utils.AppPreferences;
import stanislav.danylenko.taken.utils.AppUtils;
import stanislav.danylenko.taken.service.CheckingService;
import stanislav.danylenko.taken.R;

public class MainActivity extends AppCompatActivity {

    public static int DELAY_MILLIS = 5_000;
    public static int SENSITIVITY = 5;

    private SeekBar seekBar;
    private ActivityManager activityManager;

    private final int SENSITIVITY_MAX_VALUE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.seekBar = findViewById(R.id.seekBar);
        this.activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppUtils.isServiceRunning(activityManager, CheckingService.class)) {
            startPinActivity();
        }
    }

    public void startChecking(View view) {
        if (AppPreferences.isPsswdExists(this)) {
            SENSITIVITY = SENSITIVITY_MAX_VALUE - this.seekBar.getProgress();

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
                    .putExtra(AppUtils.DELAY, DELAY_MILLIS)
                    .putExtra(AppUtils.SENSITIVITY, SENSITIVITY));
        } else {
            startService(new Intent(this, CheckingService.class)
                    .putExtra(AppUtils.DELAY, DELAY_MILLIS)
                    .putExtra(AppUtils.SENSITIVITY, SENSITIVITY));
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
                case R.id.sixtySec:
                    DELAY_MILLIS = 60 * 1000;
                    break;
            }
        }
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

    @SuppressLint("NonConstantResourceId")
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