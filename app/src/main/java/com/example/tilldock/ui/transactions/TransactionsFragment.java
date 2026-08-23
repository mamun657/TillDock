package com.example.tilldock.ui.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.Sale;
import com.example.tilldock.ui.Nav;
import com.google.android.material.chip.Chip;

import java.util.Collections;

public class TransactionsFragment extends Fragment implements TransactionAdapter.Listener {

    private TransactionsViewModel viewModel;
    private TransactionAdapter adapter;
    private RecyclerView recycler;
    private View emptyView;
    private ProgressBar progress;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = TillDockViewModelProvider.get(this, TransactionsViewModel.class);

        recycler = view.findViewById(R.id.txn_recycler);
        emptyView = view.findViewById(R.id.txn_empty);
        progress = view.findViewById(R.id.txn_progress);


        adapter = new TransactionAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        Chip chipAll = view.findViewById(R.id.txn_filter_all);
        Chip chipToday = view.findViewById(R.id.txn_filter_today);
        Chip chipWeek = view.findViewById(R.id.txn_filter_week);
        Chip chipMonth = view.findViewById(R.id.txn_filter_month);
        wire(chipAll, TransactionsViewModel.Filter.ALL);
        wire(chipToday, TransactionsViewModel.Filter.TODAY);
        wire(chipWeek, TransactionsViewModel.Filter.WEEK);
        wire(chipMonth, TransactionsViewModel.Filter.MONTH);

        viewModel.status().observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;
            progress.setVisibility(status == TransactionsViewModel.Status.LOADING ? View.VISIBLE : View.GONE);
        });
        viewModel.sales().observe(getViewLifecycleOwner(), data -> {
            adapter.submit(data == null ? Collections.emptyList() : data);
            boolean empty = data == null || data.isEmpty();
            recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
            emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.errorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    

        viewModel.load();
    }

    private void wire(Chip chip, TransactionsViewModel.Filter filter) {
        if (chip == null) return;
        chip.setOnClickListener(v -> viewModel.setFilter(filter));
    }

    @Override
    public void onTransactionSelected(Sale sale) {
        if (sale == null || sale.getId() == null) return;
        com.example.tilldock.ui.Nav.showTransactionDetail(requireActivity(), sale.getId());
    }
}
