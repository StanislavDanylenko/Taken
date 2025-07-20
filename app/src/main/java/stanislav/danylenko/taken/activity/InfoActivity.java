package stanislav.danylenko.taken.activity;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import stanislav.danylenko.taken.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            TextView info = findViewById(R.id.info);
            info.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        final Button backButton = findViewById(R.id.back_btn);
        backButton.setOnClickListener(this::toMainActivity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void toMainActivity(View view) {
        try {
            getOnBackPressedDispatcher().onBackPressed();
        } catch (Exception e) {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}