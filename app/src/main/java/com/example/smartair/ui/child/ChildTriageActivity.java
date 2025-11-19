package com.example.smartair.ui.child;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import com.example.smartair.R;
import com.google.android.material.button.MaterialButton;

/**
 * Simple triage screen that lets a child answer red-flag questions
 * and instantly see whether to call emergency services or follow home steps.
 */
public class ChildTriageActivity extends AppCompatActivity {

    private AppCompatCheckBox speechCheck;
    private AppCompatCheckBox chestCheck;
    private AppCompatCheckBox colorCheck;
    private TextView resultTitle;
    private TextView resultMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_triage);

        speechCheck = findViewById(R.id.check_speech);
        chestCheck = findViewById(R.id.check_chest);
        colorCheck = findViewById(R.id.check_color);
        resultTitle = findViewById(R.id.result_title);
        resultMessage = findViewById(R.id.result_message);

        MaterialButton checkButton = findViewById(R.id.check_triage_button);
        checkButton.setOnClickListener(v -> evaluateFlags());
    }

    private void evaluateFlags() {
        boolean hasSpeechFlag = speechCheck.isChecked();
        boolean hasChestFlag = chestCheck.isChecked();
        boolean hasColorFlag = colorCheck.isChecked();

        boolean hasRedFlag = hasSpeechFlag || hasChestFlag || hasColorFlag;
        if (hasRedFlag) {
            resultTitle.setText(R.string.triage_result_emergency_title);
            resultMessage.setText(R.string.triage_result_emergency_message);
            setResultColors(true);
        } else {
            resultTitle.setText(R.string.triage_result_home_title);
            resultMessage.setText(R.string.triage_result_home_message);
            setResultColors(false);
        }
    }

    private void setResultColors(boolean emergency) {
        int titleColor = emergency
            ? ContextCompat.getColor(this, R.color.zone_red_text)
            : ContextCompat.getColor(this, R.color.zone_green_text);
        int messageColor = ContextCompat.getColor(this, android.R.color.black);

        resultTitle.setTextColor(titleColor);
        resultMessage.setTextColor(messageColor);
    }
}
