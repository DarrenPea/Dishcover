package com.example.myapplication.Authentication;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class User {
    private static User instance;

    private String uid;
    private String email;
    private String display_name;
    private int points;
    private String fullName;
    private Date createdTime;
    private List<String> recipeIds;
    private List<String> savedIds;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private UserListener userListener;
    private String photo_url;

    User() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        savedIds = new ArrayList<>();
    }

    public static synchronized User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    public void setListener(UserListener listener) {
        this.userListener = listener;
    }

    public void populateUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            mFirestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            uid = userId;
                            email = firebaseUser.getEmail();
                            display_name = documentSnapshot.getString("displayName");
                            points = Objects.requireNonNull(documentSnapshot.getLong("points")).intValue();
                            fullName = documentSnapshot.getString("fullName");
                            createdTime = documentSnapshot.getDate("created_time");
                            photo_url = documentSnapshot.getString("profileImageUri");


                            // Notify listener that user data is available
                            if (userListener != null) {
                                userListener.onUserDataLoaded();
                            }
                        }
                    })

                    .addOnFailureListener(e -> {
                    });
        }
    }

    public void onboardUser(String fullName, String displayName, String profileImageUri) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            Map<String, Object> userData = new HashMap<>();
            userData.put("fullName", fullName);
            userData.put("displayName", displayName);
            userData.put("profileImageUri", profileImageUri);

            mFirestore.collection("users").document(userId)
                    .update(userData)
                    .addOnSuccessListener(aVoid -> {
                        if (userListener != null) {
                            userListener.onUserDataUpdated();
                        }
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    public void updateEmailAddress(String currentPassword, String newEmail, AuthObserver observer) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User successfully reauthenticated, proceed to update email address
                        user.updateEmail(newEmail)
                                .addOnCompleteListener(emailUpdateTask -> {
                                    if (emailUpdateTask.isSuccessful()) {
                                        // Email address updated successfully
                                        sendVerificationEmail(newEmail, observer);
                                    } else {
                                        // Failed to update email address
                                        observer.onAuthStatusChanged(false, "Failed to update email address: " + emailUpdateTask.getException().getMessage());
                                    }
                                });
                    } else {
                        // Failed to reauthenticate user
                        observer.onAuthStatusChanged(false, "Failed to reauthenticate user: " + task.getException().getMessage());
                    }
                });
    }

    private void sendVerificationEmail(String email, AuthObserver observer) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(verificationTask -> {
                    if (verificationTask.isSuccessful()) {
                        // Verification email sent successfully
                        observer.onAuthStatusChanged(true, "Email verification sent to " + email);
                    } else {
                        // Failed to send verification email
                        observer.onAuthStatusChanged(false, "Failed to send verification email: " + verificationTask.getException().getMessage());
                    }
                });
    }

    public void updateEmailAddress(String newEmail, AuthObserver observer) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> {
                        observer.onAuthStatusChanged(true, "Email address updated successfully");
                        sendVerificationEmail(observer);
                    })
                    .addOnFailureListener(e -> {
                        observer.onAuthStatusChanged(false, "Failed to update email address: " + e.getMessage());
                    });
        } else {
            observer.onAuthStatusChanged(false, "No user is currently logged in");
        }
    }

    public void sendVerificationEmail(AuthObserver observer) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.sendEmailVerification()
                    .addOnSuccessListener(aVoid -> {
                        observer.onAuthStatusChanged(true, "Verification email sent successfully");
                    })
                    .addOnFailureListener(e -> {
                        observer.onAuthStatusChanged(false, "Failed to send verification email: " + e.getMessage());
                    });
        } else {
            observer.onAuthStatusChanged(false, "No user is currently logged in");
        }
    }

    public void updatePassword(String currentPassword, String newPassword, AuthObserver observer) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), currentPassword);
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(passwordUpdateTask -> {
                                        if (passwordUpdateTask.isSuccessful()) {
                                            observer.onAuthStatusChanged(true, "Password updated successfully");
                                        } else {
                                            observer.onAuthStatusChanged(false, "Failed to update password: " + passwordUpdateTask.getException().getMessage());
                                        }
                                    });
                        } else {
                            observer.onAuthStatusChanged(false, "Failed to reauthenticate user: " + task.getException().getMessage());
                        }
                    });
        } else {
            observer.onAuthStatusChanged(false, "No user is currently logged in");
        }
    }



    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;

    }

    public List<String> getSavedIds() {
        getSavedIdsFromFirestore(savedIds -> {
            this.savedIds = savedIds;
        });
        return savedIds;
    }




    public void setSavedIds(List<String> savedIds) {
        this.savedIds = savedIds;
    }

    public String getDisplayName() {
        return display_name;
    }

    public void setDisplayName(String display_name) {
        this.display_name = display_name;
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            Map<String, Object> userData = new HashMap<>();
            userData.put("displayName", display_name);

            mFirestore.collection("users").document(userId)
                    .update(userData)
                    .addOnSuccessListener(aVoid -> {
                        if (userListener != null) {
                            userListener.onUserDataUpdated();
                        }
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            Map<String, Object> userData = new HashMap<>();
            userData.put("fullName", fullName);

            mFirestore.collection("users").document(userId)
                    .update(userData)
                    .addOnSuccessListener(aVoid -> {
                        if (userListener != null) {
                            userListener.onUserDataUpdated();
                        }
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Uri getProfileImageUri() {
        if (photo_url != null && !photo_url.isEmpty()) {
            return Uri.parse(photo_url);
        } else {
            return Uri.parse("android.resource://com.example.myapplication/drawable/default_user");
        }
    }

    public void setProfileImageUri(Uri profileImageUri, OnProfileImageUpdateListener listener) {
        if (profileImageUri != null) {
            this.photo_url = profileImageUri.toString();
        } else {
            this.photo_url = null;
        }

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            Map<String, Object> userData = new HashMap<>();
            userData.put("profileImageUri", profileImageUri);
            mFirestore.collection("users").document(userId)
                    .update(userData)
                    .addOnSuccessListener(aVoid -> {
                        if (userListener != null) {
                            userListener.onUserDataUpdated();
                        }
                        if (listener != null) {
                            listener.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) {
                            listener.onFailure(e.getMessage());
                        }
                    });
        }
    }


    public List<String> getRecipeIds() {
        return recipeIds;
    }

    public void setRecipeIds(List<String> recipeIds) {
        this.recipeIds = recipeIds;
    }

    public void toggleSaveRecipe(Context context, String recipeId) {
        boolean wasSaved = savedIds.contains(recipeId);
        if (wasSaved) {
            savedIds.remove(recipeId);
            updateSavedIdsInFirestore(recipeId, false);
            Toast.makeText(context, "Recipe unsaved successfully", Toast.LENGTH_SHORT).show();
        } else {
            savedIds.add(recipeId);
            updateSavedIdsInFirestore(recipeId, true);
            Toast.makeText(context, "Recipe saved successfully", Toast.LENGTH_SHORT).show();
        }
    }

    public void getSavedIdsFromFirestore(SavedIdsListener listener) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            mFirestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Object savedIdsObject = documentSnapshot.get("savedIds");
                            if (savedIdsObject instanceof List) {
                                List<?> savedIdsList = (List<?>) savedIdsObject;
                                List<String> savedIds = new ArrayList<>();
                                for (Object id : savedIdsList) {
                                    if (id instanceof String) {
                                        savedIds.add((String) id);
                                    }
                                }
                                listener.onSavedIdsLoaded(savedIds);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure...
                    });
        }
    }

    private void updateSavedIdsInFirestore(String recipeId, boolean wasSaved) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DocumentReference userRef = mFirestore.collection("users").document(userId);

            if (wasSaved) {
                userRef.update("savedIds", FieldValue.arrayUnion(recipeId))
                        .addOnSuccessListener(aVoid -> {
                            // Successfully added recipeId to savedIds in Firestore
                        })
                        .addOnFailureListener(e -> {
                            // Failed to update savedIds in Firestore
                        });
            } else {
                userRef.update("savedIds", FieldValue.arrayRemove(recipeId))
                        .addOnSuccessListener(aVoid -> {
                            // Successfully removed recipeId from savedIds in Firestore
                        })
                        .addOnFailureListener(e -> {
                            // Failed to update savedIds in Firestore
                        });
            }
        }
    }




    public interface UserListener {
        void onUserDataLoaded();
        void onUserDataUpdated();
    }

    public interface OnProfileImageUpdateListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface SavedIdsListener {
        void onSavedIdsLoaded(List<String> savedIds);
    }

}

