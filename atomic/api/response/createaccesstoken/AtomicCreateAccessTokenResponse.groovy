package com.temenos.fnb.gsvc.atomic.api.response.createaccesstoken

import com.avoka.tm.util.Contract
import com.google.gson.annotations.SerializedName
import com.temenos.fnb.gsvc.atomic.api.response.base.AtomicBaseResponse
import com.temenos.fnb.gsvc.atomic.api.vo.Data

class AtomicCreateAccessTokenResponse extends AtomicBaseResponse{

    AtomicCreateAccessTokenResponse(){

    }

    @SerializedName("data")
    Data data

    @Override
    void validate() throws RuntimeException {
        Contract.notNull(data, "data struct")
        Contract.notNull(data.publicToken, "data.publicToken")
    }

    String getAccessToken() {
        return data.publicToken
    }
}
