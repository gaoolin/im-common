package com.im.qtech.service.msg.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.im.qtech.data.dto.param.EqLstPOJO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/14 11:50:36
 */

@TableName(value = "biz.eqp_aa_lst_parsed_detail")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)  // 注解用于启用链式调用风格，这意味着在调用 setter 方法时，可以返回当前对象，从而使得多个 setter 方法可以链式调用。
@JsonIgnoreProperties(ignoreUnknown = true)
public class EqpLstParsed extends EqLstPOJO {

    private String simId;

    private String module;

    @TableField(typeHandler = PGJsonbTypeHandler.class)
    private String mtfCheckF;

    @TableField(typeHandler = PGJsonbTypeHandler.class)
    private String mtfCheck1F;

    @TableField(typeHandler = PGJsonbTypeHandler.class)
    private String mtfCheck2F;

    @TableField(typeHandler = PGJsonbTypeHandler.class)
    private String mtfCheck3F;

    @TableField(typeHandler = PGJsonbTypeHandler.class)
    private String mtfOffAxisCheck1F;

    @TableField(typeHandler = PGJsonbTypeHandler.class)
    private String mtfOffAxisCheck2F;

    @TableField(typeHandler = PGJsonbTypeHandler.class)
    private String mtfOffAxisCheck3F;

    @TableField(typeHandler = PGJsonbTypeHandler.class)
    private String mtfOffAxisCheck4F;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime receivedTime;
}