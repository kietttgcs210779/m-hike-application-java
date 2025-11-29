package com.example.mhikeapplication.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhikeapplication.R;
import com.example.mhikeapplication.adapters.HikeImageAdapter;
import com.example.mhikeapplication.data.DatabaseHelper;
import com.example.mhikeapplication.models.Hike;
import com.example.mhikeapplication.models.HikeImage;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HikeDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_HIKE_ID = "extra_hike_id";
    private static final long AUTO_SAVE_DELAY_MS = 1500;

    private TextView textViewDetailName, textViewDetailLocation, textViewDetailDate,
            textViewDetailParking, textViewDetailLength, textViewDetailDifficulty, textViewDetailDescription;
    private Button buttonAddPhoto, buttonEdit, buttonDelete;
    private DatabaseHelper dbHelper;
    private long hikeId;
    private Hike currentHike;
    private RecyclerView recyclerViewImages;
    private View editDeleteButtonContainer;
    private TextInputLayout notesLayout;
    private TextInputEditText editTextNotes;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private Uri tempImageUri;
    private final Handler autoSaveHandler = new Handler(Looper.getMainLooper());
    private Runnable autoSaveRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_details);

        Toolbar toolbar = findViewById(R.id.toolbar_details);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        dbHelper = new DatabaseHelper(this);
        initViews();
        initLaunchers();
        setupListeners();

        hikeId = getIntent().getLongExtra(EXTRA_HIKE_ID, -1);

        if (hikeId == -1) {
            Toast.makeText(this, "Error: Hike not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHikeDetails();
    }

    private void initViews() {
        textViewDetailName = findViewById(R.id.textViewDetailName);
        textViewDetailLocation = findViewById(R.id.textViewDetailLocation);
        textViewDetailDate = findViewById(R.id.textViewDetailDate);
        textViewDetailParking = findViewById(R.id.textViewDetailParking);
        textViewDetailLength = findViewById(R.id.textViewDetailLength);
        textViewDetailDifficulty = findViewById(R.id.textViewDetailDifficulty);
        textViewDetailDescription = findViewById(R.id.textViewDetailDescription);
        buttonAddPhoto = findViewById(R.id.buttonAddPhoto);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        editDeleteButtonContainer = findViewById(R.id.edit_delete_button_container);
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        notesLayout = findViewById(R.id.notes_layout);
        editTextNotes = findViewById(R.id.editTextNotes);
    }

    private void initLaunchers() {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String newPath = copyImageToInternalStorage(uri);
                        if (newPath != null) {
                            dbHelper.addHikeImage(hikeId, newPath);
                            loadHikeImages();
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && tempImageUri != null) {
                        dbHelper.addHikeImage(hikeId, tempImageUri.getPath());
                        loadHikeImages();
                    }
                });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openCamera() {
        tempImageUri = createTempImageUri();
        if (tempImageUri != null) {
            cameraLauncher.launch(tempImageUri);
        }
    }

    private Uri createTempImageUri() {
        File tempFile;
        try {
            String fileName = "hike_" + hikeId + "_cam_" + System.currentTimeMillis();
            tempFile = File.createTempFile(fileName, ".jpg", getCacheDir());
            return FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = "hike_" + hikeId + "_img_" + System.currentTimeMillis() + ".jpg";
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

    private void setupListeners() {
        buttonDelete.setOnClickListener(view -> showDeleteConfirmationDialog());
        buttonEdit.setOnClickListener(view -> {
            Intent intent = new Intent(HikeDetailsActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_HIKE_ID, hikeId);
            startActivity(intent);
        });
        buttonAddPhoto.setOnClickListener(view -> showAddPhotoDialog());

        editTextNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                autoSaveHandler.removeCallbacks(autoSaveRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                autoSaveRunnable = () -> dbHelper.addOrUpdateNote(hikeId, s.toString());
                autoSaveHandler.postDelayed(autoSaveRunnable, AUTO_SAVE_DELAY_MS);
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hike")
                .setMessage("Are you sure you want to delete this hike? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteHike(hikeId);
                    Toast.makeText(HikeDetailsActivity.this, "Hike deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showAddPhotoDialog() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            } else if (options[item].equals("Choose from Gallery")) {
                galleryLauncher.launch("image/*");
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void loadHikeDetails() {
        currentHike = dbHelper.getHikeById(hikeId);
        if (currentHike == null) {
            finish();
            return;
        }

        textViewDetailName.setText(currentHike.getName());
        textViewDetailLocation.setText("Location: " + currentHike.getLocation());
        textViewDetailDate.setText("Date: " + currentHike.getDate());
        textViewDetailParking.setText("Parking: " + currentHike.getParkingAvailable());
        textViewDetailLength.setText("Length: " + currentHike.getLength() + " km");
        textViewDetailDifficulty.setText("Difficulty: " + currentHike.getDifficulty());

        if (currentHike.getDescription() != null && !currentHike.getDescription().isEmpty()) {
            textViewDetailDescription.setText("Description: " + currentHike.getDescription());
            textViewDetailDescription.setVisibility(View.VISIBLE);
        } else {
            textViewDetailDescription.setVisibility(View.GONE);
        }

        String note = dbHelper.getNoteForHike(hikeId);
        if (note != null) {
            editTextNotes.setText(note);
        }

        updateUIVisibility();
        loadHikeImages();
    }

    private void updateUIVisibility() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date hikeDate = sdf.parse(currentHike.getDate());
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0); todayCal.set(Calendar.MINUTE, 0); todayCal.set(Calendar.SECOND, 0); todayCal.set(Calendar.MILLISECOND, 0);
            Date today = todayCal.getTime();

            if (!hikeDate.after(today)) {
                editDeleteButtonContainer.setVisibility(View.GONE);
                buttonAddPhoto.setVisibility(View.VISIBLE);
                recyclerViewImages.setVisibility(View.VISIBLE);
                notesLayout.setVisibility(View.VISIBLE);
            } else {
                editDeleteButtonContainer.setVisibility(View.VISIBLE);
                buttonAddPhoto.setVisibility(View.GONE);
                recyclerViewImages.setVisibility(View.GONE);
                notesLayout.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            editDeleteButtonContainer.setVisibility(View.GONE);
            buttonAddPhoto.setVisibility(View.GONE);
            recyclerViewImages.setVisibility(View.GONE);
            notesLayout.setVisibility(View.GONE);
        }
    }

    private void loadHikeImages() {
        List<HikeImage> imageList = dbHelper.getImagesForHike(hikeId);
        HikeImageAdapter adapter = new HikeImageAdapter(imageList);
        recyclerViewImages.setAdapter(adapter);
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
