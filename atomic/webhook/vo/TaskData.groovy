package com.temenos.fnb.gsvc.atomic.webhook.vo

import com.google.gson.annotations.SerializedName
import com.temenos.fnb.json.JsonSerializable

class TaskData extends JsonSerializable{

    @SerializedName("authenticated")
    Boolean authenticated

    @SerializedName("previousStatus")
    String previousStatus

    @SerializedName("status")
    String status

    @SerializedName("distributionType")
    String distributionType

    @SerializedName("distributionAmount")
    Integer distributionAmount


}
