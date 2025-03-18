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

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.gastroexpert.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private TextView nameTextView;
    private TextView dateTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inisialisasi komponen UI
        nameTextView = view.findViewById(R.id.name);
        dateTextView = view.findViewById(R.id.tanggal);

        // Ambil username dari SharedPreferences atau gunakan "Guest" sebagai default
        String username = getUsernameFromPreferences();
        displayUsername(username);
        displayCurrentDate();

        // Konfigurasi ImageSlider dengan daftar slide
        setupImageSlider(view);

        return view;
    }

    /**
     * Mengambil username dari SharedPreferences.
     * Mengembalikan "Guest" jika tidak ditemukan.
     */
    private String getUsernameFromPreferences() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, "Guest");
    }

    /**
     * Menampilkan username pada TextView.
     */
    private void displayUsername(String username) {
        if (nameTextView != null) {
            nameTextView.setText(username != null ? username : "Guest");
        } else {
            Log.e(TAG, "nameTextView is null");
        }
    }

    /**
     * Menampilkan tanggal saat ini pada TextView.
     */
    private void displayCurrentDate() {
        if (dateTextView != null) {
            dateTextView.setText(getCurrentDate());
        } else {
            Log.e(TAG, "dateTextView is null");
        }
    }

    /**
     * Mengembalikan tanggal saat ini dengan format "dd/MM/yyyy".
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    /**
     * Melakukan logout dan mengarahkan pengguna ke SignInActivity.
     */
    private void performLogout() {
        clearLoginStatus();
        redirectToSignInActivity();
    }

    /**
     * Menghapus status login dari SharedPreferences.
     */
    private void clearLoginStatus() {
        SharedPreferences.Editor editor = requireActivity()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit();
        editor.remove(KEY_USERNAME);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    /**
     * Mengarahkan pengguna ke SignInActivity.
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

        // Cek status login pengguna
        if (!isUserLoggedIn()) {
            performLogout();
        }
    }

    private void setupImageSlider(View view) {
        ImageSlider imageSlider = view.findViewById(R.id.imageSlider);
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.welcome, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.welcome1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.welcome2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.welcome3, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);
    }

    /**
     * Mengecek apakah pengguna telah login.
     */
    private boolean isUserLoggedIn() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}
