package com.im.qtech.service.msg.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.qtech.common.dto.reverse.EqpReversePOJO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/06/21 08:30:15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class EqpReverseInfo extends EqpReversePOJO {
    private static final long serialVersionUID = 2L;
    private Long id;
    transient private Integer version;
}