package com.temenos.fnb.gsvc.atomic.api.requests.createaccesstoken

import com.avoka.taf.dispatcher.HttpMethod
import com.temenos.fnb.gsvc.atomic.api.requests.base.AtomicBaseRequest
import com.temenos.fnb.gsvc.atomic.api.vo.Account

class AtomicCreateAccessTokenRequest extends AtomicBaseRequest {

    String identifier
    List<Account> accounts
    int tokenLifetime

    AtomicCreateAccessTokenRequest setIdentifier(String identifier) {
        this.identifier = identifier
        return this
    }

    AtomicCreateAccessTokenRequest setAccounts(List<Account> accounts) {
        this.accounts = accounts
        return this
    }

    AtomicCreateAccessTokenRequest setTokenLifetime(int tokenLifetime) {
        this.tokenLifetime = tokenLifetime
        return this
    }

    @Override
    String contextPath() {
        return "/access-token"
    }

    @Override
    HttpMethod httpMethod() {
        return HttpMethod.POST
    }

    @Override
    void validate() throws RuntimeException {

    }

}
