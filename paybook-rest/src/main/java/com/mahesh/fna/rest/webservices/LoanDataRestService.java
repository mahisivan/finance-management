package com.mahesh.fna.rest.webservices;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mahesh.cf.customexeception.AppBaseException;
import com.mahesh.cf.customexeception.AppBaseExceptionLogger;
import com.mahesh.cf.validations.StringValidator;
import com.mahesh.fna.app.constants.DmConstants;
import com.mahesh.fna.app.service.LoanDataService;
import com.mahesh.fna.app.vo.AccountDataVO;
import com.mahesh.fna.app.vo.LoanDataVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController(value="loanDataRestService")
@RequestMapping(value="/api/LoanDataRestApi")
@Tag(name="LoanDataRest-API", description = "Loan related operations")
public class LoanDataRestService {

    @Autowired
    private LoanDataService loanDataService;

    private static final StringValidator validator = StringValidator.getInstance();
	
	@GetMapping(value = "/loan/{paymentId}")
    @Operation(description = "Service to fetch Loan Record based on Account")
    @Parameter(name = "paymentId", required = true)
	public LoanDataVO fetchLoanRecord(@PathVariable(value = "paymentId", required = true) String paymentId) {
		LoanDataVO loanDataVO = new LoanDataVO();
		loanDataVO.setPaymentId(0);
		if (validator.isNumeric(paymentId)) {
			loanDataVO.setPaymentId(Integer.parseInt(paymentId));
			loanDataVO.setAction(DmConstants.ACTION_VIEW);
		}
		loanDataVO = loanDataService.fetchRecord(loanDataVO);
		return loanDataVO;
	}

	@GetMapping(value = "/loan/{accountId}")
	@Operation(description = "Service to fetch Loan Records based on Account")
	public List<LoanDataVO> fetchLoanRecordByAccount(@PathVariable(value = "accountId", required = true) String accountId) {
		AccountDataVO accountDataVO = new AccountDataVO();
		accountDataVO.setAccountId(0);
		if (validator.isNumeric(accountId)) {
			accountDataVO.setAccountId(Integer.parseInt(accountId));
		}
		accountDataVO.setAction(DmConstants.ACTION_VIEW);
		LoanDataVO loanDataVO = loanDataService.fetchAllRecords(accountDataVO);
		return (List<LoanDataVO>) loanDataVO.getAttributesMap().get(DmConstants.LOAN_KEY);
	}

	@GetMapping(value = "/loan/{accountId}/balance")
	@Operation(description = "Service to fetch balance Amount in Loan Records based on Account")
	public LoanDataVO fetchLoanRecordBalance(@PathVariable(value = "accountId", required = true) String accountId) {
		AccountDataVO accountDataVO = new AccountDataVO();
		accountDataVO.setAccountId(0);
		if (validator.isNumeric(accountId)) {
			accountDataVO.setAccountId(Integer.parseInt(accountId));
		}
		accountDataVO.setAction(DmConstants.ACTION_VIEW);
		BigInteger balanceAmt = loanDataService.fetchBalanceAmount(accountDataVO);
		LoanDataVO loanDataVO = new LoanDataVO();
		loanDataVO.setAccountId(accountDataVO.getAccountId());
		loanDataVO.setPrincipal(balanceAmt);
		loanDataVO.setEmi(new BigInteger(DmConstants.ZERO_TEXT));
		loanDataVO.setInterest(new BigInteger(DmConstants.ZERO_TEXT));
		return loanDataVO;
	}

	@PostMapping(value = "/loan")
	@Operation(description = "Service to save Loan Records based on Account Number")
	public LoanDataVO saveLoanRecord(LoanDataVO loanDataVO) {
		try {
			loanDataVO.setAction(DmConstants.ACTION_ADD);
			loanDataVO = loanDataService.saveRecord(loanDataVO);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
		}
		return loanDataVO;
	}
	
	@PutMapping(value = "/loan")
	@Operation(description = "Service to modify Loan Records based on Account Number")
	public LoanDataVO modifyLoanRecord(LoanDataVO loanDataVO) {
		try {
			loanDataVO.setAction(DmConstants.ACTION_MODIFY);
			loanDataVO = loanDataService.modifyRecord(loanDataVO);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
		}
		return loanDataVO;
	}
	
	@DeleteMapping(value = "/loan/{paymentId}")
	@Operation(description = "Service to delete Loan Record")
	public ResponseEntity<String> recordLoanRecord(@PathVariable(value = "paymentId",required = true) String paymentId) {
		try {
			LoanDataVO loanDataVO = new LoanDataVO();
			loanDataVO.setPaymentId(0);
            if(validator.isNumeric(paymentId)) {
            	loanDataVO.setPaymentId(Integer.parseInt(paymentId));
            	loanDataVO.setAction(DmConstants.ACTION_DELETE);
            }
			loanDataVO = loanDataService.removeRecord(loanDataVO);
			return ResponseEntity.ok(AccountDataRestService.DELETE_MSG);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
			return ResponseEntity.internalServerError().body(ex.getMessage());
		}
	}
}
