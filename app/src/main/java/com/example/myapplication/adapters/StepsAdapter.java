package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Step;

import java.util.List;

public class StepsAdapter extends RecyclerView.Adapter<StepsViewHolder>{

    Context context;
    List<Step> list;

    public StepsAdapter(Context context, List<Step> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public StepsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StepsViewHolder(LayoutInflater.from(context).inflate((R.layout.list_steps), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StepsViewHolder holder, int position) {

        holder.textView_step_number.setText(String.valueOf(list.get(position).number));
        holder.textView_step_title.setText(list.get(position).step);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
class StepsViewHolder extends RecyclerView.ViewHolder {
    TextView textView_step_number, textView_step_title;

    public StepsViewHolder(@NonNull View itemView) {
        super(itemView);

        textView_step_number = itemView.findViewById(R.id.textView_step_number);
        textView_step_title = itemView.findViewById(R.id.textView_step_title);
    }
}