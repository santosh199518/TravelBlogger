package com.example.travelblogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PlaceDetails implements Serializable {

    private ArrayList <String> images;
    private String name, description, location, comment;
    private String[] speciality;
    private float rating;
    private int likeCount = 0;

    PlaceDetails(){
        name = "Name of Place";
        description = "This is the description of that place";
        location = "Kathmandu, Province 3, Nepal";
        speciality = new String[]{"Hotel", "Natural", "Food and culture"};
        images = new ArrayList<>();
        rating = 0;
    }
    PlaceDetails(ArrayList <Uri> images, String name, String location, String description, String[] speciality, float rating, String comment){
        this.images = new ArrayList<>();
        if (images == null) images = new ArrayList<>();
        for(Uri imageUri : images){
            Log.d("ImageUri",imageUri.toString());
            this.images.add(imageUri.toString());
        }
        this.name = name;
        this.location = location;
        this.description = description;
        this.speciality = speciality;
        this.rating = rating;
        this.comment = comment;
    }


    public ArrayList <Uri> getImages() {
        ArrayList <Uri> images = new ArrayList<>();
        if(images.size() == 0) return null;
        for(String image : this.images){
            images.add(Uri.parse(image));
        }
        return images;
    }

    public int getLikeCount() { return likeCount; }

    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public String getName() { return name; }

    public String getLocation() { return location; }

    public String getDescription() { return description; }

    public String[] getSpeciality() { return speciality; }

    public float getRating() { return rating; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public void setImage(ArrayList <Uri> images) {
        this.images = new ArrayList<>();
        for(Uri image : images){
            this.images.add(image.toString());
        }
    }

    public void setName(String name) { this.name = name; }

    public void setLocation(String location) { this.location = location; }

    public void setDescription(String description) { this.description = description; }

    public void setSpeciality(String[] speciality) { this.speciality = speciality; }

    public void setRating(float rating) { this.rating = rating; }

    public void addImage(Uri image) {this.images.add(image.toString());}

    public boolean uploadToDatabase(Context context, String userEmail){
        ContentValues data = new ContentValues();
        data.put(DBHelper.name, name);
        data.put(DBHelper.description, description);
        data.put(DBHelper.speciality, Arrays.toString(speciality));
        data.put(DBHelper.location, location);
        data.put(DBHelper.rating, rating);
        data.put(DBHelper.uploaded_by, userEmail);
        data.put(DBHelper.like_count, likeCount);
        data.put(DBHelper.photos, Arrays.toString(images.toArray()));
        data.put(DBHelper.comment,comment);
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.insert(DBHelper.places_table_name,null, data) != -1;
    }

    static ArrayList<PlaceDetails> getPlaceDetailsFromDatabase(Context context){
        ArrayList <PlaceDetails> places = new ArrayList<>();
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c =db.rawQuery("SELECT * FROM "+DBHelper.places_table_name,null);
        if(c.getCount() > 0){
            do{
                String str;
                ArrayList <Uri> photos = new ArrayList<>();
                try {
                    str = c.getString(c.getColumnIndex(DBHelper.photos));
                    str = str.replace("[","").replace("]","");
                    for(String photo: str.split(",")){
                        photos.add(Uri.parse(photo));
                    }
                }catch (CursorIndexOutOfBoundsException e){
                    Log.i("PlacesPhotos",e.getMessage());
                }

                str = c.getString(c.getColumnIndex(DBHelper.speciality));
                str = str.replace("[","").replace("]","");
                String[] specialities = str.split(",");
                PlaceDetails place = new PlaceDetails(photos,
                        c.getString(c.getColumnIndex(DBHelper.name)),
                        c.getString(c.getColumnIndex(DBHelper.location)),
                        c.getString(c.getColumnIndex(DBHelper.description)),
                        specialities,
                        c.getFloat(c.getColumnIndex(DBHelper.rating)),
                        c.getString(c.getColumnIndex(DBHelper.comment)));
                places.add(place);
            }while(c.moveToNext());
        }
        return places;
    }

    public void updateLikeCountToDataBase(Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("UPDATE "+ DBHelper.places_table_name+" SET "+DBHelper.like_count+" = "+likeCount+ " WHERE "+DBHelper.name+"='"+name+"'");
        db.execSQL("UPDATE "+ DBHelper.places_table_name+" SET "+DBHelper.like_count+" = "+likeCount+ " WHERE "+DBHelper.name+"='"+name+"'");
    }

}
