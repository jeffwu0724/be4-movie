package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class RequestModel_thumbnail {
    @JsonProperty(value = "movie_ids", required = true)
    private ArrayList<String> movie_id;

    @JsonCreator
    public RequestModel_thumbnail(  @JsonProperty(value = "movie_ids", required = true) ArrayList<String> movie_id) {
        this.movie_id = movie_id;
    }
    public RequestModel_thumbnail() {}

    @JsonProperty("movie_ids")
    public ArrayList<String> getMovie_id() {
        return movie_id;
    }
    @JsonProperty("movie_ids")
    public void setMovie_id(ArrayList<String> movie_id) {
        this.movie_id =  movie_id;
    }
}
