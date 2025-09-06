package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.Ride;
import com.campusride.models.RideRequest;
import com.campusride.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";
    
    private Button postRideButton, searchRideButton, rideRequestsButton, myRideRequestsButton, profileButton;
    private RecyclerView recentRidesRecyclerView;
    private RideAdapter recentRidesAdapter;
    private List<Ride> recentRidesList;
    
    private ValueEventListener driverRidesListener;
    private ValueEventListener passengerRidesListener;

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
        loadRecentRides();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners to prevent memory leaks
        if (driverRidesListener != null) {
            DatabaseReference ridesRef = FirebaseUtil.getDatabase().getReference("rides");
            ridesRef.removeEventListener(driverRidesListener);
        }
        if (passengerRidesListener != null) {
            DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("ride_requests");
            requestsRef.removeEventListener(passengerRidesListener);
        }
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
        recentRidesList = new ArrayList<>();
        recentRidesAdapter = new RideAdapter(recentRidesList, ride -> {
            // Handle ride click if needed
        });
        recentRidesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentRidesRecyclerView.setAdapter(recentRidesAdapter);
    }
    
    private void loadRecentRides() {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        loadDriverRides(currentUser);
        loadPassengerRides(currentUser);
    }
    
    private void loadDriverRides(FirebaseUser currentUser) {
        DatabaseReference ridesRef = FirebaseUtil.getDatabase().getReference("rides");
        driverRidesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear existing driver rides but keep passenger rides
                List<Ride> updatedRideList = new ArrayList<>();
                // First, keep all passenger rides (those not driven by current user)
                for (Ride ride : recentRidesList) {
                    if (!currentUser.getUid().equals(ride.getDriverId())) {
                        updatedRideList.add(ride);
                    }
                }
                
                // Add completed rides as driver (limit to 5 most recent)
                List<Ride> driverRides = new ArrayList<>();
                for (DataSnapshot rideSnapshot : dataSnapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride != null && "completed".equals(ride.getStatus()) && 
                        currentUser.getUid().equals(ride.getDriverId())) {
                        driverRides.add(ride);
                    }
                }
                
                // Sort by completion time (newest first) and limit to 5
                driverRides.sort((r1, r2) -> Long.compare(r2.getCompletedAt(), r1.getCompletedAt()));
                int limit = Math.min(driverRides.size(), 5);
                for (int i = 0; i < limit; i++) {
                    updatedRideList.add(driverRides.get(i));
                }
                
                // Update the list and sort by completion time (newest first)
                recentRidesList.clear();
                recentRidesList.addAll(updatedRideList);
                recentRidesList.sort((r1, r2) -> Long.compare(r2.getCompletedAt(), r1.getCompletedAt()));
                recentRidesAdapter.updateRides(recentRidesList);
                Log.d(TAG, "Loaded " + recentRidesList.size() + " recent rides as driver");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading driver rides", databaseError.toException());
                Toast.makeText(HomeActivity.this, "Failed to load driver rides: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };
        
        ridesRef.orderByChild("driverId").equalTo(currentUser.getUid())
                .addValueEventListener(driverRidesListener);
    }
    
    private void loadPassengerRides(FirebaseUser currentUser) {
        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("ride_requests");
        passengerRidesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Process all accepted requests for this passenger
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    RideRequest request = requestSnapshot.getValue(RideRequest.class);
                    if (request != null && "accepted".equals(request.getStatus()) && 
                        currentUser.getUid().equals(request.getPassengerId())) {
                        // Load the ride details for this accepted request
                        loadRideForRequest(request);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading passenger rides", databaseError.toException());
                Toast.makeText(HomeActivity.this, "Failed to load passenger rides: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };
        
        requestsRef.orderByChild("passengerId").equalTo(currentUser.getUid())
                .addValueEventListener(passengerRidesListener);
    }
    
    private void loadRideForRequest(RideRequest request) {
        DatabaseReference rideRef = FirebaseUtil.getDatabase().getReference("rides").child(request.getRideId());
        rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Ride ride = dataSnapshot.getValue(Ride.class);
                    if (ride != null && "completed".equals(ride.getStatus())) {
                        // Check if this ride is not already in the list
                        boolean exists = false;
                        for (Ride existingRide : recentRidesList) {
                            if (existingRide.getRideId().equals(ride.getRideId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            recentRidesList.add(ride);
                            // Sort by completion time (newest first) and limit to 5 most recent
                            recentRidesList.sort((r1, r2) -> Long.compare(r2.getCompletedAt(), r1.getCompletedAt()));
                            if (recentRidesList.size() > 5) {
                                recentRidesList.subList(5, recentRidesList.size()).clear();
                            }
                            recentRidesAdapter.updateRides(recentRidesList);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading ride for request", databaseError.toException());
            }
        });
    }
}