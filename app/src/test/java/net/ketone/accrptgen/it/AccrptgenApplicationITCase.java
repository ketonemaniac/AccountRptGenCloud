package net.ketone.accrptgen.it;

import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.service.gen.Pipeline;
import net.ketone.accrptgen.service.mail.Attachment;
import net.ketone.accrptgen.service.mail.EmailService;
import net.ketone.accrptgen.service.store.FileStorageService;
import net.ketone.accrptgen.service.store.StorageService;
import org.apache.commons.io.FileUtils;
import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * This is a test file to run the meat without booting up the whole program.
 * You need at least TWO files in the files/ folder
 * 1. credentials.properties
 * 2. All documents.xlsm
 * which both contains sensitive information and is excluded
 * output is in target/test-output folder
 */
@IfProfileValue(name = "spring.profiles.active", values = {"itcase"})
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
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


	static private Date genTime = new Date();

	@Value("${plain.filename}")
	private String PLAIN_FILENAME;

	@MockBean
	private EmailService emailService;
	@Autowired
	private ApplicationContext ctx;


	@Test
	public void testPipeline() throws Exception {


		ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);

		AccountJob accountJob = new AccountJob();
		accountJob.setGenerationTime(genTime);
		accountJob.setFilename(PLAIN_FILENAME);
		Pipeline pipeline = ctx.getBean(Pipeline.class, accountJob);
		pipeline.run();
		Mockito.verify(emailService).sendEmail(any(), argumentCaptor.capture());
		List<Attachment> attachments = argumentCaptor.getValue();
		assertThat(attachments).hasSize(3);

		for(Attachment attachment : attachments) {
			FileUtils.writeByteArrayToFile(new File("target/test-output/" + attachment.getAttachmentName()), attachment.getData());
		}

	}

}
