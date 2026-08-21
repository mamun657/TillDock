package com.example.tilldock.data.api;

import com.example.tilldock.data.repository.TokenStore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private final Retrofit retrofit;
    private final AuthApi authApi;
    private final BusinessApi businessApi;
    private final CategoryApi categoryApi;
    private final ProductApi productApi;
    private final InventoryApi inventoryApi;

    public ApiClient(String baseUrl, TokenStore tokenStore) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenStore))
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        authApi = retrofit.create(AuthApi.class);
        businessApi = retrofit.create(BusinessApi.class);
        categoryApi = retrofit.create(CategoryApi.class);
        productApi = retrofit.create(ProductApi.class);
        inventoryApi = retrofit.create(InventoryApi.class);
    }

    public AuthApi authApi() {
        return authApi;
    }

    public BusinessApi businessApi() {
        return businessApi;
    }

    public CategoryApi categoryApi() {
        return categoryApi;
    }

    public ProductApi productApi() {
        return productApi;
    }

    public InventoryApi inventoryApi() {
        return inventoryApi;
    }

    public Retrofit retrofit() {
        return retrofit;
    }
}