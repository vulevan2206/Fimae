package com.example.fimae.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fimae.models.Fimaers;
import com.example.fimae.models.Follows;
import com.example.fimae.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowRepository {
    FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private CollectionReference fimaersRef;
    private StorageReference storageReference;
    public CollectionReference followRef;
    String currentUser;
    DatabaseReference postReference;
    private FollowRepository(){
        firestore = FirebaseFirestore.getInstance();
        fimaersRef = firestore.collection("fimaers");
        followRef = firestore.collection("follows");
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getUid();
    };
    private static  FollowRepository followRepository;

    public interface FollowCheckListener {
        void onFollowCheckResult(boolean isFollowed);
    }
    public void followWithDefaultMessage(String uid) {
        follow(uid);
        String defaultMessage = "Xin chào, tôi đã bắt đầu theo dõi bạn!";
        sendMessageToUser(uid, defaultMessage);
    }

    private void sendMessageToUser(String uid, String message) {
        // Tạo một tham chiếu đến nút trong cơ sở dữ liệu Firebase để lưu trữ tin nhắn
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child("messages").child(uid);
        String messageId = messageRef.push().getKey(); // Tạo một ID ngẫu nhiên cho tin nhắn
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        messageMap.put("message", message);

        messageRef.child(messageId).setValue(messageMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("SendMessageToUser", "Tin nhắn đã được gửi thành công.");
                        } else {
                            Log.e("SendMessageToUser", "Lỗi khi gửi tin nhắn: " + task.getException().getMessage());
                        }
                    }
                });
    }
    public Task<List<Follows>> getFollowsWithDefaultMessage(String userId, boolean isUnfollow) {
        TaskCompletionSource<List<Follows>> taskCompletionSource = new TaskCompletionSource<>();

        Query query;
        if (isUnfollow) {
            query = followRef.whereEqualTo("following", userId);
        } else {
            query = followRef.whereEqualTo("follower", userId);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Follows> followsList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Follows follows = document.toObject(Follows.class);
                        // Kiểm tra xem trường message có null không
                        if (follows.getMessage() == null) {
                            // Nếu message là null, điền message mặc định
                            follows.setMessage("Xin chào, tôi đã bắt đầu theo dõi bạn!");
                        }
                        followsList.add(follows);
                    }
                    taskCompletionSource.setResult(followsList);
                })
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    public static FollowRepository getInstance(){
        if(followRepository == null) followRepository = new FollowRepository();
        return followRepository;
    }







    public void unFollowWithDefaultMessage(String uid) {
        unFollow(uid);
        String defaultMessage = "Xin chào, tôi đã ngừng theo dõi bạn!";
        sendUnfollowMessageToUser(uid, defaultMessage);
    }

    private void sendUnfollowMessageToUser(String uid, String message) {
        // Tạo một tham chiếu đến nút trong cơ sở dữ liệu Firebase để lưu trữ tin nhắn
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child("messages").child(uid);
        String messageId = messageRef.push().getKey(); // Tạo một ID ngẫu nhiên cho tin nhắn
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        messageMap.put("message", message);

        messageRef.child(messageId).setValue(messageMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("sendUnfollowMessageToUser", "Tin nhắn đã được gửi thành công.");
                    } else {
                        Log.e("sendUnfollowMessageToUser", "Lỗi khi gửi tin nhắn: " + task.getException().getMessage());
                    }
                });
    }


        // ...

//        public Task<Boolean> isFollowing(String userId) {
//            TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
//            String path = currentUser + "_" + userId;
//            followRef.document(path).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                        @Override
//                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//
//                        }
//                    })
//                    .addOnSuccessListener(documentSnapshot -> {
//                        if (documentSnapshot.exists()) {
//                            taskCompletionSource.setResult(true);
//                        } else {
//                            taskCompletionSource.setResult(false);
//                        }
//                    })
//                    .addOnFailureListener(taskCompletionSource::setException);
//
//            return taskCompletionSource.getTask();
//        }
//    public Task<ArrayList<Fimaers>> getFollowers(String userId){
//            ArrayList<Fimaers> fimaers= new ArrayList<>();
//            TaskCompletionSource<ArrayList<Fimaers>> taskCompletionSource = new TaskCompletionSource<>();
//            String path = currentUser + "_" + userId;
//            followRef.whereEqualTo("follower",userId).get()
//                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                            for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
//                                Follows follows = doc.getDocument().toObject(Follows.class);
//                                fimaersRef.document(follows.getFollowing()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                        fimaers.add( documentSnapshot.toObject(Fimaers.class));
//                                    }
//                                });
//                            }
//                            taskCompletionSource.setResult(fimaers);
//                        }
//                    })
//                    .addOnFailureListener(taskCompletionSource::setException);
//            return taskCompletionSource.getTask();
//    }
    public Task<ArrayList<Fimaers>> getFollowings(String userId) {
        ArrayList<Fimaers> fimaers = new ArrayList<>();
        TaskCompletionSource<ArrayList<Fimaers>> taskCompletionSource = new TaskCompletionSource<>();
        followRef.whereEqualTo("follower", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Task<DocumentSnapshot>> followerTasks = new ArrayList<>();

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        Follows follows = doc.getDocument().toObject(Follows.class);
                        followerTasks.add(fimaersRef.document(follows.getFollowing()).get());
                    }

                    // Wait for all the follower document tasks to complete
                    Tasks.whenAllSuccess(followerTasks)
                            .addOnSuccessListener(followerSnapshots -> {
                                for (Object snapshot :  followerSnapshots) {
                                    fimaers.add(((DocumentSnapshot)snapshot).toObject(Fimaers.class));
                                }
                                taskCompletionSource.setResult(fimaers);
                            })
                            .addOnFailureListener(taskCompletionSource::setException);
                })
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    public Task<ArrayList<Fimaers>> getFollowers(String userId) {
        ArrayList<Fimaers> fimaers = new ArrayList<>();
        TaskCompletionSource<ArrayList<Fimaers>> taskCompletionSource = new TaskCompletionSource<>();
        followRef.whereEqualTo("following", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Task<DocumentSnapshot>> followerTasks = new ArrayList<>();

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        Follows follows = doc.getDocument().toObject(Follows.class);
                        followerTasks.add(fimaersRef.document(follows.getFollower()).get());
                    }

                    // Wait for all the follower document tasks to complete
                    Tasks.whenAllSuccess(followerTasks)
                            .addOnSuccessListener(followerSnapshots -> {
                                for (Object snapshot :  followerSnapshots) {
                                    fimaers.add(((DocumentSnapshot)snapshot).toObject(Fimaers.class));
                                }
                                taskCompletionSource.setResult(fimaers);
                            })
                            .addOnFailureListener(taskCompletionSource::setException);
                })
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    public Task<Boolean> follow(String userId){
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        String path = currentUser + "_" + userId;
        Follows follows = new Follows();
        follows.setFollower(userId);
        follows.setFollowing(currentUser);
        follows.setId(path);

        followRef.document(path).set(follows).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                taskCompletionSource.setResult(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                taskCompletionSource.setResult(false);
            }
        });
        return taskCompletionSource.getTask();
    }
    public Task<Boolean> unFollow(String userId){
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        String path = currentUser + "_" + userId;
        Follows follows = new Follows();
        follows.setFollower(userId);
        follows.setFollowing(currentUser);
        followRef.document(path).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                taskCompletionSource.setResult(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                taskCompletionSource.setResult(false);
            }
        });
        return taskCompletionSource.getTask();
    }
    public Task<Boolean> isFriend(String userId){
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        String doc1 = FirebaseAuth.getInstance().getUid()+"_"+userId;
        String doc2 = userId+"_"+FirebaseAuth.getInstance().getUid();
        Query query = FollowRepository.getInstance().followRef.whereIn(FieldPath.documentId(), Arrays.asList(doc1, doc2));
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.getDocuments().size() == 2){
                    taskCompletionSource.setResult(true);
                }
                else taskCompletionSource.setResult(false);
            }
        });
        return taskCompletionSource.getTask();
    }
}
