package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Recipe implements Serializable {
    private String recipeId;
    private String dishName;
    private String imageUrl;
    private String ingredients;
    private int likes;
    private String recipe;
    private int reviews;
    private long timestampMillis; // Updated field type
    private String userId;
    private String userHandle; // New field for user's handle
    private String userProfilePicUrl; // New field for user's profile picture URL

    private List<String> likedBy;


    // Constructors
    public Recipe() {
        // Default constructor required for Firestore
        likedBy = new ArrayList<>();
    }

    public Recipe(String recipeId, String dishName, String imageUrl, String ingredients, int likes, String recipe, int reviews, Timestamp timestamp, String userId, String userHandle, String userProfilePicUrl, List<String> likedBy) {
        this.recipeId = recipeId;
        this.dishName = dishName;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
        this.likes = likes;
        this.recipe = recipe;
        this.reviews = reviews;
        this.timestampMillis = timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000;
        this.userId = userId;
        this.userHandle = userHandle;
        this.userProfilePicUrl = userProfilePicUrl;
        this.likedBy = likedBy;
    }


    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public boolean isLikedByUser(String userId) {
        return likedBy.contains(userId);
    }

    public void toggleLikeByUser(String userId) {
        if (isLikedByUser(userId)) {
            likedBy.remove(userId);
            setLikes(getLikes() - 1);
        } else {
            likedBy.add(userId);
            setLikes(getLikes() + 1);
        }
        updateLikedByInFirestore();
    }

    private void updateLikedByInFirestore() {
        String recipeId = getRecipeId();
        if (recipeId != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("recipes").document(recipeId)
                    .update("likedBy", likedBy)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "LikedBy updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating likedBy", e));
        } else {
            Log.e(TAG, "Recipe ID is null");
        }
    }

    // Getters and setters
    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
        String recipeId = getRecipeId();
        if (recipeId != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("recipes").document(recipeId)
                    .update("likes", likes)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Likes updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating likes", e));
        } else {
            Log.e(TAG, "Recipe ID is null");
        }
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public int getReviews() {
        return reviews;
    }

    public void setReviews(int reviews) {
        this.reviews = reviews;
    }

    public Timestamp getTimestamp() {
        return new Timestamp(timestampMillis / 1000, (int) ((timestampMillis % 1000) * 1000000));
    }

    public String getFormattedTimestamp() {
        Date date = new Date(timestampMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestampMillis = timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserHandle() {
        return userHandle;
    }

    public void setUserHandle(String userHandle) {
        this.userHandle = userHandle;
    }

    public String getUserProfilePicUrl() {
        return userProfilePicUrl;
    }

    public void setUserProfilePicUrl(String userProfilePicUrl) {
        this.userProfilePicUrl = userProfilePicUrl;
    }

    public static void deleteRecipe(String recipeId, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference recipeRef = firestore.collection("recipes").document(recipeId);
        recipeRef.delete()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    public void updateRecipeName(String newName, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("recipes").document(recipeId)
                .update("dishName", newName)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

}
