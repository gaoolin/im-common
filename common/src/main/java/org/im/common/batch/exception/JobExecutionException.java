package org.im.common.batch.exception;

/**
 * 作业执行异常
 * <p>
 * 作业执行过程中发生的异常
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 * @since 1.0
 */
public class JobExecutionException extends BatchProcessingException {

    private final String jobId;

    public JobExecutionException(String jobId, String message) {
        super(message);
        this.jobId = jobId;
    }

    public JobExecutionException(String jobId, String message, Throwable cause) {
        super(message, cause);
        this.jobId = jobId;
    }

    public JobExecutionException(String jobId, Throwable cause) {
        super(cause);
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }
}
