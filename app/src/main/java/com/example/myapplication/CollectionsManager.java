package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionsManager {

    private FirebaseFirestore db;
    private String userId;

    public CollectionsManager(String userId) {
        this.userId = userId;
        db = FirebaseFirestore.getInstance();
    }

    public void createNewCollectionWithRecipe(String collectionName, String recipeId, Context context) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", collectionName);
        data.put("userId", userId);
        data.put("recipeIds", new ArrayList<String>(){{ add(recipeId); }});

        db.collection("collections")
                .add(data)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            // Collection created successfully
                            Toast.makeText(context, "Collection created successfully! You have been awarded points. Check your wallet?", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to create collection
                            Toast.makeText(context, "Failed to create collection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void addRecipeToCollection(String collectionName, String recipeId, Context context) {
        db.collection("collections")
                .whereEqualTo("name", collectionName)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {

                                List<String> recipeIds = (List<String>) document.get("recipeIds");
                                if (recipeIds != null) {
                                    recipeIds.add(recipeId);
                                    document.getReference().update("recipeIds", recipeIds)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Recipe added to collection successfully
                                                        Toast.makeText(context, "Recipe added to collection successfully", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        // Failed to add recipe to collection
                                                        Toast.makeText(context, "Failed to add recipe to collection", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        } else {
                            // Error fetching collection
                            Toast.makeText(context, "Error fetching collection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void checkCollectionExists(String collectionName, OnCollectionCheckListener listener) {
        db.collection("collections")
                .whereEqualTo("name", collectionName)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            listener.onCollectionCheck(!task.getResult().isEmpty());
                        } else {
                            // Error checking for collection existence
                            listener.onCollectionCheckError(task.getException());
                        }
                    }
                });
    }

    public void getAllCollections(Context context, OnCollectionsFetchListener listener) {
        db.collection("collections")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> collectionNames = new ArrayList<>();
                    List<Integer> recipeIdsCountList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String collectionName = documentSnapshot.getString("name");
                        List<String> recipeIds = (List<String>) documentSnapshot.get("recipeIds");
                        int recipeIdsCount = recipeIds != null ? recipeIds.size() : 0;
                        collectionNames.add(collectionName);
                        recipeIdsCountList.add(recipeIdsCount);
                    }
                    Log.d("CollectionsManager", "Fetched collections: " + collectionNames.toString()); // Log fetched collections
                    listener.onCollectionsFetch(collectionNames, recipeIdsCountList);
                })
                .addOnFailureListener(e -> {
                    Log.e("CollectionsManager", "Failed to fetch collections: " + e.getMessage()); // Log error
                    Toast.makeText(context, "Failed to fetch collections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    listener.onCollectionsFetchError(e);
                });
    }

    public void fetchRecipesInCollection(String collectionName, OnRecipesFetchListener listener) {
        db.collection("collections")
                .whereEqualTo("name", collectionName)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> recipeIds = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            List<String> ids = (List<String>) document.get("recipeIds");
                            if (ids != null) {
                                recipeIds.addAll(ids);
                            }
                        }

                        if (!recipeIds.isEmpty()) {
                            db.collection("recipes")
                                    .whereIn("recipeIds", recipeIds)
                                    .get()
                                    .addOnCompleteListener(recipesTask -> {
                                        if (recipesTask.isSuccessful()) {
                                            List<Recipe> recipes = new ArrayList<>();
                                            for (DocumentSnapshot recipeDocument : recipesTask.getResult()) {
                                                Recipe recipe = recipeDocument.toObject(Recipe.class);
                                                recipes.add(recipe);
                                            }
                                            listener.onRecipesFetch(recipes);
                                        } else {
                                            listener.onRecipesFetchError(recipesTask.getException());
                                        }
                                    });
                        } else {
                            listener.onRecipesFetch(new ArrayList<>()); // Notify listener with an empty list
                        }
                    } else {
                        listener.onRecipesFetchError(task.getException());
                    }
                });
    }



    public interface OnRecipesFetchListener {
        void onRecipesFetch(List<Recipe> recipes);
        void onRecipesFetchError(Exception e);
    }



    public interface OnCollectionsFetchListener {
        void onCollectionsFetch(List<String> collections, List<Integer> recipeIdsCount);
        void onCollectionsFetchError(Exception e);
    }


    public interface OnCollectionCheckListener {
        void onCollectionCheck(boolean exists);
        void onCollectionCheckError(Exception e);
    }
}
