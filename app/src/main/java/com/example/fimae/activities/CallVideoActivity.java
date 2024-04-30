package com.example.fimae.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fimae.R;
import com.example.fimae.models.Report;
import com.example.fimae.repository.ChatRepository;
import com.example.fimae.repository.ConnectRepo;
import com.example.fimae.service.CallService;
import com.example.fimae.service.TimerService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stringee.call.StringeeCall2;
import com.stringee.common.StringeeAudioManager;
import com.stringee.listener.StatusListener;
import com.stringee.video.StringeeVideoTrack;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallVideoActivity extends AppCompatActivity {

    private int TIME_CALL = 5 * 60;

    private FrameLayout frmTextDes;

    private TextView tvStatus;
    private View vIncoming;
    private View vOption;
    private FrameLayout vLocal;
    private FrameLayout vRemote;
    private ImageButton btnSpeaker;
    private ImageButton btnMute;
    private ImageButton btnVideo;
    private ImageButton btnSwitch;
    private ImageButton btnAnswer;
    private ImageButton btnReject;
    private ImageButton btnEnd;

    private StringeeCall2 call;

    private boolean isInComingCall = false;
    private boolean isInComingCallVideo = false;

    private String to;
    private String callId;

    private StringeeCall2.SignalingState mSignalingState;
    private StringeeCall2.MediaState mMediaState;

    // audio
    private StringeeAudioManager audioManager;

    // check trang thai speaker and mic
    private boolean isSpeaker = false;
    private boolean isMicOn = true;
    private boolean isVideoOn = true;

    // like
    private boolean isLiked = false;

    // Appbar
    private ImageButton btnClose;
    private ImageButton btnReport;
    private LinearLayout layoutTimer;
    private TimerService timerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_video);

        tvStatus = findViewById(R.id.tv_status_vid);
        vIncoming = findViewById(R.id.v_incoming_vid);
        vLocal = findViewById(R.id.v_local);
        vRemote = findViewById(R.id.v_remote);
        vOption = findViewById(R.id.v_option_vid);
        btnAnswer = findViewById(R.id.btn_answer_vid);
        btnSpeaker = findViewById(R.id.btn_speaker_vid);
        btnMute = findViewById(R.id.btn_mute_vid);
        btnReject = findViewById(R.id.btn_reject_vid);
        btnVideo = findViewById(R.id.btn_video);
        btnSwitch = findViewById(R.id.btn_switch);
        frmTextDes = findViewById(R.id.frame_text_des);

        btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(() -> {
                    if(audioManager != null) {
                        audioManager.setSpeakerphoneOn(!isSpeaker);
                        isSpeaker = !isSpeaker;
                        btnSpeaker.setBackgroundResource(isSpeaker? R.drawable.background_btn_speaker_on : R.drawable.background_btn_speaker_off);
                    }
                });
            }
        });
        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(() -> {
                    if(call != null){
                        call.mute(isMicOn);
                        isMicOn = !isMicOn;
                        btnMute.setBackgroundResource(isMicOn? R.drawable.background_btn_mic_on : R.drawable.background_btn_mic_off);
                    }
                });
            }
        });
        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(() -> {
                    if(call != null){
                        call.answer(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                        vIncoming.setVisibility(View.GONE);
                        vOption.setVisibility(View.VISIBLE);
                        btnEnd.setVisibility(View.VISIBLE);
                        frmTextDes.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(() -> {
                    if(call != null){
                        call.reject(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                        onFinish();
                    }
                });
            }
        });

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(call != null){
                    call.switchCamera(new StatusListener() {
                        @Override
                        public void onSuccess() {

                        }
                    });
                }
            }
        });
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call.enableVideo(!isVideoOn);
                isVideoOn = !isVideoOn;
                btnVideo.setBackgroundResource(isVideoOn? R.drawable.background_btn_videocam_on : R.drawable.background_btn_videocam_off);
            }
        });

        if(getIntent() != null){
            isInComingCallVideo = getIntent().getBooleanExtra("isIncomingCallVideo", false);
            to = getIntent().getStringExtra("to");
            // duoc goi
            callId = getIntent().getStringExtra("callId");
        }

        // kiem tra dang goi den
        vIncoming.setVisibility(isInComingCallVideo? View.VISIBLE : View.GONE);
        vOption.setVisibility(isInComingCallVideo? View.GONE: View.VISIBLE);
        frmTextDes.setVisibility(isInComingCallVideo? View.GONE: View.VISIBLE);

        // list permission
        List<String> listPermission = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // add permisson
            listPermission.add(Manifest.permission.RECORD_AUDIO);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // add permisson
            listPermission.add(Manifest.permission.CAMERA);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // add permisson
                listPermission.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }
        if(listPermission.size() > 0){
            String[] permissions = new String[listPermission.size()];
            for(int i = 0; i < listPermission.size(); i++){
                permissions[i] = listPermission.get(i);
            }
            ActivityCompat.requestPermissions(this, permissions, 0);
            return;
        }

        initCall();

        // appbar ==================================================================
        btnClose = findViewById(R.id.btn_close_appbar);
        btnReport = findViewById(R.id.btn_report_appbar);
        btnClose.setBackgroundResource(R.drawable.ic_logout);

        btnClose.setOnClickListener(v -> {
            // cup may
            timerService.onDestroy();
            onEndCall();
        });


        // timer ==================================================================
        layoutTimer = findViewById(R.id.layout_timer);

        timerService = new TimerService(
                TIME_CALL,
                findViewById(R.id.pbTimer),
                findViewById(R.id.tv_time_connect),
                new TimerService.IOnTimeUp() {
                    @Override
                    public void onTimeUp() {
                        if(!isLiked) {
                            // neu chua like thi dung khi het thoi gian
                            onEndCall();
                            timerService.onDestroy();
                        }
                        else {
                            // neu like roi thi an di
                            layoutTimer.setVisibility(View.GONE);
                        }
                    }
                }
        );
        timerService.setTimeInit();
        timerService.startTimerSetUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerService.onDestroy();
    }

    private void onLiked() {
        // doi background button call
        // an di frame_text_like
        // doi text tv_des_call
        // doi bien like
        isLiked = true;
        frmTextDes.setVisibility(View.GONE);
        btnEnd.setBackgroundResource(R.drawable.background_btn_call);
        if(ConnectRepo.getInstance().getUserRemote() != null){
            ChatRepository.getDefaultChatInstance().getOrCreateFriendConversation(ConnectRepo.getInstance().getUserRemote().getUid());
        }
    }
    // call =======================================================================

    private void onEndCall(){
        if(call != null){
            call.hangup(new StatusListener(){
                @Override
                public void onSuccess() {

                }
            });
            onFinish();
        }
    }

    private void onFinish() {
        audioManager.stop();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //finish();
    }

    // lay token de thuc hien cuoc goi
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // lay quyen audio
        boolean isGranted = false;
        if(grantResults.length > 0){
            for(int grantResult : grantResults){
                // check trang thai quyen nguoi dung cho phep
                if(grantResult != PackageManager.PERMISSION_GRANTED){
                    isGranted = false;
                    break;
                }else{
                    isGranted = true;
                }
            }
        }
        // check nguoi dung cap quyen chua
        if(requestCode == 0){
            if(!isGranted){
                onFinish();
            } else {
                initCall();
            }
        }
    }

    private void initCall(){
        if(isInComingCallVideo){
            // cuoc goi den
            call = CallService.getInstance().call2Map.get(callId);
            if( call == null){
                onFinish();
                return;
            }
        }else{
            // tao cuoc goi moi
            call = new StringeeCall2(CallService.getInstance().client, CallService.getInstance().client.getUserId(), to);
            call.setVideoCall(true);
            call.setCustom(CallService.NORMAL);
        }

        // theo doi trang thai cuoc goi
        call.setCallListener(new StringeeCall2.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall2 stringeeCall2, StringeeCall2.SignalingState signalingState, String s, int i, String s1) {
                // trang thai dieu huong cuoc goi
                // khi nao bat dau, ket thuc
                runOnUiThread(()->{
                    mSignalingState = signalingState;
                    switch (signalingState) {
                        case CALLING:
                            tvStatus.setText("Đang gọi ne");
                            break;
                        case RINGING:
                            tvStatus.setText("Đang đổ chuông");
                            break;
                        case ANSWERED:
                            tvStatus.setText("Đang trả lời");
                            // cuoc goi bat dau
                            if(mMediaState == StringeeCall2.MediaState.CONNECTED){
                                tvStatus.setText("");
                            }
                            break;
                        case BUSY:
                            tvStatus.setText("Máy bận");
                            onFinish();
                            break;
                        case ENDED:
                            tvStatus.setText("Kết thúc");
                            onFinish();
                            break;
                    }
                });
            }

            @Override
            public void onError(StringeeCall2 stringeeCall2, int i, String s) {
                // cuoc goi bi loi
                runOnUiThread(()->{
                    tvStatus.setText("Lỗi đường truyền");
                    onFinish();
                });
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall2, StringeeCall2.SignalingState signalingState, String s) {

            }

            @Override
            public void onMediaStateChange(StringeeCall2 stringeeCall2, StringeeCall2.MediaState mediaState) {
                // khi nao co media connected
                runOnUiThread(()->{
                    mMediaState = mediaState;
                    if(mediaState == StringeeCall2.MediaState.CONNECTED){
                        if(mSignalingState == StringeeCall2.SignalingState.ANSWERED){
                            tvStatus.setText("");
                        }
                    }else{
                        // mat ket noi
                        tvStatus.setText("Đang kết nối lại");
                    }
                });
            }

            @Override
            public void onLocalStream(StringeeCall2 stringeeCall2) {
                runOnUiThread(() -> {
                    vLocal.removeAllViews();
                    vLocal.addView(stringeeCall2.getLocalView());
                    stringeeCall2.renderLocalView(true);
                });
            }

            @Override
            public void onRemoteStream(StringeeCall2 stringeeCall2) {
                runOnUiThread(() -> {
                    vRemote.removeAllViews();
                    vRemote.addView(stringeeCall2.getRemoteView());
                    stringeeCall2.renderRemoteView(false);
                });
            }

            @Override
            public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onCallInfo(StringeeCall2 stringeeCall2, JSONObject jsonObject) {

            }

            @Override
            public void onTrackMediaStateChange(String s, StringeeVideoTrack.MediaType mediaType, boolean b) {

            }
        });

        // manage audio
        audioManager = new StringeeAudioManager(this);
        audioManager.start((audioDevice, set) -> {
            // start audio
        });

        audioManager.setSpeakerphoneOn(true);
        // khoi tao cuoc goi
        if(isInComingCallVideo){
            // do chuong goi
            call.ringing(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        }else{
            call.makeCall(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        }
    }
}