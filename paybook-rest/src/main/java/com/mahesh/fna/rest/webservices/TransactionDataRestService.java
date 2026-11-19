package com.mahesh.fna.rest.webservices;

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
import com.mahesh.fna.app.service.TransactionDataService;
import com.mahesh.fna.app.vo.TransactionDataVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController(value="transactionDataRestService")
@RequestMapping(value="/api/TransactionDataRestApi")
@Tag(name="TransactionDataRest-API", description = "Transaction related operations")
public class TransactionDataRestService {

	@Autowired
    private TransactionDataService transactionDataService;

    private static final StringValidator validator = StringValidator.getInstance();
	
	@GetMapping(value = "/transaction/{transactionId}")
    @Operation(description = "Service to fetch Transaction Record")
    @Parameter(name = "salaryId", required = true)
	public TransactionDataVO fetchTransactionRecord(@PathVariable(value = "transactionId", required = true) String transactionId) {
		TransactionDataVO transactionDataVO = new TransactionDataVO();
		transactionDataVO.setTransactionId(0);
		if (validator.isNumeric(transactionId)) {
			transactionDataVO.setTransactionId(Integer.parseInt(transactionId));
			transactionDataVO.setAction(DmConstants.ACTION_VIEW);
		}
		transactionDataVO = transactionDataService.fetchRecord(transactionDataVO);
		return transactionDataVO;
	}

	@PostMapping(value = "/transaction")
	@Operation(description = "Service to save Transaction Records")
	public TransactionDataVO saveTransactionRecord(TransactionDataVO transactionDataVO) {
		try {
			transactionDataVO.setAction(DmConstants.ACTION_ADD);
			transactionDataVO = transactionDataService.saveRecord(transactionDataVO);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
		}
		return transactionDataVO;
	}
	
	@PutMapping(value = "/transaction")
	@Operation(description = "Service to modify Transaction Records")
	public TransactionDataVO modifyTransactionRecord(TransactionDataVO transactionDataVO) {
		try {
			transactionDataVO.setAction(DmConstants.ACTION_MODIFY);
			transactionDataVO = transactionDataService.modifyRecord(transactionDataVO);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
		}
		return transactionDataVO;
	}
	
	@DeleteMapping(value = "/transaction/{transactionId}")
	@Operation(description = "Service to delete Transaction Records")
	public ResponseEntity<String> recordTransactionRecord(@PathVariable(value = "transactionId",required = true) String transactionId) {		
		try {	
			TransactionDataVO transactionDataVO = new TransactionDataVO();
			transactionDataVO.setTransactionId(0);
            if(validator.isNumeric(transactionId)) {
            	transactionDataVO.setTransactionId(Integer.parseInt(transactionId));
            	transactionDataVO.setAction(DmConstants.ACTION_DELETE);
            }
            transactionDataVO = transactionDataService.removeRecord(transactionDataVO);
            return ResponseEntity.ok(AccountDataRestService.DELETE_MSG);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
			return ResponseEntity.internalServerError().body(ex.getMessage());
		}
	}	
	
}
