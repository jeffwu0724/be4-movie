package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class transactionModel {
    /*
     CAPTURE_ID(string, required)
          STATE (string, required)

         AMOUNT
               TOTAL (string, required)
               CURRENCY (string, required)

         TRANSACTION_FEE
               VALUE (string, required)
               CURRENCY (string, required)

          CREATE_TIME (string, required)
          UPDATE_TIME (string, required)

          ITEMS (itemModel[], optional)
               EMAIL (string, required)
               MOVIE_ID (string, required)
               QUANTITY (int, required)
               UNIT_PRICE (float, required)
               DISCOUNT (float, required)
               SALE_DATE (string, required)

     */
    @JsonProperty(value = "capture_id", required = true)
    public String capture_id;
    @JsonProperty(value = "state", required = true)
    public String state;
    @JsonProperty(value = "amount", required = true)
    public amountModel amount;
    @JsonProperty(value = "transaction_fee", required = true)
    public transaction_feeModel transaction_fee;

    @JsonProperty(value = "create_time")
    public String create_time;
    @JsonProperty(value = "update_time")
    public String update_time;

    @JsonProperty(value = "items", required = true)
    public ArrayList<order_itemModel> items;

    public transactionModel(){}
    public transactionModel( @JsonProperty(value = "capture_id", required = true) String capture_id,
                             @JsonProperty(value = "state", required = true) String state,
                             @JsonProperty(value = "amount", required = true) amountModel amount,
                             @JsonProperty(value = "transaction_fee", required = true) transaction_feeModel transaction_fee,
                             @JsonProperty(value = "create_time") String create_time,
                             @JsonProperty(value = "update_time") String update_time,
                             @JsonProperty(value = "items", required = true) ArrayList<order_itemModel> items){

       this.capture_id = capture_id;
       this.state = state;
       this.amount = amount;
       this.transaction_fee = transaction_fee;
       this.create_time = create_time;
       this.update_time = update_time;
       this.items = items;
    }

    @JsonProperty("capture_id")
    public String getCapture_id() {return capture_id;}
    @JsonProperty("capture_id")
    public void setCapture_id(String capture_id) {this.capture_id = capture_id;}

    @JsonProperty("state")
    public String getState(){ return state; }
    @JsonProperty("state")
    public void setState(String state){ this.state = state; }

    @JsonProperty("amount")
    public amountModel getAmount() {return amount;}
    @JsonProperty("amount")
    public void setAmount(amountModel amount) {this.amount = amount;}

    @JsonProperty("transaction_fee")
    public transaction_feeModel getTransaction_fee(){ return transaction_fee; }
    @JsonProperty("transaction_fee")
    public void setTransaction_fee(transaction_feeModel transaction_fee){ this.transaction_fee = transaction_fee; }


    @JsonProperty("create_time")
    public String getCreate_time() {return create_time;}
    @JsonProperty("create_time")
    public void setCreate_time(String create_time) {this.create_time = create_time;}

    @JsonProperty("update_time")
    public String getUpdate_time(){ return update_time; }
    @JsonProperty("update_time")
    public void setUpdate_time(String update_time){ this.update_time = update_time; }

    @JsonProperty("items")
    public ArrayList<order_itemModel> getItems(){ return items; }
    @JsonProperty("items")
    public void setItems(ArrayList<order_itemModel> items){ this.items = items; }

}
