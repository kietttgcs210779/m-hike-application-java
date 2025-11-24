package com.example.mhikeapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.mhikeapplication.R;
import com.example.mhikeapplication.adapters.UpcomingHikesAdapter;
import com.example.mhikeapplication.data.DatabaseHelper;
import com.example.mhikeapplication.models.Hike;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHikes;
    private FloatingActionButton fabAddHike;
    private DatabaseHelper dbHelper;
    private Toolbar toolbar;
    private ViewPager2 viewPagerUpcomingHikes;
    private TextView textViewUpcomingTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        toolbar = findViewById(R.id.toolbar_dashboard);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        dbHelper = new DatabaseHelper(this);
        recyclerViewHikes = findViewById(R.id.recyclerViewHikes);
        fabAddHike = findViewById(R.id.fabAddHike);
        viewPagerUpcomingHikes = findViewById(R.id.viewPagerUpcomingHikes);
        textViewUpcomingTitle = findViewById(R.id.textViewUpcomingTitle);

        fabAddHike.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHikes();
    }

    private void loadHikes() {
        List<Hike> allHikes = dbHelper.getAllHikes();
        List<Hike> upcomingHikes = new ArrayList<>();
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Hike hike : allHikes) {
            try {
                Date hikeDate = sdf.parse(hike.getDate());
                if (hikeDate != null && hikeDate.after(currentDate)) {
                    upcomingHikes.add(hike);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (upcomingHikes.isEmpty()) {
            textViewUpcomingTitle.setVisibility(View.GONE);
            viewPagerUpcomingHikes.setVisibility(View.GONE);
        } else {
            textViewUpcomingTitle.setVisibility(View.VISIBLE);
            viewPagerUpcomingHikes.setVisibility(View.VISIBLE);
            UpcomingHikesAdapter upcomingHikesAdapter = new UpcomingHikesAdapter(upcomingHikes);
            viewPagerUpcomingHikes.setAdapter(upcomingHikesAdapter);
        }
    }
}
