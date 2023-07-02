package net.ketone.accrptgen.app.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.tasks.v2.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.AccountJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Profile("gCloudStandard & !cloudRun")
@Slf4j
public class TaskQueueService implements TasksService {

    @Autowired
    private ObjectMapper mapper;

    @Value("${tasks.projectId}")
    private String projectId;

    @Override
    public AccountJob submitTask(AccountJob dto, final String endpoint) throws IOException {
        try (CloudTasksClient client = CloudTasksClient.create()) {
            String locationId = "asia-east2";
            String queueId = "accountrptgen-queue";

            // Construct the fully qualified queue name.
            String queueName = QueueName.of(projectId, locationId, queueId).toString();

            Task taskPayload =
                    Task.newBuilder()
                            .setAppEngineHttpRequest(
                                    AppEngineHttpRequest.newBuilder()
                                            .setBody(ByteString.copyFrom(mapper.writeValueAsString(dto).getBytes()))
                                            .putHeaders("content-type", "application/json")
                                            .setRelativeUri(endpoint)
                                            .setHttpMethod(HttpMethod.POST)
                                            .build())
                            .build();

            // Send create task request.
            Task[] tasks = new Task[] {taskPayload};
            for (Task task : tasks) {
                Task response = client.createTask(queueName, task);
                log.info("task created, resp={}", response);
            }
        }
        return dto;
    }

    @Override
    public boolean terminateTask(String task) {
        return false;
    }
}
