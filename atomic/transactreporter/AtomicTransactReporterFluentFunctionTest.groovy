package com.temenos.fnb.gsvc.atomic.transactreporter

import com.avoka.taf.config.Env
import com.avoka.tm.func.FormFuncResult
import com.avoka.tm.func.FuncParam
import com.avoka.tm.svc.ServiceInvoker
import com.avoka.tm.test.MockVoBuilder
import com.avoka.tm.util.XmlDoc
import com.avoka.tm.vo.SvcDef
import com.temenos.fnb.gsvc.FnbBaseSvc
import com.temenos.fnb.gsvc.FnbBaseSvcTestBase
import com.temenos.fnb.gsvc.atomic.enums.AtomicTransactStatus
import com.temenos.fnb.utils.TestUtils
import com.temenos.fnb.utils.TxnProxy
import groovy.transform.TypeChecked
import org.junit.Before
import org.junit.Test

@TypeChecked
class AtomicTransactReporterFluentFunctionTest extends FnbBaseSvcTestBase {

    private String testXml
    private SvcDef svcDefWithConfig
    private TxnProxy txnProxy

    @Before
    void setup() {
        Map<String, Object> envVars = [:]
        Env.setVars(envVars)
        svcDefWithConfig = new SvcDef(svcDef, envVars as Map<String, String>)
        testXml = testParams["Test XML Data"]
        txnProxy = new TxnProxy(new MockVoBuilder().createTxnSavedWithXml(testXml))
    }

    /*
     * Perform service unit test
     *
     * throws exception if unit test fails
     */

    @Test
    void testTransactStatusAbandoned() throws Exception {
        Map<String, String> params = [
                (AtomicTransactReporterFluentFunction.SVC_PARAM_TRANSACT_STATUS): AtomicTransactStatus.ABANDONED.toString()
        ] as Map<String, String>
        FormFuncResult result = invokeService(params)
        logger.info(result)
        assert result != null
        //assert !(result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.SUCCESS.toString())
        // assert txnProxy.getTxnProperty(AtomicReporter.TXN_PROP_REPORTER_STATUS).equalsIgnoreCase(AtomicTransactStatus.ABANDONED.toString())
    }

    @Test
    void testTransactStatusInvalid() throws Exception {
        Map<String, String> params = [
                (AtomicTransactReporterFluentFunction.SVC_PARAM_TRANSACT_STATUS): "Invalid"
        ]
        FormFuncResult result = invokeService(params)
        logger.info(result)
        assert result != null
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.ERROR.toString()
    }

    @Test
    void testTransactStatusNone() throws Exception {
        FormFuncResult result = invokeService([:])
        logger.info(result)
        assert result != null
        assert result.data.get(FnbBaseSvc.SERVICE_STATUS) == FnbBaseSvc.ServiceStatusType.ERROR.toString()
    }

    FormFuncResult invokeService(Map<String, String> params) {
        FuncParam funcParam = TestUtils.createMockFuncParam(params, txnProxy, new XmlDoc(testXml), svcDefWithConfig)
        FormFuncResult result = (FormFuncResult) new ServiceInvoker(svcDefWithConfig)
                .setLogger(logger)
                .invoke("funcParam", funcParam)
        return result
    }

}