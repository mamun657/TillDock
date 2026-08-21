package com.example.tilldock.data.repository;

import com.example.tilldock.data.api.InventoryApi;
import com.example.tilldock.data.model.InventoryItem;
import com.example.tilldock.data.model.StockAdjustmentRequest;
import com.example.tilldock.data.model.StockMovement;
import com.example.tilldock.data.model.StockMutationRequest;
import com.example.tilldock.data.model.ThresholdRequest;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Result;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

public class InventoryRepository extends BaseRepository {

    private final InventoryApi inventoryApi;

    public InventoryRepository(InventoryApi inventoryApi) {
        this.inventoryApi = inventoryApi;
    }

    public void list(Callback<List<InventoryItem>> callback) {
        IO.execute(() -> {
            try {
                Response<List<InventoryItem>> r = inventoryApi.list().execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void get(String productId, Callback<InventoryItem> callback) {
        IO.execute(() -> {
            try {
                Response<InventoryItem> r = inventoryApi.get(productId).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void stockIn(String productId, int quantity, String reason, Callback<InventoryItem> callback) {
        IO.execute(() -> {
            try {
                Response<InventoryItem> r = inventoryApi.stockIn(productId, new StockMutationRequest(quantity, reason)).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void stockOut(String productId, int quantity, String reason, Callback<InventoryItem> callback) {
        IO.execute(() -> {
            try {
                Response<InventoryItem> r = inventoryApi.stockOut(productId, new StockMutationRequest(quantity, reason)).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void adjust(String productId, int newQuantity, String reason, Callback<InventoryItem> callback) {
        IO.execute(() -> {
            try {
                Response<InventoryItem> r = inventoryApi.adjust(productId, new StockAdjustmentRequest(newQuantity, reason)).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void setThreshold(String productId, int threshold, Callback<InventoryItem> callback) {
        IO.execute(() -> {
            try {
                Response<InventoryItem> r = inventoryApi.setThreshold(productId, new ThresholdRequest(threshold)).execute();
                deliver(parseOk(r), callback);
            } catch (IOException e) {
                deliver(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliver(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void movements(String productId, int page, int size, Callback<List<StockMovement>> callback) {
        IO.execute(() -> {
            try {
                Response<List<StockMovement>> r = inventoryApi.movements(productId, page, size).execute();
                deliver(parseOk(r), callback);
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