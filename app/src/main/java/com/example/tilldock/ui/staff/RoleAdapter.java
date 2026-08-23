package com.example.tilldock.ui.staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.google.android.material.chip.Chip;

import java.util.List;

public class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.Holder> {

    private final List<StaffRolesFragment.Role> items;

    public RoleAdapter(List<StaffRolesFragment.Role> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_role, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        StaffRolesFragment.Role role = items.get(position);
        holder.name.setText(role.name);
        holder.chip.setText(role.description);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final Chip chip;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.role_name);
            chip = itemView.findViewById(R.id.role_chip);
        }
    }
}