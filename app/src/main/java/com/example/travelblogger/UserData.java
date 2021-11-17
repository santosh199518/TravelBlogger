package com.example.travelblogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;

public class UserData implements Serializable {

    private String username, password, email, imageUri;
    String favouritePlaces, likedPlaces;

    public UserData(String username, String password, String email, String imageUri, String favouritePlaces, String likedPlaces) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.imageUri = imageUri;
        this.favouritePlaces = favouritePlaces;
        this.likedPlaces = likedPlaces;
    }
    UserData(String username, String password, String email, String photo){
        this.username = capitalize(username);
        this.password = password;
        this.email = email;
        if (photo!=null) this.imageUri = photo;
        favouritePlaces = "";
        likedPlaces = "";
    }
    UserData(){
        favouritePlaces = "[]";
        likedPlaces = "[]";
    }

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

    public String getImageUri() { return imageUri; }

    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public void addFavouritePlace(String value) {  }

    public void removeFavouritePlace(String value) {  }

    public void addLikedPlace(String value) {  }

    public void removeLikedPlace(String value) {  }

    public String capitalize(@NonNull String str){
        String[] sub_strs = str.split(" ");
        StringBuilder result= new StringBuilder();
        for (String sub_str : sub_strs) {
            result.append(sub_str.substring(0, 1).toUpperCase()).append(sub_str.substring(1).toLowerCase()).append(" ");
        }
        return result.toString();
    }

    public static boolean hasUser(String email, String password, Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db=helper.getReadableDatabase();
        Cursor c= db.rawQuery("SELECT *"+" FROM "+ DBHelper.login_table_name+" where "+
                DBHelper.email+"='"+email+"' AND "+ DBHelper.password+ "= '"+password+"'",null);
        boolean result = c.getCount() !=0;
        if (result) c.close();
        return result;
    }

    public void uploadUserDataToDatabase(Context context){
        if (hasUser(email,password, context)) {
            return;
        }
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put(DBHelper.name, username);
        data.put(DBHelper.password, password);
        data.put(DBHelper.email, email);
        data.put(DBHelper.photo, imageUri);
        data.put(DBHelper.signed_in, "False");
        boolean result = db.insert(DBHelper.login_table_name, null, data) != -1;
        if (result) db.execSQL("UPDATE "+ DBHelper.login_table_name+" SET "+DBHelper.signed_in+"= 'True' WHERE "+DBHelper.email+"='"+email+"'");
    }

    public void uploadFavouritePlacesToDatabase(Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("UPDATE "+ DBHelper.login_table_name+" SET "+DBHelper.favouritePlaces+" = '"+ favouritePlaces + "' WHERE "+DBHelper.email+"='"+email+"'");
    }

    public void uploadLikedPlacesToDatabase(Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("UPDATE "+ DBHelper.login_table_name+" SET "+DBHelper.like_places+" = '"+ likedPlaces + "' WHERE "+DBHelper.email+"='"+email+"'");
    }

    public static UserData getUserDataFromDatabase(String email, String password, Context context){
        if (!hasUser(email,password, context)) return null;
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c= db.rawQuery("SELECT *"+" FROM "+ DBHelper.login_table_name+" where "+
                DBHelper.email+"='"+email+"'",null);
        c.moveToFirst();

        UserData data = new UserData(c.getString(c.getColumnIndex(DBHelper.name)), password, email, c.getString(c.getColumnIndex(DBHelper.photo)));
        data.favouritePlaces = c.getString(c.getColumnIndex(DBHelper.favouritePlaces)).replace("[","").replace("]","");
        data.likedPlaces = c.getString(c.getColumnIndex(DBHelper.like_places)).replace("[","").replace("]","");
        c.close();
        db.execSQL("UPDATE "+ DBHelper.login_table_name+" SET "+DBHelper.signed_in+"= 'True' WHERE "+DBHelper.email+"='"+email+"'");
        return data;
    }

//    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
//        return outputStream.toByteArray();
//    }
//
//    public static Bitmap getBitmapFromByteArray(byte[] byteArray){
//        if(byteArray==null) return null;
//        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//    }

    void logout(Context context){
        FirebaseAuth.getInstance().signOut();
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("UPDATE "+DBHelper.login_table_name+" SET "+ DBHelper.signed_in+" = 'False' WHERE "+DBHelper.email+"='"+email+"'");
    }


}
