package com.temenos.fnb.gsvc.atomic.getaccesstoken

import com.avoka.taf.dao.model.Product
import com.avoka.tm.util.Logger
import com.google.gson.Gson
import com.temenos.fnb.gsvc.FnbBaseSvc
import com.temenos.fnb.utils.FiservUtils
import com.temenos.fnb.utils.TxnProxy

class AccessTokenRequestHelper {

    static final String STATUS = "status"
    static final String STATUS_ACTIVE = "Active"

    static Map<String, String> buildFormProductsAccountMap(List<Product> productsSelected, Logger logger) {
        Map<String, String> formProductsAcctMap = [:]
        productsSelected.each { Product product ->
            logger.info("Product id and  Acct Number ${product.id}_${product.account.number}")
            formProductsAcctMap.put(product.id, product.account.number)
        }
        return formProductsAcctMap
    }

    static Map<String, String> getExistingAccountsMap(TxnProxy consumerPostApprovalTxnProxy) {
        String txnPropKeyExistingAccounts = FiservUtils.buildExistingAccountsUUIDTxnPropKey(FnbBaseSvc.APPLICANT_KEY_PRIMARY)
        String existingAccountsJson = consumerPostApprovalTxnProxy.getTxnProperty(txnPropKeyExistingAccounts) ?: ""
        if (!existingAccountsJson) {
            return [:]
        }
        Map<String, String> existingAccountsMap = new Gson().fromJson(existingAccountsJson, Map.class) ?: [:]
        return existingAccountsMap
    }

    static List<Map<String, Object>> getDDSEligibleAccountsInfoList(TxnProxy consumerTxnProxy, Logger logger, List<String> ddsEligibleProductIds) {
        String txnPropKey = FiservUtils.buildExistingAccountsInfoTxnPropKey(FnbBaseSvc.APPLICANT_KEY_PRIMARY)
        String txnPropValue = consumerTxnProxy.getTxnProperty(txnPropKey)
        if (txnPropValue == null || txnPropValue.isBlank()) {
            logger.error("Did not get the correct existing account List from Fiserv .. Skipping the process")
            return []
        }
        List<Map<String, Object>> existingAccountList = new Gson().fromJson(txnPropValue, List.class) ?: []
        if (existingAccountList.isEmpty()) {
            return []
        }
        Map<String, Object> accounts = existingAccountList.get(0) as Map<String, Object>
        List<Map<String, Object>> existingAccountsInfoList = (List<Map<String, Object>>) accounts.get("accounts")
        if (existingAccountsInfoList == null) {
            return []
        }

        List<Map<String, Object>> ddsEligibleAccountsInfoList = existingAccountsInfoList.findAll { Map<String, Object> accountInfo ->
            if (accountInfo.containsKey("productIdent")) {
                String productId = accountInfo.get("productIdent")
                if (productId in ddsEligibleProductIds && (accountInfo.get(STATUS) == null || ((String) accountInfo.get(STATUS)).equalsIgnoreCase(STATUS_ACTIVE))) {
                    return accountInfo
                }
            }
        }
        return ddsEligibleAccountsInfoList
    }
}
