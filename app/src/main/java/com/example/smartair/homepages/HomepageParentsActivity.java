package com.example.smartair.homepages;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.smartair.BaseActivity;
import com.example.smartair.R;
import com.example.smartair.child_managent.AddChildActivity;
import com.example.smartair.child_managent.ChooseChildForSharingActivity;
import com.example.smartair.child_managent.ManageChildActivity;
import com.example.smartair.child_managent.ViewChildrenActivity;
import com.example.smartair.inventory.InventoryActivity;
import com.example.smartair.ui.parent.ParentHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomepageParentsActivity extends BaseActivity {

    private Button btnAddChild;
    private Button btnViewChildren;
    private Button btnManageChild;
    private Button btnManageSharing;
    private Button btnManagePB;
    private Button btnInventory;


    private Button btnSignOut;
    private TextView textGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_parents);

        textGreeting = findViewById(R.id.textGreeting);
        btnAddChild = findViewById(R.id.btnAddChild);
        btnViewChildren = findViewById(R.id.btnViewChildren);
        btnManageChild = findViewById(R.id.btnManageChild);
        btnManageSharing = findViewById(R.id.btnManageSharing);
        btnManagePB = findViewById(R.id.btnManagePB);
        btnInventory = findViewById(R.id.btnInventory);
        btnSignOut = findViewById(R.id.btnSignOut);

        loadParentName();

        // 设置点击事件
        btnAddChild.setOnClickListener(v ->
                startActivity(new Intent(this, AddChildActivity.class)));

        btnViewChildren.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.smartair.child_managent.ViewChildrenActivity.class)));

        btnManageChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChooseChildForSharingActivity.class);
            intent.putExtra("mode", "manageChild");
            startActivity(intent);
        });

        btnManageSharing.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChooseChildForSharingActivity.class);
            intent.putExtra("mode", "sharing");
            startActivity(intent);
        });

        btnManagePB.setOnClickListener(v ->
                startActivity(new Intent(this, ParentHomeActivity.class)));

        btnInventory.setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));

        btnSignOut.setOnClickListener(v -> signOut());
    }

    private void loadParentName() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");

                        if (name != null && !name.isEmpty()) {
                            textGreeting.setText("Hello, " + name + "!");
                        } else {
                            String email = doc.getString("email");
                            textGreeting.setText("Hello, " + (email != null ? email : "Parent") + "!");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    textGreeting.setText("Hello, Parent!");
                });
    }
}