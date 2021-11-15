package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class transaction_feeModel {
    //Transaction fee
    @JsonProperty(value = "value", required = true)
    public String value;
    @JsonProperty(value = "currency", required = true)
    public String currency;

    public transaction_feeModel(){}
    public transaction_feeModel(@JsonProperty(value = "value", required = true) String value,
                                @JsonProperty(value = "currency", required = true) String currency){
        this.value = value;
        this.currency = currency;
    }

    @JsonProperty("value")
    public String getValue(){ return value; }
    @JsonProperty("value")
    public void setValue(String value){ this.value = value; }

    @JsonProperty("currency")
    public String getCurrency(){ return currency; }
    @JsonProperty("currency")
    public void setCurrency(String currency){ this.currency = currency; }
}
