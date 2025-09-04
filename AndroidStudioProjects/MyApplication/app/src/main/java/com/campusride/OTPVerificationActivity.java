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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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
                                Log.d(TAG, "User created successfully: " + user.getUid());
                                sendEmailVerification(user);
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Log.e(TAG, "Registration failed", task.getException());
                            // If sign up fails, display a message to the user.
                            Toast.makeText(OTPVerificationActivity.this, 
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> emailTask) {
                        progressBar.setVisibility(View.GONE);
                        if (emailTask.isSuccessful()) {
                            Log.d(TAG, "Verification email sent to: " + user.getEmail());
                            Toast.makeText(OTPVerificationActivity.this, 
                                "Registration successful. Please check your email for verification. Check your spam/junk folder if you don't see it.", 
                                Toast.LENGTH_LONG).show();
                            // Redirect to login page
                            startActivity(new Intent(OTPVerificationActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Log.e(TAG, "Failed to send verification email", emailTask.getException());
                            Toast.makeText(OTPVerificationActivity.this, 
                                "Failed to send verification email. Please check the app logs for more details.", 
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}