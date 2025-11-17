package com.example.smartair;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PrePostCheckActivity extends AppCompatActivity {

    private RadioGroup rgWhen;
    private RadioGroup rgResult;
    private RatingBar ratingBar;
    private EditText etNote;
    private Button btnSubmit;
    private ListView lvHistory;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference checkRef;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listItems = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_post_check);

        rgWhen = findViewById(R.id.rgWhen);
        rgResult = findViewById(R.id.rgResult);
        ratingBar = findViewById(R.id.ratingBar);
        etNote = findViewById(R.id.etNote);
        btnSubmit = findViewById(R.id.btnSubmitCheck);
        lvHistory = findViewById(R.id.lvCheckHistory);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        checkRef = db.collection("users").document(user.getUid()).collection("prepost_checks");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        lvHistory.setAdapter(adapter);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitCheck();
            }
        });

        // 读取历史
        checkRef.orderBy("timestamp").limit(50).get().addOnSuccessListener(queryDocumentSnapshots -> {
            listItems.clear();
            for (var doc : queryDocumentSnapshots.getDocuments()) {
                String when = doc.getString("when");
                String result = doc.getString("result");
                Double rating = doc.getDouble("rating");
                String note = doc.getString("note");
                Long ts = doc.getLong("timestamp");
                String time = ts == null ? "" : sdf.format(new Date(ts));
                listItems.add(when + " | " + result + " | rating: " + (rating==null ? "-" : rating) + " | " + time + (TextUtils.isEmpty(note) ? "" : " | note:" + note));
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(PrePostCheckActivity.this, "读取历史失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void submitCheck() {
        int whenId = rgWhen.getCheckedRadioButtonId();
        int resId = rgResult.getCheckedRadioButtonId();
        if (whenId == -1 || resId == -1) {
            Toast.makeText(this, "请选择 Before/After 和 Better/Same/Worse", Toast.LENGTH_SHORT).show();
            return;
        }
        String when = ((RadioButton) findViewById(whenId)).getText().toString();
        String result = ((RadioButton) findViewById(resId)).getText().toString();
        float rating = ratingBar.getRating();
        String note = etNote.getText().toString();

        long now = System.currentTimeMillis();
        HashMap<String, Object> data = new HashMap<>();
        data.put("when", when);
        data.put("result", result);
        data.put("rating", rating);
        data.put("note", note);
        data.put("timestamp", now);

        btnSubmit.setEnabled(false);
        checkRef.add(data).addOnSuccessListener(documentReference -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(PrePostCheckActivity.this, "评估已保存", Toast.LENGTH_SHORT).show();
            String display = when + " | " + result + " | rating: " + rating + " | " + sdf.format(new Date(now));
            listItems.add(0, display);
            adapter.notifyDataSetChanged();
            ratingBar.setRating(0);
            etNote.setText("");
        }).addOnFailureListener(e -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(PrePostCheckActivity.this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
