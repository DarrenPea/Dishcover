package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Authentication.User;
import com.example.myapplication.UserRecipePages.UserRecipePage;
import com.example.myapplication.adapters.RecipeAdapterForSavedRecipesActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SavedRecipes extends AppCompatActivity {

    private ProgressBar progressBar;

    private RecyclerView recyclerView;
    private ImageView navigateBack;
    private RecipeAdapterForSavedRecipesActivity adapter;
    private List<Recipe> recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_recipes);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recipeRecyclerView);
        navigateBack = findViewById(R.id.imageView4);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recipeList = new ArrayList<>();
        adapter = new RecipeAdapterForSavedRecipesActivity(this, recipeList, new RecipeAdapterForSavedRecipesActivity.UserRecipeOnClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(SavedRecipes.this, UserRecipePage.class);
                intent.putExtra("RECIPE", recipe);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
        loadRecipes();

        navigateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



    private void loadRecipes() {
        progressBar.setVisibility(View.VISIBLE);
        User currentUser = User.getInstance();
        if (currentUser != null) {
            currentUser.getSavedIdsFromFirestore(new User.SavedIdsListener() {
                @Override
                public void onSavedIdsLoaded(List<String> savedIds) {
                    if (savedIds != null) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        recipeList.clear(); // Clear existing list
                        for (String savedId : savedIds) {
                            db.collection("recipes")
                                    .document(savedId)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            Recipe recipe = documentSnapshot.toObject(Recipe.class);
                                            recipeList.add(recipe);
                                            adapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SavedRecipes.this, "Failed to load recipe: " + savedId, Toast.LENGTH_SHORT).show();
                                        Log.e("FirestoreException", "Failed to load recipe: " + savedId, e);
                                        progressBar.setVisibility(View.GONE);
                                    });
                        }
                    }
                }
            });
        }
    }



}
