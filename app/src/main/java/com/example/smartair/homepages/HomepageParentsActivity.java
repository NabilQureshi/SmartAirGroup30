package com.example.smartair.homepages;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.smartair.BaseActivity;
import com.example.smartair.R;
import com.example.smartair.checkin.HistorySelectChildActivity;
import com.example.smartair.checkin.ParentChildSelectActivity;
import com.example.smartair.child_managent.AddChildActivity;
import com.example.smartair.child_managent.ChooseChildForSharingActivity;
import com.example.smartair.child_managent.ManageChildActivity;
import com.example.smartair.child_managent.ViewChildrenActivity;
import com.example.smartair.inventory.InventoryActivity;
import com.example.smartair.ui.parent.ParentHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.firebase.firestore.DocumentChange;

public class HomepageParentsActivity extends BaseActivity {

    private Button btnAddChild;
    private Button btnViewChildren;
    private Button btnManageChild;
    private Button btnManageSharing;
    private Button btnManagePB;
    private Button btnInventory;
    private Button btnSignOut;
    private Button btnChildCheckIn;
    private Button btnSymptomHistory;
    private TextView textGreeting;
    private String parentId;
    private ListenerRegistration notificationListener;
    private final Map<String, ListenerRegistration> rescueListeners = new HashMap<>();
    private final Map<String, Long> lastRescueAlert = new HashMap<>();
    private final Map<String, String> childNameCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_parents); // ����s,�-��,���?�-؄�

        textGreeting = findViewById(R.id.textGreeting);
        // �^?�<�O-�O%�'r
        btnAddChild = findViewById(R.id.btnAddChild);
        btnViewChildren = findViewById(R.id.btnViewChildren);
        btnManageChild = findViewById(R.id.btnManageChild);
        btnManageSharing = findViewById(R.id.btnManageSharing);
        btnManagePB = findViewById(R.id.btnManagePB);
        btnInventory = findViewById(R.id.btnInventory);
        btnChildCheckIn = findViewById(R.id.btnChildCheckIn);
        btnSymptomHistory = findViewById(R.id.btnSymptomHistory);
        btnSignOut = findViewById(R.id.btnSignOut);


        // �S�����^�_?�??�--
        loadParentName();

        if (parentId == null) parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listenForTriageNotifications();
        setupRescueListeners();

        // �r_��r�,1�د��<��
        btnAddChild.setOnClickListener(v ->
                startActivity(new Intent(this, AddChildActivity.class)));

        //btnViewChildren.setOnClickListener(v ->
        //        startActivity(new Intent(this, ViewChildrenActivity.class)));
        btnViewChildren.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewChildrenActivity.class);
            intent.putExtra("mode", "viewchild");
            intent.putExtra("parentId", parentId);
            startActivity(intent);
        });


        btnManageChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChooseChildForSharingActivity.class);
            intent.putExtra("mode", "manageChild");
            intent.putExtra("parentId", parentId);
            startActivity(intent);
        });

        btnManageSharing.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChooseChildForSharingActivity.class);
            intent.putExtra("mode", "sharing");
            intent.putExtra("parentId", parentId);
            startActivity(intent);
        });

        btnManagePB.setOnClickListener(v ->
                startActivity(new Intent(this, ParentHomeActivity.class)));
        btnInventory.setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));
        btnChildCheckIn.setOnClickListener(v ->
                startActivity(new Intent(this, ParentChildSelectActivity.class)));
        btnSymptomHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistorySelectChildActivity.class))
        );
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

    private void listenForTriageNotifications() {
        if (parentId == null || parentId.isEmpty()) return;
        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .document(parentId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1);

        notificationListener = query.addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null) return;
            for (DocumentChange change : snapshot.getDocumentChanges()) {
                if (change.getType() == DocumentChange.Type.ADDED) {
                    String message = change.getDocument().getString("message");
                    Boolean emergency = change.getDocument().getBoolean("emergency");
                    String childName = change.getDocument().getString("childName");
                    String childId = change.getDocument().getString("childId");
                    if ((childName == null || childName.isEmpty()) && childId != null) {
                        childName = childNameCache.get(childId);
                    }
                    showTriageAlert(message, emergency != null && emergency, childName);
                }
            }
        });
    }

    private void showTriageAlert(String message, boolean emergency, String childName) {
        String title = emergency ? "Triage escalation" : "Triage alert";
        String body = (message == null || message.isEmpty())
                ? "New triage update from your child."
                : message;
        if (childName != null && !childName.isEmpty()) {
            body = body.replaceAll("child\\s+[A-Za-z0-9_-]+", "child " + childName);
            if (!body.toLowerCase().contains(childName.toLowerCase())) {
                body = body + " (child: " + childName + ")";
            }
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(body)
                .setPositiveButton("OK", null)
                .show();
    }

    private void setupRescueListeners() {
        if (parentId == null || parentId.isEmpty()) return;
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(parentId)
                .collection("children")
                .get()
                .addOnSuccessListener(childrenSnapshot -> {
                    for (DocumentSnapshot childDoc : childrenSnapshot) {
                        String childUid = childDoc.getId();
                        String childName = childDoc.getString("name");
                        if (childUid != null && childName != null) {
                            childNameCache.put(childUid, childName);
                        }
                        if (childUid != null && !childUid.isEmpty() && !rescueListeners.containsKey(childUid)) {
                            attachRescueListener(childUid);
                        }
                    }
                });
    }

    private void attachRescueListener(String childUid) {
        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .document(childUid)
                .collection("medicine_logs")
                .whereEqualTo("type", "Rescue")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(25);

        ListenerRegistration reg = query.addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null) return;
            List<Long> recentTimestamps = new ArrayList<>();
            long now = System.currentTimeMillis();
            long threshold = now - 3 * 60 * 60 * 1000L; // last 3 hours
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Long ts = doc.getLong("timestamp");
                if (ts != null && ts >= threshold) {
                    recentTimestamps.add(ts);
                }
            }
            if (recentTimestamps.size() >= 3) {
                Long lastAlertTime = lastRescueAlert.get(childUid);
                // alert again only if 5 minutes have passed since last alert for this child and prevent spam
                if (lastAlertTime == null || now - lastAlertTime > 5 * 60 * 1000L) {
                    lastRescueAlert.put(childUid, now);
                    String childName = childNameCache.get(childUid);
                    String displayName = childName != null ? childName : childUid;
                    String message = "3+ rescue uses in last 3 hours for child " + displayName;
                    showTriageAlert(message, true, childName);
                    logRescueAlert(childUid, childName, message);
                }
            }
        });
        rescueListeners.put(childUid, reg);
    }

    private void logRescueAlert(String childUid, String message, String childName) {
        if (parentId == null || parentId.isEmpty()) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("event", "rescue_repeat");
        payload.put("message", message);
        payload.put("childId", childUid);
        if (childName != null) payload.put("childName", childName);
        payload.put("emergency", true);
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(parentId)
                .collection("notifications")
                .add(payload);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
        for (ListenerRegistration reg : rescueListeners.values()) {
            if (reg != null) reg.remove();
        }
        rescueListeners.clear();
    }

}
