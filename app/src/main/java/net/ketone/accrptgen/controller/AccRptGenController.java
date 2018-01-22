package net.ketone.accrptgen.controller;

import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.dto.DownloadFileDto;
import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.gen.GenerationService;
import net.ketone.accrptgen.gen.ParsingService;
import net.ketone.accrptgen.store.StorageService;
import net.ketone.accrptgen.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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


    @RequestMapping("/hello")
    public String greeting() {
        logger.info("hello!");
        return "Hello World!";
    }

    @PostMapping("/uploadFile")
    public AccountFileDto handleFileUpload(@RequestParam("file") MultipartFile file,
                                           RedirectAttributes redirectAttributes) throws IOException {


        InputStream is = new ByteArrayInputStream(file.getBytes());
        AccountData data = parsingService.readFile(is);

        data.setGenerationTime(new Date());

        String filename = generationService.generate(data);

        AccountFileDto dto = new AccountFileDto();
        dto.setCompany(data.getCompanyName());
        dto.setFilename(filename);
        dto.setPassword(PasswordUtils.generatePassword(8));
        dto.setGenerationTime(data.getGenerationTime());
        return dto;
    }

    @PostMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestBody DownloadFileDto dto) {
        logger.info("filename is " + dto.getFilename());
        Resource file = storageService.loadAsResource(dto.getFilename());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/listFiles")
    public List<AccountFileDto> listFiles() {
        return storageService.list().stream()
                .filter(f -> f.endsWith("docx"))
                .map(
                f -> {
                    AccountFileDto dto = new AccountFileDto();
                    dto.setCompany(f.substring(0, f.lastIndexOf("-")));
                    try {
                        dto.setGenerationTime(GenerationService.sdf.parse(f.substring(f.lastIndexOf("-")+1, f.lastIndexOf("."))));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    dto.setFilename(f);
                    return dto;
                }).collect(Collectors.toList());
    }


}
