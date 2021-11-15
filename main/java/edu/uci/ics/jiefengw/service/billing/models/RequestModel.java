package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestModel {
    @JsonProperty(value = "email", required = true)
    private String email;
    @JsonProperty(value = "movie_id", required = true)
    private String movie_id;
    @JsonProperty(value = "quantity")
    private Integer quantity;

    @JsonCreator
    public RequestModel(  @JsonProperty(value = "email", required = true) String email,
                          @JsonProperty(value = "movie_id", required = true) String movie_id,
                          @JsonProperty(value = "quantity") Integer quantity) {

        this.email = email;
        this.movie_id = movie_id;
        this.quantity = quantity;
    }

    public RequestModel() {}

    public RequestModel(String email, String movie_id) {
        this.email = email;
        this.movie_id = movie_id;
    }

    public RequestModel(String email) {
        this.email = email;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("movie_id")
    public String getMovie_id() {
        return movie_id;
    }

    @JsonProperty("quantity")
    public Integer getQuantity() {
        return quantity;
    }


}








