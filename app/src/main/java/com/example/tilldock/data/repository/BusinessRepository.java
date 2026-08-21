package com.example.tilldock.data.repository;

import com.example.tilldock.data.api.BusinessApi;
import com.example.tilldock.data.model.Business;
import com.example.tilldock.data.model.BusinessRequest;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Result;

import java.io.IOException;

import retrofit2.Response;

public class BusinessRepository extends BaseRepository {

    private final BusinessApi businessApi;

    public BusinessRepository(BusinessApi businessApi) {
        this.businessApi = businessApi;
    }

    public void get(Callback<Business> callback) {
        IO.execute(() -> {
            try {
                Response<Business> r = businessApi.get().execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void upsert(BusinessRequest request, Callback<Business> callback) {
        IO.execute(() -> {
            try {
                Response<Business> r = businessApi.upsert(request).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void delete(Callback<Void> callback) {
        IO.execute(() -> {
            try {
                Response<Void> r = businessApi.delete().execute();
                deliver(parseVoid(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
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