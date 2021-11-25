package com.example.travelblogger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseCommonRegistrar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.smarteist.autoimageslider.SliderView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class AddPlacesActivity extends AppCompatActivity {
//    code for different purposes
    private static final int PICK_IMAGE_MULTIPLE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    public static final int CAMERA_PERMISSION_CODE = 103;
    public static final int READ_STORAGE_PERMISSION_CODE = 104;
    public static final int WRITE_STORAGE_PERMISSION_CODE = 105;
//    variables for storing values
    HashMap <String, String> imagesUri, uploadedImageUri;
    ArrayList <PlaceDetails> places;
    UserData user;
//    Views in layout file for this activity
    TextInputEditText placeName, placeLocation, placeDescription, placeComment,placeSpeciality;
    ExtendedFloatingActionButton addPhoto, openGallery, openCamera, close;
    SliderView placePhotos;
    RatingBar placeRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_places);

//        initializing views with their respective ids
        initializeView();
//        obtaining places list from main activity to remove redundancy of places
        places = (ArrayList<PlaceDetails>) getIntent().getSerializableExtra("places_data");
//        For enabling cross button as home button
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        }
    }


//    For adding done menu in actionbar
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

//    For creating PlaceDetails class from all details
    void addPlace(){
        if(checkCredentials()){
            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            HashMap<String,String> specialities = new HashMap<>();
            String[] specialitiesArray = placeSpeciality.getText().toString().split(",");
            for(int i=0; i<specialitiesArray.length; i++){
                specialities.put("Speciality_"+i,specialitiesArray[i]);
            }

            HashMap<String,String> comments = new HashMap<>();
            comments.put(id, placeComment.getText().toString());

            HashMap<String,Float> ratings = new HashMap<>();
            ratings.put("averageRating",placeRating.getRating());
            ratings.put(id,placeRating.getRating());

            PlaceDetails place = new PlaceDetails(imagesUri, specialities,
                    placeName.getText().toString(),
                    placeDescription.getText().toString(),
                    placeLocation.getText().toString(),
                    comments, getUploadedDate(), id, ratings, getLikeCount());

            if (isNetworkConnected()) uploadToFirebaseDatabase(place);
            else Toast.makeText(getApplicationContext(), "No Internet Connection",Toast.LENGTH_SHORT).show();
        }
    }
//    Checking weather any field is empty or not and if place already exits or not
    boolean checkCredentials() {
        String name = placeName.getText().toString().trim();
        boolean sameName = false;
        int index = 0;
        for(int i=0; i<places.size(); i++){
            if(places.get(i).getName().equals(name)){
                sameName = true;
                index = i;
            }
        }
        if(name.isEmpty()){
            placeName.setError("Name Shouldn't be empty");
            placeName.requestFocus();
            return false;
        }
        else if(placeLocation.getText().toString().trim().isEmpty()){
            placeLocation.setError("Location Shouldn't be empty");
            placeLocation.requestFocus();
            return false;
        }
        else if(placeSpeciality.getText().toString().trim().isEmpty()){
            placeSpeciality.setError("Location Shouldn't be empty");
            placeSpeciality.requestFocus();
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
        placeSpeciality = findViewById(R.id.place_speciality);
        placeDescription = findViewById(R.id.place_description);
        placeComment = findViewById(R.id.place_comment);
        placeRating = findViewById(R.id.place_rating);
        addPhoto = findViewById(R.id.open_fab);
//        For enabling user to add photo using floating action menu
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
                openCamera.setVisibility(View.VISIBLE);
                openGallery.setVisibility(View.VISIBLE);
                close.setVisibility(View.VISIBLE);
                addPhoto.setVisibility(View.INVISIBLE);
            }
        });
//        To open camera to capture photo and add it
        openCamera = findViewById(R.id.open_camera_fab);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });
//        To open gallery to add image
        openGallery = findViewById(R.id.open_gallery_fab);
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
            }
        });
//        To close the menu of floating action buttons
        close = findViewById(R.id.close_fab);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera.setVisibility(View.INVISIBLE);
                openGallery.setVisibility(View.INVISIBLE);
                close.setVisibility(View.INVISIBLE);
                addPhoto.setVisibility(View.VISIBLE);
            }
        });
        imagesUri = new HashMap<>();
        uploadedImageUri = new HashMap<>();
    }

    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddPlacesActivity.this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddPlacesActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_CODE);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddPlacesActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Allow permission to add photo!", Toast.LENGTH_LONG).show();
            openCamera.setVisibility(View.INVISIBLE);
            openGallery.setVisibility(View.INVISIBLE);
            close.setVisibility(View.INVISIBLE);
            addPhoto.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<Uri> selected_images = new ArrayList<>();
        if(data != null && resultCode == RESULT_OK) {
//            For adding all images uri from gallery to list
            if (requestCode == PICK_IMAGE_MULTIPLE) {
                //To Pick Single Image From Gallery
                if (data.getData() != null)     selected_images.add(data.getData());
                    //To Pick Multiple Image From Gallery
                else if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        selected_images.add(item.getUri());
                    }
                }
//                For resolving context and getting file-url from images
                for (int i = 0; i < selected_images.size(); i++) {
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
                    imagesUri.put("Image_" + (imagesUri.size() + 1), uri.toString());
                }

            }
//            For Capturing images and saving to gallery for getting file url to upload
            else if (requestCode == REQUEST_IMAGE_CAPTURE ) {
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/saved_images");
                myDir.mkdirs();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String fname = "IMG_"+ timeStamp +".jpg";
                File file = new File(myDir, fname);
                if (file.exists()) file.delete ();
                Uri filePath = Uri.fromFile(file);

                try {
                    FileOutputStream out = new FileOutputStream(file);
                    Bitmap bmp = (Bitmap) data.getExtras().get("data");
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    imagesUri.put("Image_"+(imagesUri.size()+1), filePath.toString());
                } catch (Exception e) {
                    Log.d("SaveBitmapException",e.getMessage());
                }
            }
//            For showing all the images in sliderview
            SliderAdapter adapter = new SliderAdapter(this, new ArrayList<>(imagesUri.values()));
            adapter.setButtonVisibility(true);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500);
            placePhotos.setSliderAdapter(adapter);
            placePhotos.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
            placePhotos.setLayoutParams(params);
            placePhotos.setScrollTimeInSec(2);
            placePhotos.setAutoCycle(true);
            placePhotos.startAutoCycle();
        }
        else Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
    }

    //    For uploading places details in firebase database
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
                                if (imagesUri.size() == uploadedImageUri.size()) {
                                    DatabaseReference df = FirebaseDatabase.getInstance().getReference()
                                            .child("Places Details").child(placeName.getText().toString());
                                    PlaceDetails newDetails = details;
                                    newDetails.setImages(uploadedImageUri);
                                    pd.setMessage("Uploading Data");
                                    df.setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                pd.dismiss();
                                                finishUploading(details);
                                                Toast.makeText(getApplicationContext(), "Place Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
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
                    pd.dismiss();
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
    private boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    void finishUploading(PlaceDetails place){
        Intent data = new Intent();
        data.putExtra("new_place",place);
        setResult(RESULT_OK, data);
        finish();
    }

}
