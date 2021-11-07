package net.ketone.accrptgen.task.gen.generate;

import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.task.gen.GenerationServiceApachePOI;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

@ExtendWith(MockitoExtension.class)
public class BannerServiceTest {

    @Mock
    private SettingsService configurationService;

    @Mock
    private StorageService persistentStorage;

    private BannerService bannerService;

    @BeforeEach
    public void init() {
        Mockito.when(configurationService.getSettings()).thenAnswer((invocationOnMock) -> {
            Properties properties = new Properties();
            properties.setProperty("auditor.0.name","auditor0");
            properties.setProperty("auditor.0.banner","banner0");
            properties.setProperty("auditor.1.name","auditor1");
            properties.setProperty("auditor.1.banner","banner1");
            return properties;
        });
        bannerService = new BannerService(configurationService, persistentStorage);
        bannerService.init();
    }

    @Test
    public void testLoadCredentialsMap() throws NoSuchFieldException, IllegalAccessException {
        Field field = BannerService.class.getDeclaredField("bannerMap");
        field.setAccessible(true);
        Map<String, String> credentialsMap = (Map<String, String>) field.get(bannerService);
        Assertions.assertThat(credentialsMap.get("auditor0")).isEqualTo("banner0");
        Assertions.assertThat(credentialsMap.get("auditor1")).isEqualTo("banner1");

    }

}
