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
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MAX_PASSWORD_LENGTH = 20;

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private ImageView showPassBtn, showConfirmPassBtn;
    private ProgressBar progressBar;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Check login status
        if (isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        // Initialize UI components
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvHaveAccount = findViewById(R.id.memilikiAkun);
        showPassBtn = findViewById(R.id.show_pass_btn);
        showConfirmPassBtn = findViewById(R.id.show_confirm_pass_btn);
        progressBar = findViewById(R.id.progressBar);

        // Initialize database reference
        database = FirebaseDatabase.getInstance().getReference("users");

        // Listener for navigating to the login page
        tvHaveAccount.setOnClickListener(view -> navigateToSignInActivity());

        // Listener for showing/hiding password
        showPassBtn.setOnClickListener(view -> togglePasswordVisibility(etPassword, showPassBtn));

        // Listener for showing/hiding confirm password
        showConfirmPassBtn.setOnClickListener(view -> togglePasswordVisibility(
                etConfirmPassword, showConfirmPassBtn
        ));

        // Validate username
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUsername();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Validate email
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Validate password
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listener for the register button
        btnRegister.setOnClickListener(view -> handleRegistration());
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
        Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    /**
     * Navigate to the sign-in activity.
     */
    private void navigateToSignInActivity() {
        Intent loginIntent = new Intent(this, SignInActivity.class);
        startActivity(loginIntent);
    }

    /**
     * Toggle password visibility.
     *
     * @param editText The EditText field to toggle.
     * @param imageView The ImageView button to change.
     */
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

    /**
     * Validate the username field.
     */
    private void validateUsername() {
        String username = etUsername.getText().toString().trim();
        if (username.isEmpty()) {
            etUsername.setError("Masukkan username");
        } else if (username.length() >= MAX_USERNAME_LENGTH) {
            etUsername.setError("Username harus kurang dari " + MAX_USERNAME_LENGTH + " karakter");
        } else {
            etUsername.setError(null);
        }
    }

    /**
     * Validate the email field.
     */
    private void validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Masukkan email");
        } else if (!isValidEmail(email)) {
            etEmail.setError("Email tidak valid");
        } else {
            etEmail.setError(null);
        }
    }

    /**
     * Validate the password field.
     */
    private void validatePassword() {
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String passwordRegex = "^(?=.*\\d)[\\w\\W]*$";

        if (password.isEmpty()) {
            etPassword.setError("Masukkan password");
        } else if (password.length() >= MAX_PASSWORD_LENGTH) {
            etPassword.setError("Password harus kurang dari " + MAX_PASSWORD_LENGTH + " karakter");
        } else if (password.equals(username)) {
            etPassword.setError("Password tidak boleh sama dengan username");
        } else if (!password.matches(passwordRegex)) {
            etPassword.setError("Password setidaknya harus mengandung satu angka");
        } else if (password.equals(email)) {
            etPassword.setError("Password tidak boleh sama dengan email");
        } else {
            etPassword.setError(null);
        }
    }

    /**
     * Handle the registration process.
     */
    private void handleRegistration() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInput(username, email, password, confirmPassword)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE); // Show loading indicator

        Query usernameQuery = database.orderByChild("username").equalTo(username);
        Query emailQuery = database.orderByChild("email").equalTo(email);

        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etUsername.setError("Username sudah digunakan");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            etEmail.setError("Email sudah digunakan");
                            progressBar.setVisibility(View.GONE);
                            return;
                        }

                        String hashedPassword = hashPassword(password);
                        if (hashedPassword == null) {
                            Toast.makeText(SignUpActivity.this, "Gagal mengenkripsi password. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            return;
                        }

                        saveUserData(username, email, hashedPassword);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        handleDatabaseError(error);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        });
    }

    /**
     * Validate user input.
     *
     * @param username The username entered by the user.
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * @param confirmPassword The confirm password entered by the user.
     * @return true if input is valid, false otherwise.
     */
    private boolean validateInput(String username, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Masukkan username");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Masukkan email");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Masukkan password");
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Konfirmasi password");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Password tidak cocok");
            return false;
        }
        return true;
    }

    /**
     * Save user data to the database.
     *
     * @param username The username of the user.
     * @param email The email of the user.
     * @param hashedPassword The hashed password of the user.
     */
    private void saveUserData(String username, String email, String hashedPassword) {
        String userId = database.push().getKey();
        if (userId != null) {
            database.child(userId).child("username").setValue(username)
                    .addOnSuccessListener(aVoid -> database.child(userId).child("email").setValue(email)
                            .addOnSuccessListener(aVoid1 -> database.child(userId).child("password").setValue(hashedPassword)
                                    .addOnSuccessListener(aVoid2 -> {
                                        Toast.makeText(SignUpActivity.this, "Berhasil membuat akun", Toast.LENGTH_SHORT).show();
                                        navigateToSignInActivity();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignUpActivity.this, "Gagal menyimpan password", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Database error: " + e.getMessage());
                                    }))
                            .addOnFailureListener(e -> {
                                Toast.makeText(SignUpActivity.this, "Gagal menyimpan email", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Database error: " + e.getMessage());
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(SignUpActivity.this, "Gagal menyimpan username", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Database error: " + e.getMessage());
                    });
        }
        progressBar.setVisibility(View.GONE); // Hide loading indicator
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
            Log.e(TAG, "Hashing error: " + e.getMessage());
            Toast.makeText(this, "Gagal mengenkripsi password. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Validate email using regex.
     *
     * @param email The email to validate.
     * @return true if email is valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    /**
     * Handle database errors.
     *
     * @param error The database error.
     */
    private void handleDatabaseError(DatabaseError error) {
        progressBar.setVisibility(View.GONE); // Hide loading indicator
        Log.e(TAG, "Database error: " + error.getMessage());
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Terjadi kesalahan saat memeriksa database. Silakan coba lagi nanti.")
                .setPositiveButton("OK", null)
                .show();
    }
}