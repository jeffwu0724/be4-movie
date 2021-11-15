package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestModel_cart_delete extends RequestModel {
    @JsonCreator
    public RequestModel_cart_delete(  @JsonProperty(value = "email", required = true) String email,
                                      @JsonProperty(value = "movie_id", required = true) String movie_id) {
        super(email, movie_id);
    }

    public RequestModel_cart_delete() {}
}
