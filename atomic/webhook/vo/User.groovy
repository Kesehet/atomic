package com.temenos.fnb.gsvc.atomic.webhook.vo

import com.google.gson.annotations.SerializedName
import com.temenos.fnb.json.JsonSerializable

class User extends JsonSerializable {
    @SerializedName("_id")
    String id
    @SerializedName("identifier")
    String identifier
    @SerializedName("connected")
    Boolean connected



}
