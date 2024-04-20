package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.RecipeStepsResponse;

import java.util.List;

public class InstructionsAdapter extends RecyclerView.Adapter<InstructionsViewHolder> {

    Context context;
    List<RecipeStepsResponse> list;

    public InstructionsAdapter(Context context, List<RecipeStepsResponse> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public InstructionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InstructionsViewHolder(LayoutInflater.from(context).inflate(R.layout.list_instructions, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InstructionsViewHolder holder, int position) {
        holder.textView_steps_name.setText(list.get(position).name);
        holder.recycler_steps.setHasFixedSize(true);
        holder.recycler_steps.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        StepsAdapter stepsAdapter = new StepsAdapter(context, list.get(position).steps);
        holder.recycler_steps.setAdapter(stepsAdapter);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
class InstructionsViewHolder extends RecyclerView.ViewHolder {
    TextView textView_steps_name;
    RecyclerView recycler_steps;
    public InstructionsViewHolder(@NonNull View itemView) {
        super(itemView);
        textView_steps_name = itemView.findViewById(R.id.textView_instructions_name);
        recycler_steps = itemView.findViewById(R.id.recycler_instructions);
    }
}