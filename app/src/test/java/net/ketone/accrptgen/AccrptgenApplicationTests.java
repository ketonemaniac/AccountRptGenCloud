package net.ketone.accrptgen;

import net.ketone.accrptgen.gen.Pipeline;
import net.ketone.accrptgen.mail.Attachment;
import net.ketone.accrptgen.mail.EmailService;
import net.ketone.accrptgen.store.FileStorageService;
import net.ketone.accrptgen.store.StorageService;
import org.apache.commons.io.FileUtils;
import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
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
 */
// @Ignore
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public class AccrptgenApplicationTests {

	static private final String PLAIN_FILENAME = "program (plain) 09.4.18.xlsm";
//	private final String COMPANY_NAME = "TestComp";
	static private Date genTime = new Date();

	@MockBean
	private EmailService emailService;
	@Autowired
	private ApplicationContext ctx;


	@TestConfiguration
	static class AppConfig {

		@Bean
		public StorageService storageService() throws IOException {
			StorageService storageService = Mockito.mock(FileStorageService.class);

			InputStream inputStream = this.getClass().getResourceAsStream("/" + PLAIN_FILENAME);

			Mockito.when(storageService.load(eq(genTime.getTime()+".xlsm"))).thenReturn(IOUtils.toByteArray(inputStream));
			Mockito.when(storageService.loadAsInputStream(any(String.class))).thenCallRealMethod();

			return storageService;
		}

	}

	@Test
	public void testPipeline() throws Exception {


		ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);

		Pipeline pipeline = ctx.getBean(Pipeline.class, genTime);
		pipeline.run();
		Mockito.verify(emailService).sendEmail(any(), argumentCaptor.capture());
		List<Attachment> attachments = argumentCaptor.getValue();
		assertThat(attachments).hasSize(3);

		for(Attachment attachment : attachments) {
			FileUtils.writeByteArrayToFile(new File("files/" + attachment.getAttachmentName()), attachment.getData());
		}

	}

}
