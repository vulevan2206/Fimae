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
import com.example.fimae.adapters.FirestoreAdapter;
import com.example.fimae.models.story.Story;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class StoryReviewProfileAdapter extends FirestoreAdapter<StoryReviewProfileAdapter.StoryReviewHolder> {
    ArrayList<Story> stories = new ArrayList<>();
    private IClickCardListener iClickCardListener;

    public interface IClickCardListener {
        void onClickUser(Story story);
    }

    public StoryReviewProfileAdapter(Query query, IClickCardListener iClickCardListener) {
        super(query);
        this.iClickCardListener = iClickCardListener;
    }

    @NonNull
    @Override
    public StoryReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_review_profile_item, parent, false);
        return new StoryReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryReviewHolder holder, int position) {
        Story story = stories.get(position);
        Glide.with(holder.itemView)
                .load(story.getUrl())
                .into(holder.storyImage);
        holder.storyImage.setOnClickListener(view -> {
            iClickCardListener.onClickUser(story);
        });
    }

    public static String formatNumber(int number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return formatWithDot(number / 1000.0, "k");
        } else {
            return formatWithDot(number / 1000000.0, "M");
        }
    }

    private static String formatWithDot(double value, String unit) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.#");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        String formattedValue = decimalFormat.format(value);
        return formattedValue + unit;
    }

    @Override
    public void OnSuccessQueryListener(ArrayList<DocumentSnapshot> queryDocumentSnapshots, ArrayList<DocumentChange> documentChanges) {
        if (stories == null) {
            stories = new ArrayList<>();
        } else {
            stories.clear();
        }
        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
            Story story = documentSnapshot.toObject(Story.class);
            stories.add(story);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public static class StoryReviewHolder extends RecyclerView.ViewHolder {
        ImageView storyImage;
        public StoryReviewHolder(View itemView) {
            super(itemView);
            storyImage = itemView.findViewById(R.id.story_review_profile_image);

        }
    }
}
