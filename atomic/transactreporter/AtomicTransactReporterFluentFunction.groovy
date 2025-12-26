package com.temenos.fnb.gsvc.atomic.transactreporter


import com.avoka.tm.func.FormFuncResult
import com.avoka.tm.func.FuncParam
import com.avoka.tm.func.FuncResult
import com.avoka.tm.util.Contract
import com.temenos.fnb.gsvc.FnbBaseFluentFunction
import com.temenos.fnb.gsvc.atomic.enums.AtomicTransactStatus
import com.temenos.fnb.gsvc.atomic.reporter.AtomicReporter
import groovy.transform.TypeChecked

@TypeChecked
class AtomicTransactReporterFluentFunction extends FnbBaseFluentFunction {
    static final String SVC_PARAM_TRANSACT_STATUS = "transactStatus"
    static final String SVC_PARAM_ACCOUNT_NAME = "accountName"
    static final String SVC_PARAM_ACCOUNT_NUMBER = "accountNumber"
    static final String SVC_PARAM_ACCOUNT_TYPE = "accountType"


    /*
     * Perform Fluent Function call.
     *
     * returns: FuncResult
     */

    FuncResult run(FuncParam param) {
        String transactStatusAsString = param.params.get(SVC_PARAM_TRANSACT_STATUS)
        String accountName = param.params.get(SVC_PARAM_ACCOUNT_NAME)
        String accountNumber = param.params.get(SVC_PARAM_ACCOUNT_NUMBER)
        String accountType = param.params.get(SVC_PARAM_ACCOUNT_TYPE)
        Contract.notBlank(transactStatusAsString, "transactStatus svc param")
        Contract.notBlank(accountName, "accountName svc param")
        Contract.notBlank(accountNumber, "accountNumber svc param")
        Contract.notBlank(accountType, "accountType svc param")
        logger.info(" accountName :: " + accountName)
        logger.info(" accountNumber :: " + accountNumber)
        logger.info(" accountType :: " + accountType)
        AtomicTransactStatus transactStatus = AtomicTransactStatus.valueOf(transactStatusAsString.toUpperCase())
        new AtomicReporter(logger, txnProxy)
                .setTransactStatus(transactStatus)
                .setAccountName(accountName)
                .setAccountNumber(accountNumber)
                .setAccountType(accountType)
                .report()
        FormFuncResult result = generateFormFuncResult(ServiceStatusType.SUCCESS, "")
        return result
    }
}