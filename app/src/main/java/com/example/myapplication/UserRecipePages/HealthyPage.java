package com.example.myapplication.UserRecipePages;

import android.annotation.SuppressLint;
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
import com.example.myapplication.R;
import com.example.myapplication.Recipe;
import com.example.myapplication.UserRecipePages.UserRecipePage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HealthyPage extends AppCompatActivity {

    private ProgressBar progressBar;

    private RecyclerView recyclerView;
    private ImageView navigateBack;
    private RecipeAdapterForHealthyRecipesActivity healthyRecipesAdapter;
    private List<Recipe> recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthy_recipes);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recipeRecyclerView);
        navigateBack = findViewById(R.id.imageView4);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recipeList = new ArrayList<>();
        healthyRecipesAdapter = new RecipeAdapterForHealthyRecipesActivity(this, recipeList, new RecipeAdapterForHealthyRecipesActivity.UserRecipeOnClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(HealthyPage.this, UserRecipePage.class);
                intent.putExtra("RECIPE", recipe);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(healthyRecipesAdapter);
        loadHealthyRecipes();

        navigateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



    @SuppressLint("NotifyDataSetChanged")
    private void loadHealthyRecipes() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("recipes")
                .whereEqualTo("tag", "Healthy")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> healthyRecipeList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        healthyRecipeList.add(recipe);
                    }
                    healthyRecipesAdapter.updateData(healthyRecipeList); // Update the adapter with healthy recipes
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HealthyPage.this, "Failed to load recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreException", "Failed to load recipes: " + e.getMessage(), e);
                    progressBar.setVisibility(View.GONE);
                });
    }
}
