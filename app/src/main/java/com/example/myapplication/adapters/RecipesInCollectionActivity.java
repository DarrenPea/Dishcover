package com.example.myapplication.adapters;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Authentication.User;
import com.example.myapplication.CollectionsManager;
import com.example.myapplication.R;
import com.example.myapplication.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipesInCollectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterForRecipesInCollection adapter;
    private List<Recipe> recipeList;
    private CollectionsManager collectionsManager;
    private String collectionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes_in_collection);

        // Initialize user and collections manager
        User user = User.getInstance();
        String userId = user.getUid();
        collectionsManager = new CollectionsManager(userId);

        // Get the collection name passed from the previous activity
        collectionName = getIntent().getStringExtra("collectionName");

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView);
        recipeList = new ArrayList<>();
        adapter = new AdapterForRecipesInCollection(this, recipeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        collectionsManager.fetchRecipesInCollection(collectionName, new CollectionsManager.OnRecipesFetchListener() {
            @Override
            public void onRecipesFetch(List<Recipe> recipes) {
                // Update the recipe list and notify adapter
                recipeList.clear();
                recipeList.addAll(recipes);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onRecipesFetchError(Exception e) {
                // Handle error fetching recipes
                Toast.makeText(RecipesInCollectionActivity.this, "Failed to fetch recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}

