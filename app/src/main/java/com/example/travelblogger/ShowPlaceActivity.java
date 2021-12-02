package com.example.travelblogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ShowPlaceActivity extends AppCompatActivity {
    SliderView placePhotos;
    ListView commentsLV;
    RatingBar placeRating, newRating;
    TextView placeName, placeLocation, placeDescription, uploadedDate, uploaderEmail, uploaderName;
    TextInputEditText newComment;
    ChipGroup placeSpecialities;
    ImageView addNewComment, uploaderImage;
    CustomAdapterForComments commentAdapter;
    PlaceDetails place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_place);

        //For enabling back button in actionbar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
//        For initializing view with their respective id
        initializeView();
//        for taking place from main activity
        place = (PlaceDetails)getIntent().getSerializableExtra("place");
//        For making sliderview for images
        SliderAdapter adapter = new SliderAdapter(this, new ArrayList<>(place.getImages().values()));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500);
        placePhotos.setLayoutParams(params);
        placePhotos.setSliderAdapter(adapter);
        placePhotos.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        placePhotos.setScrollTimeInSec(2);
        placePhotos.setAutoCycle(true);
        placePhotos.startAutoCycle();
//        Setting place name
        placeName.setText(place.getName());
//        Setting place location
        placeLocation.setText(place.getLocation());
//        Obtaining uploader details from firebase database
        Task <DataSnapshot> task = FirebaseDatabase.getInstance().getReference().child("Users").child(place.getUploadedBy()).get();
        while(!task.isComplete()){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() { }
            },500);
        }
        UserData uploader = task.getResult().getValue(UserData.class);
//        Adding all the specialities in chips
        for(String value: place.getSpeciality().values()){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.custom_chip_view, null, false);
            chip.setCloseIconVisible(false);
            chip.setText(value);
            placeSpecialities.addView(chip);
        }
//        Setting place description
        placeDescription.setText(place.getDescription());
//        Setting place rating from overall ratings
        placeRating.setRating(place.getRating().get("averageRating"));
//        Setting uploader email
        uploaderEmail.setText(uploader.getEmail());
//        Setting uploaded name
        uploaderName.setText(uploader.getUsername());
//        Setting uploader image
        Picasso.get().load(uploader.getImageUri()).placeholder(R.drawable.ic_person).into(uploaderImage);
//        Setting uploaded date
        uploadedDate.setText(place.getUploadedDate());
//        Collecting all users id, comments and rating and making a arraylist for comments listview
        ArrayList<String[]> comments = new ArrayList<>();
        HashMap<String, String> placeComments = place.getComments();
        if(placeComments!=null) {
            for (String key : placeComments.keySet()) {
                String[] value = {key, placeComments.get(key), String.valueOf(place.getRating().get(key))};
                Log.d("arraylist", Arrays.toString(value));
                comments.add(value);
            }
        }
//        Creating adapter for comments listview
        commentAdapter = new CustomAdapterForComments(this, R.layout.layout_for_places_comments,comments);
        commentsLV.setAdapter(commentAdapter);
    }

    public void initializeView(){

        placePhotos = findViewById(R.id.places_photos);
        placeName = findViewById(R.id.place_name);
        placeLocation = findViewById(R.id.place_location);
        placeSpecialities = findViewById(R.id.place_specialities);
        placeDescription = findViewById(R.id.place_description);
        placeRating = findViewById(R.id.place_rating);
        uploaderImage = findViewById(R.id.uploader_image);
        uploaderName = findViewById(R.id.uploader_name);
        uploaderEmail = findViewById(R.id.uploader_email);
        uploadedDate = findViewById(R.id.uploaded_date);
        commentsLV = findViewById(R.id.comment_lv);
        newComment = findViewById(R.id.new_comment);
        newRating = findViewById(R.id.new_rating);
        addNewComment = findViewById(R.id.add_new_comment);
        addNewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getting current userid
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                checking if user has already commented or not and if commented then returning else adding
                if(place.getComments().keySet().contains(currentUserId)) {
                    Toast.makeText(ShowPlaceActivity.this, "Already added your comment for this place", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newComment.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter comment and rating for this place", Toast.LENGTH_LONG).show();
                    newComment.requestFocus();
                    return;
                }
//                Creating new hashmap for comments
                HashMap<String, String> newComments = new HashMap<>(place.getComments());
                newComments.put(currentUserId, newComment.getText().toString().trim());
//                creating new hashmap for ratings
                float overallRating = (place.getRating().get("averageRating")+newRating.getRating())/place.getRating().size();
                HashMap<String, Float> newRatings = new HashMap<>(place.getRating());
                newRatings.put("averageRating",overallRating);
//                Updating new ratings and comments in database
                place.updateRatingAndComment(newComments, newRatings);
                newComment.setText("");
                commentAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        finishing the activity on back button pressed and returning to main activity
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
//    finishing the activity on back button pressed and returning to main activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
//    Creating custom adapter for comment list view
    public class CustomAdapterForComments extends ArrayAdapter<String[]> {
        ArrayList <String[]> comments;
        Context context;
        int resource;
        ImageView image;
        TextView name, comment;
        ProgressBar pb;
        RatingBar rating;

        public CustomAdapterForComments(@NonNull Context context, int resource, @NonNull ArrayList<String[]> objects) {
            super(context, resource, objects);
            this.context = context;
            this.comments = objects;
            this.resource = resource;
        }

        @Override
        public int getCount() {
            return comments.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//            Creating view from layout and initializing views
            View view = LayoutInflater.from(context).inflate(resource, null);
            image = view.findViewById(R.id.user_photo);
            name = view.findViewById(R.id.user_name);
            comment = view.findViewById(R.id.user_comment);
            rating = view.findViewById(R.id.user_rating);
            pb = view.findViewById(R.id.pb);
//            Getting commenter details from database and showing in views
            DatabaseReference df = FirebaseDatabase.getInstance().getReference().child("Users").child(comments.get(position)[0]);
            df.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserData user = snapshot.getValue(UserData.class);
                    assert user != null;
                    Picasso.get().load(Uri.parse(user.getImageUri())).placeholder(R.drawable.ic_add_photo).into(image);
                    name.setText(user.getUsername());
                    comment.setText(comments.get(position)[1]);
                    rating.setRating(Float.parseFloat(comments.get(position)[2]));
                    pb.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }
    }
}