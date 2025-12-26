package com.temenos.fnb.gsvc.atomic.taskstatusupdated;

import com.avoka.tm.svc.*
import com.avoka.tm.test.*
import com.avoka.tm.util.*
import com.avoka.tm.vo.*
import groovy.transform.TypeChecked
import org.junit.*

@TypeChecked
public class AtomicTaskStatusUpdatedWebhookTest extends TransactSDKUnitTest {

    /*
     * Perform service unit test
     *
     * throws exception if unit test fails
     */
    @Test
    void test() throws Exception {

        Map args = [
            "formId": 23
        ]

        Map params = [
            "svcDef": svcDef,
            "request": null,
            "user": null,
            "params": args
        ]

        def result = new ServiceInvoker(svcDef)
            .setLogger(logger)
            .invoke(params)

        // Check result
        logger.info result

        assert "groovy result - 23" == result
    }
}