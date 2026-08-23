package com.example.tilldock.ui.sales;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.PaymentMethod;
import com.example.tilldock.data.model.Sale;

import java.math.BigDecimal;

public class PaymentViewModel {

    public enum Status { IDLE, PROCESSING, SUCCESS, ERROR }

    private final NewSaleViewModel saleViewModel;

    private final MutableLiveData<Status> status = new MutableLiveData<>(Status.IDLE);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Sale> completedSale = new MutableLiveData<>(null);

    public PaymentViewModel() {
        this.saleViewModel = TillDockApplication.get().newSaleViewModel();
    }

    public LiveData<Status> status() { return status; }
    public LiveData<String> errorMessage() { return errorMessage; }
    public LiveData<Sale> completedSale() { return completedSale; }

    public void startSubmit(PaymentMethod method, BigDecimal cashReceived) {
        saleViewModel.setPaymentMethod(method);
        saleViewModel.setCashReceived(cashReceived);
        status.setValue(Status.PROCESSING);
        errorMessage.setValue(null);
        saleViewModel.submit();
    }

    public void onSaleSuccess(Sale sale) {
        status.setValue(Status.SUCCESS);
        completedSale.setValue(sale);
    }

    public void onSaleError(String message) {
        status.setValue(Status.ERROR);
        errorMessage.setValue(message);
    }

    public void reset() {
        status.setValue(Status.IDLE);
        errorMessage.setValue(null);
        completedSale.setValue(null);
    }
}
