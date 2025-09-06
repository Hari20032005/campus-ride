package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.campusride.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, ridesCountTextView;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserProfile();
        setClickListeners();
    }

    private void initViews() {
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        ridesCountTextView = findViewById(R.id.ridesCountTextView);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());
            
            // Load additional profile info from Firebase Database
            FirebaseUtil.getDatabase().getReference("users").child(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Get user data from Firebase
                            if (task.getResult().exists()) {
                                // Extract user data
                                String name = task.getResult().child("name").getValue(String.class);
                                String mobile = task.getResult().child("mobile").getValue(String.class);
                                String regNo = task.getResult().child("regNo").getValue(String.class);
                                
                                // Update UI with user data
                                nameTextView.setText(name != null ? name : "No name set");
                                
                                // You can also display mobile and regNo if you add TextViews for them
                                // For now, we'll just display name and email
                            }
                        }
                        // TODO: Load rides count from Firebase
                        ridesCountTextView.setText("0");
                    });
        }
    }

    private void setClickListeners() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUtil.getAuth().signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}