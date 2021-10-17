package com.example.travelblogger;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CustomAdapterForMainRV extends RecyclerView.Adapter<CustomAdapterForMainRV.DataHolder> {

    Context context;
    ArrayList<PlaceDetails> al;
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
        if(d1.getImages() != null) holder.photo.setImageBitmap(d1.getImages().get(0));
        holder.rb.setRating(d1.getRating());
    }

    @Override
    public int getItemCount() {
        return al.size();
    }

    class DataHolder extends RecyclerView.ViewHolder {
        TextView name, location, description;
        ImageView photo, favourite;
        RatingBar rb;
        Button like, comment;
        AnimatedVectorDrawable emptyHeart;
        AnimatedVectorDrawable fillHeart;
        boolean full = false, clicked=false;

        public DataHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name_id);
            location=itemView.findViewById(R.id.location_id);
            description=itemView.findViewById(R.id.description_id);
            photo=itemView.findViewById(R.id.place_photo_id);
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            rb=itemView.findViewById(R.id.ratingbar_id);
            like = itemView.findViewById(R.id.like_btn);
            like.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (!clicked) {
                        like.setBackgroundColor(context.getResources().getColor(R.color.purple_700));
                        like.setTextColor(context.getResources().getColor(R.color.white));
                    }
                    else{
                        like.setBackgroundColor(context.getResources().getColor(R.color.white));
                        like.setTextColor(context.getResources().getColor(R.color.purple_700));
                    }
                    clicked = !clicked;
                }
            });
            comment = itemView.findViewById(R.id.comment_btn);
            comment.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                }
            });
            emptyHeart = (AnimatedVectorDrawable) AppCompatResources.getDrawable(context, R.drawable.avd_heart_empty);
            fillHeart = (AnimatedVectorDrawable) AppCompatResources.getDrawable(context, R.drawable.avd_heart_fill);
            favourite = itemView.findViewById(R.id.favourite_iv);
            favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnimatedVectorDrawable drawable = full ? emptyHeart : fillHeart;
                    favourite.setImageDrawable(drawable);
                    drawable.start();
                    full = !full;
                }
            });
        }
    }
}
