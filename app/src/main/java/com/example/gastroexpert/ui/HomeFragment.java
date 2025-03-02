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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Get the username from the arguments or default to "Guest"
        String username = getUsernameFromArguments();

        // Log the username for debugging
        Log.d(TAG, "Username: " + username);

        // Display username in the TextView
        displayUsername(view, username);

        // Display current date
        displayCurrentDate(view);

        return view;
    }

    /**
     * Retrieve the username passed from MainActivity or fallback to "Guest".
     */
    private String getUsernameFromArguments() {
        return getArguments() != null ? getArguments().getString("username") : "Guest";
    }

    /**
     * Display the username in the TextView.
     *
     * @param view The root view of the fragment.
     * @param username The username to display.
     */
    private void displayUsername(View view, String username) {
        TextView nameTextView = view.findViewById(R.id.name);
        nameTextView.setText(username);
    }

    /**
     * Display the current date in the TextView.
     *
     * @param view The root view of the fragment.
     */
    private void displayCurrentDate(View view) {
        TextView dateTextView = view.findViewById(R.id.tanggal);
        String currentDate = getCurrentDate();
        dateTextView.setText(currentDate);
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
                .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
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
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("isLoggedIn", false);
    }
}
