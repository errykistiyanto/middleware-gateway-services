package co.id.middleware.finnet.controller.prepaid.xl;

import co.id.middleware.finnet.domain.payment.PaymentFailed;
import co.id.middleware.finnet.domain.payment.PaymentRequest;
import co.id.middleware.finnet.domain.payment.PaymentResponse;
import co.id.middleware.finnet.domain.payment.PaymentSuccess;
import co.id.middleware.finnet.repository.HistoryService;
import co.id.middleware.finnet.utils.FinnetRCTextParser;
import co.id.middleware.finnet.utils.Logging;
import co.id.middleware.finnet.utils.ReceiptTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.iso.QMUX;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
import org.jpos.util.NameRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author briantomo80@gmail.com 23/08/23
 */

@RestController
@Slf4j
@Component("PaymentPrepaidXL")
public class Payment {

    @Autowired
    private Environment env;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Logging logging;

    @Autowired
    private ReceiptTransaction receiptTransaction;

    //logstash message direction
    public static final String service = "payment";
    public static final String req_from = "request from ";
    public static final String req_to = "request to ";
    public static final String resp_from = "response from ";
    public static final String resp_to = "response to ";
    public static final String in_req = "incoming request";
    public static final String out_resp = "outgoing response";
    //logstash message direction

    @RequestMapping(value = "/v1.0/payment/xl-prepaid", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> Multibiller(@Valid @RequestBody PaymentRequest paymentRequest,
                                              HttpServletRequest httpServletRequest, @RequestParam Map<String, Object> requestParam,
                                              @RequestHeader Map<String, Object> requestHeader) throws JsonProcessingException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        PaymentSuccess paymentSuccess = new PaymentSuccess();
        PaymentFailed paymentFailed = new PaymentFailed();
        PaymentResponse paymentResponse = new PaymentResponse();

        //UUID privateData
        String uuid = UUID.randomUUID().toString();

        //env properties
        String method = httpServletRequest.getMethod();
        String uri = httpServletRequest.getRequestURI();
        // String address = request.getLocalAddr();
        String address = env.getProperty("ip.address");
        Integer getLocalPort = httpServletRequest.getLocalPort();
        String port = getLocalPort.toString();

        String URI = env.getProperty("finnet.address") + env.getProperty("finnet.uri");
        String finnetAddress = env.getProperty("finnet.address");
        String finnetUri = env.getProperty("finnet.uri");
//        String fee = env.getProperty("finnet.fee.xl-prepaid");
//        String destinationAccount = env.getProperty("finnet.ss.destinationAccount");
//        String feeAccount = env.getProperty("finnet.ss.feeAccount");
        String validationProductCode = env.getProperty("finnet.productCode.xl-prepaid");

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date now = new Date();
        String date = sdfDate.format(now);

        DecimalFormat df = new DecimalFormat("#,###");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setCurrencySymbol("");
        df.setDecimalFormatSymbols(dfs);

        String pan = paymentRequest.getPan();
        String stan = paymentRequest.getStan();
        String retrievalReferenceNumber = paymentRequest.getRetrievalReferenceNumber();
        String amount = paymentRequest.getAmount();
        String transactionDateTime = paymentRequest.getTransactionDateTime();
        String expiredDate = paymentRequest.getExpiredDate();
        String settlementDate = paymentRequest.getSettlementDate();
        String acquiringCode = paymentRequest.getAcquiringCode();
        String forwardingCode = paymentRequest.getForwardingCode();
        String channelCode = paymentRequest.getChannelCode();
        String terminalId = paymentRequest.getTerminalId();
        String clientId = paymentRequest.getClientId();
        String locationName = paymentRequest.getLocationName();
        String transactionYear = paymentRequest.getTransactionYear();
        String productCode = paymentRequest.getProductCode();
        String accountNumber = paymentRequest.getAccountNumber();
        String fee = paymentRequest.getFee();
        String destinationAccount = paymentRequest.getDestinationAccount();
        String feeAccount = paymentRequest.getFeeAccount();
        String sourceAccount = paymentRequest.getSourceAccount();
        String privateData = paymentRequest.getPrivateData();
        String productName = paymentRequest.getProductName();

        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        LinkedHashMap<String, Object> param = new LinkedHashMap<>();
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();

        //logstash message custom
        logging.restAPI(
                "" + service,
                "",
                req_from + "gateway",
                "" + retrievalReferenceNumber,
                "" + accountNumber,
                "" + "finnet-svc",
                paymentRequest,
                requestHeader,
                requestParam,
                "" + method,
                "" + uri,
                "" + address,
                "" + port,
                ""
        );
        //logstash message custom

        Space psp = SpaceFactory.getSpace("jdbm:myspace");
        String TRACES = "JPTS_TRACE";
        long traceNumber = SpaceUtil.nextLong(psp, "JPTS_TRACE") % 100000L;

        ValueOperations<String, String> values = redisTemplate.opsForValue();
        String key = values.get("co:id:bankdki:openapi:multibiller:" + clientId + privateData);

        if (key != null) {

            log.info("redis get " + "co:id:bankdki:openapi:multibiller:" + clientId + privateData);

            Gson gson2 = new Gson();
            Map<String, String> map2 = gson2.fromJson(key, Map.class);
            String redis_privateData = map2.get("privateData");
            String redis_reserveData = map2.get("reserveData");

            try {

                ISOMsg m = new ISOMsg();
                m.setMTI("0200");
                m.set(2, ISOUtil.zeropad(pan, 18));
                m.set(3, "501099");
                m.set(4, ISOUtil.zeropad(amount, 12));
                m.set(7, transactionDateTime);
                m.set(11, stan);
                m.set(12, transactionDateTime.substring(4, 10));
                m.set(13, ISODate.getDate(new Date()));
                m.set(14, expiredDate);
                m.set(15, settlementDate);
                m.set(18, channelCode);
                m.set(32, acquiringCode);
                m.set(33, forwardingCode);
                m.set(37, retrievalReferenceNumber);
                m.set(41, terminalId);
                m.set(42, clientId);
                m.set(43, "BPD DKI                              IDN");
                m.set(49, "360");
                m.set(61, ISOUtil.zeropad(redis_privateData, 13));
                m.set(103, "017000");

                // logstash
                Map<String, Object> mapRequestISO = new HashMap<>();
                for (int i = 0; i <= m.getMaxField(); i++) {
                    if (m.hasField(i)) {
                        mapRequestISO.put(String.valueOf(i), m.getString(i));
                    }
                }

                //logstash message custom
                logging.restAPI(
                        "" + service,
                        "",
                        req_to + "finnet",
                        "" + retrievalReferenceNumber,
                        "" + accountNumber,
                        "" + "finnet",
                        payload,
                        mapRequestISO,
                        param,
                        "",
                        "" + finnetUri,
                        "" + finnetAddress,
                        "",
                        ""
                );
                //logstash message custom

                QMUX qmux = (QMUX) NameRegistrar.get("mux.multibiller-mux");
                ISOMsg resp = qmux.request(m, 40000);

                System.out.println("resp payment = " + resp);

                if (resp != null) {

                    Map<String, Object> mapResponseISO = new HashMap<>();
                    for (int i = 0; i <= resp.getMaxField(); i++) {
                        if (resp.hasField(i)) {
                            mapResponseISO.put(String.valueOf(i), resp.getString(i));
                        }
                    }

                    //logstash message custom
                    logging.restAPI(
                            "" + service,
                            "",
                            resp_from + "finnet",
                            "" + retrievalReferenceNumber,
                            "" + accountNumber,
                            "" + "finnet",
                            mapResponseISO,
                            header,
                            param,
                            "",
                            "",
                            "",
                            "",
                            ""
                    );
                    //logstash message custom

                    StringBuffer screen = new StringBuffer();
                    if (resp.getString(39).equals("00")) {
                        screen.append(receiptTransaction.receiptHeader("Pembelian Pulsa XL"));

//                        screen.append("Pembelian Pulsa XL");
//                        screen.append("|");
//                        screen.append("|");

                        if (!channelCode.equals("6015")) { //!channelCode Open API
                            screen.append(receiptTransaction.receiptInfo("Ref", retrievalReferenceNumber));
                            screen.append(receiptTransaction.receiptInfo("Waktu", date + " WIB"));

//                            screen.append("Ref   : ");
//                            screen.append(retrievalReferenceNumber);
//                            screen.append("|");
//                            screen.append("Waktu : ");
//                            screen.append((date) + " WIB");
//                            screen.append("|");
                        }
                        screen.append(receiptTransaction.receiptInfo("Nomor Handphone", "0" + Long.valueOf(resp.getString(61).substring(0, 13))));
                        screen.append(receiptTransaction.receiptInfo("Nominal", "IDR " + df.format(Long.valueOf(resp.getString(61).substring(31, 43))).replace(",", ".")));
                        screen.append(receiptTransaction.receiptInfo("Harga", "IDR " + df.format(Long.valueOf(resp.getString(61).substring(31, 43))).replace(",", ".") + ",00"));

//                        screen.append("Nomor Handphone        : ");
//                        screen.append("0" + Long.valueOf(resp.getString(61).substring(0, 13)));
//                        screen.append("|");
//                        screen.append("Nominal                : ");
//                        screen.append("IDR " + df.format(Long.valueOf(resp.getString(61).substring(31, 43))).replace(",", "."));
//                        screen.append("|");
//                        screen.append("Harga                  : ");
//                        screen.append("IDR " + df.format(Long.valueOf(resp.getString(61).substring(31, 43))).replace(",", ".") + ",00");
//                        screen.append("|");
//
                        if (!channelCode.equals("6015")) { //!channelCode Open API
                            screen.append(receiptTransaction.receiptInfo("Biaya Administrasi", "IDR " + df.format(Long.valueOf(fee)).replace(",", ".") + ",00"));
                            screen.append(receiptTransaction.receiptInfo("Total Bayar", "IDR " + df.format(Long.valueOf(resp.getString(61).substring(31, 43)) + Long.valueOf(fee)).replace(",", ".") + ",00"));

//                            screen.append("Biaya Administrasi  : ");
//                            screen.append("IDR " + df.format(Long.valueOf(fee)).replace(",", ".") + ",00");
//                            screen.append("|");
//                            screen.append("Total Bayar         : ");
//                            screen.append("IDR " + df.format(Long.valueOf(resp.getString(61).substring(31, 43)) + Long.valueOf(fee)).replace(",", ".") + ",00");
                        }

                        screen.append(receiptTransaction.noReceiptFooter());

                        paymentSuccess.setResponseCode(resp.getString(39));
                        paymentSuccess.setResponseMessage(FinnetRCTextParser.parse(resp.getString(39), ""));
                        paymentResponse.setPan(pan);
                        paymentResponse.setStan(stan);
                        paymentResponse.setRetrievalReferenceNumber(retrievalReferenceNumber);
                        paymentResponse.setAmount(amount);
                        paymentResponse.setTransactionDateTime(transactionDateTime);
                        paymentResponse.setExpiredDate(expiredDate);
                        paymentResponse.setSettlementDate(settlementDate);
                        paymentResponse.setChannelCode(channelCode);
                        paymentResponse.setAcquiringCode(acquiringCode);
                        paymentResponse.setForwardingCode(forwardingCode);
                        paymentResponse.setTerminalId(terminalId);
                        paymentResponse.setClientId(clientId);
                        paymentResponse.setLocationName(locationName);
                        paymentResponse.setPrivateData(privateData);
                        paymentResponse.setScreen(screen.toString());
                        paymentResponse.setTransactionYear(transactionYear);
                        paymentResponse.setProductCode(productCode);
                        paymentResponse.setAccountNumber(accountNumber);
                        paymentResponse.setFee(fee);
                        paymentResponse.setDestinationAccount(destinationAccount);
                        paymentResponse.setFeeAccount(feeAccount);
                        paymentResponse.setSourceAccount(sourceAccount);
                        paymentResponse.setProductName(productName);
                        paymentSuccess.setData(paymentResponse);

                        //logstash message custom
                        logging.restAPI(
                                "" + service,
                                "",
                                resp_to + "gateway",
                                "" + retrievalReferenceNumber,
                                "" + accountNumber,
                                "" + "finnet-svc",
                                paymentSuccess,
                                header,
                                param,
                                "",
                                "",
                                "",
                                "",
                                "" + HttpStatus.OK
                        );
                        //logstash message custom

                        return new ResponseEntity(paymentSuccess, HttpStatus.OK);


                    } else {

                        paymentFailed.setResponseCode(resp.getString(39));
                        paymentFailed.setResponseMessage(FinnetRCTextParser.parse(resp.getString(39), ""));
                        paymentRequest.setPan(pan);
                        paymentRequest.setStan(stan);
                        paymentRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
                        paymentRequest.setAmount(amount);
                        paymentRequest.setTransactionDateTime(transactionDateTime);
                        paymentRequest.setExpiredDate(expiredDate);
                        paymentRequest.setSettlementDate(settlementDate);
                        paymentRequest.setChannelCode(channelCode);
                        paymentRequest.setAcquiringCode(acquiringCode);
                        paymentRequest.setForwardingCode(forwardingCode);
                        paymentRequest.setTerminalId(terminalId);
                        paymentRequest.setClientId(clientId);
                        paymentRequest.setLocationName(locationName);
                        paymentRequest.setPrivateData(privateData);
                        paymentRequest.setTransactionYear(transactionYear);
                        paymentRequest.setProductCode(productCode);
                        paymentRequest.setAccountNumber(accountNumber);
                        paymentRequest.setFee(fee);
                        paymentRequest.setDestinationAccount(destinationAccount);
                        paymentRequest.setFeeAccount(feeAccount);
                        paymentRequest.setSourceAccount(sourceAccount);
                        paymentRequest.setProductName(productName);
                        paymentFailed.setData(paymentRequest);

                        //logstash message custom
                        logging.restAPI(
                                "" + service,
                                "",
                                resp_to + "gateway",
                                "" + retrievalReferenceNumber,
                                "" + accountNumber,
                                "" + "finnet-svc",
                                paymentFailed,
                                header,
                                param,
                                "",
                                "",
                                "",
                                "",
                                "" + HttpStatus.OK
                        );
                        //logstash message custom

                        //Untuk Kebutuhan Reversal
                        m.set(39, resp.getString(39));

                        String respfinnet = resp.getString(39);
//                        Log.getLog(this.logger, this.logger).info("RESP PAYMENT PREPAID TSEL  ", respfinnet);
                        Gson gsonresp = new Gson();
                        Map<String, String> map = new HashMap<>();
                        map.put("responseCode", respfinnet);
                        values.set("multibiller:reversalhandling:" + m.getString(11) + accountNumber, gsonresp.toJson(map), 300, TimeUnit.SECONDS);

                        //END

                        return new ResponseEntity(paymentFailed, HttpStatus.OK);

                    }

                } else {

                    paymentFailed.setResponseCode("68");
                    paymentFailed.setResponseMessage(FinnetRCTextParser.parse("68", ""));
                    paymentRequest.setPan(pan);
                    paymentRequest.setStan(stan);
                    paymentRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
                    paymentRequest.setAmount(amount);
                    paymentRequest.setTransactionDateTime(transactionDateTime);
                    paymentRequest.setExpiredDate(expiredDate);
                    paymentRequest.setSettlementDate(settlementDate);
                    paymentRequest.setChannelCode(channelCode);
                    paymentRequest.setAcquiringCode(acquiringCode);
                    paymentRequest.setForwardingCode(forwardingCode);
                    paymentRequest.setTerminalId(terminalId);
                    paymentRequest.setClientId(clientId);
                    paymentRequest.setLocationName(locationName);
                    paymentRequest.setPrivateData(privateData);
                    paymentRequest.setTransactionYear(transactionYear);
                    paymentRequest.setProductCode(productCode);
                    paymentRequest.setAccountNumber(accountNumber);
                    paymentRequest.setFee(fee);
                    paymentRequest.setDestinationAccount(destinationAccount);
                    paymentRequest.setFeeAccount(feeAccount);
                    paymentRequest.setSourceAccount(sourceAccount);
                    paymentRequest.setProductName(productName);
                    paymentFailed.setData(paymentRequest);

                    //logstash message custom
                    logging.restAPI(
                            "" + service,
                            "",
                            resp_to + "gateway",
                            "" + retrievalReferenceNumber,
                            "" + accountNumber,
                            "" + "finnet-svc",
                            paymentFailed,
                            header,
                            param,
                            "",
                            "",
                            "",
                            "",
                            "" + HttpStatus.OK
                    );
                    //logstash message custom


                    return new ResponseEntity(paymentFailed, HttpStatus.OK);

                }

            } catch (ISOException e) {
//                e.printStackTrace();

                paymentFailed.setResponseCode("05");
                paymentFailed.setResponseMessage(FinnetRCTextParser.parse("05", ""));
                paymentRequest.setPan(pan);
                paymentRequest.setStan(stan);
                paymentRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
                paymentRequest.setAmount(amount);
                paymentRequest.setTransactionDateTime(transactionDateTime);
                paymentRequest.setExpiredDate(expiredDate);
                paymentRequest.setSettlementDate(settlementDate);
                paymentRequest.setChannelCode(channelCode);
                paymentRequest.setAcquiringCode(acquiringCode);
                paymentRequest.setForwardingCode(forwardingCode);
                paymentRequest.setTerminalId(terminalId);
                paymentRequest.setClientId(clientId);
                paymentRequest.setLocationName(locationName);
                paymentRequest.setPrivateData(privateData);
                paymentRequest.setTransactionYear(transactionYear);
                paymentRequest.setProductCode(productCode);
                paymentRequest.setAccountNumber(accountNumber);
                paymentRequest.setFee(fee);
                paymentRequest.setDestinationAccount(destinationAccount);
                paymentRequest.setFeeAccount(feeAccount);
                paymentRequest.setSourceAccount(sourceAccount);
                paymentRequest.setProductName(productName);
                paymentFailed.setData(paymentRequest);

                //logstash message custom
                logging.restAPI(
                        "" + service,
                        "",
                        resp_to + "gateway",
                        "" + retrievalReferenceNumber,
                        "" + accountNumber,
                        "" + "finnet-svc",
                        paymentFailed,
                        header,
                        param,
                        "",
                        "",
                        "",
                        "",
                        "" + HttpStatus.OK
                );
                //logstash message custom

                return new ResponseEntity(paymentFailed, HttpStatus.OK);

            } catch (NameRegistrar.NotFoundException e) {
//            e.printStackTrace();

                paymentFailed.setResponseCode("05");
                paymentFailed.setResponseMessage(FinnetRCTextParser.parse("05", ""));
                paymentRequest.setPan(pan);
                paymentRequest.setStan(stan);
                paymentRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
                paymentRequest.setAmount(amount);
                paymentRequest.setTransactionDateTime(transactionDateTime);
                paymentRequest.setExpiredDate(expiredDate);
                paymentRequest.setSettlementDate(settlementDate);
                paymentRequest.setChannelCode(channelCode);
                paymentRequest.setAcquiringCode(acquiringCode);
                paymentRequest.setForwardingCode(forwardingCode);
                paymentRequest.setTerminalId(terminalId);
                paymentRequest.setClientId(clientId);
                paymentRequest.setLocationName(locationName);
                paymentRequest.setPrivateData(privateData);
                paymentRequest.setTransactionYear(transactionYear);
                paymentRequest.setProductCode(productCode);
                paymentRequest.setAccountNumber(accountNumber);
                paymentRequest.setFee(fee);
                paymentRequest.setDestinationAccount(destinationAccount);
                paymentRequest.setFeeAccount(feeAccount);
                paymentRequest.setSourceAccount(sourceAccount);
                paymentRequest.setProductName(productName);
                paymentFailed.setData(paymentRequest);

                //logstash message custom
                logging.restAPI(
                        "" + service,
                        "",
                        resp_to + "gateway",
                        "" + retrievalReferenceNumber,
                        "" + accountNumber,
                        "" + "finnet-svc",
                        paymentFailed,
                        header,
                        param,
                        "",
                        "",
                        "",
                        "",
                        "" + HttpStatus.OK
                );
                //logstash message custom

                return new ResponseEntity(paymentFailed, HttpStatus.OK);

            }

        } else {

            paymentFailed.setResponseCode("12");
            paymentFailed.setResponseMessage(FinnetRCTextParser.parse("12", ""));
            paymentRequest.setPan(pan);
            paymentRequest.setStan(stan);
            paymentRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
            paymentRequest.setAmount(amount);
            paymentRequest.setTransactionDateTime(transactionDateTime);
            paymentRequest.setExpiredDate(expiredDate);
            paymentRequest.setSettlementDate(settlementDate);
            paymentRequest.setChannelCode(channelCode);
            paymentRequest.setAcquiringCode(acquiringCode);
            paymentRequest.setForwardingCode(forwardingCode);
            paymentRequest.setTerminalId(terminalId);
            paymentRequest.setClientId(clientId);
            paymentRequest.setLocationName(locationName);
            paymentRequest.setPrivateData(privateData);
            paymentRequest.setTransactionYear(transactionYear);
            paymentRequest.setProductCode(productCode);
            paymentRequest.setAccountNumber(accountNumber);
            paymentRequest.setFee(fee);
            paymentRequest.setDestinationAccount(destinationAccount);
            paymentRequest.setFeeAccount(feeAccount);
            paymentRequest.setSourceAccount(sourceAccount);
            paymentRequest.setProductName(productName);
            paymentFailed.setData(paymentRequest);

            //logstash message custom
            logging.restAPI(
                    "" + service,
                    "",
                    resp_to + "gateway",
                    "" + retrievalReferenceNumber,
                    "" + accountNumber,
                    "" + "finnet-svc",
                    paymentFailed,
                    header,
                    param,
                    "",
                    "",
                    "",
                    "",
                    "" + HttpStatus.OK
            );
            //logstash message custom

            return new ResponseEntity(paymentFailed, HttpStatus.OK);

        }
    }
}
