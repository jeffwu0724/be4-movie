package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseModel_cart_update extends ResponseModel{
    public ResponseModel_cart_update(int resultCode, String message) {
        super(resultCode, message);
    }
    public ResponseModel_cart_update() { }

}
