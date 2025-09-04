package com.campusride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.RideRequest;
import com.campusride.utils.FirebaseUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PassengerRideRequestAdapter extends RecyclerView.Adapter<PassengerRideRequestAdapter.RequestViewHolder> {
    private List<RideRequest> requests;

    public PassengerRideRequestAdapter(List<RideRequest> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_passenger_ride_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RideRequest request = requests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateRequests(List<RideRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView driverNameTextView, sourceTextView, destinationTextView, 
                        dateTextView, timeTextView, statusTextView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            driverNameTextView = itemView.findViewById(R.id.driverNameTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }

        public void bind(RideRequest request) {
            // Set status with appropriate color
            statusTextView.setText(request.getStatus());
            switch (request.getStatus()) {
                case "accepted":
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.green_500));
                    break;
                case "rejected":
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.red_500));
                    break;
                case "pending":
                default:
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_500));
                    break;
            }
            
            // Fetch ride details from Firebase
            DatabaseReference rideRef = FirebaseUtil.getDatabase().getReference("rides").child(request.getRideId());
            rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get ride details
                        String driverName = dataSnapshot.child("driverName").getValue(String.class);
                        String source = dataSnapshot.child("source").getValue(String.class);
                        String destination = dataSnapshot.child("destination").getValue(String.class);
                        String date = dataSnapshot.child("date").getValue(String.class);
                        String time = dataSnapshot.child("time").getValue(String.class);
                        
                        // Update UI on main thread
                        itemView.post(() -> {
                            if (driverName != null) driverNameTextView.setText(driverName);
                            if (source != null) sourceTextView.setText(source);
                            if (destination != null) destinationTextView.setText(destination);
                            if (date != null) dateTextView.setText(date);
                            if (time != null) timeTextView.setText(time);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }
}