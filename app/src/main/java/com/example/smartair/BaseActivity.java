package com.example.smartair;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.auth.LoginActivity;
import com.example.smartair.utils.SharedPrefsHelper;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity {

    protected FirebaseAuth mAuth;
    protected SharedPrefsHelper prefsHelper;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        prefsHelper = new SharedPrefsHelper(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            showSignOutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> performSignOut())
                .setNegativeButton("Cancel", null)
                .show();
    }

    protected void signOut() {
        showSignOutDialog();
    }

    private void performSignOut() {
        if (mAuth != null) {
            mAuth.signOut();
        }

        if (prefsHelper != null) {
            prefsHelper.clearUserData();
        }

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}