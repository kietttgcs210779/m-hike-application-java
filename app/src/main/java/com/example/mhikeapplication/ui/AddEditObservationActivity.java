package com.example.mhikeapplication.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.mhikeapplication.R;
import com.example.mhikeapplication.data.DatabaseHelper;
import com.example.mhikeapplication.models.HikeObservation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditObservationActivity extends AppCompatActivity {

    public static final String EXTRA_HIKE_ID = "extra_hike_id";
    public static final String EXTRA_OBSERVATION_ID = "extra_observation_id";

    private Toolbar toolbar;
    private TextInputEditText editTextObservationContent, editTextObservationNotes, editTextObservationDate, editTextObservationTime;
    private Button buttonSaveObservation, buttonGetLocation;
    private TextView textViewLocation;
    private DatabaseHelper dbHelper;

    private long hikeId = -1;
    private long observationId = -1;
    private Calendar selectedDateTime = Calendar.getInstance();
    private String originalObservationTime;
    private String observationLocation = "";

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_observation);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupToolbar();
        initLocationLauncher();
        setupListeners();

        hikeId = getIntent().getLongExtra(EXTRA_HIKE_ID, -1);
        if (getIntent().hasExtra(EXTRA_OBSERVATION_ID)) {
            observationId = getIntent().getLongExtra(EXTRA_OBSERVATION_ID, -1);
            setTitle("Edit Observation");
            if (observationId != -1) {
                loadObservationData();
            }
        } else {
            setTitle("Add New Observation");
            updateDateAndTimeViews();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_add_observation);
        editTextObservationContent = findViewById(R.id.editTextObservationContent);
        editTextObservationNotes = findViewById(R.id.editTextObservationNotes);
        editTextObservationDate = findViewById(R.id.editTextObservationDate);
        editTextObservationTime = findViewById(R.id.editTextObservationTime);
        buttonSaveObservation = findViewById(R.id.buttonSaveObservation);
        buttonGetLocation = findViewById(R.id.buttonGetLocation);
        textViewLocation = findViewById(R.id.textViewLocation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void initLocationLauncher() {
        locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        getCurrentLocation();
                    } else {
                        Toast.makeText(this, "Location permission is required to get your position.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupListeners() {
        buttonSaveObservation.setOnClickListener(view -> saveObservation());
        editTextObservationDate.setOnClickListener(view -> showDatePickerDialog());
        editTextObservationTime.setOnClickListener(view -> showTimePickerDialog());
        buttonGetLocation.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                String addressString = address.getAddressLine(0);
                                observationLocation = addressString;
                                textViewLocation.setText(addressString);
                            } else {
                                observationLocation = location.getLatitude() + ", " + location.getLongitude();
                                textViewLocation.setText("Lat: " + location.getLatitude() + "\nLon: " + location.getLongitude());
                                Toast.makeText(this, "Could not find address for this location.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            observationLocation = location.getLatitude() + ", " + location.getLongitude();
                            textViewLocation.setText("Lat: " + location.getLatitude() + "\nLon: " + location.getLongitude());
                            Toast.makeText(this, "Address lookup service not available.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Could not get location. Make sure location is enabled.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadObservationData() {
        HikeObservation observation = dbHelper.getObservationById(observationId);
        if (observation != null) {
            editTextObservationContent.setText(observation.getObservationContent());
            editTextObservationNotes.setText(observation.getObservationNotes());
            originalObservationTime = observation.getTimeOfObservation();
            observationLocation = observation.getObservationLocation();
            
            if(observationLocation != null && !observationLocation.isEmpty()){
                textViewLocation.setText(observationLocation);
            } else {
                textViewLocation.setText("Location not set");
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date observationDate = sdf.parse(originalObservationTime);
                selectedDateTime.setTime(observationDate);
                updateDateAndTimeViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateAndTimeViews();
                }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePickerDialog() {
        new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateAndTimeViews();
                }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), DateFormat.is24HourFormat(this)).show();
    }

    private void updateDateAndTimeViews(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editTextObservationDate.setText(dateFormat.format(selectedDateTime.getTime()));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        editTextObservationTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveObservation() {
        String content = editTextObservationContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Observation content cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = editTextObservationNotes.getText().toString().trim();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String fullDateTime = sdf.format(selectedDateTime.getTime());

        HikeObservation observation = new HikeObservation();
        observation.setHikeId(hikeId);
        observation.setObservationContent(content);
        observation.setObservationNotes(notes);
        observation.setObservationLocation(observationLocation);

        if (observationId == -1) {
            observation.setTimeOfObservation(fullDateTime);
            long newId = dbHelper.addObservation(observation);
            if (newId != -1) {
                Toast.makeText(this, "Observation added.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error adding observation.", Toast.LENGTH_SHORT).show();
            }
        } else {
            observation.setObservationId(observationId);
            if (fullDateTime.equals(originalObservationTime)) {
                 observation.setTimeOfObservation(originalObservationTime);
            } else {
                 observation.setTimeOfObservation(fullDateTime);
            }
            
            int rows = dbHelper.updateObservation(observation);
            if (rows > 0) {
                Toast.makeText(this, "Observation updated.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error updating observation.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
