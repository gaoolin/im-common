package com.im.qtech.data.stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.im.qtech.data.model.DeviceTypeSplitter;
import com.im.qtech.data.model.EqNetworkStatus;
import com.im.qtech.data.model.ImportantDeviceAsyncFunction;
import com.im.qtech.data.model.NormalDeviceAsyncFunction;
import com.im.qtech.data.sink.kafka.KafkaSinkProvider;
import com.im.qtech.data.sink.oracle.SimpleOracleBatchSink;
import com.im.qtech.data.sink.postgres.SimplePostgresBatchSink;
import com.im.qtech.data.source.KafkaSourceProvider;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.im.common.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/12/01
 */
public class EqpNetworkStreamJob {
    private static final Logger logger = LoggerFactory.getLogger(EqpNetworkStreamJob.class);
    private static final ObjectMapper objectMapper = JsonMapperProvider.createCustomizedInstance((m) -> {
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        m.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        m.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        m.registerModule(new JavaTimeModule());
    });
    private static final OutputTag<EqNetworkStatus> DB_WRITE_TAG = new OutputTag<EqNetworkStatus>("db-write") {
    };

    public EqpNetworkStreamJob() {
    }

    public static void main(String[] args) throws Exception {
        String savepointPath = null;

        for (int i = 0; i < args.length - 1; ++i) {
            if ("--savepointPath".equals(args[i])) {
                savepointPath = args[i + 1];
                break;
            }
        }

        if (savepointPath != null) {
            logger.warn("⚠ Flink 1.16.3 不支持在代码中设置 savepoint 恢复路径！");
            logger.warn("⚠ 请使用命令行运行：bin/flink run -s {} -c com.qtech.stream.EqpNetworkStreamJob your-job.jar", savepointPath);
            System.exit(1);
        }

        logger.info(">>>>> Starting Flink Job: Device Online Status Flink Stream Job");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(3);
        env.enableCheckpointing(10000L);
        env.getCheckpointConfig().setCheckpointTimeout(120000L);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(5000L);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);
        env.setRestartStrategy(RestartStrategies.exponentialDelayRestart(Time.of(1L, TimeUnit.SECONDS), Time.of(60L, TimeUnit.SECONDS), 2.0, Time.of(10L, TimeUnit.MINUTES), 0.1));
        DataStream<String> sourceStream = env.fromSource(KafkaSourceProvider.createSource(), WatermarkStrategy.noWatermarks(), "Kafka-Source").filter((msg) -> {
            return msg != null && !msg.isEmpty();
        });
        DeviceTypeSplitter.SplitStreams splitStreams = DeviceTypeSplitter.split(sourceStream);
        SingleOutputStreamOperator<EqNetworkStatus> normalDevices = AsyncDataStream.unorderedWait(splitStreams.getNormalStream(), new NormalDeviceAsyncFunction(), 30L, TimeUnit.SECONDS, 2000).setParallelism(6);
        SingleOutputStreamOperator<EqNetworkStatus> importantDevices = AsyncDataStream.unorderedWait(splitStreams.getImportantStream(), new ImportantDeviceAsyncFunction(), 30L, TimeUnit.SECONDS, 2000).setParallelism(6);
        DataStream<EqNetworkStatus> mergedStream = normalDevices.union(new DataStream[]{importantDevices});
        DataStream<String> kafkaStream = mergedStream.map((record) -> {
            try {
                return objectMapper.writeValueAsString(record);
            } catch (Exception var2) {
                logger.error("Failed to serialize EqNetworkStatus to JSON", var2);
                return "{}";
            }
        });
        kafkaStream.sinkTo(KafkaSinkProvider.createSink()).name("Kafka-Sink").setParallelism(3);
        SingleOutputStreamOperator<EqNetworkStatus> mainWithSideOutput = mergedStream.process(new ProcessFunction<EqNetworkStatus, EqNetworkStatus>() {
            @Override
            public void processElement(EqNetworkStatus value, Context ctx, Collector<EqNetworkStatus> out) throws Exception {
                out.collect(value);
                ctx.output(DB_WRITE_TAG, value);
            }
        });
        DataStream<EqNetworkStatus> dbWriteStream = mainWithSideOutput.getSideOutput(DB_WRITE_TAG);
        dbWriteStream.addSink(new SimpleOracleBatchSink()).name("Oracle-AbstractBatchSink").setParallelism(2);
        dbWriteStream.addSink(new SimplePostgresBatchSink()).name("Postgres-AbstractBatchSink").setParallelism(2);
        logger.info(">>>>> Executing Flink job...");
        env.execute("im-device-network-stream");
    }
}
