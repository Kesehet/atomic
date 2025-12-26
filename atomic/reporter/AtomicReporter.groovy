package com.temenos.fnb.gsvc.atomic.reporter

import com.avoka.tm.query.TxnQuery
import com.avoka.tm.util.Contract
import com.avoka.tm.util.Logger
import com.avoka.tm.vo.Txn
import com.google.gson.Gson
import com.temenos.fnb.gsvc.atomic.enums.AtomicTransactStatus
import com.temenos.fnb.gsvc.atomic.utils.AtomicUtils
import com.temenos.fnb.gsvc.forms.FormInfoManager
import com.temenos.fnb.utils.TxnProxy

class AtomicReporter {
    static final String TXN_PROP_PREFIX_REPORTER = "${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.reporting"
    static final String TXN_PROP_REPORTER_STATUS = "${TXN_PROP_PREFIX_REPORTER}.status"
    static final String TXN_PROP_REPORTER_PERSON_INFO = "${TXN_PROP_PREFIX_REPORTER}.personinfo"
    static final String TXN_PROP_REPORTER_PERSON_ACCOUNT_NAME = "${TXN_PROP_PREFIX_REPORTER}.accountName"
    static final String TXN_PROP_REPORTER_PERSON_ACCOUNT_NUMBER = "${TXN_PROP_PREFIX_REPORTER}.accountNumber"
    static final String TXN_PROP_REPORTER_PERSON__ACCOUNT_TYPE = "${TXN_PROP_PREFIX_REPORTER}.accountType"
    private Logger logger
    private TxnProxy txnProxy
    private AtomicTransactStatus transactStatus
    private Map personalInformation
    private String accountName;
    private String accountNumber;
    private String accountType;

    AtomicReporter(Logger logger, TxnProxy txnProxy) {
        this.logger = logger
        this.txnProxy = txnProxy
    }

    AtomicReporter setTransactStatus(AtomicTransactStatus transactStatus) {
        Contract.notNull(transactStatus, "transactStatus passed to setTransactStatus()")
        this.transactStatus = transactStatus
        return this
    }

    AtomicReporter setAccountName(String accountName) {
        Contract.notNull(accountName, "accountName passed to setAccountName()")
        this.accountName = accountName
        return this
    }

    AtomicReporter setAccountNumber(String accountNumber) {
        Contract.notNull(accountNumber, "accountNumber passed to setAccountNumber()")
        this.accountNumber = accountNumber
        return this
    }

    AtomicReporter setAccountType(String accountType) {
        Contract.notNull(accountType, "accountType passed to setAccountType()")
        this.accountType = accountType
        return this
    }

    private AtomicTransactStatus getTransactStatusFromTxnProps() {
        String transactStatusAsString = txnProxy.getTxnProperty(TXN_PROP_REPORTER_STATUS)
        if (transactStatusAsString == null || transactStatusAsString.isBlank()) {
            return null
        }
        return AtomicTransactStatus.valueOf(transactStatusAsString)
    }

    AtomicReporter setPersonalInformation(Map personalInformation) {
        Contract.notNull(personalInformation, "personalInformation passed to setPersonalInformation()")
        this.personalInformation = personalInformation
        return this
    }

    void report() {
        logger.info("Atomic Reporting for ${this.txnProxy.trackingCode}:")
        if (transactStatus != null) {
            logger.info("Setting txn prop ${TXN_PROP_REPORTER_STATUS} to ${transactStatus.toString()}")
            txnProxy.setProperty(TXN_PROP_REPORTER_STATUS, transactStatus.toString())
        }
        if (null != personalInformation) {
            logger.info("Setting Person Info  ${TXN_PROP_REPORTER_STATUS} to ${personalInformation.toString()}")
            txnProxy.setProperty(TXN_PROP_REPORTER_PERSON_INFO, new Gson().toJson(personalInformation))
        }
        if (null != accountName) {
            logger.info("Setting accountName   ${TXN_PROP_REPORTER_PERSON_ACCOUNT_NAME} to ${accountName.toString()}")
            txnProxy.setProperty(TXN_PROP_REPORTER_PERSON_ACCOUNT_NAME, new Gson().toJson(accountName))
        }
        if (null != accountNumber) {
            logger.info("Setting accountNumber   ${TXN_PROP_REPORTER_PERSON_ACCOUNT_NUMBER} to ${accountNumber.toString()}")
            txnProxy.setProperty(TXN_PROP_REPORTER_PERSON_ACCOUNT_NUMBER, new Gson().toJson(accountNumber))
        }
        if (null != accountType) {
            logger.info("Setting accountType   ${TXN_PROP_REPORTER_PERSON__ACCOUNT_TYPE} to ${accountType.toString()}")
            txnProxy.setProperty(TXN_PROP_REPORTER_PERSON__ACCOUNT_TYPE, new Gson().toJson(accountType))
        }
        /**
         Set the properties in the Customer Post-Approval Transaction
         */
        setAccountInfotoPostApprovalConsumerTxn()
    }

    boolean hasUserFinishedAtomicSDK() {
        AtomicTransactStatus status = getTransactStatusFromTxnProps()
        if (status == null) {
            return false
        }
        return status == AtomicTransactStatus.COMPLETED || status == AtomicTransactStatus.FINISHED
    }
/**
 * Setting the Transaction Properties to Post approval Consumer Txn
 */
    private void setAccountInfotoPostApprovalConsumerTxn() {
        logger.info(" ** setAccountInfotoPostApprovalConsumerTxn **")
        Txn dashboardTxn = new TxnQuery().setTrackingCode(txnProxy.trackingCode).withAll().firstValue()
        TxnProxy proxyTxn = new TxnProxy(dashboardTxn)
        FormInfoManager formInfoManager = new FormInfoManager(proxyTxn)
        TxnProxy postApprovalTxn = formInfoManager.getConsumerPostApprovalTxn()
        logger.info("Post Approval Tracking Code " + postApprovalTxn.trackingCode)
        logger.info("Consumer Post Approval Txn ${TXN_PROP_REPORTER_STATUS} to ${transactStatus.toString()}" +
                ", ${TXN_PROP_REPORTER_PERSON_ACCOUNT_NAME} to ${accountName.toString()}" +
                ", ${TXN_PROP_REPORTER_PERSON_ACCOUNT_NUMBER} to ${accountNumber.toString()}" +
                ", ${TXN_PROP_REPORTER_PERSON__ACCOUNT_TYPE} to ${accountType.toString()}"
        )
        logger.info("Consumer Post Approval Txn ${TXN_PROP_REPORTER_PERSON_ACCOUNT_NAME} to ${accountName.toString()}")

        if (null != postApprovalTxn) {
            postApprovalTxn.setProperty(TXN_PROP_REPORTER_STATUS, new Gson().toJson(this.transactStatus.toString()))
            postApprovalTxn.setProperty(TXN_PROP_REPORTER_PERSON_ACCOUNT_NAME, new Gson().toJson(accountName))
            postApprovalTxn.setProperty(TXN_PROP_REPORTER_PERSON_ACCOUNT_NUMBER, new Gson().toJson(accountNumber))
            postApprovalTxn.setProperty(TXN_PROP_REPORTER_PERSON__ACCOUNT_TYPE, new Gson().toJson(accountType))
        }
    }
}