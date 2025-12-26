package com.temenos.fnb.gsvc.atomic.api.requests.createaccesstoken

import com.avoka.taf.dispatcher.HttpMethod
import com.avoka.tm.util.Contract
import com.temenos.fnb.gsvc.atomic.api.requests.base.AtomicBaseRequest
import com.temenos.fnb.gsvc.atomic.api.vo.Account
import com.temenos.fnb.gsvc.atomic.api.vo.Card
import com.temenos.fnb.gsvc.atomic.api.vo.Identity

class AtomicCreateAccessTokenRequest extends AtomicBaseRequest {

    enum FundingInstrument {
        ACCOUNTS,
        CARDS
    }

    String identifier
    List<Account> accounts
    List<Card> cards
    Identity identity
    int tokenLifetime
    FundingInstrument fundingInstrument

    AtomicCreateAccessTokenRequest setIdentifier(String identifier) {
        this.identifier = identifier
        return this
    }

    AtomicCreateAccessTokenRequest setAccounts(List<Account> accounts) {
        this.accounts = accounts
        this.fundingInstrument = FundingInstrument.ACCOUNTS
        return this
    }

    AtomicCreateAccessTokenRequest setCards(List<Card> cards) {
        this.cards = cards
        this.fundingInstrument = FundingInstrument.CARDS
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
        Contract.notNull(fundingInstrument, "fundingInstrument")
        switch (fundingInstrument) {
            case FundingInstrument.ACCOUNTS:
                Contract.notNull(accounts, "accounts")
                if (accounts.isEmpty()) {
                    throw new IllegalArgumentException("accounts list is empty")
                }
                if (cards != null && !cards.isEmpty()) {
                    throw new IllegalArgumentException("cards must be empty when using accounts")
                }
                accounts.eachWithIndex { Account account, int index ->
                    Contract.notNull(account, "accounts[${index}]")
                    Contract.notBlank(account.accountNumber, "accounts[${index}].accountNumber")
                    Contract.notBlank(account.routingNumber, "accounts[${index}].routingNumber")
                    Contract.notBlank(account.type, "accounts[${index}].type")
                    Contract.notBlank(account.title, "accounts[${index}].title")
                }
                break
            case FundingInstrument.CARDS:
                Contract.notNull(cards, "cards")
                if (cards.isEmpty()) {
                    throw new IllegalArgumentException("cards list is empty")
                }
                if (accounts != null && !accounts.isEmpty()) {
                    throw new IllegalArgumentException("accounts must be empty when using cards")
                }
                cards.eachWithIndex { Card card, int index ->
                    Contract.notNull(card, "cards[${index}]")
                    Contract.notBlank(card.title, "cards[${index}].title")
                    Contract.notBlank(card.brand, "cards[${index}].brand")
                    Contract.notBlank(card.number, "cards[${index}].number")
                    Contract.notBlank(card.expiry, "cards[${index}].expiry")
                    Contract.notBlank(card.cvv, "cards[${index}].cvv")
                }
                break
        }
        Contract.notNull(identity, "identity")
        Contract.notBlank(identity.firstName, "identity.firstName")
        Contract.notBlank(identity.lastName, "identity.lastName")
        Contract.notBlank(identity.postalCode, "identity.postalCode")
        Contract.notBlank(identity.email, "identity.email")
    }

}
