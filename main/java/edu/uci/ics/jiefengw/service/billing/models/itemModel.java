package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class itemModel {
    /*
    EMAIL (string, required)

    UNIT_PRICE (float, required)

    DISCOUNT (float, required)

    QUANTITY (integer, required)

    MOVIE_ID (string, required):

    MOVIE_TITLE (string, required)

    BACKDROP_PATH (string, optional)

    POSTER_PATH (string, optional)

     */
    @JsonProperty(value = "email", required = true)
    public String email;
    @JsonProperty(value = "unit_price", required = true)
    public float unit_price;
    @JsonProperty(value = "discount", required = true)
    public float discount;
    @JsonProperty(value = "quantity", required = true)
    public Integer quantity;
    @JsonProperty(value = "movie_id", required = true)
    public String movie_id;
    @JsonProperty(value = "movie_title", required = true)
    public String movie_title;
    @JsonProperty(value = "backdrop_path")
    public String backdrop_path;
    @JsonProperty(value = "poster_path")
    public String poster_path;

    public itemModel(){}
    public itemModel(@JsonProperty(value = "email", required = true) String email,
                     @JsonProperty(value = "unit_price", required = true) float unit_price,
                     @JsonProperty(value = "discount", required = true) float discount,
                     @JsonProperty(value = "quantity", required = true) Integer quantity,
                     @JsonProperty(value = "movie_id", required = true) String movie_id,
                     @JsonProperty(value = "movie_title", required = true) String movie_title,
                     @JsonProperty(value = "backdrop_path") String backdrop_path,
                     @JsonProperty(value = "poster_path") String poster_path){
        this.email = email;
        this.unit_price = unit_price;
        this.discount = discount;
        this.quantity = quantity;
        this.movie_id = movie_id;
        this.movie_title = movie_title;
        this.backdrop_path = backdrop_path;
        this.poster_path = poster_path;
    }

    @JsonProperty("email")
    public String getEmail(){ return email; }
    @JsonProperty("email")
    public void setEmail(String email){ this.email = email; }

    @JsonProperty("unit_price")
    public float getUnit_price(){ return unit_price; }
    @JsonProperty("unit_price")
    public void setUnit_price(float unit_price){ this.unit_price = unit_price; }

    @JsonProperty("discount")
    public float getDiscount(){ return discount; }
    @JsonProperty("discount")
    public void setDiscount(float discount){ this.discount = discount; }

    @JsonProperty("quantity")
    public Integer getQuantity() {
        return quantity;
    }
    @JsonProperty("quantity")
    public void setQuantity(Integer quantity){ this.quantity = quantity; }

    @JsonProperty("movie_id")
    public String getMovie_id(){ return movie_id; }
    @JsonProperty("movie_id")
    public void setMovie_id(String movie_id){ this.movie_id = movie_id; }

    @JsonProperty("movie_title")
    public String getMovie_title(){ return movie_title; }
    @JsonProperty("movie_title")
    public void setMovie_title(String movie_title){ this.movie_title = movie_title; }

    @JsonProperty("backdrop_path")
    public String getBackdrop_path(){ return backdrop_path; }
    @JsonProperty("backdrop_path")
    public void setBackdrop_path(String backdrop_path){ this.backdrop_path = backdrop_path; }

    @JsonProperty("poster_path")
    public String getPoster_path(){ return poster_path; }
    @JsonProperty("poster_path")
    public void setPoster_path(String poster_path){ this.poster_path = poster_path; }

}
