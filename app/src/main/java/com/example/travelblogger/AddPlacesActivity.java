package com.example.travelblogger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;
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
import com.google.android.material.snackbar.Snackbar;
import com.smarteist.autoimageslider.SliderView;
import java.util.ArrayList;
import java.util.HashSet;

public class AddPlacesActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_MULTIPLE = 100;
    HashSet <Uri> imagesUri;
    SliderView placePhotos;
    RatingBar placeRating;
    Button addPhotoBtn;
    EditText placeName, placeLocation, placeDescription, placeComment;
    AppCompatMultiAutoCompleteTextView placeSpeciality;
    ChipGroup cg;
    UserData user;
    String []specialitiesName = {};
    int []specialitiesIcon = {};
    ArrayList <PlaceDetails> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_places);

        initializeView();
        places = (ArrayList<PlaceDetails>) getIntent().getSerializableExtra("places_data");
        Log.d("SizeOfArray", String.valueOf(places.size()));
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
                Log.d("ImagesUri",imagesUri.toString());
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
            if(place.uploadToDatabase(getApplicationContext(),user.getEmail())) {
                Intent data = new Intent();
                data.putExtra("new_place",place);
                setResult(RESULT_OK, data);
                finish();
            }
            else Toast.makeText(getApplicationContext(), "Sorry, Not able to Upload data",Toast.LENGTH_SHORT).show();
        }
    }

    boolean checkCredentials() {
        String name = placeName.getText().toString();
        boolean sameName = false;
        int index = 0;
        for(int i=0; i<places.size(); i++){
            if(places.get(i).getName().equals(name)){
                sameName = true;
                index = i;
            }
        }
        if(placeName.getText().toString().trim().isEmpty()){
            placeName.setError("Shouldn't be empty");
            placeName.requestFocus();
            return false;
        }
        else if(placeLocation.getText().toString().trim().isEmpty()){
            placeLocation.setError("Shouldn't be empty");
            placeLocation.requestFocus();
            return false;
        }
        else if(placeDescription.getText().toString().trim().isEmpty()){
            placeDescription.setError("Shouldn't be empty");
            placeDescription.requestFocus();
            return false;
        }
        else if(placeComment.getText().toString().trim().isEmpty()){
            placeComment.setError("share your experience here");
            placeComment.requestFocus();
            Toast.makeText(getApplicationContext(),"", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sameName) {
            Snackbar message = Snackbar.make(placeName.getRootView(), "Name already registered", Snackbar.LENGTH_LONG);
            int finalIndex = index;
            message.setAction("SHOW", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent showPlaceActivity = new Intent(getApplicationContext(), ShowPlaceActivity.class);
                    showPlaceActivity.putExtra("place",places.get(finalIndex));
                    startActivity(showPlaceActivity);
                }
            });
            message.show();
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
