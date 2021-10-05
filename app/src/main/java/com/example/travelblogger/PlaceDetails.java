package com.example.travelblogger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.util.HashMap;

public class PlaceDetails {
    private Bitmap image;
    private String name, description, speciality;
    private String location;
    private float rating;
    PlaceDetails(){
        name="Place Name";
        description="This is the description of that place";
        location="Country, State, District, City, Postal code";
        speciality="Speciality of that place";
        rating=3;
        image=null;
    }
    PlaceDetails(Bitmap image, String name, String location, String description, String speciality, float rating){
        this.image = image;
        this.name = name;
        this.location = location;
        this.description = description;
        this.speciality = speciality;
        this.rating = rating;
    }

    PlaceDetails(Bitmap image,String name, String location, String description, float rating){
        this.image = image;
        this.name = name;
        this.location = location;
        this.description = description;
        this.rating = rating;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getSpeciality() {
        return speciality;
    }

    public float getRating() {
        return rating;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }




}
