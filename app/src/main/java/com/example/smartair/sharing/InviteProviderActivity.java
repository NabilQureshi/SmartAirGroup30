package com.example.smartair.sharing;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InviteProviderActivity extends AppCompatActivity {

    private Button btnGenerateCode, btnRevoke;
    private TextView textInviteCode, textHint, textStatus;

    private FirebaseFirestore db;
    private String parentId;
    private String childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_provider);

        db = FirebaseFirestore.getInstance();


        btnGenerateCode = findViewById(R.id.btnGenerateCode);
        btnRevoke = findViewById(R.id.btnRevoke);
        textInviteCode = findViewById(R.id.textInviteCode);
        textHint = findViewById(R.id.textHint);
        textStatus = findViewById(R.id.textStatusLabel);

        parentId = FirebaseAuth.getInstance().getUid();
        childId = getIntent().getStringExtra("CHILD_ID");

        if (parentId == null || childId == null) {
            Toast.makeText(this, "Error: Missing IDs", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnGenerateCode.setOnClickListener(v -> generateInviteCode());
        btnRevoke.setOnClickListener(v -> revokeInviteCode());

        checkForExistingCode();
    }


    private void checkForExistingCode() {
        // Check the CHILD'S profile (Local)
        db.collection("parents").document(parentId)
                .collection("children").document(childId)
                .collection("invite").document("current")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String code = doc.getString("code");
                        validateGlobalCode(code);
                    } else {
                        updateUI(false, null);
                    }
                });
    }

    private void validateGlobalCode(String code) {
        if (code == null) return;

        db.collection("inviteCodes").document(code).get()
                .addOnSuccessListener(globalDoc -> {
                    if (globalDoc.exists()) {
                        Date expires = globalDoc.getDate("expires");

                        // "Valid 7 Days" Requirement check
                        if (expires != null && expires.after(new Date())) {
                            showInviteCode(code);
                        } else {
                            deleteLocalInvite();
                        }
                    } else {
                        deleteLocalInvite();
                    }
                });
    }

    private void deleteLocalInvite() {
        db.collection("parents").document(parentId)
                .collection("children").document(childId)
                .collection("invite").document("current")
                .delete();

        updateUI(false, null);
    }

    private void generateInviteCode() {
        revokeInviteCodeInternal(() -> {

            String code = String.valueOf(100000 + new Random().nextInt(900000));

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, 7);
            Date expiryDate = c.getTime();

            Map<String, Object> data = new HashMap<>();
            data.put("parentId", parentId);
            data.put("childId", childId);
            data.put("used", false);
            data.put("expires", expiryDate);

            db.collection("inviteCodes").document(code)
                    .set(data)
                    .addOnSuccessListener(a -> {

                        Map<String, Object> local = new HashMap<>();
                        local.put("code", code);

                        db.collection("parents").document(parentId)
                                .collection("children").document(childId)
                                .collection("invite").document("current")
                                .set(local)
                                .addOnSuccessListener(v -> showInviteCode(code));
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to generate code", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void revokeInviteCode() {
        revokeInviteCodeInternal(null);
        Toast.makeText(this, "Access revoked.", Toast.LENGTH_SHORT).show();
    }

    private void revokeInviteCodeInternal(Runnable onFinished) {
        db.collection("parents").document(parentId)
                .collection("children").document(childId)
                .collection("invite").document("current")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String code = doc.getString("code");

                        if (code != null) {
                            db.collection("inviteCodes").document(code).delete();
                        }
                        doc.getReference().delete();

                        updateUI(false, null);
                    }

                    if (onFinished != null) onFinished.run();
                });
    }

    private void showInviteCode(String code) {
        updateUI(true, code);
    }

    private void updateUI(boolean isActive, String code) {
        if (isActive) {
            textInviteCode.setText(code);
            textInviteCode.setVisibility(View.VISIBLE);
            textHint.setVisibility(View.VISIBLE);

            if (btnRevoke != null) btnRevoke.setVisibility(View.VISIBLE);
            btnGenerateCode.setText("Regenerate Code");
            if (textStatus != null) {
                textStatus.setText("Status: Active (Shared)");
                textStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            }
        } else {
            textInviteCode.setVisibility(View.GONE);
            textHint.setVisibility(View.GONE);

            if (btnRevoke != null) btnRevoke.setVisibility(View.GONE);
            btnGenerateCode.setText("Generate Invite Code");

            if (textStatus != null) {
                textStatus.setText("Status: Not Shared");
                textStatus.setTextColor(Color.GRAY);
            }
        }
    }
}