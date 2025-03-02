package com.example.gastroexpert.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gastroexpert.R;
import com.example.gastroexpert.databinding.ActivitySplashBinding;
import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 3000; // 3 seconds
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize EdgeToEdge (For fullscreen mode)
        EdgeToEdge.enable(this);

        // Initialize ViewBinding
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide the action bar
        hideActionBar();

        // Enable full-screen mode for devices running Android 10 (API level 29) or higher
        enableFullScreenMode();

        // Load and start the fade-in animation for the logo
        loadFadeInAnimation(binding.logo);

        // Post a delayed task to transition to SignInActivity after SPLASH_DELAY (3 seconds)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
            // Load the fade-in animation
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            logo.startAnimation(fadeIn);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load animation", e);
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