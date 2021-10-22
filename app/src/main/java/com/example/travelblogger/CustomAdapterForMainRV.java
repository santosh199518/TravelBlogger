package com.example.travelblogger;

import android.content.Context;
import android.content.Intent;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import android.graphics.drawable.AnimatedVectorDrawable;
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

import java.util.ArrayList;

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
        PlaceDetails d1=al.get(position);
        holder.name.setText(d1.getName());
        holder.location.setText(d1.getLocation());
        holder.description.setText(d1.getDescription());
        if(d1.getImages() != null) holder.photo.setImageURI(d1.getImages().get(0));
        holder.rb.setRating(d1.getRating());
        for(String value: d1.getSpeciality()){
            Chip chip = (Chip) LayoutInflater.from(context).inflate(R.layout.custom_chip_view, null, false);
            chip.setCloseIconVisible(false);
            chip.setText(value);
            holder.cg.addView(chip);
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
                    emptyHeart = (AnimatedVectorDrawable) AppCompatResources.getDrawable(context, R.drawable.avd_heart_empty);
                    fillHeart = (AnimatedVectorDrawable) AppCompatResources.getDrawable(context, R.drawable.avd_heart_fill);
                    AnimatedVectorDrawable drawable = full ? emptyHeart : fillHeart;
                    favourite.setImageDrawable(drawable);
                    drawable.start();
                    full = !full;

                    UserData user = ((MainActivity) context).user;
                    if(full) {
                        user.favouritePlaces.add(al.get(getAdapterPosition()).getName());
                        Toast.makeText(context, "Place added to Favourite List.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        user.favouritePlaces.remove(al.get(getAdapterPosition()).getName());
                        Toast.makeText(context, "Place removed from Favourite List.", Toast.LENGTH_SHORT).show();
                    }
                    user.uploadFavouritePlacesToDatabase(context);
                    break;

                case R.id.like_btn:
                    int like_count = al.get(getAdapterPosition()).getLikeCount();
                    if (!clicked) {
                        al.get(getAdapterPosition()).setLikeCount(++like_count);
                        like.setBackgroundColor(context.getResources().getColor(R.color.purple_700));
                        like.setTextColor(context.getResources().getColor(R.color.white));
                        like.setText("LIKE ("+like_count+")");
                    }
                    else{
                        al.get(getAdapterPosition()).setLikeCount(--like_count);
                        like.setBackgroundColor(context.getResources().getColor(R.color.white));
                        like.setTextColor(context.getResources().getColor(R.color.purple_700));
                        if(like_count > 0) like.setText("LIKE ("+like_count+")");
                    }
                    clicked = !clicked;
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

//        To Goto ShowPlaceActivity
        public void goToShowPlaceActivity(){
            Intent intent = new Intent(context, ShowPlaceActivity.class);
            intent.putExtra("place", al.get(getAdapterPosition()));
            context.startActivity(intent);
        }
    }
}
