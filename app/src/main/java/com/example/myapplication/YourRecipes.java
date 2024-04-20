package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Authentication.User;
import com.example.myapplication.UserRecipePages.UserRecipePage;
import com.example.myapplication.adapters.RecipeAdapterForYourRecipesActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class YourRecipes extends AppCompatActivity {
    private ImageView navigateBack;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecipeAdapterForYourRecipesActivity adapter;
    private List<Recipe> recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_recipes);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recipeRecyclerView);
        navigateBack = findViewById(R.id.imageView4);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeList = new ArrayList<>();
        adapter = new RecipeAdapterForYourRecipesActivity(this, recipeList, new RecipeAdapterForYourRecipesActivity.UserRecipeOnClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(YourRecipes.this, UserRecipePage.class);
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
            String userId = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("recipes")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Recipe recipe = document.toObject(Recipe.class);
                            recipeList.add(recipe);
                        }
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("FirestoreException", "Failed to load recipes: " + e.getMessage(), e);
                    });
        }
    }
}



