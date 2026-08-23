package com.example.tilldock.ui.transactions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.Sale;
import com.example.tilldock.data.repository.SalesRepository;
import com.example.tilldock.utils.ApiError;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TransactionsViewModel extends ViewModel {

    public enum Filter { ALL, TODAY, WEEK, MONTH }
    public enum Status { IDLE, LOADING, READY, ERROR }

    private final SalesRepository repository = TillDockApplication.get().getSalesRepository();

    private final MutableLiveData<Filter> filter = new MutableLiveData<>(Filter.ALL);
    private final MutableLiveData<List<Sale>> sales = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Status> status = new MutableLiveData<>(Status.IDLE);
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

    public LiveData<Filter> filter() { return filter; }
    public LiveData<List<Sale>> sales() { return sales; }
    public LiveData<Status> status() { return status; }
    public LiveData<String> errorMessage() { return errorMessage; }

    public void setFilter(Filter value) {
        filter.setValue(value == null ? Filter.ALL : value);
        applyFilter();
    }

    public void load() {
        status.postValue(Status.LOADING);
        errorMessage.postValue(null);
        repository.list(null, new SalesRepository.Callback<List<Sale>>() {
            @Override
            public void onSuccess(List<Sale> value) {
                sales.postValue(value == null ? Collections.emptyList() : value);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> applyFilter());
            }

            @Override
            public void onFailure(ApiError error) {
                errorMessage.postValue(error.message());
                status.postValue(Status.ERROR);
            }
        });
    }

    private void applyFilter() {
        Filter f = filter.getValue();
        if (f == null) f = Filter.ALL;
        List<Sale> raw = sales.getValue();
        if (raw == null) raw = Collections.emptyList();
        if (f == Filter.ALL) {
            sales.setValue(raw);
            status.setValue(Status.READY);
            return;
        }
        long[] range = rangeFor(f);
        List<Sale> out = new ArrayList<>();
        for (Sale s : raw) {
            Date d = parseDate(s.getCreatedAt());
            if (d == null) continue;
            long t = d.getTime();
            if (t >= range[0] && t <= range[1]) {
                out.add(s);
            }
        }
        sales.setValue(out);
        status.setValue(Status.READY);
    }

    private static long[] rangeFor(Filter filter) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();
        long end = System.currentTimeMillis();
        if (filter == Filter.WEEK) {
            c.add(Calendar.DAY_OF_YEAR, -6);
            start = c.getTimeInMillis();
        } else if (filter == Filter.MONTH) {
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
