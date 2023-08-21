package co.id.middleware.finnet.controller.postpaid.hallo;

import co.id.middleware.finnet.domain.reversal.*;
import co.id.middleware.finnet.repository.HistoryService;
import co.id.middleware.finnet.utils.FinnetRCTextParser;
import co.id.middleware.finnet.utils.Logging;
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
import java.util.*;

/**
 * @author errykistiyanto@gmail.com 2022-09-12
 */

@RestController
@Slf4j
@Component("ReversalPostpaidTelkomsel")
public class Reversal {

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

    //logstash message direction
    public static final String service = "payment";
    public static final String req_from = "request from ";
    public static final String req_to = "request to ";
    public static final String resp_from = "response from ";
    public static final String resp_to = "response to ";
    public static final String in_req = "incoming request";
    public static final String out_resp = "outgoing response";
    //logstash message direction

    @RequestMapping(value = "/v1.0/reversal/telkomsel-hallo", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> Multibiller(@Valid @RequestBody ReversalRequest reversalRequest,
                                              HttpServletRequest httpServletRequest,
                                              @RequestParam Map<String, Object> requestParam,
                                              @RequestHeader Map<String, Object> requestHeader) throws JsonProcessingException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        ReversalSuccess reversalSuccess = new ReversalSuccess();
        ReversalFailed reversalFailed = new ReversalFailed();
        ReversalResponse reversalResponse = new ReversalResponse();

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
//        String fee = env.getProperty("finnet.telkom.fee");
//        String destinationAccount = env.getProperty("finnet.ss.destinationAccount");
//        String feeAccount = env.getProperty("finnet.ss.feeAccount");
        String validationProductCode = env.getProperty("finnet.telkom.productCode");

        DecimalFormat df = new DecimalFormat("#,###");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setCurrencySymbol("");
        df.setDecimalFormatSymbols(dfs);

        String pan = reversalRequest.getPan();
        String stan = reversalRequest.getStan();
        String retrievalReferenceNumber = reversalRequest.getRetrievalReferenceNumber();
        String amount = reversalRequest.getAmount();
        String transactionDateTime = reversalRequest.getTransactionDateTime();
        String expiredDate = reversalRequest.getExpiredDate();
        String settlementDate = reversalRequest.getSettlementDate();
        String acquiringCode = reversalRequest.getAcquiringCode();
        String forwardingCode = reversalRequest.getForwardingCode();
        String channelCode = reversalRequest.getChannelCode();
        String terminalId = reversalRequest.getTerminalId();
        String clientId = reversalRequest.getClientId();
        String locationName = reversalRequest.getLocationName();
        String transactionYear = reversalRequest.getTransactionYear();
        String productCode = reversalRequest.getProductCode();
        String accountNumber = reversalRequest.getAccountNumber();
        String fee = reversalRequest.getFee();
        String destinationAccount = reversalRequest.getDestinationAccount();
        String feeAccount = reversalRequest.getFeeAccount();
        String sourceAccount = reversalRequest.getSourceAccount();
        String privateData = reversalRequest.getPrivateData();
        String productName = reversalRequest.getProductName();

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
                reversalRequest,
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
                m.setMTI("0400");
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
                m.set(61, redis_privateData);
                m.set(103, "010001");

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

                    if (resp.getString(39).equals("00")) {

                        reversalSuccess.setResponseCode(resp.getString(39));
                        reversalSuccess.setResponseMessage(FinnetRCTextParser.parse(resp.getString(39), ""));
                        reversalResponse.setPan(pan);
                        reversalResponse.setStan(stan);
                        reversalResponse.setRetrievalReferenceNumber(retrievalReferenceNumber);
                        reversalResponse.setAmount(amount);
                        reversalResponse.setTransactionDateTime(transactionDateTime);
                        reversalResponse.setExpiredDate(expiredDate);
                        reversalResponse.setSettlementDate(settlementDate);
                        reversalResponse.setChannelCode(channelCode);
                        reversalResponse.setAcquiringCode(acquiringCode);
                        reversalResponse.setForwardingCode(forwardingCode);
                        reversalResponse.setTerminalId(terminalId);
                        reversalResponse.setClientId(clientId);
                        reversalResponse.setLocationName(locationName);
                        reversalResponse.setPrivateData(privateData);
                        reversalResponse.setScreen(FinnetRCTextParser.parse("22", "")); //default screen reversal
                        reversalResponse.setTransactionYear(transactionYear);
                        reversalResponse.setProductCode(productCode);
                        reversalResponse.setAccountNumber(accountNumber);
                        reversalResponse.setFee(fee);
                        reversalResponse.setDestinationAccount(destinationAccount);
                        reversalResponse.setFeeAccount(feeAccount);
                        reversalResponse.setSourceAccount(sourceAccount);
                        reversalResponse.setProductName(productName);
                        reversalSuccess.setData(reversalResponse);

                        //logstash message custom
                        logging.restAPI(
                                "" + service,
                                "",
                                resp_to + "gateway",
                                "" + retrievalReferenceNumber,
                                "" + accountNumber,
                                "" + "finnet-svc",
                                reversalSuccess,
                                header,
                                param,
                                "",
                                "",
                                "",
                                "",
                                "" + HttpStatus.OK
                        );
                        //logstash message custom

                        return new ResponseEntity(reversalSuccess, HttpStatus.OK);


                    } else {

                        reversalFailed.setResponseCode(resp.getString(39));
                        reversalFailed.setResponseMessage(FinnetRCTextParser.parse(resp.getString(39), ""));
                        reversalRequest.setPan(pan);
                        reversalRequest.setStan(stan);
                        reversalRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
                        reversalRequest.setAmount(amount);
                        reversalRequest.setTransactionDateTime(transactionDateTime);
                        reversalRequest.setExpiredDate(expiredDate);
                        reversalRequest.setSettlementDate(settlementDate);
                        reversalRequest.setChannelCode(channelCode);
                        reversalRequest.setAcquiringCode(acquiringCode);
                        reversalRequest.setForwardingCode(forwardingCode);
                        reversalRequest.setTerminalId(terminalId);
                        reversalRequest.setClientId(clientId);
                        reversalRequest.setLocationName(locationName);
                        reversalRequest.setPrivateData(privateData);
                        reversalRequest.setTransactionYear(transactionYear);
                        reversalRequest.setProductCode(productCode);
                        reversalRequest.setAccountNumber(accountNumber);
                        reversalRequest.setFee(fee);
                        reversalRequest.setDestinationAccount(destinationAccount);
                        reversalRequest.setFeeAccount(feeAccount);
                        reversalRequest.setSourceAccount(sourceAccount);
                        reversalRequest.setProductName(productName);
                        reversalFailed.setData(reversalRequest);

                        //logstash message custom
                        logging.restAPI(
                                "" + service,
                                "",
                                resp_to + "gateway",
                                "" + retrievalReferenceNumber,
                                "" + accountNumber,
                                "" + "finnet-svc",
                                reversalFailed,
                                header,
                                param,
                                "",
                                "",
                                "",
                                "",
                                "" + HttpStatus.OK
                        );
                        //logstash message custom

                        return new ResponseEntity(reversalFailed, HttpStatus.OK);

                    }

                } else {

                    reversalFailed.setResponseCode("68");
                    reversalFailed.setResponseMessage(FinnetRCTextParser.parse("68", ""));
                    reversalRequest.setPan(pan);
                    reversalRequest.setStan(stan);
                    reversalRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
                    reversalRequest.setAmount(amount);
                    reversalRequest.setTransactionDateTime(transactionDateTime);
                    reversalRequest.setExpiredDate(expiredDate);
                    reversalRequest.setSettlementDate(settlementDate);
                    reversalRequest.setChannelCode(channelCode);
                    reversalRequest.setAcquiringCode(acquiringCode);
                    reversalRequest.setForwardingCode(forwardingCode);
                    reversalRequest.setTerminalId(terminalId);
                    reversalRequest.setClientId(clientId);
                    reversalRequest.setLocationName(locationName);
                    reversalRequest.setPrivateData(privateData);
                    reversalRequest.setTransactionYear(transactionYear);
                    reversalRequest.setProductCode(productCode);
                    reversalRequest.setAccountNumber(accountNumber);
                    reversalRequest.setFee(fee);
                    reversalRequest.setDestinationAccount(destinationAccount);
                    reversalRequest.setFeeAccount(feeAccount);
                    reversalRequest.setSourceAccount(sourceAccount);
                    reversalRequest.setProductName(productName);
                    reversalFailed.setData(reversalRequest);

                    //logstash message custom
                    logging.restAPI(
                            "" + service,
                            "",
                            resp_to + "gateway",
                            "" + retrievalReferenceNumber,
                            "" + accountNumber,
                            "" + "finnet-svc",
                            reversalFailed,
                            header,
                            param,
                            "",
                            "",
                            "",
                            "",
                            "" + HttpStatus.OK
                    );
                    //logstash message custom


                    return new ResponseEntity(reversalFailed, HttpStatus.OK);

                }

            } catch (ISOException e) {
//                e.printStackTrace();

                reversalFailed.setResponseCode("05");
                reversalFailed.setResponseMessage(FinnetRCTextParser.parse("05", ""));
                reversalRequest.setPan(pan);
                reversalRequest.setStan(stan);
                reversalRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
                reversalRequest.setAmount(amount);
                reversalRequest.setTransactionDateTime(transactionDateTime);
                reversalRequest.setExpiredDate(expiredDate);
                reversalRequest.setSettlementDate(settlementDate);
                reversalRequest.setChannelCode(channelCode);
                reversalRequest.setAcquiringCode(acquiringCode);
                reversalRequest.setForwardingCode(forwardingCode);
                reversalRequest.setTerminalId(terminalId);
                reversalRequest.setClientId(clientId);
                reversalRequest.setLocationName(locationName);
                reversalRequest.setPrivateData(privateData);
                reversalRequest.setTransactionYear(transactionYear);
                reversalRequest.setProductCode(productCode);
                reversalRequest.setAccountNumber(accountNumber);
                reversalRequest.setFee(fee);
                reversalRequest.setDestinationAccount(destinationAccount);
                reversalRequest.setFeeAccount(feeAccount);
                reversalRequest.setSourceAccount(sourceAccount);
                reversalRequest.setProductName(productName);
                reversalFailed.setData(reversalRequest);

                //logstash message custom
                logging.restAPI(
                        "" + service,
                        "",
                        resp_to + "gateway",
                        "" + retrievalReferenceNumber,
                        "" + accountNumber,
                        "" + "finnet-svc",
                        reversalFailed,
                        header,
                        param,
                        "",
                        "",
                        "",
                        "",
                        "" + HttpStatus.OK
                );
                //logstash message custom


                return new ResponseEntity(reversalFailed, HttpStatus.OK);

            } catch (NameRegistrar.NotFoundException e) {
//            e.printStackTrace();

                reversalFailed.setResponseCode("05");
                reversalFailed.setResponseMessage(FinnetRCTextParser.parse("05", ""));
                reversalRequest.setPan(pan);
                reversalRequest.setStan(stan);
                reversalRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
                reversalRequest.setAmount(amount);
                reversalRequest.setTransactionDateTime(transactionDateTime);
                reversalRequest.setExpiredDate(expiredDate);
                reversalRequest.setSettlementDate(settlementDate);
                reversalRequest.setChannelCode(channelCode);
                reversalRequest.setAcquiringCode(acquiringCode);
                reversalRequest.setForwardingCode(forwardingCode);
                reversalRequest.setTerminalId(terminalId);
                reversalRequest.setClientId(clientId);
                reversalRequest.setLocationName(locationName);
                reversalRequest.setPrivateData(privateData);
                reversalRequest.setTransactionYear(transactionYear);
                reversalRequest.setProductCode(productCode);
                reversalRequest.setAccountNumber(accountNumber);
                reversalRequest.setFee(fee);
                reversalRequest.setDestinationAccount(destinationAccount);
                reversalRequest.setFeeAccount(feeAccount);
                reversalRequest.setSourceAccount(sourceAccount);
                reversalRequest.setProductName(productName);
                reversalFailed.setData(reversalRequest);

                //logstash message custom
                logging.restAPI(
                        "" + service,
                        "",
                        resp_to + "gateway",
                        "" + retrievalReferenceNumber,
                        "" + accountNumber,
                        "" + "finnet-svc",
                        reversalFailed,
                        header,
                        param,
                        "",
                        "",
                        "",
                        "",
                        "" + HttpStatus.OK
                );
                //logstash message custom

                return new ResponseEntity(reversalFailed, HttpStatus.OK);

            }

        } else {

            reversalFailed.setResponseCode("12");
            reversalFailed.setResponseMessage(FinnetRCTextParser.parse("12", ""));
            reversalRequest.setPan(pan);
            reversalRequest.setStan(stan);
            reversalRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
            reversalRequest.setAmount(amount);
            reversalRequest.setTransactionDateTime(transactionDateTime);
            reversalRequest.setExpiredDate(expiredDate);
            reversalRequest.setSettlementDate(settlementDate);
            reversalRequest.setChannelCode(channelCode);
            reversalRequest.setAcquiringCode(acquiringCode);
            reversalRequest.setForwardingCode(forwardingCode);
            reversalRequest.setTerminalId(terminalId);
            reversalRequest.setClientId(clientId);
            reversalRequest.setLocationName(locationName);
            reversalRequest.setPrivateData(privateData);
            reversalRequest.setTransactionYear(transactionYear);
            reversalRequest.setProductCode(productCode);
            reversalRequest.setAccountNumber(accountNumber);
            reversalRequest.setFee(fee);
            reversalRequest.setDestinationAccount(destinationAccount);
            reversalRequest.setFeeAccount(feeAccount);
            reversalRequest.setSourceAccount(sourceAccount);
            reversalRequest.setProductName(productName);
            reversalFailed.setData(reversalRequest);

            //logstash message custom
            logging.restAPI(
                    "" + service,
                    "",
                    resp_to + "gateway",
                    "" + retrievalReferenceNumber,
                    "" + accountNumber,
                    "" + "finnet-svc",
                    reversalFailed,
                    header,
                    param,
                    "",
                    "",
                    "",
                    "",
                    "" + HttpStatus.OK
            );
            //logstash message custom

            return new ResponseEntity(reversalFailed, HttpStatus.OK);

        }

    }

}