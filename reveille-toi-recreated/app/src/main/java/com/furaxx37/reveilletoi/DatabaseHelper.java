package com.furaxx37.reveilletoi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "reveille_toi.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table name
    private static final String TABLE_ALARMS = "alarms";
    
    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_MINUTE = "minute";
    private static final String COLUMN_LABEL = "label";
    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_RINGTONE_URI = "ringtone_uri";
    private static final String COLUMN_CREATED_AT = "created_at";
    
    // Create table SQL
    private static final String CREATE_TABLE_ALARMS = 
        "CREATE TABLE " + TABLE_ALARMS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_HOUR + " INTEGER NOT NULL, " +
        COLUMN_MINUTE + " INTEGER NOT NULL, " +
        COLUMN_LABEL + " TEXT, " +
        COLUMN_ENABLED + " INTEGER DEFAULT 1, " +
        COLUMN_RINGTONE_URI + " TEXT, " +
        COLUMN_CREATED_AT + " INTEGER DEFAULT (strftime('%s','now')) " +
        ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_ALARMS);
            Log.d(TAG, "Database table created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database table", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        // For now, just drop and recreate the table
        // In production, you'd want to handle migrations more carefully
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
        onCreate(db);
    }

    /**
     * Insert a new alarm into the database
     */
    public long insertAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_HOUR, alarm.getHour());
            values.put(COLUMN_MINUTE, alarm.getMinute());
            values.put(COLUMN_LABEL, alarm.getLabel());
            values.put(COLUMN_ENABLED, alarm.isEnabled() ? 1 : 0);
            values.put(COLUMN_RINGTONE_URI, alarm.getRingtoneUri());
            values.put(COLUMN_CREATED_AT, System.currentTimeMillis() / 1000);
            
            id = db.insert(TABLE_ALARMS, null, values);
            
            if (id != -1) {
                alarm.setId(id);
                Log.d(TAG, "Alarm inserted with ID: " + id);
            } else {
                Log.e(TAG, "Failed to insert alarm");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error inserting alarm", e);
        } finally {
            db.close();
        }
        
        return id;
    }

    /**
     * Update an existing alarm in the database
     */
    public boolean updateAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_HOUR, alarm.getHour());
            values.put(COLUMN_MINUTE, alarm.getMinute());
            values.put(COLUMN_LABEL, alarm.getLabel());
            values.put(COLUMN_ENABLED, alarm.isEnabled() ? 1 : 0);
            values.put(COLUMN_RINGTONE_URI, alarm.getRingtoneUri());
            
            int rowsAffected = db.update(TABLE_ALARMS, values, 
                COLUMN_ID + " = ?", new String[]{String.valueOf(alarm.getId())});
            
            success = rowsAffected > 0;
            
            if (success) {
                Log.d(TAG, "Alarm updated successfully: " + alarm.getId());
            } else {
                Log.w(TAG, "No alarm found with ID: " + alarm.getId());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating alarm", e);
        } finally {
            db.close();
        }
        
        return success;
    }

    /**
     * Delete an alarm from the database
     */
    public boolean deleteAlarm(long alarmId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        
        try {
            int rowsAffected = db.delete(TABLE_ALARMS, 
                COLUMN_ID + " = ?", new String[]{String.valueOf(alarmId)});
            
            success = rowsAffected > 0;
            
            if (success) {
                Log.d(TAG, "Alarm deleted successfully: " + alarmId);
            } else {
                Log.w(TAG, "No alarm found with ID: " + alarmId);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting alarm", e);
        } finally {
            db.close();
        }
        
        return success;
    }

    /**
     * Get a specific alarm by ID
     */
    public Alarm getAlarm(long alarmId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Alarm alarm = null;
        
        try {
            Cursor cursor = db.query(TABLE_ALARMS, null, 
                COLUMN_ID + " = ?", new String[]{String.valueOf(alarmId)},
                null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                alarm = createAlarmFromCursor(cursor);
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting alarm", e);
        } finally {
            db.close();
        }
        
        return alarm;
    }

    /**
     * Get all alarms from the database
     */
    public List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            Cursor cursor = db.query(TABLE_ALARMS, null, null, null, 
                null, null, COLUMN_HOUR + " ASC, " + COLUMN_MINUTE + " ASC");
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Alarm alarm = createAlarmFromCursor(cursor);
                    if (alarm != null) {
                        alarms.add(alarm);
                    }
                }
                cursor.close();
            }
            
            Log.d(TAG, "Retrieved " + alarms.size() + " alarms from database");
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting all alarms", e);
        } finally {
            db.close();
        }
        
        return alarms;
    }

    /**
     * Get all enabled alarms
     */
    public List<Alarm> getEnabledAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            Cursor cursor = db.query(TABLE_ALARMS, null, 
                COLUMN_ENABLED + " = ?", new String[]{"1"},
                null, null, COLUMN_HOUR + " ASC, " + COLUMN_MINUTE + " ASC");
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Alarm alarm = createAlarmFromCursor(cursor);
                    if (alarm != null) {
                        alarms.add(alarm);
                    }
                }
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting enabled alarms", e);
        } finally {
            db.close();
        }
        
        return alarms;
    }

    /**
     * Create an Alarm object from a database cursor
     */
    private Alarm createAlarmFromCursor(Cursor cursor) {
        try {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            int hour = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOUR));
            int minute = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTE));
            String label = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LABEL));
            boolean enabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENABLED)) == 1;
            String ringtoneUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RINGTONE_URI));
            long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
            
            Alarm alarm = new Alarm(hour, minute, label, enabled, ringtoneUri);
            alarm.setId(id);
            alarm.setCreatedAt(createdAt);
            
            return alarm;
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating alarm from cursor", e);
            return null;
        }
    }

    /**
     * Get the count of alarms in the database
     */
    public int getAlarmCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        
        try {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ALARMS, null);
            if (cursor != null) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting alarm count", e);
        } finally {
            db.close();
        }
        
        return count;
    }
}
