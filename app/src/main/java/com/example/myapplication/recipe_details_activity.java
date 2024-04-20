package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.adapters.IngredientsAdapter;
import com.example.myapplication.adapters.InstructionsAdapter;
import com.example.myapplication.listener.RecipeDetailsListener;
import com.example.myapplication.listener.RecipeStepsListener;
import com.example.myapplication.models.RecipeDetailsResponse;
import com.example.myapplication.models.RecipeStepsResponse;
import com.squareup.picasso.Picasso;

import java.util.List;

public class recipe_details_activity extends AppCompatActivity {
    int id;
    TextView textView_meal_name, textView_meal_source, textView_meal_summary;
    ImageView imageView_meal_image;
    RecyclerView recycler_meal_ingredients, recycler_meal_instructions;
    spoonacular_request_manager manager;
    ProgressDialog dialog;
    InstructionsAdapter instructionsAdapter;
    IngredientsAdapter ingredientsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        findViews();

        id = Integer.parseInt(getIntent().getStringExtra("id"));
        manager = new spoonacular_request_manager(this);
        manager.getRecipeDetails(recipeDetailsListener, id);
        manager.getRecipeSteps(recipeStepsListener, id);
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading Details...");
        dialog.show();
    }

    private void findViews() {
        textView_meal_name = findViewById(R.id.textView_meal_name);
        textView_meal_source = findViewById(R.id.textView_meal_source);
        imageView_meal_image = findViewById(R.id.imageView_meal_image);
        recycler_meal_ingredients = findViewById(R.id.recycler_meal_ingredients);
        recycler_meal_instructions = findViewById(R.id.recycler_meal_instructions);
    }

    private final RecipeStepsListener recipeStepsListener = new RecipeStepsListener() {
        @Override
        public void didFetch(List<RecipeStepsResponse> response, String message) {
            dialog.dismiss();
            recycler_meal_instructions.setHasFixedSize(true);
            recycler_meal_instructions.setLayoutManager(new LinearLayoutManager(recipe_details_activity.this, LinearLayoutManager.VERTICAL, false));
            instructionsAdapter = new InstructionsAdapter(recipe_details_activity.this, response);
            recycler_meal_instructions.setAdapter(instructionsAdapter);
        }

        @Override
        public void didError(String message) {
            Toast.makeText(recipe_details_activity.this, message, Toast.LENGTH_LONG).show();
            Log.d("error", message);
        }
    };

    private final RecipeDetailsListener recipeDetailsListener = new RecipeDetailsListener() {
        @Override
        public void didFetch(RecipeDetailsResponse response, String message) {
            textView_meal_name.setText(response.title);
            textView_meal_source.setText(response.sourceName);
            Picasso.get().load(response.image).into(imageView_meal_image);

            recycler_meal_ingredients.setHasFixedSize(true);
            recycler_meal_ingredients.setLayoutManager(new LinearLayoutManager(recipe_details_activity.this, LinearLayoutManager.HORIZONTAL, false));
            ingredientsAdapter = new IngredientsAdapter(recipe_details_activity.this, response.extendedIngredients);
            recycler_meal_ingredients.setAdapter(ingredientsAdapter);
        }

        @Override
        public void didError(String message) {

        }
    };
}