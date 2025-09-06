package com.campusride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.RideRequest;

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
                        dateTextView, timeTextView, statusTextView, driverMobileTextView;
        private LinearLayout driverContactLayout;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            driverNameTextView = itemView.findViewById(R.id.driverNameTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            driverMobileTextView = itemView.findViewById(R.id.driverMobileTextView);
            driverContactLayout = itemView.findViewById(R.id.driverContactLayout);
        }

        public void bind(RideRequest request) {
            // Set status with appropriate color
            statusTextView.setText(request.getStatus());
            switch (request.getStatus()) {
                case "accepted":
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.green_500));
                    // Show driver contact information when accepted
                    if (request.getDriverMobile() != null && !request.getDriverMobile().isEmpty()) {
                        driverContactLayout.setVisibility(View.VISIBLE);
                        driverMobileTextView.setText("Mobile: " + request.getDriverMobile());
                    } else {
                        driverContactLayout.setVisibility(View.GONE);
                    }
                    break;
                case "rejected":
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.red_500));
                    driverContactLayout.setVisibility(View.GONE);
                    break;
                case "pending":
                default:
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_500));
                    driverContactLayout.setVisibility(View.GONE);
                    break;
            }
            
            // Set basic ride information
            driverNameTextView.setText(request.getDriverName() != null ? request.getDriverName() : "Unknown Driver");
            sourceTextView.setText(request.getSource() != null ? request.getSource() : "");
            destinationTextView.setText(request.getDestination() != null ? request.getDestination() : "");
            dateTextView.setText(request.getDate() != null ? request.getDate() : "");
            timeTextView.setText(request.getTime() != null ? request.getTime() : "");
        }
    }
}