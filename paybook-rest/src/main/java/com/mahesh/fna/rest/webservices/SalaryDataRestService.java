package com.mahesh.fna.rest.webservices;

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
import com.mahesh.fna.app.service.SalaryDataService;
import com.mahesh.fna.app.vo.SalaryDataVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController(value="salaryDataRestService")
@RequestMapping(value="/api/SalaryDataRestApi")
@Tag(name="SalaryDataRest-API", description = "Salary related operations")
public class SalaryDataRestService {

    @Autowired
    private SalaryDataService salaryDataService;

    private static final StringValidator validator = StringValidator.getInstance();
	
	@GetMapping(value = "/salary/{salaryId}")
    @Operation(description = "Service to fetch Salary Record based on Account")
    @Parameter(name = "salaryId", required = true)
	public SalaryDataVO fetchSalaryRecord(@PathVariable(value = "salaryId", required = true) String salaryId) {
		SalaryDataVO salaryDataVO = new SalaryDataVO();
		salaryDataVO.setSalaryId(0);
		if (validator.isNumeric(salaryId)) {
			salaryDataVO.setSalaryId(Integer.parseInt(salaryId));
			salaryDataVO.setAction(DmConstants.ACTION_VIEW);
		}
		salaryDataVO = salaryDataService.fetchRecord(salaryDataVO);
		return salaryDataVO;
	}

	@GetMapping(value = "/salary/{companyId}")
	@Operation(description = "Service to fetch Salary Records based on Company")
	public List<SalaryDataVO> fetchSalaryRecordByCompany(@PathVariable(value = "companyId", required = true) String companyId) {
		SalaryDataVO loanDataVO = salaryDataService.fetchAllRecords();
		return (List<SalaryDataVO>) loanDataVO.getAttributesMap().get(DmConstants.LOAN_KEY);
	}

	@PostMapping(value = "/salary")
	@Operation(description = "Service to save Salary Records based on Account Number")
	public SalaryDataVO saveSalaryRecord(SalaryDataVO salaryDataVO) {
		try {
			salaryDataVO.setAction(DmConstants.ACTION_ADD);
			salaryDataVO = salaryDataService.saveRecord(salaryDataVO);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
		}
		return salaryDataVO;
	}
	
	@PutMapping(value = "/salary")
	@Operation(description = "Service to modify Salary Records based on Account Number")
	public SalaryDataVO modifySalaryRecord(SalaryDataVO salaryDataVO) {
		try {
			salaryDataVO.setAction(DmConstants.ACTION_MODIFY);
			salaryDataVO = salaryDataService.modifyRecord(salaryDataVO);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
		}
		return salaryDataVO;
	}
	
	@DeleteMapping(value = "/salary/{salaryId}")
	@Operation(description = "Service to delete Salary Records based on Account Number")
	public ResponseEntity<String> recordSalaryRecord(@PathVariable(value = "salaryId",required = true) String salaryId) {		
		try {	
			SalaryDataVO salaryDataVO = new SalaryDataVO();
			salaryDataVO.setSalaryId(0);
            if(validator.isNumeric(salaryId)) {
            	salaryDataVO.setSalaryId(Integer.parseInt(salaryId));
                salaryDataVO.setAction(DmConstants.ACTION_DELETE);
            }
            salaryDataVO = salaryDataService.removeRecord(salaryDataVO);
            return ResponseEntity.ok(AccountDataRestService.DELETE_MSG);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
			return ResponseEntity.internalServerError().body(ex.getMessage());
		}
	}	
}
