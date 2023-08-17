package co.id.middleware.finnet.controller.telkom;

import co.id.middleware.finnet.domain.payment.*;
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
import java.util.*;

/**
 * @author errykistiyanto@gmail.com 2023-08-17
 */

@RestController
@Slf4j
@Component("paymentTelkom")
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

    //logstash message directionexception
    public static final String service = "payment";
    public static final String req_from = "request from ";
    public static final String req_to = "request to ";
    public static final String resp_from = "response from ";
    public static final String resp_to = "response to ";
    public static final String in_req = "incoming request";
    public static final String out_resp = "outgoing response";
    //logstash message direction

    @RequestMapping(value = "/v1.0/payment/telkom", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> Payment(@Valid @RequestBody PaymentRequest paymentRequest,
                                                HttpServletRequest httpServletRequest,
                                                @RequestParam Map<String, Object> requestParam,
                                                @RequestHeader Map<String, Object> requestHeader) throws JsonProcessingException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        PaymentSuccess paymentSuccess = new PaymentSuccess();
        PaymentResponse paymentResponse = new PaymentResponse();
        PaymentFailed paymentFailed = new PaymentFailed();

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

        String stan = paymentRequest.getStan();
        String retrievalReferenceNumber = paymentRequest.getRetrievalReferenceNumber();
        String transactionDateTime = paymentRequest.getTransactionDateTime();
        String transactionYear = paymentRequest.getTransactionYear();
        String amount = paymentRequest.getAmount();
        String accountNumber = paymentRequest.getAccountNumber();
        String productCode = paymentRequest.getProductCode();
        String channelCode = paymentRequest.getChannelCode();
        String clientId = paymentRequest.getClientId();
        String productName = paymentRequest.getProductName();
        String privateData = paymentRequest.getPrivateData();

        String destinationAccount = paymentRequest.getDestinationAccount();
        String feeAccount = paymentRequest.getFeeAccount();
        String sourceAccount = paymentRequest.getSourceAccount();

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
                paymentFailed.setResponseCode("12");
                paymentFailed.setResponseMessage("invalid productCode");
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
            payload1.put("transactionType", "50");
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
            JsonNode sysCode = root.path("sysCode");
            JsonNode resp_ProductCode = root.path("productCode");
            JsonNode resp_terminal = root.path("terminal");
            JsonNode resp_transactionType = root.path("transactionType");
            JsonNode resp_amount = root.path("amount");
            JsonNode feeAmount = root.path("feeAmount");
            JsonNode bit48 = root.path("bit48");
            JsonNode resp_traxId = root.path("traxId");
            JsonNode resp_timeStamp = root.path("timeStamp");
            JsonNode timeStampServer = root.path("timeStampServer");
            JsonNode resp_bit61 = root.path("bit61");

            JsonNode customerData = root.path("customerData");
            JsonNode customerId = customerData.path("customerId");
            JsonNode customerName = customerData.path("customerName");
            JsonNode npwp = customerData.path("npwp");

            JsonNode kodeDivre = customerData.path("kodeDivre");
            JsonNode kodeDatel = customerData.path("kodeDatel");
            JsonNode jumlahBill = customerData.path("jumlahBill");

            if (resultCode.asText() != null) {
                StringBuffer screen = new StringBuffer();
                if (resultCode.asText().equals("00")) {

                    screen.append("Pembayaran Telkom/Speedy"); // header
                    screen.append("|");
                    screen.append("|");

                    if (! channelCode.equals("6015")){ //!channelCode Open API
                        screen.append("Ref   : ");
                        screen.append(retrievalReferenceNumber);
                        screen.append("|");
                        screen.append("Waktu : ");
                        screen.append((date1) + " WIB");
                        screen.append("|");
                    }

                    screen.append("ID Pelanggan        : ");
                    screen.append("0" + Long.valueOf(customerId.asText()));
                    screen.append("|");
                    screen.append("Nama Pelanggan      : ");
                    screen.append(customerName.asText());
                    screen.append("|");
                    screen.append("Nilai Tagihan       : ");
                    screen.append("IDR " + df.format(Long.valueOf(resp_amount.asText())).replace(",", ".") + ",00");
                    screen.append("|");

                    //case lebih dari 1 tagihan
                    switch (jumlahBill.asText()) {
                        case "2":

                            JsonNode case2_billInfo1 = customerData.path("billInfo1");
                            JsonNode case2_nomorReferensi1 = case2_billInfo1.path("nomorReferensi");
                            JsonNode case2_nilaiTagihan1 = case2_billInfo1.path("nilaiTagihan");

                            JsonNode case2_billInfo2 = customerData.path("billInfo2");
                            JsonNode case2_nomorReferensi2 = case2_billInfo2.path("nomorReferensi");
                            JsonNode case2_nilaiTagihan2 = case2_billInfo2.path("nilaiTagihan");

                            screen.append("Nilai Tagihan Ke - 1");
                            screen.append("IDR " + df.format(Long.valueOf(case2_nilaiTagihan1.asText())).replace(",", ".") + ",00"); //amount only 31,43
                            screen.append("|");

                            screen.append("Nilai Tagihan Ke - 2");
                            screen.append("IDR " + df.format(Long.valueOf(case2_nilaiTagihan2.asText())).replace(",", ".") + ",00"); //amount only 31,43
                            screen.append("|");

                            break;

                        case "3":

                            JsonNode case3_billInfo1 = customerData.path("billInfo1");
                            JsonNode case3_nomorReferensi1 = case3_billInfo1.path("nomorReferensi");
                            JsonNode case3_nilaiTagihan1 = case3_billInfo1.path("nilaiTagihan");

                            JsonNode case3_billInfo2 = customerData.path("billInfo2");
                            JsonNode case3_nomorReferensi2 = case3_billInfo2.path("nomorReferensi");
                            JsonNode case3_nilaiTagihan2 = case3_billInfo2.path("nilaiTagihan");

                            JsonNode case3_billInfo3 = customerData.path("billInfo3");
                            JsonNode case3_nomorReferensi3 = case3_billInfo3.path("nomorReferensi");
                            JsonNode case3_nilaiTagihan3 = case3_billInfo3.path("nilaiTagihan");

                            screen.append("Nilai Tagihan Ke - 1");
                            screen.append("IDR " + df.format(Long.valueOf(case3_nilaiTagihan1.asText())).replace(",", ".") + ",00"); //amount only 31,43
                            screen.append("|");

                            screen.append("Nilai Tagihan Ke - 2");
                            screen.append("IDR " + df.format(Long.valueOf(case3_nilaiTagihan2.asText())).replace(",", ".") + ",00"); //amount only 31,43
                            screen.append("|");

                            screen.append("Nilai Tagihan Ke - 3");
                            screen.append("IDR " + df.format(Long.valueOf(case3_nilaiTagihan3.asText())).replace(",", ".") + ",00"); //amount only 31,43
                            screen.append("|");

                            break;

                    }

                    if (!channelCode.equals("6015")){ //!channelCode Open API
                        screen.append("Biaya Administrasi  : ");
                        screen.append("IDR " + df.format(Long.valueOf(fee)).replace(",", ".") + ",00");
                        screen.append("|");
                        screen.append("Total Bayar         : ");
                        screen.append("IDR " + df.format(Long.valueOf(resp_amount.asText()) + Long.valueOf(fee)).replace(",", ".") + ",00");
                        screen.append("|");
                    }

                    screen.append("|");

                    paymentResponse.setStan(stan);
                    paymentResponse.setAmount(""+Long.valueOf(resp_amount.asText()));
                    paymentResponse.setTransactionDateTime(transactionDateTime);
                    paymentResponse.setTransactionYear(transactionYear);
                    paymentResponse.setRetrievalReferenceNumber(retrievalReferenceNumber);
                    paymentResponse.setScreen(screen.toString());
                    paymentResponse.setFee(fee);
                    paymentResponse.setProductCode(productCode);
                    paymentResponse.setAccountNumber(accountNumber);
                    paymentResponse.setChannelCode(channelCode);
                    paymentResponse.setClientId(clientId);
                    paymentResponse.setProductName(productName);
                    paymentResponse.setPrivateData(uuid);
                    paymentResponse.setDestinationAccount(destinationAccount);
                    paymentResponse.setFeeAccount(feeAccount);
                    paymentResponse.setSourceAccount(sourceAccount);

                    paymentSuccess.setResponseCode(resultCode.asText());
                    paymentSuccess.setResponseMessage(resultDesc.asText());
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

                    paymentFailed.setResponseCode(resultCode.asText());
                    paymentFailed.setResponseMessage(resultDesc.asText());
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

                paymentFailed.setResponseCode("68");
                paymentFailed.setResponseMessage("Timeout");
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

        } catch (JsonProcessingException e) {

            paymentFailed.setResponseCode("05");
            paymentFailed.setResponseMessage("General Error");
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

        } catch (RestClientException e) {

            paymentFailed.setResponseCode("05");
            paymentFailed.setResponseMessage("General Error");
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

        }else {

            paymentFailed.setResponseCode("12");
            paymentFailed.setResponseMessage("invalid privateData");
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