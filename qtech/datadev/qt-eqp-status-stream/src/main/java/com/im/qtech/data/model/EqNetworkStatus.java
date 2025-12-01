package com.im.qtech.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.im.qtech.data.serde.EqNetworkStatusDeserializer;
import com.im.qtech.data.serde.EqNetworkStatusSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备联网状态实体类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/23 13:56:20
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class EqNetworkStatus implements Serializable {
    @JsonProperty("receive_date")
    private String receiveDate;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("device_type")
    private String deviceType;

    @JsonProperty("LotName")
    private String lotName;

    @JsonProperty("Remote_control")
    private String remoteControl;

    @JsonDeserialize(using = EqNetworkStatusDeserializer.class)
    @JsonSerialize(using = EqNetworkStatusSerializer.class)
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdated;
}