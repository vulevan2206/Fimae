package com.example.fimae.adapters.StoryAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fimae.R;
import com.example.fimae.models.story.Story;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class StoryReviewAdapter extends RecyclerView.Adapter<StoryReviewAdapter.StoryReviewHolder> {

    private ArrayList<Story> stories = new ArrayList<>();
    private final IClickCardListener iClickCardListener;

    public interface IClickCardListener {
        void onClickUser(Story story);
    }
    private Query query;

    public StoryReviewAdapter(Query query, IClickCardListener iClickCardListener) {
        this.query = query;
        this.iClickCardListener = iClickCardListener;

    }


    @NonNull
    @Override
    public StoryReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_review_item, parent, false);
        return new StoryReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryReviewHolder holder, int position) {
        Story story = stories.get(position);
        Glide.with(holder.itemView)
                .load(story.getUrl())
                .into(holder.storyImage);

        holder.itemView.setOnClickListener(view -> {
            iClickCardListener.onClickUser(story);
        });
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public static class StoryReviewHolder extends RecyclerView.ViewHolder {
        ImageView storyImage;
        public StoryReviewHolder(View itemView) {
            super(itemView);
            storyImage = itemView.findViewById(R.id.story_image);

        }
    }
}
