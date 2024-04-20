package com.example.myapplication.UserRecipePages;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Authentication.User;
import com.example.myapplication.R;
import com.example.myapplication.Recipe;
import com.bumptech.glide.Glide;

public class UserRecipePage extends AppCompatActivity {

    private TextView recipeNameTextView;
    private TextView userNameTextView;
    private TextView ingredientsTextView, recipeTextView;
    private User user;
    private ImageView recipeImageView, userProfileImageView, navigateBack;
    private ImageButton likesButton, saveButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_page);

        recipeNameTextView = findViewById(R.id.recipeName);
        userNameTextView = findViewById(R.id.userName);
        ingredientsTextView = findViewById(R.id.textView);
        recipeImageView = findViewById(R.id.recipeImage);
        userProfileImageView = findViewById(R.id.profilePicture);
        likesButton = findViewById(R.id.likesButton);
        saveButton = findViewById(R.id.saveButton);
        recipeTextView = findViewById(R.id.textView4);
        navigateBack = findViewById(R.id.navigateBack);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        user = User.getInstance();
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("RECIPE");


        if (recipe != null) {
            recipeNameTextView.setText(recipe.getDishName());
            userNameTextView.setText(recipe.getUserHandle());
            ingredientsTextView.setText(recipe.getIngredients());
            recipeTextView.setText(recipe.getRecipe());

            Glide.with(this)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.undraw_page_not_found_re_e9o6)
                    .into(recipeImageView);

            Glide.with(this)
                    .load(recipe.getUserProfilePicUrl())
                    .placeholder(R.drawable.default_img_foreground)
                    .into(userProfileImageView);

            updateLikesButtonState(recipe);
            updateSaveButtonState(recipe);

        }


        likesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert recipe != null;
                if (recipe.isLikedByUser(user.getUid())) {
                    recipe.toggleLikeByUser(user.getUid());
                    likesButton.setImageResource(R.drawable.likes_outlined);
                } else {
                    int currentLikes = recipe.getLikes();
                    recipe.toggleLikeByUser(user.getUid());
                    likesButton.setImageResource(R.drawable.likes_filled);
                }
                updateLikesButtonState(recipe);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recipe != null) {
                    boolean isSaved = user.getSavedIds().contains(recipe.getRecipeId());
                    if (!isSaved) {
                        user.toggleSaveRecipe(UserRecipePage.this, recipe.getRecipeId());
                        saveButton.setImageResource(R.drawable.bookmarks);
                    } else {
                        user.toggleSaveRecipe(UserRecipePage.this, recipe.getRecipeId());
                        saveButton.setImageResource(R.drawable.bookmarks_outlined);
                    }
                    sharedPreferences.edit().putBoolean(recipe.getRecipeId(), !isSaved).apply();

                }
            }
        });

        navigateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }




    private void updateLikesButtonState(Recipe recipe) {
        if (recipe != null) {
            if (recipe.isLikedByUser(user.getUid())) {
                likesButton.setImageResource(R.drawable.likes_filled);
            } else {
                likesButton.setImageResource(R.drawable.likes_outlined);
            }
        }
    }

    private void updateSaveButtonState(Recipe recipe) {
        if (recipe != null) {
            boolean isSaved = user.getSavedIds().contains(recipe.getRecipeId());

            if (isSaved) {
                saveButton.setImageResource(R.drawable.bookmarks);
            } else {
                saveButton.setImageResource(R.drawable.bookmarks_outlined);
            }
        }
    }
}
