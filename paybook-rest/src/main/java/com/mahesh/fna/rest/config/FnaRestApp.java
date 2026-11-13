package com.mahesh.fna.rest.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = "com.mahesh.fna.rest")
public class FnaRestApp {

	public static void main(String[] args) throws Exception {
		// Old Code : SpringApplication.run(DmApplication.class, args);
		SpringApplicationBuilder builder = new SpringApplicationBuilder(FnaRestApp.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
	}
}
