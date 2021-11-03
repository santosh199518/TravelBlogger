package com.example.travelblogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import java.util.Arrays;

public class DBHelper extends SQLiteOpenHelper {

    //Name of Database
    public static final String db_name="Let's_Connect_DataBase";

    //Login table name with it's columns name
    public static final String login_table_name ="Login_Credentials";
    public static final String name="NAME";
    public static final String password="PASSWORD";
    public static final String email="EMAIL";
    public static final String id="ID";
    public static final String photo ="PHOTO";
    public static final String signed_in ="SIGNED_IN";
    public static final String favouritePlaces = "FAVOURITE_PLACES";
    public static final String like_places = "LIKED_PLACES";

    //Feedback table name with it's columns name
    public static final String feedback_table_name ="Feedbacks";
    public static final String question1 = "EXPERIENCE_RATING";
    public static final String question2 = "APP_RATING";
    public static final String sub_question1 = "EASE_OF_USE";
    public static final String sub_question2 = "QUALITY";
    public static final String sub_question3 = "DESIGN";
    public static final String sub_question4 = "REDUNDANT";

    //Places Details table name with it's columns name
    public static final String places_table_name = "PLACES_DETAILS";
    public static final String description = "DESCRIPTION";
    public static final String speciality = "SPECIALITY";
    public static final String location = "LOCATION";
    public static final String rating = "RATING";
    public static final String photos = "PHOTOS_URI";
    public static final String uploaded_by = "UPLOADED_BY";
    public static final String like_count = "LIKE_COUNT";
    public static final String comment = "COMMENT";

    Context context;
    public DBHelper(Context context){
        super(context,db_name,null,1);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE IF NOT EXISTS "+login_table_name+
                "("+name +" TEXT NOT NULL, "+
                password +" TEXT NOT NULL, "+
                email +" TEXT UNIQUE NOT NULL,"+
                id +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                photo +" BLOB , "+
                favouritePlaces+" TEXT ,"+
                like_places+" TEXT ,"+
                signed_in +" TEXT NOT NULL)";
        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS "+feedback_table_name+
                "("+id +" INTEGER NOT NULL,"+
                question1 +" DOUBLE NOT NULL, "+
                question2 +" DOUBLE NOT NULL, "+
                sub_question1 +" DOUBLE NOT NULL, "+
                sub_question2 +" DOUBLE NOT NULL, "+
                sub_question3 +" DOUBLE NOT NULL, "+
                sub_question4 +" DOUBLE NOT NULL) ";
        db.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS "+places_table_name+
                "("+name +" TEXT PRIMARY KEY UNIQUE NOT NULL,"+
                description +" TEXT NOT NULL, "+
                speciality +" TEXT, "+
                location +" TEXT NOT NULL, "+
                rating +" DOUBLE , "+
                photos +" TEXT ,"+
                like_count + " INTEGER ,"+
                comment + " TEXT ," +
                uploaded_by +" INTEGER NOT NULL) ";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    static void updateFeedback(Context c, float []answers){
        DBHelper helper = new DBHelper(c);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM "+ login_table_name +" WHERE "+signed_in+"='True'",null);
        cur.moveToFirst();
        int user_id = cur.getInt(cur.getColumnIndex(id));

        ContentValues values = new ContentValues();
        values.put(question1,answers[0]);
        values.put(question2,answers[1]);
        values.put(sub_question1,answers[2]);
        values.put(sub_question2,answers[3]);
        values.put(sub_question3,answers[4]);
        values.put(sub_question4,answers[5]);
        cur = db.rawQuery("SELECT * FROM "+feedback_table_name+" WHERE ID ="+user_id,null);
        cur.moveToFirst();
        if(cur.getCount() == 0){
            values.put(id,user_id);
            Log.d("ratings", Arrays.toString(answers)+"ID: "+user_id);
            if (db.insert(DBHelper.feedback_table_name, null, values) != -1)
                Toast.makeText(c,"Thank You! for your valuable feedback.",Toast.LENGTH_SHORT).show();
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

    static float[] getFeedback(Context context){
        float []feedbacks = new float[]{0, 0, 0, 0, 0, 0};
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM "+ login_table_name +" WHERE "+signed_in+"='True'",null);
        cur.moveToFirst();
        int user_id = cur.getInt(cur.getColumnIndex(id));
        cur = db.rawQuery("SELECT * FROM "+feedback_table_name+" WHERE ID ="+user_id,null);
        if (cur.getCount() != 0) {
            cur.moveToFirst();
            feedbacks[0] = cur.getFloat(cur.getColumnIndex(DBHelper.question1));
            feedbacks[1] = cur.getFloat(cur.getColumnIndex(DBHelper.question2));
            feedbacks[2] = cur.getFloat(cur.getColumnIndex(DBHelper.sub_question1));
            feedbacks[3] = cur.getFloat(cur.getColumnIndex(DBHelper.sub_question2));
            feedbacks[4] = cur.getFloat(cur.getColumnIndex(DBHelper.sub_question3));
            feedbacks[5] = cur.getFloat(cur.getColumnIndex(DBHelper.sub_question4));
        }
        return feedbacks;
    }
}
