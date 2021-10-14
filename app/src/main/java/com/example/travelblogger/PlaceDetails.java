package com.example.travelblogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import java.io.Serializable;
import java.util.ArrayList;

public class PlaceDetails implements Serializable {
    private ArrayList <byte[]> images;
    private String name, description, speciality;
    private String location;
    private float rating;
    PlaceDetails(){
        images = new ArrayList<>();
        rating = 0;
    }
    PlaceDetails(ArrayList <Bitmap> images, String name, String location, String description, String speciality, float rating){
        this.images = new ArrayList<>();
        for(Bitmap image : images){
            this.images.add(UserData.getBitmapAsByteArray(image));
        }
        this.name = name;
        this.location = location;
        this.description = description;
        this.speciality = speciality;
        this.rating = rating;
    }

    PlaceDetails(ArrayList <Bitmap> images,String name, String location, String description, float rating){
        this.images = new ArrayList<>();
        for(Bitmap image : images){
            this.images.add(UserData.getBitmapAsByteArray(image));
        }
        this.name = name;
        this.location = location;
        this.description = description;
        this.rating = rating;
    }

    public ArrayList <Bitmap> getImages() {
        ArrayList <Bitmap> images = new ArrayList<>();
        if(images.size() == 0) return null;
        for(byte[] image : this.images){
            images.add(UserData.getBitmapFromByteArray(image));
        }
        return images;
    }

    public String getName() { return name; }

    public String getLocation() { return location; }

    public String getDescription() { return description; }

    public String getSpeciality() { return speciality; }

    public float getRating() { return rating; }

    public void setImage(ArrayList <Bitmap> images) {
        this.images = new ArrayList<>();
        for(Bitmap image : images){
            this.images.add(UserData.getBitmapAsByteArray(image));
        }
    }

    public void setName(String name) { this.name = name; }

    public void setLocation(String location) { this.location = location; }

    public void setDescription(String description) { this.description = description; }

    public void setSpeciality(String speciality) { this.speciality = speciality; }

    public void setRating(float rating) { this.rating = rating; }

    public void addImage(Bitmap image) {this.images.add(UserData.getBitmapAsByteArray(image));}

    public boolean uploadToDatabase(Context context, int user_id){
        ContentValues data = new ContentValues();
        data.put(DBHelper.name, name);
        data.put(DBHelper.description, description);
        data.put(DBHelper.speciality, speciality);
        data.put(DBHelper.location, location);
        data.put(DBHelper.rating, rating);
        data.put(DBHelper.uploaded_by, user_id);
        for(byte[] photo : images){
            data.put(DBHelper.photos, photo);
        }
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.insert(DBHelper.places_table_name,null, data) != -1;
    }



}
