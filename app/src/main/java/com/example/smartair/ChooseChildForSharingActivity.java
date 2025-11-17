package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChooseChildForSharingActivity extends AppCompatActivity {

    private ListView listViewChildren;
    private FirebaseFirestore db;
    private String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_child_for_sharing);

        listViewChildren = findViewById(R.id.listViewChildren);
        db = FirebaseFirestore.getInstance();
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadChildren();
    }

    private void loadChildren() {
        db.collection("parents")
                .document(parentId)
                .collection("children")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Toast.makeText(this, "No children added yet!", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    List<String> childNames = new ArrayList<>();
                    List<String> childIds = new ArrayList<>();

                    snapshot.forEach(doc -> {
                        childNames.add(doc.getString("name"));
                        childIds.add(doc.getId());
                    });

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_list_item_1, childNames);

                    listViewChildren.setAdapter(adapter);

                    listViewChildren.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedChildId = childIds.get(position);

                        Intent intent = new Intent(this, ManageSharingActivity.class);
                        intent.putExtra("childId", selectedChildId);
                        startActivity(intent);
                    });
                });
    }
}
