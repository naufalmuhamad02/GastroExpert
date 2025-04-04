package com.example.gastroexpert.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gastroexpert.R;
import com.example.gastroexpert.databinding.ActivitySplashBinding;
import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 2000; // Reduced delay to 2 seconds
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Set content view using ViewBinding
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Enable full-screen mode
        enableFullScreenMode();

        // Hide the action bar for the splash screen
        hideActionBar();

        // Load and start the fade-in animation for the logo
        loadFadeInAnimation(binding.logo);

        // Post a delayed task to transition to SignInActivity after SPLASH_DELAY (2 seconds)
        handler.postDelayed(this::transitionToSignInActivity, SPLASH_DELAY);
    }

    /**
     * Hide the action bar for the splash screen.
     */
    private void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    /**
     * Enable full-screen mode with compatibility for different Android versions.
     */
    private void enableFullScreenMode() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // For Android 11 and higher, use insets controller for hiding status and navigation bars
            getWindow().setDecorFitsSystemWindows(false);
            Objects.requireNonNull(getWindow().getInsetsController()).hide(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars()
            );
        } else {
            // For older versions, use system UI visibility flags
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    /**
     * Load and apply a fade-in animation to the logo.
     * @param logo The ImageView to apply the animation.
     */
    private void loadFadeInAnimation(ImageView logo) {
        try {
            int animationResId = R.anim.fade_in;
            if (animationResId == 0) {
                Log.e(TAG, "Failed to load fade-in animation: Invalid resource ID");
                // Fallback: Display the logo without animation
                logo.setVisibility(View.VISIBLE);
                return;
            }

            Animation fadeIn = AnimationUtils.loadAnimation(this, animationResId);
            if (fadeIn == null) {
                Log.e(TAG, "Failed to load fade-in animation: Animation resource is null");
                // Fallback: Display the logo without animation
                logo.setVisibility(View.VISIBLE);
            } else {
                logo.startAnimation(fadeIn);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load fade-in animation: Unexpected error", e);
            // Fallback: Display the logo without animation
            logo.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Failed to load animation", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Transition to the SignInActivity after a delay.
     */
    private void transitionToSignInActivity() {
        // Start SignInActivity and finish this activity
        Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();

        // Apply a fade-in and fade-out transition between activities
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove any pending callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null);
    }
}