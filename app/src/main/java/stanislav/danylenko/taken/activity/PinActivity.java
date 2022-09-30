package stanislav.danylenko.taken.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;

import stanislav.danylenko.taken.utils.AppPreferences;
import stanislav.danylenko.taken.utils.AppUtils;
import stanislav.danylenko.taken.service.CheckingService;
import stanislav.danylenko.taken.R;

public class PinActivity extends AppCompatActivity {

    public static final String EMPTY_STRING = "";

    private PinView oldPassword;
    private PinView newPassword;
    private PinView newPasswordRepeat;

    private TextView oldPasswordValidation;
    private TextView newPasswordValidation;
    private TextView newPasswordRepeatValidation;

    private Button backButton;

    private boolean containsError = false;
    private boolean newMode = false;
    private boolean serviceRunning = false;

    private ActivityManager activityManager;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getIntExtra(AppUtils.BROADCAST_MESSAGE_PARAM, -1);
            if (serviceRunning && value == AppUtils.BROADCAST_MESSAGE_SERVICE_STOPPED) {
                toMainActivity(null);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        int itemWidth = getItemWidth();

        this.oldPassword = findViewById(R.id.oldPassword);
        this.newPassword = findViewById(R.id.newPassword);
        this.newPasswordRepeat = findViewById(R.id.newPasswordRepeat);

        configureItemSize(this.oldPassword, itemWidth);
        configureItemSize(this.newPassword, itemWidth);
        configureItemSize(this.newPasswordRepeat, itemWidth);

        this.oldPasswordValidation = findViewById(R.id.oldPasswordValidation);
        this.newPasswordValidation = findViewById(R.id.newPasswordValidation);
        this.newPasswordRepeatValidation = findViewById(R.id.newPasswordRepeatValidation);

        this.backButton = findViewById(R.id.back_btn);
        this.backButton.setOnClickListener(this::toMainActivity);

        Button successButton = findViewById(R.id.success_btn);
        successButton.setOnClickListener(this::confirmInput);

        this.activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        cleanErrors();

        registerBroadcastMessageReceiver();
    }

    private void registerBroadcastMessageReceiver() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter(AppUtils.BROADCAST_MESSAGE));
    }

    private int getItemWidth() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidthPx = displayMetrics.widthPixels;
        int tenPercents = screenWidthPx / 10;
        int canBeFilled = screenWidthPx - tenPercents * 2;
        return canBeFilled / 4;
    }

    private void configureItemSize(PinView view, int itemSize) {
        if (itemSize > 10) {
            view.setItemWidth(itemSize);
            view.setItemHeight(itemSize);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setAppBarText(getString(R.string.app_bar_update_pin));
        setMainPinViewText(getString(R.string.new_pin_view_new_pin));

        checkExistingPassword();
        checkServiceRunningPassword();
    }

    private void setAppBarText(String text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }

    private void setMainPinViewText(String text) {
        TextView view = findViewById(R.id.newPasswordText);
        view.setText(text);
    }

    private void cleanErrors() {
        oldPasswordValidation.setText(EMPTY_STRING);
        newPasswordValidation.setText(EMPTY_STRING);
        newPasswordRepeatValidation.setText(EMPTY_STRING);
        containsError = false;
    }

    private boolean isEmpty(String data) {
        return EMPTY_STRING.equals(data);
    }

    private boolean isNotComplete(String data) {
        return data.length() != 4;
    }


    public void confirmInput(View view) {
        cleanErrors();

        if (this.serviceRunning) {
            validateInput(newPassword, newPasswordValidation);
            validateCurrentTheSame();
            if (!this.containsError) {
                stopService(new Intent(this, CheckingService.class));
                toMainActivity(null);
            }
        } else {
            if (!this.newMode) {
                validateInput(oldPassword, oldPasswordValidation);
                validateOldTheSame();
            }

            validateInput(newPassword, newPasswordValidation);
            validateInput(newPasswordRepeat, newPasswordRepeatValidation);

            validateNewPasswordsTheSame();

            if (!this.containsError) {
                AppPreferences.putData(this, AppPreferences.PSSWD, newPassword.getText().toString());
                if (this.newMode) {
                    Toast.makeText(this, getString(R.string.password_saved), Toast.LENGTH_LONG).show();
                }
                toMainActivity(null);
            }
        }
    }


    private void validateInput(PinView view, TextView errorView) {
        String oldPass = view.getText().toString();
        if (isEmpty(oldPass)) {
            errorView.setText(R.string.set_value);
            this.containsError = true;
        } else if (isNotComplete(oldPass)) {
            errorView.setText(R.string.wrong_length);
            this.containsError = true;
        }
    }

    private void validateNewPasswordsTheSame() {
        String newPass = newPassword.getText().toString();
        String newPassRepeat = newPasswordRepeat.getText().toString();

        if (newPasswordRepeatValidation.getText().toString().equals(EMPTY_STRING)) {
            if (!isEmpty(newPass) && !isEmpty(newPassRepeat) && !newPass.equals(newPassRepeat)) {
                newPasswordRepeatValidation.setText(R.string.passwords_equals);
                this.containsError = true;
            }
        }
    }

    private void validateOldTheSame() {
        String old = AppPreferences.getData(this, AppPreferences.PSSWD);
        String input = oldPassword.getText().toString();

        if (!old.equals(input)) {
            oldPasswordValidation.setText(R.string.incorrect_existing);
            this.containsError = true;
        }
    }

    private void validateCurrentTheSame() {
        String pass = AppPreferences.getData(this, AppPreferences.PSSWD);
        String input = newPassword.getText().toString();

        if (!pass.equals(input)) {
            newPasswordValidation.setText(R.string.incorrect_password);
            this.containsError = true;
        }
    }

    public void toMainActivity(View view) {
        try {
            onBackPressed();
        } catch (Exception e) {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void checkExistingPassword() {
        String existingPassword = AppPreferences.getData(this, AppPreferences.PSSWD);
        if (isEmpty(existingPassword)) {
            this.newMode = true;
            hideOldPassword();
            setAppBarText(getString(R.string.app_bar_create_your_pin));
        } else {
            this.newMode = false;
            showOldPassword();
        }
    }

    private void checkServiceRunningPassword() {
        if (AppUtils.isServiceRunning(activityManager, CheckingService.class)) {
            this.serviceRunning = true;
            hideNewPasswordRepeat();
            hideOldPassword();
            backButton.setEnabled(false);
            newPasswordValidation.setText(R.string.enter_pin);
            setAppBarText(getString(R.string.app_bar_enter_your_pin));
            setMainPinViewText(EMPTY_STRING);
        } else {
            this.serviceRunning = false;
            backButton.setEnabled(true);
            showNewPasswordRepeat();
        }
    }

    private void hideOldPassword() {
        findViewById(R.id.oldPassword).setVisibility(View.INVISIBLE);
        findViewById(R.id.oldPasswordText).setVisibility(View.INVISIBLE);
        findViewById(R.id.oldPasswordValidation).setVisibility(View.INVISIBLE);
    }

    private void showOldPassword() {
        findViewById(R.id.oldPassword).setVisibility(View.VISIBLE);
        findViewById(R.id.oldPasswordText).setVisibility(View.VISIBLE);
        findViewById(R.id.oldPasswordValidation).setVisibility(View.VISIBLE);
    }

    private void hideNewPasswordRepeat() {
        findViewById(R.id.newPasswordRepeat).setVisibility(View.INVISIBLE);
        findViewById(R.id.newPasswordRepeatText).setVisibility(View.INVISIBLE);
        findViewById(R.id.newPasswordRepeatValidation).setVisibility(View.INVISIBLE);
        findViewById(R.id.newPasswordRepeatValidation).setVisibility(View.INVISIBLE);
    }

    private void showNewPasswordRepeat() {
        findViewById(R.id.newPasswordRepeat).setVisibility(View.VISIBLE);
        findViewById(R.id.newPasswordRepeatText).setVisibility(View.VISIBLE);
        findViewById(R.id.newPasswordRepeatValidation).setVisibility(View.VISIBLE);
    }
}