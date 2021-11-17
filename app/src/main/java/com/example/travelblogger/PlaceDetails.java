package com.example.travelblogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PlaceDetails implements Serializable {

    HashMap<String, String> images, comments;
    HashMap<Integer, String>  speciality;
    String name, description, location;
    String uploadedDate, uploadedBy;
    float rating;
    int likeCount = 0;

    PlaceDetails(){
        name = "Name of Place";
        description = "This is the description of that place";
        location = "Kathmandu, Province 3, Nepal";
        speciality = new HashMap<>();
        images = new HashMap<>();
        rating = 0;
    }
    public PlaceDetails(HashMap<String, String> images, HashMap<Integer, String> speciality, String name, String description, String location, HashMap<String, String> comments, String uploadedDate, String uploadedBy, float rating, int likeCount) {
        if (images == null) this.images = new HashMap<>();
        else this.images = images;
        if (speciality == null) this.speciality = new HashMap<>();
        else this.speciality = speciality;
        this.name = name;
        this.description = description;
        this.location = location;
        this.comments = comments;
        this.uploadedDate = uploadedDate;
        this.uploadedBy = uploadedBy;
        this.rating = rating;
        this.likeCount = likeCount;
    }

    public HashMap <String, String> getImages() {
        return this.images;
    }

    public void setImages(HashMap<String, String> images) { this.images = images; }

    public int getLikeCount() { return likeCount; }

    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public HashMap<Integer, String> getSpeciality() { return speciality; }

    public void setSpeciality(HashMap<Integer, String> speciality) { this.speciality = speciality; }

    public float getRating() { return rating; }

    public void setRating(float rating) { this.rating = rating; }

    public HashMap<String, String> getComments() { return comments; }

    public void setComments(HashMap<String, String> comments) { this.comments = comments; }

    public String getUploadedDate() { return uploadedDate; }

    public void setUploadedDate(String uploadedDate) { this.uploadedDate = uploadedDate; }

    public String getUploadedBy() { return uploadedBy; }

    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public void addImage(Uri image) {
        if(!images.containsValue(image.toString()))
            images.put("Image_"+images.size(),image.toString());
    }

    public boolean uploadToDatabase(Context context, String userEmail){
        ContentValues data = new ContentValues();
        data.put(DBHelper.name, name);
        data.put(DBHelper.description, description);
        String s = speciality.keySet().toString()+":"+speciality.values().toString();
        data.put(DBHelper.speciality, s);
        data.put(DBHelper.location, location);
        data.put(DBHelper.rating, rating);
        data.put(DBHelper.uploaded_by, userEmail);
        data.put(DBHelper.uploaded_date,uploadedDate);
        data.put(DBHelper.like_count, likeCount);
        data.put(DBHelper.photos, Arrays.toString(images.values().toArray()));
        s = comments.keySet().toString()+":"+ comments.values().toString();
        data.put(DBHelper.comment,s);
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
            c.moveToFirst();
            do{
                String str;
                HashMap<String, String> photosUri = new HashMap<>();

//                To retrieve arrays of photoUri and convert them to Hashmap to create PlaceDetails Objects
                str = c.getString(c.getColumnIndex(DBHelper.photos));
                str = str.replace("[","").replace("]","");
                String[] photosArray = str.split(",");
                for(int i=0; i<photosArray.length; i++){
                    photosUri.put("Photo_"+i,photosArray[i]);
                }
//                To retrieve arrays of specialities and convert them to Hashmap to create PlaceDetails Objects
                HashMap<Integer, String> specialities = new HashMap<>();
                str = c.getString(c.getColumnIndex(DBHelper.speciality));
                String []set = str.split(":");
                String[] key = set[0].replace("[","").replace("]","").split(",");
                String[] values = set[1].replace("[","").replace("]","").split(",");
                if(values.length == key.length)
                    for(int i=0; i<values.length; i++)
                        specialities.put( Integer.parseInt(key[i].trim()), values[i]);
//                To retrieve arrays of comments and convert them to Hashmap to create PlaceDetails Objects
                HashMap<String, String> comments = new HashMap<>();
                str = c.getString(c.getColumnIndex(DBHelper.comment));
                set = str.split(":");
                key = set[0].replace("[","").replace("]","").split(",");
                values = set[1].replace("[","").replace("]","").split(",");
                for(int i=0; i<values.length; i++)
                    comments.put( key[i], values[i]);

                PlaceDetails place = new PlaceDetails(photosUri, specialities,
                        c.getString(c.getColumnIndex(DBHelper.name)),
                        c.getString(c.getColumnIndex(DBHelper.description)),
                        c.getString(c.getColumnIndex(DBHelper.location)),
                        comments,
                        c.getString(c.getColumnIndex(DBHelper.uploaded_date)),
                        c.getString(c.getColumnIndex(DBHelper.uploaded_by)),
                        c.getFloat(c.getColumnIndex(DBHelper.rating)),
                        c.getInt(c.getColumnIndex(DBHelper.like_count)));
                places.add(place);
            }while(c.moveToNext());
        }
        return places;
    }

    public void updateLikeCountToDataBase(Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("UPDATE "+ DBHelper.places_table_name+" SET "+DBHelper.like_count+" = "+likeCount+ " WHERE "+DBHelper.name+"='"+name+"'");
        FirebaseDatabase.getInstance().getReference().child("Places Details").child(name).child("likeCount")
                .setValue(likeCount).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void addComment(Context context, UserData user, String comment){
        this.comments.put(user.getEmail(),comment);
    }



}
