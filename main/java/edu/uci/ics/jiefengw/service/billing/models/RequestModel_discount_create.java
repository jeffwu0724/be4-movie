package edu.uci.ics.jiefengw.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestModel_discount_create {
    /*
    EMAIL (string, required)

    CODE (string, required)

    DISCOUNT (float, required): must be > 0.0 and <= 1.0

    SALE_START (string, required): date must be >= today

    SALE_END (string, required): date must be > sale start date

    LIMIT (int, required): maximum number of usages per user

     */
    @JsonProperty(value = "email", required = true)
    private String email;
    @JsonProperty(value = "code", required = true)
    private String code;
    @JsonProperty(value = "discount", required = true)
    private Float discount;
    @JsonProperty(value = "sale_start", required = true)
    private String sale_start;
    @JsonProperty(value = "sale_end", required = true)
    private String sale_end;
    @JsonProperty(value = "limit", required = true)
    private Integer limit;

    @JsonCreator
    public RequestModel_discount_create(  @JsonProperty(value = "email", required = true) String email,
                          @JsonProperty(value = "code", required = true) String code,
                          @JsonProperty(value = "discount") Float discount,
                          @JsonProperty(value = "sale_start", required = true) String sale_start,
                          @JsonProperty(value = "sale_end", required = true) String sale_end,
                          @JsonProperty(value = "limit", required = true) Integer limit) {

        this.email = email;
        this.code = code;
        this.discount = discount;
        this.sale_start = sale_start;
        this.sale_end = sale_end;
        this.limit = limit;
    }

    public RequestModel_discount_create() {}


    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("discount")
    public Float getDiscount() {
        return discount;
    }

    @JsonProperty("sale_start")
    public String getSale_start() {
        return sale_start;
    }

    @JsonProperty("sale_end")
    public String getSale_end() {
        return sale_end;
    }

    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }
}
