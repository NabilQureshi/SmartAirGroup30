package com.example.smartair.ui.parent;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.R;
import com.example.smartair.data.ChildRepository;
import com.example.smartair.model.Child;
import com.example.smartair.util.ZoneCalculator;
import com.google.android.material.button.MaterialButton;
import java.util.List;

/**
 * Parent home activity for managing children and their Personal Best values.
 * Allows parents to view children, set/update PB, and see zone information.
 */
public class ParentHomeActivity extends AppCompatActivity {
    private ChildRepository childRepository;
    private LinearLayout childrenContainer;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        childRepository = new ChildRepository(this);

        childrenContainer = findViewById(R.id.children_container);
        emptyStateText = findViewById(R.id.empty_state_text);

        // for test create sample of child it it don't exist
        if (childRepository.getAllChildren().isEmpty()) {
            Child sampleChild = new Child(null, "Sample Child", "2010-01-01");
            childRepository.addChild(sampleChild);
        }

        refreshChildrenList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChildrenList();
    }

    private void refreshChildrenList() {
        List<Child> children = childRepository.getAllChildren();
        childrenContainer.removeAllViews();

        if (children.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            childrenContainer.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            childrenContainer.setVisibility(View.VISIBLE);

            for (Child child : children) {
                View childCard = createChildCard(child);
                childrenContainer.addView(childCard);
            }
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
                "Green: ≥%d | Yellow: %d-%d | Red: <%d",
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

