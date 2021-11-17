package com.example.travelblogger;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;

import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterViewHolder> {


    private final ArrayList <Uri> mSliderItems;
    Context context;
    public SliderAdapter(Context context, int[] sliderDataArrayList) {
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
    public SliderAdapter(Context context, String[] sliderDataArrayList) {
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
        Picasso.get().load(mSliderItems.get(position)).placeholder(R.drawable.ic_add_photo).into(viewHolder.imageViewBackground);
        Log.d("ImageUri", mSliderItems.get(position).toString());
    }

    @Override
    public int getCount() {
        return mSliderItems.size();
    }

    static class SliderAdapterViewHolder extends SliderViewAdapter.ViewHolder {
        View itemView;
        ImageView imageViewBackground;

        public SliderAdapterViewHolder(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.image);
            this.itemView = itemView;
        }
    }
}
