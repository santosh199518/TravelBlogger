package com.example.travelblogger;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    DrawerLayout obj;
    ActionBarDrawerToggle abdt;
    UserData user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = (UserData) getIntent().getSerializableExtra("user data");
        obj=findViewById(R.id.main_drawer);
        abdt=new ActionBarDrawerToggle(this,obj,R.string.open,R.string.close);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        obj.addDrawerListener(abdt);
        abdt.syncState();

        logic();
    }

    void logic() {
        NavigationView nv=findViewById(R.id.nav_view);
        View header=nv.getHeaderView(0);
        TextView name=header.findViewById(R.id.username_tv);
        name.setText(user.getUsername());
        TextView email=header.findViewById(R.id.email_tv);
        email.setText(user.getEmail());
        ImageView photo=header.findViewById(R.id.photo_iv);
        photo.setImageBitmap(user.getPhoto());
        getSupportFragmentManager().beginTransaction().replace(R.id.fl,new MainFragment()).commit();

        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment f=null;
                int id=item.getItemId();
                switch (id){
                    case R.id.homeID:
                        f=new MainFragment();
                        break;
                    case R.id.aboutID:
                        f=new AboutUsFrag();
                        break;
                    case R.id.feedbackID:
                        f=new FeedbackFrag();
                        break;
                    case R.id.settingID:
                        f=new SettingFrag();
                        break;
                    case R.id.shareID:
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT,"Travel Blogging Application");
                        String app_url = "";
                        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT,app_url);
                        Intent shareIntent = Intent.createChooser(sendIntent, "Share via");
                        startActivity(shareIntent);
                        break;
                    case R.id.logoutID:
                        Intent i=new Intent(getApplicationContext(),LoginPage.class);
                        user.logout(getApplicationContext());
                        startActivity(i);
                        finish();
                }
                if(f!=null){
                    FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fl,f);
                    ft.commit();
                    obj.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (abdt.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}