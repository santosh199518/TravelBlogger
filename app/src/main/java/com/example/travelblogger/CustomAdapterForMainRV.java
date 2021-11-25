package com.example.travelblogger;

import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.logging.LogRecord;

public class CustomAdapterForMainRV extends RecyclerView.Adapter<CustomAdapterForMainRV.DataHolder> {

    Context context;
    ArrayList <PlaceDetails> al;
    CustomAdapterForMainRV(Context context, ArrayList<PlaceDetails> places){
        this.context = context;
        al=places;
    }

    @NonNull
    @Override
    public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li=LayoutInflater.from(context);
        View v1=li.inflate(R.layout.layout_for_places_rv,parent,false);
        return new DataHolder(v1);
    }

    @Override
    public void onBindViewHolder(@NonNull DataHolder holder, int position) {
        PlaceDetails d1 = al.get(position);
        holder.name.setText(d1.getName());
        holder.location.setText(d1.getLocation());
        holder.description.setText(d1.getDescription());
        if(d1.getLikeCount()>0){
            holder.likeCount.setVisibility(View.VISIBLE);
            holder.likeCount.setText("Liked by "+d1.getLikeCount()+" others");
        }
        else holder.likeCount.setVisibility(View.GONE);
        ArrayList<String> keySet = new ArrayList<>(d1.getImages().keySet());
        if(!d1.getImages().isEmpty()) Picasso.get().load(d1.getImages().get(keySet.get(0))).placeholder(R.drawable.ic_add_photo).into(holder.photo);
        holder.rb.setRating(d1.getRating().get("averageRating"));

        holder.cg.removeAllViews();
        int i=0;
        for(String title: d1.getSpeciality().values()){
            if(i<2) {
                i++;
                Chip chip = (Chip) LayoutInflater.from(context).inflate(R.layout.custom_chip_view, null, false);
                chip.setCloseIconVisible(false);
                chip.setTextAlignment(Chip.TEXT_ALIGNMENT_TEXT_START);
                chip.setText(title);
                holder.cg.addView(chip);
            }
        }
        i=0;
        UserData user = ((MainActivity) context).user;
        if(user.likedPlaces != null && user.likedPlaces.contains(al.get(position).getName())){
            if(d1.getLikeCount()==1)    holder.likeCount.setText("Liked by You");
            else    holder.likeCount.setText("Liked by You and "+(d1.getLikeCount()-1)+" others");
            holder.like.setBackgroundColor(context.getResources().getColor(R.color.purple_700,null));
            holder.like.setTextColor(context.getResources().getColor(R.color.white,null));
            holder.clicked = true;
        }

        if(user.favouritePlaces!=null && user.favouritePlaces.contains(al.get(position).getName())){
            holder.full = false;
            holder.fillHeartAnimation();
        }
        holder.uploadedDate.setText(al.get(position).getUploadedDate());
        DatabaseReference df = FirebaseDatabase.getInstance().getReference().child("Users").child(al.get(position).getUploadedBy());
        Task <DataSnapshot> task = df.get();
        while(!task.isComplete()){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            },1000);
        }
        UserData uploader = task.getResult().getValue(UserData.class);
        holder.uploaderName.setText(uploader.getUsername());
        Picasso.get().load(uploader.getImageUri()).placeholder(R.drawable.ic_person).into(holder.uploaderImage);
    }

    @Override
    public int getItemCount() {
        return al.size();
    }

    class DataHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name, location, description, likeCount, uploaderName, uploadedDate;
        ImageView photo, favourite, uploaderImage;
        RatingBar rb;
        Button like, share;
        ChipGroup cg;

        AnimatedVectorDrawable emptyHeart;
        AnimatedVectorDrawable fillHeart;
        boolean full = false, clicked=false;

        public DataHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name_id);
            name.setOnClickListener(this);

            location=itemView.findViewById(R.id.location_id);
            description=itemView.findViewById(R.id.description_id);
            description.setOnClickListener(this);
            likeCount = itemView.findViewById(R.id.like_count);

            photo=itemView.findViewById(R.id.place_photo_id);
            photo.setOnClickListener(this);

            rb=itemView.findViewById(R.id.ratingbar_id);
            like = itemView.findViewById(R.id.like_btn);
            like.setOnClickListener(this);

            share = itemView.findViewById(R.id.share);
            share.setOnClickListener(this);

            favourite = itemView.findViewById(R.id.favourite_iv);
            favourite.setOnClickListener(this);
            cg =itemView.findViewById(R.id.place_speciality_cg);

            uploaderName = itemView.findViewById(R.id.uploader_name);
            uploaderImage = itemView.findViewById(R.id.uploader_image);
            uploadedDate = itemView.findViewById(R.id.uploaded_date);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.favourite_iv:
                    fillHeartAnimation();
                    UserData user = ((MainActivity) context).user;
                    if(user.favouritePlaces == null) user.favouritePlaces = new ArrayList<>();
                    if(full) {
                        user.favouritePlaces.add(al.get(getAdapterPosition()).getName());
                        Toast.makeText(context, al.get(getAdapterPosition()).getName()+" added to Favourite List.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        user.favouritePlaces.remove(al.get(getAdapterPosition()).getName());
                        Toast.makeText(context, al.get(getAdapterPosition()).getName()+" removed from Favourite List.", Toast.LENGTH_SHORT).show();
                    }
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("favouritePlaces").setValue(user.favouritePlaces);
                    user.uploadFavouritePlacesToDatabase(context);
                    break;

                case R.id.like_btn:
                    int like_count = al.get(getAdapterPosition()).getLikeCount();
                    UserData user1 = ((MainActivity) context).user;
                    if(user1.likedPlaces == null) user1.likedPlaces = new ArrayList<>();
                    if (!clicked) {
                        user1.likedPlaces.add(al.get(getAdapterPosition()).getName());
                        al.get(getAdapterPosition()).setLikeCount(++like_count);
                        like.setBackgroundColor(context.getResources().getColor(R.color.teal_200));
                        like.setTextColor(context.getResources().getColor(R.color.white));
                    }
                    else{
                        user1.likedPlaces.remove(al.get(getAdapterPosition()).getName());
                        al.get(getAdapterPosition()).setLikeCount(--like_count);
                        like.setBackgroundColor(context.getResources().getColor(R.color.white));
                        like.setTextColor(context.getResources().getColor(R.color.teal_200));
                    }
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("likedPlaces").setValue(user1.likedPlaces);
                    user1.uploadLikedPlacesToDatabase(context);
                    al.get(getAdapterPosition()).updateLikeCountToDataBase(context);
                    clicked = !clicked;
                    break;

                case R.id.share:
                    Intent sendIntent = new Intent();
                    sendIntent.putExtra(Intent.EXTRA_TITLE,al.get(getAdapterPosition()).getName());
                    ArrayList<Uri> images = new ArrayList<>();
                    for(String imagePath: al.get(getAdapterPosition()).getImages().values()){
                        images.add(Uri.parse(imagePath));
                    }
                    sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,images);
                    sendIntent.setType("image/*");
                    String app_url = "https://github.com/santosh199518/TravelBlogger/blob/master/app-debug.apk";
                    sendIntent.putExtra(android.content.Intent.EXTRA_TEXT,app_url);
                    Intent shareIntent = Intent.createChooser(sendIntent, "Share via");
                    context.startActivity(shareIntent);
                    break;
                case R.id.name_id:
                case R.id.location_id:
                case R.id.description_id:
                case R.id.place_photo_id:
                    goToShowPlaceActivity();
                    break;
            }
        }

        private void fillHeartAnimation() {

            emptyHeart = (AnimatedVectorDrawable) ContextCompat.getDrawable(context, R.drawable.avd_heart_empty);
            fillHeart = (AnimatedVectorDrawable) AppCompatResources.getDrawable(context, R.drawable.avd_heart_fill);
            AnimatedVectorDrawable drawable = full ? emptyHeart : fillHeart;
            favourite.setImageDrawable(drawable);
            drawable.start();
            full = !full;
        }

        //        To Goto ShowPlaceActivity
        public void goToShowPlaceActivity(){
            Intent intent = new Intent(context, ShowPlaceActivity.class);
            intent.putExtra("place", al.get(getAdapterPosition()));
            context.startActivity(intent);
        }
    }
}
