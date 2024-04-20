package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.listener.RecipeClickListener;
import com.example.myapplication.models.SearchIngredientsResponse;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchIngredientsAdapter extends RecyclerView.Adapter<SearchIngredientsViewHolder> {
    Context context;
    List<SearchIngredientsResponse> list;
    RecipeClickListener listener;

    public SearchIngredientsAdapter(Context context, List<SearchIngredientsResponse> list, RecipeClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchIngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchIngredientsViewHolder(LayoutInflater.from(context).inflate(R.layout.recipe_card, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SearchIngredientsViewHolder holder, int position) {
        holder.textView_title.setText(list.get(position).title);
        holder.textView_title.setSelected(true);
        holder.textView_likes.setText(list.get(position).likes+" Likes");
        Picasso.get().load(list.get(position).image).into(holder.imageView_food);

        holder.search_list_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SearchRecipeAdapter", "Recipe clicked: " + list.get(holder.getAdapterPosition()).id);
                listener.onRecipeClicked(String.valueOf(list.get(holder.getAdapterPosition()).id));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
class SearchIngredientsViewHolder extends RecyclerView.ViewHolder {
    CardView search_list_container;
    TextView textView_title, textView_likes;
    ImageView imageView_food;
    public SearchIngredientsViewHolder(@NonNull View itemView) {
        super(itemView);
        search_list_container = itemView.findViewById(R.id.search_list_container);
        textView_title = itemView.findViewById(R.id.textView_title);
        textView_likes = itemView.findViewById(R.id.textView_likes);
        imageView_food = itemView.findViewById(R.id.imageView_food);
    }
}