package com.example.fimae.adapters.MediaSliderAdapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


import com.example.fimae.fragments.SliderMediaFragment;

import java.util.ArrayList;

public class MediaSliderAdapter extends FragmentStateAdapter {
    Context context;
    ArrayList<MediaSliderItem> mediaSliderItems;

    public MediaSliderAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<MediaSliderItem> items) {
        super(fragmentActivity);
        this.mediaSliderItems = items;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new SliderMediaFragment(mediaSliderItems.get(position));
    }

    @Override
    public int getItemCount() {
        if (mediaSliderItems != null)
            return mediaSliderItems.size();
        return 0;
    }

}
