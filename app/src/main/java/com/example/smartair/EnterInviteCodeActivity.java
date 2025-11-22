package com.example.smartair;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EnterInviteCodeActivity extends AppCompatActivity {

    private EditText editInviteCode;
    private Button btnRedeem;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_invite_code);

        db = FirebaseFirestore.getInstance();

        editInviteCode = findViewById(R.id.editInviteCode);
        btnRedeem = findViewById(R.id.btnRedeem);

        btnRedeem.setOnClickListener(v -> {
            String code = editInviteCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Enter the 6-digit code", Toast.LENGTH_SHORT).show();
                return;
            }
            redeemCode(code);
        });
    }

    private void redeemCode(String code) {
        String providerId = FirebaseAuth.getInstance().getUid();
        if (providerId == null) {
            Toast.makeText(this, "You must be signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("inviteCodes").document(code)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Invalid invite code", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Boolean used = doc.getBoolean("used");
                    Date expires = doc.getDate("expires");

                    if (used != null && used) {
                        Toast.makeText(this, "Code already used", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (expires != null && expires.before(new Date())) {
                        Toast.makeText(this, "Invite code expired", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String parentId = doc.getString("parentId");
                    String childId = doc.getString("childId");

                    if (parentId == null || childId == null) {
                        Toast.makeText(this, "Invalid invite data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    linkProvider(providerId, parentId, childId);

                    db.collection("inviteCodes").document(code).update("used", true);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error validating invite code", Toast.LENGTH_SHORT).show()
                );
    }

    private void linkProvider(String providerId, String parentId, String childId) {

        Map<String, Object> linkData = new HashMap<>();
        linkData.put("parentId", parentId);
        linkData.put("childId", childId);
        linkData.put("linkedAt", new Date());

        // Provider → child link (permanent)
        db.collection("providers")
                .document(providerId)
                .collection("linkedChildren")
                .document(childId)
                .set(linkData);

        // Parent → provider link (for revoke later)
        db.collection("parents")
                .document(parentId)
                .collection("children")
                .document(childId)
                .collection("linkedProviders")
                .document(providerId)
                .set(linkData)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Child linked successfully!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(a ->
                        Toast.makeText(this, "Failed to link child", Toast.LENGTH_LONG).show());
    }
}
