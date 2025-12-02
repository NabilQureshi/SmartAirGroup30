package com.example.smartair.ui.parent;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.R;
import com.example.smartair.model.Child;
import com.example.smartair.util.ZoneCalculator;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Parent home activity for managing children and their Personal Best values.
 * Allows parents to view children, set/update PB, and see zone information.
 */
public class ParentHomeActivity extends AppCompatActivity {
    private LinearLayout childrenContainer;
    private TextView emptyStateText;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        childrenContainer = findViewById(R.id.children_container);
        emptyStateText = findViewById(R.id.empty_state_text);

        refreshChildrenList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChildrenList();
    }

    private void refreshChildrenList() {
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .collection("children")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Child> children = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Child child = new Child();
                        child.setId(doc.getId()); // child UID
                        child.setName(doc.getString("name"));
                        child.setDateOfBirth(doc.getString("dob"));
                        Long pbValue = doc.getLong("personalBest");
                        if (pbValue != null) child.setPersonalBest(pbValue.intValue());
                        children.add(child);
                    }
                    bindChildren(children);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load children", Toast.LENGTH_SHORT).show();
                    bindChildren(new ArrayList<>());
                });
    }

    private void bindChildren(List<Child> children) {
        childrenContainer.removeAllViews();

        if (children.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            childrenContainer.setVisibility(View.GONE);
            return;
        }

        emptyStateText.setVisibility(View.GONE);
        childrenContainer.setVisibility(View.VISIBLE);

        for (Child child : children) {
            View childCard = createChildCard(child);
            childrenContainer.addView(childCard);
        }
    }

    private View createChildCard(Child child) {
        View cardView = getLayoutInflater().inflate(R.layout.item_child_pb, childrenContainer, false);

        TextView nameText = cardView.findViewById(R.id.child_name);
        TextView pbText = cardView.findViewById(R.id.child_pb);
        TextView zoneInfoText = cardView.findViewById(R.id.zone_info);
        MaterialButton setPbButton = cardView.findViewById(R.id.set_pb_button);

        nameText.setText(child.getName());

        if (child.hasPersonalBest()) {
            pbText.setText("Personal Best: " + child.getPersonalBest() + " L/min");
            pbText.setVisibility(View.VISIBLE);
            
            // Show zone thresholds
            int[] thresholds = ZoneCalculator.getZoneThresholds(child.getPersonalBest());
            int greenThreshold = thresholds[0];
            int yellowThreshold = thresholds[1];
            String zoneInfo = String.format(
                "Green: >=%d | Yellow: %d-%d | Red: <%d",
                greenThreshold, yellowThreshold, greenThreshold - 1, yellowThreshold
            );
            zoneInfoText.setText(zoneInfo);
            zoneInfoText.setVisibility(View.VISIBLE);
        } else {
            pbText.setText("Personal Best: Not Set");
            pbText.setVisibility(View.VISIBLE);
            zoneInfoText.setVisibility(View.GONE);
        }

        setPbButton.setOnClickListener(v -> {
            SetPersonalBestDialog dialog = SetPersonalBestDialog.newInstance(child);
            dialog.show(getSupportFragmentManager(), "SetPersonalBestDialog");
        });

        return cardView;
    }
}

