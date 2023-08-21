package co.id.middleware.finnet.controller.postpaid.hallo;

import co.id.middleware.finnet.domain.inquiry.*;
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
import java.util.concurrent.TimeUnit;

/**
 * @author errykistiyanto@gmail.com 2022-09-12
 */

@RestController
@Slf4j
@Component("InquiryPostpaidTelkomsel")
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


    @RequestMapping(value = "/v1.0/inquiry/telkomsel-hallo", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> multibiller(@Valid @RequestBody InquiryRequest inquiryRequest,
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
        String fee = env.getProperty("finnet.telkom.fee");
        String destinationAccount = env.getProperty("finnet.ss.destinationAccount");
        String feeAccount = env.getProperty("finnet.ss.feeAccount");
        String validationProductCode = env.getProperty("finnet.telkom.productCode");

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

            ISOMsg m = new ISOMsg();
            m.setMTI("0200");
            m.set(2, ISOUtil.zeropad(pan, 18));
            m.set(3, "381099");
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
            m.set(61, ISOUtil.zeropad(accountNumber, 13));
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
            ISOMsg resp = qmux.request(m, 30000);

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
                switch (resp.getString(39)) {
                    case "00":
                        if (resp.getString(4).equals("000000000000")) {

                            inquiryFailed.setResponseCode("88"); // set already paid if case rc 00 & bit4 000000000000
                            inquiryFailed.setResponseMessage(FinnetRCTextParser.parse("88", ""));
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

                        } else {
                            screen.append("Telkomsel Halo");
                            screen.append("|");
                            screen.append("|");
                            screen.append("No Handphone        : ");
                            screen.append("0" + Long.valueOf(accountNumber));
                            screen.append("|");
                            screen.append("Nama Pelanggan      : ");
                            screen.append(resp.getString(61).substring(43, 88));
                            screen.append("|");
                            screen.append("Referensi Tagihan   : ");
                            screen.append(resp.getString(61).substring(20, 31)); //BILL REFERENCE
                            screen.append("|");
                            screen.append("Nilai Tagihan       : ");
                            screen.append("IDR " + df.format(Long.valueOf(resp.getString(4))).replace(",", ".") + ",00"); // AMOUNT
                            screen.append("|");
                            screen.append("|");

                            Gson gson4 = new Gson();
                            Map<String, String> map4 = new HashMap<>();

                            map4.put("privateData", resp.getString(61).substring(0, resp.getString(61).length() - 45)); //for request payment finnet
                            map4.put("reserveData", resp.getString(61)); // original response finnet

                            //save redis
                            ValueOperations<String, String> values = redisTemplate.opsForValue();
                            values.set("co:id:bankdki:openapi:multibiller:" + clientId + uuid, gson4.toJson(map4), 30, TimeUnit.MINUTES); //30 minutes for test only, prod 1 minutes

                            log.info("redis set " + "co:id:bankdki:openapi:multibiller:" + clientId + uuid);

                            inquirySuccess.setResponseCode(resp.getString(39));
                            inquirySuccess.setResponseMessage(FinnetRCTextParser.parse(resp.getString(39), ""));
                            inquiryResponse.setPan(pan);
                            inquiryResponse.setStan(stan);
                            inquiryResponse.setRetrievalReferenceNumber(retrievalReferenceNumber);
                            inquiryResponse.setAmount(resp.getString(4).replaceFirst("^0+(?!$)", ""));
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
                        }

                    default:

                        inquiryFailed.setResponseCode(resp.getString(39));
                        inquiryFailed.setResponseMessage(FinnetRCTextParser.parse(resp.getString(39), ""));
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

            } else {

                inquiryFailed.setResponseCode("68");
                inquiryFailed.setResponseMessage(FinnetRCTextParser.parse("68", ""));
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

        } catch (ISOException e) {

            System.out.println("ISOException = "+e.getMessage() );

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

        } catch (NameRegistrar.NotFoundException e) {

            System.out.println("NameRegistrar = "+e.getMessage() );

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