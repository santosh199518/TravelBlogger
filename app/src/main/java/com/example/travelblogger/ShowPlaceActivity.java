package com.example.travelblogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShowPlaceActivity extends AppCompatActivity {

    SliderView placePhotos;
    ListView commentsLV;
    RatingBar placeRating, newRating;
    TextView placeName, placeLocation, placeDescription;
    TextInputEditText newComment;
    ChipGroup placeSpecialities;
    ImageView addNewComment;
    CustomAdapterForComments commentAdapter;
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
        SliderAdapter adapter = new SliderAdapter(this, new ArrayList<>(place.getImages().values()));
        placePhotos.setSliderAdapter(adapter);
        placePhotos.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        placePhotos.setScrollTimeInSec(2);
        placePhotos.setAutoCycle(true);
        placePhotos.startAutoCycle();
        placeName.setText(place.getName());
        placeLocation.setText(place.getLocation());
        for(String value: place.getSpeciality().values()){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.custom_chip_view, null, false);
            chip.setCloseIconVisible(false);
            chip.setText(value);
            placeSpecialities.addView(chip);
        }
        placeDescription.setText(place.getDescription());
        placeRating.setRating(place.getRating());
        ArrayList<String[]> comments = new ArrayList<>();
        HashMap<String, String> placeComments = place.getComments();
        if(placeComments!=null) {
            for (String key : placeComments.keySet()) {
                comments.add(new String[]{key, place.getComments().get(key)});
            }
        }
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
        commentsLV = findViewById(R.id.comment_lv);
        newComment = findViewById(R.id.new_comment);
        newRating = findViewById(R.id.new_rating);
        newRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                int newRating = (int) (place.getRating()+rating)/2;
                place.setRating(newRating);
                place.updateRating(newRating);
                commentAdapter.notifyDataSetChanged();
            }
        });
        addNewComment = findViewById(R.id.add_new_comment);
        addNewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                place.addComment(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        newComment.getText().toString().trim());
                newComment.setText("");
                commentAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public class CustomAdapterForComments extends ArrayAdapter<String[]> {
        ArrayList <String[]> comments;
        Context context;
        int resource;
        ImageView image;
        TextView name, comment;
        ProgressBar pb;

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
//            return super.getView(position, convertView, parent);
            View view = LayoutInflater.from(context).inflate(resource, null);
            image = view.findViewById(R.id.user_photo);
            name = view.findViewById(R.id.user_name);
            comment = view.findViewById(R.id.user_comment);
            pb = view.findViewById(R.id.pb);

            DatabaseReference df = FirebaseDatabase.getInstance().getReference().child("Users").child(comments.get(position)[0]);
            df.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserData user = snapshot.getValue(UserData.class);
                    assert user != null;
                    Picasso.get().load(Uri.parse(user.getImageUri())).placeholder(R.drawable.ic_add_photo).into(image);
                    name.setText(user.getUsername());
                    comment.setText(comments.get(position)[1]);
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