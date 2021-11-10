package net.ketone.accrptgen.task.gen.generate;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.store.StorageService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BannerService {

    private final SettingsService configurationService;

    private final StorageService persistentStorage;

    private Map<String, String> bannerMap;

    @PostConstruct
    public void init() {
        bannerMap = getCredentialsMap("auditor.");
    }

    private Map<String, String> getCredentialsMap(final String prefix) {
        return configurationService.getSettings().entrySet().stream()
                .filter(entry -> entry.getKey().toString().startsWith(prefix))
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(entry -> entry.getKey().toString().split("\\.")[1]),
                        map -> map.entrySet().stream()
                                .collect(Collectors.toMap(entry -> entry.getValue()
                                                .stream()
                                                .filter(e -> e.getKey().toString().contains(".name"))
                                                .findFirst()
                                                .get().getValue().toString(),
                                        entry -> entry.getValue()
                                                .stream()
                                                .filter(e -> e.getKey().toString().contains(".banner"))
                                                .findFirst()
                                                .get().getValue().toString())
                                ))
                );
    }

    /**
     * Gets image from the FIRST word of the auditorName
     * @param auditorName
     * @return
     */
    public InputStream getImage(final String auditorName) {
        String banner = Optional.ofNullable(auditorName)
                .map(s -> s.split(" "))
                .map(arr -> arr[0])
                .map(bannerMap::get)
                .orElseThrow(() -> new RuntimeException("Unable to find banner from Auditor name" +
                        auditorName));
        return Try.of(() -> persistentStorage.loadAsInputStream(
                StorageService.BANNER_PATH + banner))
                .getOrElseThrow(e -> new RuntimeException("Unable to load Image", e));
    }

}
