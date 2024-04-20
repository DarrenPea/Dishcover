package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExistingCollectionsAdapter extends RecyclerView.Adapter<ExistingCollectionsAdapter.CollectionViewHolder> {
    private Context context;
    private List<String> collectionsList;
    private List<Integer> recipeIdsCountList;

    private OnItemClickListener listener;

    public ExistingCollectionsAdapter(Context context, List<String> collectionsList, List<Integer> recipeIdsCountList, OnItemClickListener listener) {
        this.context = context;
        this.collectionsList = collectionsList;
        this.recipeIdsCountList = recipeIdsCountList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.existing_collections_element, parent, false);
        return new CollectionViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        String collectionName = collectionsList.get(position);
        int recipeIdsCount = recipeIdsCountList.get(position);
        holder.bind(collectionName, recipeIdsCount);
    }

    @Override
    public int getItemCount() {
        return collectionsList.size();
    }

    public class CollectionViewHolder extends RecyclerView.ViewHolder {
        private TextView collectionNameTextView;
        private TextView recipeIdsCountTextView;

        public CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            collectionNameTextView = itemView.findViewById(R.id.collectionTitle);
            recipeIdsCountTextView = itemView.findViewById(R.id.nSaved);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(collectionsList.get(position));
                    }
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(String collectionName, int recipeIdsCount) {
            collectionNameTextView.setText(collectionName);
            recipeIdsCountTextView.setText(recipeIdsCount + " recipe(s) saved");
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String collectionName);
    }
}
