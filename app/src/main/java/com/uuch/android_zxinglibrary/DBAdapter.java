package com.uuch.android_zxinglibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

    static final String KEY_ROWID = "_id";
    static final String KEY_NAME = "name";
    static final String KEY_EMAIL = "phone";
    static final String KEY_NICK = "nick";
    static final String KEY_TAG = "tag";
    static final String KEY_DEPARTMENT = "department";
    static final String KEY_TYPE = "type";
    static final String KEY_SEX = "sex";
    static final String KEY_CHECKED = "checked";
    static final String TAG = "DBAdapter";

    static final String DATABASE_NAME = "MyDB";
    static final String DATABASE_TABLE = "contacts";
    static final int DATABASE_VERSION = 1;

    static final String DATABASE_CREATE =
            "create table contacts( _id integer primary key autoincrement, " +
                    "name text not null, " +
                    "sex text not null, " +
                    "phone text not null, " +
                    "nick text not null, " +
                    "department text not null, " +
                    "type text not null, " +
                    "checked INTEGER DEFAULT 0, " +
                    "tag text not null, " +
                    "r1 text, r2 text);";

    final Context context;

    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBAdapter(Context cxt)
    {
        this.context = cxt;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {

        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            try
            {
                db.execSQL(DATABASE_CREATE);
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            Log.wtf(TAG, "Upgrading database from version "+ oldVersion + "to "+
                    newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }

    //open the database
    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }
    //close the database
    public void close()
    {
        DBHelper.close();
    }

    //insert a contact into the database
    public long insertContact(String name, String sex, String phone, String department, String type, String tag)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_SEX, sex);
        initialValues.put(KEY_EMAIL, phone);
        initialValues.put(KEY_DEPARTMENT, department);
        // initialValues.put(KEY_NICK, PinyinHelper.getInstance().getFirstPinyins(name));
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_TAG, tag);

        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //delete a particular contact
    public boolean deleteContact(long rowId)
    {
        return db.delete(DATABASE_TABLE, KEY_ROWID + " = " +rowId, null) > 0;
    }

    public boolean deleteContact(String tag)
    {
        return db.delete(DATABASE_TABLE, KEY_TAG + " = \"" +tag + "\"", null) > 0;
    }

    //retreves all the contacts
    public Cursor getAllContacts(String tag)
    {
        return db.query(DATABASE_TABLE, new String[]{KEY_NAME, KEY_SEX, KEY_EMAIL, KEY_DEPARTMENT, KEY_CHECKED, KEY_NICK, KEY_ROWID}, KEY_TAG + " = \"" + tag + "\"", null, null, null, null);
    }
    //retreves all the contacts
    public Cursor getAllContacts(String tag, String type)
    {
        return db.query(DATABASE_TABLE, new String[]{KEY_NAME, KEY_SEX, KEY_EMAIL, KEY_DEPARTMENT, KEY_CHECKED, KEY_NICK, KEY_ROWID}, KEY_TAG + " = \"" + tag + "\" and " + KEY_TYPE + " = \"" + type + "\"", null, null, null, null);
    }

    //checkin
    public boolean updateContact(String name, int checked)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_CHECKED, checked);
        return db.update(DATABASE_TABLE, args, KEY_NAME + "=" + name, null) > 0;
    }
}