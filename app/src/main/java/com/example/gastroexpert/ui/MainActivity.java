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

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "MyPrefs";
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
        if (navigationView == null) {
            Log.e(TAG, "NavigationView not found in layout");
            return;
        }
        navigationView.setNavigationItemSelectedListener(this);

        // Set up the ActionBarDrawerToggle to handle the hamburger icon and drawer opening/closing
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
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
            navigateToFragment(new DiagnosisFragment(), username, R.id.nav_diagnosis);
        } else {
            navigateToFragment(new HomeFragment(), null, R.id.nav_beranda);
        }
    }

    /**
     * Navigate to a specific fragment.
     *
     * @param fragment The fragment to navigate to.
     * @param username The username to pass to the fragment (if applicable).
     * @param menuItemId The menu item ID to mark as checked.
     */
    private void navigateToFragment(androidx.fragment.app.Fragment fragment, String username, int menuItemId) {
        if (username != null) {
            Bundle args = new Bundle();
            args.putString("username", username);
            fragment.setArguments(args);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        // Set the selected menu item
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.getMenu().findItem(menuItemId).setChecked(true);
        } else {
            Log.e(TAG, "NavigationView is null");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try {
            int id = item.getItemId();

            if (id == R.id.nav_beranda) {
                navigateToFragment(new HomeFragment(), null, R.id.nav_beranda);
                Log.d(TAG, "Navigated to HomeFragment");
            } else if (id == R.id.nav_penyakit) {
                navigateToFragment(new DiseaseFragment(), null, R.id.nav_penyakit);
                Log.d(TAG, "Navigated to DiseaseFragment");
            } else if (id == R.id.nav_tentang) {
                navigateToFragment(new AboutFragment(), null, R.id.nav_tentang);
                Log.d(TAG, "Navigated to AboutFragment");
            } else if (id == R.id.nav_riwayat) {
                navigateToFragment(new HistoryFragment(), null, R.id.nav_riwayat);
                Log.d(TAG, "Navigated to HistoryFragment");
            } else if (id == R.id.nav_diagnosis) {
                navigateToFragment(new DiagnosisFragment(), username, R.id.nav_diagnosis);
                Log.d(TAG, "Navigated to DiagnosisFragment");
            } else if (id == R.id.nav_logout) {
                logout();
                Log.d(TAG, "Logged out");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling navigation item selection", e);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Handle logout functionality.
     */
    private void logout() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        Intent logoutIntent = new Intent(MainActivity.this, SignInActivity.class);
        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logoutIntent);
        finish();
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
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
            if (!isLoggedIn) {
                Intent loginIntent = new Intent(MainActivity.this, SignInActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status", e);
        }
    }
}