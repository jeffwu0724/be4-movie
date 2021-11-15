package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ResponseModel_order_place extends ResponseModel{
    @JsonProperty(value = "approve_url")
    private String approve_url;

    @JsonProperty(value = "token")
    private String token;

    public ResponseModel_order_place(int resultCode, String message,String approve_url,String token) {
        super(resultCode, message);
        this.approve_url = approve_url;
        this.token = token;
    }
    public ResponseModel_order_place() { }

    @JsonProperty("approve_url")
    public String getApprove_url() {
        return approve_url;
    }
    @JsonProperty("approve_url")
    public void setApprove_url(String approve_url) {
        this.approve_url = approve_url;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }
    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }
}
