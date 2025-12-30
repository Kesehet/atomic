package com.temenos.fnb.gsvc.atomic.accesstoken

import com.avoka.taf.config.Env
import com.avoka.taf.dao.model.Product
import com.avoka.tm.util.Logger
import com.temenos.fnb.gsvc.atomic.api.query.AtomicQuery
import com.temenos.fnb.gsvc.atomic.api.response.createaccesstoken.AtomicCreateAccessTokenResponse
import com.temenos.fnb.gsvc.atomic.utils.AtomicUtils
import com.temenos.fnb.utils.TxnProxy

import java.time.LocalDateTime

class AccessTokenManager {

    static final String TXN_PROP_ACCESS_TOKEN = "${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.accessToken"
    static final String TXN_PROP_ACCESS_TOKEN_EXPIRATION = "${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.accessTokenExpiration"
    static final String EXPIRATION_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    private Logger logger
    private TxnProxy txnProxy

    AccessTokenManager(Logger logger, TxnProxy txnProxy) {
        this.logger = logger
        this.txnProxy = txnProxy
    }

    void setAccessTokenTxnProps(String accessToken) {
        String expirationDateTimeString = getAccessTokenExpirationDateTimeString()
        txnProxy.setProperty(TXN_PROP_ACCESS_TOKEN, accessToken)
        txnProxy.setProperty(TXN_PROP_ACCESS_TOKEN_EXPIRATION, expirationDateTimeString)
        logger.info("set access token and expiration to txn props")
    }

    private static String getAccessTokenExpirationDateTimeString() {
        LocalDateTime currentDateTime = LocalDateTime.now()
        LocalDateTime expirationDateTime = currentDateTime.plus(Env.vars.get(AtomicQuery.CONFIG_SERVICE_KEY_API_TOKEN_LIFETIME) as int)
        return expirationDateTime.format(EXPIRATION_DATE_TIME_FORMAT)
    }

    String getAccessTokenFromAtomic(List<Product> products, Map<String, String> existingAccountsMap, String servicePrefix) {
        return getAccessTokenFromAtomicDds(products, existingAccountsMap, servicePrefix)
    }

    String getAccessTokenFromAtomicDds(List<Product> products, Map<String, String> existingAccountsMap, String servicePrefix) {
        logger.info("Calling Atomic for Access Token")
        AtomicQuery query = new AtomicQuery(logger, txnProxy)
        AtomicCreateAccessTokenResponse response = query.processCreateAccessTokenDdsQuery(products, existingAccountsMap, servicePrefix)
        String accessToken = response.getAccessToken()
        logger.info("Access Token retrieved: ${accessToken != null && !accessToken.isBlank()}")
        return accessToken
    }

    String getAccessTokenFromAtomic(List<Product> products, List<Map<String, Object>> existingAccountsList, String servicePrefix, Map<String, String> formProductMap) {
        return getAccessTokenFromAtomicDds(products, existingAccountsList, servicePrefix, formProductMap)
    }

    String getAccessTokenFromAtomicDds(List<Product> products, List<Map<String, Object>> existingAccountsList, String servicePrefix, Map<String, String> formProductMap) {
        logger.info("Calling Atomic for Access Token")
        String accessToken = ""
        AtomicQuery query = new AtomicQuery(logger, txnProxy)
        AtomicCreateAccessTokenResponse response = query.processCreateAccessTokenDdsQuery(products, existingAccountsList, servicePrefix, formProductMap)
        if (response != null)
            accessToken = response.getAccessToken()

        logger.info("Access Token retrieved: ${accessToken != null && !accessToken.isBlank()}")
        return accessToken
    }

    String getAccessTokenFromAtomicPaymentSwitch(List<Product> products, Map<String, String> existingAccountsMap, String servicePrefix) {
        logger.info("Calling Atomic for Access Token")
        AtomicQuery query = new AtomicQuery(logger, txnProxy)
        AtomicCreateAccessTokenResponse response = query.processCreateAccessTokenPaymentSwitchQuery(products, existingAccountsMap, servicePrefix)
        String accessToken = response.getAccessToken()
        logger.info("Access Token retrieved: ${accessToken != null && !accessToken.isBlank()}")
        return accessToken
    }

    String getAccessTokenFromAtomicPaymentSwitch(List<Product> products, List<Map<String, Object>> existingAccountsList, String servicePrefix, Map<String, String> formProductMap) {
        logger.info("Calling Atomic for Access Token")
        String accessToken = ""
        AtomicQuery query = new AtomicQuery(logger, txnProxy)
        AtomicCreateAccessTokenResponse response = query.processCreateAccessTokenPaymentSwitchQuery(products, existingAccountsList, servicePrefix, formProductMap)
        if (response != null)
            accessToken = response.getAccessToken()

        logger.info("Access Token retrieved: ${accessToken != null && !accessToken.isBlank()}")
        return accessToken
    }

    String getAccessTokenFromTxnProps() {
        if (txnProxy == null) {
            logger.info("Error - no txn proxy")
        }
        String accessToken = txnProxy.getTxnProperty(TXN_PROP_ACCESS_TOKEN)
        if (accessToken == null || accessToken.isBlank()) {
            logger.info("No access token found in txn props")
            return null
        }
        String expirationAsString = txnProxy.getTxnProperty(TXN_PROP_ACCESS_TOKEN_EXPIRATION)
        if (expirationAsString == null || expirationAsString.isBlank()) {
            logger.info("No access token expiration found in txn props")
            return null
        }
        LocalDateTime expirationDateTime = LocalDateTime.parse(expirationAsString, EXPIRATION_DATE_TIME_FORMAT)
        if (expirationDateTime.isBefore(LocalDateTime.now())) {
            logger.info("Access token in txn props is expired")
            return null
        }
        logger.info("Access token in txn props and not expired")
        return accessToken
    }

}
