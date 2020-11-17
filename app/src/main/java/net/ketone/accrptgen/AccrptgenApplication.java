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
		SpringApplication.run(AccrptgenApplication.class, args);
	}

}
