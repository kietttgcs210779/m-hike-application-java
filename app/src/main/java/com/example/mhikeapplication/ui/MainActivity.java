package com.example.mhikeapplication.ui;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.mhikeapplication.R;
import com.example.mhikeapplication.data.DatabaseHelper;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText editTextName, editTextLocation, editTextDate, editTextLength;
    private RadioGroup radioGroupParking;
    private RadioButton radioYes, radioNo;
    private Spinner spinnerDifficulty;
    private Button buttonSave;
    private DatabaseHelper dbHelper;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        editTextName = findViewById(R.id.editTextName);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDate = findViewById(R.id.editTextDate);
        editTextLength = findViewById(R.id.editTextLength);
        radioGroupParking = findViewById(R.id.radioGroupParking);
        radioYes = findViewById(R.id.radioYes);
        radioNo = findViewById(R.id.radioNo);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        buttonSave = findViewById(R.id.buttonSave);

        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);

        buttonSave.setOnClickListener(v -> saveHike());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    editTextDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveHike() {
        String name = editTextName.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String lengthStr = editTextLength.getText().toString().trim();
        String difficulty = spinnerDifficulty.getSelectedItem().toString();

        // Validation
        if (name.isEmpty() || location.isEmpty() || date.isEmpty() || lengthStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedParkingId = radioGroupParking.getCheckedRadioButtonId();
        if (selectedParkingId == -1) {
            Toast.makeText(this, "Please select parking availability", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedParkingId);
        String parkingAvailable = selectedRadioButton.getText().toString();
        double length = Double.parseDouble(lengthStr);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_LOCATION, location);
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_PARKING_AVAILABLE, parkingAvailable);
        values.put(DatabaseHelper.COLUMN_LENGTH, length);
        values.put(DatabaseHelper.COLUMN_DIFFICULTY, difficulty);

        long newRowId = db.insert(DatabaseHelper.TABLE_HIKES, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Hike saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving hike.", Toast.LENGTH_SHORT).show();
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
