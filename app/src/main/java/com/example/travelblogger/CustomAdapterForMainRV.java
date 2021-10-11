package com.example.travelblogger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        if(d1.getImage()!=null) holder.photo.setImageBitmap(d1.getImage());
        holder.rb.setRating(d1.getRating());
    }

    @Override
    public int getItemCount() {
        return al.size();
    }

    static class DataHolder extends RecyclerView.ViewHolder {
        TextView name, location, description;
        ImageView photo;
        LinearLayout ll;
        RatingBar rb;
        public DataHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name_id);
            location=itemView.findViewById(R.id.location_id);
            description=itemView.findViewById(R.id.description_id);
            photo=itemView.findViewById(R.id.place_photo_id);
            rb=itemView.findViewById(R.id.ratingbar_id);
            ll=itemView.findViewById(R.id.ll);
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }
}
