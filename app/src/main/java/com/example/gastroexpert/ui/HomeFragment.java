package com.example.gastroexpert.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.gastroexpert.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String PREFS_NAME = "MyPrefs";
    private TextView nameTextView;
    private TextView dateTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI components
        nameTextView = view.findViewById(R.id.name);
        dateTextView = view.findViewById(R.id.tanggal);

        // Retrieve the username from SharedPreferences or fallback to "Guest"
        String username = getUsernameFromPreferences();

        // Display username and current date
        displayUsername(username);
        displayCurrentDate();

        return view;
    }

    /**
     * Retrieve the username from SharedPreferences or fallback to "Guest".
     */
    private String getUsernameFromPreferences() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("username", "Guest"); // Default to "Guest" if not found
    }

    /**
     * Display the username in the TextView.
     *
     * @param username The username to display.
     */
    private void displayUsername(String username) {
        if (nameTextView != null) {
            nameTextView.setText(username);
        } else {
            Log.e(TAG, "nameTextView is null");
        }
    }

    /**
     * Display the current date in the TextView.
     */
    private void displayCurrentDate() {
        if (dateTextView != null) {
            String currentDate = getCurrentDate();
            dateTextView.setText(currentDate);
        } else {
            Log.e(TAG, "dateTextView is null");
        }
    }

    /**
     * Get the current date formatted as "dd/MM/yyyy".
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    /**
     * Perform logout if the user is not logged in.
     */
    private void performLogout() {
        clearLoginStatus();
        redirectToSignInActivity();
    }

    /**
     * Clear login status in SharedPreferences.
     */
    private void clearLoginStatus() {
        SharedPreferences.Editor editor = requireActivity()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit();
        editor.clear();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
    }

    /**
     * Redirect to the SignInActivity.
     */
    private void redirectToSignInActivity() {
        Intent intent = new Intent(requireContext(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if the user is logged in
        if (!isUserLoggedIn()) {
            performLogout(); // Logout and redirect if not logged in
        }
    }

    /**
     * Check if the user is logged in by checking the SharedPreferences.
     */
    private boolean isUserLoggedIn() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("isLoggedIn", false);
    }
}