package com.example.travelblogger;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    private static final int ADD_PLACE_INTENT = 100;
    ArrayList <PlaceDetails> places;
    CustomAdapterForMainRV adapter;
    RecyclerView rv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        places =new ArrayList<>();
        places.addAll(PlaceDetails.getPlaceDetailsFromDatabase(getActivity()));
        places.add(new PlaceDetails());
        places.add(new PlaceDetails());
        places.add(new PlaceDetails());
        places.add(new PlaceDetails());
        places.add(new PlaceDetails());
        places.add(new PlaceDetails());
        adapter=new CustomAdapterForMainRV(getActivity(), places);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
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
}