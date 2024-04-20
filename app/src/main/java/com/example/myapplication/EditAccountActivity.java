package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.Authentication.User;
import com.example.myapplication.editprofile.EditEmail;
import com.example.myapplication.editprofile.EditHandle;
import com.example.myapplication.editprofile.EditName;
import com.example.myapplication.editprofile.EditPassword;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditAccountActivity extends AppCompatActivity {
    private ImageView navigateBack;
    private TextView usernameTextView;
    private User currentUser;
    private ImageView profilePhotoImageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private RelativeLayout profilePictureButton, nameButton, handleButton, emailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        usernameTextView = findViewById(R.id.userName);
        profilePhotoImageView = findViewById(R.id.profilePicture);
        nameButton = findViewById(R.id.nameButton);
        handleButton = findViewById(R.id.usernameLayout);
        emailButton = findViewById(R.id.emailLayout);
        profilePictureButton = findViewById(R.id.profilePictureLayout);
        navigateBack = findViewById(R.id.imageView4);

        currentUser = User.getInstance();
        String userName = currentUser.getDisplayName();
        usernameTextView.setText(userName != null ? userName: "");
        Uri profileImageUri = currentUser.getProfileImageUri();
        profilePhotoImageView.setImageURI(profileImageUri);


        nameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditAccountActivity.this, EditName.class);
                startActivity(intent);
                finish();
            }
        });

        handleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditAccountActivity.this, EditHandle.class);
                startActivity(intent);
                finish();
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditAccountActivity.this, EditEmail.class);
                startActivity(intent);
                finish();
            }
        });

        profilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSourceOptions();
            }
        });

        navigateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showImageSourceOptions() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent chooserIntent = Intent.createChooser(pickPhotoIntent, "Select Picture");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePictureIntent});
        startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImageUri = data.getData();
                Glide.with(this).load(selectedImageUri).into(profilePhotoImageView);
                currentUser.setProfileImageUri(selectedImageUri, new User.OnProfileImageUpdateListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(EditAccountActivity.this, "Profile image updated successfully. Looking good!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(EditAccountActivity.this, "Failed to update profile image: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }



}

