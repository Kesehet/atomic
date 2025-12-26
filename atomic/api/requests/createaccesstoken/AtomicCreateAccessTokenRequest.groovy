package com.temenos.fnb.gsvc.atomic.api.requests.createaccesstoken

import com.avoka.taf.dispatcher.HttpMethod
import com.avoka.tm.util.Contract
import com.temenos.fnb.gsvc.atomic.api.requests.base.AtomicBaseRequest
import com.temenos.fnb.gsvc.atomic.api.vo.Account
import com.temenos.fnb.gsvc.atomic.api.vo.Identity

class AtomicCreateAccessTokenRequest extends AtomicBaseRequest {

    String identifier
    List<Account> accounts
    Identity identity
    int tokenLifetime

    AtomicCreateAccessTokenRequest setIdentifier(String identifier) {
        this.identifier = identifier
        return this
    }

    AtomicCreateAccessTokenRequest setAccounts(List<Account> accounts) {
        this.accounts = accounts
        return this
    }

    AtomicCreateAccessTokenRequest setIdentity(Identity identity) {
        this.identity = identity
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
        Contract.notBlank(identifier, "identifier")
        Contract.notNull(accounts, "accounts")
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("accounts list is empty")
        }
        accounts.eachWithIndex { Account account, int index ->
            Contract.notNull(account, "accounts[${index}]")
            Contract.notBlank(account.accountNumber, "accounts[${index}].accountNumber")
            Contract.notBlank(account.routingNumber, "accounts[${index}].routingNumber")
            Contract.notBlank(account.type, "accounts[${index}].type")
            Contract.notBlank(account.title, "accounts[${index}].title")
        }
        Contract.notNull(identity, "identity")
        Contract.notBlank(identity.firstName, "identity.firstName")
        Contract.notBlank(identity.lastName, "identity.lastName")
        Contract.notBlank(identity.postalCode, "identity.postalCode")
        Contract.notBlank(identity.email, "identity.email")
    }

}
