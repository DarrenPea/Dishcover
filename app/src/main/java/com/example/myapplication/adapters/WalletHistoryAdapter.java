package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.WalletAction;

import java.util.ArrayList;

public class WalletHistoryAdapter extends RecyclerView.Adapter<WalletHistoryAdapter.ViewHolder> {
    private ArrayList<WalletAction> walletActions;

    public WalletHistoryAdapter(ArrayList<WalletAction> walletActions) {
        this.walletActions = walletActions;
    }

    public void setData(ArrayList<WalletAction> walletActions) {
        this.walletActions = walletActions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WalletAction action = walletActions.get(position);
        holder.bind(action);
    }

    @Override
    public int getItemCount() {
        return walletActions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView actionTextView, timestampTextView, pointsGainedTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            actionTextView = itemView.findViewById(R.id.action_text);
            pointsGainedTextView = itemView.findViewById(R.id.points_gained_text);
            timestampTextView = itemView.findViewById(R.id.timestamp_text);
        }

        @SuppressLint("SetTextI18n")
        public void bind(WalletAction action) {
            actionTextView.setText(action.getAction());
            pointsGainedTextView.setText("+" + action.getPointsGained());
            timestampTextView.setText(action.getFormattedTimestamp());
        }
    }
}

