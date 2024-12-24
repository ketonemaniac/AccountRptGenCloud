package net.ketone.accrptgen.task.api;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.task.service.tasks.TaskSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/v2/accrptgen")
@Slf4j
public class ReportController {

    @Autowired
    private TaskSubmissionService taskSubmissionService;

    @PostMapping(path = "/file", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AccountJob>> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                              @RequestParam("seed") Integer clientRandInt,
                                                              Principal principal) throws IOException {

        User user = User.builder().username("yo").build();
        if(file.getOriginalFilename().lastIndexOf(".") == -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file extension found");
        }
        return taskSubmissionService.triage(
                file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")),
                file.getBytes(),
                Optional.of(user), clientRandInt);
    }


}
