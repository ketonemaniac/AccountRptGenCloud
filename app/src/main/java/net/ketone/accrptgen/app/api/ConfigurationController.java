package net.ketone.accrptgen.app.api;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.app.domain.dto.ConfigurationDto;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.util.ExcelTaskUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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

    private static final String ALLDOCS = "allDocs";
    private static final String AUDITPRG = "auditPrg";
    private static final String DBIZFUNDING = "dBizFunding";
    private static final String BREAKDOWNTABS = "breakdownTabs";
    private static final String BANNER = "banner";




    private String getFileTypePath(final String fileType) {
        switch(fileType) {
            case ALLDOCS:
                return StorageService.ALLDOCS_PATH;
            case AUDITPRG:
                return StorageService.AUDIT_PRG_PATH;
            case DBIZFUNDING:
                return StorageService.DBIZ_FUNDING_PATH;
            case BANNER:
                return StorageService.BANNER_PATH;
            case BREAKDOWNTABS:
                return StorageService.BREAKDOWN_TABS_PATH;
            default:
                return StringUtils.EMPTY;
        }
    }

    private String getConfigurationName(final String fileType) {
        switch(fileType) {
            case ALLDOCS:
                return SettingsService.PREPARSE_TEMPLATE_PROP;
            case AUDITPRG:
                return SettingsService.PREPARSE_AUIDTPRG_TEMPLATE_PROP;
            case DBIZFUNDING:
                return SettingsService.PREPARSE_DBIZ_FUNDING_TEMPLATE_PROP;
            case BREAKDOWNTABS:
                return SettingsService.PREPARSE_BREAKDOWN_TABS_TEMPLATE_PROP;
            default:
                return StringUtils.EMPTY;
        }
    }

    @PutMapping("/template/{fileType}")
    public AccountJob handleTemplateFileUpload(@PathVariable final String fileType,
                                               @RequestParam("file") MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        if(ALLDOCS.equalsIgnoreCase(fileType)) {
            Try.run(() -> ExcelTaskUtils.evaluateAll("templateFileUpload", ExcelTaskUtils.openExcelWorkbook(fileBytes)))
            .getOrElseThrow(ex -> {
                log.error("Error uploading template file", ex);
                return new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            });
        }
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
    public AccountJob setActiveTemplate(@PathVariable("fileType") final String fileType,
            @PathVariable("filename") final String filename) {
        configurationService.saveSetting(
                getConfigurationName(fileType), filename);
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
                .dBizFunding(properties.getProperty(SettingsService.PREPARSE_DBIZ_FUNDING_TEMPLATE_PROP))
                .breakdownTabs(properties.getProperty(SettingsService.PREPARSE_BREAKDOWN_TABS_TEMPLATE_PROP))
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