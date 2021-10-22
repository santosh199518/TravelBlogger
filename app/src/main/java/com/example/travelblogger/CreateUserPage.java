package com.example.travelblogger;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Objects;

public class CreateUserPage extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE_PICK = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int MY_CAMERA_PERMISSION_CODE = 103;
    private static final int READ_STORAGE_PERMISSION_CODE = 104;
    TextInputEditText name,password,confirm_password,email;
    TextView upload_photo_tv;
    ImageView photo;
    Bitmap userPic=null;
    Button create_user;
    UserData user = new UserData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_page);

        initializeView();
        if (getIntent().hasExtra("user data")) FromGoogleSignIn();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo:
               takePictureIntent();
                break;

            case R.id.upload_photo_tv:
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateUserPage.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_CODE);
                }
                else {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , REQUEST_IMAGE_PICK);
                }
                break;

            case R.id.create_user:
                if (checkCredentials()) {
                    user.setUsername(name.getText().toString());
                    user.setPassword(password.getText().toString());
                    user.setEmail(email.getText().toString());
                    user.setPhoto(userPic);
                    if (user.uploadUserDataToDatabase(getApplicationContext())) {
                        Toast.makeText(getApplicationContext(), "User Created Successfully", Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                        finish();
                    }
                    else Toast.makeText(getApplicationContext(), "Cannot create user", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void takePictureIntent() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CreateUserPage.this,
                    new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
        else {
            Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    public void uploadPictureIntent(Intent data){
        Uri selectedImage =  data.getData();
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

    public boolean checkCredentials() {
        boolean result = true;
        String n= Objects.requireNonNull(name.getText()).toString();
        String e= Objects.requireNonNull(email.getText()).toString();
        String p= Objects.requireNonNull(password.getText()).toString();
        String c= Objects.requireNonNull(confirm_password.getText()).toString();
        if(n.trim().isEmpty()){
            name.setError("please provide username");
            name.requestFocus();
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
        else if(userPic==null){
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
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
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void FromGoogleSignIn(){
        user = (UserData) getIntent().getSerializableExtra("user data");
        name.setText(user.getUsername());
        email.setText(user.getEmail());
        userPic = user.getPhoto();
        photo.setImageBitmap(userPic);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,LoginPage.class);
        startActivity(intent);
        finish();
    }

    public void goToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user data", user);
        startActivity(intent);
    }
}