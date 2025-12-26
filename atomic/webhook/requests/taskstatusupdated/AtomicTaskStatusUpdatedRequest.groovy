package com.temenos.fnb.gsvc.atomic.webhook.requests.taskstatusupdated
import com.google.gson.annotations.SerializedName
import com.temenos.fnb.gsvc.atomic.webhook.vo.Company
import com.temenos.fnb.gsvc.atomic.webhook.vo.TaskData
import com.temenos.fnb.gsvc.atomic.webhook.vo.User

class AtomicTaskStatusUpdatedRequest  {

    @SerializedName("eventType")
    String eventType

    @SerializedName("eventTime")
    String eventTime

    @SerializedName("user")
    User user

    @SerializedName("publicToken")
    String publicToken

    @SerializedName("authenticationMethod")
    String authenticationMethod

    @SerializedName("company")
    Company company

    @SerializedName("task")
    String task

    @SerializedName("taskWorkflow")
    String taskWorkflow

    @SerializedName("product")
    String product

    @SerializedName("data")
    TaskData data



}
