package com.example.gastroexpert.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.gastroexpert.R;

public class AboutFragment extends Fragment {
    private static final String TAG = "AboutFragment";
    private static final int SCREEN_HEIGHT_THRESHOLD_DP = 900;
    private static final int LARGE_SCREEN_MARGIN_TOP_DP = 32;
    private static final int SMALL_SCREEN_MARGIN_DP = 10;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        // Fix casting issue: CardView instead of RelativeLayout
        CardView cardView = view.findViewById(R.id.cardView);

        if (imageView == null || cardView == null) {
            Log.e(TAG, "Error: ImageView or CardView not found in layout");
            return view;
        }

        adjustUIForScreenSize(imageView, cardView);

        return view;
    }

    private void adjustUIForScreenSize(ImageView imageView, CardView cardView) {
        int screenHeightDp = getScreenHeightInDp();
        Log.d(TAG, "Screen height in DP: " + screenHeightDp);

        // Adjust layout based on screen height
        if (screenHeightDp > SCREEN_HEIGHT_THRESHOLD_DP) {
            imageView.setVisibility(View.VISIBLE);
            adjustCardViewMargins(cardView, dpToPx(LARGE_SCREEN_MARGIN_TOP_DP), 0, 0, 0);
        } else {
            imageView.setVisibility(View.GONE);
            int marginInPx = dpToPx(SMALL_SCREEN_MARGIN_DP);
            adjustCardViewMargins(cardView, marginInPx, marginInPx, marginInPx, marginInPx);
        }

        Log.d(TAG, "ImageView visibility: " + (imageView.getVisibility() == View.VISIBLE ? "Visible" : "Gone"));
    }

    private void adjustCardViewMargins(CardView cardView, int top, int bottom, int start, int end) {
        ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();

        if (layoutParams instanceof CardView.LayoutParams) {
            CardView.LayoutParams cardViewLayoutParams = (CardView.LayoutParams) layoutParams;
            cardViewLayoutParams.setMargins(start, top, end, bottom);
            cardView.setLayoutParams(cardViewLayoutParams);
        } else {
            Log.e(TAG, "Error: CardView's parent layout is not a CardView or LinearLayout");
        }
    }

    private int getScreenHeightInDp() {
        int screenHeightPx = getResources().getDisplayMetrics().heightPixels;
        float density = getResources().getDisplayMetrics().density;
        return (int) (screenHeightPx / density);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
