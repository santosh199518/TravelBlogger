package com.example.travelblogger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.TextView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.smarteist.autoimageslider.SliderView;

public class ShowPlaceActivity extends AppCompatActivity {

    SliderView placePhotos;
    RatingBar placeRating;
    TextView placeName, placeLocation, placeDescription;
    ChipGroup placeSpecialities;
    PlaceDetails place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_place);

        //For enabling back button in actionbar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        initializeView();
        place = (PlaceDetails)getIntent().getSerializableExtra("place");
        SliderAdapter adapter = new SliderAdapter(this, place.getImages());
        placePhotos.setSliderAdapter(adapter);
        placeName.setText(place.getName());
        placeLocation.setText(place.getLocation());
        for(String value: place.getSpeciality()){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.custom_chip_view, null, false);
            chip.setCloseIconVisible(false);
            chip.setText(value);
            placeSpecialities.addView(chip);
        }
        placeDescription.setText(place.getDescription());
        placeRating.setRating(place.getRating());
    }

    public void initializeView(){

        placePhotos = findViewById(R.id.places_photos);
        placeName = findViewById(R.id.place_name);
        placeLocation = findViewById(R.id.place_location);
        placeSpecialities = findViewById(R.id.place_specialities);
        placeDescription = findViewById(R.id.place_description);
        placeRating = findViewById(R.id.place_rating);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}