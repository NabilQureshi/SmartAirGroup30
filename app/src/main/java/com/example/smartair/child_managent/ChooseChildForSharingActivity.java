package com.example.smartair.child_managent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.sharing.ManageSharingActivity;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChooseChildForSharingActivity extends AppCompatActivity {

    private RecyclerView recyclerChildren;
    private ChildAdapter adapter;
    private List<Child> childList = new ArrayList<>();
    private ProgressBar loadingIndicator;

    private FirebaseFirestore db;
    private String parentId;
    private String mode = "sharing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_child_for_sharing);

        db = FirebaseFirestore.getInstance();
        loadingIndicator = findViewById(R.id.loadingIndicator);
        recyclerChildren = findViewById(R.id.recyclerChildren);
        recyclerChildren.setLayoutManager(new LinearLayoutManager(this));

        // 获取传入的 parentId，如果没有就用当前登录用户
        parentId = getIntent().getStringExtra("parentId");
        if (parentId == null) {
            parentId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        String m = getIntent().getStringExtra("mode");
        if (m != null) mode = m;

        adapter = new ChildAdapter(childList, child -> {
            Intent intent;
            if ("manageChild".equals(mode)) {
                intent = new Intent(this, ManageChildActivity.class);
            } else {
                intent = new Intent(this, ManageSharingActivity.class);
            }
            intent.putExtra("childId", child.getUid());
            intent.putExtra("username", child.getUsername());
            intent.putExtra("password", child.getPassword());
            intent.putExtra("name", child.getName());
            intent.putExtra("dob", child.getDob());
            intent.putExtra("notes", child.getNotes());
            startActivity(intent);
        });
        recyclerChildren.setAdapter(adapter);

        // 实时监听父母名下的 children
        listenChildrenRealtime();
    }

    private void listenChildrenRealtime() {
        loadingIndicator.setVisibility(ProgressBar.VISIBLE);
        recyclerChildren.setVisibility(RecyclerView.GONE);

        db.collection("users")
                .document(parentId)
                .collection("children")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        loadingIndicator.setVisibility(ProgressBar.GONE);
                        recyclerChildren.setVisibility(RecyclerView.VISIBLE);

                        if (e != null) {
                            Toast.makeText(ChooseChildForSharingActivity.this,
                                    "Error loading children: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        childList.clear();

                        if (snapshots == null || snapshots.isEmpty()) {
                            Toast.makeText(ChooseChildForSharingActivity.this,
                                    "No children added yet! (ParentId=" + parentId + ")", Toast.LENGTH_LONG).show();
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        for (QueryDocumentSnapshot doc : snapshots) {
                            Child child = doc.toObject(Child.class);
                            child.setUid(doc.getId());
                            childList.add(child);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
