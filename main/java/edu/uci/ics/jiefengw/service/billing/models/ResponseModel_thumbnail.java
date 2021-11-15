package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ResponseModel_thumbnail {
    @JsonProperty(value = "resultCode", required = true)
    private int resultCode;
    @JsonProperty(value = "message", required = true)
    private String message;
    @JsonProperty(value = "thumbnails")
    ArrayList<thumbnailModel> thumbNail = new ArrayList<thumbnailModel>();

    public ResponseModel_thumbnail(int resultCode, String message,  ArrayList<thumbnailModel> thumbNail) {

        this.resultCode = resultCode;
        this.message = message;
        this.thumbNail = thumbNail;
    }
    public ResponseModel_thumbnail() { }

    @JsonProperty("resultCode")
    public int getResultCode() {
        return resultCode;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("thumbnails")
    public ArrayList<thumbnailModel> getThumbNailList(){
        return thumbNail;
    }
    @JsonProperty("thumbnails")
    public void setThumbNailList(ArrayList<thumbnailModel> thumbNail){
        this.thumbNail = thumbNail;
    }
}
