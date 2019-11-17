package net.ketone.accrptgen.config;

public class Constants {

    // Queue
    public static final String GEN_QUEUE_NAME= "accountrptgen-queue";
    public static final String GEN_QUEUE_ENDPOINT = "/worker";

    public static final String STATUS_QUEUE_NAME= "statistics-queue";
    public static final String STATUS_QUEUE_ENDPOINT = "/updateStat";

    // Status
    public enum Status {
        PRELOADED, PENDING, GENERATING, EMAIL_SENT, FAILED
    }

    // Files
    // stores google API key, SendGrid key
    public static final String CREDENTIALS_FILE = "credentials.properties";
    // template Doc for report generation
    public static final String TEMPLATE_FILE = "template.docx";
    // all history, latest first
    // must be of format xxxxx.yyy
    public static final String HISTORY_FILE = "history.txt";
    // login users
    public static final String USERS_FILE = "users.txt";
    public static final String USERS_FILE_SEPARATOR = ",";



}
