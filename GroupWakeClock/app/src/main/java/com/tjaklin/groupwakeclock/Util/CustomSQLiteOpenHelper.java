package com.tjaklin.groupwakeclock.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tjaklin.groupwakeclock.Models.AlarmSQLite;

import java.util.ArrayList;

public class CustomSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = CustomSQLiteOpenHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "baza_alarmi";
    private static final int DATABASE_VERSION = 1;

    public CustomSQLiteOpenHelper(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "Constructor()!");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate()!");

        db.execSQL(AlarmSQLite.CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade()!");
        resetDatabase();
    }

    private void resetDatabase() {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + AlarmSQLite.TABLE);
        database.execSQL(AlarmSQLite.CREATE_TABLE_QUERY);
        database.close();
    }

    /**
     * CRUD FOR Alarms
     *
     * */
    //C
    public boolean insertAlarm(AlarmSQLite alarm) {

        ContentValues values = new ContentValues();
        values.put(AlarmSQLite.COLUMN_DATETIME, alarm.getDatetime());
        values.put(AlarmSQLite.COLUMN_GROUP_NAME, alarm.getGroupname());
        values.put(AlarmSQLite.COLUMN_EMAIL, alarm.getEmail());
        values.put(AlarmSQLite.COLUMN_TYPE, alarm.getType());
        values.put(AlarmSQLite.COLUMN_COMPLEXITY, alarm.getComplexity());
        values.put(AlarmSQLite.COLUMN_QUESTION, alarm.getQuestion());
        values.put(AlarmSQLite.COLUMN_ANSWERS, alarm.getAnswer());
        values.put(AlarmSQLite.COLUMN_GROUP_CHAT_ID, alarm.getGroupChatID());
        values.put(AlarmSQLite.COLUMN_MEMBERSHIP_ID, alarm.getMembershipID());

        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;
        try {
            result = db.insertOrThrow(AlarmSQLite.TABLE, null, values);
            Log.d(TAG, "insertAlarm().result = " + result);
        } catch (SQLException e) {
            // This can be thrown when we try to insert duplicates.
            Log.d(TAG, "insertAlarm(): exception SQLException called!");
        }

        db.close();

        return result != -1;
    }
    //R
        // Read 1 alarm for a certain user
    public AlarmSQLite readAlarm(long datetime, String userEmail) {

        Cursor cursor = getReadableDatabase().query(AlarmSQLite.TABLE, null,
                AlarmSQLite.COLUMN_DATETIME + "=?" + " AND " + AlarmSQLite.COLUMN_EMAIL + "=?",
                new String[]{String.valueOf(datetime), userEmail},
                null, null, null, null);

        if ( (cursor == null) || (!cursor.moveToFirst())) {
            Log.d(TAG, "readAlarm().cursor is either NULL or empty!");
            return null;
        }

        AlarmSQLite newAlarm = new AlarmSQLite(datetime);
        newAlarm.setEmail(userEmail);

        newAlarm.setGroupname(cursor.getString(cursor.getColumnIndex(AlarmSQLite.COLUMN_GROUP_NAME)));
        newAlarm.setType(cursor.getString(cursor.getColumnIndex(AlarmSQLite.COLUMN_TYPE)));
        newAlarm.setComplexity(cursor.getInt(cursor.getColumnIndex(AlarmSQLite.COLUMN_COMPLEXITY)));
        newAlarm.setQuestion(cursor.getBlob(cursor.getColumnIndex(AlarmSQLite.COLUMN_QUESTION)));
        newAlarm.setAnswer(cursor.getString(cursor.getColumnIndex(AlarmSQLite.COLUMN_ANSWERS)));
        newAlarm.setGroupChatID(cursor.getString(cursor.getColumnIndex(AlarmSQLite.COLUMN_GROUP_CHAT_ID)));
        newAlarm.setMembershipID(cursor.getString(cursor.getColumnIndex(AlarmSQLite.COLUMN_MEMBERSHIP_ID)));

        cursor.close();

        return newAlarm;
    }
        // Read all alarms for a certain user
    public ArrayList<AlarmSQLite> readAlarms(String userEmail) {

        Cursor cursor = getReadableDatabase().query(AlarmSQLite.TABLE, null,
                AlarmSQLite.COLUMN_EMAIL + "=?", new String[]{userEmail},
                null, null, null, null);

        if ( (cursor == null) || (!cursor.moveToFirst())) {
            Log.d(TAG, "readAlarms().cursor is either NULL or empty!");
            return null;
        }

        ArrayList<AlarmSQLite> result = new ArrayList<>();

        do {
            long datetime = cursor.getLong(cursor.getColumnIndex(AlarmSQLite.COLUMN_DATETIME));

            AlarmSQLite newAlarm = new AlarmSQLite(datetime);
            newAlarm.setEmail(userEmail);

            newAlarm.setGroupname(cursor.getString(cursor.getColumnIndex(AlarmSQLite.COLUMN_GROUP_NAME)));
            newAlarm.setType(cursor.getString(cursor.getColumnIndex(AlarmSQLite.COLUMN_TYPE)));
            newAlarm.setComplexity(cursor.getInt(cursor.getColumnIndex(AlarmSQLite.COLUMN_COMPLEXITY)));
            newAlarm.setQuestion(cursor.getBlob(cursor.getColumnIndex(AlarmSQLite.COLUMN_QUESTION)));
            newAlarm.setAnswer(cursor.getString(cursor.getColumnIndex(AlarmSQLite.COLUMN_ANSWERS)));
            newAlarm.setGroupChatID(cursor.getString(cursor.getColumnIndex(AlarmSQLite.COLUMN_GROUP_CHAT_ID)));
            newAlarm.setMembershipID(cursor.getString(cursor.getColumnIndex(AlarmSQLite.COLUMN_MEMBERSHIP_ID)));
            result.add(newAlarm);
            Log.d(TAG, "readAlarms(): Alarm added to list!" );

        } while (cursor.moveToNext());

        cursor.close();

        return result;
    }
    //U
        // I don't use updateAlarm()

    //D
    public boolean deleteAlarm(long datetime, String userEmail) {
        SQLiteDatabase db = getWritableDatabase();
        int numberOfDeletedRows = db.delete(AlarmSQLite.TABLE,
                AlarmSQLite.COLUMN_DATETIME + "=?" + " AND " + AlarmSQLite.COLUMN_EMAIL + "=?",
                new String[]{Long.toString(datetime), userEmail});
        Log.d(TAG, "deleted rows = " + numberOfDeletedRows);

        return numberOfDeletedRows == 1;
    }

}
