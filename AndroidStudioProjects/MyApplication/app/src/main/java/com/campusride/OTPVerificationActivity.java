package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.campusride.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class OTPVerificationActivity extends AppCompatActivity {

    private static final String TAG = "OTPVerification";
    private EditText otpEditText;
    private Button verifyButton;
    private ProgressBar progressBar;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get the email and password from the intent
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        
        Log.d(TAG, "Starting OTP verification for email: " + email);
        
        // Check if Firebase is properly initialized
        FirebaseAuth auth = FirebaseUtil.getAuth();
        if (auth == null) {
            Log.e(TAG, "FirebaseAuth is null - Firebase not properly initialized");
            Toast.makeText(this, "Firebase not properly initialized", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setClickListeners();
    }

    private void initViews() {
        otpEditText = findViewById(R.id.otpEditText);
        verifyButton = findViewById(R.id.verifyButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setClickListeners() {
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpEditText.getText().toString().trim();
                
                if (TextUtils.isEmpty(otp)) {
                    otpEditText.setError("OTP is required");
                    otpEditText.requestFocus();
                    return;
                }
                
                if (otp.length() != 6) {
                    otpEditText.setError("OTP must be 6 digits");
                    otpEditText.requestFocus();
                    return;
                }
                
                // For now, we'll just create the user and send email verification
                // In a real implementation, you would verify the OTP with a backend service
                createUserAndSendEmailVerification();
            }
        });
    }

    private void createUserAndSendEmailVerification() {
        Log.d(TAG, "Attempting to create user with email: " + email);
        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseAuth auth = FirebaseUtil.getAuth();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, send verification email
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "User created successfully: " + user.getUid() + " with email: " + user.getEmail());
                                // Reload the user to ensure we have the latest data
                                user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> reloadTask) {
                                        if (reloadTask.isSuccessful()) {
                                            Log.d(TAG, "User reloaded successfully");
                                            sendEmailVerification(user);
                                        } else {
                                            Log.e(TAG, "Failed to reload user", reloadTask.getException());
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(OTPVerificationActivity.this, 
                                                "Failed to reload user: " + reloadTask.getException().getMessage(), 
                                                Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Log.e(TAG, "User is null after successful creation");
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(OTPVerificationActivity.this, 
                                    "Unexpected error: User is null after creation", 
                                    Toast.LENGTH_LONG).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Log.e(TAG, "Registration failed", task.getException());
                            
                            // Provide more specific error messages
                            String errorMessage = "Registration failed";
                            if (task.getException() != null) {
                                if (task.getException() instanceof FirebaseAuthEmailException) {
                                    errorMessage = "Email error: " + task.getException().getMessage();
                                } else if (task.getException() instanceof FirebaseAuthException) {
                                    errorMessage = "Authentication error: " + task.getException().getMessage();
                                } else {
                                    errorMessage = "Registration failed: " + task.getException().getMessage();
                                }
                            }
                            
                            Toast.makeText(OTPVerificationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Registration failed with exception", e);
                        Toast.makeText(OTPVerificationActivity.this, 
                            "Registration failed: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void sendEmailVerification(FirebaseUser user) {
        Log.d(TAG, "Attempting to send verification email to: " + user.getEmail());
        
        // Check if the user's email is already verified (shouldn't happen but just in case)
        if (user.isEmailVerified()) {
            Log.d(TAG, "User's email is already verified");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(OTPVerificationActivity.this, 
                "Your email is already verified. You can now log in.", 
                Toast.LENGTH_LONG).show();
            // Redirect to login page
            startActivity(new Intent(OTPVerificationActivity.this, LoginActivity.class));
            finish();
            return;
        }
        
        // Let's add some additional checks
        Log.d(TAG, "User email: " + user.getEmail());
        Log.d(TAG, "User display name: " + user.getDisplayName());
        Log.d(TAG, "User is email verified: " + user.isEmailVerified());
        
        // Add a small delay to ensure the user is properly created
        user.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Verification email sent successfully to: " + user.getEmail());
                        Toast.makeText(OTPVerificationActivity.this, 
                            "Registration successful! Verification email sent to: " + user.getEmail() + 
                            ". Please check your inbox and spam/junk folders.", 
                            Toast.LENGTH_LONG).show();
                        // Redirect to login page
                        startActivity(new Intent(OTPVerificationActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Failed to send verification email to: " + user.getEmail(), e);
                        
                        // Try to get more specific error information
                        String errorMessage = "Failed to send verification email";
                        if (e instanceof FirebaseAuthActionCodeException) {
                            errorMessage = "Email action code error: " + e.getMessage();
                            Log.e(TAG, "Action code error details:", e);
                        } else if (e instanceof FirebaseAuthException) {
                            errorMessage = "Firebase authentication error: " + e.getMessage();
                            Log.e(TAG, "Firebase auth error details:", e);
                        } else {
                            errorMessage = "Failed to send verification email: " + e.getMessage();
                            Log.e(TAG, "General error details:", e);
                        }
                        
                        // Log the full stack trace for debugging
                        Log.e(TAG, "Full error details:", e);
                        
                        Toast.makeText(OTPVerificationActivity.this, 
                            errorMessage, 
                            Toast.LENGTH_LONG).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Email verification task completed with failure", task.getException());
                        } else {
                            Log.d(TAG, "Email verification task completed successfully");
                        }
                    }
                });
    }
}