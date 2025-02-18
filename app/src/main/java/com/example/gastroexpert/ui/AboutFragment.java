package com.example.gastroexpert.ui;

import android.os.Bundle;
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

        ImageView imageView = view.findViewById(R.id.imageView);  // Gambar bawah
        CardView cardView = view.findViewById(R.id.cardView);

        // Mendapatkan tinggi layar dalam dp
        int screenHeightDp = getResources().getConfiguration().screenHeightDp;

        // Mengubah visibilitas gambar berdasarkan tinggi layar
        if (screenHeightDp > 900) {
            imageView.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
            layoutParams.setMargins(0, 32, 0, 0);
            cardView.setLayoutParams(layoutParams);
        } else {
            imageView.setVisibility(View.GONE);
            int marginInDp = 10;
            float density = getResources().getDisplayMetrics().density;
            int marginInPixel = Math.round(marginInDp * density);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
            layoutParams.setMargins(marginInPixel, marginInPixel, marginInPixel, marginInPixel);
            cardView.setLayoutParams(layoutParams);
        }

        return view;
    }
}
