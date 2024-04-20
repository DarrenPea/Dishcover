package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePage extends AppCompatActivity {

    private CircleImageView profileImageView;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize profile image view
        profileImageView = findViewById(R.id.profile_pic);

        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Check if user is logged in
        if (user != null) {
            String userId = user.getUid();
            firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get profile picture URL
                            String profilePicUrl = documentSnapshot.getString("photo_url");
                            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                // Load profile picture using URL directly
                                // You can use any image loading library here if necessary
                                // For simplicity, we use the setImageURI method which accepts a URI
                                profileImageView.setImageURI(Uri.parse(profilePicUrl));
                                showToast("Profile image loaded successfully");
                            } else {
                                showToast("Profile image URL is empty");
                            }
                        } else {
                            showToast("User document doesn't exist");
                        }
                    })
                    .addOnFailureListener(e -> {
                        showToast("Error fetching user document: " + e.getMessage());
                    });
        } else {
            showToast("User is null");
        }
        showToast("SUP");
    }



    // Helper method to display Toast messages
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
