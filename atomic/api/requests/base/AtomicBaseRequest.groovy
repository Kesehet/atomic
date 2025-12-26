package com.temenos.fnb.gsvc.atomic.api.requests.base

import com.avoka.taf.dispatcher.BaseRequest
import com.avoka.taf.dispatcher.ContentType

abstract class AtomicBaseRequest extends BaseRequest {

    @Override
    ContentType contentType() {
        return ContentType.json
    }

}
