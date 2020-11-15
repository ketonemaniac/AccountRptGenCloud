package net.ketone.accrptgen.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.tasks.v2.*;
import com.google.protobuf.ByteString;
import net.ketone.accrptgen.domain.dto.AccountJob;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;

@Service
@Profile("gCloudStandard")
public class TaskQueueService implements TasksService {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public AccountJob submitTask(AccountJob dto) throws IOException {
        try (CloudTasksClient client = CloudTasksClient.create()) {
            // TODO(developer): Uncomment these lines and replace with your values.
             String projectId = "accountrptgen-hk-test";
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
                                            .setRelativeUri("/worker")
                                            .setHttpMethod(HttpMethod.POST)
                                            .build())
                            .build();

            // Send create task request.
            Task[] tasks = new Task[] {taskPayload};
            for (Task task : tasks) {
                Task response = client.createTask(queueName, task);
                System.out.println(response);
            }
        }
        return dto;
    }

    @Override
    public boolean terminateTask(String task) {
        return false;
    }
}
