package com.im.qtech.service.msg.kafka;

import com.im.qtech.service.msg.avro.EqpReverseInfoRecord;
import com.im.qtech.service.msg.serializer.EqpReverseCompositeKey;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/24 09:59:23
 */

// @Component
public class OracleKafkaStateStore implements KeyValueStore<EqpReverseCompositeKey, EqpReverseInfoRecord> {
    private static final Logger logger = LoggerFactory.getLogger(OracleKafkaStateStore.class);
    private final String storeName;

    // @Autowired
    // @Qualifier("firstJdbcTemplate")
    private JdbcTemplate dataSource;

    public OracleKafkaStateStore(@Value("${spring.kafka.store.name}") String storeName) {
        this.storeName = storeName;
    }

    @Override
    public void put(EqpReverseCompositeKey compositeKey, EqpReverseInfoRecord record) {
        String sql = "MERGE INTO IMBIZ.IM_KAFKA_WB_OLP_CHK_DEDUPLICATE target " +
                "USING (SELECT ? AS SIM_ID, ? AS PROD_TYPE, ? AS CHK_DT FROM dual) source " +
                "ON (target.SIM_ID = source.SIM_ID AND target.CHK_DT = source.CHK_DT) " +
                "WHEN MATCHED THEN " +
                "UPDATE SET target.PROD_TYPE = source.PROD_TYPE, " +
                "target.CODE = ?, " +
                "target.DESCRIPTION = ?, " +
                "target.UPDATED_AT = SYSTIMESTAMP " +
                "WHEN NOT MATCHED THEN " +
                "INSERT (SIM_ID, PROD_TYPE, CHK_DT, CODE, DESCRIPTION) " +
                "VALUES (source.SIM_ID, source.PROD_TYPE, source.CHK_DT, ?, ?)";
        dataSource.update(sql, compositeKey.getSimId(), compositeKey.getModule(), compositeKey.getChkDt(), record.getCode(),
                record.getDescription(), record.getCode(), record.getDescription());

    }

    @Override
    public EqpReverseInfoRecord get(EqpReverseCompositeKey compositeKey) {
        String sql = "SELECT * FROM IMBIZ.IM_KAFKA_WB_OLP_CHK_DEDUPLICATE " +
                "WHERE SIM_ID = ? AND CHK_DT = ?";

        return dataSource.query(sql, new Object[]{compositeKey.getSimId(), compositeKey.getChkDt()},
                rs -> {
                    if (rs.next()) {
                        EqpReverseInfoRecord record = new EqpReverseInfoRecord();
                        record.setSimId(rs.getString("SIM_ID"));
                        record.setModule(rs.getString("PROD_TYPE"));
                        record.setChkDt(rs.getTimestamp("CHK_DT").toInstant());
                        record.setCode(rs.getInt("CODE"));
                        record.setDescription(rs.getString("DESCRIPTION"));
                        return record;
                    }
                    return null;
                });
    }

    @Override
    public EqpReverseInfoRecord putIfAbsent(EqpReverseCompositeKey compositeKey, EqpReverseInfoRecord record) {
        return null;
    }

    @Override
    public KeyValueIterator<EqpReverseCompositeKey, EqpReverseInfoRecord> range(EqpReverseCompositeKey compositeKey, EqpReverseCompositeKey k1) {
        return null;
    }


    @Override
    public String name() {
        return this.storeName;
    }

    @Override
    public void init(ProcessorContext context, StateStore root) {
        // 初始化
    }

    @Override
    public void flush() {

    }

    @Override
    public void putAll(List<KeyValue<EqpReverseCompositeKey, EqpReverseInfoRecord>> list) {

    }

    @Override
    public EqpReverseInfoRecord delete(EqpReverseCompositeKey compositeKey) {
        return null;
    }

    @Override
    public KeyValueIterator<EqpReverseCompositeKey, EqpReverseInfoRecord> all() {
        return null;
    }

    @Override
    public long approximateNumEntries() {
        return 0;
    }

    @Override
    public void close() {
        // 关闭连接等
    }

    @Override
    public boolean persistent() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }
    // 其他方法的实现...
}
