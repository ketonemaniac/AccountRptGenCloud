package net.ketone.accrptgen.controller;

import net.ketone.accrptgen.admin.CredentialsService;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

//    private static final Logger logger = LoggerFactory.getLogger(AccRptGenController.class);
    private static final Logger logger = Logger.getLogger(AdminController.class.getName());

    @Autowired
    @Qualifier("persistentStorage")
    private StorageService storageService;
    @Autowired
    private CredentialsService credentialsService;

    @RequestMapping("/hello")
    public String greeting() {
        logger.info("hello from Admin!");
        return "Hello World from Admin!";
    }

    /**
     * upload
     *
     * @param file
     * @param redirectAttributes
     * @return
     * @throws IOException
     */
    @PostMapping("/uploadFile")
    public AccountFileDto handleTemplateFileUpload(@RequestParam("file") MultipartFile file,
                                           RedirectAttributes redirectAttributes) throws IOException {

        logger.info("uploading template file=" + file.getOriginalFilename());
        storageService.store(file.getBytes(), file.getOriginalFilename());
        credentialsService.saveCredential(CredentialsService.PREPARSE_TEMPLATE_PROP, file.getOriginalFilename());
        AccountFileDto dto = new AccountFileDto();
        dto.setFilename(file.getOriginalFilename());
        return dto;
    }


    @PostMapping("/saveParam")
    public Map<String, String> saveParameters(@RequestBody Map<String, String> params) {
        credentialsService.saveCredentials(params);
        return params;
    }

    @GetMapping("getParam")
    public Map<String, String> getParameters() {
        Properties properties = credentialsService.getCredentials();
        Map<String, String> params = properties.stringPropertyNames().stream()
                .filter(s -> !s.equalsIgnoreCase(CredentialsService.SENDGRID_API_KEY_PROP))
                .collect(Collectors.toMap(
                Function.identity(), s -> properties.getProperty(s)
        ));
        return params;
    }

}