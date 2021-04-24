package net.ketone.accrptgen.api;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.domain.dto.ConfigurationDto;
import net.ketone.accrptgen.service.credentials.SettingsService;
import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.service.store.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/settings")
@Slf4j
public class ConfigurationController {

    @Autowired
    private StorageService persistentStorage;
    @Autowired
    private SettingsService configurationService;

    private String getFileTypePath(final String fileType) {
        switch(fileType) {
            case "allDocs":
                return StorageService.ALLDOCS_PATH;
            case "auditPrg":
                return StorageService.AUDIT_PRG_PATH;
            case "banner":
                return StorageService.BANNER_PATH;
            default:
                return StringUtils.EMPTY;
        }
    }

    @PutMapping("/template/{fileType}")
    public AccountJob handleTemplateFileUpload(@PathVariable final String fileType,
                                               @RequestParam("file") MultipartFile file) throws IOException {
        persistentStorage.store(file.getBytes(),
                getFileTypePath(fileType) + file.getOriginalFilename());
        return setActiveTemplate(fileType, file.getOriginalFilename());
    }

    @DeleteMapping("/template/{fileType}/{filename}")
    public String deleteTemplate(@PathVariable final String fileType,
            @PathVariable("filename") final String filename) {
        persistentStorage.delete(getFileTypePath(fileType) + filename);
        return filename;
    }

    @PostMapping("template/{fileType}/active/{filename}")
    public AccountJob setActiveTemplate(@PathVariable final String fileType,
            @PathVariable("filename") final String filename) {
        configurationService.saveSetting(
                fileType.equals("allDocs") ? SettingsService.PREPARSE_TEMPLATE_PROP :
                        SettingsService.PREPARSE_AUIDTPRG_TEMPLATE_PROP, filename);
        return AccountJob.builder()
                .filename(filename)
                .build();
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
        configurationService.saveSettings(params);
        return params;
    }

    @GetMapping("all")
    public ConfigurationDto getParameters() {
        Properties properties = configurationService.getSettings();
        return ConfigurationDto.builder()
                .allDocs(properties.getProperty(SettingsService.PREPARSE_TEMPLATE_PROP))
                .auditPrg(properties.getProperty(SettingsService.PREPARSE_AUIDTPRG_TEMPLATE_PROP))
                .sendTo(Arrays.asList(properties.getProperty(SettingsService.SEND_TO_PROP).split(";")))
                .auditors(properties.entrySet().stream()
                        .filter(entry -> entry.getKey().toString().startsWith("auditor."))
                        .collect(Collectors.collectingAndThen(
                                    Collectors.groupingBy(entry -> entry.getKey().toString().split("\\.")[1]),
                                map -> map.entrySet().stream()
                                        .map(entry -> ConfigurationDto.AuditorDto.builder()
                                                .name(entry.getValue()
                                                        .stream()
                                                        .filter(e -> e.getKey().toString().contains(".name"))
                                                        .findFirst()
                                                        .get().getValue().toString())
                                                .banner(entry.getValue()
                                                        .stream()
                                                        .filter(e -> e.getKey().toString().contains(".banner"))
                                                        .findFirst()
                                                        .get().getValue().toString())
                                                .build())
                                .collect(Collectors.toList())
                        ))
                ).build();
    }

    /**
     * List of files, grouped by folder.
     * Those in root folder goes to "root"
     * @return
     */
    @GetMapping("fileList")
    public Map<String, List<String>> getFileList() {
        return persistentStorage.list()
                .stream()
                .map(s -> s.split(File.separator))
                .collect(Collectors.groupingBy(s -> s.length > 1 ? s[0] : "root",
                        Collectors.mapping(arr -> arr[arr.length-1], Collectors.toList())))
                ;
    }


}