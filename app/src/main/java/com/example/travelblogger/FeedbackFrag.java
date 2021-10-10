package com.example.travelblogger;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

public class FeedbackFrag extends Fragment {

    Button submit;
    RatingBar r1,r2,r3,r4,r5,r6;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);
        initializeViewOn(v);
        return v;
    }

    private void initializeViewOn(View v) {
        submit = v.findViewById(R.id.submit_btn);
        r1=v.findViewById(R.id.ans1);
        r2 = v.findViewById(R.id.ans2);
        r3 = v.findViewById(R.id.sub_ans1);
        r4 = v.findViewById(R.id.sub_ans2);
        r5 = v.findViewById(R.id.sub_ans3);
        r6 = v.findViewById(R.id.sub_ans4);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ans1 = (int) r1.getRating();
                int ans2 = (int) r2.getRating();
                int sub_ans1 = (int) r3.getRating();
                int sub_ans2 = (int) r4.getRating();
                int sub_ans3 = (int) r5.getRating();
                int sub_ans4 = (int) r6.getRating();
            }
        });
    }
}