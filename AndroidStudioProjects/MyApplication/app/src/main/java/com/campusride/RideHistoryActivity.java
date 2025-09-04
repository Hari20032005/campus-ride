package com.campusride;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RideHistoryActivity extends AppCompatActivity {

    private RecyclerView rideHistoryRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        initViews();
        setupRecyclerView();
    }

    private void initViews() {
        rideHistoryRecyclerView = findViewById(R.id.rideHistoryRecyclerView);
    }

    private void setupRecyclerView() {
        rideHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Add adapter for ride history
    }
}