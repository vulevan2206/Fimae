package com.example.fimae.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fimae.R;
import com.example.fimae.activities.OnChatActivity;
import com.example.fimae.activities.SearchUserActivity;
import com.example.fimae.adapters.ConversationAdapter;
import com.example.fimae.models.Conversation;
import com.example.fimae.models.Fimaers;
import com.example.fimae.repository.ChatRepository;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;

public class ChatFragment extends Fragment {
    ConversationAdapter adapter;
    RecyclerView recyclerView;
    private LinearLayout searchbar;
    HashMap<String, Date> readLastMessageAt = new HashMap<>();

    void initListener() {
        Query query = ChatRepository.getDefaultChatInstance().getConversationQuery();
        adapter = new ConversationAdapter(query, new ConversationAdapter.IClickConversationListener() {
            @Override
            public void onClickConversation(Conversation conversation, Fimaers fimaers) {
                Intent intent = new Intent(getContext(), OnChatActivity.class);
                intent.putExtra("conversationID", conversation.getId());
                intent.putExtra("fimaer", fimaers);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        // Lắng nghe sự kiện của query để lấy dữ liệu từ Firestore và cập nhật HashMap
        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Xử lý khi có lỗi xảy ra
                return;
            }

            for (DocumentSnapshot document : value.getDocuments()) {
                Conversation conversation = document.toObject(Conversation.class);
                if (conversation != null) {
                    // Lấy DocumentReference của tin nhắn cuối cùng
                    DocumentReference lastMessageRef = conversation.getLastMessage();
                    // Nếu lastMessageRef không null
                    if (lastMessageRef != null) {
                        // Truy cập dữ liệu của tin nhắn cuối cùng từ Firestore
                        lastMessageRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot messageSnapshot = task.getResult();
                                if (messageSnapshot != null && messageSnapshot.exists()) {
                                    // Lấy thời gian của tin nhắn cuối cùng từ dữ liệu Firestore
                                    Date lastMessageTime = messageSnapshot.getDate("time"); // Thay "time" bằng tên trường chứa thời gian trong tài liệu của bạn
                                    // Thêm thời gian của tin nhắn cuối cùng vào HashMap
                                    readLastMessageAt.put(conversation.getId(), lastMessageTime);
                                    adapter.notifyDataSetChanged(); // Cập nhật lại RecyclerView sau khi cập nhật dữ liệu thời gian
                                }
                            } else {
                                // Xử lý khi có lỗi xảy ra khi truy cập dữ liệu của tin nhắn cuối cùng
                            }
                        });
                    }
                }
            }
        });

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.list_user);
        searchbar = view.findViewById(R.id.search_bar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        searchbar.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchUserActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter == null) {
            initListener();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
