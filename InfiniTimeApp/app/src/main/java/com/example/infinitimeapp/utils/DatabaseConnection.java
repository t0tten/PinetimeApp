package com.example.infinitimeapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import static com.example.infinitimeapp.common.Constants.DATABASE_NAME;
import static com.example.infinitimeapp.common.Constants.DATABASE_VERSION;

public class DatabaseConnection extends SQLiteOpenHelper {
    private final String TABLE_NAME = "watch";
    private final String COLUMN_NAME_1 = "name";
    private final String ROW_NAME = "pinetime";
    private final String COLUMN_NAME_2 = "mac";

    public DatabaseConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void saveMACToDatabase(String macAddress) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_1, ROW_NAME);
        values.put(COLUMN_NAME_2, macAddress);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public String readMACFromDatabase() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                COLUMN_NAME_2
        };
        String selection = COLUMN_NAME_1 + " = ?";
        String[] selectionArgs = { ROW_NAME };
        String sortOrder = COLUMN_NAME_2 + " DESC";
        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        String mac_address = "";
        if(cursor.moveToFirst()) {
            mac_address = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_2));
        }
        cursor.close();
        db.close();

        return mac_address;
    }

    public void removeMacFromDatabase() {
        SQLiteDatabase db = getReadableDatabase();
        String selection = COLUMN_NAME_1 + " LIKE ?";
        String[] selectionArgs = { ROW_NAME };
        db.delete(TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME +
                "(" + BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME_1 + " TEXT, " +
                COLUMN_NAME_2 + " TEXT)";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
