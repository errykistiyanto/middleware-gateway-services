package co.id.middleware.finnet.util;

import co.id.middleware.finnet.entity.DataLog;
import co.id.middleware.finnet.repository.HistoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author errykistiyanto@gmail.com 2023-08-02
 */

@Slf4j
@Component("Logging")
public class Logging {

    @Autowired
    private HistoryService historyService;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public String restAPI(String service, String process, String message_direction, String retrieval_number, String account_number, String message_destination,

        Object payload, Object header, Object param, String method, String uri, String address, String port, String status) {

        LinkedHashMap<String, Object> payload1 = new LinkedHashMap<>();
        payload1.put("method", method);
        payload1.put("address", address);
        payload1.put("uri", uri);
        payload1.put("port", port);
        payload1.put("status", status);
        payload1.put("payload", payload);
        payload1.put("header", header);
        payload1.put("param", param);

        LinkedHashMap<String, Object> data1 = new LinkedHashMap<>();
        data1.put("service", service);
        data1.put("process", process);
        data1.put("message_direction", message_direction);
        data1.put("retrieval_number", retrieval_number);
        data1.put("account_number", account_number);
        data1.put("message_"+message_destination, payload1);

        log.info(gson.toJson(data1)
                .replace("\\u003d", "=")
                .replace("\\u0026", "&") //character &
                .replace("\\u0027", "'") //character '
                .replace("\\\\", " ") //replace single backslash
        );

        DataLog dataLog1 = new DataLog();
        dataLog1.setDate(new Date());
        dataLog1.setMessageData(gson.toJson(data1)
                .replace("\\u003d", "=")
                .replace("\\u0026", "&") //character &
                .replace("\\u0027", "'") //character '
                .replace("\\\\", " ") //replace single backslash
        );

        historyService.save(dataLog1);

        return null;
    }


    public Object convertHeader(Object header) throws JsonProcessingException {

        //convert header for logstash spec
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> map = mapper.readValue(gson.toJson(header)
                        .replace("[","")
                        .replace("]","")
                , Map.class);
        //convert header for logstash spec

        return map;
    }

}
