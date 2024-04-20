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

public class FavouritesPage extends AppCompatActivity {

    private ProgressBar progressBar;

    private RecyclerView recyclerView;
    private ImageView navigateBack;
    private RecipeAdapterForFavouriteRecipesActivity adapter;
    private List<Recipe> recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_recipes);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recipeRecyclerView);
        navigateBack = findViewById(R.id.imageView4);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recipeList = new ArrayList<>();
        adapter = new RecipeAdapterForFavouriteRecipesActivity(this, recipeList, new RecipeAdapterForFavouriteRecipesActivity.UserRecipeOnClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(FavouritesPage.this, UserRecipePage.class);
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



    @SuppressLint("NotifyDataSetChanged")
    private void loadRecipes() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> unsortedRecipeList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        unsortedRecipeList.add(recipe);
                    }
                    List<Recipe> sortedRecipeList = mergeSortRecipes(unsortedRecipeList);
                    recipeList.clear();
                    recipeList.addAll(sortedRecipeList);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FavouritesPage.this, "Failed to load recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreException", "Failed to load recipes: " + e.getMessage(), e);
                    progressBar.setVisibility(View.GONE);
                });
    }


    private List<Recipe> mergeSortRecipes(List<Recipe> recipes) {
        if (recipes.size() <= 1) {
            return recipes;
        }

        int mid = recipes.size() / 2;
        List<Recipe> left = mergeSortRecipes(recipes.subList(0, mid));
        List<Recipe> right = mergeSortRecipes(recipes.subList(mid, recipes.size()));

        return merge(left, right);
    }

    private List<Recipe> merge(List<Recipe> left, List<Recipe> right) {
        List<Recipe> merged = new ArrayList<>();
        int leftIndex = 0;
        int rightIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (left.get(leftIndex).getLikes() >= right.get(rightIndex).getLikes()) {
                merged.add(left.get(leftIndex));
                leftIndex++;
            } else {
                merged.add(right.get(rightIndex));
                rightIndex++;
            }
        }

        while (leftIndex < left.size()) {
            merged.add(left.get(leftIndex));
            leftIndex++;
        }

        while (rightIndex < right.size()) {
            merged.add(right.get(rightIndex));
            rightIndex++;
        }

        return merged;
    }
}
