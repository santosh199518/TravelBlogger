package com.example.travelblogger;

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
import java.util.Objects;

public class MainFragment extends Fragment {

    private static final int ADD_PLACE_INTENT = 100;
    ArrayList<PlaceDetails> data;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data=new ArrayList<>();
        data.addAll(PlaceDetails.getPlaceDetailsFromDatabase(getActivity()));
        data.add(new PlaceDetails());
        data.add(new PlaceDetails());
        data.add(new PlaceDetails());
        data.add(new PlaceDetails());
        data.add(new PlaceDetails());
        data.add(new PlaceDetails());
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
                requireActivity().startActivity(intent);
            }
        });
        RecyclerView rv = v.findViewById(R.id.main_fragment_rv);
        LinearLayoutManager llm=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        rv.setLayoutManager(llm);
        CustomAdapterForMainRV adapter=new CustomAdapterForMainRV(getActivity(),data);
        rv.setAdapter(adapter);
        return v;
    }

}