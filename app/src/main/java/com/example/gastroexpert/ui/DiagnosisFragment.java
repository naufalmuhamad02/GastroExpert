package com.example.gastroexpert.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.gastroexpert.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DiagnosisFragment extends Fragment {

    private final ArrayList<CheckBox> checkBoxList = new ArrayList<>();
    private String username;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagnosis, container, false);

        // Initialize progress bar and other UI components
        progressBar = view.findViewById(R.id.progressBar);
        LinearLayout cardContainer = view.findViewById(R.id.cardContainer);
        cardContainer.removeAllViews();
        cardContainer.setPadding(20, 20, 20, 125);

        // Title text
        TextView titleTextView = createTitleTextView();
        cardContainer.addView(titleTextView);

        // Requirement layout
        LinearLayout parentLayout = createRequirementLayout();
        cardContainer.addView(parentLayout);

        // Fetch gejala data from Firebase
        fetchGejalaData(view);

        // Get username from arguments
        Bundle args = getArguments();
        if (args != null) {
            username = args.getString("username");
        }

        // Proses button
        Button prosesButton = view.findViewById(R.id.prosesButton);
        prosesButton.setOnClickListener(v -> handleProsesButtonClick());

        return view;
    }

    private void fetchGejalaData(View view) {
        // Show loading indicator while fetching data
        progressBar.setVisibility(View.VISIBLE);

        // Firebase reference for "gejala" data
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("gejala");
        Query query = databaseReference.orderByChild("id_gejala");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Hide loading indicator once data is fetched
                progressBar.setVisibility(View.GONE);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String idg = snapshot.child("id_gejala").getValue(String.class);
                    String ng = snapshot.child("nama_gejala").getValue(String.class);

                    if (idg != null && ng != null) {
                        addSymptomCard(view, idg, ng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load gejala data", Toast.LENGTH_SHORT).show();
                Log.e("DiagnosisFragment", "Error fetching data: " + databaseError.getMessage());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private TextView createTitleTextView() {
        TextView titleTextView = new TextView(requireContext());
        LinearLayout.LayoutParams titleLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleLayoutParams.setMargins(0, 50, 0, 20);
        titleTextView.setLayoutParams(titleLayoutParams);
        titleTextView.setText("Daftar Gejala");
        titleTextView.setTextSize(24);
        titleTextView.setTextColor(getResources().getColor(R.color.Black));
        titleTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return titleTextView;
    }

    @SuppressLint("SetTextI18n")
    private LinearLayout createRequirementLayout() {
        LinearLayout parentLayout = new LinearLayout(requireContext());
        parentLayout.setOrientation(LinearLayout.VERTICAL);
        parentLayout.setPadding(8, 8, 8, 8);
        parentLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        TextView titleText = new TextView(requireContext());
        titleText.setText("Ketentuan");
        titleText.setTextSize(20);
        parentLayout.addView(titleText);

        TextView minRequirementTextView = new TextView(requireContext());
        minRequirementTextView.setText("- minimal pilih 5 gejala");
        minRequirementTextView.setTextSize(16);
        parentLayout.addView(minRequirementTextView);

        TextView maxRequirementTextView = new TextView(requireContext());
        maxRequirementTextView.setText("- maksimal pilih 8 gejala");
        maxRequirementTextView.setTextSize(16);
        parentLayout.addView(maxRequirementTextView);

        return parentLayout;
    }

    private void addSymptomCard(View view, String idg, String ng) {
        CardView cardView = new CardView(requireContext());
        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardLayoutParams.setMargins(20, 0, 20, 60);
        cardView.setLayoutParams(cardLayoutParams);
        cardView.setCardElevation(5);
        cardView.setRadius(30);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(16, 16, 16, 16);

        CheckBox checkBox = new CheckBox(requireContext());
        checkBox.setId(idg.hashCode());
        LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        checkBoxParams.weight = 1;
        checkBox.setLayoutParams(checkBoxParams);
        checkBox.setText(ng);
        layout.addView(checkBox);

        checkBoxList.add(checkBox);
        cardView.addView(layout);
        ((LinearLayout) view.findViewById(R.id.cardContainer)).addView(cardView);
    }

    private void handleProsesButtonClick() {
        StringBuilder gejalaTerpilih = new StringBuilder();
        int checkedCount = 0;

        for (CheckBox checkBox : checkBoxList) {
            if (checkBox.isChecked()) {
                gejalaTerpilih.append(checkBox.getText().toString()).append("#");
                checkedCount++;
            }
        }

        if (checkedCount == 0) {
            Toast.makeText(requireContext(), "Silakan pilih gejala dahulu!", Toast.LENGTH_SHORT).show();
        } else if (checkedCount < 5) {
            Toast.makeText(requireContext(), "Minimal pilih 5 gejala!", Toast.LENGTH_SHORT).show();
        } else if (checkedCount > 8) {
            Toast.makeText(requireContext(), "Maksimal pilih 8 gejala!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(requireContext(), ResultDiagnosisActivity.class);
            intent.putExtra("HASIL", gejalaTerpilih.toString());
            intent.putExtra("username", username);
            startActivity(intent);
            requireActivity().finish();
        }
    }
}