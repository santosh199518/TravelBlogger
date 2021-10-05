package com.example.travelblogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class UserData implements Serializable {

    private String username, password, email;
    private Bitmap photo;

    UserData( String username, String password, String email, Bitmap photo){
        this.username = capitalize(username);
        this.password = password;
        this.email = email;
        this.photo=photo;
    }

    UserData(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = capitalize(username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    UserData getUserDataFromDatabase(String email, String password, Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db=helper.getReadableDatabase();
        Cursor c= db.rawQuery("SELECT *"+" FROM "+DBHelper.login_table_name+" where "+
                DBHelper.email+"='"+email+"'",null);
        db.execSQL("UPDATE "+DBHelper.login_table_name+" SET "+DBHelper.signed_in+"='True' WHERE "+DBHelper.email+"='"+email+"'");
        c.moveToFirst();
        if(c.getCount()!=0 && c.getString(c.getColumnIndex(DBHelper.password)).equals(password)){
            UserData user = new UserData();
            user.setEmail(email);
            user.setPassword(password);
            user.setUsername(c.getString(c.getColumnIndex(DBHelper.name)));
            Bitmap pic=getBitmapFromByteArray(c.getBlob(c.getColumnIndex(DBHelper.photo)));
            user.setPhoto(pic);
            c.close();
            return user;
        }
        c.close();
        return null;

    }

    public byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    public Bitmap getBitmapFromByteArray(byte[] byteArray){
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public boolean uploadUserDataToDatabase(Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues data = new ContentValues();
        if (isUserAvailable(context)) {
            data.put(DBHelper.name, username);
            data.put(DBHelper.password, password);
            data.put(DBHelper.email, email);
            data.put(DBHelper.photo, getBitmapAsByteArray(photo));
            data.put(DBHelper.signed_in, "False");
        }
        return db.insert(DBHelper.login_table_name, null, data) != -1;
    }

    public boolean isUserAvailable(Context context){
        DBHelper helper=new DBHelper(context);
        Cursor c=helper.getTable(DBHelper.login_table_name);
        if(c.getCount()==0) return true;
        c.moveToFirst();
        do{
            if(c.getString(c.getColumnIndex(DBHelper.email)).equals(email)) {
                Toast.makeText(context, "Email Already Exits", Toast.LENGTH_SHORT).show();
                c.close();
                return false;
            }
        }while(c.moveToNext());
        c.close();
        return true;
    }

    void logout(Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("UPDATE "+DBHelper.login_table_name+" SET "+DBHelper.signed_in+" = 'False' WHERE "+DBHelper.email+"='"+email+"'");
    }
    public String capitalize(String str){
        String[] sub_strs = str.split(" ");
        String result="";
        for (String sub_str : sub_strs) {
            result = sub_str.substring(0, 1).toUpperCase() + sub_str.substring(1).toLowerCase() + " ";
        }
        return result;
    }

}
