package com.example.myapplication.editprofile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.EditAccountActivity;
import com.example.myapplication.R;
import com.example.myapplication.Authentication.AuthObserver;
import com.example.myapplication.Authentication.User;
import com.google.android.material.textfield.TextInputEditText;

public class EditPassword extends AppCompatActivity implements AuthObserver {

    private TextView saveButton;
    private TextInputEditText editTextNewPassword, editTextCfmPassword, editTextCurrentPassword;

    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.save);
        editTextNewPassword = findViewById(R.id.editTextPassword);
        editTextCfmPassword = findViewById(R.id.editTextCfmPassword);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditPassword.this, EditAccountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void updatePassword() {
        String currentPassword = editTextCurrentPassword.getText() != null ? editTextCurrentPassword.getText().toString() : "";
        String newPassword = editTextNewPassword.getText() != null ? editTextNewPassword.getText().toString() : "";
        String confirmPassword = editTextCfmPassword.getText() != null ? editTextCfmPassword.getText().toString() : "";

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty() || currentPassword.isEmpty()) {
            Toast.makeText(EditPassword.this, "Please fill up all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = User.getInstance();
        currentUser.updatePassword(currentPassword, newPassword, this);
    }

    @Override
    public void onAuthStatusChanged(boolean isSuccess, String message) {
        if (isSuccess) {
            Toast.makeText(EditPassword.this, message, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(EditPassword.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
