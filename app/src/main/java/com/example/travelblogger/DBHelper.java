package com.example.travelblogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class DBHelper extends SQLiteOpenHelper {

    public static final String db_name="Let's_Connect_DataBase";
    public static final String login_table_name ="Login_Credentials";
    public static final String name="NAME";
    public static final String password="PASSWORD";
    public static final String email="EMAIL";
    public static final String id="ID";
    public static final String photo ="PHOTO";
    public static final String signed_in ="SIGNED_IN";

    public static final String feedback_table_name ="Feedbacks";


    Context context;
    public DBHelper(Context context){
        super(context,db_name,null,1);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+login_table_name+
                "("+name +" TEXT NOT NULL, "+
                password +" TEXT NOT NULL, "+
                email +" TEXT UNIQUE NOT NULL,"+
                id +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                photo +" Blob , "+
                signed_in +" TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



}
