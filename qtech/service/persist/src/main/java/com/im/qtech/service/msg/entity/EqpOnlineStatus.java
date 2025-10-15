package com.im.qtech.service.msg.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.im.qtech.service.msg.serde.EqpOnlineStatusDeserializer;
import com.im.qtech.service.msg.serde.EqpOnlineStatusSerializer;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/11/19 09:39:46
 */

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class EqpOnlineStatus {
    @JsonProperty("receive_date")
    private String receiveDate;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("device_type")
    private String deviceType;

    @JsonProperty("Remote_control")
    private String remoteControl;


    @JsonDeserialize(using = EqpOnlineStatusDeserializer.class)
    @JsonSerialize(using = EqpOnlineStatusSerializer.class)
    private String status;

    private LocalDateTime lastUpdated;
}
