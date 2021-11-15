package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ResponseModel_order_retrieve extends ResponseModel{
    @JsonProperty(value = "transactions")
    private ArrayList<transactionModel> transactions;
    public ResponseModel_order_retrieve(int resultCode, String message,ArrayList<transactionModel> transactions) {
        super(resultCode, message);
        this.transactions = transactions;
    }
    public ResponseModel_order_retrieve() { }

    @JsonProperty("transactions")
    public ArrayList<transactionModel> getTransactions() {
        return transactions;
    }
    @JsonProperty("transactions")
    public void setTransactions(ArrayList<transactionModel> transactions) {
        this.transactions = transactions;
    }
}
