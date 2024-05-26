
        package com.example.fimae.fragments;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static androidx.databinding.DataBindingUtil.setContentView;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fimae.R;
import com.example.fimae.activities.HomeActivity;
import com.example.fimae.activities.ProfileActivity;
import com.example.fimae.activities.SearchUserActivity;
import com.example.fimae.adapters.BottomSheetItemAdapter;
import com.example.fimae.adapters.UserHomeViewAdapter;
import com.example.fimae.models.BottomSheetItem;
import com.example.fimae.models.Fimaers;
import com.example.fimae.models.GenderMatch;
import com.example.fimae.repository.ConnectRepo;
import com.example.fimae.repository.FimaerRepository;
import com.example.fimae.repository.FollowRepository;
import com.example.fimae.repository.PostRepository;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {
    private int followingUserCount = 0;
    private int notFollowingUserCount = 0;
    private FirebaseFirestore firestore;
    private CollectionReference fimaeUserRef;
    private ImageView imgSearch;
    private View mView;
    private RecyclerView mRcvUsers;
    private RecyclerView mRcvMe;
    private HomeActivity homeActivity;
    private UserHomeViewAdapter userAdapter;
    private ArrayList<Fimaers> mUsers;
    private ArrayList<Fimaers> followingUsers;
    private ArrayList<Fimaers> notFollowingUsers;
    private LinearLayout mBtnChat;
    private LinearLayout mBtnCallVoice;
    private LinearLayout mBtnCallVideo;
    private ImageButton mBtnNoti;
    private ImageButton mBtnSetting;
    // Setting bottom sheet
    private RangeSlider mRangeAges;
    private TextView mTvRangeAges;
    // btn male
    private LinearLayout mLayoutBtnMale;
    private ImageView mImgMale;
    private TextView mTvMale;
    // btn female
    private LinearLayout mLayoutBtnFemale;
    private ImageView mImgFemale;
    private TextView mTvFemale;
    // btn both
    private LinearLayout mLayoutBtnBoth;
    private ImageView mImgBoth;
    private TextView mTvBoth;
    // btn Hoan thanh
    private AppCompatButton mBtnFinish;
    private Button btn_setting;
    private GenderMatch genderMatch;

    float xDown = 0, yDown = 0;
    private FimaeBottomSheet fimaeBottomSheet;
    List<BottomSheetItem> sheetItems;

    private ListenerRegistration mlisten;

    static public boolean isShowFloatingWaiting = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        createBottomSheetItem();




        // recycleView: List users
        mRcvUsers = mView.findViewById(R.id.recycler_users);
        mRcvMe=mView.findViewById(R.id.recycle_me);
        homeActivity = (HomeActivity) getActivity();
        mUsers = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(homeActivity);
        mRcvUsers.setLayoutManager(linearLayoutManager);

        notFollowingUsers = new ArrayList<>();
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(homeActivity);
        followingUsers = new ArrayList<>();
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(homeActivity);
        userAdapter = new UserHomeViewAdapter(this.getContext());

        userAdapter.setData(mUsers, new UserHomeViewAdapter.IClickCardUserListener() {
            @Override
            public void onClickUser(Fimaers user) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra("uid", user.getUid());
                startActivity(intent);
            }
        });
        mRcvUsers.setAdapter(userAdapter);
        mRcvMe.setLayoutManager(new LinearLayoutManager(getContext()));
        firestore = FirebaseFirestore.getInstance();
        GetMyUsers();
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mlisten != null)
            mlisten.remove();
    }

    private void createBottomSheetItem(){
        sheetItems = new ArrayList<BottomSheetItem>(){
            {
                add(new BottomSheetItem(R.drawable.ic_chat_dots, "Tới cuộc trò chuyện"));
                add(new BottomSheetItem(R.drawable.ic_user_block, "Hủy theo dõi"));
            }
        };
    }
    private void createBottomSheet(Fimaers user){
        fimaeBottomSheet = null;
        fimaeBottomSheet = new FimaeBottomSheet(sheetItems, new BottomSheetItemAdapter.IClickBottomSheetItemListener() {
            @Override
            public void onClick(BottomSheetItem bottomSheetItem) {
                if(bottomSheetItem.getTitle().equals("Tới cuộc trò chuyện")){
                    PostRepository.getInstance().goToChatWithUser(user.getUid(), HomeFragment.this.getContext());
                }
                else if(bottomSheetItem.getTitle().equals("Hủy theo dõi")){
                    FollowRepository.getInstance().unFollow(user.getUid());
                }
            }
        });
        fimaeBottomSheet.show(getParentFragmentManager(), "GoChat");
    }



    ListenerRegistration listenerRegistrationUser;

    private void GetMyUsers() {
        ArrayList<Fimaers> meUsers = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        fimaeUserRef = firestore.collection("fimaers");
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fimaeUserRef.document(localUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Fimaers currentUser = documentSnapshot.toObject(Fimaers.class);
                        if (currentUser != null) {
                            meUsers.add(currentUser);
                            UserHomeViewAdapter meUserAdapter = new UserHomeViewAdapter(getContext());
                            meUserAdapter.setData(meUsers, new UserHomeViewAdapter.IClickCardUserListener() {
                                @Override
                                public void onClickUser(Fimaers user) {
                                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                                    intent.putExtra("uid", user.getUid());
                                    startActivity(intent);
                                }
                            });
                            mRcvMe.setAdapter(meUserAdapter);
                            meUserAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Log.d("Loi", "Error getting current user document: ", e);
                });
    }



    // ===== setting user =================================================================================
    private void settingUser() {
        View dialogSetting = getLayoutInflater().inflate(R.layout.bottom_sheet_setting, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this.getContext());
        bottomSheetDialog.setContentView(dialogSetting);
        bottomSheetDialog.show();

// set match components
        mRangeAges = bottomSheetDialog.findViewById(R.id.range_slider_age);
        mTvRangeAges = bottomSheetDialog.findViewById(R.id.tv_st_range_ages);
        // btn male
        mLayoutBtnMale = bottomSheetDialog.findViewById(R.id.btn_st_male);
        mImgMale = bottomSheetDialog.findViewById(R.id.img_st_male);
        mTvMale = bottomSheetDialog.findViewById(R.id.tv_st_male);
        // btn female
        mLayoutBtnFemale = bottomSheetDialog.findViewById(R.id.btn_st_female);
        mImgFemale = bottomSheetDialog.findViewById(R.id.img_st_female);
        mTvFemale = bottomSheetDialog.findViewById(R.id.tv_st_female);
        // btn both
        mLayoutBtnBoth = bottomSheetDialog.findViewById(R.id.btn_st_both);
        mImgBoth = bottomSheetDialog.findViewById(R.id.img_st_both);
        mTvBoth = bottomSheetDialog.findViewById(R.id.tv_st_both);

        mBtnFinish = bottomSheetDialog.findViewById(R.id.btn_st_finish);
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // thay doi range slide
        Fimaers userLocal = ConnectRepo.getInstance().getUserLocal(); // Kiểm tra null ở đây
        if (userLocal != null) {
            genderMatch = userLocal.getGenderMatch();
            if (genderMatch != null) {
                toggleGenderButtons(genderMatch);
            }
            float min = userLocal.getMinAgeMatch();
            float max = userLocal.getMaxAgeMatch();
            if (12 <= min && min <= 40 && 12 <= max && max <= 40) {
                String rangeAges = Math.round(min) + "-" + Math.round(max);
                mTvRangeAges.setText(rangeAges);
                mRangeAges.setValues(min, max);
            }
        }
        else {
            Toast.makeText(getContext(), "Null userclocal", Toast.LENGTH_SHORT).show();
        }

        mRangeAges.addOnChangeListener((slider, value, fromUser) -> {
            String rangeAges = Math.round(slider.getValues().get(0)) + "-" + Math.round(slider.getValues().get(1));
            mTvRangeAges.setText(rangeAges);
        });

        mLayoutBtnMale.setOnClickListener(v -> {
            // click btn male
            genderMatch = GenderMatch.male;
            toggleGenderButtons(genderMatch);
        });
        mLayoutBtnFemale.setOnClickListener(v -> {
            // click btn female
            genderMatch = GenderMatch.female;
            toggleGenderButtons(genderMatch);
        });
        mLayoutBtnBoth.setOnClickListener(v -> {
            // click btn both
            genderMatch = GenderMatch.both;
            toggleGenderButtons(genderMatch);
        });

        mBtnFinish.setOnClickListener(v -> {
            if (genderMatch == null) {
                Toast.makeText(getContext(), "Vui lòng chọn giới tính!", Toast.LENGTH_SHORT).show();
                return;
            }

            Fimaers userLocalFinish = ConnectRepo.getInstance().getUserLocal(); // Kiểm tra null ở đây

            if (userLocalFinish != null) {
                // finish and save to user
                userLocalFinish.setMinAgeMatch(Math.round(mRangeAges.getValues().get(0)));
                userLocalFinish.setMaxAgeMatch(Math.round(mRangeAges.getValues().get(1)));

                fimaeUserRef.document(userLocalFinish.getUid()).set(userLocalFinish)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "User has been updated..", Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        getUsersByFilter(genderMatch, Math.round(mRangeAges.getValues().get(0)), Math.round(mRangeAges.getValues().get(1)));
                                        userAdapter.notifyDataSetChanged();
                                    }
                                }, 500); // 1000 milliseconds delay
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Fail to update the data..", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Handle the case where userLocalFinish is null
                Toast.makeText(getContext(), "UserLocalFinish is null", Toast.LENGTH_SHORT).show();

            }

            bottomSheetDialog.dismiss();
        });
    }



    private void toggleGenderButtons(GenderMatch genderMatch) {
        if(genderMatch == GenderMatch.male) {
            mImgMale.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary_2), PorterDuff.Mode.SRC_IN);
            mTvMale.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_2));

            mImgFemale.setColorFilter(ContextCompat.getColor(getContext(), R.color.text_tertiary), android.graphics.PorterDuff.Mode.SRC_IN);
            mTvFemale.setTextColor(ContextCompat.getColor(getContext(), R.color.background_button_dark_1_startColor));

            mImgBoth.setColorFilter(ContextCompat.getColor(getContext(), R.color.text_tertiary), android.graphics.PorterDuff.Mode.SRC_IN);
            mTvBoth.setTextColor(ContextCompat.getColor(getContext(), R.color.background_button_dark_1_startColor));
        } else if(genderMatch == GenderMatch.female) {
            mImgMale.setColorFilter(ContextCompat.getColor(getContext(), R.color.text_tertiary), android.graphics.PorterDuff.Mode.SRC_IN);
            mTvMale.setTextColor(ContextCompat.getColor(getContext(), R.color.background_button_dark_1_startColor));

            mImgFemale.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary_2), android.graphics.PorterDuff.Mode.SRC_IN);
            mTvFemale.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_2));

            mImgBoth.setColorFilter(ContextCompat.getColor(getContext(), R.color.text_tertiary), android.graphics.PorterDuff.Mode.SRC_IN);
            mTvBoth.setTextColor(ContextCompat.getColor(getContext(), R.color.background_button_dark_1_startColor));
        } else if(genderMatch == GenderMatch.both) {
            mImgMale.setColorFilter(ContextCompat.getColor(getContext(), R.color.text_tertiary), android.graphics.PorterDuff.Mode.SRC_IN);
            mTvMale.setTextColor(ContextCompat.getColor(getContext(), R.color.background_button_dark_1_startColor));

            mImgFemale.setColorFilter(ContextCompat.getColor(getContext(), R.color.text_tertiary), android.graphics.PorterDuff.Mode.SRC_IN);
            mTvFemale.setTextColor(ContextCompat.getColor(getContext(), R.color.background_button_dark_1_startColor));

            mImgBoth.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary_2), android.graphics.PorterDuff.Mode.SRC_IN);
            mTvBoth.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_2));
        }
    }
    private void getUsersByFilter(GenderMatch genderMatch, int minAge, int maxAge) {
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Tạo một truy vấn Firebase Firestore để lấy người dùng theo điều kiện lọc
        Query query = fimaeUserRef.whereNotEqualTo("uid", localUid); // Loại bỏ người dùng hiện tại
        if (genderMatch != null) {
            query = query.whereEqualTo("gender", genderMatch.toString()); // Lọc theo giới tính
        }

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    mUsers.clear(); // Xóa danh sách người dùng hiện tại
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Fimaers user = document.toObject(Fimaers.class);
                        // Kiểm tra tuổi của người dùng
                        int userAge = Integer.parseInt(user.getAge());
                        if (userAge >= minAge && userAge <= maxAge) {
                            mUsers.add(user); // Thêm người dùng phù hợp vào danh sách
                        }
                    }
                    // Sắp xếp danh sách người dùng theo thời gian tạo
                    Collections.sort(mUsers, new Comparator<Fimaers>() {
                        @Override
                        public int compare(Fimaers user1, Fimaers user2) {
                            return Long.compare(user2.getTimeCreated().getDate(), user1.getTimeCreated().getDate());
                        }
                    });
                    userAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView với danh sách mới
                } else {
                    Log.d("Loi", "Error getting documents: ", task.getException());
                }
            }
        });
    }
    private void getUsersFollowing() {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Xóa danh sách người dùng hiện tại trước khi thêm mới
        mUsers.clear();

        // Bước 1: Lấy danh sách UID của người dùng đang được theo dõi bởi người dùng hiện tại
        FirebaseFirestore.getInstance()
                .collection("follows")
                .whereEqualTo("following", currentUserUid)
                .get()
                .addOnSuccessListener(followingQueryDocumentSnapshots -> {
                    List<String> followingIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : followingQueryDocumentSnapshots) {
                        String followingId = document.getString("follower"); // Lấy UID của người dùng đang theo dõi
                        if (followingId != null) {
                            followingIds.add(followingId);
                        }
                    }

// Bước 2: Lấy thông tin chi tiết của người dùng đang được theo dõi
                    for (String id : followingIds) {
                        FirebaseFirestore.getInstance()
                                .collection("fimaers")
                                .document(id)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    Fimaers user = documentSnapshot.toObject(Fimaers.class);
                                    if (user != null) {
                                        mUsers.add(user);
                                        userAdapter.notifyDataSetChanged(); // Cập nhật adapter mỗi lần có người dùng mới được thêm vào danh sách
                                        countUsersInTabsFollowing();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Xử lý lỗi
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi
                });
    }

    private void getUsersNotFollowing() {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Lấy danh sách người dùng không nằm trong danh sách người dùng đang được theo dõi
        mUsers.clear(); // Xóa danh sách người dùng hiện tại trước khi thêm mới

        // Tạo một truy vấn Firestore để lấy tất cả người dùng
        FirebaseFirestore.getInstance()
                .collection("fimaers")
                .get()
                .addOnSuccessListener(allUsersQueryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : allUsersQueryDocumentSnapshots) {
                        Fimaers user = document.toObject(Fimaers.class);
                        // Lọc ra những người dùng không nằm trong danh sách người dùng đang được theo dõi và không phải là người dùng hiện tại
                        if (!user.getUid().equals(currentUserUid)) {
                            FirebaseFirestore.getInstance()
                                    .collection("follows")
                                    .whereEqualTo("following", currentUserUid)
                                    .whereEqualTo("follower", user.getUid())
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (queryDocumentSnapshots.isEmpty()) {
                                            // Nếu danh sách người theo dõi không chứa người dùng hiện tại, thêm người dùng vào danh sách chưa được theo dõi
                                            mUsers.add(user);
                                            userAdapter.notifyDataSetChanged(); // Cập nhật adapter
                                            countUsersInTabsNotFollowing();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Xử lý lỗi
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgSearch = view.findViewById(R.id.imgSearch);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển đến trang ActivitySearchUser
                Intent intent = new Intent(getContext(), SearchUserActivity.class);
                startActivity(intent);
            }
        });

        // Gọi phương thức để lấy danh sách người dùng khi fragment được tạo ra
        getUsersFollowing(); // Mặc định hiển thị danh sách người dùng đang theo dõi
        TabLayout tabLayout = view.findViewById(R.id.tab_home);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Xác định tab được chọn
                switch (tab.getPosition()) {
                    case 0:
                        // Hiển thị danh sách người dùng đang theo dõi khi nhấn vào tab "Đang theo dõi"
                        getUsersFollowing();
                        tab.setText("Đang theo dõi(" + followingUserCount + ")");
                        break;
                    case 1:
                        // Hiển thị danh sách người dùng chưa được theo dõi khi nhấn vào tab "Đề xuất"
                        getUsersNotFollowing();
                        tab.setText("Đề xuất(" + notFollowingUserCount + ")");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
    }
    private void countUsersInTabsFollowing() {
        followingUserCount = mUsers.size();

    }
    private void countUsersInTabsNotFollowing() {

        notFollowingUserCount = mUsers.size();
    }



}