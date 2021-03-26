package net.ketone.accrptgen.api;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.domain.dto.DownloadFileDto;
import net.ketone.accrptgen.service.credentials.CredentialsService;
import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.service.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/settings")
@Slf4j
public class SettingsController {

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
    @PutMapping("/template")
    public AccountJob handleTemplateFileUpload(@RequestParam("file") MultipartFile file,
                                               RedirectAttributes redirectAttributes) throws IOException {

        log.info("uploading template file=" + file.getOriginalFilename());
        persistentStorage.store(file.getBytes(), file.getOriginalFilename());
        credentialsService.saveCredential(CredentialsService.PREPARSE_TEMPLATE_PROP, file.getOriginalFilename());
        AccountJob dto = new AccountJob();
        dto.setFilename(file.getOriginalFilename());
        return dto;
    }

    @GetMapping("file")
    public ResponseEntity<Resource> getFile(@RequestParam("file") String filename) throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(Optional.ofNullable(filename)
                        .map(persistentStorage::loadAsInputStream)
                        .map(InputStreamResource::new)
                        .orElseThrow()
                );
    }


    @PostMapping("/upsert")
    public Map<String, String> saveParameters(@RequestBody Map<String, String> params) {
        credentialsService.saveCredentials(params);
        return params;
    }

    @GetMapping("all")
    public Map<String, String> getParameters() {
        Properties properties = credentialsService.getCredentials();
        Map<String, String> params = properties.stringPropertyNames().stream()
                .filter(s -> !s.equalsIgnoreCase(CredentialsService.SENDGRID_API_KEY_PROP))
                .filter(s -> !s.equalsIgnoreCase(CredentialsService.MONGODB_PASS))
                .collect(Collectors.toMap(
                Function.identity(), s -> properties.getProperty(s)
        ));
        return params;
    }

}