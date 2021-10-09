package com.example.travelblogger;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class LoginPage extends AppCompatActivity implements View.OnClickListener {

    private static final int GOOGLE_SIGN_IN = 1;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton signInButton;
    TextInputEditText email, password;
    TextView createUser,forgetPassword;
    Button signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);


//        checkUserStatus();
        initializeViews();
        GoogleSignInOptions gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in_button:
                googleSignIn();
                break;

            case R.id.custom_signin_button:
                signIn();
                break;

            case R.id.signup_button:
                signUp();
                break;

            case R.id.forget_password_tv:
                forgetPassword();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN && resultCode==RESULT_OK) {
            if(data != null) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        }
    }

    //To allow user to sign-in if they have log-out or have already created an account.
    public void signIn(){
        UserData user = UserData.getUserDataFromDatabase(email.getText().toString(), password.getText().toString(),getApplicationContext());
        if(user != null){
            Intent mainActivity = new Intent(this, MainActivity.class);
            mainActivity.putExtra("user data", user);
            startActivity(mainActivity);
            finish();
        }
        else Toast.makeText(getApplicationContext(), "User and Password doesn't matches.",Toast.LENGTH_SHORT).show();
    }

    //To allow user to create account for first time.
    public void signUp(){
        Intent i=new Intent(this,CreateUserPage.class);
        startActivity(i);
    }

    //To allow user to sign in through google using google api.
    public void googleSignIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    //Activity to change password if user forget it accidentally.
    private void forgetPassword() {
        Intent changePasswordIntent = new Intent(this,ForgetPassword.class);
        startActivity(changePasswordIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                Uri personPhoto = acct.getPhotoUrl();
                DownloadImage download = new DownloadImage();
                download.execute(personPhoto.toString());
                Bitmap photo = download.get();
                UserData user = new UserData(personName, null, personEmail, photo);
                //user.uploadUserDataToDatabase(getApplicationContext());
                Intent mainActivity = new Intent(getApplicationContext(), CreateUserPage.class);
                mainActivity.putExtra("user data", user);
                startActivity(mainActivity);
                finish();
            }
        } catch (ApiException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    //To initialize view for interaction
    public void initializeViews() {
        email =findViewById(R.id.username_edittext);
        password=findViewById(R.id.password_edittext);
        signIn=findViewById(R.id.custom_signin_button);
        signIn.setOnClickListener(this);
        createUser=findViewById(R.id.signup_button);
        createUser.setOnClickListener(this);
        signInButton = findViewById(R.id.google_sign_in_button);
        signInButton.setOnClickListener(this);
        forgetPassword=findViewById(R.id.forget_password_tv);
        forgetPassword.setOnClickListener(this);
    }

    //To check weather any active user that have signed-in recently.
    public void checkUserStatus(){
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * "+" FROM "+DBHelper.login_table_name+" WHERE "+DBHelper.signed_in+"= 'True' ",null);

        if(c.getCount() != 0){
            c.moveToFirst();
            UserData user = new UserData(c.getString(c.getColumnIndex(DBHelper.name)),
                    c.getString(c.getColumnIndex(DBHelper.password)),
                    c.getString(c.getColumnIndex(DBHelper.email)),
                    UserData.getBitmapFromByteArray(c.getBlob(c.getColumnIndex(DBHelper.photo))));
            c.close();
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            mainActivity.putExtra("user data", user);
            startActivity(mainActivity);
            finish();

        }
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
            e.printStackTrace();
            return null;
        }
    }

    class DownloadImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            return getBitmapFromURL(strings[0]);
        }
    }
}