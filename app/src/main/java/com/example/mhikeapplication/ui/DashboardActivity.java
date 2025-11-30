package com.example.mhikeapplication.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
    private HikeAdapter allHikesAdapter;

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
        allHikesAdapter = new HikeAdapter(new ArrayList<>(), this);
        recyclerViewAllHikes.setAdapter(allHikesAdapter);
    }

    private void setupListeners() {
        fabAddHike.setOnClickListener(view -> {
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
            if ("Upcoming".equals(hike.getStatus())) {
                upcomingHikes.add(hike);
            } else if ("Running".equals(hike.getStatus())) {
                runningHikes.add(hike);
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

        allHikesAdapter.updateData(allHikes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Hike> filteredList = dbHelper.searchHikes(newText);
                allHikesAdapter.updateData(filteredList);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                loadData();
                return true;
            }
        });

        return true;
    }

    @Override
    public void onHikeClick(Hike hike) {
        Intent intent = new Intent(this, HikeDetailsActivity.class);
        intent.putExtra(HikeDetailsActivity.EXTRA_HIKE_ID, hike.getId());
        startActivity(intent);
    }
}
