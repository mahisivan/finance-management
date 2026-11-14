package com.mahesh.fna.rest.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import com.mahesh.fna.app.service.AccountDataService;
import com.mahesh.fna.app.service.ChartDataService;
import com.mahesh.fna.app.service.CreditCardService;
import com.mahesh.fna.app.service.LoanDataService;
import com.mahesh.fna.app.service.MasterDataService;
import com.mahesh.fna.app.service.SalaryDataService;
import com.mahesh.fna.app.service.TransactionDataService;
import com.mahesh.fna.app.service.UserDataService;
import com.mahesh.fna.app.service.impl.AccountDataServiceImpl;
import com.mahesh.fna.app.service.impl.ChartDataServiceImpl;
import com.mahesh.fna.app.service.impl.CreditCardServiceImpl;
import com.mahesh.fna.app.service.impl.LoanDataServiceImpl;
import com.mahesh.fna.app.service.impl.MasterDataServiceImpl;
import com.mahesh.fna.app.service.impl.SalaryDataServiceImpl;
import com.mahesh.fna.app.service.impl.TransactionDataServiceImpl;
import com.mahesh.fna.app.service.impl.UserDataServiceImpl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class FinanceAppServiceBeans {

    @Value("${server.servlet.context-path}")
    private String contextPath;
	
	@Bean(name = "accountDataService")
	@Description("AccountDataService for Spring WebFlow")
	public AccountDataService accountDataServiceBean() {
		return new AccountDataServiceImpl();
	}
	
	@Bean(name = "loanDataService")
	@Description("LoanDataService for Spring WebFlow")
	public LoanDataService loanDataServiceBean() {
		return new LoanDataServiceImpl();		
	}
	
	@Bean(name = "masterDataService")
	@Description("MasterDataService for Spring WebFlow")
	public MasterDataService masterDataServiceBean() {
		return new MasterDataServiceImpl();		
	}
	
	@Bean(name = "salaryDataService")
	@Description("SalaryDataService for Spring WebFlow")
	public SalaryDataService salaryDataServiceBean() {
		return new SalaryDataServiceImpl();
	}
	
	@Bean(name = "transactionDataService")
	@Description("TransactionDataService for Spring WebFlow")
	public TransactionDataService transactionDataServiceBean() {
		return new TransactionDataServiceImpl();
	}
	
	@Bean(name = "userDataService")
	@Description("UserDataService for Spring WebFlow")
	public UserDataService userDataServiceBean() {
		return new UserDataServiceImpl();
	}
	
	@Bean(name = "chartDataService")
	@Description("ChartDataService for Spring WebFlow")
	public ChartDataService chartDataServiceBean() {
		return new ChartDataServiceImpl();
	}
	
	@Bean(name = "creditCardService")
	@Description("CreditCardService for Spring WebFlow")
	public CreditCardService creditCardServiceBean() {
		return new CreditCardServiceImpl();
	}

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
