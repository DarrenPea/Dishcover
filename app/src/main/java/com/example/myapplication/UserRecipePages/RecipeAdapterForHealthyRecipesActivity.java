package com.example.myapplication.UserRecipePages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Authentication.User;
import com.example.myapplication.R;
import com.example.myapplication.Recipe;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RecipeAdapterForHealthyRecipesActivity extends RecyclerView.Adapter<RecipeAdapterForHealthyRecipesActivity.RecipeViewHolder> {
    private Context context;
    private List<Recipe> recipeList;

    private User user;

    private UserRecipeOnClickListener listener;

    public interface UserRecipeOnClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapterForHealthyRecipesActivity(Context context, List<Recipe> recipeList, UserRecipeOnClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_recipes_element, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.bind(recipe);
    }

    public void updateData(List<Recipe> newRecipes) {
        recipeList.clear();
        recipeList.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        private ImageView settingsIcon;
        private ImageView recipeImageView;
        private ImageView userImageView;
        private TextView dishNameTextView;
        private TextView likesTextView;
        private TextView reviewsTextView;
        private TextView handleTextView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImageView = itemView.findViewById(R.id.recipeImage);
            dishNameTextView = itemView.findViewById(R.id.recipeTitle);
            likesTextView = itemView.findViewById(R.id.likes);
            reviewsTextView = itemView.findViewById(R.id.reviews);
            handleTextView = itemView.findViewById(R.id.handle);
            userImageView = itemView.findViewById(R.id.profilePicture);
            settingsIcon = itemView.findViewById(R.id.settingsIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Recipe recipe = recipeList.get(position);
                        listener.onRecipeClick(recipe);
                    }
                }
            });

            settingsIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOptions(recipeList.get(getAdapterPosition()));
                }
            });
        }

        @SuppressLint("SetTextI18n")
        private void showOptions(Recipe recipe) {
            user = User.getInstance();

            View bottomSheetView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.home_recipe_options, null);

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(itemView.getContext());
            bottomSheetDialog.setContentView(bottomSheetView);

            TextView like = bottomSheetView.findViewById(R.id.like);

            // Initialize like button icon based on liked state
            if (recipe.isLikedByUser(user.getUid())) {
                like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likes_filled, 0, 0, 0);
                like.setText("Retract your like");
            } else {
                like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likes_outlined, 0, 0, 0);
                like.setText("Like this recipe");
            }

            like.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    if (recipe.isLikedByUser(user.getUid())) {
                        recipe.toggleLikeByUser(user.getUid());
                        String newLikesText = recipe.getLikes() + " Likes";
                        likesTextView.setText(newLikesText);
                        like.setText("Like this recipe");
                        Toast.makeText(v.getContext(), "You unliked this recipe!", Toast.LENGTH_SHORT).show();

                        like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likes_outlined, 0, 0, 0);
                    } else {
                        int currentLikes = recipe.getLikes();
                        int newLikes = currentLikes + 1;
                        recipe.toggleLikeByUser(user.getUid());
                        String newLikesText = newLikes + " Likes";
                        likesTextView.setText(newLikesText);
                        like.setText("Retract your like");
                        Toast.makeText(v.getContext(), "You liked this recipe!", Toast.LENGTH_SHORT).show();

                        like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likes_filled, 0, 0, 0);
                    }

                    // Save liked state in SharedPreferences
                    SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences("LIKED_STATE", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(recipe.getRecipeId(), recipe.isLikedByUser(user.getUid()));
                    editor.apply();
                }
            });

            bottomSheetDialog.show();
        }


        public void bind(Recipe recipe) {
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.bg_gradient_space)
                    .error(R.drawable.user_icon)
                    .into(recipeImageView);

            dishNameTextView.setText(recipe.getDishName());
            String likesText = recipe.getLikes() + " Likes";
            likesTextView.setText(likesText);
            String reviewsText = recipe.getReviews() + " Reviews";
            reviewsTextView.setText(reviewsText);
            fetchUserDetails(recipe.getUserId());
        }

        @SuppressLint("SetTextI18n")
        private void fetchUserDetails(String userId) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String handle = documentSnapshot.getString("displayName");
                            String profilePictureUrl = documentSnapshot.getString("profileImageUri");

                            handleTextView.setText("@" + handle);

                            Glide.with(context)
                                    .load(profilePictureUrl)
                                    .placeholder(R.drawable.account_circle)
                                    .error(R.drawable.account_circle)
                                    .into(userImageView);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        handleTextView.setText("@Unknown");
                        Glide.with(context)
                                .load(R.drawable.account_circle)
                                .placeholder(R.drawable.account_circle)
                                .error(R.drawable.account_circle)
                                .into(userImageView);
                    });
        }
    }
}
