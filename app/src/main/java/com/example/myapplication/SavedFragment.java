package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapplication.Authentication.User;
import com.example.myapplication.UserRecipePages.UserRecipePage;
import com.example.myapplication.adapters.RecipeAdapterForSavedRecipesActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SavedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ProgressBar progressBar;
    private User currentUser;
    private RecyclerView recyclerView;
    private ImageView navigateBack;
    private RecipeAdapterForSavedRecipesActivity adapter;
    private List<Recipe> recipeList;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SavedFragment() {
        // Required empty public constructor
    }

    public static SavedFragment newInstance(String param1, String param2) {
        SavedFragment fragment = new SavedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_saved, container, false);

        progressBar = rootView.findViewById(R.id.progressBar);
        recyclerView = rootView.findViewById(R.id.recipeRecyclerView);
        navigateBack = rootView.findViewById(R.id.imageView4);
        currentUser = User.getInstance();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recipeList = new ArrayList<>();
        adapter = new RecipeAdapterForSavedRecipesActivity(getContext(), recipeList, new RecipeAdapterForSavedRecipesActivity.UserRecipeOnClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(getContext(), UserRecipePage.class);
                intent.putExtra("RECIPE", recipe);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
        loadRecipes();

        return rootView;
    }

    private void loadRecipes() {
        progressBar.setVisibility(View.VISIBLE);
        User currentUser = User.getInstance();
        if (currentUser != null) {
            currentUser.getSavedIdsFromFirestore(new User.SavedIdsListener() {
                @Override
                public void onSavedIdsLoaded(List<String> savedIds) {
                    if (savedIds != null) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        recipeList.clear(); // Clear existing list
                        for (String savedId : savedIds) {
                            db.collection("recipes")
                                    .document(savedId)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            Recipe recipe = documentSnapshot.toObject(Recipe.class);
                                            recipeList.add(recipe);
                                            adapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to load recipe: " + savedId, Toast.LENGTH_SHORT).show();
                                        Log.e("FirestoreException", "Failed to load recipe: " + savedId, e);
                                        progressBar.setVisibility(View.GONE);
                                    });
                        }
                    }
                }
            });
        }
    }
}
