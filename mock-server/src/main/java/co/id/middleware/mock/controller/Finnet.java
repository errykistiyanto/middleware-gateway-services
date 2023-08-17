package co.id.middleware.mock.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author errykistiyanto@gmail.com 2020-10-19
 */

@RestController
@Component("FinnetServices")
@Slf4j
public class Finnet {

    //logstash message direction
    public static final String apps = "mock-server";
    public static final String req_from = "request from ";
    public static final String req_to = "request to ";
    public static final String resp_from = "response from ";
    public static final String resp_to = "response to ";
    public static final String host = "collega";

    @Autowired
    private Environment env;

    @RequestMapping(value = "/mock-server-finnet", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> Services(@RequestBody Map<String, Object> cgson) throws JsonProcessingException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(gson.toJson(cgson));
        String productCode = root.path("productCode").asText();
        String transactionType = root.path("transactionType").asText();

        String service = "";
        String response = "";
        switch (transactionType+productCode) {
            case "38001001":
                service = "inquiry telkom";
                response = "{\n" +
                        "  \"bit61\": \"00210123460040200061209A524559     233960WARREN CUCCURULLO380 1252160 \",\n" +
                        "  \"customerData\": {\n" +
                        "    \"kodeDivre\": \"02\",\n" +
                        "    \"kodeDatel\": \"0006\",\n" +
                        "    \"jumlahBill\": \"1\",\n" +
                        "    \"billInfo1\": {\n" +
                        "      \"nomorReferensi\": \"209A524559\",\n" +
                        "      \"nilaiTagihan\": \"233960\"\n" +
                        "    },\n" +
                        "    \"customerId\": \"0021012346004\",\n" +
                        "    \"customerName\": \"WARREN CUCCURULLO380\",\n" +
                        "    \"npwp\": \"1252160\"\n" +
                        "  },\n" +
                        "  \"resultCode\": \"00\",\n" +
                        "  \"resultDesc\": \"Approve\",\n" +
                        "  \"sysCode\": \"935118275750\",\n" +
                        "  \"productCode\": \"001001\",\n" +
                        "  \"terminal\": \"G009TRKK\",\n" +
                        "  \"transactionType\": \"38\",\n" +
                        "  \"amount\": \"000000233960\",\n" +
                        "  \"feeAmount\": \"2500\",\n" +
                        "  \"bit48\": \"\",\n" +
                        "  \"traxId\": \"9351182757508043\",\n" +
                        "  \"timeStamp\": \"09-06-2017 09:05:32:340\",\n" +
                        "  \"timeStampServer\": \"09-06-2017 02:23:31:823355\"\n" +
                        "}";
                break;
            case "50001001":
                service = "payment telkom";
                response = "{\n" +
                        "  \"bit61\": \"00210123460040200061209A524559     233960WARREN CUCCURULLO3801252160 \",\n" +
                        "  \"customerData\": {\n" +
                        "    \"kodeDivre\": \"02\",\n" +
                        "    \"kodeDatel\": \"0006\",\n" +
                        "    \"jumlahBill\": \"1\",\n" +
                        "    \"billInfo1\": {\n" +
                        "      \"nomorReferensi\": \"209A524559\",\n" +
                        "      \"nilaiTagihan\": \"233960\"\n" +
                        "    },\n" +
                        "    \"customerId\": \"0021012346004\",\n" +
                        "    \"customerName\": \"WARREN CUCCURULLO380\",\n" +
                        "    \"npwp\": \"1252160\"\n" +
                        "  },\n" +
                        "  \"resultCode\": \"00\",\n" +
                        "  \"resultDesc\": \"Approve\",\n" +
                        "  \"sysCode\": \"935118275750\",\n" +
                        "  \"productCode\": \"001001\",\n" +
                        "  \"terminal\": \"G009TRKK\",\n" +
                        "  \"transactionType\": \"50\",\n" +
                        "  \"amount\": \"000000233960\",\n" +
                        "  \"feeAmount\": \"2500\",\n" +
                        "  \"bit48\": \"\",\n" +
                        "  \"traxId\": \"9351182757508045\",\n" +
                        "  \"timeStamp\": \"09-06-2017 09:06:32:340\",\n" +
                        "  \"timeStampServer\": \"09-06-2017 02:35:47:047435\"\n" +
                        "}";
                break;
            case "51001001":
                service = "reversal telkom";
                response = "{\n" +
                        "  \"bit61\": \"00210123460040200061209A524559     233960WARREN CUCCURULLO3801252160 \",\n" +
                        "  \"customerData\": {\n" +
                        "    \"kodeDivre\": \"02\",\n" +
                        "    \"kodeDatel\": \"0006\",\n" +
                        "    \"jumlahBill\": \"1\",\n" +
                        "    \"billInfo1\": {\n" +
                        "      \"nomorReferensi\": \"209A524559\",\n" +
                        "      \"nilaiTagihan\": \"233960\"\n" +
                        "    },\n" +
                        "    \"customerId\": \"0021012346004\",\n" +
                        "    \"customerName\": \"WARREN CUCCURULLO380\",\n" +
                        "    \"npwp\": \"1252160\"\n" +
                        "  },\n" +
                        "  \"resultCode\": \"00\",\n" +
                        "  \"resultDesc\": \"Approve\",\n" +
                        "  \"sysCode\": \"935118275750\",\n" +
                        "  \"productCode\": \"001001\",\n" +
                        "  \"terminal\": \"G009TRKK\",\n" +
                        "  \"transactionType\": \"50\",\n" +
                        "  \"amount\": \"000000233960\",\n" +
                        "  \"feeAmount\": \"2500\",\n" +
                        "  \"bit48\": \"\",\n" +
                        "  \"traxId\": \"9351182757508045\",\n" +
                        "  \"timeStamp\": \"09-06-2017 09:06:32:340\",\n" +
                        "  \"timeStampServer\": \"09-06-2017 02:35:47:047435\"\n" +
                        "}";
                break;

        }

        LinkedHashMap<String, Object> payload1 = new LinkedHashMap<>();
        payload1.put("payload", cgson);

        LinkedHashMap<String, Object> data1 = new LinkedHashMap<>();
        data1.put("service", service);
        data1.put("message_direction", req_from + "gateway");
        data1.put("message_"+apps, payload1);

        log.info(gson.toJson(data1)
                .replace("\\u003d", "=")
                .replace("\\u0026", "&") //character &
                .replace("\\u0027", "'") //character '
                .replace("\\\\", " ") //replace single backslash
        );


        ObjectMapper mapper2 = new ObjectMapper();
        Map<String,Object> map = mapper2.readValue(response, Map.class);

        LinkedHashMap<String, Object> payload2 = new LinkedHashMap<>();
        payload2.put("payload", map);

        LinkedHashMap<String, Object> data2 = new LinkedHashMap<>();
        data2.put("service", service);
        data2.put("message_direction", resp_to + "gateway");
        data2.put("message_"+apps, payload2);

        log.info(gson.toJson(data2)
                .replace("\\u003d", "=")
                .replace("\\u0026", "&") //character &
                .replace("\\u0027", "'") //character '
                .replace("\\\\", " ") //replace single backslash
        );

        return new ResponseEntity(response, HttpStatus.OK);
    }
}