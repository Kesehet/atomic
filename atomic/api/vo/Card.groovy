package com.temenos.fnb.gsvc.atomic.api.vo

import com.google.gson.annotations.SerializedName
import com.temenos.fnb.json.JsonSerializable

class Card extends JsonSerializable {

    @SerializedName("title")
    String title

    @SerializedName("brand")
    String brand

    @SerializedName("number")
    String number

    @SerializedName("expiry")
    String expiry

    @SerializedName("cvv")
    String cvv

    Card setTitle(String title) {
        this.title = title
        return this
    }

    Card setBrand(String brand) {
        this.brand = brand
        return this
    }

    Card setNumber(String number) {
        this.number = number
        return this
    }

    Card setExpiry(String expiry) {
        this.expiry = expiry
        return this
    }

    Card setCvv(String cvv) {
        this.cvv = cvv
        return this
    }
}
