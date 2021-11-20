package com.example.travelblogger;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;
import java.util.ArrayList;


public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterViewHolder> {

    private final ArrayList <Uri> mSliderItems;
    Context context;
    boolean isButtonVisible = false;

    public SliderAdapter(Context context, int[] sliderDataArrayList) {
        this.context = context;
        ArrayList <Uri> mSliderItems = new ArrayList<>();
        for(int i:sliderDataArrayList){
            mSliderItems.add(getUriToDrawable(context, i));
        }
        this.mSliderItems = mSliderItems;
    }

    public SliderAdapter(Context context, ArrayList <String> sliderDataArrayList) {
        mSliderItems = new ArrayList<>();
        for(String values: sliderDataArrayList)
            mSliderItems.add(Uri.parse(values));
        this.context = context;
    }

    public Uri getUriToDrawable(@NonNull Context context, @AnyRes int drawableId) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId) );
    }

    @Override
    public SliderAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.imageview_layout, null);
        return new SliderAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterViewHolder viewHolder, final int position) {

        Picasso.get().load(mSliderItems.get(position)).placeholder(R.drawable.ic_add_photo)
                    .into(viewHolder.imageViewBackground);
        if (isButtonVisible) {
            viewHolder.cv.setVisibility(View.VISIBLE);
            viewHolder.delete.setVisibility(View.VISIBLE);
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mSliderItems.isEmpty()) {
                        mSliderItems.remove(position);
                        notifyDataSetChanged();
                    }
                }
            });
        } else {
            viewHolder.delete.setVisibility(View.INVISIBLE);
            viewHolder.cv.setVisibility(View.INVISIBLE);
        }
        Log.d("ImageUri", mSliderItems.get(position).toString());

    }

    void setButtonVisibility(boolean visibility){
        isButtonVisible = visibility;
    }

    @Override
    public int getCount() {
        return mSliderItems.size();
    }

    class SliderAdapterViewHolder extends SliderViewAdapter.ViewHolder {
        View itemView;
        CardView cv;
        ImageView imageViewBackground;
        ImageView delete;
        public SliderAdapterViewHolder(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.image);
            delete = itemView.findViewById(R.id.delete);
            cv=itemView.findViewById(R.id.card_view);
            this.itemView = itemView;
        }
    }
}
