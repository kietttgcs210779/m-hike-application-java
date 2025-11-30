package com.example.mhikeapplication.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mhikeapplication.R;
import com.example.mhikeapplication.data.DatabaseHelper;
import com.example.mhikeapplication.models.HikeObservation;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditObservationActivity extends AppCompatActivity {

    public static final String EXTRA_HIKE_ID = "extra_hike_id";
    public static final String EXTRA_OBSERVATION_ID = "extra_observation_id";

    private Toolbar toolbar;
    private TextInputEditText editTextObservationContent, editTextObservationNotes, editTextObservationDate, editTextObservationTime;
    private Button buttonSaveObservation;
    private DatabaseHelper dbHelper;

    private long hikeId = -1;
    private long observationId = -1;
    private Calendar selectedDateTime = Calendar.getInstance();
    private String originalObservationTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_observation);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();
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
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void setupListeners() {
        buttonSaveObservation.setOnClickListener(v -> saveObservation());
        editTextObservationDate.setOnClickListener(v -> showDatePickerDialog());
        editTextObservationTime.setOnClickListener(v -> showTimePickerDialog());
    }
    
    private void loadObservationData() {
        HikeObservation observation = dbHelper.getObservationById(observationId);
        if (observation != null) {
            editTextObservationContent.setText(observation.getObservationContent());
            editTextObservationNotes.setText(observation.getObservationNotes());
            originalObservationTime = observation.getTimeOfObservation();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date observationDate = sdf.parse(originalObservationTime);
                selectedDateTime.setTime(observationDate);
                updateDateAndTimeViews();
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback if parsing fails
                editTextObservationDate.setText("Error");
                editTextObservationTime.setText("Error");
            }
        } else {
            Toast.makeText(this, "Could not find observation data.", Toast.LENGTH_SHORT).show();
            finish();
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
