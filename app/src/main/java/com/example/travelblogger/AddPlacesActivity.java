package com.example.travelblogger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.smarteist.autoimageslider.SliderView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class AddPlacesActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_MULTIPLE = 100;
    HashMap <String, String> imagesUri, uploadedImageUri, comments;
    HashMap <String, String> specialitiesName;
    SliderView placePhotos;
    RatingBar placeRating;
    Button addPhotoBtn;
    EditText placeName, placeLocation, placeDescription, placeComment;
    AppCompatMultiAutoCompleteTextView placeSpeciality;
    ChipGroup cg;
    UserData user;
    ArrayList <PlaceDetails> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_places);

        initializeView();
        places = (ArrayList<PlaceDetails>) getIntent().getSerializableExtra("places_data");
        //For enabling cross button as home button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        actionBar.setDisplayHomeAsUpEnabled(true);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ArrayList<Uri> selected_images = new ArrayList<>();
        try {
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data) {
                if(data.getData()!=null){
                    selected_images.add(data.getData());
                }
                else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            selected_images.add(item.getUri());
                        }
                    }
                }
                for(int i=0 ;i<selected_images.size();i++) {
                    Cursor cursor = getContentResolver().query(selected_images.get(i), null, null, null, null);
                    cursor.moveToFirst();
                    String document_id = cursor.getString(0);
                    document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
                    cursor.close();
                    cursor = getContentResolver().query(
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
                    cursor.moveToFirst();
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    cursor.close();

                    Uri uri = Uri.fromFile(new File(path));
                    imagesUri.put("Image_" +(imagesUri.size()+1) , uri.toString());
                }
                Log.d("ImagesUri",imagesUri.toString());

                SliderAdapter adapter = new SliderAdapter(this, new ArrayList<>(imagesUri.values()));
                adapter.setButtonVisibility(true);
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500);
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
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                finish();
                break;
            case R.id.done:
                addPlace();
        }
        return true;
    }

    void addPlace(){
        if(checkCredentials()){
            String[] specialitiesArray = placeSpeciality.getText().toString().split(",");
            HashMap<String,String> specialities = new HashMap<>();
            HashMap<String,String> comments = new HashMap<>();
            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            comments.put(id, placeComment.getText().toString());
            for(int i=0; i<specialitiesArray.length; i++){
                specialities.put(String.valueOf(i),specialitiesArray[i]);
            }

            PlaceDetails place = new PlaceDetails(imagesUri, specialities,
                    placeName.getText().toString(),
                    placeDescription.getText().toString(),
                    placeLocation.getText().toString(),
                    comments,
                    getUploadedDate(),
                    user.getUsername(),
                    placeRating.getRating(),
                    getLikeCount());

            if (isNetworkConnected()) uploadToFirebaseDatabase(place);
            else
                Toast.makeText(getApplicationContext(), "Sorry, Not able to Upload data",Toast.LENGTH_SHORT).show();


        }
    }

    void finishUploading(PlaceDetails place){
        Intent data = new Intent();
        data.putExtra("new_place",place);
        setResult(RESULT_OK, data);
        finish();
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
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");

                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
            }
        });
        imagesUri = new HashMap<>();
        uploadedImageUri = new HashMap<>();
        specialitiesName = new HashMap<>();
    }

    void uploadToFirebaseDatabase(PlaceDetails details){
        for(String key: imagesUri.keySet()) {
            ProgressDialog pd = new ProgressDialog(AddPlacesActivity.this);
            pd.setTitle("Uploading");
            pd.setMessage("Uploading Images");
            pd.setIndeterminate(false);
            pd.setMax(100);
            pd.setCancelable(false);
            pd.show();
            StorageReference sr = FirebaseStorage.getInstance().getReference().child("Place Images")
                    .child(placeName.getText().toString())
                    .child(user.getEmail()+"_"+key+".jpg");
            sr.putFile(Uri.parse(imagesUri.get(key))).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        sr.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                uploadedImageUri.put(key, task.getResult().toString());
                                DatabaseReference df = FirebaseDatabase.getInstance().getReference()
                                    .child("Places Details").child(placeName.getText().toString());
                                PlaceDetails details = new PlaceDetails(uploadedImageUri, specialitiesName,
                                    placeName.getText().toString(), placeDescription.getText().toString(),
                                    placeLocation.getText().toString(), comments, getUploadedDate(),
                                    user.getEmail(), placeRating.getRating(), getLikeCount());
                                if (imagesUri.size() == uploadedImageUri.size()) {
                                    pd.setMessage("Uploading Data");
                                    df.setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                finishUploading(details);
                                                Toast.makeText(getApplicationContext(), "Place Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    int percent = (int) ((100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount());
                    pd.setProgress(percent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private int getLikeCount() {
        return 0;
    }

    private String getUploadedDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
    public boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
