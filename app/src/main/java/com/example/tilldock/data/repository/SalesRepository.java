package com.example.tilldock.data.repository;

import com.example.tilldock.data.api.SalesApi;
import com.example.tilldock.data.model.Sale;
import com.example.tilldock.data.model.SaleReport;
import com.example.tilldock.data.model.SaleRequest;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Result;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Response;

public class SalesRepository extends BaseRepository {

    private final SalesApi salesApi;

    public SalesRepository(SalesApi salesApi) {
        this.salesApi = salesApi;
    }

    public void create(SaleRequest request, Callback<Sale> callback) {
        IO.execute(() -> {
            try {
                Response<Sale> r = salesApi.create(request).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void list(String businessId, Callback<List<Sale>> callback) {
        IO.execute(() -> {
            try {
                Response<List<Sale>> r = salesApi.list(businessId).execute();
                deliver(parseOkOrEmpty(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void get(String id, Callback<Sale> callback) {
        IO.execute(() -> {
            try {
                Response<Sale> r = salesApi.get(id).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void report(String period, Callback<SaleReport> callback) {
        IO.execute(() -> {
            try {
                Response<SaleReport> r = salesApi.report(period).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    private <T> Result<T> parseOkOrEmpty(Response<T> response) {
        if (response.isSuccessful()) {
            T body = response.body();
            if (body == null) {
                body = (T) Collections.emptyList();
            }
            return Result.success(body);
        }
        String raw = null;
        try {
            if (response.errorBody() != null) {
                raw = response.errorBody().string();
            }
        } catch (IOException ignored) {
        }
        return Result.failure(parseError(response.code(), raw));
    }

    public interface Callback<T> {
        void onSuccess(T value);

        void onFailure(ApiError error);
    }

    private <T> void deliver(Result<T> result, Callback<T> callback) {
        if (callback == null) return;
        if (result.isSuccess()) {
            callback.onSuccess(result.value());
        } else {
            callback.onFailure(result.error());
        }
    }
}
