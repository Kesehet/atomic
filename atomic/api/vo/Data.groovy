package com.temenos.fnb.gsvc.atomic.api.vo

import com.google.gson.annotations.SerializedName
import com.temenos.fnb.json.JsonSerializable

class Data extends JsonSerializable {

    @SerializedName("publicToken")
    String publicToken

    @SerializedName("token")
    String token
}
