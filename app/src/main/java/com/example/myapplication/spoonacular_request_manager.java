package com.example.myapplication;

import android.content.Context;

import com.example.myapplication.listener.RandomRecipeResponseListener;
import com.example.myapplication.listener.RecipeDetailsListener;
import com.example.myapplication.listener.RecipeStepsListener;
import com.example.myapplication.listener.SearchIngredientsResponseListener;
import com.example.myapplication.models.ExtendedIngredient;
import com.example.myapplication.models.RecipeDetailsResponse;
import com.example.myapplication.models.RecipeStepsResponse;
import com.example.myapplication.models.SearchIngredientsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class spoonacular_request_manager {
    Context context;
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public spoonacular_request_manager(Context context) {
        this.context = context;
    }

    public void getSpecificRecipes(SearchIngredientsResponseListener listener, String ingredients) {
        CallSpecificRecipes callSpecificRecipes = retrofit.create(CallSpecificRecipes.class);
        Call<List<SearchIngredientsResponse>> call = callSpecificRecipes.callSpecificRecipe(context.getString(R.string.api_key), "6", ingredients);
        call.enqueue(new Callback<List<SearchIngredientsResponse>>() {
            @Override
            public void onResponse(Call<List<SearchIngredientsResponse>> call, Response<List<SearchIngredientsResponse>> response) {
                if (!response.isSuccessful()) {
                    listener.didError(response.message());
                }
                listener.didFetch(response.body(), response.message());
            }

            @Override
            public void onFailure(Call<List<SearchIngredientsResponse>> call, Throwable t) {
                listener.didError(t.getMessage());
            }
        });
    }

    public void getRecipeSteps(RecipeStepsListener listener, int id) {
        CallRecipeSteps callRecipeSteps = retrofit.create(CallRecipeSteps.class);
        Call<List<RecipeStepsResponse>> call = callRecipeSteps.callStepsDetails(id, context.getString(R.string.api_key));
        call.enqueue(new Callback<List<RecipeStepsResponse>>() {
            @Override
            public void onResponse(Call<List<RecipeStepsResponse>> call, Response<List<RecipeStepsResponse>> response) {
                if (!response.isSuccessful()) {
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }

            @Override
            public void onFailure(Call<List<RecipeStepsResponse>> call, Throwable t) {
                listener.didError(t.getMessage());
            }
        });
    }

    public void getRecipeDetails(RecipeDetailsListener listener, int id) {
        CallRecipeDetails callRecipeDetails = retrofit.create(CallRecipeDetails.class);
        Call<RecipeDetailsResponse> call = callRecipeDetails.callRecipeDetails(id, context.getString(R.string.api_key));
        call.enqueue(new Callback<RecipeDetailsResponse>() {
            @Override
            public void onResponse(Call<RecipeDetailsResponse> call, Response<RecipeDetailsResponse> response) {
                if (!response.isSuccessful()) {
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }

            @Override
            public void onFailure(Call<RecipeDetailsResponse> call, Throwable t) {
                listener.didError(t.getMessage());
            }
        });
    }

    public void getRandomRecipes(RandomRecipeResponseListener listener, String tags) {
        CallRandomRecipes callRandomRecipes = retrofit.create(CallRandomRecipes.class);
        Call<ExtendedIngredient> call = callRandomRecipes.callRandomRecipe("10", tags, context.getString(R.string.api_key));
        call.enqueue(new Callback<ExtendedIngredient>() {
            @Override
            public void onResponse(Call<ExtendedIngredient> call, Response<ExtendedIngredient> response) {
                if (!response.isSuccessful()) {
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }

            @Override
            public void onFailure(Call<ExtendedIngredient> call, Throwable t) {
                listener.didError(t.getMessage());
            }
        });
    }

    private interface CallSpecificRecipes {
        @GET("recipes/findByIngredients?number=10&ranking=1&ignorePantry=false")
        Call<List<SearchIngredientsResponse>> callSpecificRecipe(
                @Query("apiKey") String apiKey,
                @Query("number") String number,
                @Query("ingredients") String ingredients
        );
    }

    private interface CallRecipeSteps {
        @GET("recipes/{id}/analyzedInstructions?stepBreakdown=true")
        Call<List<RecipeStepsResponse>> callStepsDetails(
                @Path("id") int id,
                @Query("apiKey") String apiKey
        );
    }

    private interface CallRecipeDetails {
        @GET("recipes/{id}/information")
        Call<RecipeDetailsResponse> callRecipeDetails(
                @Path("id") int id,
                @Query("apiKey") String apiKey
        );
    }

    private interface CallRandomRecipes {
        @GET("recipes/random")
        Call<ExtendedIngredient> callRandomRecipe (
                @Query("number") String number,
                @Query("include tags") String tags,
                @Query("apiKey") String apiKey
        );
    }
}
