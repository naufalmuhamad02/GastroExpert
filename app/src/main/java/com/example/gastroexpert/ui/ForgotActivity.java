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

public class ForgotActivity extends AppCompatActivity {

    private static final String TAG = "ForgotActivity";
    private static final String PREFS_NAME = "MyPrefs";
    private static final int MAX_PASSWORD_LENGTH = 20;

    private EditText etUsername, etPassword, etConfirmPassword;
    private ImageView showPassBtn, showConfirmPassBtn;
    private ProgressBar progressBar;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        // Check login status
        if (isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        // Initialize UI components
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etPasswords);
        Button btnChangePassword = findViewById(R.id.btnGanti);
        TextView tvLogin = findViewById(R.id.ingat);
        showPassBtn = findViewById(R.id.show_pass_btn);
        showConfirmPassBtn = findViewById(R.id.show_confpass_btn);
        progressBar = findViewById(R.id.progressBar);

        // Initialize database reference
        database = FirebaseDatabase.getInstance().getReference("users");

        // Listener for navigating to the login page
        tvLogin.setOnClickListener(view -> navigateToSignInActivity());

        // Listener for showing/hiding password
        showPassBtn.setOnClickListener(view -> togglePasswordVisibility(etPassword, showPassBtn));

        // Listener for showing/hiding confirm password
        showConfirmPassBtn.setOnClickListener(view -> togglePasswordVisibility(etConfirmPassword, showConfirmPassBtn));

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

        // Validate confirm password
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listener for the change password button
        btnChangePassword.setOnClickListener(view -> handleChangePassword());
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
        Intent mainIntent = new Intent(ForgotActivity.this, MainActivity.class);
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
            etUsername.setError(null);
            return;
        }

        Query query = database.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    etUsername.setError("Akun tidak terdaftar");
                } else {
                    etUsername.setError(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        });
    }

    /**
     * Validate the password field.
     */
    private void validatePassword() {
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String passwordRegex = "^(?=.*\\d)[\\w\\W]*$";

        if (password.isEmpty()) {
            etPassword.setError("Masukkan password");
        } else if (password.length() >= MAX_PASSWORD_LENGTH) {
            etPassword.setError("Password harus kurang dari " + MAX_PASSWORD_LENGTH + " karakter");
        } else if (!password.matches(passwordRegex)) {
            etPassword.setError("Password setidaknya harus mengandung satu angka");
        } else if (password.equals(username)) {
            etPassword.setError("Password tidak boleh sama dengan username");
        } else {
            etPassword.setError(null);
        }
    }

    /**
     * Validate the confirm password field.
     */
    private void validateConfirmPassword() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(confirmPassword) && !confirmPassword.equals(password)) {
            etConfirmPassword.setError("Password tidak sama");
        } else {
            etConfirmPassword.setError(null);
        }
    }

    /**
     * Handle the change password process.
     */
    private void handleChangePassword() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInput(username, password, confirmPassword)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE); // Show loading indicator

        Query query = database.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    etUsername.setError("Akun tidak terdaftar");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String hashedPassword = hashPassword(password);
                    if (hashedPassword == null) {
                        Toast.makeText(ForgotActivity.this, "Gagal mengenkripsi password. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    userSnapshot.child("password").getRef().setValue(hashedPassword)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ForgotActivity.this, "Berhasil mengganti password", Toast.LENGTH_SHORT).show();
                                navigateToSignInActivity();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ForgotActivity.this, "Gagal mengganti password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Database error: " + e.getMessage());
                            });
                }
                progressBar.setVisibility(View.GONE); // Hide loading indicator
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
     * @param password The password entered by the user.
     * @param confirmPassword The confirm password entered by the user.
     * @return true if input is valid, false otherwise.
     */
    private boolean validateInput(String username, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Masukkan username");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Masukkan password");
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Masukkan konfirmasi password");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Password tidak cocok");
            return false;
        }
        return true;
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