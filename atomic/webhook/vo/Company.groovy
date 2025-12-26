package com.temenos.fnb.gsvc.atomic.webhook.vo

import com.google.gson.annotations.SerializedName
import com.temenos.fnb.json.JsonSerializable

class Company extends JsonSerializable{

    @SerializedName("_id")
    String id
    @SerializedName("name")
    String name
    @SerializedName("branding")
    Branding branding




}
