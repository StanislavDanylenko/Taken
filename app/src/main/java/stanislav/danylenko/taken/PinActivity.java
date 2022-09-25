package stanislav.danylenko.taken;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        this.oldPassword = findViewById(R.id.oldPassword);
        this.newPassword = findViewById(R.id.newPassword);
        this.newPasswordRepeat = findViewById(R.id.newPasswordRepeat);

        this.oldPasswordValidation = findViewById(R.id.oldPasswordValidation);
        this.newPasswordValidation = findViewById(R.id.newPasswordValidation);
        this.newPasswordRepeatValidation = findViewById(R.id.newPasswordRepeatValidation);

        this.backButton = findViewById(R.id.back_btn);

        cleanErrors();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkExistingPassword();
        checkServiceRunningPassword();
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
            validateEnteredTheSame();
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

            validateTheSame();

            if (!this.containsError) {
                AppPreferences.putData(this, AppPreferences.PSSWD, newPassword.getText().toString());
                if (this.newMode) {
                    Toast.makeText(this, "Password saved, try to start tracking again!", Toast.LENGTH_LONG).show();
                }
                toMainActivity(null);
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

    private void validateTheSame() {
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

    private void validateEnteredTheSame() {
        String pass = AppPreferences.getData(this, AppPreferences.PSSWD);
        String input = newPassword.getText().toString();

        if (!pass.equals(input)) {
            newPasswordValidation.setText(R.string.incorrect_password);
            this.containsError = true;
        }
    }

    public void toMainActivity(View view) {
        onBackPressed();
    }

    private void checkExistingPassword() {
        String existingPassword = AppPreferences.getData(this, AppPreferences.PSSWD);
        if (isEmpty(existingPassword)) {
            this.newMode = true;
            hideOldPassword();
        } else {
            this.newMode = false;
            showOldPassword();
        }
    }

    private void checkServiceRunningPassword() {
        if (isServiceRunning(CheckingService.class)) {
            this.serviceRunning = true;
            hideNewPasswordRepeat();
            hideOldPassword();
            backButton.setEnabled(false);
            newPasswordValidation.setText(R.string.enter_pin);
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