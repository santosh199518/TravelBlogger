package com.example.travelblogger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.smarteist.autoimageslider.SliderPager;
import com.smarteist.autoimageslider.SliderView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class AddPlacesActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_MULTIPLE = 100;
    HashSet <Uri> imagesUri;
    SliderView placePhotos;
    RatingBar placeRating;
    Button addPhotoBtn;
    EditText placeName, placeLocation, placeSpeciality;
    TextInputEditText placeDescription, placeComment;
    ChipGroup cg;
    UserData user;
    String []specialitiesName = {};
    int []specialitiesIcon = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_places);

        initializeView();
        //For enabling cross button as home button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data) {
                if(data.getData()!=null){
                    imagesUri.add(data.getData());
                }
                else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            imagesUri.add(item.getUri());
                        }
                    }
                }
                SliderAdapter adapter = new SliderAdapter(this, new ArrayList<>(imagesUri));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500);
                placePhotos.setSliderAdapter(adapter);
                placePhotos.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
                placePhotos.setLayoutParams(params);
                placePhotos.setScrollTimeInSec(2);
                placePhotos.setAutoCycle(true);
                placePhotos.startAutoCycle();
            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                break;
            case R.id.done:
                addPlace();
        }
        finish();
        return true;
    }

    void addPlace(){
        if(checkCredentials()){
            PlaceDetails place = new PlaceDetails(new ArrayList<>(imagesUri),
                    placeName.getText().toString(),
                    placeLocation.getText().toString(),
                    placeDescription.getText().toString(),
                    placeSpeciality.getText().toString().split(" "),
                    placeRating.getRating(),
                    placeComment.getText().toString());
            if(place.uploadToDatabase(getApplicationContext(),user.getEmail())) finish();
            else Toast.makeText(getApplicationContext(), "Sorry, Not able to Upload data",Toast.LENGTH_SHORT).show();
        }
    }

    boolean checkCredentials() {
        if(placeName.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(),"Please Enter Name of place", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(placeLocation.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(),"Please Enter Location of place", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(placeDescription.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(),"Please write about place in description.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(placeComment.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(),"Please share your experience.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void initializeView(){
        user = (UserData) getIntent().getSerializableExtra("user data");
        placePhotos = findViewById(R.id.place_photos);
        placeName = findViewById(R.id.place_name);
        placeLocation = findViewById(R.id.place_location);
        cg = findViewById(R.id.place_speciality_cg);
        placeSpeciality = findViewById(R.id.place_speciality);
        placeDescription = findViewById(R.id.place_description);
        placeComment = findViewById(R.id.place_comment);
        placeRating = findViewById(R.id.place_rating);
        addPhotoBtn = findViewById(R.id.add_photo_btn);
        addPhotoBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
            }
        });
        imagesUri = new HashSet<>();
    }
}
