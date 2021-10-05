package com.example.travelblogger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    ArrayList<PlaceDetails> data;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data=new ArrayList<>();
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
        RecyclerView rv = v.findViewById(R.id.main_fragment_rv);
        LinearLayoutManager llm=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        rv.setLayoutManager(llm);
        CustomAdapterForMainRV adapter=new CustomAdapterForMainRV(getActivity(),data);
        rv.setAdapter(adapter);
        return v;
    }
}