package com.example.smartair.badges_system;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;


public class BadgeActivity extends AppCompatActivity {

    private RecyclerView rvBadges;
    private BadgeAdapter adapter;
    private String uid;
    private String newBadgeId; // 刚解锁的徽章，用于高亮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String newBadge = getIntent().getStringExtra("newBadge");
        // newBadge 可能是 "badge_2"，也可能是 null（如果不是从解锁来的）

        setContentView(R.layout.activity_badge);

        rvBadges = findViewById(R.id.rvBadges);
        rvBadges.setLayoutManager(new LinearLayoutManager(this));

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 接收从 medicinelog 传来的新徽章 ID
        newBadgeId = getIntent().getStringExtra("newBadge");

        loadBadges();
    }

    private void loadBadges() {
        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("badges")
                .get()
                .addOnSuccessListener(snap -> {

                    List<BadgeModel> list = new ArrayList<>();

                    for (int i = 1; i <= 5; i++) {
                        String id = "badge_" + i;
                        DocumentSnapshot doc = snap.getDocuments()
                                .stream()
                                .filter(d -> d.getId().equals(id))
                                .findFirst()
                                .orElse(null);

                        boolean achieved = doc != null && doc.getBoolean("achieved") != null
                                && doc.getBoolean("achieved");

                        String time = (doc != null && doc.getTimestamp("firstAchieved") != null)
                                ? doc.getTimestamp("firstAchieved").toDate().toString()
                                : "——";

                        String description = doc != null && doc.getString("description") != null
                                ? doc.getString("description")
                                : "继续努力，相信你可以做到的！";

                        list.add(new BadgeModel(
                                id,
                                "徽章 " + i,
                                achieved,
                                time,
                                description,
                                id.equals(newBadgeId)   // 是否高亮
                        ));
                    }

                    adapter = new BadgeAdapter(list, this);
                    rvBadges.setAdapter(adapter);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "加载徽章失败：" + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}
