package net.ketone.accrptgen.controller;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import net.ketone.accrptgen.admin.StatisticsService;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.dto.DownloadFileDto;
import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.gen.GenerationService;
import net.ketone.accrptgen.gen.ParsingService;
import net.ketone.accrptgen.mail.Attachment;
import net.ketone.accrptgen.mail.EmailService;
import net.ketone.accrptgen.store.StorageService;
import net.ketone.accrptgen.threading.ThreadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
public class AccRptGenController {

    private static final Logger logger = LoggerFactory.getLogger(AccRptGenController.class);

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
    private ThreadingService threadingService;


    @RequestMapping("/hello")
    public String greeting() {
        logger.info("hello!");
        return "Hello World!";
    }

    @PostMapping("/uploadFile")
    public AccountFileDto handleFileUploadTest(@RequestParam("file") MultipartFile file,
                                           RedirectAttributes redirectAttributes) throws IOException {
        final byte[] fileBytes = file.getBytes();
        InputStream is = new ByteArrayInputStream(fileBytes);
        final Date generationTime = new Date();
        String companyName = parsingService.extractCompanyName(is);
        String filename = companyName + "-" + GenerationService.sdf.format(generationTime);

        storageService.store(new ByteArrayInputStream(fileBytes), filename + ".xlsm");
        AccountFileDto dto = new AccountFileDto();
        dto.setCompany(companyName);
        dto.setFilename(filename + ".docx");
        dto.setGenerationTime(new Date());
        dto.setStatus(AccountFileDto.Status.GENERATING.name());
        statisticsService.updateAccountReport(dto);

        threadingService.runPipeline(companyName, generationTime, filename);

        return dto;
    }



    @PostMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestBody DownloadFileDto dto) throws IOException {
        logger.info("filename is " + dto.getFilename());
        InputStream is = storageService.load(dto.getFilename());
        Resource resource = new InputStreamResource(is);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + dto.getFilename() + "\"").body(resource);
    }

    @GetMapping("/listFiles")
    public List<AccountFileDto> listFiles() {
        return statisticsService.getRecentGenerations();
    }


}

