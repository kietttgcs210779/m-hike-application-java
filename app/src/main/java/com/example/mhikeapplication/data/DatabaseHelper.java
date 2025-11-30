package com.example.mhikeapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mhikeapplication.models.Hike;
import com.example.mhikeapplication.models.HikeImage;
import com.example.mhikeapplication.models.HikeObservation;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mHike.db";
    private static final int DATABASE_VERSION = 8;

    // Hike Table
    public static final String TABLE_HIKES = "hikes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PARKING_AVAILABLE = "parking_available";
    public static final String COLUMN_LENGTH = "length";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_COVER_PHOTO_PATH = "cover_photo_path";
    public static final String COLUMN_IS_FAVORITE = "is_favorite";
    public static final String COLUMN_STATUS = "status";

    // Hike Images Table
    public static final String TABLE_HIKE_IMAGES = "hike_images";
    public static final String COLUMN_IMG_ID = "img_id";
    public static final String COLUMN_HIKE_ID_FK = "hike_id";
    public static final String COLUMN_IMAGE_PATH = "image_path";

    // Hike Notes Table
    public static final String TABLE_HIKE_NOTES = "hike_notes";
    public static final String COLUMN_NOTE_ID = "note_id";
    public static final String COLUMN_NOTE_HIKE_ID_FK = "hike_id";
    public static final String COLUMN_NOTE_CONTENT = "content";
    
    // Hike Observations Table
    public static final String TABLE_HIKE_OBSERVATIONS = "hike_observations";
    public static final String COLUMN_OBSERVATION_ID = "observation_id";
    public static final String COLUMN_OBSERVATION_HIKE_ID_FK = "hike_id";
    public static final String COLUMN_OBSERVATION_CONTENT = "observation_content";
    public static final String COLUMN_OBSERVATION_TIME = "time_of_observation";
    public static final String COLUMN_OBSERVATION_NOTES = "observation_notes";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HIKES_TABLE = "CREATE TABLE " + TABLE_HIKES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_LOCATION + " TEXT NOT NULL, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_PARKING_AVAILABLE + " TEXT NOT NULL, " +
                COLUMN_LENGTH + " REAL NOT NULL, " +
                COLUMN_DIFFICULTY + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_RATING + " REAL, " +
                COLUMN_COVER_PHOTO_PATH + " TEXT, " +
                COLUMN_STATUS + " TEXT NOT NULL DEFAULT 'Upcoming', " +
                COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0" +
                ");";
        db.execSQL(CREATE_HIKES_TABLE);

        String CREATE_HIKE_IMAGES_TABLE = "CREATE TABLE " + TABLE_HIKE_IMAGES + " (" +
                COLUMN_IMG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HIKE_ID_FK + " INTEGER NOT NULL, " +
                COLUMN_IMAGE_PATH + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_HIKE_ID_FK + ") REFERENCES " +
                TABLE_HIKES + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                ");";
        db.execSQL(CREATE_HIKE_IMAGES_TABLE);

        String CREATE_HIKE_NOTES_TABLE = "CREATE TABLE " + TABLE_HIKE_NOTES + " (" +
                COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOTE_HIKE_ID_FK + " INTEGER UNIQUE NOT NULL, " +
                COLUMN_NOTE_CONTENT + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_NOTE_HIKE_ID_FK + ") REFERENCES " +
                TABLE_HIKES + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                ");";
        db.execSQL(CREATE_HIKE_NOTES_TABLE);
        
        String CREATE_HIKE_OBSERVATIONS_TABLE = "CREATE TABLE " + TABLE_HIKE_OBSERVATIONS + " (" +
                COLUMN_OBSERVATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_OBSERVATION_HIKE_ID_FK + " INTEGER NOT NULL, " +
                COLUMN_OBSERVATION_CONTENT + " TEXT NOT NULL, " +
                COLUMN_OBSERVATION_TIME + " TEXT NOT NULL, " +
                COLUMN_OBSERVATION_NOTES + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_OBSERVATION_HIKE_ID_FK + ") REFERENCES " +
                TABLE_HIKES + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                ");";
        db.execSQL(CREATE_HIKE_OBSERVATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIKE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIKE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIKE_OBSERVATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIKES);
        onCreate(db);
    }

    public int updateHike(ContentValues values, long hikeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_HIKES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(hikeId)});
    }

    public void updateHikeStatus(long hikeId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);
        db.update(TABLE_HIKES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(hikeId)});
    }

    public void deleteHike(long hikeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HIKES, COLUMN_ID + " = ?", new String[]{String.valueOf(hikeId)});
    }

    public long addHikeImage(long hikeId, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HIKE_ID_FK, hikeId);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        return db.insert(TABLE_HIKE_IMAGES, null, values);
    }

    public void addOrUpdateNote(long hikeId, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_HIKE_ID_FK, hikeId);
        values.put(COLUMN_NOTE_CONTENT, content);
        db.replace(TABLE_HIKE_NOTES, null, values);
    }

    public String getNoteForHike(long hikeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String note = "";
        Cursor cursor = db.query(TABLE_HIKE_NOTES, new String[]{COLUMN_NOTE_CONTENT}, COLUMN_NOTE_HIKE_ID_FK + " = ?", new String[]{String.valueOf(hikeId)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_CONTENT));
            }
            cursor.close();
        }
        return note;
    }
    public long addObservation(HikeObservation observation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_OBSERVATION_HIKE_ID_FK, observation.getHikeId());
        values.put(COLUMN_OBSERVATION_CONTENT, observation.getObservationContent());
        values.put(COLUMN_OBSERVATION_TIME, observation.getTimeOfObservation());
        values.put(COLUMN_OBSERVATION_NOTES, observation.getObservationNotes());
        return db.insert(TABLE_HIKE_OBSERVATIONS, null, values);
    }

    public int updateObservation(HikeObservation observation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_OBSERVATION_CONTENT, observation.getObservationContent());
        values.put(COLUMN_OBSERVATION_TIME, observation.getTimeOfObservation());
        values.put(COLUMN_OBSERVATION_NOTES, observation.getObservationNotes());
        return db.update(TABLE_HIKE_OBSERVATIONS, values, COLUMN_OBSERVATION_ID + " = ?", new String[]{String.valueOf(observation.getObservationId())});
    }

    public void deleteObservation(long observationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HIKE_OBSERVATIONS, COLUMN_OBSERVATION_ID + " = ?", new String[]{String.valueOf(observationId)});
    }

    public List<HikeObservation> getAllObservationsForHike(long hikeId) {
        List<HikeObservation> observationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String orderBy = "SUBSTR(" + COLUMN_OBSERVATION_TIME + ", 7, 4) ASC, " + 
                "SUBSTR(" + COLUMN_OBSERVATION_TIME + ", 4, 2) ASC, " + 
                "SUBSTR(" + COLUMN_OBSERVATION_TIME + ", 1, 2) ASC, " + 
                "SUBSTR(" + COLUMN_OBSERVATION_TIME + ", 12, 2) ASC, " + 
                "SUBSTR(" + COLUMN_OBSERVATION_TIME + ", 15, 2) ASC";

        Cursor cursor = db.query(TABLE_HIKE_OBSERVATIONS, null, COLUMN_OBSERVATION_HIKE_ID_FK + " = ?", new String[]{String.valueOf(hikeId)}, null, null, orderBy);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                HikeObservation observation = new HikeObservation();
                observation.setObservationId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATION_ID)));
                observation.setHikeId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATION_HIKE_ID_FK)));
                observation.setObservationContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATION_CONTENT)));
                observation.setTimeOfObservation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATION_TIME)));
                observation.setObservationNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATION_NOTES)));
                observationList.add(observation);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return observationList;
    }

    public HikeObservation getObservationById(long observationId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HIKE_OBSERVATIONS, null, COLUMN_OBSERVATION_ID + " = ?", new String[]{String.valueOf(observationId)}, null, null, null);

        HikeObservation observation = null;
        if (cursor != null && cursor.moveToFirst()) {
            observation = new HikeObservation();
            observation.setObservationId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATION_ID)));
            observation.setHikeId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATION_HIKE_ID_FK)));
            observation.setObservationContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATION_CONTENT)));
            observation.setTimeOfObservation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATION_TIME)));
            observation.setObservationNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATION_NOTES)));
            cursor.close();
        }
        return observation;
    }

    public List<Hike> getAllHikes() {
        List<Hike> hikes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String orderByDate = "SUBSTR(" + COLUMN_DATE + ", 7, 4) DESC, " + 
                "SUBSTR(" + COLUMN_DATE + ", 4, 2) DESC, " + 
                "SUBSTR(" + COLUMN_DATE + ", 1, 2) DESC, " + 
                "SUBSTR(" + COLUMN_DATE + ", 12, 2) DESC, " + 
                "SUBSTR(" + COLUMN_DATE + ", 15, 2) DESC";
        Cursor cursor = db.query(TABLE_HIKES, null, null, null, null, null, orderByDate);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    hikes.add(cursorToHike(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return hikes;
    }

    public Hike getHikeById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HIKES, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        Hike hike = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                hike = cursorToHike(cursor);
            }
            cursor.close();
        }
        return hike;
    }

    public List<HikeImage> getImagesForHike(long hikeId) {
        List<HikeImage> images = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HIKE_IMAGES, null, COLUMN_HIKE_ID_FK + " = ?", new String[]{String.valueOf(hikeId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                HikeImage image = new HikeImage();
                image.setImgId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_IMG_ID)));
                image.setHikeId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HIKE_ID_FK)));
                image.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
                images.add(image);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return images;
    }

    private Hike cursorToHike(Cursor cursor) {
        Hike hike = new Hike();
        hike.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        hike.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        hike.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
        hike.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
        hike.setParkingAvailable(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARKING_AVAILABLE)));
        hike.setLength(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LENGTH)));
        hike.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY)));
        hike.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
        hike.setRating(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RATING)));
        hike.setCoverPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COVER_PHOTO_PATH)));
        hike.setIsFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)));
        hike.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
        return hike;
    }
}
