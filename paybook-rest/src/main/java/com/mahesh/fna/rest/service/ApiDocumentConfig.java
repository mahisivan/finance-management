package com.mahesh.fna.rest.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class ApiDocumentConfig {

    @Value("${server.servlet.context-path}")
    private String contextPath;
	
    @Bean
    public OpenAPI openAPI() {
        OpenAPI openAPIVO = new OpenAPI();
        Info infoVO = new Info();
        infoVO.title("PayBook Rest Services API");
        infoVO.version("1.0");
        infoVO.description("Spring Boot 4.0.0 API Documentation");
        openAPIVO.setInfo(infoVO);
        openAPIVO.servers(Collections.singletonList(
                new Server().url(contextPath).description("PayBook Rest Services URL")
        ));
        return openAPIVO;
    }
}
