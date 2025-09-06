package com.campusride.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";
    private static FirebaseAuth mAuth;
    private static FirebaseDatabase mDatabase;
    private static boolean isInitialized = false;

    public static synchronized void initialize(Context context) {
        if (!isInitialized) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context);
                    Log.d(TAG, "FirebaseApp initialized successfully");
                } else {
                    Log.d(TAG, "FirebaseApp already initialized");
                }
                isInitialized = true;
            } catch (Exception e) {
                Log.e(TAG, "Error initializing FirebaseApp", e);
            }
        } else {
            Log.d(TAG, "FirebaseUtil already initialized, skipping initialization");
        }
    }

    public static FirebaseAuth getAuth() {
        if (mAuth == null) {
            try {
                mAuth = FirebaseAuth.getInstance();
                Log.d(TAG, "FirebaseAuth instance created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating FirebaseAuth instance", e);
            }
        }
        return mAuth;
    }

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            try {
                mDatabase = FirebaseDatabase.getInstance();
                Log.d(TAG, "FirebaseDatabase instance created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating FirebaseDatabase instance", e);
            }
        }
        return mDatabase;
    }
}