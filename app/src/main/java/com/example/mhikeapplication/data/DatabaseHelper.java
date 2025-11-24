package com.example.mhikeapplication.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mhikeapplication.models.Hike;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "mHike.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    public static final String TABLE_HIKES = "hikes";

    // Hike Table Columns
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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HIKES_TABLE = "CREATE TABLE " + TABLE_HIKES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT NOT NULL,"
                + COLUMN_LOCATION + " TEXT NOT NULL,"
                + COLUMN_DATE + " TEXT NOT NULL,"
                + COLUMN_PARKING_AVAILABLE + " TEXT NOT NULL,"
                + COLUMN_LENGTH + " REAL NOT NULL,"
                + COLUMN_DIFFICULTY + " TEXT NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_RATING + " INTEGER,"
                + COLUMN_COVER_PHOTO_PATH + " TEXT,"
                + COLUMN_IS_FAVORITE + " INTEGER NOT NULL DEFAULT 0"
                + ")";
        db.execSQL(CREATE_HIKES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIKES);
        onCreate(db);
    }

    public List<Hike> getAllHikes() {
        List<Hike> hikes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HIKES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
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
                hikes.add(hike);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return hikes;
    }
}
