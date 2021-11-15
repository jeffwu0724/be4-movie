package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseModel_cart_insert extends  ResponseModel{
    public ResponseModel_cart_insert(int resultCode, String message) {
        super(resultCode, message);
    }
    public ResponseModel_cart_insert() { }
}
