package com.example.myapplication.listener;

import com.example.myapplication.models.SearchIngredientsResponse;

import java.util.List;

public interface SearchIngredientsResponseListener {
    void didFetch(List<SearchIngredientsResponse> response, String message);
    void didError(String message);
}
