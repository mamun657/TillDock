package com.example.tilldock.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.google.android.material.chip.Chip;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private ReportsViewModel viewModel;
    private TopProductAdapter adapter;
    private TextView kpiSales;
    private TextView kpiCount;
    private TextView kpiAvg;
    private TextView kpiTax;
    private ProgressBar progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        RecyclerView recycler = view.findViewById(R.id.reports_top_products);
        adapter = new TopProductAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        kpiSales = view.findViewById(R.id.reports_kpi_sales);
        kpiCount = view.findViewById(R.id.reports_kpi_count);
        kpiAvg = view.findViewById(R.id.reports_kpi_avg);
        kpiTax = view.findViewById(R.id.reports_kpi_tax);
        progress = view.findViewById(R.id.reports_progress);

        wire(view.findViewById(R.id.reports_period_day), ReportsViewModel.Period.DAY);
        wire(view.findViewById(R.id.reports_period_week), ReportsViewModel.Period.WEEK);
        wire(view.findViewById(R.id.reports_period_month), ReportsViewModel.Period.MONTH);

        viewModel.loading().observe(getViewLifecycleOwner(), l -> progress.setVisibility(Boolean.TRUE.equals(l) ? View.VISIBLE : View.GONE));
        viewModel.summary().observe(getViewLifecycleOwner(), s -> {
            if (s == null) return;
            kpiSales.setText(formatMoney(s.totalSales));
            kpiCount.setText(String.format(Locale.US, "%d", s.saleCount));
            kpiAvg.setText(formatMoney(s.avgTicket));
            kpiTax.setText(formatMoney(s.totalTax));
        });
        viewModel.topProducts().observe(getViewLifecycleOwner(), adapter::submit);
        viewModel.errorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                kpiSales.setText("—");
            }
        });

        viewModel.load();
    }

    private void wire(Chip chip, ReportsViewModel.Period period) {
        if (chip == null) return;
        chip.setOnClickListener(v -> viewModel.setPeriod(period));
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return String.format(Locale.US, "$%s", value.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }
}
