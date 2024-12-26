package net.ketone.accrptgen.it;

import net.ketone.accrptgen.app.AccrptgenApplication;
import net.ketone.accrptgen.common.mail.EmailService;
import net.ketone.accrptgen.common.store.FileStorageService;
import net.ketone.accrptgen.common.store.StorageService;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * This is a test file to run the meat without booting up the whole program.
 * You need at least TWO files in the files/ folder
 * 1. credentials.properties
 * 2. All documents.xlsm
 * which both contains sensitive information and is excluded
 * output is in target/test-output folder
 */
@IfProfileValue(name = "spring.profiles.active", values = {"itcase"})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("local")
@SpringBootTest(classes = AccrptgenApplication.class)
public class AccrptgenApplicationITCase {

	@TestConfiguration
	static class AppConfig {

		@Value("${plain.filename}${plain.filename.extension}")
		private String PLAIN_FILENAME;

		@Bean
		@Primary
		public StorageService storageService() throws IOException {
			StorageService storageService = Mockito.mock(FileStorageService.class);

			InputStream inputStream = this.getClass().getResourceAsStream("/" + PLAIN_FILENAME);

			Mockito.when(storageService.load(eq(PLAIN_FILENAME))).thenReturn(IOUtils.toByteArray(inputStream));
			Mockito.when(storageService.loadAsInputStream(any(String.class))).thenCallRealMethod();

			return storageService;
		}

	}


	static private LocalDateTime genTime = LocalDateTime.now();

	@Value("${plain.filename}")
	private String PLAIN_FILENAME;

	@MockBean
	private EmailService emailService;
	@Autowired
	private ApplicationContext ctx;


	@Test
	public void testPipeline() throws Exception {


//		ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
//
//		AccountJob accountJob = AccountJob.builder()
//				.generationTime(genTime)
//				.filename(PLAIN_FILENAME)
//				.build();
//		AccountRptTask accountRptTask = ctx.getBean(AccountRptTask.class, accountJob);
//		accountRptTask.run();
//		Mockito.verify(emailService).sendEmail(any(), argumentCaptor.capture(), any());
//		List<Attachment> attachments = argumentCaptor.getValue();
//		assertThat(attachments).hasSize(3);
//
//		for(Attachment attachment : attachments) {
//			FileUtils.writeByteArrayToFile(new File("target/test-output/" + attachment.getAttachmentName()), attachment.getData());
//		}

	}

}
