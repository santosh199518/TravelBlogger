package com.example.travelblogger;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    private static final int ADD_PLACE_INTENT = 100;
    ArrayList <PlaceDetails> places;
    CustomAdapterForMainRV adapter;
    RecyclerView rv;
    ProgressBar progress;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        places =new ArrayList<>();
        getPlaceDetailsFromFirebase(getActivity());
        adapter=new CustomAdapterForMainRV(getActivity(), places);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        progress = v.findViewById(R.id.progress);
        FloatingActionButton add = v.findViewById(R.id.add_fab);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),AddPlacesActivity.class);
                intent.putExtra("user data",((MainActivity) requireActivity()).user);
                intent.putExtra("places_data", places);
                getActivity().startActivityForResult(intent, ADD_PLACE_INTENT);
            }
        });
        rv = v.findViewById(R.id.main_fragment_rv);
        LinearLayoutManager llm=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_PLACE_INTENT && resultCode == RESULT_OK && data!=null){
            PlaceDetails place = (PlaceDetails) data.getSerializableExtra("new_place");
            places.add(place);
            adapter.notifyItemInserted(places.indexOf(place));
        }
    }

    void getPlaceDetailsFromFirebase(Context context){
        FirebaseDatabase.getInstance().getReference().child("Places Details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                places =new ArrayList<>();
                for(DataSnapshot place : snapshot.getChildren()){
                    PlaceDetails p = place.getValue(PlaceDetails.class);
                    places.add(p);
                    adapter=new CustomAdapterForMainRV(getActivity(), places);
                    rv.setAdapter(adapter);
                    progress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Database Error:"+error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Database Error:",error.getDetails()+"\n"+error.getMessage());
            }
        });
    }
}