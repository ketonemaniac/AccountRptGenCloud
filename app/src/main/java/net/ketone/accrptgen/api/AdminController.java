package net.ketone.accrptgen.api;

import net.ketone.accrptgen.service.credentials.CredentialsService;
import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.service.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private StorageService persistentStorage;
    @Autowired
    private CredentialsService credentialsService;

    /**
     * upload
     *
     * @param file
     * @param redirectAttributes
     * @return
     * @throws IOException
     */
    @PostMapping("/uploadFile")
    public AccountJob handleTemplateFileUpload(@RequestParam("file") MultipartFile file,
                                               RedirectAttributes redirectAttributes) throws IOException {

        logger.info("uploading template file=" + file.getOriginalFilename());
        persistentStorage.store(file.getBytes(), file.getOriginalFilename());
        credentialsService.saveCredential(CredentialsService.PREPARSE_TEMPLATE_PROP, file.getOriginalFilename());
        AccountJob dto = new AccountJob();
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