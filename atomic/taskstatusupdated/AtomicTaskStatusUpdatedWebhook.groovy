package com.temenos.fnb.gsvc.atomic.taskstatusupdated

import com.avoka.taf.dao.viewmodel.ViewCustomer
import com.avoka.taf.dao.viewmodel.ViewCustomers
import com.avoka.tm.http.HttpRequest
import com.avoka.tm.http.PostRequest
import com.avoka.tm.query.TxnQuery
import com.avoka.tm.util.*
import com.avoka.tm.vo.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.temenos.fnb.gsvc.atomic.enums.AtomicTransactStatus

import com.temenos.fnb.gsvc.atomic.reporter.AtomicReporter
import com.temenos.fnb.gsvc.atomic.utils.AtomicUtils
import com.temenos.fnb.gsvc.atomic.webhook.requests.taskstatusupdated.AtomicTaskStatusUpdatedRequest
import com.temenos.fnb.utils.TxnProxy
import com.temenos.fnb.utils.TxnUtils
import groovy.transform.TypeChecked
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.w3c.dom.Document

import javax.servlet.http.*

@TypeChecked
public class AtomicTaskStatusUpdatedWebhook {

    public Logger logger

    static final String SERVICE_NAME = "taskStatusUpdated"
    static final String TXN_PROP_SERVICE_PREFIX = "${AtomicUtils.TXN_PROP_PREFIX_ATOMIC}.${SERVICE_NAME}"
    static final String SUCCESS_RESPONSE = "SUCCESS"
    static final String UTF_8_ENCODING = "UTF-8"

    /**
     * Webhook Callback API for Atomic taskStatusUpdate
     */
    Object invoke(SvcDef svcDef, HttpServletRequest request, User user, Map params) {
        String trackingCode = null
        try {


            String requestBody = parseRequestBody(request)
            validateRequestBody(requestBody)

            AtomicTaskStatusUpdatedRequest atomicRequest = parseAtomicRequest(requestBody)


            trackingCode = extractTrackingCode(atomicRequest)
            logger.info("Processing atomic task status update for tracking code: ${trackingCode}")

            Txn txn = findTransactionByTrackingCode(trackingCode)
            validateTransaction(txn, trackingCode)
            //Extract Primary Customer
            TxnProxy txnProxy = new TxnProxy(txn)
            Map<String, String> primaryCustomerInfo = extractPrimaryCustomerInfo(txnProxy)
            // Create HTTP request
            HttpRequest httpRequest = createHttpRequest(request, requestBody)

            // Process transaction
            processTransaction(txn, httpRequest, request.getRequestURL().toString())

            // Update transaction status
            updateTransactionStatus(txn, atomicRequest)

            logger.info("Successfully processed atomic task status update for tracking code: ${trackingCode}")
            return SUCCESS_RESPONSE

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for tracking code [${trackingCode}]: ${e.message}")
            throw e
        } catch (IllegalStateException e) {
            logger.error("Processing error for tracking code [${trackingCode}]: ${e.message}")
            throw e
        } catch (Exception e) {
            logger.error("Unexpected error processing atomic task status update for tracking code [${trackingCode}]: ${e.message}")
            throw new RuntimeException("Internal server error", e)
        }
    }




    private String parseRequestBody(HttpServletRequest request) {
        try {
            String body = IOUtils.toString(request.getInputStream(), UTF_8_ENCODING)
            logger.debug("Request body parsed successfully, length: ${body?.length() ?: 0}")
            return body
        } catch (IOException e) {
            logger.error("Failed to read request body: ${e.message}")
            throw new IllegalArgumentException("Unable to read request body", e)
        } catch (Exception e) {
            logger.error("Unexpected error parsing request body: ${e.message}")
            throw new IllegalArgumentException("Invalid request body format", e)
        }
    }


    private void validateRequestBody(String requestBody) {
        if (StringUtils.isBlank(requestBody)) {
            logger.error("Empty or blank request body received")
            throw new IllegalArgumentException("Request body is required and cannot be empty")
        }
    }


    private AtomicTaskStatusUpdatedRequest parseAtomicRequest(String requestBody) {
        try {
            Gson gson = new Gson()
            AtomicTaskStatusUpdatedRequest request = gson.fromJson(requestBody, AtomicTaskStatusUpdatedRequest.class)
            logger.debug("Atomic request parsed successfully")
            return request
        } catch (JsonSyntaxException e) {
            logger.error("Invalid JSON syntax in request body: ${e.message}")
            throw new IllegalArgumentException("Invalid JSON format in request body", e)
        } catch (Exception e) {
            logger.error("Failed to parse atomic request: ${e.message}", )
            throw new IllegalArgumentException("Unable to parse request data", e)
        }
    }


    private String extractTrackingCode(AtomicTaskStatusUpdatedRequest atomicRequest) {
        String trackingCode = atomicRequest.user.identifier
        if (StringUtils.isBlank(trackingCode)) {
            throw new IllegalArgumentException("Tracking code (user identifier) is required")
        }
        return trackingCode.trim()
    }


    private Txn findTransactionByTrackingCode(String trackingCode) {
        try {
            logger.debug("Searching for transaction with tracking code: ${trackingCode}")
            Txn txn = new TxnQuery()
                    .setTrackingCode(trackingCode)
                    .withAll()
                    .firstValue()
            logger.debug("Transaction query completed for tracking code: ${trackingCode}")
            return txn
        } catch (Exception e) {
            logger.error("Failed to query transaction for tracking code [${trackingCode}]: ${e.message}")
            throw new IllegalStateException("Database query failed for tracking code: ${trackingCode}", e)
        }
    }


    private void validateTransaction(Txn txn, String trackingCode) {
        if (!txn) {
            logger.warn("No transaction found for tracking code: ${trackingCode}")
            throw new IllegalStateException("Transaction not found for tracking code: ${trackingCode}")
        }
        logger.debug("Transaction found for tracking code: ${trackingCode}, ID: ${txn.id}")
    }


    private void processTransaction(Txn txn, HttpRequest httpRequest, String requestUrl) {
        try {
            TxnProxy txnProxy = new TxnProxy(txn)
            new TxnUtils(txnProxy, TXN_PROP_SERVICE_PREFIX, "", requestUrl)
                    .addNonPrefix()
                    .addHttpRequest(httpRequest)
                    .showAll()
                    .processServiceRequestResponse()
            logger.debug("Transaction processed successfully for ID: ${txn.id}")
        } catch (Exception e) {
            logger.error("Failed to process transaction [${txn?.id}]: ${e.message}")
            throw new IllegalStateException("Transaction processing failed", e)
        }
    }


    private void updateTransactionStatus(Txn txn, AtomicTaskStatusUpdatedRequest atomicRequest) {
        try {
            String statusString = atomicRequest.data.status.toUpperCase().trim()
            logger.debug("Converting status string to enum: ${statusString}")

            AtomicTransactStatus atomicTransactionStatus = AtomicTransactStatus.valueOf(statusString)

            TxnProxy txnProxy = new TxnProxy(txn)
            new AtomicReporter(logger, txnProxy)
                    .setPersonalInformation(extractPrimaryCustomerInfo(txnProxy))
                    .setTransactStatus(atomicTransactionStatus)
                    .report()


            logger.info("Transaction status updated to [${atomicTransactionStatus}] for tracking code: ${atomicRequest.user.identifier}")
        } catch (IllegalArgumentException e) {
            logger.error("Invalid status value [${atomicRequest.data.status}]: ${e.message}")
            throw new IllegalArgumentException("Invalid status value: ${atomicRequest.data.status}", e)
        } catch (Exception e) {
            logger.error("Failed to update transaction status for ID [${txn?.id}]: ${e.message}")
            throw new IllegalStateException("Status update failed", e)
        }
    }


    private HttpRequest createHttpRequest(HttpServletRequest request,String requestBody) {
        try {
            HttpRequest httpRequest = new PostRequest(request.getRequestURL().toString())
            httpRequest.setMessage(requestBody)
            copyRequestHeaders(request, httpRequest)

            logger.debug("HTTP request created successfully")
            return httpRequest
        } catch (Exception e) {
            logger.error("Failed to create HTTP request: ${e.message}")
            throw new IllegalStateException("HTTP request creation failed", e)
        }
    }


    private void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest httpRequest) {
        try {
            Enumeration<String> headerNames = servletRequest.getHeaderNames()

            while (headerNames?.hasMoreElements()) {
                String headerName = headerNames.nextElement()
                String headerValue = servletRequest.getHeader(headerName)

                if (StringUtils.isNotBlank(headerName) && StringUtils.isNotBlank(headerValue)) {
                    httpRequest.addHeader(headerName, headerValue)
                }
            }

            logger.debug("Copied  headers to HTTP request")
        } catch (Exception e) {
            logger.warn("Failed to copy some request headers: ${e.message}")
        }
    }


    /**
     * Extracts primary customer information from TxnProxy

     */
    private Map<String, String> extractPrimaryCustomerInfo(TxnProxy txnProxy) {
        try {
            logger.info("Inside extractPrimaryCustomerInfo")
            Document appDoc
            appDoc = new XmlDoc(txnProxy.formXml).document
            ViewCustomers customers = ViewCustomers.restoreViewCustomersFromXml(appDoc)

            // Get primary customer
            ViewCustomer primaryCustomer = customers.primary

            if (!primaryCustomer) {
                logger.warn("No primary customer found in transaction")
                return [:]
            }
//TODO Account Information needed
            // Extract primary customer information
            Map<String, String> customerInfo = [
                    firstName: primaryCustomer.firstName ?: '',
                    lastName: primaryCustomer.lastName ?: '',
                    fullName: primaryCustomer.fullName ?: '',
                    ssn: primaryCustomer.tin ?: '',
                    dateOfBirth: primaryCustomer.dateOfBirth ?: '',
            ]


            return customerInfo

        } catch (Exception e) {
            logger.error("Failed to extract primary customer information: ${e.message}")
            return [:]
        }
    }


}