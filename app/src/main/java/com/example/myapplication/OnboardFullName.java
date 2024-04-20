package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Authentication.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class OnboardFullName extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private TextView skipButton, submitButton;
    private ImageButton cameraButton;
    private CircleImageView profileImageView;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_GALLERY = 102;
    private static final int REQUEST_CAMERA_PERMISSION = 103;
    private Uri selectedImageUri; // Declare Uri variable to store selected image URI
    private FirebaseUser user;

    private TextInputEditText editTextFullName, editTextUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard_full_name);

        skipButton = findViewById(R.id.skipButton);
        submitButton = findViewById(R.id.submit);
        cameraButton = findViewById(R.id.selectImageButton);
        profileImageView = findViewById(R.id.profileImageView);
        editTextFullName = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OnboardFullName.this, BottomNavigator.class);
                startActivity(intent);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show options to take photo or select from gallery
                showImageSourceOptions();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeUserDataInFirestore();
            }
        });
    }

    private void showImageSourceOptions() {
        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Permission is already granted, proceed to show options
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Intent chooserIntent = Intent.createChooser(pickPhotoIntent, "Select Picture");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePictureIntent});
            startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, show image source options
                showImageSourceOptions();
            } else {
                // Camera permission denied, show a message or handle accordingly
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (data != null && data.getData() != null) {
                    // Image selected from the gallery
                    selectedImageUri = data.getData(); // Store the URI
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        profileImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Image captured from the camera
                    assert data != null;
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    profileImageView.setImageBitmap(imageBitmap);
                    // Create a temporary URI for the captured image
                    selectedImageUri = getImageUri(imageBitmap);
                }
            }
        }
    }

    private void storeUserDataInFirestore() {
        if (user != null && selectedImageUri != null) {

            String newFullName = Objects.requireNonNull(editTextFullName.getText()).toString();
            String newDisplayName = Objects.requireNonNull(editTextUsername.getText()).toString();
            String newProfileImageUri = selectedImageUri.toString();
            User userData = User.getInstance();
            userData.setFullName(newFullName);
            userData.setDisplayName(newDisplayName);
            userData.setProfileImageUri(Uri.parse(newProfileImageUri), null);
            userData.onboardUser(newFullName, newDisplayName, newProfileImageUri);
            Intent intent = new Intent(OnboardFullName.this, BottomNavigator.class);
            startActivity(intent);
        } else {
            Toast.makeText(OnboardFullName.this, "Please select a profile photo", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to get URI from Bitmap
    private Uri getImageUri(Bitmap inImage) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "temp_image", null);
        return Uri.parse(path);
    }
}
