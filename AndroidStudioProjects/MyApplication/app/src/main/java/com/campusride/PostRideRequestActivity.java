package com.campusride;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.campusride.models.PassengerRideRequest;
import com.campusride.models.User;
import com.campusride.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.UUID;

public class PostRideRequestActivity extends AppCompatActivity {

    private static final String TAG = "PostRideRequest";
    
    private EditText sourceEditText, destinationEditText, dateEditText, timeEditText;
    private Button postRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ride_request);

        initViews();
        setClickListeners();
    }

    private void initViews() {
        sourceEditText = findViewById(R.id.sourceEditText);
        destinationEditText = findViewById(R.id.destinationEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        postRequestButton = findViewById(R.id.postRequestButton);
    }

    private void setClickListeners() {
        // For now, we'll allow manual entry of locations
        // In a full implementation, you would integrate with Google Places API
        
        sourceEditText.setFocusableInTouchMode(true);
        sourceEditText.setClickable(true);

        destinationEditText.setFocusableInTouchMode(true);
        destinationEditText.setClickable(true);

        // Add date picker functionality
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Add time picker functionality
        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        postRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postRideRequest();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                PostRideRequestActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Month is 0-based, so add 1
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        dateEditText.setText(selectedDate);
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                PostRideRequestActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                        timeEditText.setText(selectedTime);
                    }
                },
                hour, minute, true); // true for 24-hour format
        timePickerDialog.show();
    }

    private void postRideRequest() {
        String source = sourceEditText.getText().toString().trim();
        String destination = destinationEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String time = timeEditText.getText().toString().trim();

        if (source.isEmpty() || destination.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch passenger details from Firebase before creating request
        DatabaseReference userRef = FirebaseUtil.getDatabase().getReference("users").child(currentUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User passenger = snapshot.getValue(User.class);
                    if (passenger != null) {
                        // Create a new ride request
                        String requestId = UUID.randomUUID().toString();
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        
                        PassengerRideRequest rideRequest = new PassengerRideRequest(
                                requestId,
                                currentUser.getUid(),
                                passenger.getName() != null ? passenger.getName() : "Unknown Passenger",
                                passenger.getMobile() != null ? passenger.getMobile() : "",
                                passenger.getRegNo() != null ? passenger.getRegNo() : "",
                                source,
                                destination,
                                date,
                                time,
                                timestamp
                        );

                        // Save to Firebase
                        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests");
                        Log.d(TAG, "Attempting to save ride request: " + requestId);
                        
                        requestsRef.child(requestId).setValue(rideRequest)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Ride request created successfully");
                                        Toast.makeText(PostRideRequestActivity.this, "Ride request posted successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Log.e(TAG, "Failed to create ride request", task.getException());
                                        Toast.makeText(PostRideRequestActivity.this, "Failed to post ride request: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create ride request with exception", e);
                                    Toast.makeText(PostRideRequestActivity.this, "Failed to post ride request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(PostRideRequestActivity.this, "Failed to load passenger information", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PostRideRequestActivity.this, "Passenger profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostRideRequestActivity.this, "Failed to load passenger information: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}