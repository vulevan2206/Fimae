package com.example.fimae.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fimae.components.MessageView;
import com.example.fimae.models.Fimaers;
import com.example.fimae.models.Message;
import com.example.fimae.repository.FimaerRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MessageAdapter extends FirestoreAdapter {
    Context context;
    final int SENDER_VIEW_HOLDER = 0;
    final int RECEIVER_VIEW_HOLDER = 1;
    final int SENDER_POST_VIEW_HOLDER = 3;
    final int RECEIVER_POST_VIEW_HOLDER = 4;

    public MessageAdapter(Query query, Context context) {
        super(query);
        this.context = context;
        startListening();
    }

    private ArrayList<Message> messages;

    public Message getMessage(int position) {
        if (messages == null)
            return null;
        return messages.get(position);
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public int getMessageType(int position) {
        Message message = getMessage(position);
        Log.i("Tag", message.getType());
        String x = message.getType();
        if (message.getType().equals(Message.POST_LINK)) {
            if (!Objects.equals(message.getIdSender(), FirebaseAuth.getInstance().getUid())) {
                return RECEIVER_POST_VIEW_HOLDER;
            }
            return SENDER_POST_VIEW_HOLDER;
        }
        return (Objects.equals(message.getIdSender(), FirebaseAuth.getInstance().getUid())) ? SENDER_VIEW_HOLDER : RECEIVER_VIEW_HOLDER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MessageView messageView = new MessageView(context);
        return new IncomingViewholder(messageView);
    }

    HashMap<String, Fimaers> fimaersHashMap = new HashMap<>();

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = getMessage(position);
        MessageView messageView = (MessageView) holder.itemView;
        messageView.setMessage(message);
        Fimaers fimaers = fimaersHashMap.get(message.getIdSender());
        if (fimaers == null) {
            FimaerRepository.getInstance().getFimaerById(message.getIdSender()).addOnSuccessListener(fimaers1 -> {
                fimaersHashMap.put(message.getIdSender(), fimaers1);
                messageView.setFimaers(fimaers1);
            });
        } else {
            messageView.setFimaers(fimaers);
        }

        messageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    showDeleteConfirmationDialog(adapterPosition);
                }
                return true; // Consume the long-click event
            }
        });    }
    public void bind(Message message, RecyclerView.ViewHolder holder) {
        MessageView messageView = (MessageView) holder.itemView;
        messageView.setMessage(message);
        Fimaers fimaers = fimaersHashMap.get(message.getIdSender());
        if (fimaers == null) {
            FimaerRepository.getInstance().getFimaerById(message.getIdSender()).addOnSuccessListener(fimaers1 -> {
                fimaersHashMap.put(message.getIdSender(), fimaers1);
                messageView.setFimaers(fimaers1);
            });
        } else {
            messageView.setFimaers(fimaers);
        }
    }


    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMessage(position))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteMessage(int position) {
        Message message = getMessage(position);
        if (message != null) {
            FirebaseFirestore.getInstance().collection("conversations")
                    .document(message.getConversationID())
                    .collection("messages")
                    .document(message.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Error deleting message", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void OnSuccessQueryListener(ArrayList queryDocumentSnapshots, ArrayList arrayList) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.clear();
        for (Object document : queryDocumentSnapshots) {
            Message message = ((DocumentSnapshot) document).toObject(Message.class);
            messages.add(message);
        }
        notifyDataSetChanged();
    }

    public static class IncomingViewholder extends RecyclerView.ViewHolder {
        MessageView messageView;

        public IncomingViewholder(@NonNull View itemView) {
            super(itemView);
            messageView = (MessageView) itemView;
        }
    }

    @Override
    public int getItemCount() {
        if (messages == null) {
            return 0;
        }
        return messages.size();
    }

    @Override
    public void stopListening() {
        super.stopListening();
        messages.clear();
        fimaersHashMap.clear();
    }
    public void deleteMessage(Message message) {
        if (messages != null) {
            messages.remove(message);
            notifyDataSetChanged();
        }
    }
    public int getPosition(Message message) {
        return messages.indexOf(message);
    }
    public void removeMessage(int position) {
        if (position != -1 && position < messages.size()) {
            messages.remove(position);
            notifyItemRemoved(position); // Đảm bảo RecyclerView cập nhật ngay lập tức
        }
    }

    public void addMessage(int position, String text) {
        Log.d("MessageAdapter", "Adding message at position: " + position + " with text: " + text);
        Message message = new Message();
        message.setContent(text);
        message.setIdSender(FirebaseAuth.getInstance().getUid());

        if (position <= messages.size()) { // Kiểm tra để tránh IndexOutOfBoundsException
            messages.add(position, message);
            notifyItemInserted(position);
            Log.d("MessageAdapter", "Message added and adapter notified.");
        } else {
            Log.e("MessageAdapter", "Attempted to add message at invalid position: " + position);
        }
    }
}
