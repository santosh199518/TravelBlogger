package com.example.travelblogger;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderView;
import java.util.Arrays;

public class WelcomePage extends AppCompatActivity {

    SliderView sliderView;
    int []images = { R.drawable.img_2, R.drawable.img_3, R.drawable.img_4, R.drawable.img_5, R.drawable.img_6, R.drawable.img_1 };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        SliderAdapter adapter = new SliderAdapter(this, images);
        sliderView = findViewById(R.id.slider_id);
        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        sliderView.setSliderAdapter(adapter);
        sliderView.setScrollTimeInSec(2);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();

        Handler h = new Handler();
        String uid = null;
        try {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if(uid!=null && isNetworkConnected()) {
            FirebaseDatabase.getInstance().getReference().child("Users").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserData user = snapshot.getValue(UserData.class);
                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivity.putExtra("user data", user);
                    startActivity(mainActivity);
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            UserData user = checkUserStatus();
            if (user != null) {
                h.postDelayed(() -> {
                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivity.putExtra("user data", user);
                    startActivity(mainActivity);
                    finish();
                }, 5000);
            } else {
                h.postDelayed(() -> {
                    Intent loginPage = new Intent(getApplicationContext(), LoginPage.class);
                    startActivity(loginPage);
                    finish();
                }, 5000);
            }
        }

    }

    private boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public UserData checkUserStatus(){
        UserData user = null;
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+DBHelper.login_table_name+" WHERE "+DBHelper.signed_in+"= 'True' ",null);

        if(c.getCount() != 0){
            c.moveToFirst();
            user = new UserData(c.getString(c.getColumnIndex(DBHelper.name)),
                    c.getString(c.getColumnIndex(DBHelper.password)),
                    c.getString(c.getColumnIndex(DBHelper.email)),
                    c.getString(c.getColumnIndex(DBHelper.photo)));

            c.moveToFirst();
            String fav = c.getString(c.getColumnIndex(DBHelper.favouritePlaces));
            if(fav!=null) {
                fav.replace("[","").replace("]","");
                if(!fav.trim().isEmpty()) user.favouritePlaces.addAll(Arrays.asList(fav.split(",")));
            }
            c.moveToFirst();
            String like = c.getString(c.getColumnIndex(DBHelper.like_places));
            if(like!=null ) {
                like = like.replace("[", "").replace("]", "");
                if (!like.trim().isEmpty()) user.likedPlaces.addAll(Arrays.asList(like.split(",")));
            }
            c.close();
        }
        return user;
    }
}