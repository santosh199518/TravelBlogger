package com.example.travelblogger;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    UserData user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = (UserData) getIntent().getSerializableExtra("user data");
        drawerLayout =findViewById(R.id.main_drawer);
        actionBarDrawerToggle =new ActionBarDrawerToggle(this, drawerLayout,R.string.open,R.string.close);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        createNavigationView();
    }

    void createNavigationView() {
        NavigationView nv = findViewById(R.id.nav_view);
        View header = nv.getHeaderView(0);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showProfile = new Intent(MainActivity.this, CreateUserPage.class);
                showProfile.putExtra("ToProfile",true);
                showProfile.putExtra("user data", user);
                startActivity(showProfile);
            }
        });
        TextView name = header.findViewById(R.id.username_tv);
        name.setText(user.getUsername());

        TextView email=header.findViewById(R.id.email_tv);
        email.setText(user.getEmail());

        ImageView photo=header.findViewById(R.id.photo_iv);
        Picasso.get().load(user.getImageUri()).placeholder(R.drawable.ic_person).into(photo);

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
                    case R.id.favourite_placesID:
                        f=new FavouritePlacesFrag();
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
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        if (isNetworkConnected()) menu.findItem(R.id.connectivity).setIcon(R.drawable.ic_online);
        else menu.findItem(R.id.connectivity).setIcon(R.drawable.ic_offline);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        else{
            if(item.getItemId() == R.id.report)
                Toast.makeText(getApplicationContext(), "Report submitted",Toast.LENGTH_SHORT).show();
            else if(item.getItemId() == R.id.notification_id){
                Intent notification = new Intent(getApplicationContext(), MyNotification.class);
                startActivity(notification);
            }
        }
        return true;
    }

    public boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

//    void createNotification( String message, PlaceDetails place){
//        int notificationId = 1;
//        Intent intent = new Intent(this, ShowPlaceActivity.class);
//        intent.putExtra("place",place);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.tour)
//                .setContentTitle(place.getName())
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.notify(notificationId, builder.build());
//    }


}