package com.example.smartair.ui.child;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import com.example.smartair.R;
import com.example.smartair.util.PEFZoneCalculator;
import com.example.smartair.utils.SharedPrefsHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;

/**
 * Simple triage screen that lets a child answer red-flag questions
 * and see whether to call emergency services or follow home steps.
 */
public class ChildTriageActivity extends AppCompatActivity {

    private AppCompatCheckBox speechCheck;
    private AppCompatCheckBox chestCheck;
    private AppCompatCheckBox colorCheck;
    private AppCompatCheckBox rescueCheck;
    private TextInputEditText pefInput;
    private TextView resultTitle;
    private TextView resultMessage;
    private TextView resultZoneLabel;
    private TextView resultZoneValue;
    private TextView timerValue;

    private CountDownTimer triageTimer;
    private Integer personalBest;

    private FirebaseFirestore db;
    private FirebaseUser user;
    private CollectionReference triageSessions;
    private SharedPrefsHelper prefsHelper;
    private String parentId;
    private String childId;

    private static final long TRIAGE_TIMER_MS = 10 * 60 * 1000L;
    private boolean sessionStartLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_triage);

        speechCheck = findViewById(R.id.check_speech);
        chestCheck = findViewById(R.id.check_chest);
        colorCheck = findViewById(R.id.check_color);
        rescueCheck = findViewById(R.id.check_rescue);
        pefInput = findViewById(R.id.pef_input);
        resultTitle = findViewById(R.id.result_title);
        resultMessage = findViewById(R.id.result_message);
        resultZoneLabel = findViewById(R.id.result_zone_label);
        resultZoneValue = findViewById(R.id.result_zone_value);
        timerValue = findViewById(R.id.timer_value);

        MaterialButton checkButton = findViewById(R.id.check_triage_button);
        checkButton.setOnClickListener(v -> evaluateFlags());

        prefsHelper = new SharedPrefsHelper(this);
        parentId = prefsHelper.getParentId();
        childId = prefsHelper.getUserId();

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            triageSessions = db.collection("users")
                .document(user.getUid())
                .collection("triage_sessions");
        }
        loadPersonalBest();
        logTriageStart();
    }

    private void evaluateFlags() {
        boolean hasSpeechFlag = speechCheck.isChecked();
        boolean hasChestFlag = chestCheck.isChecked();
        boolean hasColorFlag = colorCheck.isChecked();
        boolean usedRescueRecently = rescueCheck.isChecked();
        Integer currentPef = parsePefInput();

        boolean hasRedFlag = hasSpeechFlag || hasChestFlag || hasColorFlag;
        if (hasRedFlag) {
            resultTitle.setText(R.string.triage_result_emergency_title);
            resultMessage.setText(R.string.triage_result_emergency_message);
            setResultColors(true);
            showZoneDetails(null);
            stopTimer();
            logTriageOutcome(true, currentPef, usedRescueRecently, hasSpeechFlag, hasChestFlag, hasColorFlag);
        } else {
            PEFZoneCalculator.ZoneResult zoneResult = PEFZoneCalculator.calculateZone(currentPef, personalBest);
            resultTitle.setText(R.string.triage_result_home_title);
            resultMessage.setText(messageForZone(zoneResult, usedRescueRecently));
            setResultColors(false);
            showZoneDetails(zoneResult);
            startTimer();
            logTriageOutcome(false, currentPef, usedRescueRecently, hasSpeechFlag, hasChestFlag, hasColorFlag);
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

    private void showZoneDetails(PEFZoneCalculator.ZoneResult zoneResult) {
        if (zoneResult == null || !zoneResult.isReady()) {
            resultZoneLabel.setVisibility(View.GONE);
            resultZoneValue.setVisibility(View.GONE);
            return;
        }

        String zoneLabel;
        switch (zoneResult.getZone()) {
            case GREEN:
                zoneLabel = getString(R.string.zone_label_green);
                break;
            case YELLOW:
                zoneLabel = getString(R.string.zone_label_yellow);
                break;
            case RED:
                zoneLabel = getString(R.string.zone_label_red);
                break;
            default:
                zoneLabel = getString(R.string.zone_label_unknown);
                break;
        }

        String zoneValue = getString(R.string.triage_zone_value_format,
            zoneLabel,
            String.format("%.0f", zoneResult.getPercentOfPersonalBest()));
        resultZoneLabel.setVisibility(TextView.VISIBLE);
        resultZoneValue.setVisibility(View.VISIBLE);
        resultZoneValue.setText(zoneValue);
    }

    private String messageForZone(PEFZoneCalculator.ZoneResult zoneResult, boolean usedRescueRecently) {
        String baseMessage;
        if (zoneResult == null || !zoneResult.isReady() || zoneResult.getZone() == PEFZoneCalculator.Zone.UNKNOWN) {
            baseMessage = getString(R.string.triage_result_zone_unknown);
        } else {
            switch (zoneResult.getZone()) {
                case GREEN:
                    baseMessage = getString(R.string.triage_result_zone_green);
                    break;
                case YELLOW:
                    baseMessage = getString(R.string.triage_result_zone_yellow);
                    break;
                case RED:
                    baseMessage = getString(R.string.triage_result_zone_red);
                    break;
                default:
                    baseMessage = getString(R.string.triage_result_zone_unknown);
                    break;
            }
        }

        if (usedRescueRecently) {
            baseMessage = baseMessage + " " + getString(R.string.triage_rescue_used_note);
        }
        return baseMessage;
    }

    private Integer parsePefInput() {
        if (pefInput == null || pefInput.getText() == null) return null;
        String text = pefInput.getText().toString().trim();
        if (text.isEmpty()) return null;
        try {
            int value = Integer.parseInt(text);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void startTimer() {
        stopTimer();
        timerValue.setText(getString(R.string.triage_timer_running, 10, 0));
        triageTimer = new CountDownTimer(TRIAGE_TIMER_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                timerValue.setText(getString(R.string.triage_timer_running, minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerValue.setText(R.string.triage_timer_finished);
            }
        };
        triageTimer.start();
    }

    private void stopTimer() {
        if (triageTimer != null) {
            triageTimer.cancel();
            triageTimer = null;
        }
        timerValue.setText(R.string.triage_timer_finished);
    }

    private void loadPersonalBest() {
        if (user == null) {
            personalBest = null;
            return;
        }
        db.collection("users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Long pbValue = doc.getLong("personalBest");
                    personalBest = pbValue != null ? pbValue.intValue() : null;
                } else {
                    personalBest = null;
                }
            })
            .addOnFailureListener(e -> personalBest = null);
    }

    private void logTriageStart() {
        if (sessionStartLogged) return;
        sessionStartLogged = true;
        if (triageSessions != null) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("timestamp", System.currentTimeMillis());
            data.put("event", "start");
            data.put("outcome", "in_progress");
            data.put("personalBest", personalBest);
            triageSessions.add(data);
        }
        sendParentNotification("triage_start", "Triage session started.", false);
    }

    private void logTriageOutcome(boolean emergency, Integer currentPef, boolean usedRescueRecently,
                                  boolean speechFlag, boolean chestFlag, boolean colorFlag) {
        if (triageSessions == null) return;
        HashMap<String, Object> data = new HashMap<>();
        data.put("timestamp", System.currentTimeMillis());
        data.put("event", emergency ? "escalation" : "check");
        data.put("outcome", emergency ? "call_emergency" : "home_steps");
        data.put("emergency", emergency);
        data.put("pef", currentPef);
        data.put("personalBest", personalBest);
        data.put("usedRescue", usedRescueRecently);
        data.put("flagSpeech", speechFlag);
        data.put("flagChest", chestFlag);
        data.put("flagColor", colorFlag);
        triageSessions.add(data);

        if (emergency) {
            sendParentNotification("triage_emergency", "Triage escalated: call emergency now.", true);
        }
    }

    private void sendParentNotification(String event, String message, boolean emergency) {
        if (parentId == null || parentId.isEmpty() || db == null) return;
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("event", event);
        payload.put("message", message);
        payload.put("childId", childId);
        payload.put("emergency", emergency);
        db.collection("users")
            .document(parentId)
            .collection("notifications")
            .add(payload);
    }
}
