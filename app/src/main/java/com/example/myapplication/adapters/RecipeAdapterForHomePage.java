package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Authentication.User;
import com.example.myapplication.CollectionsManager;
import com.example.myapplication.ExistingCollectionsAdapter;
import com.example.myapplication.R;
import com.example.myapplication.Recipe;
import com.example.myapplication.WalletManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapterForHomePage extends RecyclerView.Adapter<RecipeAdapterForHomePage.RecipeViewHolder> {
    private Context context;
    private List<Recipe> recipeList;

    private User user;

    private UserRecipeOnClickListener listener;

    private List<String> existingCollectionsList;

    public interface UserRecipeOnClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapterForHomePage(Context context, List<Recipe> recipeList, UserRecipeOnClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
        this.existingCollectionsList = new ArrayList<>();

    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_page_recipes_element, parent, false);
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
        private WalletManager userWalletManager;
        private CollectionsManager userCollectionsManager;


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

        private void showNewCollectionDialog(Recipe recipe) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.new_collection_dialog, null);
            builder.setView(view);

            EditText newCollectionNameEditText = view.findViewById(R.id.newCollectionNameEditText);
            TextView cancel = view.findViewById(R.id.cancelButton);
            TextView saveNewCollectionButton = view.findViewById(R.id.saveNewCollectionButton);

            AlertDialog newCollectionDialog = builder.create();

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newCollectionDialog.dismiss();
                }
            });

            saveNewCollectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newCollectionName = newCollectionNameEditText.getText().toString();
                    if (!TextUtils.isEmpty(newCollectionName)) {


                        userCollectionsManager.checkCollectionExists(newCollectionName, new CollectionsManager.OnCollectionCheckListener() {
                            @Override
                            public void onCollectionCheck(boolean exists) {
                                if (exists) {
                                    Toast.makeText(builder.getContext(), "Collection with the same name already exists!", Toast.LENGTH_SHORT).show();
                                } else {
                                    userCollectionsManager.createNewCollectionWithRecipe(newCollectionName, recipe.getRecipeId(), builder.getContext());
                                    userWalletManager.appendToWalletHistory("Created a new collection:", 10);
                                    userWalletManager.updatePoints(10);
                                    newCollectionDialog.dismiss();
                                }
                            }
                            @Override
                            public void onCollectionCheckError(Exception e) {
                                Toast.makeText(builder.getContext(), "Failed to check existing collections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(builder.getContext(), "Please enter a collection name", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            newCollectionDialog.show();
        }

        private void showSaveOptionsDialog(Recipe recipe) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.save_options_dialog, null);
            builder.setView(view);


            userCollectionsManager.getAllCollections(context, new CollectionsManager.OnCollectionsFetchListener() {
                @Override
                public void onCollectionsFetch(List<String> collections, List<Integer> recipeIdsCountList) {
                    RecyclerView existingCollectionsRecyclerView = view.findViewById(R.id.existingCollectionsRecyclerView);
                    ExistingCollectionsAdapter adapter = new ExistingCollectionsAdapter(context, collections, recipeIdsCountList, new ExistingCollectionsAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(String collectionName) {
                           Toast.makeText(context, "Recipe has been saved to this collection successfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Set the adapter to RecyclerView
                    existingCollectionsRecyclerView.setAdapter(adapter);

                    // Optionally, you can also set a layout manager if needed
                    existingCollectionsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                    //TextView existingCollectionButton = view.findViewById(R.id.existingCollectionButton);
                    TextView newCollectionButton = view.findViewById(R.id.newCollectionButton);
                    ImageView simpleSaveButton = view.findViewById(R.id.simpleSaveButton);
                    ImageView recipeImage = view.findViewById(R.id.recipeImage);

                    Glide.with(context)
                            .load(recipe.getImageUrl())
                            .placeholder(R.drawable.collections_bookmark)
                            .error(R.drawable.user_icon)
                            .into(recipeImage);

                    AlertDialog dialog = builder.create();

                    newCollectionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showNewCollectionDialog(recipe);
                        }
                    });

                    simpleSaveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            user.toggleSaveRecipe(builder.getContext(), recipe.getRecipeId());
                        }
                    });


                    dialog.show();
                }

                @Override
                public void onCollectionsFetchError(Exception e) {
                    // Error fetching collections
                    Toast.makeText(context, "Failed to fetch collections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            /*fetchExistingCollections();



            RecyclerView existingCollectionsRecyclerView = view.findViewById(R.id.existingCollectionsRecyclerView);
            ExistingCollectionsAdapter adapter = new ExistingCollectionsAdapter(context, existingCollectionsList);

            // Set the adapter to RecyclerView
            existingCollectionsRecyclerView.setAdapter(adapter);

            // Optionally, you can also set a layout manager if needed
            existingCollectionsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            //TextView existingCollectionButton = view.findViewById(R.id.existingCollectionButton);
            TextView newCollectionButton = view.findViewById(R.id.newCollectionButton);
            ImageView simpleSaveButton = view.findViewById(R.id.simpleSaveButton);
            ImageView recipeImage = view.findViewById(R.id.recipeImage);

            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.collections_bookmark)
                    .error(R.drawable.user_icon)
                    .into(recipeImage);

            AlertDialog dialog = builder.create();

            /*existingCollectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Implement logic to retrieve existing collections from Firebase
                    // and display them to the user in another dialog
                    // Allow the user to select a collection to add the recipe to
                    // Update Firestore accordingly

                    // After user selection and Firestore update, dismiss the dialog
                    dialog.dismiss();
                }
            });

            newCollectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNewCollectionDialog(recipe);
                }
            });

            simpleSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user.toggleSaveRecipe(builder.getContext(), recipe.getRecipeId());
                }
            });

            dialog.show();*/
        }

        private void fetchExistingCollections() {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String userId = user.getUid();

            firestore.collection("collections")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        existingCollectionsList.clear(); // Clear previous data

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String collectionName = document.getString("name");
                            existingCollectionsList.add(collectionName);
                        }

                        // Notify the adapter that data has changed
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Toast.makeText(context, "Failed to fetch existing collections!", Toast.LENGTH_SHORT).show();
                    });
        }

        @SuppressLint("SetTextI18n")
        private void showOptions(Recipe recipe) {
            user = User.getInstance();
            String userId = user.getUid();
            userWalletManager = new WalletManager(userId);
            userCollectionsManager = new CollectionsManager(userId);

            View bottomSheetView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.home_recipe_options, null);

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(itemView.getContext());
            bottomSheetDialog.setContentView(bottomSheetView);

            TextView like = bottomSheetView.findViewById(R.id.like);
            TextView save = bottomSheetView.findViewById(R.id.save);

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

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSaveOptionsDialog(recipe);
                    user.toggleSaveRecipe(bottomSheetDialog.getContext(), recipe.getRecipeId());
                }
            });

            bottomSheetDialog.show();
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
            fetchUserDetails(recipe.getUserId());
        }


        @SuppressLint("SetTextI18n")
        private void fetchUserDetails(String userId) {
            if (userId != null) {
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
            } else {
                // Handle the case where userId is null
                handleTextView.setText("@Unknown");
                Glide.with(context)
                        .load(R.drawable.account_circle)
                        .placeholder(R.drawable.account_circle)
                        .error(R.drawable.account_circle)
                        .into(userImageView);
            }
        }

    }

}
