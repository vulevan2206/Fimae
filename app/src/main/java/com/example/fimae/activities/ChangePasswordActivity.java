package com.example.fimae.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fimae.R;
import com.example.fimae.repository.AuthRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordActivity extends AppCompatActivity {
    EditText currentPassword, newPassword, confirmNewPassword;
    Button btnChangePassword;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    AuthRepository authRepository = AuthRepository.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    CollectionReference fimaeUsersRefer = firestore.collection("fimae-users");
    DatabaseReference usersRef = database.getReference("fimae-users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        currentPassword = findViewById(R.id.edtCurrentPassword);
        newPassword = findViewById(R.id.edtNewPassword);
        confirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePassword();
            }
        });

        // Set back button click listener here
        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void ChangePassword() {
        String currentpass = currentPassword.getText().toString();
        String newpass = newPassword.getText().toString();
        String confirmpass = confirmNewPassword.getText().toString();

        if (currentpass.length() < 6) {
            newPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            newPassword.requestFocus();
            return;
        }
        if (currentpass.equals(newpass)) {
            currentPassword.setError("Mật khẩu mới không được giống mật khẩu hiện tại");
            currentPassword.requestFocus();
            return;
        }
        if (newpass.length() < 6) {
            newPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            newPassword.requestFocus();
            return;
        }
        if (!newpass.equals(confirmpass)) {
            confirmNewPassword.setError("Mật khẩu mới không khớp");
            confirmNewPassword.requestFocus();
            return;
        }


        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Xác thực lại mật khẩu hiện tại
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentpass);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Nếu xác thực thành công, đổi mật khẩu
                                currentUser.updatePassword(newpass)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                                    currentPassword.setText("");
                                                    newPassword.setText("");
                                                    confirmNewPassword.setText("");
                                                    currentPassword.clearFocus();
                                                    newPassword.clearFocus();
                                                    confirmNewPassword.clearFocus();

                                                    // Đổi mật khẩu thành công, làm các công việc khác nếu cần
                                                } else {
                                                    Toast.makeText(ChangePasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Mật khẩu hiện tại không chính xác", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

}


