package com.example.gastroexpert.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.gastroexpert.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReviewActivity extends AppCompatActivity {

    private TextView defenisiHeader;
    private TextView defenisi;
    private TextView solusiHeader;
    private TextView solusi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_review), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.navbar);
        setSupportActionBar(toolbar);

        TextView containerDataDiterima = findViewById(R.id.dataDiterima);
        TextView tvNamaPenyakit = findViewById(R.id.hd);
        defenisiHeader = findViewById(R.id.defenisiHeader);
        solusiHeader = findViewById(R.id.solusiHeader);
        defenisi = findViewById(R.id.defenisi);
        solusi = findViewById(R.id.solusi);
        ImageView imageView = findViewById(R.id.image);

        String str_hasil = getIntent().getStringExtra("hasil");
        String username = getIntent().getStringExtra("username");
        String penyakit = getIntent().getStringExtra("penyakit");
        String persentase = getIntent().getStringExtra("persentase");

        String[] gejala_terpilih = new String[0];
        if (str_hasil != null) {
            gejala_terpilih = str_hasil.split("#");
        }

        StringBuffer output_gejala_terpilih = new StringBuffer();
        int no = 1;
        for (String s_gejala_terpilih : gejala_terpilih) {
            output_gejala_terpilih.append(no++)
                    .append(". ")
                    .append(s_gejala_terpilih)
                    .append("\n");
        }

        containerDataDiterima.setText(output_gejala_terpilih);

        String coloredNamaPenyakit = "<font color='#007cfd' ><b>" + penyakit + "</b></font>";
        String persen = "<font color='#007cfd' ><b>" + persentase + "</b></font>";

        tvNamaPenyakit.setText(Html.fromHtml(username + " kemungkinan menderita " + coloredNamaPenyakit + " dengan tingkat kepastian " + persen + "%"));

        DatabaseReference penyakitRef = FirebaseDatabase.getInstance().getReference("penyakit");
        penyakitRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String np = snapshot.child("nama_penyakit").getValue(String.class);
                    String imageURL = snapshot.child("imageURL").getValue(String.class);
                    String penjelasan = snapshot.child("penjelasan").getValue(String.class);
                    String solution = snapshot.child("solusi").getValue(String.class);
                    defenisiHeader.setText("Apa itu " + penyakit);
                    solusiHeader.setText("Solusi dari " + penyakit);

                    // Membandingkan nama penyakit dengan namaPenyakit
                    assert np != null;
                    if (np.equals(penyakit)) {
                        Glide.with(ReviewActivity.this)
                                .load(imageURL)
                                .into(imageView);
                        defenisi.setText(penjelasan);
                        solusi.setText(solution);

                        break;
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }

        });
    }

    public void onBackButtonClick(View view) {
        finish();
    }
}