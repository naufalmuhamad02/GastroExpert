package com.example.gastroexpert.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.gastroexpert.R;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable edge-to-edge display for better visual integration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EdgeToEdge.enable(this);
        }

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Set up the NavigationView and set the item selection listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set up the ActionBarDrawerToggle to handle the hamburger icon and drawer opening/closing
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Retrieve the username from the intent
        username = getIntent().getStringExtra("username");
        if (username == null) {
            username = "Guest";
        }

        // Check if the app should navigate to a specific fragment based on the intent
        String navigateTo = getIntent().getStringExtra("navigateTo");
        if ("diagnosis".equals(navigateTo)) {
            DiagnosisFragment fragment = new DiagnosisFragment();
            Bundle args = new Bundle();
            args.putString("username", username);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            navigationView.setCheckedItem(R.id.nav_diagnosis);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).addToBackStack(null).commit();
            navigationView.setCheckedItem(R.id.nav_beranda);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try {
            if (item.getItemId() == R.id.nav_beranda) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).addToBackStack(null).commit();
                Log.d("MainActivity", "Navigated to HomeFragment");
            } else if (item.getItemId() == R.id.nav_penyakit) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DiseaseFragment()).addToBackStack(null).commit();
                Log.d("MainActivity", "Navigated to DiseaseFragment");
            } else if (item.getItemId() == R.id.nav_tentang) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).addToBackStack(null).commit();
                Log.d("MainActivity", "Navigated to AboutFragment");
            } else if (item.getItemId() == R.id.nav_riwayat) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HistoryFragment()).addToBackStack(null).commit();
                Log.d("MainActivity", "Navigated to HistoryFragment");
            } else if (item.getItemId() == R.id.nav_diagnosis) {
                DiagnosisFragment fragment = new DiagnosisFragment();
                Bundle args = new Bundle();
                args.putString("username", username);
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                Log.d("MainActivity", "Navigated to DiagnosisFragment");
            } else if (item.getItemId() == R.id.nav_logout) {
                SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();
                Intent logout = new Intent(MainActivity.this, SignInActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logout);
                finish();
                Log.d("MainActivity", "Logged out");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error handling navigation item selection", e);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Keluar")
                    .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                    .setPositiveButton("Ya", (dialog, which) -> finish())
                    .setNegativeButton("Tidak", null)
                    .show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
            if (!isLoggedIn) {
                Intent loginIntent = new Intent(MainActivity.this, SignInActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error checking login status", e);
        }
    }
}