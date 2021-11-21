package com.example.travelblogger;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    ProgressBar pb;
    TextView msg;
    ArrayList <PlaceDetails> places;
    CustomAdapterForMainRV adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favourite_places, container, false);
        place_rv = v.findViewById(R.id.place_rv);
        pb = v.findViewById(R.id.pb);
        msg = v.findViewById(R.id.msg);
        msg.setVisibility(View.INVISIBLE);
        getFavouritePlacesFromFirebase();
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
                    if(!favourites.isEmpty()) {
                        for (String placeName : favourites) {
                            df.child(placeName).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    PlaceDetails newPlace = snapshot.getValue(PlaceDetails.class);
                                    Log.d("tag","PlaceName:\t"+placeName+"ObtainedName:\t"+newPlace.getName());
                                    places.add(newPlace);
                                    adapter = new CustomAdapterForMainRV(getContext(), places);
                                    place_rv.setAdapter(adapter);
                                    LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false);
                                    place_rv.setLayoutManager(llm);
                                    pb.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    else {
                        adapter = new CustomAdapterForMainRV(getContext(), places);
                        place_rv.setAdapter(adapter);
                        pb.setVisibility(View.GONE);
                        msg.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

}