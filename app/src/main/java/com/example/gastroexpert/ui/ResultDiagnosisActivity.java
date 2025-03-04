package com.example.gastroexpert.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gastroexpert.R;
import com.example.gastroexpert.database.DatabaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResultDiagnosisActivity extends AppCompatActivity {

    private TextView defenisiHeader;
    private TextView defenisi;
    private TextView solusiHeader;
    private TextView solusi;
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_result_diagnosis);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.diagnosa), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.navbar);
        setSupportActionBar(toolbar);

        LinearLayout btnBeranda = findViewById(R.id.btnBeranda);
        LinearLayout btnDiagnosis = findViewById(R.id.btnDiagnosis);
        TextView containerDataDiterima = findViewById(R.id.dataDiterima);
        TextView tvNamaPenyakit = findViewById(R.id.hd);
        defenisiHeader = findViewById(R.id.defenisiHeader);
        solusiHeader = findViewById(R.id.solusiHeader);
        defenisi = findViewById(R.id.defenisi);
        solusi = findViewById(R.id.solusi);
        ImageView imageView = findViewById(R.id.image);

        Calendar calendar = Calendar.getInstance();

        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());

        String tanggal = simpleDateFormat.format(calendar.getTime());

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        if (databaseHelper.openDatabase())
            sqLiteDatabase = databaseHelper.getReadableDatabase();

        String str_hasil = getIntent().getStringExtra("HASIL");
        String username = getIntent().getStringExtra("username");

        String[] gejala_terpilih = new String[0];
        if (str_hasil != null) {
            gejala_terpilih = str_hasil.split("#");
        }

        HashMap<String, Double> mapHasil = new HashMap<>();

        String query_penyakit = "SELECT id_penyakit FROM penyakit ORDER BY id_penyakit";
        Cursor cursor_penyakit = sqLiteDatabase.rawQuery(query_penyakit, null);
        while (cursor_penyakit.moveToNext()) {
            String id_penyakit = cursor_penyakit.getString(0);

            String query_gejala = "SELECT id_gejala FROM rules WHERE id_penyakit = '" + id_penyakit + "'";
            Cursor cursor_gejala = sqLiteDatabase.rawQuery(query_gejala, null);

            int total_gejala = cursor_gejala.getCount();
            int matched_gejala = 0;

            while (cursor_gejala.moveToNext()) {
                String id_gejala = cursor_gejala.getString(0);
                for (String gejala : gejala_terpilih) {
                    String query_gejala_terpilih = "SELECT id_gejala FROM gejala WHERE nama_gejala = '" + gejala + "'";
                    Cursor cursor_gejala_terpilih = sqLiteDatabase.rawQuery(query_gejala_terpilih, null);
                    cursor_gejala_terpilih.moveToFirst();

                    if (cursor_gejala_terpilih.getString(0).equals(id_gejala)) {
                        matched_gejala++;
                    }
                    cursor_gejala_terpilih.close();
                }
            }
            cursor_gejala.close();

            double probability = ((double) matched_gejala / total_gejala) * 100;
            mapHasil.put(id_penyakit, probability);
        }
        cursor_penyakit.close();

        StringBuffer output_gejala_terpilih = new StringBuffer();
        int no = 1;
        for (String s_gejala_terpilih : gejala_terpilih) {
            output_gejala_terpilih.append(no++)
                    .append(". ")
                    .append(s_gejala_terpilih)
                    .append("\n");
        }

        containerDataDiterima.setText(output_gejala_terpilih);

        Map<String, Double> sortedHasil = sortByValue(mapHasil);

        Map.Entry<String, Double> entry = sortedHasil.entrySet().iterator().next();
        String kode_penyakit = entry.getKey();
        double hasil_cf = entry.getValue();
        int persentase = (int) hasil_cf;
        Log.d("TAG", String.valueOf(hasil_cf));

        String query_penyakit_hasil = "SELECT nama_penyakit FROM penyakit where id_penyakit='" + kode_penyakit + "'";
        Cursor cursor_hasil = sqLiteDatabase.rawQuery(query_penyakit_hasil, null);
        cursor_hasil.moveToFirst();

        String namaPenyakit = cursor_hasil.getString(0);

        String coloredNamaPenyakit = "<font color='#007cfd' ><b>" + namaPenyakit + "</b></font>";
        String persen = "<font color='#007cfd' ><b>" + persentase + "</b></font>";

        tvNamaPenyakit.setText(Html.fromHtml(username + " kemungkinan menderita " + coloredNamaPenyakit + " dengan tingkat kepastian " + persen + "%"));

        cursor_hasil.close();

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
                    defenisiHeader.setText("Apa itu " + namaPenyakit);
                    solusiHeader.setText("Solusi dari " + namaPenyakit);

                    // Membandingkan nama penyakit dengan namaPenyakit
                    assert np != null;
                    if (np.equals(namaPenyakit)) {
                        Glide.with(ResultDiagnosisActivity.this)
                                .load(imageURL)
                                .apply(new RequestOptions().placeholder(R.drawable.gastro_image))  // Placeholder image
                                .error(R.drawable.gastro_image) // Error image if Glide fails
                                .onlyRetrieveFromCache(false) // Prevent caching issues
                                .into(imageView);
                        defenisi.setText(penjelasan);
                        solusi.setText(solution);

                        DatabaseReference riwayatRef = FirebaseDatabase.getInstance().getReference("Riwayat");

                        Map<String, Object> riwayatData = new HashMap<>();
                        riwayatData.put("penyakit", namaPenyakit);
                        riwayatData.put("persentase", persentase);
                        riwayatData.put("tanggal", tanggal);
                        riwayatData.put("hasil", str_hasil);

                        assert username != null;
                        riwayatRef.child(username).push().setValue(riwayatData);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        btnDiagnosis.setOnClickListener(view -> {
            Intent intent = new Intent(ResultDiagnosisActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("navigateTo", "diagnosis");
            startActivity(intent);
            finish();
        });
        btnBeranda.setOnClickListener(view -> {
            // Buat objek fragment yang akan dituju
            Intent masuk = new Intent(getApplicationContext(), MainActivity.class);
            masuk.putExtra("username", username);
            startActivity(masuk);
            finish();
        });
    }

    private static Map<String, Double> sortByValue(Map<String, Double> unsortMap) {
        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Check if the drawer is open, if so, close it
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed(); // If drawer is not open, continue the default back press behavior
        }
    }
}