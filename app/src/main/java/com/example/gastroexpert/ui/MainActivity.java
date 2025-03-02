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

        // Initialize Edge-to-Edge display for fullscreen
        EdgeToEdge.enable(this);

        // Set up the toolbar and drawer layout
        setUpToolbar();
        setUpDrawerLayout();

        // Retrieve the username from Intent or default to "Guest"
        username = getIntent().getStringExtra("username");
        if (username == null || username.isEmpty()) {
            username = "Guest";  // Default username if none is passed
        }

        // Navigate to the relevant fragment
        handleFragmentNavigation();
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setUpDrawerLayout() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        } else {
            Log.e(TAG, "NavigationView not found in layout");
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, findViewById(R.id.toolbar), R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void handleFragmentNavigation() {
        String navigateTo = getIntent().getStringExtra("navigateTo");

        if ("diagnosis".equals(navigateTo)) {
            navigateToFragment(new DiagnosisFragment(), username, R.id.nav_diagnosis);
        } else {
            navigateToFragment(new HomeFragment(), username, R.id.nav_beranda);
        }
    }

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

        setSelectedMenuItem(menuItemId);
    }

    private void setSelectedMenuItem(int menuItemId) {
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
                navigateToFragment(new HomeFragment(), username, R.id.nav_beranda);
            } else if (id == R.id.nav_penyakit) {
                navigateToFragment(new DiseaseFragment(), username, R.id.nav_penyakit);
            } else if (id == R.id.nav_tentang) {
                navigateToFragment(new AboutFragment(), username, R.id.nav_tentang);
            } else if (id == R.id.nav_riwayat) {
                navigateToFragment(new HistoryFragment(), username, R.id.nav_riwayat);
            } else if (id == R.id.nav_diagnosis) {
                navigateToFragment(new DiagnosisFragment(), username, R.id.nav_diagnosis);
            } else if (id == R.id.nav_logout) {
                logout();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling navigation item selection", e);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            Intent loginIntent = new Intent(MainActivity.this, SignInActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();
        }
    }
}