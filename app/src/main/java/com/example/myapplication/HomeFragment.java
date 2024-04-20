package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Authentication.User;
import com.example.myapplication.DishcoverRecipePages.BiriyaniPage;
import com.example.myapplication.DishcoverRecipePages.NoodlePage;
import com.example.myapplication.DishcoverRecipePages.TomahawkPage;
import com.example.myapplication.UserRecipePages.FavouritesPage;
import com.example.myapplication.UserRecipePages.HealthyPage;
import com.example.myapplication.UserRecipePages.UserRecipePage;
import com.example.myapplication.adapters.RecipeAdapterForHomePage;
import com.example.myapplication.barcode.BarcodeMain;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private TextView fullNameTextView, favorites, healthy;
    private RecyclerView recyclerView, recyclerViewHealthy;
    private CardView tomahawk, ramen, biriyani;
    private RecipeAdapterForHomePage adapter;
    private RecipeAdapterForHomePage healthyRecipesAdapter;
    private List<Recipe> recipeList;
    private SearchView searchView;

    private ImageView barcodeOpen;
    private ProgressBar loadingProgressBar;




    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    public void onResume() {
        super.onResume();
        loadRecipes();
        searchView.setQuery("", false);
        searchView.clearFocus();

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        loadingProgressBar = view.findViewById(R.id.progressBar);
        favorites = view.findViewById(R.id.textView9);
        healthy = view.findViewById(R.id.textView10);
        barcodeOpen = view.findViewById(R.id.scanner_bg);
        tomahawk = view.findViewById(R.id.tomahawkCard);
        ramen = view.findViewById(R.id.ramenCard);
        biriyani = view.findViewById(R.id.biriyaniCard);

        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(requireContext(), spoonacular_recipes.class);
                // Pass the search query as an extra to the new activity
                intent.putExtra("SEARCH_QUERY", query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle text changes
                return false;
            }
        });
        fullNameTextView = view.findViewById(R.id.display_name_placeholder);
        recyclerView = view.findViewById(R.id.communityRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recipeList = new ArrayList<>();
        adapter = new RecipeAdapterForHomePage(requireContext(), recipeList, new RecipeAdapterForHomePage.UserRecipeOnClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(requireContext(), UserRecipePage.class);
                intent.putExtra("RECIPE", recipe);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
        loadRecipes();

        recyclerViewHealthy = view.findViewById(R.id.healthyRecyclerView);

        LinearLayoutManager layoutManagerHealthy = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewHealthy.setLayoutManager(layoutManagerHealthy);

        healthyRecipesAdapter = new RecipeAdapterForHomePage(requireContext(), new ArrayList<>(), new RecipeAdapterForHomePage.UserRecipeOnClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(requireContext(), UserRecipePage.class);
                intent.putExtra("RECIPE", recipe);
                startActivity(intent);
            }
        });
        recyclerViewHealthy.setAdapter(healthyRecipesAdapter);
        loadHealthyRecipes();

        User currentUser = User.getInstance();
        String displayName = currentUser.getFullName();
        fullNameTextView.setText(displayName != null ? displayName + "!" : "");

        barcodeOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BarcodeMain.class);
                startActivity(intent);
            }
        });

        tomahawk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TomahawkPage.class);
                startActivity(intent);
            }
        });


        ramen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NoodlePage.class);
                startActivity(intent);
            }
        });


        biriyani.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BiriyaniPage.class);
                startActivity(intent);
            }
        });

        favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FavouritesPage.class);
                startActivity(intent);
            }
        });

        healthy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HealthyPage.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == getActivity().RESULT_OK) {
            loadRecipes();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadRecipes() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> unsortedRecipeList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        unsortedRecipeList.add(recipe);
                    }
                    List<Recipe> sortedRecipeList = mergeSortRecipes(unsortedRecipeList);
                    recipeList.clear();
                    recipeList.addAll(sortedRecipeList);
                    adapter.notifyDataSetChanged();
                    loadingProgressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreException", "Failed to load recipes: " + e.getMessage(), e);
                    loadingProgressBar.setVisibility(View.GONE);
                });
    }

    private void loadHealthyRecipes() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("recipes")
                .whereEqualTo("tag", "Healthy")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> healthyRecipeList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        healthyRecipeList.add(recipe);
                    }
                    healthyRecipesAdapter.updateData(healthyRecipeList); // Update the adapter with healthy recipes
                    loadingProgressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreException", "Failed to load recipes: " + e.getMessage(), e);
                    loadingProgressBar.setVisibility(View.GONE);
                });
    }



    private List<Recipe> mergeSortRecipes(List<Recipe> recipes) {
        if (recipes.size() <= 1) {
            return recipes;
        }

        int mid = recipes.size() / 2;
        List<Recipe> left = mergeSortRecipes(recipes.subList(0, mid));
        List<Recipe> right = mergeSortRecipes(recipes.subList(mid, recipes.size()));

        return merge(left, right);
    }

    private List<Recipe> merge(List<Recipe> left, List<Recipe> right) {
        List<Recipe> merged = new ArrayList<>();
        int leftIndex = 0;
        int rightIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (left.get(leftIndex).getLikes() >= right.get(rightIndex).getLikes()) {
                merged.add(left.get(leftIndex));
                leftIndex++;
            } else {
                merged.add(right.get(rightIndex));
                rightIndex++;
            }
        }

        while (leftIndex < left.size()) {
            merged.add(left.get(leftIndex));
            leftIndex++;
        }

        while (rightIndex < right.size()) {
            merged.add(right.get(rightIndex));
            rightIndex++;
        }

        return merged;
    }

}
