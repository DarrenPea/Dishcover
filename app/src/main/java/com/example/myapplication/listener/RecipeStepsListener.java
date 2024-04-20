package com.example.myapplication.listener;

import com.example.myapplication.models.RecipeStepsResponse;

import java.util.List;

public interface RecipeStepsListener {
    void didFetch(List<RecipeStepsResponse> response, String message);
    void didError(String message);
}
