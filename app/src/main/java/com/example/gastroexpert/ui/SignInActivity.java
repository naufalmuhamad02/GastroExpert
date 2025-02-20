package com.example.gastroexpert.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private static final String PREFS_NAME = "MyPrefs";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private EditText etUsername, etPassword;
    private ImageView showPassBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Check login status
        if (isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        // Initialize UI components
        TextView btnRegister = findViewById(R.id.btnRegister);
        Button btnLogin = findViewById(R.id.btnlogin);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        showPassBtn = findViewById(R.id.show_pass_btn);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        progressBar = findViewById(R.id.progressBar);

        // Listener for navigating to the registration page
        btnRegister.setOnClickListener(view -> navigateToSignUpActivity());

        // Listener for showing/hiding the password
        showPassBtn.setOnClickListener(view -> togglePasswordVisibility());

        // Listener for the login button
        btnLogin.setOnClickListener(view -> handleLogin());

        // Listener for navigating to the forgot password page
        forgotPassword.setOnClickListener(v -> navigateToForgotPasswordActivity());
    }

    /**
     * Check if the user is already logged in.
     *
     * @return true if logged in, false otherwise.
     */
    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Log.d(TAG, "User is already logged in");
        }
        return isLoggedIn;
    }

    /**
     * Navigate to the main activity.
     */
    private void navigateToMainActivity() {
        Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    /**
     * Navigate to the sign-up activity.
     */
    private void navigateToSignUpActivity() {
        Intent registerIntent = new Intent(this, SignUpActivity.class);
        startActivity(registerIntent);
    }

    /**
     * Navigate to the forgot password activity.
     */
    private void navigateToForgotPasswordActivity() {
        Intent forgotIntent = new Intent(this, ForgotActivity.class);
        startActivity(forgotIntent);
    }

    /**
     * Toggle password visibility.
     */
    private void togglePasswordVisibility() {
        int selection = etPassword.getSelectionEnd();
        if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            showPassBtn.setImageResource(R.drawable.eye_slash);
        } else {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            showPassBtn.setImageResource(R.drawable.eye_pass);
        }
        etPassword.setSelection(selection);
    }

    /**
     * Handle the login process.
     */
    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput(username, password)) {
            return;
        }

        String hashedPassword = hashPassword(password);
        if (hashedPassword.isEmpty()) {
            Toast.makeText(this, "Error hashing password. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE); // Show loading indicator
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        Query query = database.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean match = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String dbUsername = userSnapshot.child("username").getValue(String.class);
                    String dbPassword = userSnapshot.child("password").getValue(String.class);

                    if (dbUsername != null && dbPassword != null &&
                            dbUsername.equalsIgnoreCase(username) && dbPassword.equals(hashedPassword)) {
                        match = true;
                        break;
                    }
                }

                progressBar.setVisibility(View.GONE); // Hide loading indicator

                if (match) {
                    saveLoginStatus(username);
                    Toast.makeText(SignInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                } else {
                    Toast.makeText(SignInActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide loading indicator
                Log.e(TAG, "Database error: " + error.getMessage());
                showErrorDialog();
            }
        });
    }

    /**
     * Validate user input.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @return true if input is valid, false otherwise.
     */
    private boolean validateInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username or Password is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(this, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Save the login status in SharedPreferences.
     *
     * @param username The username of the logged-in user.
     */
    private void saveLoginStatus(String username) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.apply();
    }

    /**
     * Hash the password using SHA-256.
     *
     * @param password The password to hash.
     * @return The hashed password as a hexadecimal string.
     */
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
            Log.e(TAG, "Error hashing password", e);
            Toast.makeText(this, "Error hashing password. Please try again.", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    /**
     * Show an error dialog with a generic message.
     */
    private void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("An error occurred while accessing the database. Please try again later.")
                .setPositiveButton("OK", null)
                .show();
    }
}