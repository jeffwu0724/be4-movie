package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ResponseModel_discount_apply extends ResponseModel {
    @JsonProperty(value = "items")
    private ArrayList<itemModel> items;
    public ResponseModel_discount_apply(int resultCode, String message,ArrayList<itemModel> items) {
        super(resultCode, message);
        this.items = items;
    }
    public ResponseModel_discount_apply() { }

    @JsonProperty("items")
    public ArrayList<itemModel> getItems() {
        return items;
    }
    @JsonProperty("items")
    public void setItems(ArrayList<itemModel> items) {
        this.items = items;
    }
}
