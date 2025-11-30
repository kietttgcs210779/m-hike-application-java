package com.example.mhikeapplication.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.mhikeapplication.R;
import com.example.mhikeapplication.adapters.HikeAdapter;
import com.example.mhikeapplication.adapters.OnHikeClickListener;
import com.example.mhikeapplication.adapters.UpcomingHikesSliderAdapter;
import com.example.mhikeapplication.data.DatabaseHelper;
import com.example.mhikeapplication.models.Hike;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements OnHikeClickListener {

    private static final int STORAGE_PERMISSION_CODE = 101;

    private RecyclerView recyclerViewAllHikes, recyclerViewRunningHikes;
    private FloatingActionButton fabAddHike;
    private DatabaseHelper dbHelper;
    private Toolbar toolbar;
    private ViewPager2 viewPagerUpcomingHikes;
    private TextView textViewUpcomingTitle, textViewRunningTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        setupToolbar();
        setupRecyclerViews();
        setupListeners();

        dbHelper = new DatabaseHelper(this);
        checkPermissions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_dashboard);
        fabAddHike = findViewById(R.id.fabAddHike);
        viewPagerUpcomingHikes = findViewById(R.id.viewPagerUpcomingHikes);
        textViewUpcomingTitle = findViewById(R.id.textViewUpcomingTitle);
        recyclerViewRunningHikes = findViewById(R.id.recyclerViewRunningHikes);
        textViewRunningTitle = findViewById(R.id.textViewRunningTitle);
        recyclerViewAllHikes = findViewById(R.id.recyclerViewAllHikes);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    private void setupRecyclerViews() {
        recyclerViewRunningHikes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRunningHikes.setNestedScrollingEnabled(false);
        recyclerViewAllHikes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAllHikes.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        fabAddHike.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void checkPermissions() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_CODE);
        } else {
            loadData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            loadData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private void loadData() {
        updateHikeStatuses();
        List<Hike> allHikesList = dbHelper.getAllHikes();
        List<Hike> upcomingHikes = new ArrayList<>();
        List<Hike> runningHikes = new ArrayList<>();

        for (Hike hike : allHikesList) {
            switch (hike.getStatus()) {
                case "Upcoming":
                    upcomingHikes.add(hike);
                    break;
                case "Running":
                    runningHikes.add(hike);
                    break;
            }
        }
        
        updateUI(allHikesList, upcomingHikes, runningHikes);
    }

    private void updateHikeStatuses() {
        List<Hike> upcomingHikes = dbHelper.getAllHikes();
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (Hike hike : upcomingHikes) {
            if ("Upcoming".equals(hike.getStatus())) {
                try {
                    Date hikeDate = sdf.parse(hike.getDate());
                    if (hikeDate != null && hikeDate.before(now)) {
                        dbHelper.updateHikeStatus(hike.getId(), "Running");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateUI(List<Hike> allHikes, List<Hike> upcomingHikes, List<Hike> runningHikes) {
        if (upcomingHikes.isEmpty()) {
            textViewUpcomingTitle.setVisibility(View.GONE);
            viewPagerUpcomingHikes.setVisibility(View.GONE);
        } else {
            textViewUpcomingTitle.setVisibility(View.VISIBLE);
            viewPagerUpcomingHikes.setVisibility(View.VISIBLE);
            viewPagerUpcomingHikes.setAdapter(new UpcomingHikesSliderAdapter(upcomingHikes));
        }

        if(runningHikes.isEmpty()) {
            textViewRunningTitle.setVisibility(View.GONE);
            recyclerViewRunningHikes.setVisibility(View.GONE);
        } else {
            textViewRunningTitle.setVisibility(View.VISIBLE);
            recyclerViewRunningHikes.setVisibility(View.VISIBLE);
            recyclerViewRunningHikes.setAdapter(new HikeAdapter(runningHikes, this));
        }

        recyclerViewAllHikes.setAdapter(new HikeAdapter(allHikes, this));
    }

    @Override
    public void onHikeClick(Hike hike) {
        Intent intent = new Intent(DashboardActivity.this, HikeDetailsActivity.class);
        intent.putExtra(HikeDetailsActivity.EXTRA_HIKE_ID, hike.getId());
        startActivity(intent);
    }
}
