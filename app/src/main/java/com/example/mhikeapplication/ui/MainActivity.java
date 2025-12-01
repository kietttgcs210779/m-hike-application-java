package com.example.mhikeapplication.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mhikeapplication.R;
import com.example.mhikeapplication.data.DatabaseHelper;
import com.example.mhikeapplication.models.Hike;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_HIKE_ID = "extra_hike_id";

    private TextInputLayout textInputLayoutName, textInputLayoutLocation, textInputLayoutDate, textInputLayoutTime, textInputLayoutLength;
    private TextInputEditText editTextName, editTextLocation, editTextDate, editTextTime, editTextLength, editTextDescription;
    private RadioGroup radioGroupParking;
    private Spinner spinnerDifficulty;
    private Button buttonSave, buttonAddCoverPhoto;
    private TextView textViewCoverPhotoPath;
    private DatabaseHelper dbHelper;
    private Toolbar toolbar;

    private String coverPhotoPath = "";
    private ActivityResultLauncher<String> coverPhotoLauncher;
    private long currentHikeId = -1;
    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        initCoverPhotoLauncher();
        setupListeners();
        setupSpinner();

        if (getIntent().hasExtra(EXTRA_HIKE_ID)) {
            currentHikeId = getIntent().getLongExtra(EXTRA_HIKE_ID, -1);
            setTitle("Edit Hike");
            if (currentHikeId != -1) {
                loadHikeData(currentHikeId);
            }
        } else {
            setTitle("Add New Hike");
            updateDateEditText();
            updateTimeEditText();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        textInputLayoutName = findViewById(R.id.textInputLayoutName);
        textInputLayoutLocation = findViewById(R.id.textInputLayoutLocation);
        textInputLayoutDate = findViewById(R.id.textInputLayoutDate);
        textInputLayoutTime = findViewById(R.id.textInputLayoutTime);
        textInputLayoutLength = findViewById(R.id.textInputLayoutLength);
        editTextName = findViewById(R.id.editTextName);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextLength = findViewById(R.id.editTextLength);
        editTextDescription = findViewById(R.id.editTextDescription);
        radioGroupParking = findViewById(R.id.radioGroupParking);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        buttonSave = findViewById(R.id.buttonSave);
        buttonAddCoverPhoto = findViewById(R.id.buttonAddCoverPhoto);
        textViewCoverPhotoPath = findViewById(R.id.textViewCoverPhotoPath);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void initCoverPhotoLauncher() {
        coverPhotoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String newPath = copyImageToInternalStorage(uri);
                        if (newPath != null) {
                            coverPhotoPath = newPath;
                            textViewCoverPhotoPath.setText(new File(newPath).getName());
                        } else {
                            Toast.makeText(this, "Failed to save cover photo.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupListeners() {
        editTextDate.setOnClickListener(view -> showDatePickerDialog());
        editTextTime.setOnClickListener(view -> showTimePickerDialog());
        buttonSave.setOnClickListener(view -> saveHike());
        buttonAddCoverPhoto.setOnClickListener(view -> coverPhotoLauncher.launch("image/*"));
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);
    }

    private void loadHikeData(long hikeId) {
        Hike hike = dbHelper.getHikeById(hikeId);
        if (hike != null) {
            editTextName.setText(hike.getName());
            editTextLocation.setText(hike.getLocation());
            editTextLength.setText(String.valueOf(hike.getLength()));
            editTextDescription.setText(hike.getDescription());

            try {
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                Date date = dateTimeFormat.parse(hike.getDate());
                selectedDateTime.setTime(date);
                updateDateEditText();
                updateTimeEditText();
            } catch (Exception e) {
                editTextDate.setText(hike.getDate());
                editTextTime.setText("");
            }

            if ("Yes".equalsIgnoreCase(hike.getParkingAvailable())) {
                radioGroupParking.check(R.id.radioYes);
            } else {
                radioGroupParking.check(R.id.radioNo);
            }

            String[] difficultyLevels = getResources().getStringArray(R.array.difficulty_array);
            int spinnerPosition = Arrays.asList(difficultyLevels).indexOf(hike.getDifficulty());
            if(spinnerPosition >= 0) {
                spinnerDifficulty.setSelection(spinnerPosition);
            }

            coverPhotoPath = hike.getCoverPhotoPath();
            if (coverPhotoPath != null && !coverPhotoPath.isEmpty()) {
                textViewCoverPhotoPath.setText(new File(coverPhotoPath).getName());
            } else {
                textViewCoverPhotoPath.setText("No cover photo selected");
            }
        }
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateEditText();
                }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePickerDialog() {
        new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateTimeEditText();
                }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), DateFormat.is24HourFormat(this)).show();
    }

    private void updateDateEditText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        editTextDate.setText(dateFormat.format(selectedDateTime.getTime()));
    }

    private void updateTimeEditText() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        editTextTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = "cover_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);

            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
            }
            inputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        if (editTextName.getText().toString().trim().isEmpty()) {
            textInputLayoutName.setError("Name is required");
            isValid = false;
        } else {
            textInputLayoutName.setError(null);
        }

        if (editTextLocation.getText().toString().trim().isEmpty()) {
            textInputLayoutLocation.setError("Location is required");
            isValid = false;
        } else {
            textInputLayoutLocation.setError(null);
        }

        if (editTextDate.getText().toString().trim().isEmpty()) {
            textInputLayoutDate.setError("Date is required");
            isValid = false;
        } else {
            textInputLayoutDate.setError(null);
        }

        if (editTextTime.getText().toString().trim().isEmpty()) {
            textInputLayoutTime.setError("Time is required");
            isValid = false;
        } else {
            textInputLayoutTime.setError(null);
        }

        if (editTextLength.getText().toString().trim().isEmpty()) {
            textInputLayoutLength.setError("Length is required");
            isValid = false;
        } else {
            textInputLayoutLength.setError(null);
        }

        if (radioGroupParking.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select parking availability", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void saveHike() {
        if (!validateInput()) {
            return;
        }

        String name = editTextName.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        double length = Double.parseDouble(editTextLength.getText().toString().trim());
        String difficulty = spinnerDifficulty.getSelectedItem().toString();
        String description = editTextDescription.getText().toString().trim();
        RadioButton selectedRadioButton = findViewById(radioGroupParking.getCheckedRadioButtonId());
        String parkingAvailable = selectedRadioButton.getText().toString();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        String fullDateTime = dateTimeFormat.format(selectedDateTime.getTime());

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_LOCATION, location);
        values.put(DatabaseHelper.COLUMN_DATE, fullDateTime);
        values.put(DatabaseHelper.COLUMN_PARKING_AVAILABLE, parkingAvailable);
        values.put(DatabaseHelper.COLUMN_LENGTH, length);
        values.put(DatabaseHelper.COLUMN_DIFFICULTY, difficulty);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_COVER_PHOTO_PATH, coverPhotoPath);

        if (currentHikeId == -1) {
            long newRowId = dbHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_HIKES, null, values);
            if (newRowId != -1) {
                Toast.makeText(this, "Hike added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error adding hike.", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = dbHelper.updateHike(values, currentHikeId);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Hike updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error updating hike.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
