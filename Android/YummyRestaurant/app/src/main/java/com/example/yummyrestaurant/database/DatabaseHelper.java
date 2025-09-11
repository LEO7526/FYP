package com.example.yummyrestaurant.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "yummyrestaurant.db";
    private static final int DATABASE_VERSION = 3; // 版本号增加以触发onUpgrade

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role"; // 新增角色字段

    public static final String TABLE_REVIEWS = "reviews";
    public static final String COLUMN_REVIEW_ID = "_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_EMAIL = "user_email";
    public static final String COLUMN_RATING = "rating";

    public static final String TABLE_CHAT = "chat";
    public static final String COLUMN_CHAT_ID = "_id";
    public static final String COLUMN_CHAT_MESSAGE = "message";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_ROLE + " TEXT)";
        db.execSQL(CREATE_TABLE_USERS);

        String CREATE_TABLE_REVIEWS = "CREATE TABLE " + TABLE_REVIEWS + " (" +
                COLUMN_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_USER_EMAIL + " TEXT, " +
                COLUMN_RATING + " REAL)";
        db.execSQL(CREATE_TABLE_REVIEWS);

        String CREATE_TABLE_CHAT = "CREATE TABLE " + TABLE_CHAT + " (" +
                COLUMN_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CHAT_MESSAGE + " TEXT)";
        db.execSQL(CREATE_TABLE_CHAT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
        onCreate(db);
    }

    public long insertUser(SQLiteDatabase db, String name, String email, String password, String role) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role); // 新增角色字段

        // Check if the email already exists
        if (userExists(db, email)) {
            Toast.makeText(context, "Email address already existed", Toast.LENGTH_SHORT).show();
            return -1;
        }

        return db.insert(TABLE_USERS, null, values);
    }

    private boolean userExists(SQLiteDatabase db, String email) {
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public long insertReview(SQLiteDatabase db, String userId, String userEmail, double rating) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_USER_EMAIL, userEmail);
        values.put(COLUMN_RATING, rating);

        return db.insert(TABLE_REVIEWS, null, values);
    }

    public Cursor getAllReviews() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_REVIEWS, null);
    }

    public long insertChatMessage(SQLiteDatabase db, String message) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_MESSAGE, message);

        return db.insert(TABLE_CHAT, null, values);
    }

    public Cursor getAllChatMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CHAT, null);
    }

    // Method to get user role from SQLite
    public String getUserRole(SQLiteDatabase db, String userId) {
        String role = null;
        String query = "SELECT " + COLUMN_ROLE + " FROM " + TABLE_USERS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{userId});
        int columnIndex = cursor.getColumnIndex(COLUMN_ROLE);

        if (columnIndex != -1 && cursor.moveToFirst()) {
            role = cursor.getString(columnIndex);
        }

        cursor.close();
        return role;
    }

    // 方法用于获取上下文
    private Context getContext() {
        return context;
    }
}
