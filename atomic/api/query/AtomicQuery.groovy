package com.temenos.fnb.gsvc.atomic.api.query

import com.avoka.taf.config.Env
import com.avoka.taf.dao.model.Product
import com.avoka.taf.dao.viewmodel.ViewCustomer
import com.avoka.taf.dao.viewmodel.ViewCustomers
import com.avoka.taf.dispatcher.Dispatcher
import com.avoka.taf.dispatcher.exception.DispatchException
import com.avoka.tm.http.HttpRequest
import com.avoka.tm.http.HttpResponse
import com.avoka.tm.query.TxnQuery
import com.avoka.tm.util.Contract
import com.avoka.tm.util.Logger
import com.google.gson.Gson
import com.temenos.fnb.gsvc.FnbBaseSvc
import com.temenos.fnb.gsvc.atomic.api.requests.base.AtomicBaseRequest
import com.temenos.fnb.gsvc.atomic.api.requests.createaccesstoken.AtomicCreateAccessTokenDdsRequest
import com.temenos.fnb.gsvc.atomic.api.requests.createaccesstoken.AtomicCreateAccessTokenPaymentSwitchRequest
import com.temenos.fnb.gsvc.atomic.api.requests.createaccesstoken.AtomicCreateAccessTokenRequest
import com.temenos.fnb.gsvc.atomic.api.response.base.AtomicBaseResponse
import com.temenos.fnb.gsvc.atomic.api.response.createaccesstoken.AtomicCreateAccessTokenResponse
import com.temenos.fnb.gsvc.atomic.api.vo.Account
import com.temenos.fnb.gsvc.atomic.api.vo.Identity
import com.temenos.fnb.gsvc.forms.FormInfoManager
import com.temenos.fnb.utils.FiservUtils
import com.temenos.fnb.utils.TxnProxy
import com.temenos.fnb.utils.TxnUtils

class AtomicQuery {

    private final static String HEADER_KEY_API_KEY = "x-api-key"
    private final static String HEADER_KEY_API_SECRET = "x-api-secret"

    static final String CONFIG_SERVICE_KEY_API_KEY = "apiKey"
    static final String CONFIG_SERVICE_KEY_API_SECRET = "apiSecret"
    static final String CONFIG_SERVICE_KEY_API_TOKEN_LIFETIME = "tokenLifetime"
    static final String CONFIG_SERVICE_KEY_API_ENDPOINT = "endpoint"

    static final String TXN_PROP_CONSUMER_ACCOUNT_MAP = "AccountMap"

    private Logger logger
    private TxnProxy txnProxy

    AtomicQuery(Logger logger, TxnProxy txnProxy) {
        this.logger = logger
        this.txnProxy = txnProxy
    }

    private <T extends AtomicBaseResponse> T processAtomicQuery(AtomicBaseRequest request, String svcTxnPropPrefix, Class<T> responseClass) {
        String endpoint = Env.vars.get(CONFIG_SERVICE_KEY_API_ENDPOINT) as String
        Contract.notBlank(endpoint, "${CONFIG_SERVICE_KEY_API_ENDPOINT} key in ConfigService")
        Dispatcher dispatcher = new Dispatcher(endpoint, logger)
        HttpRequest httpRequest = dispatcher.buildHttpRequest(request)
        httpRequest.addHeaders(buildHeaders())
        HttpResponse httpResponse = httpRequest.execute()
        new TxnUtils(txnProxy, svcTxnPropPrefix, "", endpoint)
                .addNonPrefix()
                .addHttpRequest(httpRequest)
                .addHttpResponse(httpResponse)
                .showAll()
                .processServiceRequestResponse()
        if (httpResponse.status < 200 || httpResponse.status > 299) {
            throw new DispatchException(httpResponse, "Unexpected HTTP status in Atomic API call: ${httpResponse.status}")
        }
        T response = (T) responseClass.getConstructor().newInstance().deserialize(httpResponse)
        response.httpRequest = httpRequest
        response.httpResponse = httpResponse
        return response
    }

    private static Map<String, String> buildHeaders() {
        String apiKey = Env.vars.get(CONFIG_SERVICE_KEY_API_KEY) as String
        Contract.notBlank(apiKey, "${CONFIG_SERVICE_KEY_API_KEY} key in ConfigService")
        String apiSecret = Env.vars.get(CONFIG_SERVICE_KEY_API_SECRET) as String
        Contract.notBlank(apiSecret, "${CONFIG_SERVICE_KEY_API_SECRET} key in ConfigService")
        return [
                (HEADER_KEY_API_KEY)   : apiKey,
                (HEADER_KEY_API_SECRET): apiSecret
        ]
    }

    AtomicCreateAccessTokenResponse processCreateAccessTokenDdsQuery(List<Product> products, Map<String, String> existingAccountsMap, String svcTxnPropPrefix) {
        AtomicCreateAccessTokenDdsRequest request = buildCreateAccessTokenDdsRequest(products, existingAccountsMap, null)
        return (AtomicCreateAccessTokenResponse) processAtomicQuery(request, svcTxnPropPrefix, AtomicCreateAccessTokenResponse)
    }

    AtomicCreateAccessTokenResponse processCreateAccessTokenDdsQuery(List<Product> products, List<Map<String, Object>> existingAccountsList, String svcTxnPropPrefix, Map<String, String> formProductMap) {
        AtomicCreateAccessTokenDdsRequest request = buildCreateAccessTokenDdsRequest(products, existingAccountsList, formProductMap)
        if (request.getAccounts() == null || request.getAccounts().size() == 0 ||
                (request.getAccounts().size() >= 1 && request.getAccounts()[0].getAccountNumber() == null)
                || (request.getAccounts().size() >= 1 && request.getAccounts()[0].getAccountNumber().isEmpty())) {

            logger.info("Did not get the account list to send to atomic. Not making call to atomic... ")
            return null;
        }
        return (AtomicCreateAccessTokenResponse) processAtomicQuery(request, svcTxnPropPrefix, AtomicCreateAccessTokenResponse)
    }

    AtomicCreateAccessTokenResponse processCreateAccessTokenPaymentSwitchQuery(List<Product> products, Map<String, String> existingAccountsMap, String svcTxnPropPrefix) {
        AtomicCreateAccessTokenPaymentSwitchRequest request = buildCreateAccessTokenPaymentSwitchRequest(products, existingAccountsMap, null)
        return (AtomicCreateAccessTokenResponse) processAtomicQuery(request, svcTxnPropPrefix, AtomicCreateAccessTokenResponse)
    }

    AtomicCreateAccessTokenResponse processCreateAccessTokenPaymentSwitchQuery(List<Product> products, List<Map<String, Object>> existingAccountsList, String svcTxnPropPrefix, Map<String, String> formProductMap) {
        AtomicCreateAccessTokenPaymentSwitchRequest request = buildCreateAccessTokenPaymentSwitchRequest(products, existingAccountsList, formProductMap)
        if (request.getAccounts() == null || request.getAccounts().size() == 0 ||
                (request.getAccounts().size() >= 1 && request.getAccounts()[0].getAccountNumber() == null)
                || (request.getAccounts().size() >= 1 && request.getAccounts()[0].getAccountNumber().isEmpty())) {

            logger.info("Did not get the account list to send to atomic. Not making call to atomic... ")
            return null;
        }
        return (AtomicCreateAccessTokenResponse) processAtomicQuery(request, svcTxnPropPrefix, AtomicCreateAccessTokenResponse)
    }

    AtomicCreateAccessTokenDdsRequest buildCreateAccessTokenDdsRequest(List<Product> newProducts, List<Map<String, Object>> existingAccountsList, Map<String, String> formProductMap) {
        return buildCreateAccessTokenRequest(new AtomicCreateAccessTokenDdsRequest(), newProducts, existingAccountsList, formProductMap)
    }

    AtomicCreateAccessTokenPaymentSwitchRequest buildCreateAccessTokenPaymentSwitchRequest(List<Product> newProducts, List<Map<String, Object>> existingAccountsList, Map<String, String> formProductMap) {
        return buildCreateAccessTokenRequest(new AtomicCreateAccessTokenPaymentSwitchRequest(), newProducts, existingAccountsList, formProductMap)
    }

    private <T extends AtomicCreateAccessTokenRequest> T buildCreateAccessTokenRequest(T request, List<Product> newProducts, List<Map<String, Object>> existingAccountsList, Map<String, String> formProductMap) {
        String routingNumber = Env.vars.get(FnbBaseSvc.CONFIG_SERVICE_KEY_ROUTING_NUMBER)
        Contract.notBlank(routingNumber, "${FnbBaseSvc.CONFIG_SERVICE_KEY_ROUTING_NUMBER} key in ConfigService")
        String tokenLifetimeString = Env.vars.get(CONFIG_SERVICE_KEY_API_TOKEN_LIFETIME) as String
        Contract.notBlank(tokenLifetimeString, "${CONFIG_SERVICE_KEY_API_TOKEN_LIFETIME} key in ConfigService")
        int tokenLifetime = tokenLifetimeString as int
        List<Account> accounts = buildAccounts(newProducts, existingAccountsList, routingNumber, formProductMap)
        FormInfoManager formInfoManager = new FormInfoManager(txnProxy)
        TxnProxy consumerPreApprovalTxnProxy = formInfoManager.getConsumerPreApprovalTxn()
        Identity identity = buildIdentityFromTxn(consumerPreApprovalTxnProxy ?: txnProxy)
        return request
                .setAccounts(accounts)
                .setIdentity(identity)
                .setIdentifier(consumerPreApprovalTxnProxy?.trackingCode ?: txnProxy.trackingCode)
                .setTokenLifetime(tokenLifetime)
    }

    private List<Account> buildAccounts(List<Product> newProducts, List<Map<String, Object>> existingAccountsList, String routingNumber, Map<String, String> formProductMap) {
        TxnProxy txn = new TxnProxy(new TxnQuery().setTrackingCode(txnProxy.trackingCode).withAll().firstValue())
        TxnProxy consumerTxnProxy = new FormInfoManager(txn).getConsumerPostApprovalTxn()
        List<Account> accounts = []
        if (!newProducts.isEmpty()) {
            accounts.addAll(buildAccountsForNewProducts(consumerTxnProxy, newProducts, routingNumber, formProductMap, logger))
        }
        if (!existingAccountsList.isEmpty()) {
            accounts.addAll(buildAccountsForExistingAccounts(consumerTxnProxy, existingAccountsList, routingNumber))
        }
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("No new or existing accounts")
        }
        return accounts
    }

    private static List<Account> buildAccountsForExistingAccounts(TxnProxy consumerTxnProxy, List<Map<String, Object>> existingAccountsList, String routingNumber) {
        Map<String, String> existingAccountsMap = getExistingAccountsMap(consumerTxnProxy)
        List<Account> accounts = existingAccountsList.collect { Map<String, Object> account ->
            Product.FiservAccountType fiservAccountType
            try {
                fiservAccountType = account.get("accountType") as Product.FiservAccountType
            } catch (Exception ignored) {
                fiservAccountType = Product.FiservAccountType.NONE
            }
            String atomicAccountType = getAtomicAccountTypeFromFiservAccountType(fiservAccountType)
            if (!atomicAccountType.isBlank()) {
                String uniqueId = account.get("uniqueId")
                String desc = account.get("desc")
                Contract.notBlank(uniqueId, "uniqueId for account in existingAccountsInfoList")
                String accountNumber = existingAccountsMap[uniqueId]
                Contract.notBlank(accountNumber, "accountNumber for existing product ${uniqueId} in existingAccountsMap")
                return new Account()
                        .setAccountNumber(accountNumber)
                        .setRoutingNumber(routingNumber)
                        .setTitle(desc)
                        .setType(atomicAccountType)
            }
        }
        accounts.removeAll([null])
        return accounts
    }

    AtomicCreateAccessTokenDdsRequest buildCreateAccessTokenDdsRequest(List<Product> newProducts, Map<String, String> existingAccountsMap, Map<String, String> formProductMap) {
        return buildCreateAccessTokenRequest(new AtomicCreateAccessTokenDdsRequest(), newProducts, existingAccountsMap, formProductMap)
    }

    AtomicCreateAccessTokenPaymentSwitchRequest buildCreateAccessTokenPaymentSwitchRequest(List<Product> newProducts, Map<String, String> existingAccountsMap, Map<String, String> formProductMap) {
        return buildCreateAccessTokenRequest(new AtomicCreateAccessTokenPaymentSwitchRequest(), newProducts, existingAccountsMap, formProductMap)
    }

    private <T extends AtomicCreateAccessTokenRequest> T buildCreateAccessTokenRequest(T request, List<Product> newProducts, Map<String, String> existingAccountsMap, Map<String, String> formProductMap) {
        String routingNumber = Env.vars.get(FnbBaseSvc.CONFIG_SERVICE_KEY_ROUTING_NUMBER)
        Contract.notBlank(routingNumber, "${FnbBaseSvc.CONFIG_SERVICE_KEY_ROUTING_NUMBER} key in ConfigService")
        String tokenLifetimeString = Env.vars.get(CONFIG_SERVICE_KEY_API_TOKEN_LIFETIME) as String
        Contract.notBlank(tokenLifetimeString, "${CONFIG_SERVICE_KEY_API_TOKEN_LIFETIME} key in ConfigService")
        int tokenLifetime = tokenLifetimeString as int
        List<Account> accounts = buildAccounts(newProducts, existingAccountsMap, routingNumber, formProductMap)
        FormInfoManager formInfoManager = new FormInfoManager(txnProxy)
        TxnProxy consumerPreApprovalTxnProxy = formInfoManager.getConsumerPreApprovalTxn()
        Identity identity = buildIdentityFromTxn(consumerPreApprovalTxnProxy ?: txnProxy)
        return request
                .setAccounts(accounts)
                .setIdentity(identity)
                .setIdentifier(txnProxy.trackingCode)
                .setTokenLifetime(tokenLifetime)
    }

    private List<Account> buildAccounts(List<Product> newProducts, Map<String, String> existingAccountsMap, String routingNumber, Map<String, String> formProductMap) {
        TxnProxy consumerTxnProxy = new FormInfoManager(txnProxy).getConsumerPostApprovalTxn()
        List<Account> accounts = []
        if (!newProducts.isEmpty()) {
            accounts.addAll(buildAccountsForNewProducts(consumerTxnProxy, newProducts, routingNumber, formProductMap, logger))
        }
        if (!existingAccountsMap.isEmpty()) {
            accounts.addAll(buildAccountsForExistingAccounts(consumerTxnProxy, existingAccountsMap, routingNumber))
        }
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("No new or existing accounts")
        }
        return accounts
    }

    private static Map<String, String> getConsumerAccountsMap(TxnProxy consumerTxnProxy) {
        String property = consumerTxnProxy?.getTxnProperty(TXN_PROP_CONSUMER_ACCOUNT_MAP) ?: null
        if (!property) {
            return [:]
        }
        return new Gson().fromJson(consumerTxnProxy.getTxnProperty(TXN_PROP_CONSUMER_ACCOUNT_MAP), Map.class) ?: [:]
    }

    static List<Map<String, Object>> getConsumerExistingAccountsInfoList(TxnProxy consumerTxnProxy) {
        String txnPropKey = FiservUtils.buildExistingAccountsInfoTxnPropKey(FnbBaseSvc.APPLICANT_KEY_PRIMARY)
        String txnPropValue = consumerTxnProxy.getTxnProperty(txnPropKey)
        Contract.notBlank(txnPropValue, "${txnPropKey} txn prop")
        List<Map<String, Object>> existingAccountList = new Gson().fromJson(txnPropValue, List.class) ?: []
        if (existingAccountList.isEmpty()) {
            throw new IllegalArgumentException("${txnPropKey} txn prop is empty")
        }
        Map<String, Object> thing = existingAccountList.get(0) as Map<String, Object>
        List<Map<String, Object>> existingAccountsInfoList = (List<Map<String, Object>>) thing.get("accounts")
        Contract.notNull(existingAccountsInfoList, "accounts key in ${txnPropKey}")
        return existingAccountsInfoList
    }

    private static List<Account> buildAccountsForNewProducts(TxnProxy consumerTxnProxy, List<Product> products, String routingNumber, Map<String, String> formProductMap, Logger logger) {
        Map<String, String> newAccountsMap = getConsumerAccountsMap(consumerTxnProxy)
        logger.info("Account Number for map is -->" + newAccountsMap.size())
        return products.collect { Product product ->
            String accountNumber = newAccountsMap[product?.uniqueId]
            logger.info("Account Number for map is -->" + accountNumber)

            //map can be blank if no fiserv error, so to send to atomic the pending acct number we get acct number from product picked
            if (accountNumber == null || accountNumber.isEmpty() || accountNumber.isBlank()) {
                logger.info("Account Map is null from fiserv, so getting acct number from form")
                accountNumber = formProductMap[product?.id]
                logger.info("Account Number for form is -->" + accountNumber)
            }
            return new Account()
                    .setAccountNumber(accountNumber)
                    .setRoutingNumber(routingNumber)
                    .setTitle(product.name)
                    .setType(product.type.toString().toLowerCase())
        }
    }

    private static List<Account> buildAccountsForExistingAccounts(TxnProxy consumerTxnProxy, Map<String, String> existingAccountsMap, String routingNumber) {
        List<Map<String, Object>> existingAccountsInfoList = getConsumerExistingAccountsInfoList(consumerTxnProxy)
        List<Account> accounts = existingAccountsInfoList.collect { Map<String, Object> account ->
            Product.FiservAccountType fiservAccountType
            try {
                fiservAccountType = account.get("accountType") as Product.FiservAccountType
            } catch (Exception ignored) {
                fiservAccountType = Product.FiservAccountType.NONE
            }
            String atomicAccountType = getAtomicAccountTypeFromFiservAccountType(fiservAccountType)
            if (!atomicAccountType.isBlank()) {
                String uniqueId = account.get("uniqueId")
                String desc = account.get("desc")
                Contract.notBlank(uniqueId, "uniqueId for account in existingAccountsInfoList")
                String accountNumber = existingAccountsMap[uniqueId]
                Contract.notBlank(accountNumber, "accountNumber for existing product ${uniqueId} in existingAccountsMap")
                return new Account()
                        .setAccountNumber(accountNumber)
                        .setRoutingNumber(routingNumber)
                        .setTitle(desc)
                        .setType(atomicAccountType)
            }
        }
        accounts.removeAll([null])
        return accounts
    }

    private static String getAtomicAccountTypeFromFiservAccountType(Product.FiservAccountType fiservAccountType) {
        switch (fiservAccountType) {
            case Product.FiservAccountType.DDA:
                return "checking"
                break
            case Product.FiservAccountType.SDA:
                return "savings"
                break
        }
        return ""
    }

    private static Identity buildIdentityFromTxn(TxnProxy txnProxy) {
        if (txnProxy?.formXml == null || txnProxy.formXml.isBlank()) {
            return null
        }
        ViewCustomers customers = ViewCustomers.restoreViewCustomersFromXml(txnProxy.appDoc)
        ViewCustomer primaryCustomer = customers?.primary
        if (!primaryCustomer) {
            return null
        }
        def currentAddress = primaryCustomer.addressCurrent
        return new Identity()
                .setFirstName(primaryCustomer?.firstName)
                .setLastName(primaryCustomer?.lastName)
                .setPostalCode(currentAddress?.postalCode)
                .setAddress(currentAddress?.street1)
                .setAddress2(currentAddress?.street2)
                .setCity(currentAddress?.city)
                .setState(currentAddress?.state)
                .setPhone(primaryCustomer?.phoneNumber)
                .setEmail(primaryCustomer?.email)
    }

    static Map<String, String> getExistingAccountsMap(TxnProxy consumerPostApprovalTxnProxy) {
        String txnPropKeyExistingAccounts = FiservUtils.buildExistingAccountsUUIDTxnPropKey(FnbBaseSvc.APPLICANT_KEY_PRIMARY)
        String existingAccountsJson = consumerPostApprovalTxnProxy.getTxnProperty(txnPropKeyExistingAccounts) ?: ""
        Contract.notBlank(existingAccountsJson, "existingAccountsJson")
        Map<String, String> existingAccountsMap = new Gson().fromJson(existingAccountsJson, Map.class) ?: [:]
        return existingAccountsMap
    }

}
