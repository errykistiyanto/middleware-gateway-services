package co.id.middleware.finnet.controller.prepaid.indosat;

import co.id.middleware.finnet.domain.inquiry.InquiryFailed;
import co.id.middleware.finnet.domain.inquiry.InquiryRequest;
import co.id.middleware.finnet.domain.inquiry.InquiryResponse;
import co.id.middleware.finnet.domain.inquiry.InquirySuccess;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author briantomo80@gmail.com 28/08/23
 */

@RestController
@Slf4j
@Component("InquiryPrepaidIndosat")
public class Inquiry {

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
    public static final String service = "inquiry";
    public static final String req_from = "request from ";
    public static final String req_to = "request to ";
    public static final String resp_from = "response from ";
    public static final String resp_to = "response to ";
    public static final String in_req = "incoming request";
    public static final String out_resp = "outgoing response";
    //logstash message direction


    @RequestMapping(value = "/v1.0/inquiry/indosat-prepaid", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> Multibiller(@Valid @RequestBody InquiryRequest inquiryRequest,
                                              HttpServletRequest httpServletRequest,
                                              @RequestParam Map<String, Object> requestParam,
                                              @RequestHeader Map<String, Object> requestHeader) throws JsonProcessingException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        InquirySuccess inquirySuccess = new InquirySuccess();
        InquiryFailed inquiryFailed = new InquiryFailed();
        InquiryResponse inquiryResponse = new InquiryResponse();

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
        String fee = env.getProperty("finnet.fee.indosat-prepaid");
        String destinationAccount = env.getProperty("finnet.ss.destinationAccount");
        String feeAccount = env.getProperty("finnet.ss.feeAccount");
        String validationProductCode = env.getProperty("finnet.productCode.indosat-prepaid");

        DecimalFormat df = new DecimalFormat("#,###");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setCurrencySymbol("");
        df.setDecimalFormatSymbols(dfs);

        String pan = inquiryRequest.getPan();
        String stan = inquiryRequest.getStan();
        String retrievalReferenceNumber = inquiryRequest.getRetrievalReferenceNumber();
        String amount = inquiryRequest.getAmount();
        String transactionDateTime = inquiryRequest.getTransactionDateTime();
        String expiredDate = inquiryRequest.getExpiredDate();
        String settlementDate = inquiryRequest.getSettlementDate();
        String acquiringCode = inquiryRequest.getAcquiringCode();
        String forwardingCode = inquiryRequest.getForwardingCode();
        String channelCode = inquiryRequest.getChannelCode();
        String terminalId = inquiryRequest.getTerminalId();
        String clientId = inquiryRequest.getClientId();
        String locationName = inquiryRequest.getLocationName();
        String transactionYear = inquiryRequest.getTransactionYear();
        String productCode = inquiryRequest.getProductCode();
        String accountNumber = inquiryRequest.getAccountNumber();
        String productName = inquiryRequest.getProductName();

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
                inquiryRequest,
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

        try {

            System.out.println("productCode = " + productCode);
            System.out.println("productName = " + productName.replace(" ","-"));

            //checking productCode
            if (!productCode.equals(validationProductCode)) {
                inquiryFailed.setResponseCode("12");
                inquiryFailed.setResponseMessage("invalid productCode");
                inquiryFailed.setData(inquiryRequest);

                //logstash message custom
                logging.restAPI(
                        "" + service,
                        "",
                        resp_to + "gateway",
                        "" + retrievalReferenceNumber,
                        "" + accountNumber,
                        "" + "finnet-svc",
                        inquiryFailed,
                        header,
                        param,
                        "",
                        "",
                        "",
                        "",
                        "" + HttpStatus.OK
                );
                //logstash message custom

                return new ResponseEntity(inquiryFailed, HttpStatus.OK);

            }

            StringBuffer screen = new StringBuffer();
            switch (Integer.parseInt(amount)) {

                case 15000:

                case 20000:

                case 25000:

                case 30000:

                case 40000:

                case 50000:

                case 75000:

                case 100000:

                case 150000:

                case 200000:

                case 300000:

                case 500000:

                case 1000000:
                    screen.append("Indosat Prepaid");
                    screen.append("|");
                    screen.append("|");
                    screen.append("Nomor Handphone      : "+"0"+Long.valueOf(accountNumber).toString());
                    screen.append("|");
                    screen.append("Nominal              : IDR "+df.format(Long.valueOf(amount)).replace(",", ".") + ",00");
                    screen.append("|");

                    if (! channelCode.equals("6015")){ //!channelCode Open API
                        screen.append("Biaya Administrasi   : IDR "+df.format(Long.valueOf(fee)).replace(",", ".") + ",00");
                        screen.append("|");
                        screen.append("Total Bayar          : IDR "+df.format(Long.valueOf(amount) + Long.valueOf(fee)).replace(",", ".") + ",00");
                        screen.append("|");
                    }
                    screen.append("|");
                    break;

                case 0:
                    break;
            }

            Gson gson4 = new Gson();
            Map<String, String> map4 = new HashMap<>();

            map4.put("privateData", accountNumber); //for request payment finnet
            map4.put("reserveData", accountNumber); // original response finnet

            //save redis
            ValueOperations<String, String> values = redisTemplate.opsForValue();
            values.set("co:id:bankdki:openapi:multibiller:" + clientId + uuid, gson4.toJson(map4), 30, TimeUnit.MINUTES); //30 minutes for test only, prod 1 minutes

            log.info("redis set " + "co:id:bankdki:openapi:multibiller:" + clientId + uuid);

            inquirySuccess.setResponseCode("00");
            inquirySuccess.setResponseMessage(FinnetRCTextParser.parse("00", ""));
            inquiryResponse.setPan(pan);
            inquiryResponse.setStan(stan);
            inquiryResponse.setRetrievalReferenceNumber(retrievalReferenceNumber);
            inquiryResponse.setAmount(amount.replaceFirst("^0+(?!$)", ""));
            inquiryResponse.setTransactionDateTime(transactionDateTime);
            inquiryResponse.setExpiredDate(expiredDate);
            inquiryResponse.setSettlementDate(settlementDate);
            inquiryResponse.setChannelCode(channelCode);
            inquiryResponse.setAcquiringCode(acquiringCode);
            inquiryResponse.setForwardingCode(forwardingCode);
            inquiryResponse.setTerminalId(terminalId);
            inquiryResponse.setClientId(clientId);
            inquiryResponse.setLocationName(locationName);
            inquiryResponse.setPrivateData(uuid);
            inquiryResponse.setScreen(screen.toString());
            inquiryResponse.setTransactionYear(transactionYear);
            inquiryResponse.setProductCode(productCode);
            inquiryResponse.setAccountNumber(accountNumber);
            inquiryResponse.setFee(fee);
            inquiryResponse.setDestinationAccount(destinationAccount);
            inquiryResponse.setFeeAccount(feeAccount);
            inquiryResponse.setProductName(productName);
            inquirySuccess.setData(inquiryResponse);

            //logstash message custom
            logging.restAPI(
                    "" + service,
                    "",
                    resp_to + "gateway",
                    "" + retrievalReferenceNumber,
                    "" + accountNumber,
                    "" + "finnet-svc",
                    inquirySuccess,
                    header,
                    param,
                    "",
                    "",
                    "",
                    "",
                    "" + HttpStatus.OK
            );
            //logstash message custom

            return new ResponseEntity(inquirySuccess, HttpStatus.OK);


        } catch (NumberFormatException e) {
            e.printStackTrace();

            inquiryFailed.setResponseCode("05");
            inquiryFailed.setResponseMessage(FinnetRCTextParser.parse("05", ""));
            inquiryRequest.setPan(pan);
            inquiryRequest.setStan(stan);
            inquiryRequest.setRetrievalReferenceNumber(retrievalReferenceNumber);
            inquiryRequest.setAmount(amount);
            inquiryRequest.setTransactionDateTime(transactionDateTime);
            inquiryRequest.setExpiredDate(expiredDate);
            inquiryRequest.setSettlementDate(settlementDate);
            inquiryRequest.setChannelCode(channelCode);
            inquiryRequest.setAcquiringCode(acquiringCode);
            inquiryRequest.setForwardingCode(forwardingCode);
            inquiryRequest.setTerminalId(terminalId);
            inquiryRequest.setClientId(clientId);
            inquiryRequest.setLocationName(locationName);
            inquiryRequest.setTransactionYear(transactionYear);
            inquiryRequest.setProductCode(productCode);
            inquiryRequest.setAccountNumber(accountNumber);
            inquiryRequest.setProductName(productName);
            inquiryFailed.setData(inquiryRequest);

            //logstash message custom
            logging.restAPI(
                    "" + service,
                    "",
                    resp_to + "gateway",
                    "" + retrievalReferenceNumber,
                    "" + accountNumber,
                    "" + "finnet-svc",
                    inquiryFailed,
                    header,
                    param,
                    "",
                    "",
                    "",
                    "",
                    "" + HttpStatus.OK
            );
            //logstash message custom

            return new ResponseEntity(inquiryFailed, HttpStatus.OK);
        }

    }
}
