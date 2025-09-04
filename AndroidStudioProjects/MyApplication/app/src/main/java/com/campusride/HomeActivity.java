package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends BaseActivity {

    private Button postRideButton, searchRideButton, rideRequestsButton, myRideRequestsButton, profileButton;
    private RecyclerView recentRidesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        FirebaseUtil.initialize(this);

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setClickListeners();
        setupRecyclerView();
    }

    private void initViews() {
        postRideButton = findViewById(R.id.postRideButton);
        searchRideButton = findViewById(R.id.searchRideButton);
        rideRequestsButton = findViewById(R.id.rideRequestsButton);
        myRideRequestsButton = findViewById(R.id.myRideRequestsButton);
        profileButton = findViewById(R.id.profileButton);
        recentRidesRecyclerView = findViewById(R.id.recentRidesRecyclerView);
    }

    private void setClickListeners() {
        postRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, RideCreationActivity.class));
            }
        });

        searchRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, RideSearchActivity.class));
            }
        });

        rideRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, RideRequestsActivity.class));
            }
        });

        myRideRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, PassengerRideRequestsActivity.class));
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
        });
    }

    private void setupRecyclerView() {
        recentRidesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Add adapter for recent rides
    }
}