package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.campusride.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton, forgotPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if user is already logged in and email is verified
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Log.d(TAG, "User is already logged in and email is verified");
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        } else if (currentUser != null) {
            Log.d(TAG, "User is logged in but email is not verified");
        }

        initViews();
        setClickListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
    }

    private void setClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        // Removed the college email restriction
        // if (!email.endsWith("@vitstudent.ac.in")) {
        //     emailEditText.setError("Please use your college email (@vitstudent.ac.in)");
        //     emailEditText.requestFocus();
        //     return;
        // }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        // Sign in with Firebase
        FirebaseAuth auth = FirebaseUtil.getAuth();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = auth.getCurrentUser();
                            Log.d(TAG, "Sign in successful for user: " + (user != null ? user.getUid() : "null"));
                            if (user != null && user.isEmailVerified()) {
                                Log.d(TAG, "Email is verified, proceeding to home screen");
                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                Log.d(TAG, "Email is not verified, prompting user to verify");
                                Toast.makeText(LoginActivity.this, "Please verify your email address before logging in. Check your spam/junk folder for the verification email.", Toast.LENGTH_LONG).show();
                                auth.signOut(); // Sign out the user
                            }
                        } else {
                            Log.e(TAG, "Login failed", task.getException());
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        // Removed the college email restriction
        // if (!email.endsWith("@vitstudent.ac.in")) {
        //     emailEditText.setError("Please use your college email (@vitstudent.ac.in)");
        //     emailEditText.requestFocus();
        //     return;
        // }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        // Instead of directly creating user, redirect to OTP verification activity
        Intent intent = new Intent(LoginActivity.this, OTPVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);
    }
    
    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }
        
        // Removed the college email restriction
        // if (!email.endsWith("@vitstudent.ac.in")) {
        //     emailEditText.setError("Please use your college email (@vitstudent.ac.in)");
        //     emailEditText.requestFocus();
        //     return;
        // }
        
        FirebaseAuth auth = FirebaseUtil.getAuth();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password reset email sent to: " + email);
                            Toast.makeText(LoginActivity.this, 
                                "Password reset email sent to: " + email + 
                                ". Please check your inbox and spam/junk folders.", 
                                Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Failed to send password reset email to: " + email, task.getException());
                            Toast.makeText(LoginActivity.this, 
                                "Failed to send password reset email: " + task.getException().getMessage(), 
                                Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Exception when sending password reset email to: " + email, e);
                    Toast.makeText(LoginActivity.this, 
                        "Exception when sending password reset email: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
    }
}