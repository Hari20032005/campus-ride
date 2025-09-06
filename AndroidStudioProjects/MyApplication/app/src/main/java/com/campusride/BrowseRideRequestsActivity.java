package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.PassengerRideRequest;
import com.campusride.models.User;
import com.campusride.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BrowseRideRequestsActivity extends AppCompatActivity {

    private static final String TAG = "BrowseRideRequests";
    
    private RecyclerView rideRequestsRecyclerView;
    private BrowseRideRequestsAdapter rideRequestAdapter;
    private List<PassengerRideRequest> pendingRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_ride_requests);

        initViews();
        setupRecyclerView();
        loadPendingRideRequests();
    }

    private void initViews() {
        rideRequestsRecyclerView = findViewById(R.id.rideRequestsRecyclerView);
    }

    private void setupRecyclerView() {
        pendingRequests = new ArrayList<>();
        rideRequestAdapter = new BrowseRideRequestsAdapter(pendingRequests, new BrowseRideRequestsAdapter.OnRequestActionListener() {
            @Override
            public void onAcceptRequest(PassengerRideRequest request) {
                showAcceptConfirmation(request);
            }
            
            @Override
            public void onViewDetails(PassengerRideRequest request) {
                viewRideRequestDetails(request);
            }
        });
        rideRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rideRequestsRecyclerView.setAdapter(rideRequestAdapter);
    }
    
    private void loadPendingRideRequests() {
        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests");
        Log.d(TAG, "Attempting to load pending ride requests");
        Log.d(TAG, "Database reference: " + requestsRef.toString());
        
        // Listen for all requests, then filter on client side
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Data loaded successfully, count: " + dataSnapshot.getChildrenCount());
                pendingRequests.clear();
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    PassengerRideRequest request = requestSnapshot.getValue(PassengerRideRequest.class);
                    if (request != null) {
                        // Only show pending requests or requests accepted by current driver
                        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
                        if (currentUser != null) {
                            if ("pending".equals(request.getStatus())) {
                                // Show all pending requests
                                pendingRequests.add(request);
                                Log.d(TAG, "Added pending request: " + request.getRequestId() + " from " + request.getPassengerName());
                            } else if ("accepted".equals(request.getStatus()) && 
                                      currentUser.getUid().equals(request.getDriverId())) {
                                // Show requests accepted by current driver
                                pendingRequests.add(request);
                                Log.d(TAG, "Added accepted request (by current driver): " + request.getRequestId());
                            }
                        }
                    }
                }
                // Sort by creation time (newest first)
                pendingRequests.sort((r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                rideRequestAdapter.updateRequests(pendingRequests);
                Log.d(TAG, "Loaded " + pendingRequests.size() + " ride requests (pending + accepted by current driver)");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading ride requests", databaseError.toException());
                String errorMessage = databaseError.getMessage();
                if (databaseError.getCode() == DatabaseError.PERMISSION_DENIED) {
                    errorMessage = "Permission denied. Please check Firebase security rules.";
                }
                Toast.makeText(BrowseRideRequestsActivity.this, "Failed to load ride requests: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void showAcceptConfirmation(PassengerRideRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Accept Ride Request")
                .setMessage("Do you want to accept this ride request from " + request.getPassengerName() + 
                           " from " + request.getSource() + " to " + request.getDestination() + "?")
                .setPositiveButton("Accept", (dialog, which) -> acceptRideRequest(request))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void acceptRideRequest(PassengerRideRequest request) {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to accept ride requests", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if this request is already accepted by this driver
        if ("accepted".equals(request.getStatus()) && currentUser.getUid().equals(request.getDriverId())) {
            // Request is already accepted by this driver, just show details
            viewRideRequestDetails(request);
            return;
        }
        
        // Check if this request is already accepted by another driver
        if ("accepted".equals(request.getStatus())) {
            // Request is already accepted by another driver
            Toast.makeText(this, "This ride request has already been accepted by another driver", Toast.LENGTH_SHORT).show();
            // Refresh the list to remove this request
            pendingRequests.remove(request);
            rideRequestAdapter.updateRequests(pendingRequests);
            return;
        }
        
        // Fetch driver details from Firebase
        DatabaseReference userRef = FirebaseUtil.getDatabase().getReference("users").child(currentUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User driver = snapshot.getValue(User.class);
                    if (driver != null) {
                        // Update the ride request with driver information
                        request.setStatus("accepted");
                        request.setDriverId(currentUser.getUid());
                        request.setDriverName(driver.getName() != null ? driver.getName() : "Unknown Driver");
                        request.setDriverMobile(driver.getMobile() != null ? driver.getMobile() : "");
                        request.setAcceptedAt(System.currentTimeMillis());
                        
                        // Save to Firebase
                        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests");
                        requestsRef.child(request.getRequestId()).setValue(request)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(BrowseRideRequestsActivity.this, 
                                            "Ride request accepted successfully! Contact details shared with passenger.", Toast.LENGTH_LONG).show();
                                        
                                        // Update the request in the list to show "View Details" button
                                        for (int i = 0; i < pendingRequests.size(); i++) {
                                            if (pendingRequests.get(i).getRequestId().equals(request.getRequestId())) {
                                                pendingRequests.set(i, request);
                                                break;
                                            }
                                        }
                                        rideRequestAdapter.updateRequests(pendingRequests);
                                    } else {
                                        Toast.makeText(BrowseRideRequestsActivity.this, 
                                            "Failed to accept ride request: " + task.getException().getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(BrowseRideRequestsActivity.this, "Failed to load driver information", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BrowseRideRequestsActivity.this, "Driver profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BrowseRideRequestsActivity.this, "Failed to load driver information: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void viewRideRequestDetails(PassengerRideRequest request) {
        Intent intent = new Intent(this, DriverRideRequestDetailsActivity.class);
        intent.putExtra("REQUEST_ID", request.getRequestId());
        startActivity(intent);
    }
}