package com.example.travelblogger;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutUsFrag extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_us, container, false);
    }
}