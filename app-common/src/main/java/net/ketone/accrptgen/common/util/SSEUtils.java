package net.ketone.accrptgen.common.util;

import net.ketone.accrptgen.common.model.AccountJob;
import org.springframework.http.codec.ServerSentEvent;

public class SSEUtils {

    public static ServerSentEvent<AccountJob> toSSE(final AccountJob accountJob) {
        return ServerSentEvent.<AccountJob> builder()
                .event("message")
                .data(accountJob).build();
    }
}
