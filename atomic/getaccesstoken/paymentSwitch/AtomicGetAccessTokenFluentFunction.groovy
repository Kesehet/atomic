package com.temenos.fnb.gsvc.atomic.getaccesstoken

import com.avoka.taf.config.Env
import com.avoka.taf.dao.model.Product
import com.avoka.taf.dao.query.ProductRepositoryQuery
import com.avoka.tm.func.FormFuncResult
import com.avoka.tm.func.FuncParam
import com.avoka.tm.func.FuncResult
import com.temenos.fnb.gsvc.FnbBaseFluentFunction
import com.temenos.fnb.gsvc.atomic.accesstoken.AccessTokenManager
import com.temenos.fnb.gsvc.atomic.utils.AtomicUtils
import com.temenos.fnb.gsvc.forms.FormInfoManager
import com.temenos.fnb.utils.TxnProxy
import groovy.json.JsonBuilder
import groovy.transform.TypeChecked

@TypeChecked
class AtomicGetAccessTokenFluentFunction extends FnbBaseFluentFunction {

    static final String SERVICE_NAME = "getAccessToken"

    static final String TXN_PROP_SERVICE_PREFIX = "${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.${SERVICE_NAME}"

    static final String RESULT_DATA_KEY_ACCESS_TOKEN = "accessToken"
    static final String RESULT_MESSAGE_CREATED_NEW_ACCESS_TOKEN = "created new access token"
    static final String RESULT_MESSAGE_RETRIEVED_ACCESS_TOKEN_FROM_TXN_PROPS = "retrieved access token from txn props"
    static final String RESULT_MESSAGE_NO_DDS_ELIGIBLE_PRODUCTS = "no dds eligible products"
    static final String RESULT_MESSAGE_CONSUMER_POST_APPROVAL_TXN_NOT_COMPLETED = "consumer post-approval txn not completed"
    static final String RESULT_MESSAGE_NO_CONSUMER_POST_APPROVAL_TXN = "no consumer post-approval txn"
    static final String RESULT_MESSAGE_NO_CONSUMER_DEPOSIT_PRODUCTS = "no consumer deposit products or eligible account"

    /*
     * Perform Fluent Function call.
     *
     * returns: FuncResult
     */

    FuncResult run(FuncParam param) {

        setEnv(param.svcDef, AtomicUtils.CONFIG_SERVICE_APP_NAME, SERVICE_NAME, txnProxy, logger)
        logger.debug("Env vars:")
        logger.debug(new JsonBuilder(Env.vars).toPrettyString())
        AccessTokenManager accessTokenManager = new AccessTokenManager(logger, txnProxy)
        String accessToken = accessTokenManager.getAccessTokenFromTxnProps()
        if (accessToken != null && !accessToken.isBlank()) {
            return generateSuccessfulResult(RESULT_MESSAGE_RETRIEVED_ACCESS_TOKEN_FROM_TXN_PROPS, accessToken)
        }

        TxnProxy consumerPostApprovalTxnProxy = new FormInfoManager(txnProxy).getConsumerPostApprovalTxn()
        if (!consumerPostApprovalTxnProxy?.isFormStatusCompleted()) {
            return generateSuccessfulResult(RESULT_MESSAGE_CONSUMER_POST_APPROVAL_TXN_NOT_COMPLETED)
        }

        List<Product> products = Product.restoreProductsFromXml(txnProxy.getAppDoc())
        logger.info("found product from dashboard Tran XML , size -->" + products.size())
        TxnProxy consumerPreApprovalTxnProxy = new FormInfoManager(txnProxy).getConsumerPreApprovalTxn()
        List<Product> productsSelected = Product.restoreProductsFromXml(consumerPreApprovalTxnProxy.getAppDoc())
        logger.info("found product from consumer preapproval Tran XML , size -->" + productsSelected.size())
        Map<String, String> formProductsAcctMap = AccessTokenRequestHelper.buildFormProductsAccountMap(productsSelected, logger)

        List<Product> consumerDepositProducts = Product.filterForConsumerDepositProducts(productsSelected)
        logger.info("consumerDepositProducts  after raw product filter , size -->" + consumerDepositProducts.size())
        List<Product> allProducts = ProductRepositoryQuery.getAllProductsFromConfig(consumerPostApprovalTxnProxy, param.svcDef, logger)

        List<Product> ddsEligibleProducts = Product.getDDSEligibleProducts(allProducts)
        List<String> ddsEligibleProductIds = ddsEligibleProducts.collect { Product product ->
            product.id
        }
        List<Product> consumerDepositProductsDDSEligible = new ArrayList<>();
        for (String id : ddsEligibleProductIds) {
            for (Product product : consumerDepositProducts) {
                if (product.id == id) {
                    consumerDepositProductsDDSEligible.add(product)
                }
            }
        }
        List<Map<String, Object>> existingAccountsList = AccessTokenRequestHelper.getDDSEligibleAccountsInfoList(consumerPostApprovalTxnProxy, logger, ddsEligibleProductIds)
        if (consumerDepositProductsDDSEligible.isEmpty() && existingAccountsList.isEmpty()) {
            logger.error("Did not get valid product or existing Account eligible for DDS..")
            return generateSuccessfulResult(RESULT_MESSAGE_NO_CONSUMER_DEPOSIT_PRODUCTS)
        }
        accessToken = accessTokenManager.getAccessTokenFromAtomicPaymentSwitch(consumerDepositProductsDDSEligible, existingAccountsList, TXN_PROP_SERVICE_PREFIX, formProductsAcctMap)
        accessTokenManager.setAccessTokenTxnProps(accessToken)
        return generateSuccessfulResult(RESULT_MESSAGE_CREATED_NEW_ACCESS_TOKEN, accessToken)
    }

    FormFuncResult generateSuccessfulResult(String message, String accessToken = null) {
        logger.info("Returning with message ${message}")
        FormFuncResult result = generateFormFuncResult(ServiceStatusType.SUCCESS, message)
        if (accessToken != null && !accessToken.isBlank()) {
            result.data.put(RESULT_DATA_KEY_ACCESS_TOKEN, accessToken)
        }
        logFuncResult(result, true, 100, logger)
        return result
    }

}
