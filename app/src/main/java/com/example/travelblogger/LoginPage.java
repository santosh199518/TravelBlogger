package com.example.travelblogger;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

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
        
        initializeViews();
        GoogleSignInOptions gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in_button:
                googleSignIn(v);
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
        UserData user = UserData.getUserDataFromDatabase(email.getText().toString(),
                password.getText().toString(),getApplicationContext());
        if(user != null){
            Intent mainActivity = new Intent(this, MainActivity.class);
            mainActivity.putExtra("user data", user);
            startActivity(mainActivity);
            finish();
        }
        else if(isNetworkConnected()){
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                DatabaseReference df = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                df.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        UserData user = task.getResult().getValue(UserData.class);
                                        if (user != null) {
                                            user.uploadUserDataToDatabase(getApplicationContext());
                                            Intent mainActivity = new Intent(LoginPage.this, MainActivity.class);
                                            mainActivity.putExtra("user data", user);
                                            startActivity(mainActivity);
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(!isNetworkConnected())
            Toast.makeText(getApplicationContext(), "No Internet Access",Toast.LENGTH_SHORT).show();
    }

    //To allow user to create account for first time.
    public void signUp(){
        Intent i=new Intent(this,CreateUserPage.class);
        startActivity(i);
    }

    //To allow user to sign in through google using google api.
    public void googleSignIn(View v){
        if (isNetworkConnected()){
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
        }
        else {
            Snackbar snackbar = Snackbar
                    .make(v, "No Internet Connection", Snackbar.LENGTH_LONG)
                    .setAction("Try Again", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            googleSignIn(v);
                        }
                    });
            snackbar.show();
        }

    }

    //Activity to change password if user forget it accidentally.
    private void forgetPassword() {
        String e = Objects.requireNonNull(email.getText()).toString();
        if(e.trim().isEmpty()){
            email.setError("Insert Email");
            return;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(e).matches()){
            email.setError("Invalid Email");
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(e).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    DBHelper helper = new DBHelper(getApplicationContext());
                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.delete(DBHelper.login_table_name,DBHelper.email,new String[]{e});
                    Toast.makeText(getApplicationContext(), "Password resent link send to " + e + " successfully.", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                Uri personPhoto = acct.getPhotoUrl();

                UserData user = new UserData(personName, null, personEmail, personPhoto.toString());
                Intent mainActivity = new Intent(getApplicationContext(), CreateUserPage.class);
                mainActivity.putExtra("user data", user);
                startActivity(mainActivity);
                finish();
            }
        } catch (ApiException e) {
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

    public boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


}
