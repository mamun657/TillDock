package com.example.tilldock.data.api;

import com.example.tilldock.data.model.InventoryItem;
import com.example.tilldock.data.model.StockAdjustmentRequest;
import com.example.tilldock.data.model.StockMovement;
import com.example.tilldock.data.model.StockMutationRequest;
import com.example.tilldock.data.model.ThresholdRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface InventoryApi {

    @GET("api/inventory")
    Call<List<InventoryItem>> list();

    @GET("api/inventory/{id}")
    Call<InventoryItem> get(@Path("id") String id);

    @POST("api/inventory/{id}/stock-in")
    Call<InventoryItem> stockIn(@Path("id") String id, @Body StockMutationRequest request);

    @POST("api/inventory/{id}/stock-out")
    Call<InventoryItem> stockOut(@Path("id") String id, @Body StockMutationRequest request);

    @POST("api/inventory/{id}/adjust")
    Call<InventoryItem> adjust(@Path("id") String id, @Body StockAdjustmentRequest request);

    @PATCH("api/inventory/{id}/threshold")
    Call<InventoryItem> setThreshold(@Path("id") String id, @Body ThresholdRequest request);

    @GET("api/inventory/{id}/movements")
    Call<List<StockMovement>> movements(@Path("id") String id, @Query("page") int page, @Query("size") int size);
}