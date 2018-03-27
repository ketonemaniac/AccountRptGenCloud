package net.ketone.accrptgen;

import org.apache.poi.util.POILogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccrptgenApplication {

	public static void main(String[] args) {
//		System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.SystemOutLogger" );
//		System.setProperty("poi.log.level", POILogger.DEBUG + "");
		SpringApplication.run(AccrptgenApplication.class, args);
	}
}
