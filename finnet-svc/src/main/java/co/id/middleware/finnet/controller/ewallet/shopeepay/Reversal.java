package co.id.middleware.finnet.controller.ewallet.shopeepay;

import co.id.middleware.finnet.domain.reversal.ReversalFailed;
import co.id.middleware.finnet.domain.reversal.ReversalRequest;
import co.id.middleware.finnet.domain.reversal.ReversalResponse;
import co.id.middleware.finnet.domain.reversal.ReversalSuccess;
import co.id.middleware.finnet.repository.HistoryService;
import co.id.middleware.finnet.utils.FinnetRCTextParser;
import co.id.middleware.finnet.utils.Logging;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author briantomo80@gmail.com 05/09/23
 */

@RestController
@Slf4j
@Component("ReversalShopeepay")
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
    public static final String service = "reversal";
    public static final String req_from = "request from ";
    public static final String req_to = "request to ";
    public static final String resp_from = "response from ";
    public static final String resp_to = "response to ";
    public static final String in_req = "incoming request";
    public static final String out_resp = "outgoing response";
    //logstash message direction

    @RequestMapping(value = "/v1.0/reversal/shopeepay", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
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
//        String fee = env.getProperty("finnet.fee.shopeepay");
//        String destinationAccount = env.getProperty("finnet.ss.destinationAccount");
//        String feeAccount = env.getProperty("finnet.ss.feeAccount");
        String validationProductCode = env.getProperty("finnet.productCode.shopeepay");

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

            //NEW ADD
            String keyrevfinnet = values.get("multibiller:reversalhandling:" + stan + accountNumber);
//                Log.getLog(this.logger, this.logger).info("KEY ", keyrevfinnet);
            Gson gsonkirpg = new Gson();
            Map<String, String> map = gsonkirpg.fromJson(keyrevfinnet, Map.class);
            //END

            if (map != null) {

                String resppayment = map.get("responseCode");

                log.info("NILAI resppayment --> " + resppayment);

                reversalSuccess.setResponseCode(resppayment);
                reversalSuccess.setResponseMessage(FinnetRCTextParser.parse(resppayment, ""));
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

                reversalFailed.setResponseCode("22");
                reversalFailed.setResponseMessage("Maaf untuk sementara transaksi tidak dapat dilakukan");
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
