package com.example.smartair.badges_system;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.utils.SharedPrefsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BadgeActivity extends AppCompatActivity {

    private RecyclerView rvBadges;
    private BadgeAdapter adapter;
    private String uid;
    private String newBadgeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge);

        newBadgeId = getIntent().getStringExtra("newBadge");
        rvBadges = findViewById(R.id.rvBadges);
        rvBadges.setLayoutManager(new LinearLayoutManager(this));

        // Support child username-only logins by falling back to saved userId in SharedPrefs
        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            uid = prefs.getUserId();
        }

        if (uid == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBadges();
    }

    private void loadBadges() {
        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("badges")
                .get()
                .addOnSuccessListener(snap -> {

                    List<BadgeModel> list = new ArrayList<>();

                    list.add(buildBadge("badge_1", "First perfect controller week", snap));
                    list.add(buildBadge("badge_2", "10 high-quality technique sessions", snap));
                    list.add(buildBadge("badge_3", "Low rescue month", snap));

                    adapter = new BadgeAdapter(list, this);
                    rvBadges.setAdapter(adapter);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load badges: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private BadgeModel buildBadge(String id, String title, QuerySnapshot snap) {
        DocumentSnapshot doc = null;
        for (DocumentSnapshot d : snap.getDocuments()) {
            if (d.getId().equals(id)) {
                doc = d;
                break;
            }
        }

        boolean achieved = doc != null && doc.getBoolean("achieved") != null
                && doc.getBoolean("achieved");

        String time = (doc != null && doc.getTimestamp("firstAchieved") != null)
                ? doc.getTimestamp("firstAchieved").toDate().toString()
                : "Not achieved yet";

        String description = doc != null && doc.getString("description") != null
                ? doc.getString("description")
                : "";

        return new BadgeModel(
                id,
                title,
                achieved,
                time,
                description,
                id.equals(newBadgeId)
        );
    }
}
