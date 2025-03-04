package com.example.gastroexpert.ui;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private static final String PREFS_NAME = "MyPrefs";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private EditText etUsername, etPassword;
    private ImageView showPassBtn;
    private ProgressBar progressBar;
    private Button btnLogin;

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        // Initialize UI components
        initializeUIComponents();

        // Check if the user is already logged in
        if (isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("users");

        // Set up listeners for buttons
        setupListeners();
    }

    private void initializeUIComponents() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        showPassBtn = findViewById(R.id.show_pass_btn);
        progressBar = findViewById(R.id.progressBar);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(view -> handleLogin());
        showPassBtn.setOnClickListener(view -> togglePasswordVisibility());
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        TextView signUp = findViewById(R.id.btnRegister);

        forgotPassword.setOnClickListener(view -> navigateToForgotPasswordActivity());
        signUp.setOnClickListener(view -> navigateToSignUpActivity());
    }

    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean("isLoggedIn", false);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("username", etUsername.getText().toString().trim());
        startActivity(intent);
        finish();
    }

    private void navigateToForgotPasswordActivity() {
        startActivity(new Intent(this, ForgotActivity.class));
    }

    private void navigateToSignUpActivity() {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    private void togglePasswordVisibility() {
        if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            showPassBtn.setImageResource(R.drawable.eye_slash);
        } else {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            showPassBtn.setImageResource(R.drawable.eye_pass);
        }
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput(username, password)) return;

        // Disable the login button and show progress
        btnLogin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        String hashedPassword = hashPassword(password);

        // Query Firebase for user credentials
        queryUserInDatabase(username, hashedPassword);
    }

    private boolean validateInput(String username, String password) {
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Enter username");
            return false;
        }
        if (!username.matches("[a-zA-Z0-9]+")) {
            etUsername.setError("Username must contain only letters and numbers");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
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
            Log.e(TAG, "Hashing error", e);
            return "";
        }
    }

    private void queryUserInDatabase(String username, String hashedPassword) {
        Query query = database.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isValid = false;
                for (DataSnapshot user : snapshot.getChildren()) {
                    String dbPassword = user.child("password").getValue(String.class);
                    if (dbPassword != null && dbPassword.equals(hashedPassword)) {
                        isValid = true;
                        break;
                    }
                }

                // Hide progress bar and enable login button
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (isValid) {
                    saveLoginStatus(username);
                    Toast.makeText(SignInActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                } else {
                    Toast.makeText(SignInActivity.this, "Username/password incorrect", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                showErrorDialog();
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void saveLoginStatus(String username) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.apply();
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There was a connection issue. Please try again later.")
                .setPositiveButton("OK", null)
                .show();
    }
}