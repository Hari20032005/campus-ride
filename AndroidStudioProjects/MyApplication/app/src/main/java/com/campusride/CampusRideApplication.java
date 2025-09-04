package com.campusride;

import android.app.Application;
import android.util.Log;

import com.campusride.utils.FirebaseUtil;

public class CampusRideApplication extends Application {
    private static final String TAG = "CampusRideApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CampusRideApplication.onCreate() called");
        
        // Initialize Firebase
        FirebaseUtil.initialize(this);
        
        Log.d(TAG, "Firebase initialized in Application.onCreate()");
    }
}