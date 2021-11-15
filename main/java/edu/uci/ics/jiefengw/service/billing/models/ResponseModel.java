package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseModel {
    @JsonProperty(value = "resultCode", required = true)
    private int resultCode;
    @JsonProperty(value = "message", required = true)
    private String message;

    public ResponseModel(int resultCode, String message) {
        this.resultCode = resultCode;
        this.message = message;
    }
    public ResponseModel() { }

    @JsonProperty("resultCode")
    public int getResultCode() {
        return resultCode;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }


}
