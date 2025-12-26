package com.temenos.fnb.gsvc.atomic.api.response.base

import com.avoka.taf.dispatcher.BaseResponse
import com.avoka.tm.http.HttpRequest
import com.avoka.tm.http.HttpResponse

abstract class AtomicBaseResponse extends BaseResponse {

    HttpResponse httpResponse
    HttpRequest httpRequest

}
