package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Recipe;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterForRecipesInCollection extends RecyclerView.Adapter<AdapterForRecipesInCollection.RecipeViewHolder> {

    private Context context;
    private List<Recipe> recipeList;

    public AdapterForRecipesInCollection(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item_layout, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {

        private ImageView recipeImageView;
        private TextView recipeNameTextView, likes, reviews, handle;
        private CircleImageView profilePicture;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImageView = itemView.findViewById(R.id.recipeImage);
            recipeNameTextView = itemView.findViewById(R.id.recipeName);
            likes = itemView.findViewById(R.id.likes);
            reviews = itemView.findViewById(R.id.reviews);
            handle = itemView.findViewById(R.id.handle);
            profilePicture = itemView.findViewById(R.id.profilePicture);
        }

        public void bind(Recipe recipe) {
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.undraw_page_not_found_re_e9o6)
                    .error(R.drawable.undraw_page_not_found_re_e9o6)
                    .into(recipeImageView);

            recipeNameTextView.setText(recipe.getDishName());
            likes.setText(recipe.getLikes());
            reviews.setText(recipe.getReviews());
            handle.setText(recipe.getUserHandle());

            Glide.with(context)
                    .load(recipe.getUserProfilePicUrl())
                    .placeholder(R.drawable.undraw_page_not_found_re_e9o6)
                    .error(R.drawable.undraw_page_not_found_re_e9o6)
                    .into(profilePicture);
        }
    }
}
