package stanislav.danylenko.taken;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static int DELAY_MILLIS = 5_000;
    public static int SENSITIVITY = 5;

    private LinearLayout info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info = findViewById(R.id.info);
    }

    public void startChecking(View view) {
        SeekBar seekBar = findViewById(R.id.seekBar);
        SENSITIVITY = seekBar.getProgress();

        startService(new Intent(this, CheckingService.class)
                .putExtra(AppConstants.DELAY, DELAY_MILLIS)
                .putExtra(AppConstants.SENSITIVITY, SENSITIVITY));
    }


    public void stopChecking(View view) {
        stopService(new Intent(this, CheckingService.class));
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

    public void showHideInfo(View view) {
        int visibility = info.getVisibility();
        if (visibility == View.INVISIBLE) {
            info.setVisibility(View.VISIBLE);
        } else {
            info.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}