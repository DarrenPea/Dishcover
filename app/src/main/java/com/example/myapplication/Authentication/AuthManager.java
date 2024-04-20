package com.example.myapplication.Authentication;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class AuthManager implements AuthSubject {
    private List<AuthObserver> observers;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public AuthManager() {
        observers = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void registerObserver(AuthObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(AuthObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(boolean isSuccess, String message) {
        for (AuthObserver observer : observers) {
            observer.onAuthStatusChanged(isSuccess, message);
        }
    }

    public void registerNewUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userId = user.getUid();
                        String display_name = generateUniqueDisplayName();
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setPoints(0);
                        newUser.setDisplayName(display_name);
                        newUser.setCreatedTime(new Date());
                        newUser.setUid(userId);

                        db.collection("users").document(userId)
                                .set(newUser)
                                .addOnCompleteListener(docTask -> {
                                    if (docTask.isSuccessful()) {
                                        notifyObservers(true, "User registered successfully");
                                    } else {
                                        String errorMessage = docTask.getException() != null ? docTask.getException().getMessage() : "Unknown error";
                                        notifyObservers(false, "Failed to register user: " + errorMessage);
                                    }
                                });
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        notifyObservers(false, "Registration failed: " + errorMessage);
                    }
                });
    }

    public void signInUser(String email, String password, boolean rememberMe, AuthObserver observer) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        observer.onAuthStatusChanged(true, "Login successful");
                    } else {
                        observer.onAuthStatusChanged(false, "Login failed: " + task.getException().getMessage());
                    }
                });
    }


    public void resetPasswordLinkSender(String email, AuthObserver observer) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            mAuth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(resetTask -> {
                                        if (resetTask.isSuccessful()) {
                                            observer.onAuthStatusChanged(true, "Password reset email sent successfully");
                                        } else {
                                            observer.onAuthStatusChanged(false, "Failed to send password reset email: " + resetTask.getException().getMessage());
                                        }
                                    });
                        } else {
                            observer.onAuthStatusChanged(false, "Email address does not exist");
                        }
                    } else {
                        observer.onAuthStatusChanged(false, "Error checking email existence: " + task.getException().getMessage());
                    }
                });
    }

    public void changePassword(String currentPassword, String newPassword, AuthObserver observer) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
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
    }



    private String generateUniqueDisplayName() {
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return "dishcover-user" + sb.toString();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

}
