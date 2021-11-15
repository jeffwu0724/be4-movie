package edu.uci.ics.jiefengw.service.billing.models;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestModel_cart_insert extends RequestModel{
    @JsonCreator
    public RequestModel_cart_insert(  @JsonProperty(value = "email", required = true) String email,
                          @JsonProperty(value = "movie_id", required = true) String movie_id,
                          @JsonProperty(value = "quantity") Integer quantity) {

        super(email, movie_id, quantity);
    }

    public RequestModel_cart_insert() {}
}
