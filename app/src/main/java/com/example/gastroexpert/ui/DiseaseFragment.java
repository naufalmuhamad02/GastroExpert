package com.example.gastroexpert.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gastroexpert.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DiseaseFragment extends Fragment {

    private static final String TAG = "DiseaseFragment";
    private ProgressBar progressBar;
    private DiseaseAdapter diseaseAdapter;
    private List<Disease> diseaseList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_disease, container, false);

        // Initialize RecyclerView and progress bar
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize list and adapter
        diseaseList = new ArrayList<>();
        diseaseAdapter = new DiseaseAdapter(diseaseList);

        // Set up RecyclerView with layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(diseaseAdapter);
        recyclerView.setHasFixedSize(true); // Optimization for fixed-size list

        // Show loading indicator while fetching data
        progressBar.setVisibility(View.VISIBLE);

        // Get Firebase reference for diseases
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("penyakit");

        // Fetch data from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Hide loading indicator when data is fetched
                progressBar.setVisibility(View.GONE);

                // Clear previous data and reload new data
                diseaseList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Extract disease name and image URL
                    String namaPenyakit = snapshot.child("nama_penyakit").getValue(String.class);
                    String imageURL = snapshot.child("imageURL").getValue(String.class);

                    // If imageURL exists, add the disease to the list
                    if (namaPenyakit != null && imageURL != null && !imageURL.isEmpty()) {
                        diseaseList.add(new Disease(namaPenyakit, imageURL));
                    }
                }

                // Notify adapter about data changes
                diseaseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error here
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Database Error: " + databaseError.getMessage());
                Toast.makeText(getActivity(), "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private static class Disease {
        private final String name;
        private final String imageURL;

        public Disease(String name, String imageURL) {
            this.name = name;
            this.imageURL = imageURL;
        }

        public String getName() {
            return name;
        }

        public String getImageURL() {
            return imageURL;
        }
    }

    private static class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder> {

        private final List<Disease> diseaseList;

        public DiseaseAdapter(List<Disease> diseaseList) {
            this.diseaseList = diseaseList;
        }

        @NonNull
        @Override
        public DiseaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_disease, parent, false);
            return new DiseaseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DiseaseViewHolder holder, int position) {
            Disease disease = diseaseList.get(position);
            holder.bind(disease);
        }

        @Override
        public int getItemCount() {
            return diseaseList.size();
        }

        public static class DiseaseViewHolder extends RecyclerView.ViewHolder {

            private final TextView diseaseNameTextView;
            private final ImageView diseaseImageView;

            public DiseaseViewHolder(View itemView) {
                super(itemView);
                diseaseNameTextView = itemView.findViewById(R.id.diseaseName);
                diseaseImageView = itemView.findViewById(R.id.diseaseImage);
            }

            public void bind(Disease disease) {
                diseaseNameTextView.setText(disease.getName());

                // Load image using Glide with error handling
                Glide.with(itemView.getContext())
                        .load(disease.getImageURL())
                        .apply(new RequestOptions().placeholder(R.drawable.gastro_image))  // Placeholder image
                        .error(R.drawable.gastro_image) // Error image if Glide fails
                        .into(diseaseImageView);
            }
        }
    }
}