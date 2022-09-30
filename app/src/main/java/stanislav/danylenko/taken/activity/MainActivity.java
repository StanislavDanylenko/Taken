package stanislav.danylenko.taken.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import stanislav.danylenko.taken.utils.AppPreferences;
import stanislav.danylenko.taken.utils.AppUtils;
import stanislav.danylenko.taken.service.CheckingService;
import stanislav.danylenko.taken.R;

public class MainActivity extends AppCompatActivity {

    public static int DELAY_MILLIS = 5_000;
    public static int SENSITIVITY = 5;
    public static boolean STOP_ON_SCREEN_UNLOCK = false;

    private SeekBar seekBar;
    private ActivityManager activityManager;

    private static void onCheckBoxClick(View v) {
        CheckBox checkBox = (CheckBox) v;
        STOP_ON_SCREEN_UNLOCK = checkBox.isChecked();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.seekBar = findViewById(R.id.seekBar);
        this.activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        addListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeInfoIcon();
        if (AppUtils.isServiceRunning(activityManager, CheckingService.class)) {
            startPinActivity();
        }
    }

    private void addListeners() {
        RadioButton instantly = findViewById(R.id.instantly);
        RadioButton fivesec = findViewById(R.id.fivesec);
        RadioButton tensec = findViewById(R.id.tensec);
        RadioButton twentysec = findViewById(R.id.twentysec);
        RadioButton thirtySec = findViewById(R.id.thirtySec);
        RadioButton sixtySec = findViewById(R.id.sixtySec);

        instantly.setOnClickListener(this::onRadioButtonClicked);
        fivesec.setOnClickListener(this::onRadioButtonClicked);
        tensec.setOnClickListener(this::onRadioButtonClicked);
        twentysec.setOnClickListener(this::onRadioButtonClicked);
        thirtySec.setOnClickListener(this::onRadioButtonClicked);
        sixtySec.setOnClickListener(this::onRadioButtonClicked);

        Button start = findViewById(R.id.startButton);
        start.setOnClickListener(this::startChecking);

        CheckBox stopOnUnlockCheckbox = findViewById(R.id.stop_on_unlock_chkbx);
        stopOnUnlockCheckbox.setOnClickListener(MainActivity::onCheckBoxClick);
    }

    private void changeInfoIcon() {
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                TextView view = findViewById(R.id.info_message);
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_baseline_info_grey_24);
                break;
        }
    }

    public void startChecking(View view) {
        if (AppPreferences.isPsswdExists(this)) {
            SENSITIVITY = AppUtils.SENSITIVITY_MAX_VALUE - this.seekBar.getProgress();
            startService();
        } else {
            Toast.makeText(this, R.string.password_not_set_first_start, Toast.LENGTH_LONG).show();
        }
        startPinActivity();
    }

    private void startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, CheckingService.class)
                    .putExtra(AppUtils.DELAY, DELAY_MILLIS)
                    .putExtra(AppUtils.SENSITIVITY, SENSITIVITY)
                    .putExtra(AppUtils.STOP_ON_UNLOCK, STOP_ON_SCREEN_UNLOCK));
        } else {
            startService(new Intent(this, CheckingService.class)
                    .putExtra(AppUtils.DELAY, DELAY_MILLIS)
                    .putExtra(AppUtils.SENSITIVITY, SENSITIVITY)
                    .putExtra(AppUtils.STOP_ON_UNLOCK, STOP_ON_SCREEN_UNLOCK));
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