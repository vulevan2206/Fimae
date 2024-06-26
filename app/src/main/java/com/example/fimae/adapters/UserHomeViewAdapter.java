package com.example.fimae.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fimae.R;
import com.example.fimae.activities.DetailPostActivity;
import com.example.fimae.fragments.FimaeBottomSheet;
import com.example.fimae.models.BottomSheetItem;
import com.example.fimae.models.Fimaers;
import com.example.fimae.models.Follows;
import com.example.fimae.repository.FollowRepository;
import com.example.fimae.repository.PostRepository;
import com.example.fimae.utils.StringUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserHomeViewAdapter extends RecyclerView.Adapter<UserHomeViewAdapter.ViewHolder> implements Filterable {

    private List<Fimaers> mUsers;
    private List<Fimaers> mUsersOld;
    private IClickCardUserListener iClickCardUserListener;

    Context context;
    public interface IClickCardUserListener {
        void onClickUser(Fimaers user);
    }
    public UserHomeViewAdapter(Context context){
        this.context = context;
    }
    public void setData(List<Fimaers> mUsers, IClickCardUserListener inIClickCardUserListener) {
        this.mUsers = mUsers;
        mUsersOld = mUsers;
        this.iClickCardUserListener = inIClickCardUserListener;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mLayoutCard;
        private CircleImageView mAvatarView;
        private TextView mTextName;
        private TextView mTextDes;

        private TextView mTextAge;
        private LinearLayout mLayoutGenderAge;
        private ImageView mIconGender;
        private ImageView onlineStatus;
        private TextView offlineStatus;
//        private TextView follow;
//        private TextView goChat;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mLayoutCard = itemView.findViewById(R.id.item_user_layout_card);
            mAvatarView = itemView.findViewById(R.id.item_user_avatar_view);
            mTextName = itemView.findViewById(R.id.item_user_tv_name);
            mTextDes = itemView.findViewById(R.id.item_user_tv_description);
            mTextAge = itemView.findViewById(R.id.item_user_tv_age);
            mLayoutGenderAge = itemView.findViewById(R.id.item_user_layout_gender_age);
            mIconGender = itemView.findViewById(R.id.item_user_ic_gender);
            onlineStatus = itemView.findViewById(R.id.imv_status_indicator);
            offlineStatus = itemView.findViewById(R.id.tv_status);
//            follow = itemView.findViewById(R.id.follow);
//            goChat = itemView.findViewById(R.id.chat);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View heroView = inflater.inflate(R.layout.item_user, parent, false);
        return new ViewHolder(heroView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Fimaers user = mUsers.get(position);
        if (user == null) {
            return;
        }
        Picasso.get().load(user.getAvatarUrl()).placeholder(R.drawable.ic_default_avatar).into(holder.mAvatarView);
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        // Check if firstName is null, if null, use an empty string instead
        firstName = firstName != null ? firstName : "";
        // Check if lastName is null, if null, use an empty string instead
        lastName = lastName != null ? lastName : "";
        // Set the text in the TextView
        holder.mTextName.setText(firstName + " " + lastName);

        // Handle description text
        String description = user.getBio();
        if (description != null && description.length() > 40) {
            description = description.substring(0, 40) + "...";
        }
        holder.mTextDes.setText(description);


        holder.mTextAge.setText(String.valueOf(user.calculateAge()));
        holder.mLayoutGenderAge.setBackgroundResource(user.isGender() ? R.drawable.shape_gender_border_blue : R.drawable.shape_gender_border_pink);
        holder.mIconGender.setImageResource(user.isGender() ? R.drawable.ic_male : R.drawable.ic_female);
        if(user.isOnline()){
            holder.onlineStatus.setVisibility(View.VISIBLE);
            holder.offlineStatus.setVisibility(View.GONE);
        } else if(user.getLastActiveMinuteAgo() <= 60){
            holder.onlineStatus.setVisibility(View.GONE);
            holder.offlineStatus.setVisibility(View.VISIBLE);
            holder.offlineStatus.setText(user.getLastActiveMinuteAgo() + "m");
        } else {
            holder.onlineStatus.setVisibility(View.GONE);
            holder.offlineStatus.setVisibility(View.GONE);
        }
        holder.mLayoutCard.setOnClickListener(v -> iClickCardUserListener.onClickUser(user));
    }

    @Override
    public int getItemCount() {
        if (mUsers != null) {
            return mUsers.size();
        }
        return 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String search = constraint.toString();
                if (search.isEmpty()) {
                    mUsers = mUsersOld;
                } else {
                    List<Fimaers> list = new ArrayList<>();
                    for (Fimaers fimaer : mUsersOld) {
                        if(fimaer.getName().toLowerCase().contains(search.toLowerCase())){
                            list.add(fimaer);
                        }
                    }
                    mUsers = list;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mUsers;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null) {
                    mUsers = (List<Fimaers>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }
}
