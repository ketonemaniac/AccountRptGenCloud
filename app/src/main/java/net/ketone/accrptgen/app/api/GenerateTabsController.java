package net.ketone.accrptgen.app.api;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.app.service.ClientRandIntChecker;
import net.ketone.accrptgen.app.util.UserUtils;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.task.GenerateTabTask;
import net.ketone.accrptgen.task.service.tasks.TaskSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/v2/accrptgen")
@Slf4j
public class GenerateTabsController {

    @Autowired
    private TaskSubmissionService taskSubmissionService;
    @Autowired
    private ClientRandIntChecker clientRandIntChecker;
    @Autowired
    private StorageService tempStorage;


    @PostMapping(path = "/file", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void generateTabs(@RequestParam("file") MultipartFile file,
                                                              @RequestParam("seed") Integer clientRandInt,
                                                              Principal principal) throws Exception {

        log.info("USER IS {}, CLIENT RAND INT IS {}", UserUtils.getAuthenticatedUser(), clientRandInt);
        if(clientRandIntChecker.checkDuplicate(clientRandInt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate Request " + clientRandInt);
        };
        if(file.getOriginalFilename().lastIndexOf(".") == -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file extension found");
        }
        taskSubmissionService.submitTabGeneration(
                file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")),
                file.getBytes(), UserUtils.getUserFromPrincipal(principal),
                clientRandInt);
    }


}
