package com.temenos.fnb.gsvc.atomic.api.vo

import com.avoka.taf.config.Env
import com.avoka.taf.dao.model.Product
import com.avoka.tm.util.Contract
import com.google.gson.annotations.SerializedName
import com.temenos.fnb.gsvc.atomic.api.query.AtomicQuery
import com.temenos.fnb.json.JsonSerializable

class Account extends JsonSerializable {

    @SerializedName("accountNumber")
    String accountNumber

    @SerializedName("routingNumber")
    String routingNumber

    @SerializedName("type")
    String type

    @SerializedName("title")
    String title

    Account setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber
        return this
    }

    Account setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber
        return this
    }

    Account setType(String type) {
        this.type = type
        return this
    }

    Account setTitle(String title) {
        this.title = title
        return this
    }
}
