package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class thumbnailModel {
        @JsonProperty(value = "movie_id", required = true)
        public String movie_id;
        @JsonProperty(value = "title", required = true)
        public String title;
        @JsonProperty(value = "backdrop_path")
        public String backdrop_path;
        @JsonProperty(value = "poster_path")
        public String poster_path;

        public thumbnailModel() {}
        @JsonCreator
        public thumbnailModel(   //@JsonProperty(value = "movieS", required = true) movieModel movieS,
                                 @JsonProperty(value = "movie_id", required = true) String movie_id,
                                 @JsonProperty(value = "title", required = true) String title,
                                 @JsonProperty(value = "backdrop_path") String backdrop_path,
                                 @JsonProperty(value = "poster_path") String poster_path
        ) {
            // this.movieS = movieS;
            this.movie_id = movie_id;
            this.title = title;
            this.backdrop_path = backdrop_path;
            this.poster_path = poster_path;
        }

        @JsonProperty("movie_id")
        public String getMovie_id(){ return movie_id; }
        @JsonProperty("movie_id")
        public void setMovie_id(String movie_id){ this.movie_id = movie_id; }

        @JsonProperty("title")
        public String getTitle(){ return title; }
        @JsonProperty("title")
        public void setTitle(String title){ this.title = title; }

        @JsonProperty("backdrop_path")
        public String getBackdrop_path(){ return backdrop_path; }
        @JsonProperty("backdrop_path")
        public void setBackdrop_path(String backdrop_path){ this.backdrop_path = backdrop_path; }

        @JsonProperty("poster_path")
        public String getPoster_path(){ return poster_path; }
        @JsonProperty("poster_path")
        public void setPoster_path(String poster_path){ this.poster_path = poster_path; }
    }


