package com.example.myapplication;

import androidx.annotation.NonNull;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Authentication.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class tes_profile extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_CODE = 101;
    private User user;
    private CircleImageView profileImageView;
    private TextView displayNameTextView;
    private FirebaseFirestore firestore;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tes_profile);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        user = User.getInstance();
        profileImageView = findViewById(R.id.profile_pic);
        displayNameTextView = findViewById(R.id.display_name);
        Button takePhotoButton = findViewById(R.id.take_photo_button);

        // Set click listener for the take photo button
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check camera permission
                if (ContextCompat.checkSelfPermission(tes_profile.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(tes_profile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start camera intent
                    openCamera();
                } else {
                    // Request camera and storage permissions
                    ActivityCompat.requestPermissions(tes_profile.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
                }
            }
        });

        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = firestore.collection("users").document(userId);

            // Fetch user data from Firestore
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get profile picture URL and display name
                        String profilePicUrl = document.getString("photo_url");
                        String displayName = document.getString("display_name");

                        // Load profile picture using Picasso with error handling
                        Picasso.get().load(profilePicUrl).into(profileImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                // Image loaded successfully, do nothing
                            }

                            @Override
                            public void onError(Exception e) {
                                // Show error message in a Toast
                                Toast.makeText(getApplicationContext(), "Error loading profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Set display name
                        displayNameTextView.setText(displayName);
                    }
                }
            });
        }
    }

    // Method to start camera intent
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // Photo captured successfully
            Bundle extras = data.getExtras();
            if (extras != null) {
                // Get the captured photo
                Bitmap photoBitmap = (Bitmap) extras.get("data");
                if (photoBitmap != null) {
                    // Convert bitmap to URI
                    photoUri = getImageUri(photoBitmap);
                    if (photoUri != null) {
                        // Update user profile photo URL in Firestore
                        updateUserProfilePhoto(photoUri.toString());
                    }
                }
            }
        }
    }

    // Method to convert bitmap to URI
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    // Method to update user profile photo URL in Firestore
    private void updateUserProfilePhoto(String photoUrl) {
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = firestore.collection("users").document(userId);

            // Create a map to update user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("photo_url", photoUrl);

            // Update user data in Firestore
            userRef.set(userData, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Profile photo URL updated successfully
                            // Display the updated photo using Picasso
                            Picasso.get().load(photoUrl).into(profileImageView);
                        } else {
                            // Failed to update profile photo URL
                            Toast.makeText(getApplicationContext(), "Failed to update profile photo", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Camera and storage permissions granted, start camera intent
                openCamera();
            } else {
                // Permission denied, display error message
                Toast.makeText(this, "Camera and storage permissions are required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
