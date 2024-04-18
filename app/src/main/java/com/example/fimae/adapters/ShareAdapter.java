package com.example.fimae.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fimae.R;
import com.example.fimae.activities.DetailPostActivity;
import com.example.fimae.databinding.ShareItemUserBinding;
import com.example.fimae.models.Fimaers;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ViewHolder> {
    private Context mContext;
    private List<Fimaers> userInfos;
    private DetailPostActivity.BottomItemClickCallback callback;

    public ShareAdapter(Context mContext, List<Fimaers> userInfos, DetailPostActivity.BottomItemClickCallback callback) {
        this.mContext = mContext;
        this.userInfos = userInfos;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ShareItemUserBinding binding = ShareItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareAdapter.ViewHolder holder, int position) {
        ShareItemUserBinding binding = holder.binding;
        Fimaers userInfo = userInfos.get(position);
        String fullName = userInfo.getFirstName() + " " + userInfo.getLastName(); // Tạo chuỗi tên đầy đủ từ firstName và lastName
        binding.userName.setText(fullName);
        Picasso.get().load(userInfo.getAvatarUrl()).placeholder(R.drawable.ic_default_avatar).into(binding.imageAvatar);
        binding.slogan.setText(userInfo.getBio());
        if (!userInfo.isGender()) {
            binding.itemUserIcGender.setImageResource(R.drawable.ic_male);
            binding.genderAgeIcon.setBackgroundResource(R.drawable.shape_gender_border_pink);
        }
        binding.ageTextView.setText(String.valueOf(userInfo.calculateAge()));
        binding.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onClick(userInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userInfos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShareItemUserBinding binding;

        public ViewHolder(ShareItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
