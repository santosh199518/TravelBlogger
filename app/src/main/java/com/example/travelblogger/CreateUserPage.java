package com.example.travelblogger;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CreateUserPage extends AppCompatActivity implements View.OnClickListener {
//    Code for different purposes
    public static final int REQUEST_IMAGE_PICK = 101;
    public static final int REQUEST_IMAGE_CAPTURE = 102;
    public static final int CAMERA_PERMISSION_CODE = 103;
    public static final int READ_STORAGE_PERMISSION_CODE = 104;
    public static final int WRITE_STORAGE_PERMISSION_CODE = 105;
//    Views listed in layout
    TextInputEditText name,password,confirm_password,email;
    TextView upload_photo_tv;
    ImageView photo;
    Button create_user;
//    Variables for storing data
    Uri imageUri;
    Bitmap userPic;
    UserData user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_page);

        initializeView();
        if (getIntent().getBooleanExtra("FromGoogleSignIn",false))
            fromGoogleSignIn();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo:
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateUserPage.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                }
                else {
                    Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                break;

            case R.id.upload_photo_tv:
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateUserPage.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_CODE);
                }
                else {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , REQUEST_IMAGE_PICK);
                }
                break;

            case R.id.create_user:
                createUser();
        }
    }

     void createUser() {
        imageUri = saveBitmap(userPic);
        if (checkCredentials()) {
            user = new UserData();
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();
            String n = name.getText().toString().trim();
            user.setUsername(n);
            user.setPassword(p);
            user.setEmail(e);
            user.setImageUri(imageUri.toString());
            uploadToFireBaseDatabase();
        }
    }

    public boolean checkCredentials() {
        boolean result = true;
        String n= Objects.requireNonNull(name.getText()).toString();
        String e= Objects.requireNonNull(email.getText()).toString();
        String p= Objects.requireNonNull(password.getText()).toString();
        String c= Objects.requireNonNull(confirm_password.getText()).toString();
        if(n.trim().isEmpty()){
            name.setError("Insert Name");
            name.requestFocus();
            result=false;
        }
        else if(e.trim().isEmpty()){
            email.setError("Insert Email-ID");
            email.requestFocus();
            result=false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(e).matches()){
            email.setError("Invalid Email-ID");
            email.requestFocus();
            result=false;
        }
        else if(p.trim().isEmpty()){
            password.setError("Password cannot be empty");
            password.requestFocus();
            result = false;
        }
        else if(!p.equals(c) ){
            confirm_password.setError("Password Not Matched");
            confirm_password.requestFocus();
            result=false;
        }
        else if(imageUri == null ){
            Toast.makeText(getApplicationContext(),"Please select a photo.",Toast.LENGTH_SHORT).show();
            result=false;
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null){
            userPic = (Bitmap) data.getExtras().get("data");
            photo.setImageBitmap(userPic);
        }
        if(requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK){
            if(data!=null) uploadPictureIntent(data);
        }
    }

    public void uploadPictureIntent(Intent data){
        Uri selectedImage =  data.getData();
        imageUri = selectedImage;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        if (selectedImage != null) {
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                userPic = BitmapFactory.decodeFile(picturePath);
                //scaling image to 512 width by maintaining aspect ratio to decrease size of image
                int nh = (int) ( userPic.getHeight() * (512.0 / userPic.getWidth()) );
                userPic = Bitmap.createScaledBitmap(userPic, 512, nh, true);
                photo.setImageBitmap(userPic);
                cursor.close();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
            else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == READ_STORAGE_PERMISSION_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , REQUEST_IMAGE_PICK);
            }
            else {
                Toast.makeText(this, "Read Storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == WRITE_STORAGE_PERMISSION_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createUser();
            }
            else {
                Toast.makeText(this, "Write Storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void fromGoogleSignIn() {
        UserData user = (UserData) getIntent().getSerializableExtra("user data");
        name.setText(user.getUsername());
        email.setText(user.getEmail());
        imageUri = Uri.parse(user.getImageUri());
        DownloadImage download = new DownloadImage();
        download.execute(imageUri.toString());
        try{
            userPic = download.get();
        }catch (ExecutionException | InterruptedException e){
            Log.d("Downloadimage",e.getMessage());
            Toast.makeText(getApplicationContext(), "Unable to retrieve photo", Toast.LENGTH_SHORT).show();
        }
        photo.setImageBitmap(userPic);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,LoginPage.class);
        startActivity(intent);
        finish();
    }

    public void goToMainActivity(UserData user){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user data", user);
        startActivity(intent);
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (IOException e) {
            Log.d("IOException",e.getMessage());
        }
        return null;
    }

    Uri saveBitmap(Bitmap bmp) {
        Uri filePath = null;
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CreateUserPage.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_CODE);
        }
        else {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/saved_images");
            myDir.mkdirs();
            String fname = name.getText().toString().trim()+"_Profile_Picture.jpg";

            File file = new File(myDir, fname);
            if (file.exists()) file.delete ();
            filePath = Uri.fromFile(file);

            try {
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                Log.d("SaveBitmapException",e.getMessage());
            }

        }
        return filePath;
    }

    public void initializeView(){
        name = findViewById(R.id.username);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        confirm_password = findViewById(R.id.confirm_password);
        photo = findViewById(R.id.photo);
        photo.setOnClickListener(this);
        create_user = findViewById(R.id.create_user);
        create_user.setOnClickListener(this);
        upload_photo_tv = findViewById(R.id.upload_photo_tv);
        upload_photo_tv.setOnClickListener(this);
    }

    public void uploadToFireBaseDatabase() {
        ProgressDialog dialog = new ProgressDialog(CreateUserPage.this);
        dialog.setTitle("Creating User");
        dialog.setMessage("Authenticating");
        dialog.setCancelable(false);
        dialog.show();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        dialog.setMessage("Uploading Data");
                        StorageReference uploader = FirebaseStorage.getInstance().getReference()
                            .child("User Photos").child(uid).child(user.getUsername()+"_ProfilePicture");
                        uploader.putFile(imageUri)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()){
                                        uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                dialog.setMessage("Uploading Details");
                                                UserData user1 = new UserData(user.getUsername(),user.getPassword(),
                                                    user.getEmail(), uri.toString(),
                                                    user.favouritePlaces, user.likedPlaces);
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Users").child(uid)
                                                        .setValue(user1)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(CreateUserPage.this, "User created Successfully", Toast.LENGTH_SHORT).show();
                                                                    user.uploadUserDataToDatabase(getApplicationContext());
                                                                    dialog.dismiss();
                                                                    goToMainActivity(user);
                                                                    finish();
                                                                }
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("DatabaseFailure", e.getMessage());
                                                        deleteFirebaseUser();
                                                        dialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    float percent = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                                    dialog.setMessage("Uploaded: "+(int)percent+"%");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    deleteFirebaseUser();
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            });
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
    }

    void deleteFirebaseUser(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(),user.getPassword());
        firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                firebaseUser.delete();
            }
        });
    }

    class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            return getBitmapFromURL(strings[0]);
        }
    }
}