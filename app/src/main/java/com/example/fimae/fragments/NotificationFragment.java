package com.example.fimae.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.fimae.adapters.NotificationAdapter;
import com.example.fimae.models.Follows;
import com.example.fimae.repository.FollowRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import com.example.fimae.R;
import com.example.fimae.adapters.NotificationAdapter;
import com.example.fimae.models.Follows;
import com.google.firebase.auth.FirebaseUser;

public class NotificationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        // Check if user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Retrieve follows asynchronously for both follow and unfollow cases
            FollowRepository.getInstance().getFollowsWithDefaultMessage(uid, false)
                    .addOnSuccessListener(follows -> {
                        // Create adapter and set it to ListView
                        NotificationAdapter adapter = new NotificationAdapter(getContext(), follows);
                        ListView listView = rootView.findViewById(R.id.listViewMessages);
                        listView.setAdapter(adapter);
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Log.e("NotificationFragment", "Failed to retrieve follows: " + e.getMessage());
                    });
        } else {
            // User not authenticated, handle accordingly
            Log.e("NotificationFragment", "User not authenticated");
        }

        return rootView;
    }

}
