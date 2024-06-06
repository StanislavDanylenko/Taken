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
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import stanislav.danylenko.taken.utils.AppPreferences;
import stanislav.danylenko.taken.utils.AppUtils;
import stanislav.danylenko.taken.service.CheckingService;
import stanislav.danylenko.taken.R;

public class MainActivity extends AppCompatActivity {

    private ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        Button start = findViewById(R.id.startButton);
        start.setOnClickListener(this::startChecking);
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
        if (AppPreferences.isPasswordExists(this)) {
            startService();
        } else {
            Toast.makeText(this, R.string.password_not_set_first_start, Toast.LENGTH_LONG).show();
        }
        startPinActivity();
    }

    private void startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, CheckingService.class)
                    .putExtra(AppUtils.DELAY, getSelectedDelay())
                    .putExtra(AppUtils.SENSITIVITY, getSensitivity())
                    .putExtra(AppUtils.STOP_ON_UNLOCK, isStopOnUnlockCheckBoxChecked()));
        } else {
            startService(new Intent(this, CheckingService.class)
                    .putExtra(AppUtils.DELAY, getSelectedDelay())
                    .putExtra(AppUtils.SENSITIVITY, getSensitivity())
                    .putExtra(AppUtils.STOP_ON_UNLOCK, isStopOnUnlockCheckBoxChecked()));
        }
    }

    public int getSelectedDelay() {
        RadioGroup radioGroup = findViewById(R.id.radios);
        int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();

        RadioButton radioButton = findViewById(checkedRadioButtonId);
        String tag = (String) radioButton.getTag();
        double value = Double.parseDouble(tag);

        return (int) (value * 1000);
    }

    private int getSensitivity() {
        SeekBar seekBar = findViewById(R.id.seekBar);
        return AppUtils.SENSITIVITY_MAX_VALUE - seekBar.getProgress();
    }

    private boolean isStopOnUnlockCheckBoxChecked() {
        CheckBox checkBox = findViewById(R.id.stop_on_unlock_chkbx);
        return checkBox.isChecked();
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