package co.id.middleware.finnet.controller.telkom;

import co.id.middleware.finnet.domain.reversal.*;
import co.id.middleware.finnet.repository.HistoryService;
import co.id.middleware.finnet.util.Logging;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author errykistiyanto@gmail.com 2023-08-17
 */

@RestController
@Slf4j
@Component("reversalTelkom")
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

    //logstash message directionexception
    public static final String service = "reversal";
    public static final String req_from = "request from ";
    public static final String req_to = "request to ";
    public static final String resp_from = "response from ";
    public static final String resp_to = "response to ";
    public static final String in_req = "incoming request";
    public static final String out_resp = "outgoing response";
    //logstash message direction

    @RequestMapping(value = "/v1.0/reversal/telkom", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> Reversal(@Valid @RequestBody ReversalRequest reversalRequest,
                                                HttpServletRequest httpServletRequest,
                                                @RequestParam Map<String, Object> requestParam,
                                                @RequestHeader Map<String, Object> requestHeader) throws JsonProcessingException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        ReversalSuccess reversalSuccess = new ReversalSuccess();
//        ReversalResponse reversalResponse = new ReversalResponse();
        ReversalFailed reversalFailed = new ReversalFailed();

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
//        String destinationAccount = env.getProperty("finnet.ss.destinationAccount");
//        String feeAccount = env.getProperty("finnet.ss.feeAccount");

        SimpleDateFormat formatTxDate = new SimpleDateFormat("yyyyMMdd");
        String txDate = formatTxDate.format(new Date());

        SimpleDateFormat formatTxHour = new SimpleDateFormat("HHmmss");
        String txHour = formatTxHour.format(new Date());

        SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");
        String date = formatDate.format(new Date());

        DecimalFormat df = new DecimalFormat("#,###");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setCurrencySymbol("");
        df.setDecimalFormatSymbols(dfs);

        SimpleDateFormat sdfDate1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date now1 = new Date();
        String date1 = sdfDate1.format(now1);

        String stan = reversalRequest.getStan();
        String retrievalReferenceNumber = reversalRequest.getRetrievalReferenceNumber();
        String transactionDateTime = reversalRequest.getTransactionDateTime();
        String transactionYear = reversalRequest.getTransactionYear();
        String amount = reversalRequest.getAmount();
        String accountNumber = reversalRequest.getAccountNumber();
        String productCode = reversalRequest.getProductCode();
        String channelCode = reversalRequest.getChannelCode();
        String clientId = reversalRequest.getClientId();
        String productName = reversalRequest.getProductName();
        String privateData = reversalRequest.getPrivateData();

        String destinationAccount = reversalRequest.getDestinationAccount();
        String feeAccount = reversalRequest.getFeeAccount();
        String sourceAccount = reversalRequest.getSourceAccount();

        System.out.println("productName = " + productName.replace(" ","-"));

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


        ValueOperations<String, String> values = redisTemplate.opsForValue();
        String key = values.get("co:id:middleware:finnet:"+clientId+privateData);

        if (key != null) {
            log.info("redis get " + "co:id:middleware:finnet:"+clientId+privateData);

            Gson gson2 = new Gson();
            Map<String, String> map2 = gson2.fromJson(key, Map.class);
            String redis_privateData = map2.get("privateData");

        try {

            System.out.println("productCode = " + productCode);

            //checking productCode
            if(!productCode.equals("100001")){
                reversalFailed.setResponseCode("12");
                reversalFailed.setResponseMessage("invalid productCode");
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

            HttpHeaders headers1 = new HttpHeaders();
            //headers.set("Authorization", "Bearer " + token);
            headers1.setContentType(MediaType.APPLICATION_JSON);

            Object convertHeader = logging.convertHeader(headers1);

            //for generate signature
            String userName = "finnetframe";
            String password = "password";
            String finnetProductCode = "001001";
            String channel = channelCode;
            String terminal = "BPD ACEH";
            String finnetTransactionType = "38";
            String billNumber = accountNumber;
            String traxId = retrievalReferenceNumber; // retrievalReferenceNumber request

            Date now = new Date();
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS"); //timeStamp
            String timeStamp = sdfDate.format(now);
            System.out.println("timeStamp " + timeStamp);

            String originalString1 = timeStamp + password;
            String sha256hex = DigestUtils.sha256Hex(originalString1);
            System.out.println("sha256hex " + sha256hex);

            String originalString2 = userName + finnetProductCode + channel + terminal + finnetTransactionType + billNumber + traxId + sha256hex;
            String md5hex = DigestUtils.md5Hex(originalString2);
            System.out.println("md5hex " + md5hex);

            LinkedHashMap<String, Object> payload1 = new LinkedHashMap<>();
            payload1.put("userName", "finnetframe");
            payload1.put("signature", md5hex);
            payload1.put("productCode", "001001");
            payload1.put("channel", channelCode);
            payload1.put("terminal", "G009TRKK");
            payload1.put("terminalName", "Loket Bidakara");
            payload1.put("terminalLocation", "Bidakara Pancoran");
            payload1.put("transactionType", "51");
            payload1.put("billNumber", Strings.padStart(accountNumber, 13, '0'));
            payload1.put("amount", Strings.padStart(amount, 12, '0'));
            payload1.put("feeAmount", "0");
            payload1.put("bit61", redis_privateData);
            payload1.put("traxId", retrievalReferenceNumber);
            payload1.put("timeStamp", timeStamp);
            payload1.put("addInfo1", "");
            payload1.put("addInfo2", "");
            payload1.put("addInfo3", "");

            //logstash message custom
            logging.restAPI(
                    "" + service,
                    "",
                    req_to + "finnet",
                    "" + retrievalReferenceNumber,
                    "" + accountNumber,
                    "" + "finnet",
                    payload1,
                    convertHeader,
                    param,
                    "" + "POST",
                    "" + finnetUri,
                    "" + finnetAddress,
                    "",
                    ""
            );
            //logstash message custom

            HttpEntity<String> request = new HttpEntity<>(gson.toJson(payload1), headers1);
            ResponseEntity<String> response = restTemplate.postForEntity(URI, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            Object payload2 = mapper.readValue(response.getBody(), Object.class);

            //logstash message custom
            logging.restAPI(
                    "" + service,
                    "",
                    resp_from + "finnet",
                    "" + retrievalReferenceNumber,
                    "" + accountNumber,
                    "" + "finnet",
                    payload2,
                    header,
                    param,
                    "",
                    "",
                    "",
                    "",
                    "" + response.getStatusCode()
            );
            //logstash message custom

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode resultCode = root.path("resultCode");
            JsonNode resultDesc = root.path("resultDesc");

            if (resultCode.asText() != null) {

                if (resultCode.asText().equals("00")) {

                    reversalSuccess.setResponseCode(resultCode.asText());
                    reversalSuccess.setResponseMessage(resultDesc.asText());
                    reversalSuccess.setData(reversalRequest);

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

                    reversalFailed.setResponseCode(resultCode.asText());
                    reversalFailed.setResponseMessage(resultDesc.asText());
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
                reversalFailed.setResponseMessage("Timeout");
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

        } catch (JsonProcessingException e) {

            reversalFailed.setResponseCode("05");
            reversalFailed.setResponseMessage("General Error");
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

        } catch (RestClientException e) {

            reversalFailed.setResponseCode("05");
            reversalFailed.setResponseMessage("General Error");
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

        }else {

            reversalFailed.setResponseCode("12");
            reversalFailed.setResponseMessage("invalid privateData");
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