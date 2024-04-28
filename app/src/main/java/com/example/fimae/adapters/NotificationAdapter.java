package com.example.fimae.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fimae.models.Follows;

import java.util.List;
import com.example.fimae.R;
public class NotificationAdapter extends ArrayAdapter<Follows> {
    private Context mContext;
    private List<Follows> mFollows;

    public NotificationAdapter(@NonNull Context context, @NonNull List<Follows> follows) {
        super(context, 0, follows);
        mContext = context;
        mFollows = follows;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.message_item, parent, false);
        }

        Follows currentFollow = mFollows.get(position);

        TextView messageTextView = listItem.findViewById(R.id.messageTextView);
        messageTextView.setText(currentFollow.getMessage());

        return listItem;
    }
}
