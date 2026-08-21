package com.example.tilldock.ui.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.StockMovement;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class MovementsBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "MovementsBottomSheet";
    private static final java.util.ArrayList<StockMovement> CURRENT = new java.util.ArrayList<>();

    public static void show(FragmentManager fm, List<StockMovement> movements) {
        MovementsBottomSheet sheet = new MovementsBottomSheet();
        CURRENT.clear();
        if (movements != null) CURRENT.addAll(movements);
        sheet.show(fm, TAG);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_movements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.movements_title);
        TextView empty = view.findViewById(R.id.movements_empty);
        RecyclerView recycler = view.findViewById(R.id.movements_recycler);
        title.setText(R.string.inventory_movements_title);
        if (CURRENT.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
            return;
        }
        empty.setVisibility(View.GONE);
        recycler.setVisibility(View.VISIBLE);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        MovementAdapter adapter = new MovementAdapter();
        recycler.setAdapter(adapter);
        adapter.submit(CURRENT);
    }

    static class MovementAdapter extends RecyclerView.Adapter<MovementAdapter.VH> {

        private final java.util.ArrayList<StockMovement> items = new java.util.ArrayList<>();

        void submit(List<StockMovement> next) {
            items.clear();
            if (next != null) items.addAll(next);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movement, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            StockMovement m = items.get(position);
            holder.type.setText(m.getMovementType());
            Integer delta = m.getDelta();
            String deltaText = delta == null ? "" : (delta >= 0 ? "+" + delta : String.valueOf(delta));
            holder.delta.setText(deltaText);
            holder.quantities.setText(holder.itemView.getContext().getString(R.string.inventory_movement_quantities,
                    m.getPreviousQuantity() == null ? 0 : m.getPreviousQuantity(),
                    m.getNewQuantity() == null ? 0 : m.getNewQuantity()));
            holder.reason.setText(m.getReason() == null ? "" : m.getReason());
            String created = m.getCreatedAt();
            holder.time.setText(created == null ? "" : created);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView type;
            final TextView delta;
            final TextView quantities;
            final TextView reason;
            final TextView time;

            VH(@NonNull View itemView) {
                super(itemView);
                type = itemView.findViewById(R.id.movement_item_type);
                delta = itemView.findViewById(R.id.movement_item_delta);
                quantities = itemView.findViewById(R.id.movement_item_quantities);
                reason = itemView.findViewById(R.id.movement_item_reason);
                time = itemView.findViewById(R.id.movement_item_time);
            }
        }
    }
}