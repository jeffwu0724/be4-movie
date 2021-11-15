package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ResponseModel_cart_retrieve extends ResponseModel{
    @JsonProperty(value = "items")
    private ArrayList<itemModel> items;
    public ResponseModel_cart_retrieve(int resultCode, String message,ArrayList<itemModel> items) {
        super(resultCode, message);
        this.items = items;
    }
    public ResponseModel_cart_retrieve() { }

    @JsonProperty("items")
    public ArrayList<itemModel> getItems() {
        return items;
    }
    @JsonProperty("items")
    public void setItems(ArrayList<itemModel> items) {
        this.items = items;
    }
}
