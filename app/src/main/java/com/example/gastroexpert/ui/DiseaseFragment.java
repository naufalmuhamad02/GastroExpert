package com.example.gastroexpert.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gastroexpert.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DiseaseFragment extends Fragment {

    private LinearLayout cardContainer;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_disease, container, false);

        // Initialize card container
        cardContainer = view.findViewById(R.id.cardContainer);
        cardContainer.removeAllViews(); // Clear any pre-existing views

        // Title for the fragment
        TextView titleTextView = createTitleTextView();
        cardContainer.addView(titleTextView);

        // Get Firebase reference for diseases
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("penyakit");

        // Fetch data from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Extract disease name and image URL
                    String namaPenyakit = snapshot.child("nama_penyakit").getValue(String.class);
                    String imageURL = snapshot.child("imageURL").getValue(String.class);

                    // If imageURL exists, load the card view
                    if (namaPenyakit != null && imageURL != null && !imageURL.isEmpty()) {
                        createCardView(namaPenyakit, imageURL);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error here
                Log.e("DiseaseFragment", "Database Error: " + databaseError.getMessage());
            }
        });

        return view;
    }

    @SuppressLint("SetTextI18n")
    private TextView createTitleTextView() {
        TextView titleTextView = new TextView(requireContext());
        LinearLayout.LayoutParams titleLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleLayoutParams.setMargins(0, 80, 0, 100); // Set margin
        titleTextView.setLayoutParams(titleLayoutParams);
        titleTextView.setText("Daftar Penyakit");
        titleTextView.setTextSize(24);
        titleTextView.setTextColor(getResources().getColor(R.color.Black));
        titleTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return titleTextView;
    }

    /**
     * Creates a CardView for a disease and adds it to the container.
     *
     * @param diseaseName the name of the disease
     * @param imageURL    the URL of the disease's image
     */
    private void createCardView(String diseaseName, String imageURL) {
        // Create CardView
        CardView cardView = new CardView(requireContext());
        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardLayoutParams.setMargins(50, 0, 50, 50); // Set margin for CardView
        cardView.setLayoutParams(cardLayoutParams);
        cardView.setRadius(15);
        cardView.setCardElevation(4);

        // Linear Layout for content inside CardView
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        // ImageView inside the CardView
        ImageView imageView = createImageView(imageURL);

        layout.addView(imageView);

        // TextView for disease name
        TextView textView = createDiseaseNameTextView(diseaseName);
        layout.addView(textView);

        // Add layout to CardView
        cardView.addView(layout);

        // Add onClickListener to store the disease name in SharedPreferences
        cardView.setOnClickListener(v -> saveDiseaseToPreferences(diseaseName));

        // Finally, add the CardView to the container
        cardContainer.addView(cardView);
    }

    private ImageView createImageView(String imageURL) {
        ImageView imageView = new ImageView(requireContext());
        int heightInDp = 180;
        int heightInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightInPx
        );
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Load image using Glide with error handling
        Glide.with(requireContext())
                .load(imageURL)
                .apply(new RequestOptions().placeholder(R.drawable.gastro_image))  // Placeholder image
                .error(R.drawable.gastro_image) // Error image if Glide fails
                .onlyRetrieveFromCache(false) // Prevent caching issues
                .into(imageView);

        return imageView;
    }

    private TextView createDiseaseNameTextView(String diseaseName) {
        TextView textView = new TextView(requireContext());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins(12, 12, 0, 15); // Set margin for text
        textView.setLayoutParams(textParams);
        textView.setText(diseaseName);
        textView.setTextSize(20);
        textView.setTextColor(getResources().getColor(R.color.Black));
        return textView;
    }

    /**
     * Save the disease name to SharedPreferences when a user clicks on a disease card.
     */
    private void saveDiseaseToPreferences(String diseaseName) {
        // Save the selected disease name to SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("DiseasePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selected_disease", diseaseName);  // Save disease name
        editor.apply();  // Apply changes
        Log.d("DiseaseFragment", "Saved disease name: " + diseaseName);
    }
}