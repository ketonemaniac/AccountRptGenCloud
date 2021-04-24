package net.ketone.accrptgen.api;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.repackaged.org.joda.time.DateTime;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.config.Constants;
import net.ketone.accrptgen.exception.ValidationException;
import net.ketone.accrptgen.service.stats.StatisticsService;
import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.domain.dto.DownloadFileDto;
import net.ketone.accrptgen.service.gen.GenerationService;
import net.ketone.accrptgen.service.gen.ParsingService;
import net.ketone.accrptgen.service.mail.EmailService;
import net.ketone.accrptgen.service.store.StorageService;
import net.ketone.accrptgen.service.tasks.TaskQueueService;
import net.ketone.accrptgen.service.tasks.TasksService;
import net.ketone.accrptgen.util.UserUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/accrptgen")
@Slf4j
public class AccRptGenController {

    @Autowired
    private GenerationService generationService;
    @Autowired
    private ParsingService parsingService;
    @Autowired
    private StorageService tempStorage;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private TasksService tasksService;

    @PostMapping("/file")
    public AccountJob handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException, ValidationException {
        if(file.getOriginalFilename().lastIndexOf(".") == -1) {
            throw new ValidationException("sadfa");
        }
        file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1, file.getOriginalFilename().length());
        final byte[] fileBytes = file.getBytes();
        final Date generationTime = new Date();
        tempStorage.store(fileBytes, generationTime.getTime()+".xlsm");

        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes));
        AccountJob dto = AccountJob.builder()
                .id(UUID.randomUUID())
                .company(parsingService.extractCompanyName(workbook))
                .period(parsingService.extractPeriodEnding(workbook))
                .filename(String.valueOf(generationTime.getTime()))
                .status(Constants.Status.PRELOADED.name())
                .submittedBy(UserUtils.getAuthenticatedUser())
                .build();
        statisticsService.updateTask(dto);
        return dto;
    }

    @PostMapping("/startGeneration")
    public AccountJob startGeneration(final AccountJob requestDto) throws IOException {
        log.info("cacheFilename=" + requestDto.getFilename() + "; referredBy=" + requestDto.getReferredBy());
        AccountJob dto = requestDto.toBuilder()
                .status(Constants.Status.PENDING.name())
                .submittedBy(UserUtils.getAuthenticatedUser())
                .generationTime(LocalDateTime.now())
                .build();
        try {
            String inputFileName = dto.getFilename() + ".xlsm";
            if(!tempStorage.hasFile(inputFileName)) {
                log.warn("File not present: " + inputFileName);
                dto.setStatus(Constants.Status.FAILED.name());
                return dto;
            }
            tasksService.submitTask(dto);
            statisticsService.updateTask(dto);
        } catch (Exception e) {
            log.error("Error in startGeneration", e);
            dto.setStatus(Constants.Status.FAILED.name());
            statisticsService.updateTask(dto);
        }
        return dto;

    }


    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(@RequestParam("file") String file) throws IOException {
        String fileName = file + ".zip";
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

