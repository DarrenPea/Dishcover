package com.example.myapplication.adapters;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Recipe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class RecipeAdapterForYourRecipesActivity extends RecyclerView.Adapter<RecipeAdapterForYourRecipesActivity.RecipeViewHolder> {
    private Context context;
    private List<Recipe> recipeList;

    private UserRecipeOnClickListener listener;

    public interface UserRecipeOnClickListener {
        void onRecipeClick(Recipe recipe);

    }

    public RecipeAdapterForYourRecipesActivity(Context context, List<Recipe> recipeList, UserRecipeOnClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.your_recipes_element, parent, false);
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
        private ImageView settingsIcon;
        private TextView dishNameTextView;
        private TextView likesTextView;
        private TextView reviewsTextView;
        private TextView createdTimeTextView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            recipeImageView = itemView.findViewById(R.id.recipeImage);
            dishNameTextView = itemView.findViewById(R.id.recipeTitle);
            likesTextView = itemView.findViewById(R.id.likes);
            reviewsTextView = itemView.findViewById(R.id.reviews);
            createdTimeTextView = itemView.findViewById(R.id.timestamp);
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


        public void bind(Recipe recipe) {
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.loading_file)
                    .error(R.drawable.not_found)
                    .into(recipeImageView);

            dishNameTextView.setText(recipe.getDishName());
            String likesText = recipe.getLikes() + " Likes";
            likesTextView.setText(likesText);
            String reviewsText = recipe.getReviews() + " Reviews";
            reviewsTextView.setText(reviewsText);
            String createdTimeText = recipe.getFormattedTimestamp();
            createdTimeTextView.setText(createdTimeText);
        }

        private void showOptions(Recipe recipe) {

            View bottomSheetView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.your_recipes_options, null);

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(itemView.getContext());
            bottomSheetDialog.setContentView(bottomSheetView);

            TextView edit = bottomSheetView.findViewById(R.id.edit);
            TextView delete = bottomSheetView.findViewById(R.id.delete);

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show a popup dialog to edit recipe name
                    showEditRecipeNameDialog(recipe, bottomSheetDialog);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show a confirmation popup dialog for delete action
                    showDeleteConfirmationDialog(recipe, bottomSheetDialog);
                }
            });
            bottomSheetDialog.show();
        }

        private void showEditRecipeNameDialog(Recipe recipe, BottomSheetDialog bottomSheetDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            View dialogView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.edit_recipename_dialog, null);
            EditText editText = dialogView.findViewById(R.id.emailLayout);

            builder.setView(dialogView)
                    .setTitle("Edit Recipe Name")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Save the edited recipe name
                            String newName = editText.getText().toString();
                            recipe.updateRecipeName(newName,
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Recipe name updated successfully
                                            recipe.setDishName(newName);
                                            int updatedPosition = recipeList.indexOf(recipe);
                                            if (updatedPosition != -1) {
                                                notifyItemChanged(updatedPosition);
                                            }
                                            bottomSheetDialog.dismiss();
                                        }
                                    },
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Failed to update recipe name
                                            Log.e(TAG, "Error updating recipe name", e);
                                            // Handle the error as needed
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }

        private void showDeleteConfirmationDialog(Recipe recipe, BottomSheetDialog bottomSheetDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete the recipe from Firebase using Recipe class method
                            Recipe.deleteRecipe(recipe.getRecipeId(),
                                    new OnSuccessListener<Void>() {
                                        @SuppressLint("RestrictedApi")
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Recipe deleted successfully
                                            Log.d(TAG, "Recipe deleted successfully");
                                            int position = recipeList.indexOf(recipe);
                                            if (position != -1) {
                                                recipeList.remove(position);
                                                notifyItemRemoved(position);
                                            }
                                            bottomSheetDialog.dismiss();
                                        }
                                    },
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Failed to delete recipe
                                            Log.e(TAG, "Error deleting recipe", e);
                                            // Handle the error as needed
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
        // Other existing code
    }
}

