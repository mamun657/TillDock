package com.example.tilldock.ui.categories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.Category;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface Listener {
        void onEdit(Category category);

        void onDelete(Category category);
    }

    private final List<Category> items = new ArrayList<>();
    private final Listener listener;

    public CategoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Category> next) {
        items.clear();
        if (next != null) items.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Category c = items.get(position);
        holder.name.setText(c.getName());
        if (c.getDescription() == null || c.getDescription().isEmpty()) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(c.getDescription());
        }
        holder.edit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(c);
        });
        holder.delete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(c);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView description;
        final MaterialButton edit;
        final MaterialButton delete;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.category_item_name);
            description = itemView.findViewById(R.id.category_item_description);
            edit = itemView.findViewById(R.id.category_item_edit);
            delete = itemView.findViewById(R.id.category_item_delete);
        }
    }
}