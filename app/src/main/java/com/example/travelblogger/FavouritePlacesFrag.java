package com.example.travelblogger;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavouritePlacesFrag extends Fragment {

    RecyclerView place_rv;
    ArrayList <PlaceDetails> places;
    CustomAdapterForMainRV adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFavouritePlacesFromFirebase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favourite_places, container, false);
        place_rv = v.findViewById(R.id.place_rv);
        place_rv.setAdapter(adapter);
        return v;
    }

    private void getFavouritePlacesFromFirebase() {
        FirebaseDatabase.getInstance().getReference().child("Users")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("favouritePlaces")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList <String> favourites = (ArrayList<String>) snapshot.getValue();
                    places = new ArrayList<>();
                    DatabaseReference df = FirebaseDatabase.getInstance().getReference().child("Places Details");
                    if(!places.isEmpty()) {
                        for (String place : favourites) {
                            df.child(place).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    PlaceDetails place = snapshot.getValue(PlaceDetails.class);
                                    if (places.contains(place)) places.remove(place);
                                    places.add(place);
                                    adapter = new CustomAdapterForMainRV(getContext(), places);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    else adapter = new CustomAdapterForMainRV(getContext(), places);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

}