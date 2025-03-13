package stanislav.danylenko.taken.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import stanislav.danylenko.taken.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            TextView info = findViewById(R.id.info);
            info.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }
    }
}