package com.im.aa.inspection.entity.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/14
 */

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)  // 注解用于启用链式调用风格，这意味着在调用 setter 方法时，可以返回当前对象，从而使得多个 setter 方法可以链式调用。
@JsonIgnoreProperties(ignoreUnknown = true)
public class EqLstParsed extends EqLstSet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(EqLstParsed.class);

    private String simId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receivedTime;

    @Override
    public void reset() {
        this.simId = null;
        this.receivedTime = null;
        super.reset();  // 调用父类的 reset 方法，重置父类的字段
    }

    @Override
    public void fillWithData(List<EqLstCommand> eqLstCommands) {
        super.fillWithData(eqLstCommands);
    }

    /**
     * @param target
     * @param data
     * @throws IllegalAccessException
     * @desc 多态性：
     * 在Java中，方法调用是动态绑定的。这意味着在运行时，实际调用的方法是由对象的实际类型决定的，而不是由引用类型决定的。
     * 当 super.fillWithData(aaListCommands) 被调用时，父类 ImAaListParams 的 fillWithData 方法会被执行。
     * 在父类的 fillWithData 方法中，如果调用了 setFieldsFromData，由于 this 指向的是子类实例，因此会调用子类中重写的 setFieldsFromData 方法。
     */
    @Override
    public void setFieldsFromData(Object target, List<Map<String, String>> data) throws IllegalAccessException {
        super.setFieldsFromData(target, data);
    }
}
