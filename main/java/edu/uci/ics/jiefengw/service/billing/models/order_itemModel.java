package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class order_itemModel {
    /*
      EMAIL (string, required)
               MOVIE_ID (string, required)
               QUANTITY (int, required)
               UNIT_PRICE (float, required)
               DISCOUNT (float, required)
               SALE_DATE (string, required)

     */
    @JsonProperty(value = "email", required = true)
    public String email;
    @JsonProperty(value = "movie_id", required = true)
    public String movie_id;
    @JsonProperty(value = "quantity", required = true)
    public Integer quantity;
    @JsonProperty(value = "unit_price", required = true)
    public float unit_price;
    @JsonProperty(value = "discount", required = true)
    public float discount;
    @JsonProperty(value = "sale_date", required = true)
    public String sale_date;

    public order_itemModel(){}
    public order_itemModel(@JsonProperty(value = "email", required = true) String email,
                           @JsonProperty(value = "movie_id", required = true) String movie_id,
                           @JsonProperty(value = "quantity", required = true) Integer quantity,
                           @JsonProperty(value = "unit_price", required = true) float unit_price,
                           @JsonProperty(value = "discount", required = true) float discount,
                           @JsonProperty(value = "sale_date", required = true) String sale_date
                    ){
        this.email = email;
        this.movie_id = movie_id;
        this.quantity = quantity;
        this.unit_price = unit_price;
        this.discount = discount;
        this.sale_date = sale_date;
    }

    @JsonProperty("email")
    public String getEmail(){ return email; }
    @JsonProperty("email")
    public void setEmail(String email){ this.email = email; }

    @JsonProperty("movie_id")
    public String getMovie_id(){ return movie_id; }
    @JsonProperty("movie_id")
    public void setMovie_id(String movie_id){ this.movie_id = movie_id; }

    @JsonProperty("quantity")
    public Integer getQuantity() {
        return quantity;
    }
    @JsonProperty("quantity")
    public void setQuantity(Integer quantity){ this.quantity = quantity; }

    @JsonProperty("unit_price")
    public float getUnit_price(){ return unit_price; }
    @JsonProperty("unit_price")
    public void setUnit_price(float unit_price){ this.unit_price = unit_price; }

    @JsonProperty("discount")
    public float getDiscount(){ return discount; }
    @JsonProperty("discount")
    public void setDiscount(float discount){ this.discount = discount; }

    @JsonProperty("sale_date")
    public String getSale_date(){ return sale_date; }
    @JsonProperty("sale_date")
    public void setSale_date(String sale_date){ this.sale_date = sale_date; }


}
