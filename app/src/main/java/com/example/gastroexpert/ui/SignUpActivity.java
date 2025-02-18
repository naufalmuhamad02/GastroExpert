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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.gastroexpert.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private boolean isUsernameValid = false;
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;
    private DatabaseReference database;
    private ImageView showPassBtn, showConfirmPassBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable Edge-to-Edge mode for immersive UI
        setContentView(R.layout.activity_sign_up);

        // Check login status
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            // If already logged in, redirect to MainActivity
            Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();
        }

        // Set padding based on system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvHaveAccount = findViewById(R.id.memilikiAkun);
        showPassBtn = findViewById(R.id.show_pass_btn);
        showConfirmPassBtn = findViewById(R.id.show_confirm_pass_btn);

        // Initialize database reference
        database = FirebaseDatabase.getInstance().getReference("users");

        // Listener for navigating to the login page
        tvHaveAccount.setOnClickListener(view -> {
            Intent loginIntent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(loginIntent);
        });

        // Listener for showing/hiding password
        showPassBtn.setOnClickListener(view -> {
            int selection = etPassword.getSelectionEnd();
            if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showPassBtn.setImageResource(R.drawable.eye_slash); // Change icon to eye-slash when showing password
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showPassBtn.setImageResource(R.drawable.eye_pass); // Change icon to eye-pass when hiding password
            }
            etPassword.setSelection(selection);
        });

        // Listener for showing/hiding confirm password
        showConfirmPassBtn.setOnClickListener(view -> {
            int selection = etConfirmPassword.getSelectionEnd();
            if (etConfirmPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showConfirmPassBtn.setImageResource(R.drawable.eye_slash); // Change icon to eye-slash when showing password
            } else {
                etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showConfirmPassBtn.setImageResource(R.drawable.eye_pass); // Change icon to eye-pass when hiding password
            }
            etConfirmPassword.setSelection(selection);
        });

        // Validate username
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String username = etUsername.getText().toString().trim();
                if (username.isEmpty()) {
                    etUsername.setError("Masukkan username");
                    isUsernameValid = false;
                } else if (username.length() <= 5) {
                    etUsername.setError("Username harus lebih dari 5 karakter");
                    isUsernameValid = false;
                } else if (username.length() >= 20) {
                    etUsername.setError("Username harus kurang dari 20 karakter");
                    isUsernameValid = false;
                } else {
                    etUsername.setError(null);
                    isUsernameValid = true;
                }
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
                String email = etEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    etEmail.setError("Masukkan email");
                    isEmailValid = false;
                } else if (!isValidEmail(email)) {
                    etEmail.setError("Email tidak valid");
                    isEmailValid = false;
                } else {
                    etEmail.setError(null);
                    isEmailValid = true;
                }
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
                String password = etPassword.getText().toString().trim();
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String passwordRegex = "^(?=.*\\d)[\\w\\W]*$";

                if (password.isEmpty()) {
                    etPassword.setError("Masukkan password");
                    isPasswordValid = false;
                } else if (password.length() <= 5) {
                    etPassword.setError("Password harus lebih dari 5 karakter");
                    isPasswordValid = false;
                } else if (password.length() >= 20) {
                    etPassword.setError("Password harus kurang dari 20 karakter");
                    isPasswordValid = false;
                } else if (password.equals(username)) {
                    etPassword.setError("Password tidak boleh sama dengan username");
                    isPasswordValid = false;
                } else if (!password.matches(passwordRegex)) {
                    etPassword.setError("Password setidaknya harus mengandung satu angka");
                    isPasswordValid = false;
                } else if (password.equals(email)) {
                    etPassword.setError("Password tidak boleh sama dengan email");
                    isPasswordValid = false;
                } else {
                    etPassword.setError(null);
                    isPasswordValid = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listener for the register button
        btnRegister.setOnClickListener(view -> {
            final String username = etUsername.getText().toString().trim();
            final String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();
            final String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validate empty inputs
            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Masukkan username");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Masukkan email");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Masukkan password");
                return;
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                etConfirmPassword.setError("Konfirmasi password");
                return;
            }

            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Password tidak cocok");
                return;
            }

            if (isUsernameValid && isEmailValid && isPasswordValid) {
                // Check the uniqueness of username and email in the database
                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean usernameExists = false;
                        boolean emailExists = false;

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String existingUsername = userSnapshot.child("username").getValue(String.class);
                            String existingEmail = userSnapshot.child("email").getValue(String.class);

                            if (existingUsername != null && existingUsername.equals(username)) {
                                etUsername.setError("Username sudah digunakan");
                                usernameExists = true;
                                break;
                            }

                            if (existingEmail != null && existingEmail.equals(email)) {
                                etEmail.setError("Email sudah digunakan");
                                emailExists = true;
                                break;
                            }
                        }

                        if (!usernameExists && !emailExists) {
                            // Hash password
                            String hashedPassword = hashPassword(password);

                            // Save new user data to the database
                            database.child(username).child("username").setValue(username)
                                    .addOnSuccessListener(aVoid -> database.child(username).child("email").setValue(email)
                                            .addOnSuccessListener(aVoid1 -> database.child(username).child("password").setValue(hashedPassword)
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        Toast.makeText(SignUpActivity.this, "Berhasil membuat akun", Toast.LENGTH_SHORT).show();
                                                        Intent loginIntent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                        startActivity(loginIntent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(SignUpActivity.this, "Gagal menyimpan password", Toast.LENGTH_SHORT).show();
                                                        Log.e("SignUpActivity", "Database error: " + e.getMessage());
                                                    }))
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(SignUpActivity.this, "Gagal menyimpan email", Toast.LENGTH_SHORT).show();
                                                Log.e("SignUpActivity", "Database error: " + e.getMessage());
                                            }))
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignUpActivity.this, "Gagal menyimpan username", Toast.LENGTH_SHORT).show();
                                        Log.e("SignUpActivity", "Database error: " + e.getMessage());
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SignUpActivity.this, "Terjadi kesalahan saat memeriksa database", Toast.LENGTH_SHORT).show();
                        Log.e("SignUpActivity", "Database error: " + error.getMessage());
                    }
                });
            }
        });
    }

    // Method to hash the password using SHA-256
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
            Log.e("SignUpActivity", "Hashing error: " + e.getMessage());
            return null;
        }
    }

    // Method to validate email using regex
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
}