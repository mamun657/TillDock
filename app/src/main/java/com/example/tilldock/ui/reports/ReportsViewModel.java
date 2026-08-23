package com.example.tilldock.ui.reports;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.Sale;
import com.example.tilldock.data.repository.SalesRepository;
import com.example.tilldock.utils.ApiError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ReportsViewModel extends ViewModel {

    public enum Period { DAY, WEEK, MONTH }

    public static class Summary {
        public BigDecimal totalSales = BigDecimal.ZERO;
        public BigDecimal totalTax = BigDecimal.ZERO;
        public int saleCount = 0;
        public BigDecimal avgTicket = BigDecimal.ZERO;
    }

    public static class ProductRow {
        public String productId;
        public String name;
        public int units;
        public BigDecimal revenue = BigDecimal.ZERO;
    }

    private final SalesRepository repository = TillDockApplication.get().getSalesRepository();

    private final MutableLiveData<Period> period = new MutableLiveData<>(Period.DAY);
    private final MutableLiveData<Summary> summary = new MutableLiveData<>(new Summary());
    private final MutableLiveData<List<ProductRow>> topProducts = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(Boolean.FALSE);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    private static final SimpleDateFormat[] PARSERS = new SimpleDateFormat[]{
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
    };

    static {
        for (SimpleDateFormat f : PARSERS) {
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
    }

    public LiveData<Period> period() { return period; }
    public LiveData<Summary> summary() { return summary; }
    public LiveData<List<ProductRow>> topProducts() { return topProducts; }
    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> errorMessage() { return errorMessage; }

    public void setPeriod(Period value) {
        period.setValue(value == null ? Period.DAY : value);
        recompute();
    }

    public void load() {
        loading.postValue(Boolean.TRUE);
        errorMessage.postValue(null);
        repository.list(null, new SalesRepository.Callback<List<Sale>>() {
            @Override
            public void onSuccess(List<Sale> value) {
                loading.postValue(Boolean.FALSE);
                List<Sale> all = value == null ? Collections.emptyList() : value;
                cache = all;
                // recompute() uses setValue() which must run on the main thread.
                // Switch to main thread before recomputing after async load completes.
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> recompute());
            }

            @Override
            public void onFailure(ApiError error) {
                loading.postValue(Boolean.FALSE);
                errorMessage.postValue(error.message());
            }
        });
    }

    private List<Sale> cache = Collections.emptyList();

    private void recompute() {
        Period p = period.getValue();
        if (p == null) p = Period.DAY;
        long[] range = rangeFor(p);
        List<Sale> filtered = new ArrayList<>();
        for (Sale s : cache) {
            Date d = parseDate(s.getCreatedAt());
            if (d == null) continue;
            long t = d.getTime();
            if (t >= range[0] && t <= range[1]) filtered.add(s);
        }

        Summary s = new Summary();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (Sale sale : filtered) {
            total = total.add(sale.getTotal() == null ? BigDecimal.ZERO : sale.getTotal());
            tax = tax.add(sale.getTax() == null ? BigDecimal.ZERO : sale.getTax());
        }
        s.totalSales = total;
        s.totalTax = tax;
        s.saleCount = filtered.size();
        if (s.saleCount > 0) {
            s.avgTicket = total.divide(BigDecimal.valueOf(s.saleCount), 2, RoundingMode.HALF_UP);
        }
        summary.setValue(s);

        Map<String, ProductRow> agg = new HashMap<>();
        for (Sale sale : filtered) {
            if (sale.getItems() == null) continue;
            for (com.example.tilldock.data.model.SaleItem item : sale.getItems()) {
                String id = item.getProductId() == null ? (item.getProductName() == null ? "unknown" : item.getProductName()) : item.getProductId();
                ProductRow row = agg.get(id);
                if (row == null) {
                    row = new ProductRow();
                    row.productId = id;
                    row.name = item.getProductName() == null ? "—" : item.getProductName();
                    agg.put(id, row);
                }
                int qty = item.getQuantity() == null ? 0 : item.getQuantity();
                row.units += qty;
                BigDecimal unit = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
                row.revenue = row.revenue.add(unit.multiply(BigDecimal.valueOf(qty)));
            }
        }

        List<ProductRow> rows = new ArrayList<>(agg.values());
        Collections.sort(rows, new Comparator<ProductRow>() {
            @Override
            public int compare(ProductRow a, ProductRow b) {
                return b.revenue.compareTo(a.revenue);
            }
        });
        if (rows.size() > 10) rows = rows.subList(0, 10);
        topProducts.setValue(rows);
    }

    private static long[] rangeFor(Period period) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();
        long end = System.currentTimeMillis();
        if (period == Period.WEEK) {
            c.add(Calendar.DAY_OF_YEAR, -6);
            start = c.getTimeInMillis();
        } else if (period == Period.MONTH) {
            c.add(Calendar.DAY_OF_YEAR, -29);
            start = c.getTimeInMillis();
        }
        return new long[]{start, end};
    }

    private static Date parseDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        for (SimpleDateFormat f : PARSERS) {
            try {
                return f.parse(raw);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }
}
