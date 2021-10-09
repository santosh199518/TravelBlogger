package com.example.travelblogger;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.smarteist.autoimageslider.SliderViewAdapter;


public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterViewHolder> {

    private final int[] mSliderItems;
    public SliderAdapter(Context context, int[] sliderDataArrayList) {
        this.mSliderItems = sliderDataArrayList;
    }

    @Override
    public SliderAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.imageview_layout, null);
        return new SliderAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterViewHolder viewHolder, final int position) {

        final int sliderItem = mSliderItems[position];
        viewHolder.imageViewBackground.setImageResource(sliderItem);
    }

    @Override
    public int getCount() {
        return mSliderItems.length;
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
