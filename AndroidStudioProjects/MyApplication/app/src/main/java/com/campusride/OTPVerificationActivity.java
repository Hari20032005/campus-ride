package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OTPVerificationActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyButton;
    private ProgressBar progressBar;
    private String verificationId;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get the verification ID and email/password from the intent
        verificationId = getIntent().getStringExtra("verificationId");
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
                
                verifyOTP(otp);
            }
        });
    }

    private void verifyOTP(String otp) {
        progressBar.setVisibility(View.VISIBLE);
        
        // For email verification, we'll create the user directly and send email verification
        // Since Firebase doesn't have email OTP built-in, we'll send a verification email
        createUserAndSendEmailVerification();
    }

    private void createUserAndSendEmailVerification() {
        FirebaseAuth auth = FirebaseUtil.getAuth();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, send verification email
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> emailTask) {
                                                progressBar.setVisibility(View.GONE);
                                                if (emailTask.isSuccessful()) {
                                                    Toast.makeText(OTPVerificationActivity.this, 
                                                        "Registration successful. Please check your email for verification.", 
                                                        Toast.LENGTH_LONG).show();
                                                    // Redirect to login page
                                                    startActivity(new Intent(OTPVerificationActivity.this, LoginActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(OTPVerificationActivity.this, 
                                                        "Failed to send verification email.", 
                                                        Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            // If sign up fails, display a message to the user.
                            Toast.makeText(OTPVerificationActivity.this, 
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}