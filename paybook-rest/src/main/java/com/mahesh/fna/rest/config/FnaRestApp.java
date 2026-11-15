package com.mahesh.fna.rest.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.mahesh.fna.rest")
@EnableJpaRepositories(basePackages = "com.mahesh.fna.rest.dao")
@EntityScan(basePackages = "com.mahesh.fna.rest.dao.entity")
@ComponentScan(basePackages = "com.mahesh.fna.rest")
public class FnaRestApp {

	public static void main(String[] args) throws Exception {
		// Old Code : SpringApplication.run(DmApplication.class, args);
		SpringApplicationBuilder builder = new SpringApplicationBuilder(FnaRestApp.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
	}
}
