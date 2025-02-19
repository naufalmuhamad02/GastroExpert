package com.example.gastroexpert.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import androidx.appcompat.app.AlertDialog;
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

public class ForgotActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, confPassword;
    private boolean mail = false;
    private boolean cpass = false;
    private ImageView show_pass_btn, show_confpass_btn;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EdgeToEdge.enable(this);
        }
        setContentView(R.layout.activity_forgot);

        // Check login status
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Intent mainIntent = new Intent(ForgotActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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
        etPassword = findViewById(R.id.etPassword);
        confPassword = findViewById(R.id.etPasswords);
        Button btnGanti = findViewById(R.id.btnGanti);
        TextView ingatakun = findViewById(R.id.ingat);
        show_pass_btn = findViewById(R.id.show_pass_btn);
        show_confpass_btn = findViewById(R.id.show_confpass_btn);

        // Listener for navigating to the login page
        ingatakun.setOnClickListener(view -> {
            Intent login = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(login);
        });

        // Listener for showing/hiding password
        show_pass_btn.setOnClickListener(view -> {
            int selection = etPassword.getSelectionEnd();
            if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                show_pass_btn.setImageResource(R.drawable.eye_slash);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                show_pass_btn.setImageResource(R.drawable.eye_pass);
            }
            etPassword.setSelection(selection);
        });

        // Listener for showing/hiding confirm password
        show_confpass_btn.setOnClickListener(view -> {
            int selection = confPassword.getSelectionEnd();
            if (confPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                confPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                show_confpass_btn.setImageResource(R.drawable.eye_slash);
            } else {
                confPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                show_confpass_btn.setImageResource(R.drawable.eye_pass);
            }
            confPassword.setSelection(selection);
        });

        // Validate username
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String username = etUsername.getText().toString().trim();
                if (username.isEmpty()) {
                    etUsername.setError(null);
                    mail = false;
                    return;
                }

                database = FirebaseDatabase.getInstance().getReference("users");
                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean userFound = false;
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String existingUsername = userSnapshot.child("username").getValue(String.class);
                            if (existingUsername != null && existingUsername.equalsIgnoreCase(username)) {
                                etUsername.setError(null);
                                mail = true;
                                userFound = true;
                                break;
                            }
                        }
                        if (!userFound) {
                            etUsername.setError("Akun tidak terdaftar");
                            mail = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ForgotActivity", "Database error: " + error.getMessage());
                        new AlertDialog.Builder(ForgotActivity.this)
                                .setTitle("Error")
                                .setMessage("Terjadi kesalahan saat memeriksa database. Silakan coba lagi nanti.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
            }
        });

        // Validate password
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = etPassword.getText().toString().trim();
                String username = etUsername.getText().toString().trim();
                String passwordRegex = "^(?=.*\\d)[\\w\\W]*$";

                if (password.isEmpty()) {
                    etPassword.setError("Masukkan password");
                } else if (password.length() >= 20) {
                    etPassword.setError("Password harus kurang dari 20 karakter");
                } else if (!password.matches(passwordRegex)) {
                    etPassword.setError("Password setidaknya harus mengandung satu angka");
                } else if (password.equals(username)) {
                    etPassword.setError("Password tidak boleh sama dengan username");
                } else {
                    etPassword.setError(null);
                }
            }
        });

        // Validate confirm password
        confPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = etPassword.getText().toString().trim();
                String cpassword = confPassword.getText().toString().trim();

                if (!cpassword.isEmpty() && !cpassword.equals(password)) {
                    confPassword.setError("Password tidak sama");
                    cpass = false;
                } else {
                    confPassword.setError(null);
                    cpass = true;
                }
            }
        });

        // Listener for the change password button
        btnGanti.setOnClickListener(view -> {
            final String username = etUsername.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();
            final String cpassword = confPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Masukkan username");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Masukkan password");
                return;
            }

            if (TextUtils.isEmpty(cpassword)) {
                confPassword.setError("Masukkan konfirmasi password");
                return;
            }

            if (!cpass) {
                confPassword.setError("Password tidak sama");
                return;
            }

            if (mail) {
                database = FirebaseDatabase.getInstance().getReference("users");
                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean usernameFound = false;
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String usernames = userSnapshot.child("username").getValue(String.class);
                            if (usernames != null && usernames.equalsIgnoreCase(username)) {
                                String hashedPassword = hashPassword(password);
                                if (hashedPassword == null) {
                                    Toast.makeText(ForgotActivity.this, "Gagal mengenkripsi password. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                userSnapshot.child("password").getRef().setValue(hashedPassword)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(ForgotActivity.this, "Berhasil mengganti password", Toast.LENGTH_SHORT).show();
                                            Intent loginIntent = new Intent(ForgotActivity.this, SignInActivity.class);
                                            startActivity(loginIntent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(ForgotActivity.this, "Gagal mengganti password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                usernameFound = true;
                                break;
                            }
                        }
                        if (!usernameFound) {
                            Toast.makeText(ForgotActivity.this, "Akun tidak terdaftar", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ForgotActivity", "Database error: " + error.getMessage());
                        new AlertDialog.Builder(ForgotActivity.this)
                                .setTitle("Error")
                                .setMessage("Terjadi kesalahan saat memeriksa database. Silakan coba lagi nanti.")
                                .setPositiveButton("OK", null)
                                .show();
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
            Log.e("ForgotActivity", "Hashing error: " + e.getMessage());
            Toast.makeText(this, "Gagal mengenkripsi password. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}