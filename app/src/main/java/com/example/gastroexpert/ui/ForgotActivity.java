package com.example.gastroexpert.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gastroexpert.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ForgotActivity extends AppCompatActivity {

    private static final String TAG = "ForgotActivity";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private EditText etUsername, etPassword, etConfirmPassword;
    private ImageView showPassBtn, showConfirmPassBtn;
    private ProgressBar progressBar;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot);

        // Initialize UI components
        initializeUIComponents();

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("users");

        // Set up listeners for buttons
        setupListeners();
    }

    private void initializeUIComponents() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etPasswords);
        showPassBtn = findViewById(R.id.show_pass_btn);
        showConfirmPassBtn = findViewById(R.id.show_confpass_btn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        Button btnChangePassword = findViewById(R.id.btnGanti);
        TextView tvLogin = findViewById(R.id.ingat);

        // Listener for changing password
        btnChangePassword.setOnClickListener(view -> handleChangePassword());
        tvLogin.setOnClickListener(view -> navigateToSignInActivity());

        // Show/hide password and confirm password
        showPassBtn.setOnClickListener(view -> togglePasswordVisibility(etPassword, showPassBtn));
        showConfirmPassBtn.setOnClickListener(view -> togglePasswordVisibility(etConfirmPassword, showConfirmPassBtn));
    }

    private void togglePasswordVisibility(EditText editText, ImageView imageView) {
        int selection = editText.getSelectionEnd();
        if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.eye_slash);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.eye_pass);
        }
        editText.setSelection(selection);
    }

    private void navigateToSignInActivity() {
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    private void handleChangePassword() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate input fields
        if (!validateInput(username, password, confirmPassword)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE); // Show loading indicator

        // Query Firebase to check if the username exists
        Query query = database.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    etUsername.setError("Account not registered");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Hash the new password
                String hashedPassword = hashPassword(password);
                if (hashedPassword == null) {
                    Toast.makeText(ForgotActivity.this, "Failed to encrypt password. Please try again.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Update password in Firebase
                updatePasswordInDatabase(snapshot, hashedPassword);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
                progressBar.setVisibility(View.GONE); // Hide progress bar on failure
            }
        });
    }

    private boolean validateInput(String username, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Enter username");
            return false;
        }
        if (!username.matches("[a-zA-Z0-9]+")) {
            etUsername.setError("Username must contain only letters and numbers");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter new password");
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm new password");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            etPassword.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            return false;
        }
        return true;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Hashing error: " + e.getMessage());
            return null;
        }
    }

    private void updatePasswordInDatabase(DataSnapshot snapshot, String hashedPassword) {
        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            userSnapshot.child("password").getRef().setValue(hashedPassword)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ForgotActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                        navigateToSignInActivity();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ForgotActivity.this, "Failed to change password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Database error: " + e.getMessage());
                    });
        }
    }

    private void handleDatabaseError(DatabaseError error) {
        progressBar.setVisibility(View.GONE);
        Log.e(TAG, "Database error: " + error.getMessage());
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There was an error checking the database. Please try again later.")
                .setPositiveButton("OK", null)
                .show();
    }
}