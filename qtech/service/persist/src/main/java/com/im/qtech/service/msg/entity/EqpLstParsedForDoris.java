package com.im.qtech.service.msg.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.qtech.data.dto.param.EqLstPOJO;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/15
 */
@TableName(value = "im_aa_list_parsed_detail", autoResultMap = true, schema = "qtech_eq_dwd")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)  // 注解用于启用链式调用风格，这意味着在调用 setter 方法时，可以返回当前对象，从而使得多个 setter 方法可以链式调用。
@JsonIgnoreProperties(ignoreUnknown = true)
public class EqpLstParsedForDoris extends EqLstPOJO {

    private String simId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime receivedTime;
}