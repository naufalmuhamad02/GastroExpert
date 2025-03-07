package com.example.gastroexpert.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private static final String PREFS_NAME = "MyPrefs";
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 20;

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private ImageView showPassBtn, showConfirmPassBtn;
    private ProgressBar progressBar;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        if (isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        initializeUIComponents();
        setupListeners();

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("users");
    }

    private void initializeUIComponents() {
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        showPassBtn = findViewById(R.id.show_pass_btn);
        showConfirmPassBtn = findViewById(R.id.show_confirm_pass_btn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvHaveAccount = findViewById(R.id.memilikiAkun);

        tvHaveAccount.setOnClickListener(view -> navigateToSignInActivity());
        showPassBtn.setOnClickListener(view -> togglePasswordVisibility(etPassword, showPassBtn));
        showConfirmPassBtn.setOnClickListener(view -> togglePasswordVisibility(etConfirmPassword, showConfirmPassBtn));

        etUsername.addTextChangedListener(createTextWatcher(this::validateUsername));
        etEmail.addTextChangedListener(createTextWatcher(this::validateEmail));
        etPassword.addTextChangedListener(createTextWatcher(this::validatePassword));
        etConfirmPassword.addTextChangedListener(createTextWatcher(this::validateConfirmPassword));

        btnRegister.setOnClickListener(view -> handleRegistration());
    }

    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean("isLoggedIn", false);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToSignInActivity() {
        startActivity(new Intent(this, SignInActivity.class));
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

    private TextWatcher createTextWatcher(Runnable validateMethod) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateMethod.run();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private void validateUsername() {
        String username = etUsername.getText().toString().trim();
        if (username.isEmpty()) {
            etUsername.setError("Enter username");
        } else if (username.length() < MIN_USERNAME_LENGTH) {
            etUsername.setError("Username must be at least " + MIN_USERNAME_LENGTH + " characters");
        } else if (username.length() >= MAX_USERNAME_LENGTH) {
            etUsername.setError("Username must be less than " + MAX_USERNAME_LENGTH + " characters");
        } else if (!username.matches("[a-zA-Z0-9]+")) {
            etUsername.setError("Username must contain only letters and numbers");
        } else {
            etUsername.setError(null);
        }
    }

    private void validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Enter email");
        } else if (!isValidEmail(email)) {
            etEmail.setError("Invalid email address");
        } else {
            etEmail.setError(null);
        }
    }

    private void validatePassword() {
        String password = etPassword.getText().toString().trim();
        String passwordRegex = "^(?=.*\\d)[\\w\\W]*$";
        if (password.isEmpty()) {
            etPassword.setError("Enter password");
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            etPassword.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        } else if (password.length() >= MAX_PASSWORD_LENGTH) {
            etPassword.setError("Password must be less than " + MAX_PASSWORD_LENGTH + " characters");
        } else if (!password.matches(passwordRegex)) {
            etPassword.setError("Password must contain at least one number");
        } else {
            etPassword.setError(null);
        }
    }

    private void validateConfirmPassword() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm password");
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
        } else {
            etConfirmPassword.setError(null);
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    private void handleRegistration() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInput(username, email, password, confirmPassword)) return;

        progressBar.setVisibility(View.VISIBLE);

        Query usernameQuery = database.orderByChild("username").equalTo(username);
        Query emailQuery = database.orderByChild("email").equalTo(email);

        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etUsername.setError("Username already taken");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            etEmail.setError("Email already taken");
                            progressBar.setVisibility(View.GONE);
                            return;
                        }

                        String hashedPassword = hashPassword(password);
                        saveUserData(username, email, hashedPassword);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        handleDatabaseError(error);
                    }
                });
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        });
    }

    private boolean validateInput(String username, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Enter username");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm password");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void saveUserData(String username, String email, String hashedPassword) {
        String userId = database.push().getKey();
        if (userId != null) {
            database.child(userId).child("username").setValue(username)
                    .addOnSuccessListener(aVoid -> database.child(userId).child("email").setValue(email)
                            .addOnSuccessListener(aVoid1 -> database.child(userId).child("password").setValue(hashedPassword)
                                    .addOnSuccessListener(aVoid2 -> {
                                        Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                        navigateToSignInActivity();
                                    })
                            )
                    )
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Error saving user data", e);
                        Toast.makeText(SignUpActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error generating user ID");
            Toast.makeText(SignUpActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
        }
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
