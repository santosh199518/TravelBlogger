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
    public static final String question1 = "EXPERIENCE_RATING";
    public static final String question2 = "APP_RATING";
    public static final String sub_question1 = "EASE_OF_USE";
    public static final String sub_question2 = "QUALITY";
    public static final String sub_question3 = "DESIGN";
    public static final String sub_question4 = "REDUNDANT";

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

        db.execSQL("CREATE TABLE IF NOT EXISTS "+feedback_table_name+
                "("+id +" INTEGER NOT NULL,"+
                question1 +" INTEGER NOT NULL, "+
                question2 +" INTEGER NOT NULL, "+
                sub_question1 +" INTEGER NOT NULL, "+
                sub_question2 +" INTEGER NOT NULL, "+
                sub_question3 +" INTEGER NOT NULL, "+
                sub_question4 +" INTEGER NOT NULL) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    static void updateFeedback(Context c, int []answers){
        DBHelper helper = new DBHelper(c);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cur = db.rawQuery("SELECT ID FROM "+ login_table_name +" WHERE "+signed_in+" = 'True'",null);
        int user_id = Integer.parseInt(cur.getString(cur.getColumnIndex(id)));
        ContentValues values = new ContentValues();
        values.put(question1,answers[0]);
        values.put(question2,answers[1]);
        values.put(sub_question1,answers[2]);
        values.put(sub_question2,answers[3]);
        values.put(sub_question3,answers[4]);
        values.put(sub_question4,answers[5]);
        cur = db.rawQuery("SELECT * FROM "+feedback_table_name+" WHERE ID ="+id,null);
        if(cur.getCount() == 0){
            values.put(id,user_id);
            if (db.insert(DBHelper.login_table_name, null, values) != -1)
                Toast.makeText(c,"Thank You! for your valueable feedback.",Toast.LENGTH_SHORT).show();
        }
        else{
            db.execSQL("UPDATE "+ feedback_table_name+" SET "+
                    question1 + "= " + answers[0]+ ","+question2 + "= " + answers[1]+","+
                    sub_question1+"= "+answers[2]+ ","+sub_question2+"= "+answers[3]+","+
                    sub_question3+"= "+answers[4]+ ","+sub_question4+"= "+answers[5]+","+
                    " WHERE "+id+"="+user_id);
            Toast.makeText(c,"Your feedback has been updated successfully.",Toast.LENGTH_SHORT).show();
        }
        cur.close();
    }


}
