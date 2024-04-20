package com.example.myapplication;

public class search_ingredient_formatter {
    String ingredients;
    search_ingredient_formatter(String ingredients) {
        this.ingredients = ingredients;
    }
    String formatString() {
        String[] words = ingredients.split(",\\s*");

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                builder.append(",+").append(words[i]);
            }
            else {
                builder.append(words[i]);
            }
        }

        return builder.toString();
    }
}
