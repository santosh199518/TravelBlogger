package com.example.travelblogger;

import android.content.Context;
import android.content.Intent;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.util.Log;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

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
        ArrayList<String> keySet = new ArrayList<>(d1.getImages().keySet());
        if(!d1.getImages().isEmpty()) Picasso.get().load(d1.getImages().get(keySet.get(0))).placeholder(R.drawable.ic_add_photo).into(holder.photo);
        holder.rb.setRating(d1.getRating());

        holder.cg.removeAllViews();

        for(String title: d1.getSpeciality().values()){
            Chip chip =(Chip) LayoutInflater.from(context).inflate(R.layout.custom_chip_view, null, false);
            chip.setCloseIconVisible(false);
            chip.setTextAlignment(Chip.TEXT_ALIGNMENT_TEXT_START);
            chip.setText(title);
            holder.cg.addView(chip);
        }

        UserData user = ((MainActivity) context).user;

        if(Arrays.asList(user.likedPlaces).contains(al.get(position).getName())){
            holder.like.setBackgroundColor(context.getResources().getColor(R.color.purple_700));
            holder.like.setTextColor(context.getResources().getColor(R.color.white));
            holder.clicked = true;
        }

        if(Arrays.asList(user.favouritePlaces).contains(al.get(position).getName())){
            holder.full = false;
            holder.fillHeartAnimation();
        }
    }

    @Override
    public int getItemCount() {
        return al.size();
    }

    class DataHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name, location, description;
        ImageView photo, favourite;
        RatingBar rb;
        Button like, comment;
        ChipGroup cg;
        Chip []chip = new Chip[5];

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

            photo=itemView.findViewById(R.id.place_photo_id);
            photo.setOnClickListener(this);

            rb=itemView.findViewById(R.id.ratingbar_id);
            like = itemView.findViewById(R.id.like_btn);
            like.setOnClickListener(this);

            comment = itemView.findViewById(R.id.comment_btn);
            comment.setOnClickListener(this);

            favourite = itemView.findViewById(R.id.favourite_iv);
            favourite.setOnClickListener(this);
            cg =itemView.findViewById(R.id.place_speciality_cg);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.favourite_iv:
                    fillHeartAnimation();
                    UserData user = ((MainActivity) context).user;
                    if(full) {
                        user.addFavouritePlace(al.get(getAdapterPosition()).getName());
                        Toast.makeText(context, al.get(getAdapterPosition()).getName()+" added to Favourite List.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        user.removeFavouritePlace(al.get(getAdapterPosition()).getName());
                        Toast.makeText(context, al.get(getAdapterPosition()).getName()+" removed from Favourite List.", Toast.LENGTH_SHORT).show();
                    }
                    user.uploadFavouritePlacesToDatabase(context);
                    break;

                case R.id.like_btn:
                    int like_count = al.get(getAdapterPosition()).getLikeCount();
                    UserData user1 = ((MainActivity) context).user;
                    if (!clicked) {
                        user1.addLikedPlace(al.get(getAdapterPosition()).getName());
                        al.get(getAdapterPosition()).setLikeCount(++like_count);
                        like.setBackgroundColor(context.getResources().getColor(R.color.purple_700));
                        like.setTextColor(context.getResources().getColor(R.color.white));
                    }
                    else{
                        user1.removeLikedPlace(al.get(getAdapterPosition()).getName());
                        al.get(getAdapterPosition()).setLikeCount(--like_count);
                        like.setBackgroundColor(context.getResources().getColor(R.color.white));
                        like.setTextColor(context.getResources().getColor(R.color.purple_700));
                    }
                    clicked = !clicked;
                    user1.uploadLikedPlacesToDatabase(context);
                    al.get(getAdapterPosition()).updateLikeCountToDataBase(context);
                    break;

                case R.id.name_id:
                case R.id.location_id:
                case R.id.description_id:
                case R.id.place_photo_id:
                case R.id.comment_btn:
                    goToShowPlaceActivity();
                    break;
            }
        }

        private void fillHeartAnimation() {

            emptyHeart = (AnimatedVectorDrawable) AppCompatResources.getDrawable(context, R.drawable.avd_heart_empty);
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
