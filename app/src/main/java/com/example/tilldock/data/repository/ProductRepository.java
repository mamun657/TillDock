package com.example.tilldock.data.repository;

import com.example.tilldock.data.api.ProductApi;
import com.example.tilldock.data.model.Product;
import com.example.tilldock.data.model.ProductRequest;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Result;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class ProductRepository extends BaseRepository {

    private final ProductApi productApi;

    public ProductRepository(ProductApi productApi) {
        this.productApi = productApi;
    }

    public void list(boolean includeArchived,
                     String query,
                     String categoryId,
                     String status,
                     String sort,
                     Callback<List<Product>> callback) {
        IO.execute(() -> {
            try {
                Response<List<Product>> r = productApi.list(includeArchived, query, categoryId, status, sort).execute();
                deliver(parseOkOrEmpty(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void get(String id, Callback<Product> callback) {
        IO.execute(() -> {
            try {
                Response<Product> r = productApi.get(id).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void create(ProductRequest request, Callback<Product> callback) {
        IO.execute(() -> {
            try {
                Response<Product> r = productApi.create(request).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void update(String id, ProductRequest request, Callback<Product> callback) {
        IO.execute(() -> {
            try {
                Response<Product> r = productApi.update(id, request).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void archive(String id, Callback<Product> callback) {
        IO.execute(() -> {
            try {
                Response<Product> r = productApi.archive(id).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void restore(String id, Callback<Product> callback) {
        IO.execute(() -> {
            try {
                Response<Product> r = productApi.restore(id).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void uploadImage(String id, byte[] bytes, String mimeType, String filename, Callback<Product> callback) {
        IO.execute(() -> {
            try {
                RequestBody body = RequestBody.create(bytes, MediaType.parse(mimeType));
                MultipartBody.Part part = MultipartBody.Part.createFormData("file", filename, body);
                Response<Product> r = productApi.uploadImage(id, part).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void deleteImage(String id, Callback<Product> callback) {
        IO.execute(() -> {
            try {
                Response<Product> r = productApi.deleteImage(id).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void setImageUrl(String id, String url, Callback<Product> callback) {
        IO.execute(() -> {
            try {
                RequestBody body = RequestBody.create(
                        "{\"imageUrl\":\"" + (url == null ? "" : url.replace("\"", "\\\"")) + "\"}",
                        MediaType.parse("application/json")
                );
                Response<Product> r = productApi.setImageUrl(id, body).execute();
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