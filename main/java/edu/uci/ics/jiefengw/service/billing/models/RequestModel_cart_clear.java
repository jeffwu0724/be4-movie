package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestModel_cart_clear extends RequestModel{
    @JsonCreator
    public RequestModel_cart_clear(  @JsonProperty(value = "email", required = true) String email) {
        super(email);
    }

    public RequestModel_cart_clear() {}
}
