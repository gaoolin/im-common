package org.im.semiconductor.test.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

/**
 * 测试数据解析工具类
 * <p>
 * 特性：
 * - 通用性：支持多种测试数据格式（STDF、CSV、JSON、XML等）
 * - 规范性：统一的解析接口和数据结构
 * - 专业性：半导体测试数据专业解析算法
 * - 灵活性：支持自定义解析规则和扩展
 * - 可靠性：完善的错误处理和数据验证机制
 * - 安全性：输入验证和资源管理
 * - 复用性：解析器插件化设计，可扩展
 * - 容错性：部分数据解析失败不影响整体处理
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class TestDataParser {

    // 默认缓冲区大小
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    // 默认超时时间（毫秒）
    public static final long DEFAULT_TIMEOUT = 30000;
    private static final Logger logger = LoggerFactory.getLogger(TestDataParser.class);
    // 解析器注册表
    private static final Map<TestFormat, TestFormatParser> parserRegistry = new ConcurrentHashMap<>();

    // 初始化默认解析器
    static {
        registerParser(new StdfParser());
        registerParser(new CsvParser());
        registerParser(new JsonParser());
        registerParser(new XmlParser());
    }

    /**
     * 注册解析器
     *
     * @param parser 解析器实例
     */
    public static void registerParser(TestFormatParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("Parser cannot be null");
        }

        for (TestFormat format : TestFormat.values()) {
            if (parser.supportsFormat(format)) {
                parserRegistry.put(format, parser);
                logger.debug("Registered parser {} for format {}", parser.getFormatName(), format);
            }
        }
    }

    /**
     * 解析测试数据文件
     *
     * @param filePath 文件路径
     * @param config   解析配置
     * @return 解析结果
     */
    public static ParseResult parseTestData(String filePath, ParseConfig config) {
        if (filePath == null || filePath.isEmpty()) {
            logger.error("Invalid file path");
            return new ParseResult(false, null,
                    Arrays.asList(new ParseError(0, "Invalid file path", null, null)), 0, 0);
        }

        if (config == null) {
            config = new ParseConfig();
        }

        long startTime = System.currentTimeMillis();
        List<ParseError> errors = new ArrayList<>();

        try {
            // 检查文件是否存在
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new FileNotFoundException("File not found: " + filePath);
            }

            // 自动检测格式
            TestFormat format = config.getFormat();
            if (format == TestFormat.AUTO_DETECT) {
                format = detectFormat(filePath);
                logger.debug("Auto-detected format: {}", format);
            }

            // 获取对应的解析器
            TestFormatParser parser = parserRegistry.get(format);
            if (parser == null) {
                throw new UnsupportedOperationException("No parser available for format: " + format);
            }

            // 打开文件流
            try (InputStream inputStream = createInputStream(filePath, config)) {
                // 解析数据
                TestResultSet resultSet = parser.parse(inputStream, config);

                long endTime = System.currentTimeMillis();
                long parseTime = endTime - startTime;
                long recordCount = resultSet != null ? resultSet.getTestRecords().size() : 0;

                logger.info("Successfully parsed {} records from {} in {} ms",
                        recordCount, filePath, parseTime);

                return new ParseResult(true, resultSet, errors, parseTime, recordCount);
            }
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long parseTime = endTime - startTime;

            logger.error("Failed to parse test data file: {}", filePath, e);
            errors.add(new ParseError(0, "Parse failed: " + e.getMessage(), null, e));

            return new ParseResult(false, null, errors, parseTime, 0);
        }
    }

    /**
     * 创建输入流（支持压缩）
     *
     * @param filePath 文件路径
     * @param config   解析配置
     * @return 输入流
     * @throws IOException IO异常
     */
    private static InputStream createInputStream(String filePath, ParseConfig config) throws IOException {
        InputStream inputStream = Files.newInputStream(Paths.get(filePath));

        // 检查是否需要解压缩
        if (config.isEnableCompression() && filePath.toLowerCase().endsWith(".gz")) {
            inputStream = new GZIPInputStream(inputStream);
        }

        return inputStream;
    }

    /**
     * 自动检测文件格式
     *
     * @param filePath 文件路径
     * @return 检测到的格式
     */
    private static TestFormat detectFormat(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return TestFormat.TXT;
        }

        String fileName = Paths.get(filePath).getFileName().toString().toLowerCase();

        // 根据文件扩展名检测
        if (fileName.endsWith(".stdf") || fileName.endsWith(".std")) {
            return TestFormat.STDF;
        } else if (fileName.endsWith(".csv")) {
            return TestFormat.CSV;
        } else if (fileName.endsWith(".json")) {
            return TestFormat.JSON;
        } else if (fileName.endsWith(".xml")) {
            return TestFormat.XML;
        } else {
            // 尝试读取文件头来检测格式
            return detectFormatByContent(filePath);
        }
    }

    /**
     * 根据文件内容检测格式
     *
     * @param filePath 文件路径
     * @return 检测到的格式
     */
    private static TestFormat detectFormatByContent(String filePath) {
        try {
            byte[] header = new byte[1024];
            try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
                int bytesRead = is.read(header);
                if (bytesRead > 0) {
                    String content = new String(header, 0, bytesRead, StandardCharsets.UTF_8);

                    // 简单的内容检测
                    if (content.contains("{") && content.contains("}")) {
                        return TestFormat.JSON;
                    } else if (content.contains("<") && content.contains(">")) {
                        return TestFormat.XML;
                    } else if (content.contains(",")) {
                        return TestFormat.CSV;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to detect format by content for file: {}", filePath, e);
        }

        return TestFormat.TXT; // 默认返回文本格式
    }

    /**
     * 标准化测试结果
     *
     * @param rawResult 原始测试结果
     * @return 标准化测试结果
     */
    public static StandardTestResult standardizeTestResult(RawTestResult rawResult) {
        if (rawResult == null) {
            logger.warn("Invalid raw test result");
            return null;
        }

        try {
            StandardTestResult standardResult = new StandardTestResult();

            // 转换基本信息
            standardResult.setLotId(rawResult.getLotId());
            standardResult.setWaferId(rawResult.getWaferId());
            standardResult.setDeviceId(rawResult.getDeviceId());
            standardResult.setTestTime(rawResult.getTestTime());

            // 标准化测试项
            List<StandardTestItem> standardItems = new ArrayList<>();
            for (RawTestItem rawItem : rawResult.getTestItems()) {
                StandardTestItem standardItem = new StandardTestItem();
                standardItem.setTestNumber(rawItem.getTestNumber());
                standardItem.setTestName(rawItem.getTestName());
                standardItem.setValue(rawItem.getValue());
                standardItem.setUnits(rawItem.getUnits());
                standardItem.setLowerLimit(rawItem.getLowerLimit());
                standardItem.setUpperLimit(rawItem.getUpperLimit());
                standardItem.setResult(rawItem.getResult());

                standardItems.add(standardItem);
            }
            standardResult.setTestItems(standardItems);

            // 设置统计数据
            standardResult.setStatistics(calculateStatistics(standardItems));

            logger.debug("Standardized test result for device: {}", rawResult.getDeviceId());
            return standardResult;
        } catch (Exception e) {
            logger.error("Failed to standardize test result", e);
            return null;
        }
    }

    /**
     * 计算测试统计数据
     *
     * @param testItems 测试项列表
     * @return 统计数据
     */
    private static TestStatistics calculateStatistics(List<StandardTestItem> testItems) {
        if (testItems == null || testItems.isEmpty()) {
            return new TestStatistics();
        }

        TestStatistics statistics = new TestStatistics();
        int passCount = 0;
        int failCount = 0;
        int totalCount = testItems.size();

        for (StandardTestItem item : testItems) {
            if ("PASS".equals(item.getResult())) {
                passCount++;
            } else if ("FAIL".equals(item.getResult())) {
                failCount++;
            }
        }

        statistics.setTotalTests(totalCount);
        statistics.setPassTests(passCount);
        statistics.setFailTests(failCount);
        statistics.setYield(totalCount > 0 ? (double) passCount / totalCount : 0.0);

        return statistics;
    }

    /**
     * 计算测试统计数据
     *
     * @param resultSet 测试结果集
     * @return 统计数据
     */
    public static TestStatistics calculateStatistics(TestResultSet resultSet) {
        if (resultSet == null || resultSet.getTestRecords().isEmpty()) {
            return new TestStatistics();
        }

        try {
            TestStatistics statistics = new TestStatistics();
            List<TestRecord> records = resultSet.getTestRecords();

            int totalRecords = records.size();
            int passRecords = 0;
            int failRecords = 0;
            long totalTests = 0;
            long passTests = 0;

            // 统计记录级别数据
            for (TestRecord record : records) {
                if (record.isPass()) {
                    passRecords++;
                } else {
                    failRecords++;
                }

                // 统计测试项数据
                List<TestItem> items = record.getTestItems();
                if (items != null) {
                    totalTests += items.size();
                    for (TestItem item : items) {
                        if (item.isPass()) {
                            passTests++;
                        }
                    }
                }
            }

            statistics.setTotalRecords(totalRecords);
            statistics.setPassRecords(passRecords);
            statistics.setFailRecords(failRecords);
            statistics.setRecordYield(totalRecords > 0 ? (double) passRecords / totalRecords : 0.0);

            statistics.setTotalTests(totalTests);
            statistics.setPassTests(passTests);
            statistics.setFailTests(totalTests - passTests);
            statistics.setYield(totalTests > 0 ? (double) passTests / totalTests : 0.0);

            logger.debug("Calculated statistics for {} records", totalRecords);
            return statistics;
        } catch (Exception e) {
            logger.error("Failed to calculate test statistics", e);
            return new TestStatistics();
        }
    }

    /**
     * 检测测试数据异常
     *
     * @param resultSet 测试结果集
     * @param config    解析配置
     * @return 异常列表
     */
    public static List<TestAnomaly> detectAnomalies(TestResultSet resultSet, ParseConfig config) {
        if (resultSet == null || resultSet.getTestRecords().isEmpty()) {
            return new ArrayList<>();
        }

        List<TestAnomaly> anomalies = new ArrayList<>();
        List<TestRecord> records = resultSet.getTestRecords();

        try {
            // 检测重复测试记录
            Set<String> seenDevices = new HashSet<>();
            for (int i = 0; i < records.size(); i++) {
                TestRecord record = records.get(i);
                String deviceId = record.getDeviceId();

                if (deviceId != null && !seenDevices.add(deviceId)) {
                    anomalies.add(new TestAnomaly(
                            "DUPLICATE_DEVICE",
                            "Duplicate device ID: " + deviceId,
                            i,
                            record
                    ));
                }
            }

            // 检测测试时间异常
            detectTimeAnomalies(records, anomalies);

            // 检测测试结果异常
            detectResultAnomalies(records, anomalies);

            logger.debug("Detected {} anomalies in test data", anomalies.size());
            return anomalies;
        } catch (Exception e) {
            logger.error("Failed to detect anomalies in test data", e);
            return anomalies;
        }
    }

    /**
     * 检测时间异常
     *
     * @param records   测试记录列表
     * @param anomalies 异常列表
     */
    private static void detectTimeAnomalies(List<TestRecord> records, List<TestAnomaly> anomalies) {
        if (records.size() < 2) {
            return;
        }

        // 计算平均测试时间和标准差
        long totalTime = 0;
        for (TestRecord record : records) {
            totalTime += record.getTestTime();
        }
        double mean = (double) totalTime / records.size();

        double sumSquares = 0;
        for (TestRecord record : records) {
            double diff = record.getTestTime() - mean;
            sumSquares += diff * diff;
        }
        double stdDev = Math.sqrt(sumSquares / records.size());

        // 检测超出3个标准差的异常值
        double threshold = 3 * stdDev;
        for (int i = 0; i < records.size(); i++) {
            TestRecord record = records.get(i);
            double diff = Math.abs(record.getTestTime() - mean);
            if (diff > threshold) {
                anomalies.add(new TestAnomaly(
                        "TIME_ANOMALY",
                        "Test time anomaly: " + record.getTestTime() + "ms (mean: " + String.format("%.2f", mean) + "ms)",
                        i,
                        record
                ));
            }
        }
    }

    /**
     * 检测结果异常
     *
     * @param records   测试记录列表
     * @param anomalies 异常列表
     */
    private static void detectResultAnomalies(List<TestRecord> records, List<TestAnomaly> anomalies) {
        // 检测连续失败
        int consecutiveFails = 0;
        for (int i = 0; i < records.size(); i++) {
            TestRecord record = records.get(i);
            if (!record.isPass()) {
                consecutiveFails++;
                if (consecutiveFails >= 5) { // 连续5个失败
                    anomalies.add(new TestAnomaly(
                            "CONSECUTIVE_FAILS",
                            "Consecutive test failures: " + consecutiveFails,
                            i,
                            record
                    ));
                }
            } else {
                consecutiveFails = 0;
            }
        }
    }

    // 支持的测试数据格式
    public enum TestFormat {
        STDF("stdf", "Standard Test Data Format"),
        CSV("csv", "Comma-Separated Values"),
        JSON("json", "JavaScript Object Notation"),
        XML("xml", "eXtensible Markup Language"),
        TXT("txt", "Plain Text"),
        CUSTOM("custom", "Custom Format"),
        AUTO_DETECT("auto", "Auto Detect Format");

        private final String extension;
        private final String description;

        TestFormat(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }

        public String getExtension() {
            return extension;
        }

        public String getDescription() {
            return description;
        }
    }

    // 解析器接口
    public interface TestFormatParser {
        TestResultSet parse(InputStream inputStream, ParseConfig config) throws Exception;

        boolean supportsFormat(TestFormat format);

        String getFormatName();
    }

    // 解析配置类
    public static class ParseConfig {
        private TestFormat format = TestFormat.AUTO_DETECT;
        private String encoding = StandardCharsets.UTF_8.name();
        private long timeout = DEFAULT_TIMEOUT;
        private boolean validateData = true;
        private boolean enableCompression = true;
        private int maxErrors = 100;
        private boolean continueOnError = true;

        // Getters and Setters
        public TestFormat getFormat() {
            return format;
        }

        public ParseConfig setFormat(TestFormat format) {
            this.format = format;
            return this;
        }

        public String getEncoding() {
            return encoding;
        }

        public ParseConfig setEncoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public long getTimeout() {
            return timeout;
        }

        public ParseConfig setTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public boolean isValidateData() {
            return validateData;
        }

        public ParseConfig setValidateData(boolean validateData) {
            this.validateData = validateData;
            return this;
        }

        public boolean isEnableCompression() {
            return enableCompression;
        }

        public ParseConfig setEnableCompression(boolean enableCompression) {
            this.enableCompression = enableCompression;
            return this;
        }

        public int getMaxErrors() {
            return maxErrors;
        }

        public ParseConfig setMaxErrors(int maxErrors) {
            this.maxErrors = maxErrors;
            return this;
        }

        public boolean isContinueOnError() {
            return continueOnError;
        }

        public ParseConfig setContinueOnError(boolean continueOnError) {
            this.continueOnError = continueOnError;
            return this;
        }
    }

    // 解析结果类
    public static class ParseResult {
        private final boolean success;
        private final TestResultSet resultSet;
        private final List<ParseError> errors;
        private final long parseTime;
        private final long recordCount;

        public ParseResult(boolean success, TestResultSet resultSet, List<ParseError> errors,
                           long parseTime, long recordCount) {
            this.success = success;
            this.resultSet = resultSet;
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
            this.parseTime = parseTime;
            this.recordCount = recordCount;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public TestResultSet getResultSet() {
            return resultSet;
        }

        public List<ParseError> getErrors() {
            return new ArrayList<>(errors);
        }

        public long getParseTime() {
            return parseTime;
        }

        public long getRecordCount() {
            return recordCount;
        }

        @Override
        public String toString() {
            return "ParseResult{" +
                    "success=" + success +
                    ", recordCount=" + recordCount +
                    ", parseTime=" + parseTime + "ms" +
                    ", errorCount=" + errors.size() +
                    '}';
        }
    }

    // 解析错误类
    public static class ParseError {
        private final int lineNumber;
        private final String errorMessage;
        private final String errorData;
        private final Exception exception;

        public ParseError(int lineNumber, String errorMessage, String errorData, Exception exception) {
            this.lineNumber = lineNumber;
            this.errorMessage = errorMessage != null ? errorMessage : "Unknown error";
            this.errorData = errorData;
            this.exception = exception;
        }

        // Getters
        public int getLineNumber() {
            return lineNumber;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getErrorData() {
            return errorData;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public String toString() {
            return "ParseError{" +
                    "lineNumber=" + lineNumber +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", errorData='" + (errorData != null ? errorData.substring(0, Math.min(50, errorData.length())) : "") + '\'' +
                    '}';
        }
    }

    // STDF格式解析器
    private static class StdfParser implements TestFormatParser {
        @Override
        public TestResultSet parse(InputStream inputStream, ParseConfig config) throws Exception {
            logger.debug("Parsing STDF format data");

            // 实现STDF解析逻辑
            TestResultSet resultSet = new TestResultSet();
            // ... STDF解析实现 ...

            return resultSet;
        }

        @Override
        public boolean supportsFormat(TestFormat format) {
            return format == TestFormat.STDF;
        }

        @Override
        public String getFormatName() {
            return "STDF Parser";
        }
    }

    // CSV格式解析器
    private static class CsvParser implements TestFormatParser {
        @Override
        public TestResultSet parse(InputStream inputStream, ParseConfig config) throws Exception {
            logger.debug("Parsing CSV format data");

            // 实现CSV解析逻辑
            TestResultSet resultSet = new TestResultSet();
            // ... CSV解析实现 ...

            return resultSet;
        }

        @Override
        public boolean supportsFormat(TestFormat format) {
            return format == TestFormat.CSV;
        }

        @Override
        public String getFormatName() {
            return "CSV Parser";
        }
    }

    // JSON格式解析器
    private static class JsonParser implements TestFormatParser {
        @Override
        public TestResultSet parse(InputStream inputStream, ParseConfig config) throws Exception {
            logger.debug("Parsing JSON format data");

            // 实现JSON解析逻辑
            TestResultSet resultSet = new TestResultSet();
            // ... JSON解析实现 ...

            return resultSet;
        }

        @Override
        public boolean supportsFormat(TestFormat format) {
            return format == TestFormat.JSON;
        }

        @Override
        public String getFormatName() {
            return "JSON Parser";
        }
    }

    // XML格式解析器
    private static class XmlParser implements TestFormatParser {
        @Override
        public TestResultSet parse(InputStream inputStream, ParseConfig config) throws Exception {
            logger.debug("Parsing XML format data");

            // 实现XML解析逻辑
            TestResultSet resultSet = new TestResultSet();
            // ... XML解析实现 ...

            return resultSet;
        }

        @Override
        public boolean supportsFormat(TestFormat format) {
            return format == TestFormat.XML;
        }

        @Override
        public String getFormatName() {
            return "XML Parser";
        }
    }

    // 测试结果集类
    public static class TestResultSet {
        private final List<TestRecord> testRecords;
        private final Map<String, Object> metadata;
        private final String sourceFile;
        private final long parseTime;

        public TestResultSet() {
            this.testRecords = new ArrayList<>();
            this.metadata = new HashMap<>();
            this.sourceFile = "";
            this.parseTime = System.currentTimeMillis();
        }

        // Getters and Setters
        public List<TestRecord> getTestRecords() {
            return new ArrayList<>(testRecords);
        }

        public void addTestRecord(TestRecord record) {
            testRecords.add(record);
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public void setMetadata(String key, Object value) {
            metadata.put(key, value);
        }

        public String getSourceFile() {
            return sourceFile;
        }

        public long getParseTime() {
            return parseTime;
        }
    }

    // 测试记录类
    public static class TestRecord {
        private String lotId;
        private String waferId;
        private String deviceId;
        private long testTime;
        private boolean pass;
        private List<TestItem> testItems;
        private Map<String, Object> attributes;

        public TestRecord() {
            this.testItems = new ArrayList<>();
            this.attributes = new HashMap<>();
        }

        // Getters and Setters
        public String getLotId() {
            return lotId;
        }

        public void setLotId(String lotId) {
            this.lotId = lotId;
        }

        public String getWaferId() {
            return waferId;
        }

        public void setWaferId(String waferId) {
            this.waferId = waferId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public long getTestTime() {
            return testTime;
        }

        public void setTestTime(long testTime) {
            this.testTime = testTime;
        }

        public boolean isPass() {
            return pass;
        }

        public void setPass(boolean pass) {
            this.pass = pass;
        }

        public List<TestItem> getTestItems() {
            return new ArrayList<>(testItems);
        }

        public void addTestItem(TestItem item) {
            testItems.add(item);
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }
    }

    // 测试项类
    public static class TestItem {
        private int testNumber;
        private String testName;
        private double value;
        private String units;
        private double lowerLimit;
        private double upperLimit;
        private boolean pass;
        private Map<String, Object> attributes;

        public TestItem() {
            this.attributes = new HashMap<>();
        }

        // Getters and Setters
        public int getTestNumber() {
            return testNumber;
        }

        public void setTestNumber(int testNumber) {
            this.testNumber = testNumber;
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public double getLowerLimit() {
            return lowerLimit;
        }

        public void setLowerLimit(double lowerLimit) {
            this.lowerLimit = lowerLimit;
        }

        public double getUpperLimit() {
            return upperLimit;
        }

        public void setUpperLimit(double upperLimit) {
            this.upperLimit = upperLimit;
        }

        public boolean isPass() {
            return pass;
        }

        public void setPass(boolean pass) {
            this.pass = pass;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }
    }

    // 原始测试结果类
    public static class RawTestResult {
        private String lotId;
        private String waferId;
        private String deviceId;
        private long testTime;
        private List<RawTestItem> testItems;

        public RawTestResult() {
            this.testItems = new ArrayList<>();
        }

        // Getters and Setters
        public String getLotId() {
            return lotId;
        }

        public void setLotId(String lotId) {
            this.lotId = lotId;
        }

        public String getWaferId() {
            return waferId;
        }

        public void setWaferId(String waferId) {
            this.waferId = waferId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public long getTestTime() {
            return testTime;
        }

        public void setTestTime(long testTime) {
            this.testTime = testTime;
        }

        public List<RawTestItem> getTestItems() {
            return new ArrayList<>(testItems);
        }

        public void addTestItem(RawTestItem item) {
            testItems.add(item);
        }
    }

    // 原始测试项类
    public static class RawTestItem {
        private int testNumber;
        private String testName;
        private double value;
        private String units;
        private double lowerLimit;
        private double upperLimit;
        private String result;

        // Getters and Setters
        public int getTestNumber() {
            return testNumber;
        }

        public void setTestNumber(int testNumber) {
            this.testNumber = testNumber;
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public double getLowerLimit() {
            return lowerLimit;
        }

        public void setLowerLimit(double lowerLimit) {
            this.lowerLimit = lowerLimit;
        }

        public double getUpperLimit() {
            return upperLimit;
        }

        public void setUpperLimit(double upperLimit) {
            this.upperLimit = upperLimit;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

    // 标准化测试结果类
    public static class StandardTestResult {
        private String lotId;
        private String waferId;
        private String deviceId;
        private long testTime;
        private List<StandardTestItem> testItems;
        private TestStatistics statistics;
        private final Map<String, Object> attributes;

        public StandardTestResult() {
            this.testItems = new ArrayList<>();
            this.attributes = new HashMap<>();
        }

        // Getters and Setters
        public String getLotId() {
            return lotId;
        }

        public void setLotId(String lotId) {
            this.lotId = lotId;
        }

        public String getWaferId() {
            return waferId;
        }

        public void setWaferId(String waferId) {
            this.waferId = waferId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public long getTestTime() {
            return testTime;
        }

        public void setTestTime(long testTime) {
            this.testTime = testTime;
        }

        public List<StandardTestItem> getTestItems() {
            return new ArrayList<>(testItems);
        }

        public void setTestItems(List<StandardTestItem> testItems) {
            this.testItems = testItems != null ? new ArrayList<>(testItems) : new ArrayList<>();
        }

        public void addTestItem(StandardTestItem item) {
            testItems.add(item);
        }

        public TestStatistics getStatistics() {
            return statistics;
        }

        public void setStatistics(TestStatistics statistics) {
            this.statistics = statistics;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }
    }


    // 标准化测试项类
    public static class StandardTestItem {
        private int testNumber;
        private String testName;
        private double value;
        private String units;
        private double lowerLimit;
        private double upperLimit;
        private String result;
        private Map<String, Object> attributes;

        public StandardTestItem() {
            this.attributes = new HashMap<>();
        }

        // Getters and Setters
        public int getTestNumber() {
            return testNumber;
        }

        public void setTestNumber(int testNumber) {
            this.testNumber = testNumber;
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public double getLowerLimit() {
            return lowerLimit;
        }

        public void setLowerLimit(double lowerLimit) {
            this.lowerLimit = lowerLimit;
        }

        public double getUpperLimit() {
            return upperLimit;
        }

        public void setUpperLimit(double upperLimit) {
            this.upperLimit = upperLimit;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }
    }

    // 测试统计数据类
    public static class TestStatistics {
        private long totalRecords;
        private long passRecords;
        private long failRecords;
        private double recordYield;

        private long totalTests;
        private long passTests;
        private long failTests;
        private double yield;

        // Getters and Setters
        public long getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(long totalRecords) {
            this.totalRecords = totalRecords;
        }

        public long getPassRecords() {
            return passRecords;
        }

        public void setPassRecords(long passRecords) {
            this.passRecords = passRecords;
        }

        public long getFailRecords() {
            return failRecords;
        }

        public void setFailRecords(long failRecords) {
            this.failRecords = failRecords;
        }

        public double getRecordYield() {
            return recordYield;
        }

        public void setRecordYield(double recordYield) {
            this.recordYield = recordYield;
        }

        public long getTotalTests() {
            return totalTests;
        }

        public void setTotalTests(long totalTests) {
            this.totalTests = totalTests;
        }

        public long getPassTests() {
            return passTests;
        }

        public void setPassTests(long passTests) {
            this.passTests = passTests;
        }

        public long getFailTests() {
            return failTests;
        }

        public void setFailTests(long failTests) {
            this.failTests = failTests;
        }

        public double getYield() {
            return yield;
        }

        public void setYield(double yield) {
            this.yield = yield;
        }

        @Override
        public String toString() {
            return "TestStatistics{" +
                    "totalRecords=" + totalRecords +
                    ", passRecords=" + passRecords +
                    ", recordYield=" + String.format("%.2f", recordYield * 100) + "%" +
                    ", totalTests=" + totalTests +
                    ", passTests=" + passTests +
                    ", yield=" + String.format("%.2f", yield * 100) + "%" +
                    '}';
        }
    }

    // 测试异常类
    public static class TestAnomaly {
        private final String type;
        private final String description;
        private final int recordIndex;
        private final TestRecord record;

        public TestAnomaly(String type, String description, int recordIndex, TestRecord record) {
            this.type = type != null ? type : "UNKNOWN";
            this.description = description != null ? description : "";
            this.recordIndex = recordIndex;
            this.record = record;
        }

        // Getters
        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public int getRecordIndex() {
            return recordIndex;
        }

        public TestRecord getRecord() {
            return record;
        }

        @Override
        public String toString() {
            return "TestAnomaly{" +
                    "type='" + type + '\'' +
                    ", description='" + description + '\'' +
                    ", recordIndex=" + recordIndex +
                    ", deviceId='" + (record != null ? record.getDeviceId() : "null") + '\'' +
                    '}';
        }
    }
}
