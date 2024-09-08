package com.onurelustu.kisilerimjava;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.onurelustu.kisilerimjava.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class RehberAdapter extends RecyclerView.Adapter<RehberAdapter.RehberHolder> {

    ArrayList<rehber> rehberArrayList;

    public RehberAdapter(ArrayList<rehber> rehberArrayList) {
        this.rehberArrayList = rehberArrayList;
    }

    @NonNull
    @Override
    public RehberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new RehberHolder(recyclerRowBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull RehberAdapter.RehberHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(rehberArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),KisilerimAktivitesi.class);
                intent.putExtra("ingo","old");
                intent.putExtra("RehberId",rehberArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return rehberArrayList.size();
    }

    public class RehberHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;
       public RehberHolder(RecyclerRowBinding binding){
           super(binding.getRoot());
           this.binding=binding;
       }
    }


}
