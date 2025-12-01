package com.example.smartair.child_managent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.dashboard.DashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewChildrenActivity extends AppCompatActivity {

    private RecyclerView recyclerChildren;
    private ChildAdapter adapter;
    private List<Child> childList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_children_activity);

        recyclerChildren = findViewById(R.id.recyclerChildren);
        recyclerChildren.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化 Adapter
        adapter = new ChildAdapter(childList, child -> {
            Intent intent = new Intent(ViewChildrenActivity.this, DashboardActivity.class);
            intent.putExtra("childId", child.getUid());
            intent.putExtra("username", child.getUsername());
            intent.putExtra("password", child.getPassword());
            intent.putExtra("name", child.getName());
            intent.putExtra("dob", child.getDob());
            intent.putExtra("notes", child.getNotes());
            startActivity(intent);
        });

        recyclerChildren.setAdapter(adapter);

        loadChildren();
    }

    private void loadChildren() {
        // 优先用 Intent 传过来的 parentId，否则才用当前登录用户
        String parentId = getIntent().getStringExtra("parentId");
        if (parentId == null) {
            parentId = user.getUid();
        }




        // 监听 children collection 的所有文档
        db.collection("users")
                .document(parentId)
                .collection("children")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading children: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    childList.clear();

                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Toast.makeText(this, "No children found.", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Child child = doc.toObject(Child.class);
                        child.setUid(doc.getId()); // 保证 uid 有值
                        childList.add(child);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
