package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class amountModel {
    //Amount
    @JsonProperty(value = "total", required = true)
    public String total;
    @JsonProperty(value = "currency", required = true)
    public String currency;

    public amountModel(){}
    public amountModel(@JsonProperty(value = "total", required = true) String total,
                       @JsonProperty(value = "currency", required = true) String currency){
        this.total = total;
        this.currency = currency;
    }

    @JsonProperty("total")
    public String getTotal(){ return total; }
    @JsonProperty("total")
    public void setTotal(String total){ this.total = total; }

    @JsonProperty("currency")
    public String getCurrency(){ return currency; }
    @JsonProperty("currency")
    public void setCurrency(String currency){ this.currency = currency; }
}
