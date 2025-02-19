package com.example.gastroexpert.ui;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.gastroexpert.R;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        CardView cardView = view.findViewById(R.id.cardView);

        if (imageView == null || cardView == null) {
            Log.e("AboutFragment", "Error: ImageView or CardView not found in layout");
            return view;
        }

        // Mendapatkan tinggi layar dalam piksel
        int screenHeightPx = getResources().getDisplayMetrics().heightPixels;

        // Konversi tinggi layar dari piksel ke DP
        float density = getResources().getDisplayMetrics().density;
        int screenHeightDp = (int) (screenHeightPx / density);

        // Logging tinggi layar
        Log.d("AboutFragment", "Screen height in DP: " + screenHeightDp);

        // Mengubah visibilitas gambar berdasarkan tinggi layar
        if (screenHeightDp > 900) {
            imageView.setVisibility(View.VISIBLE);
            if (cardView.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
                layoutParams.setMargins(0, 32, 0, 0);
                cardView.setLayoutParams(layoutParams);
            } else {
                Log.e("AboutFragment", "Error: CardView's parent layout is not a RelativeLayout");
            }
        } else {
            imageView.setVisibility(View.GONE);
            int marginInDp = 10;
            int marginInPixel = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    marginInDp,
                    getResources().getDisplayMetrics()
            );

            if (cardView.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
                layoutParams.setMargins(marginInPixel, marginInPixel, marginInPixel, marginInPixel);
                cardView.setLayoutParams(layoutParams);
            } else {
                Log.e("AboutFragment", "Error: CardView's parent layout is not a RelativeLayout");
            }
        }

        // Logging visibilitas ImageView
        Log.d("AboutFragment", "ImageView visibility: " + (imageView.getVisibility() == View.VISIBLE ? "Visible" : "Gone"));

        return view;
    }
}