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
    private static final long SPLASH_DELAY = 3000;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        // Enable fullscreen mode
        enableFullScreenMode();

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Use ViewBinding instead of findViewById
        com.example.gastroexpert.databinding.ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "SplashActivity started");

        // Load animation
        loadFadeInAnimation(binding.logo);

        // Delay before transitioning
        handler.postDelayed(this::transitionToSignInActivity, SPLASH_DELAY);
    }

    /**
     * Enables fullscreen mode using modern Android API.
     */
    private void enableFullScreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Objects.requireNonNull(getWindow().getInsetsController()).hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    /**
     * Loads fade-in animation for the logo.
     */
    private void loadFadeInAnimation(ImageView logo) {
        try {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            if (fadeIn != null) {
                logo.startAnimation(fadeIn);
            } else {
                Log.e(TAG, "Failed to load fade-in animation");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading animation", e);
        }
    }

    /**
     * Transitions to SignInActivity.
     */
    private void transitionToSignInActivity() {
        Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
        Log.d(TAG, "Transitioning to SignInActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "SplashActivity destroyed");
    }
}