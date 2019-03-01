package net.ketone.accrptgen;

import org.apache.poi.util.POILogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class AccrptgenApplication {

	public static void main(String[] args) {
//		System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.SystemOutLogger" );
//		System.setProperty("poi.log.level", POILogger.DEBUG + "");
		SpringApplication.run(AccrptgenApplication.class, args);
	}

	@Configuration
	@ComponentScan(lazyInit = true)
	static class LocalConfig {
	}
}
