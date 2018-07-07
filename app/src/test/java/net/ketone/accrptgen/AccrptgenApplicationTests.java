package net.ketone.accrptgen;

import com.google.api.client.util.IOUtils;
import net.ketone.accrptgen.gen.Pipeline;
import net.ketone.accrptgen.mail.Attachment;
import net.ketone.accrptgen.mail.EmailService;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;

@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public class AccrptgenApplicationTests {

	private final String PLAIN_FILENAME = "program (plain) 09.4.18.xlsm";
	private final String COMPANY_NAME = "TestComp";

	@MockBean
	private EmailService emailService;
	@Autowired
	private ApplicationContext ctx;

	@Test
	public void testPipeline() throws Exception {

		InputStream inputStream = this.getClass().getResourceAsStream("/" + PLAIN_FILENAME);
		FileOutputStream f = new FileOutputStream("files/" + COMPANY_NAME + ".xlsm");
		IOUtils.copy(inputStream,f);

		ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);

		Pipeline pipeline = ctx.getBean(Pipeline.class, COMPANY_NAME, new Date(), COMPANY_NAME);
		pipeline.run();
		Mockito.verify(emailService).sendEmail(any(), argumentCaptor.capture());
		List<Attachment> attachments = argumentCaptor.getValue();
		assertThat(attachments).hasSize(3);

		for(Attachment attachment : attachments) {
			FileUtils.writeByteArrayToFile(new File("files/" + attachment.getAttachmentName()), attachment.getData());
		}

	}

}
