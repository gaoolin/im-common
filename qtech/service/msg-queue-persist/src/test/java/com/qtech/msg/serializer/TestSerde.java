package com.qtech.msg.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2024/11/26 11:23:20
 * desc   :
 */


public class TestSerde {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper objectMapper = configureObjectMapper();
        EqOnlineStatus eqOnlineStatus = new EqOnlineStatus();
        eqOnlineStatus.setReceiveDate("2024-11-26 11:14:22");
        eqOnlineStatus.setDeviceId("17");
        eqOnlineStatus.setDeviceType("4GLinuxAA");
        eqOnlineStatus.setRemoteControl("1");
        eqOnlineStatus.setStatus("1");

        String json = objectMapper.writeValueAsString(eqOnlineStatus);
        System.out.println(json);


        String a = "{\"Status\":\"1\",\"lastUpdated\":null,\"receive_date\":\"2024-11-26 11:14:22\",\"device_id\":\"17\",\"device_type\":\"4GLinuxAA\",\"Remote_control\":\"1\"}";
        EqOnlineStatus eqOnlineStatus1 = objectMapper.readValue(a, EqOnlineStatus.class);
        System.out.println(eqOnlineStatus1);
        String s = objectMapper.writeValueAsString(eqOnlineStatus1);
        System.out.println(s);

    }

    public static ObjectMapper configureObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .enable(MapperFeature.AUTO_DETECT_FIELDS);
    }
}