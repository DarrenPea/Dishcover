package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.SearchIngredientsAdapter;
import com.example.myapplication.listener.RecipeClickListener;
import com.example.myapplication.listener.SearchIngredientsResponseListener;
import com.example.myapplication.models.SearchIngredientsResponse;

import java.util.List;

public class spoonacular_recipes extends AppCompatActivity {
    ProgressDialog dialog;
    spoonacular_request_manager manager;
    SearchIngredientsAdapter searchIngredientsAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipes);

        // Getting search bar query
        String query = getIntent().getStringExtra("SEARCH_QUERY");
        String ingredients = new search_ingredient_formatter(query).formatString();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading...");

        manager = new spoonacular_request_manager(this);
        manager.getSpecificRecipes(searchIngredientsResponseListener, ingredients);
        dialog.show();
    }

    private final SearchIngredientsResponseListener searchIngredientsResponseListener = new SearchIngredientsResponseListener() {
        @Override
        public void didFetch(List<SearchIngredientsResponse> response, String message) {
            dialog.dismiss();
            recyclerView = findViewById(R.id.search_ingredients);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(spoonacular_recipes.this, 1));
            searchIngredientsAdapter = new SearchIngredientsAdapter(spoonacular_recipes.this, response, recipeClickListener);
            recyclerView.setAdapter(searchIngredientsAdapter);
        }

        @Override
        public void didError(String message) {
            Toast.makeText(spoonacular_recipes.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    private final RecipeClickListener recipeClickListener = new RecipeClickListener() {
        @Override
        public void onRecipeClicked(String id) {
            startActivity(new Intent(spoonacular_recipes.this, recipe_details_activity.class)
                    .putExtra("id", id));
        }
    };
}
