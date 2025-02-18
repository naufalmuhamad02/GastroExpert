package com.example.gastroexpert.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
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
        EdgeToEdge.enable(this);

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

        // Check if the app should navigate to a specific fragment based on the intent
        String navigateTo = getIntent().getStringExtra("navigateTo");
        if (navigateTo != null && navigateTo.equals("diagnosis")) {
            // Navigate to the DiagnosisFragment and pass the username as an argument
            DiagnosisFragment fragment = new DiagnosisFragment();
            Bundle args = new Bundle();
            args.putString("username", username);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            navigationView.setCheckedItem(R.id.nav_diagnosis);
        } else if (savedInstanceState == null) {
            // If no specific fragment is specified, navigate to the HomeFragment by default
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_beranda);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item selection
        if (item.getItemId() == R.id.nav_beranda) {
            // Navigate to the HomeFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            Log.d("MainActivity", "Navigated to HomeFragment");
        } else if (item.getItemId() == R.id.nav_penyakit) {
            // Navigate to the DiseaseFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DiseaseFragment()).commit();
            Log.d("MainActivity", "Navigated to DiseaseFragment");
        } else if (item.getItemId() == R.id.nav_tentang) {
            // Navigate to the AboutFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
            Log.d("MainActivity", "Navigated to AboutFragment");
        } else if (item.getItemId() == R.id.nav_riwayat) {
            // Navigate to the HistoryFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HistoryFragment()).commit();
            Log.d("MainActivity", "Navigated to HistoryFragment");
        } else if (item.getItemId() == R.id.nav_diagnosis) {
            // Navigate to the DiagnosisFragment and pass the username as an argument
            DiagnosisFragment fragment = new DiagnosisFragment();
            Bundle args = new Bundle();
            args.putString("username", username);
            fragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
            Log.d("MainActivity", "Navigated to DiagnosisFragment");
        } else if (item.getItemId() == R.id.nav_logout) {
            // Handle logout by clearing the login status in SharedPreferences
            getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().putBoolean("isLoggedIn", false).apply();
            // Start the SignInActivity and clear the activity stack
            Intent logout = new Intent(MainActivity.this, SignInActivity.class);
            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logout);
            finish(); // Close the MainActivity
            Log.d("MainActivity", "Logged out");
        }
        // Close the navigation drawer after handling the item selection
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        // Handle the back button press
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Close the navigation drawer if it is open
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Call the superclass method to handle the back button press
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the user is logged in
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            // If the user is not logged in, start the SignInActivity and clear the activity stack
            Intent loginIntent = new Intent(MainActivity.this, SignInActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish(); // Close the MainActivity
        }
    }
}