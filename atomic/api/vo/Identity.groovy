package com.temenos.fnb.gsvc.atomic.api.vo

import com.google.gson.annotations.SerializedName
import com.temenos.fnb.json.JsonSerializable

class Identity extends JsonSerializable {

    @SerializedName("firstName")
    String firstName

    @SerializedName("lastName")
    String lastName

    @SerializedName("postalCode")
    String postalCode

    @SerializedName("address")
    String address

    @SerializedName("address2")
    String address2

    @SerializedName("city")
    String city

    @SerializedName("state")
    String state

    @SerializedName("phone")
    String phone

    @SerializedName("email")
    String email

    Identity setFirstName(String firstName) {
        this.firstName = firstName
        return this
    }

    Identity setLastName(String lastName) {
        this.lastName = lastName
        return this
    }

    Identity setPostalCode(String postalCode) {
        this.postalCode = postalCode
        return this
    }

    Identity setAddress(String address) {
        this.address = address
        return this
    }

    Identity setAddress2(String address2) {
        this.address2 = address2
        return this
    }

    Identity setCity(String city) {
        this.city = city
        return this
    }

    Identity setState(String state) {
        this.state = state
        return this
    }

    Identity setPhone(String phone) {
        this.phone = phone
        return this
    }

    Identity setEmail(String email) {
        this.email = email
        return this
    }
}
