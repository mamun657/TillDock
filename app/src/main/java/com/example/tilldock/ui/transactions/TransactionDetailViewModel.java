package com.example.tilldock.ui.transactions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.Sale;
import com.example.tilldock.data.repository.SalesRepository;
import com.example.tilldock.utils.ApiError;

public class TransactionDetailViewModel extends ViewModel {

    public enum Status { IDLE, LOADING, READY, ERROR }

    private final SalesRepository repository = TillDockApplication.get().getSalesRepository();

    private final MutableLiveData<Sale> sale = new MutableLiveData<>(null);
    private final MutableLiveData<Status> status = new MutableLiveData<>(Status.IDLE);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    public LiveData<Sale> sale() { return sale; }
    public LiveData<Status> status() { return status; }
    public LiveData<String> errorMessage() { return errorMessage; }

    public void load(String saleId) {
        if (saleId == null || saleId.isEmpty()) {
            errorMessage.setValue("Missing sale id");
            status.setValue(Status.ERROR);
            return;
        }
        status.postValue(Status.LOADING);
        errorMessage.postValue(null);
        repository.get(saleId, new SalesRepository.Callback<Sale>() {
            @Override
            public void onSuccess(Sale value) {
                sale.postValue(value);
                status.postValue(Status.READY);
            }

            @Override
            public void onFailure(ApiError error) {
                errorMessage.postValue(error.message());
                status.postValue(Status.ERROR);
            }
        });
    }
}
