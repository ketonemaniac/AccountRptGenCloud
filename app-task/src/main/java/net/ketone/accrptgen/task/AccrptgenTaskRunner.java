package net.ketone.accrptgen.task;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;

@RequiredArgsConstructor
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class AccrptgenTaskRunner {

	private final ApplicationContext ctx;

	public static void main(String[] args) {
		SpringApplication.run(AccrptgenTaskRunner.class, args);
	}

}
