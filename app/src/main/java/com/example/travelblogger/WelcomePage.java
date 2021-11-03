package com.example.travelblogger;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.smarteist.autoimageslider.SliderView;
import java.util.HashSet;

public class WelcomePage extends AppCompatActivity {

    SliderView sliderView;
    int []images = {R.drawable.img_6, R.drawable.img_1, R.drawable.img_2, R.drawable.img_3, R.drawable.img_4, R.drawable.img_5 };
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

        Handler h=new Handler();
        UserData user = checkUserStatus();
        if(user != null) {
            h.postDelayed(() -> {
                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                mainActivity.putExtra("user data", user);
                startActivity(mainActivity);
                finish();
            }, 5000);
        }
        else{
            h.postDelayed(() -> {
                Intent loginPage = new Intent(getApplicationContext(), LoginPage.class);
                startActivity(loginPage);
                finish();
            }, 5000);
        }
    }

    public UserData checkUserStatus(){
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+DBHelper.login_table_name+" WHERE "+DBHelper.signed_in+"= 'True' ",null);
        UserData user = null;
        if(c.getCount() != 0){
            c.moveToFirst();
            user = new UserData(c.getString(c.getColumnIndex(DBHelper.name)),
                    c.getString(c.getColumnIndex(DBHelper.password)),
                    c.getString(c.getColumnIndex(DBHelper.email)),
                    UserData.getBitmapFromByteArray(c.getBlob(c.getColumnIndex(DBHelper.photo))));

            c.moveToFirst();
            String fav = c.getString(c.getColumnIndex(DBHelper.favouritePlaces));
            HashSet<String> favouritePlaces = new HashSet<>();
            if(fav != null) {
                fav = fav.replace("[", "").replace("]", "");
                for (String name : fav.split(",")) {
                    favouritePlaces.add(name.trim());
                }
            }
            user.favouritePlaces = favouritePlaces;
            c.moveToFirst();
            String like = c.getString(c.getColumnIndex(DBHelper.like_places));
            HashSet<String> likedPlaces = new HashSet<>();
            if(like != null) {
                like = like.replace("[", "").replace("]", "");
                for (String name : like.split(",")) {
                    likedPlaces.add(name.trim());
                }
            }
            user.likedPlaces = likedPlaces;
            c.close();
        }
        return user;
    }
}