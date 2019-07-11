package net.ketone.accrptgen.controller;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import net.ketone.accrptgen.exception.ValidationException;
import net.ketone.accrptgen.stats.StatisticsService;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.dto.DownloadFileDto;
import net.ketone.accrptgen.gen.GenerationService;
import net.ketone.accrptgen.gen.ParsingService;
import net.ketone.accrptgen.mail.EmailService;
import net.ketone.accrptgen.store.StorageService;
import net.ketone.accrptgen.tasks.TasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class AccRptGenController {

    private static final Logger logger = Logger.getLogger(AccRptGenController.class.getName());

    @Autowired
    private GenerationService generationService;
    @Autowired
    private ParsingService parsingService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private TasksService tasksService;

    @Value("${build.version}")
    private String buildVersion;

    @Value("${build.timestamp}")
    private String buildTimestamp;

    @RequestMapping("/version")
    public Map<String, String> getVersion() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(buildTimestamp, df);
        String timestamp = df.format(localDateTime.toInstant(ZoneOffset.UTC).atOffset(ZoneOffset.of("+8")));
        Map<String, String> verMap = new HashMap<>();
        verMap.put("version", buildVersion);
        verMap.put("timestamp" , timestamp);
        return verMap;
    }

    @PostMapping("/uploadFile")
    public AccountFileDto handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException, ValidationException {
        if(file.getOriginalFilename().lastIndexOf(".") == -1) {
            throw new ValidationException("sadfa");
        }
        file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1, file.getOriginalFilename().length());
        final byte[] fileBytes = file.getBytes();
        final long generationTime = new Date().getTime();
        storageService.store(fileBytes, generationTime+".xlsm");
        return tasksService.submitTask(new Date(generationTime));
    }



    @PostMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestBody DownloadFileDto dto) throws IOException {
        String fileName = dto.getFilename();
        logger.info("filename is " + fileName);
        InputStream is = storageService.loadAsInputStream(fileName);
        Resource resource = new InputStreamResource(is);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"").body(resource);
    }

    @GetMapping("/listFiles")
    public List<AccountFileDto> listFiles() {
        return statisticsService.getRecentTasks();
    }

    @GetMapping("/terminateTask/{id}")
    public int deleteTask(@PathVariable("id") String id) {
        return tasksService.terminateTask(id) ? 1 : 0;
    }


    @GetMapping("/purgeQueue")
    public boolean purgeQueue() {
        Queue q = QueueFactory.getQueue("accountrptgen-queue");
        q.purge();
        return true;
    }


}

