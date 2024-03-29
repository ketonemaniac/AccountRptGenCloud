package net.ketone.accrptgen.app.api;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.app.exception.ValidationException;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.app.service.tasks.TaskSubmissionService;
import net.ketone.accrptgen.app.service.tasks.TasksService;
import net.ketone.accrptgen.app.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api/accrptgen")
@Slf4j
public class AccRptGenController {

    @Autowired
    private StorageService tempStorage;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private TasksService tasksService;
    @Autowired
    private TaskSubmissionService taskSubmissionService;

    @PostMapping("/file")
    public AccountJob handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException, ValidationException {
        if(file.getOriginalFilename().lastIndexOf(".") == -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file extension found");
        }
        return taskSubmissionService.triage(
                file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")),
                file.getBytes());
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(@RequestParam("file") String fileName) throws IOException {
        log.info("filename is " + fileName);
        InputStream is = tempStorage.loadAsInputStream(fileName);
        Resource resource = new InputStreamResource(is);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"").body(resource);
    }

    @GetMapping("/taskList")
    public List<AccountJob> listFiles() {
        return statisticsService.getRecentTasks(UserUtils.getAuthenticatedUser());
    }

    @GetMapping("/terminateTask/{id}")
    public int deleteTask(@PathVariable("id") String id) {
        return tasksService.terminateTask(id) ? 1 : 0;
    }


    @GetMapping("/purgeQueue")
    public boolean purgeQueue() {
        Queue q = QueueFactory.getQueue(Constants.GEN_QUEUE_NAME);
        q.purge();
        return true;
    }

}

