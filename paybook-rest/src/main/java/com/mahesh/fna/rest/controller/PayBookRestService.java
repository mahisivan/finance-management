package com.mahesh.fna.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mahesh.cf.customexeception.AppBaseException;
import com.mahesh.cf.customexeception.AppBaseExceptionLogger;
import com.mahesh.fna.app.service.TransactionDataService;
import com.mahesh.fna.app.vo.ReportSearchVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/fna/")
@Tag(name = "PayBookRestService-API", description = "Transaction related operations")
public class PayBookRestService {

	@Autowired
	private TransactionDataService transactionDataService;

	@GetMapping("/message")
	public ResponseEntity<String> welcomeMessage() {
		return ResponseEntity.ok("<h1>API Documentation : /api/fna/api-docs</h1>");
	}

	@PostMapping(value = "/transaction/report")
	@Operation(description = "Service to generate transaction report")
	public ResponseEntity<ReportSearchVO> recordTransactionRecord(ReportSearchVO searchVO) {
		try {
			searchVO = new ReportSearchVO();
			searchVO.setFromDate("01/JAN/2000");
			searchVO.setToDate("01/JAN/2027");
			searchVO.setAccountId(0);
			searchVO.setDebtorId(3);
			searchVO.setTransactionType("ALL");
			searchVO.setFileType(".pdf");
			searchVO.setReportName("DebtReport");
			searchVO = transactionDataService.generateTransactionReport(searchVO);
			return ResponseEntity.ok(searchVO);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
			return ResponseEntity.internalServerError().body(searchVO);
		}
	}
}