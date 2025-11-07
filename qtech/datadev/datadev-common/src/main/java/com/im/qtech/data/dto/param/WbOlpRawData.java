package com.im.qtech.data.dto.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/23 13:41:06
 */
@Data
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class WbOlpRawData implements Serializable {
    private static final long serialVersionUID = 2L;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime dt;
    private String simId;
    private String moduleId;
    private Integer wireId;
    private String leadX;
    private String leadY;
    private String padX;
    private String padY;
    private Integer checkPort;
    private Integer piecesIndex;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date loadTime;
}
