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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

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
        float []feedbacks = DBHelper.getFeedback(context);
        r1.setRating(feedbacks[0]);
        r2.setRating(feedbacks[1]);
        r3.setRating(feedbacks[2]);
        r4.setRating(feedbacks[3]);
        r5.setRating(feedbacks[4]);
        r6.setRating(feedbacks[5]);
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
                HashMap<String, Integer> feedbacks = new HashMap<>();
                feedbacks.put(DBHelper.question1, (int)r1.getRating());
                feedbacks.put(DBHelper.question2, (int)r2.getRating());
                feedbacks.put(DBHelper.sub_question1, (int)r3.getRating());
                feedbacks.put(DBHelper.sub_question2, (int)r4.getRating());
                feedbacks.put(DBHelper.sub_question3, (int)r5.getRating());
                feedbacks.put(DBHelper.sub_question4, (int)r6.getRating());
                updateFeedbackInFirebase(feedbacks);
            }
        });
    }

    void updateFeedbackInFirebase(HashMap <String, Integer> feedbacks){
        FirebaseDatabase.getInstance().getReference().child("Feedbacks")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(feedbacks)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        DBHelper.updateFeedback(context, feedbacks);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}