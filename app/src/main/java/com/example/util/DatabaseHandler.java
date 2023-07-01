package com.example.util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.item.DownloadList;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "MyBook";

    // book download table name
    private static final String TABLE_NAME_DOWNLOAD = "book_download";

    // book Table Columns names
    private static final String ID = "auto_id";
    private static final String KEY_BOOK_ID = "book_id";
    private static final String KEY_BOOK_TITLE = "book_title";
    private static final String KEY_BOOK_IMAGE = "image";
    private static final String KEY_BOOK_FILE_URL = "book_file_url";
    private static final String KEY_BOOK_AUTHOR_NAME = "book_author_name";


    //book epub Table Columns name
    private static final String KEY_EPUB_BOOK_ID = "id";
    private static final String KEY_EPUB_BOOK_LAST_READ_POSITION = "last_read_position";

    //book pdf Table Columns name
    private static final String KEY_PDF_BOOK_ID = "id";
    private static final String KEY_PDF_BOOK_LAST_READ_POSITION = "pdf_last_read_position";
    // book epub read position table name
    private static final String TABLE_NAME_EPUB = "epub";

    // book pdf read position table name
    private static final String TABLE_NAME_PDF = "pdf";
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_DOWNLOAD = "CREATE TABLE " + TABLE_NAME_DOWNLOAD + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_BOOK_ID + " TEXT,"
                + KEY_BOOK_TITLE + " TEXT," + KEY_BOOK_IMAGE + " TEXT,"
                + KEY_BOOK_AUTHOR_NAME + "" + " TEXT," + KEY_BOOK_FILE_URL + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_DOWNLOAD);


        String CREATE_TABLE_EPUB = "CREATE TABLE " + TABLE_NAME_EPUB + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_EPUB_BOOK_ID + " TEXT,"
                + KEY_EPUB_BOOK_LAST_READ_POSITION + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE_EPUB);

        String CREATE_TABLE_PDF = "CREATE TABLE " + TABLE_NAME_PDF + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_PDF_BOOK_ID + " TEXT,"
                + KEY_PDF_BOOK_LAST_READ_POSITION + " INT"
                + ")";
        db.execSQL(CREATE_TABLE_PDF);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DOWNLOAD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EPUB);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PDF);
        onCreate(db);
    }

    //-------------Download Table-------------------//

    // Adding new book detail
    public void addDownload(DownloadList downloadList) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BOOK_ID, downloadList.getId());
        values.put(KEY_BOOK_TITLE, downloadList.getTitle());
        values.put(KEY_BOOK_IMAGE, downloadList.getImage());
        values.put(KEY_BOOK_AUTHOR_NAME, downloadList.getAuthor());
        values.put(KEY_BOOK_FILE_URL, downloadList.getUrl());

        // Inserting Row
        db.insert(TABLE_NAME_DOWNLOAD, null, values);
        db.close(); // Closing database connection
    }

    // Getting All book
    public List<DownloadList> getDownload() {
        List<DownloadList> downloadLists = new ArrayList<DownloadList>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_DOWNLOAD;

        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DownloadList list = new DownloadList();
                list.setId(cursor.getString(1));
                list.setTitle(cursor.getString(2));
                list.setImage(cursor.getString(3));
                list.setAuthor(cursor.getString(4));
                list.setUrl(cursor.getString(5));

                // Adding book to list
                downloadLists.add(list);
            } while (cursor.moveToNext());
        }

        // return book list
        return downloadLists;
    }

    // Deleting single book
    public void deleteDownloadBook(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_DOWNLOAD, KEY_BOOK_ID + "=" + id, null);
    }

    //check book id in database or not
    public boolean checkIdDownloadBook(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_DOWNLOAD + " WHERE " + KEY_BOOK_ID + "=" + id;
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount() == 0;
    }

    //-------------Download Table-------------------//



    //-------------Download Table-------------------//

    //-------------EPUB Table-------------------//

    // Adding new epub detail
    public void addEpub(String id, String lastReadPosition) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EPUB_BOOK_ID, id);
        values.put(KEY_EPUB_BOOK_LAST_READ_POSITION, lastReadPosition);

        // Inserting Row
        db.insert(TABLE_NAME_EPUB, null, values);
        db.close(); // Closing database connection
    }

    // Getting epub
    public String getEpub(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_NAME_EPUB, new String[]{KEY_EPUB_BOOK_ID,
                        KEY_EPUB_BOOK_LAST_READ_POSITION}, KEY_EPUB_BOOK_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(KEY_EPUB_BOOK_LAST_READ_POSITION));

    }

    // Updating epub in database
    public void updateEpub(String id, String lastReadPosition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EPUB_BOOK_LAST_READ_POSITION, lastReadPosition);
        // updating row
        db.update(TABLE_NAME_EPUB, values, KEY_EPUB_BOOK_ID + "=" + id, null);
    }

    //check epub id in database or not
    public boolean checkIdEpub(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_EPUB + " WHERE " + KEY_EPUB_BOOK_ID + "=" + id;
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount() == 0;
    }

    //-------------EPUB Table-------------------//


    //-------------PDF Table-------------------//

    // Adding new pdf detail
    public void addPdf(String id, int lastReadPosition) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PDF_BOOK_ID, id);
        values.put(KEY_PDF_BOOK_LAST_READ_POSITION, lastReadPosition);

        // Inserting Row
        db.insert(TABLE_NAME_PDF, null, values);
        db.close(); // Closing database connection
    }

    // Getting pdf
    public int getPdf(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_NAME_PDF, new String[]{KEY_PDF_BOOK_ID,
                        KEY_PDF_BOOK_LAST_READ_POSITION}, KEY_PDF_BOOK_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndex(KEY_PDF_BOOK_LAST_READ_POSITION));

    }

    // Updating pdf in database
    public void updatePdf(String id, int lastReadPosition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PDF_BOOK_LAST_READ_POSITION, lastReadPosition);
        // updating row
        db.update(TABLE_NAME_PDF, values, KEY_PDF_BOOK_ID + "=" + id, null);
    }

    //check pdf id in database or not
    public boolean checkIdPdfBook(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_PDF + " WHERE " + KEY_PDF_BOOK_ID + "=" + id;
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount() == 0;
    }

    //-------------PDF Table-------------------//


}
