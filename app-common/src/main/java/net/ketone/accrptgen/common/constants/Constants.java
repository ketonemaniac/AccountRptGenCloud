package net.ketone.accrptgen.common.constants;

public class Constants {

    // Queue
    public static final String GEN_QUEUE_NAME= "accountrptgen-queue";
    public static final String GEN_QUEUE_ENDPOINT = "/worker";
    public static final String GEN_QUEUE_ENDPOINT_EXECL_EXTRACT = "/task/excel-extract-task";

    public static final String STATUS_QUEUE_NAME= "statistics-queue";
    public static final String STATUS_QUEUE_ENDPOINT = "/updateStat";

    // Status
    public enum Status {
        PRELOADED, PENDING, GENERATING, EMAIL_SENT, FAILED
    }

    // Files
    // stores google API key, SendGrid key
    public static final String CREDENTIALS_FILE = "credentials.properties";
    // stores non-password settings
    public static final String CONFIGURATION_FILE = "configuration.properties";
    // template Doc for report generation
    public static final String TEMPLATE_FILE = "template.docx";
    // all history, latest first
    // must be of format xxxxx.yyy
    @Deprecated
    public static final String HISTORY_FILE = "history.txt";

}
