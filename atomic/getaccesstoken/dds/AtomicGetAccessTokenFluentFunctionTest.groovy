package com.temenos.fnb.gsvc.atomic.getaccesstoken

import com.avoka.commons.test.AvokaUnitTest
import com.avoka.taf.config.Env
import com.avoka.taf.dao.model.Product
import com.avoka.tm.func.FormFuncResult
import com.avoka.tm.func.FuncParam
import com.avoka.tm.svc.ServiceInvoker
import com.avoka.tm.test.MockVoBuilder
import com.avoka.tm.test.TransactSDKUnitTest
import com.avoka.tm.util.XmlDoc
import com.avoka.tm.vo.SvcDef
import com.avoka.tm.vo.Txn
import com.temenos.fnb.gsvc.FnbBaseSvc
import com.temenos.fnb.gsvc.atomic.accesstoken.AccessTokenManager
import com.temenos.fnb.gsvc.atomic.api.query.AtomicQuery
import com.temenos.fnb.gsvc.atomic.api.requests.createaccesstoken.AtomicCreateAccessTokenRequest
import com.temenos.fnb.gsvc.atomic.utils.AtomicUtils
import com.temenos.fnb.gsvc.forms.MockFormInfoSetupManager
import com.temenos.fnb.gsvc.forms.page.PageConstants
import com.temenos.fnb.utils.TestUtils
import com.temenos.fnb.utils.TxnMockingManager
import com.temenos.fnb.utils.TxnProxy
import groovy.json.JsonBuilder
import groovy.transform.TypeChecked
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category

@TypeChecked
@Category(AvokaUnitTest.class)
class AtomicGetAccessTokenFluentFunctionTest extends TransactSDKUnitTest {

    private String testXml
    private SvcDef svcDefWithConfig
    private TxnProxy dashboardTxnProxy
    private Txn dashboardTxn
    private final String accessToken = "AcCeSsToKeN"
    private final String expirationDateTimeString = "2030-01-10 08:22:11"
    private final String productUniqueId = "81299413-b65e-4f68-9401-bf5dbaab8a83"
    private final String productUniqueId2 = "2b746ece-d515-42ab-8443-f7b54d86bb4d"
    Map<String, String> accountMap = [
            (productUniqueId) : "66666666666",
            (productUniqueId2): "77777777777"
    ]


    @Before
    void setup() {
        Map<String, Object> envVars = [:]
        envVars.put(AtomicQuery.CONFIG_SERVICE_KEY_API_TOKEN_LIFETIME, 8000)
        envVars.put(FnbBaseSvc.CONFIG_SERVICE_KEY_ROUTING_NUMBER, 44444444)
        envVars.put(AtomicQuery.CONFIG_SERVICE_KEY_API_KEY, "aPiKeY")
        envVars.put(AtomicQuery.CONFIG_SERVICE_KEY_API_SECRET, "ApIsEcReT")
        envVars.put(AtomicQuery.CONFIG_SERVICE_KEY_API_ENDPOINT, "https://google.com")
        envVars.put("productsXml", "<ProductRepository><Products><Product><Group>Consumer</Group><Id>23901</Id><Name>Freestyle Checking</Name><AltName>Freestyle Checking</AltName><Type>Checking</Type><AtomicInfo>\n" +
                "                <IsEligibleForDDS>true</IsEligibleForDDS>\n" +
                "            </AtomicInfo></Product></Products></ProductRepository>")

        Env.setVars(envVars)
        svcDefWithConfig = new SvcDef(svcDef, envVars as Map<String, String>)
        //Env.logger = logger

        testXml = testParams["Test XML Data"]

        // --- Create a Dashboard Txn
        dashboardTxnProxy = TxnMockingManager.buildTestTxn(
                "https://dev.fnb.avoka-transact.com/workspaces/app/fnb-dashboard/3.0.0",
                testXml, PageConstants.FormCodes.FORM_CODE_FNB_DASHBOARD.name, "Workspaces", Txn.FORM_SAVED, Txn.DELIVERY_NOT_READY)


        new MockFormInfoSetupManager()
                .addDashboardTxn(dashboardTxnProxy)
                .saveMockFormInfo()

    }


    /*
* Perform service unit test
*
* throws exception if unit test fails
*/

    @Test
    void testUnexpiredTokenTxn() throws Exception {
        testXml = testParams["Test XML Data"]
        dashboardTxnProxy.setProperty(AccessTokenManager.TXN_PROP_ACCESS_TOKEN, accessToken)
        dashboardTxnProxy.setProperty(AccessTokenManager.TXN_PROP_ACCESS_TOKEN_EXPIRATION, expirationDateTimeString)

//        TxnProxy preApprovalTxn = buildTxn(testXml)
        TxnProxy postApprovalTxn = buildTxn(testXml)
        //new TxnUpdater(postApprovalTxn).setFormStatus(Txn.FORM_COMPLETED).update()
        postApprovalTxn.setFormStatus(Txn.FORM_COMPLETED)

//        Map<String, FormInfo> formInfoMap = [:]
//        formInfoMap.put(preApprovalTxn.trackingCode, new FormInfo(new TxnProxy(preApprovalTxn)))
//        formInfoMap.put(postApprovalTxn.trackingCode, new FormInfo(postApprovalTxn))
//        formInfoMap.put(dashboardTxn.trackingCode, new FormInfo(dashboardTxn))
//        FormInfoManager.saveFormInfoToTxns(formInfoMap)

        new MockFormInfoSetupManager()
                .addDashboardTxn(dashboardTxnProxy)
                .addPostApprovalTxn(postApprovalTxn)
                .saveMockFormInfo()

        FormFuncResult result = invokeService([:])
        logger.info result
        assert result != null
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.SUCCESS.toString()
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS_MESSAGE) == AtomicGetAccessTokenFluentFunction.RESULT_MESSAGE_RETRIEVED_ACCESS_TOKEN_FROM_TXN_PROPS
        assert result.data.get(AtomicGetAccessTokenFluentFunction.RESULT_DATA_KEY_ACCESS_TOKEN) == accessToken
    }

    /*
 * Perform service unit test
 *
 * throws exception if unit test fails
 */

    @Test
    void testNoPostApprovalTxn() throws Exception {
        testXml = testParams["Test XML Data"]
        dashboardTxn = new MockVoBuilder().createTxnSavedWithXml(testXml)
        dashboardTxnProxy = new TxnProxy(dashboardTxn)
        TxnProxy preApprovalTxn = buildTxn(testXml)
//        Map<String, FormInfo> formInfoMap = [:]
//        formInfoMap.put(dashboardTxn.trackingCode, new FormInfo(dashboardTxn))
//        FormInfoManager.saveFormInfoToTxns(formInfoMap)
        new MockFormInfoSetupManager()
                .addDashboardTxn(dashboardTxnProxy)
                .addConsumerTxn(preApprovalTxn)
                .saveMockFormInfo()

        FormFuncResult result = invokeService([:])
        logger.info result
        assert result != null
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.SUCCESS.toString()
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS_MESSAGE) == AtomicGetAccessTokenFluentFunction.RESULT_MESSAGE_CONSUMER_POST_APPROVAL_TXN_NOT_COMPLETED
    }

    /*
     * Perform service unit test
     *
     * throws exception if unit test fails
     */

    @Test
    void testPostApprovalTxnNotCompleted() throws Exception {
        testXml = testParams["Test XML Data"]
        dashboardTxn = new MockVoBuilder().createTxnSavedWithXml(testXml)
        dashboardTxnProxy = new TxnProxy(dashboardTxn)
        //TxnProxy preApprovalTxn = buildTxn(testXml)
        TxnProxy postApprovalTxn = buildTxn(testXml)
//        Map<String, FormInfo> formInfoMap = [:]
//        formInfoMap.put(preApprovalTxn.trackingCode, new FormInfo(preApprovalTxn))
//        formInfoMap.put(postApprovalTxn.trackingCode, new FormInfo(postApprovalTxn))
//        formInfoMap.put(dashboardTxn.trackingCode, new FormInfo(dashboardTxn))
//        FormInfoManager.saveFormInfoToTxns(formInfoMap)
        new MockFormInfoSetupManager()
                .addDashboardTxn(dashboardTxnProxy)
                .addPostApprovalTxn(postApprovalTxn)
                .saveMockFormInfo()

        FormFuncResult result = invokeService([:])
        logger.info result
        assert result != null
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.SUCCESS.toString()
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS_MESSAGE) == AtomicGetAccessTokenFluentFunction.RESULT_MESSAGE_CONSUMER_POST_APPROVAL_TXN_NOT_COMPLETED
    }

    /*
 * Perform service unit test
 *
 * throws exception if unit test fails
 */

    @Test
    void testNoConsumerDepositProducts() throws Exception {
        testXml = testParams["No Consumer Deposit Products XML"]
        dashboardTxn = new MockVoBuilder().createTxnSavedWithXml(testXml)
        dashboardTxnProxy = new TxnProxy(dashboardTxn)
        TxnProxy preApprovalTxn = buildTxn(testXml)
        TxnProxy postApprovalTxn = buildTxn(testXml)
//        new TxnUpdater(postApprovalTxn).setFormStatus(Txn.FORM_COMPLETED).update()
        postApprovalTxn.setFormStatus(Txn.FORM_COMPLETED)
//        Map<String, FormInfo> formInfoMap = [:]
//        formInfoMap.put(preApprovalTxn.trackingCode, new FormInfo(preApprovalTxn))
//        formInfoMap.put(postApprovalTxn.trackingCode, new FormInfo(postApprovalTxn))
//        formInfoMap.put(dashboardTxn.trackingCode, new FormInfo(dashboardTxn))
//        FormInfoManager.saveFormInfoToTxns(formInfoMap)

        new MockFormInfoSetupManager()
                .addDashboardTxn(dashboardTxnProxy)
                .addConsumerTxn(preApprovalTxn)
                .addConsumerTxn(postApprovalTxn)
                .saveMockFormInfo()

        FormFuncResult result = invokeService([:])
        logger.info result
        assert result != null
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.SUCCESS.toString()
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS_MESSAGE) == "no consumer deposit products or eligible account"
    }


    /*
* Perform service unit test
*
* throws exception if unit test fails
*/

    @Test
    void testGetAccessTokenFromAtomicWithNewProductsSuccess() throws Exception {
        testXml = testParams["Test XML Data"]
        dashboardTxn = new MockVoBuilder().createTxnSavedWithXml(testXml)
        dashboardTxnProxy = new TxnProxy(dashboardTxn)
        TxnProxy preApprovalTxn = buildTxn(testXml)
        //TxnProxy postApprovalTxn = buildTxn(testXml)
        TxnProxy consumerPostApprovalTxnProxy = buildTxn(testXml)
        //consumerPostApprovalTxnProxy.setProperty(FiservUtils.buildExistingAccountsUUIDTxnPropKey(FnbBaseSvc.APPLICANT_KEY_PRIMARY), new JsonBuilder(accountMap).toString())
        consumerPostApprovalTxnProxy.setProperty(AtomicQuery.TXN_PROP_CONSUMER_ACCOUNT_MAP, new JsonBuilder(accountMap).toString())
        consumerPostApprovalTxnProxy.setFormStatus(Txn.FORM_COMPLETED)

        //new TxnUpdater(postApprovalTxn).setFormStatus(Txn.FORM_COMPLETED).update()
        preApprovalTxn.setFormStatus(Txn.FORM_COMPLETED)
        new MockFormInfoSetupManager()
                .addDashboardTxn(dashboardTxnProxy)
                .addConsumerTxn(preApprovalTxn)
                .addConsumerTxn(consumerPostApprovalTxnProxy)
                .saveMockFormInfo()
        mockRequestAndResponse("Response Success", 200, consumerPostApprovalTxnProxy)
        FormFuncResult result = invokeService([:])
        logger.info result
        assert result != null
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.ERROR.toString()
        assert !result.data.get(AtomicGetAccessTokenFluentFunction.RESULT_DATA_KEY_ACCESS_TOKEN).toString().isBlank()

        assert !dashboardTxnProxy.getTxnProperty("${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.${AtomicGetAccessTokenFluentFunction.SERVICE_NAME}.Request").isBlank()
        assert !dashboardTxnProxy.getTxnProperty("${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.${AtomicGetAccessTokenFluentFunction.SERVICE_NAME}.Response").isBlank()
    }

/*
    @Test
    void testGetAccessTokenFromAtomicWithExistingAccountsSuccess() throws Exception {
        testXml = testParams["No Consumer Deposit Products XML"]
//        dashboardTxn = new MockVoBuilder().createTxnSavedWithXml(testXml)
//        dashboardTxnProxy = new TxnProxy(dashboardTxn)
        TxnProxy preApprovalTxn = buildTxn(testXml)
        //TxnProxy postApprovalTxn = buildTxn(testXml)
        TxnProxy consumerPostApprovalTxnProxy = buildTxn(testXml)
        consumerPostApprovalTxnProxy.setProperty(FiservUtils.buildExistingAccountsUUIDTxnPropKey(FnbBaseSvc.APPLICANT_KEY_PRIMARY), new JsonBuilder(accountMap).toString())
        String existingAccountInfo = testParams["Txn Prop Existing Account Info"] as String
        consumerPostApprovalTxnProxy.setProperty(FiservUtils.buildExistingAccountsInfoTxnPropKey(FnbBaseSvc.APPLICANT_KEY_PRIMARY), existingAccountInfo)
        //consumerPostApprovalTxnProxy.setProperty(AtomicQuery.TXN_PROP_CONSUMER_ACCOUNT_MAP, new JsonBuilder(accountMap).toString())
        consumerPostApprovalTxnProxy.setFormStatus(Txn.FORM_COMPLETED)
        //new TxnUpdater(postApprovalTxn).setFormStatus(Txn.FORM_COMPLETED).update()
        preApprovalTxn.setFormStatus(Txn.FORM_COMPLETED)
        new MockFormInfoSetupManager()
                .addDashboardTxn(dashboardTxnProxy)
                .addConsumerTxn(preApprovalTxn)
                .addConsumerTxn(consumerPostApprovalTxnProxy)
                .saveMockFormInfo()
        mockRequestAndResponse("Response Success", 200, consumerPostApprovalTxnProxy)
        FormFuncResult result = invokeService([:])
        logger.info result
        assert result != null
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.SUCCESS.toString()
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS_MESSAGE) == AtomicGetAccessTokenFluentFunction.RESULT_MESSAGE_CREATED_NEW_ACCESS_TOKEN
        assert !result.data.get(AtomicGetAccessTokenFluentFunction.RESULT_DATA_KEY_ACCESS_TOKEN).toString().isBlank()
        assert !dashboardTxnProxy.getTxnProperty(AccessTokenManager.TXN_PROP_ACCESS_TOKEN).isBlank()
        assert result.data.get(AtomicGetAccessTokenFluentFunction.RESULT_DATA_KEY_ACCESS_TOKEN) == accessToken
        assert !dashboardTxnProxy.getTxnProperty("${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.${AtomicGetAccessTokenFluentFunction.SERVICE_NAME}.Request").isBlank()
        assert !dashboardTxnProxy.getTxnProperty("${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.${AtomicGetAccessTokenFluentFunction.SERVICE_NAME}.Response").isBlank()
    }


 */
    /*
* Perform service unit test
*
* throws exception if unit test fails
*/

    /*
    @Test
    void testGetAccessTokenFromAtomicMissingIdentifier() throws Exception {
        testXml = testParams["Test XML Data"]
//        dashboardTxn = new MockVoBuilder().createTxnSavedWithXml(testXml)
//        dashboardTxnProxy = new TxnProxy(dashboardTxn)
//        Txn preApprovalTxn = buildTxn(testXml)
        //TxnProxy postApprovalTxn = buildTxn(testXml)
        TxnProxy consumerPostApprovalTxnProxy = buildTxn(testXml)
        consumerPostApprovalTxnProxy.setProperty(AtomicQuery.TXN_PROP_CONSUMER_ACCOUNT_MAP, new JsonBuilder(accountMap).toString())
        //new TxnUpdater(postApprovalTxn).setFormStatus(Txn.FORM_COMPLETED).update()
        postApprovalTxn.setFormStatus(Txn.FORM_COMPLETED)
        new MockFormInfoSetupManager()
                .addDashboardTxn(dashboardTxnProxy)
                .addConsumerTxn(postApprovalTxn)
                .saveMockFormInfo()
        mockRequestAndResponse("Response Missing Identifier", 400, consumerPostApprovalTxnProxy)
        FormFuncResult result = invokeService([:])
        logger.info result
        assert result != null
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.ERROR.toString()
        assert result.data.get(AtomicGetAccessTokenFluentFunction.RESULT_DATA_KEY_ACCESS_TOKEN) == null
        assert dashboardTxnProxy.getTxnProperty(AccessTokenManager.TXN_PROP_ACCESS_TOKEN) == null
        assert !dashboardTxnProxy.getTxnProperty("${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.${AtomicGetAccessTokenFluentFunction.SERVICE_NAME}.Request").isBlank()
        assert !dashboardTxnProxy.getTxnProperty("${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.${AtomicGetAccessTokenFluentFunction.SERVICE_NAME}.Response").isBlank()
    }
*/

    /*
* Perform service unit test
*
* throws exception if unit test fails
*/
/*
    @Test
    void testGetAccessTokenFromAtomicWrongAPIKey() throws Exception {
        testXml = testParams["Test XML Data"]
//        dashboardTxn = new MockVoBuilder().createTxnSavedWithXml(testXml)
//        dashboardTxnProxy = new TxnProxy(dashboardTxn)
//        Txn preApprovalTxn = buildTxn(testXml)
        TxnProxy postApprovalTxn = buildTxn(testXml)

        TxnProxy consumerPostApprovalTxnProxy = buildTxn(testXml)
        consumerPostApprovalTxnProxy.setProperty(AtomicQuery.TXN_PROP_CONSUMER_ACCOUNT_MAP, new JsonBuilder(accountMap).toString())
        //new TxnUpdater(postApprovalTxn).setFormStatus(Txn.FORM_COMPLETED).update()
        postApprovalTxn.setFormStatus(Txn.FORM_COMPLETED)
        new MockFormInfoSetupManager()
                .addDashboardTxn(dashboardTxnProxy)
                .addConsumerTxn(postApprovalTxn)
                .saveMockFormInfo()
        mockRequestAndResponse("Response Wrong API Key", 401, consumerPostApprovalTxnProxy)
        FormFuncResult result = invokeService([:])
        logger.info result
        assert result != null
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.ERROR.toString()
        assert result.data.get(AtomicGetAccessTokenFluentFunction.RESULT_DATA_KEY_ACCESS_TOKEN) == null
        assert dashboardTxnProxy.getTxnProperty(AccessTokenManager.TXN_PROP_ACCESS_TOKEN) == null
        assert !dashboardTxnProxy.getTxnProperty("${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.${AtomicGetAccessTokenFluentFunction.SERVICE_NAME}.Request").isBlank()
        assert !dashboardTxnProxy.getTxnProperty("${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.${AtomicGetAccessTokenFluentFunction.SERVICE_NAME}.Response").isBlank()
    }
*/

    FormFuncResult invokeService(Map<String, String> params) {
        FuncParam funcParam = TestUtils.createMockFuncParam(params, dashboardTxnProxy, new XmlDoc(testXml), svcDefWithConfig)
        FormFuncResult result = (FormFuncResult) new ServiceInvoker(svcDefWithConfig)
                .setLogger(logger)
                .invoke("funcParam", funcParam)
        return result
    }

    static TxnProxy buildTxn(String formXml) {
        return TxnMockingManager.buildTestTxn(
                "https://dev.fnb.avoka-transact.com/workspaces-future/app/post-approval",
                formXml, PageConstants.FormCodes.FORM_CODE_FNB_CONSUMER.name, "Workspaces", Txn.FORM_ASSIGNED, Txn.DELIVERY_NOT_READY
        )
    }

    void mockRequestAndResponse(String mockResponseResourceName, int responseStatus, TxnProxy consumerPostApprovalTxnProxy) {
        String mockResponse = testParams[mockResponseResourceName]
        List<Product> products = Product.restoreProductsFromXml(dashboardTxnProxy.getAppDoc())
        products = Product.filterForConsumerDepositProducts(products)
        Map<String, String> existingAccountsMap = AccessTokenRequestHelper.getExistingAccountsMap(consumerPostApprovalTxnProxy)
        AtomicCreateAccessTokenRequest request = new AtomicQuery(logger, dashboardTxnProxy).buildCreateAccessTokenRequest(products, existingAccountsMap, existingAccountsMap)
        TestUtils.registerMockRequestResponse(request, responseStatus, mockResponse)
    }

}
