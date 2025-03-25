package net.ketone.accrptgen.app.api;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.app.service.ClientRandIntChecker;
import net.ketone.accrptgen.task.service.tasks.TaskSubmissionService;
import net.ketone.accrptgen.task.service.tasks.TasksService;
import net.ketone.accrptgen.app.util.UserUtils;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.app.model.auth.AuthenticatedUser;
import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.common.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accrptgen")
@Slf4j
public class AccRptGenController {

    @Autowired
    private StorageService tempStorage;
    @Autowired
    private StatisticsService statisticsService;

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


    @GetMapping("/taskList/{docType}")
    public List<AccountJob> listFiles(@PathVariable("docType") String docType) {
        return statisticsService.getRecentTasks(UserUtils.getAuthenticatedUser(), docType);
    }

}

