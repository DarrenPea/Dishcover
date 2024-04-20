package com.example.myapplication.listener;

import com.example.myapplication.models.ExtendedIngredient;

public interface RandomRecipeResponseListener {
    void didFetch(ExtendedIngredient response, String message);
    void didError(String message);
}
