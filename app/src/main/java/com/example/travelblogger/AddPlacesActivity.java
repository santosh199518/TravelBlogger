package com.example.travelblogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AddPlacesActivity extends AppCompatActivity {

    ArrayList<PlaceDetails> places;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_places);
        CustomAdapterForPlacesComments adapter = new CustomAdapterForPlacesComments(this, R.layout.layout_for_places_comments, places);
    }
}

class CustomAdapterForPlacesComments extends ArrayAdapter <PlaceDetails> {

    Context context;
    int layout_id;
    ArrayList<PlaceDetails> places;
    public CustomAdapterForPlacesComments(@NonNull Context context, int resource, @NonNull ArrayList<PlaceDetails> places) {
        super(context, resource, places);
        this.context = context;
        this.layout_id = resource;
        this.places = places;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layout_id, null);
        ImageView photo = v.findViewById(R.id.user_photo);
        photo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                photo.setImageBitmap(places.get(position));
            }
        });
        TextView name = v.findViewById(R.id.user_name);
        name.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                name.setText(places.get(position));
            }
        });
        TextView comment = v.findViewById(R.id.user_comment);
        comment.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                comment.setText(places.get(position));
            }
        });
        return v;
    }
}