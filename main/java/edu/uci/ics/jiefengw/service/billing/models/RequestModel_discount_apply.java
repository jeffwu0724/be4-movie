package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestModel_discount_apply extends RequestModel {
    @JsonProperty(value = "discount_code", required = true)
    private String discount_code;

    @JsonCreator
    public RequestModel_discount_apply(  @JsonProperty(value = "email", required = true) String email,
                                         @JsonProperty(value = "discount_code", required = true) String discount_code
                                        ) {
        super(email);
        this.discount_code = discount_code;
    }

    public RequestModel_discount_apply() {}

    @JsonProperty("discount_code")
    public String getDiscount_code() {
        return discount_code;
    }
}
