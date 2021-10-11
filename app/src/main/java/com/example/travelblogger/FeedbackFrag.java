package com.example.travelblogger;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

public class FeedbackFrag extends Fragment {

    Button submit;
    RatingBar r1,r2,r3,r4,r5,r6;
    Context context;
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
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
                double []answers = new double[6];
                answers[0] = r1.getRating();
                answers[1] = r2.getRating();
                answers[2] = r3.getRating();
                answers[3] = r4.getRating();
                answers[4] = r5.getRating();
                answers[5] = r6.getRating();
                DBHelper.updateFeedback(context, answers);
            }
        });
    }
}