package com.example.gastroexpert.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gastroexpert.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 3000; // Durasi splash screen dalam milidetik (3 detik)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        // Aktifkan mode fullscreen
        enableFullScreenMode();

        // Hapus Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_splash);

        // Logging untuk memastikan aktivitas dimuat
        Log.d(TAG, "SplashActivity started");

        // Cek apakah elemen logo ada di layout
        ImageView logo = findViewById(R.id.logo);
        if (logo == null) {
            Log.e(TAG, "Error: ImageView with ID 'logo' not found in layout");
            finish(); // Tutup aktivitas jika logo tidak ditemukan
            return;
        }

        // Load animasi fade-in
        loadFadeInAnimation(logo);

        // Handler untuk delay sebelum pindah ke SignInActivity
        new Handler(Looper.getMainLooper()).postDelayed(this::transitionToSignInActivity, SPLASH_DELAY);
    }

    /**
     * Mengaktifkan mode fullscreen untuk aktivitas ini.
     */
    private void enableFullScreenMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    /**
     * Memuat animasi fade-in untuk elemen logo.
     *
     * @param logo ImageView yang akan dianimasikan.
     */
    private void loadFadeInAnimation(ImageView logo) {
        try {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            logo.startAnimation(fadeIn);
        } catch (Exception e) {
            Log.e(TAG, "Error loading animation", e);
        }
    }

    /**
     * Melakukan transisi ke SignInActivity.
     */
    private void transitionToSignInActivity() {
        Intent signInIntent = new Intent(SplashActivity.this, SignInActivity.class);
        startActivity(signInIntent);
        finish(); // Tutup SplashActivity agar tidak bisa kembali ke layar ini
        Log.d(TAG, "Transitioning to SignInActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Logging untuk memastikan aktivitas dihancurkan
        Log.d(TAG, "SplashActivity destroyed");
    }
}