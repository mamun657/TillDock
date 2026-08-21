package com.example.tilldock.data.repository;

import com.example.tilldock.data.api.CategoryApi;
import com.example.tilldock.data.model.Category;
import com.example.tilldock.data.model.CategoryRequest;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Result;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

public class CategoryRepository extends BaseRepository {

    private final CategoryApi categoryApi;

    public CategoryRepository(CategoryApi categoryApi) {
        this.categoryApi = categoryApi;
    }

    public void list(Callback<List<Category>> callback) {
        IO.execute(() -> {
            try {
                Response<List<Category>> r = categoryApi.list().execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void create(CategoryRequest request, Callback<Category> callback) {
        IO.execute(() -> {
            try {
                Response<Category> r = categoryApi.create(request).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void update(String id, CategoryRequest request, Callback<Category> callback) {
        IO.execute(() -> {
            try {
                Response<Category> r = categoryApi.update(id, request).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void delete(String id, Callback<Void> callback) {
        IO.execute(() -> {
            try {
                Response<Void> r = categoryApi.delete(id).execute();
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