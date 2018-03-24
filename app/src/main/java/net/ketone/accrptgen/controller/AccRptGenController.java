package net.ketone.accrptgen.controller;

import net.ketone.accrptgen.admin.StatisticsService;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.dto.DownloadFileDto;
import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.gen.GenerationService;
import net.ketone.accrptgen.gen.ParsingService;
import net.ketone.accrptgen.mail.EmailService;
import net.ketone.accrptgen.store.StorageService;
import net.ketone.accrptgen.util.PasswordUtils;
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

import java.io.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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


    @RequestMapping("/hello")
    public String greeting() {
        logger.info("hello!");
        return "Hello World!";
    }

    @PostMapping("/uploadFile")
    public AccountFileDto handleFileUpload(@RequestParam("file") MultipartFile file,
                                           RedirectAttributes redirectAttributes) throws IOException {

        // TODO: use ThreadPool and Future to keep track of file generation status
        final byte[] fileBytes = file.getBytes();
        InputStream is = new ByteArrayInputStream(fileBytes);
        final Date generationTime = new Date();
        String companyName = parsingService.extractCompanyName(is);
        String filename = companyName + "-" + GenerationService.sdf.format(generationTime) + ".docx";

        AccountFileDto dto = new AccountFileDto();
        dto.setCompany(companyName);
        dto.setFilename(filename);
        dto.setGenerationTime(new Date());

        new Thread( () -> {
            try {
                InputStream is1 = new ByteArrayInputStream(fileBytes);
                ByteArrayOutputStream os = parsingService.preParse(is1);
                is1.close();
                InputStream is2 = new ByteArrayInputStream(os.toByteArray());
                os.close();
                AccountData data = parsingService.readFile(is2);
                is2.close();
                data.setGenerationTime(generationTime);

                // TODO: fix locale problems, generation time does not match filename
                ByteArrayOutputStream os1 = generationService.generate(data);
                byte[] bytes = os1.toByteArray();
                os1.close();
                try {
                    storageService.store(new ByteArrayInputStream(bytes), filename);
                } catch (IOException e) {
                    logger.error("Error storing generated file", e);
                    throw new RuntimeException(e);
                }
                emailService.sendEmail(companyName, filename, new ByteArrayInputStream(bytes));
                dto.setStatus(AccountFileDto.Status.EMAIL_SENT.name());
                statisticsService.updateAccountReport(dto);
            } catch (Exception e) {
                dto.setStatus(AccountFileDto.Status.FAILED.name());
                try {
                    statisticsService.updateAccountReport(dto);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                logger.warn("Gneration Failed." , e);
            }
        }).start();

        dto.setStatus(AccountFileDto.Status.GENERATING.name());
        statisticsService.updateAccountReport(dto);

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
        // TODO: append files which are generating
        // TODO: sort by descending time
//        return storageService.list().stream()
//                .filter(f -> f.endsWith("docx"))
//                .map(
//                f -> {
//                    AccountFileDto dto = new AccountFileDto();
//                    dto.setCompany(f.substring(0, f.lastIndexOf("-")));
//                    try {
//                        dto.setGenerationTime(GenerationService.sdf.parse(f.substring(f.lastIndexOf("-")+1, f.lastIndexOf("."))));
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                    dto.setFilename(f);
//                    return dto;
//                }).collect(Collectors.toList());
        return statisticsService.getRecentGenerations();
    }


}
