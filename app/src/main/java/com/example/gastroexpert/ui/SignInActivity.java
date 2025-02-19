package com.example.gastroexpert.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
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

public class SignInActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private ImageView showPassBtn;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EdgeToEdge.enable(this);
        }
        setContentView(R.layout.activity_signin);

        // Check login status
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);
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
        TextView btnRegister = findViewById(R.id.btnRegister);
        Button btnLogin = findViewById(R.id.btnlogin);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        showPassBtn = findViewById(R.id.show_pass_btn);
        TextView forgotPassword = findViewById(R.id.forgotPassword);

        // Listener for navigating to the registration page
        btnRegister.setOnClickListener(view -> {
            Intent registerIntent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(registerIntent);
        });

        // Listener for showing/hiding the password
        showPassBtn.setOnClickListener(view -> {
            int selection = etPassword.getSelectionEnd();
            if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showPassBtn.setImageResource(R.drawable.eye_slash);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showPassBtn.setImageResource(R.drawable.eye_pass);
            }
            etPassword.setSelection(selection);
        });

        // Listener for the login button
        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validation input
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Username or Password is empty", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(getApplicationContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                // Hash the password
                String hashedPassword = hashPassword(password);

                // Check if hashing failed
                if (hashedPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Error hashing password. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Access Firebase database
                database = FirebaseDatabase.getInstance().getReference("users");
                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean match = false;

                        // Iterate through users in the database
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String dbUsername = userSnapshot.child("username").getValue(String.class);
                            String dbPassword = userSnapshot.child("password").getValue(String.class);

                            Log.d("SignInActivity", "Checking user: " + userSnapshot.getKey());
                            Log.d("SignInActivity", "Retrieved username: " + dbUsername);
                            Log.d("SignInActivity", "Retrieved password: " + dbPassword);

                            // Compare username and password
                            if (dbUsername != null && dbPassword != null &&
                                    dbUsername.equalsIgnoreCase(username) && dbPassword.equals(hashedPassword)) {
                                match = true;
                                break;
                            }
                        }

                        // Handle login result
                        if (match) {
                            try {
                                SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                                editor.putBoolean("isLoggedIn", true);
                                editor.putString("username", username);
                                editor.apply();

                                Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();

                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mainIntent);
                                finish();
                            } catch (Exception e) {
                                Log.e("SignInActivity", "Error saving login status", e);
                                Toast.makeText(getApplicationContext(), "Error saving login status. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("SignInActivity", "Database error: " + error.getMessage());
                        new AlertDialog.Builder(SignInActivity.this)
                                .setTitle("Error")
                                .setMessage("An error occurred while accessing the database. Please try again later.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
            }
        });

        // Listener for navigating to the forgot password page
        forgotPassword.setOnClickListener(v -> {
            Intent forgotIntent = new Intent(SignInActivity.this, ForgotActivity.class);
            startActivity(forgotIntent);
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
            Log.e("SignInActivity", "Error hashing password", e);
            Toast.makeText(this, "Error hashing password. Please try again.", Toast.LENGTH_SHORT).show();
            return ""; // Return empty string instead of null
        }
    }
}