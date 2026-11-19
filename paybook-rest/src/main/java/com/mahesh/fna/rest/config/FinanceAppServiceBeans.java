package com.mahesh.fna.rest.config;

import java.util.HashMap;
import java.util.Map;

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
import com.mahesh.fna.app.service.impl.SqlClientConfigurer;
import com.mahesh.fna.app.service.impl.TransactionDataServiceImpl;
import com.mahesh.fna.app.service.impl.UserDataServiceImpl;

@Configuration
public class FinanceAppServiceBeans {

	@Bean(name = "accountDataService")
	@Description("AccountDataService for Spring WebFlow")
	public AccountDataService accountDataServiceBean() {
		return (AccountDataService) fnaBeanMap().get(AccountDataService.class.getSimpleName());
	}
	
	@Bean(name = "loanDataService")
	@Description("LoanDataService for Spring WebFlow")
	public LoanDataService loanDataServiceBean() {
		return (LoanDataService) fnaBeanMap().get(LoanDataService.class.getSimpleName());
	}
	
	@Bean(name = "masterDataService")
	@Description("MasterDataService for Spring WebFlow")
	public MasterDataService masterDataServiceBean() {
		return (MasterDataService) fnaBeanMap().get(MasterDataService.class.getSimpleName());
	}
	
	@Bean(name = "salaryDataService")
	@Description("SalaryDataService for Spring WebFlow")
	public SalaryDataService salaryDataServiceBean() {
		return (SalaryDataService) fnaBeanMap().get(SalaryDataService.class.getSimpleName());
	}
	
	@Bean(name = "transactionDataService")
	@Description("TransactionDataService for Spring WebFlow")
	public TransactionDataService transactionDataServiceBean() {
		return (TransactionDataService) fnaBeanMap().get(TransactionDataService.class.getSimpleName());
	}
	
	@Bean(name = "userDataService")
	@Description("UserDataService for Spring WebFlow")
	public UserDataService userDataServiceBean() {
		return (UserDataService) fnaBeanMap().get(UserDataService.class.getSimpleName());
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
	
    public Map<String,SqlClientConfigurer> fnaBeanMap() {
        Map<String,SqlClientConfigurer> beanMap = new HashMap<String,SqlClientConfigurer>();
        beanMap.put(AccountDataService.class.getSimpleName(), new AccountDataServiceImpl());
        beanMap.put(LoanDataService.class.getSimpleName(), new LoanDataServiceImpl());
        beanMap.put(MasterDataService.class.getSimpleName(), new MasterDataServiceImpl());
        beanMap.put(SalaryDataService.class.getSimpleName(), new SalaryDataServiceImpl());
        beanMap.put(TransactionDataService.class.getSimpleName(), new TransactionDataServiceImpl());
        beanMap.put(UserDataService.class.getSimpleName(), new UserDataServiceImpl());
        return beanMap;
    }
}
