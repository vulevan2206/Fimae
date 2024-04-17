package com.example.fimae.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fimae.fragments.ChatFragment;
import com.example.fimae.fragments.FeedFragment;
import com.example.fimae.fragments.HomeFragment;
import com.example.fimae.fragments.NotificationFragment;
import com.example.fimae.fragments.ProfileFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {


    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new FeedFragment();
            case 2:
                    return new NotificationFragment();
            case 3:
                return new ChatFragment();
            case 4:
                return new ProfileFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
