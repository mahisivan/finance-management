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
import com.mahesh.fna.app.enums.AccountTypeEnum;
import com.mahesh.fna.app.service.AccountDataService;
import com.mahesh.fna.app.vo.AccountDataVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController(value="accountDataRestService")
@RequestMapping(value="/api/AccountDataRestApi")
@Tag(name="AccountDataRest-API", description = "Account related operations")
public class AccountDataRestService {

    @Autowired
    private AccountDataService accountDataService;
    
    public static final String DELETE_MSG = "Deleted Successfully";

    private static final StringValidator validator = StringValidator.getInstance();

    @GetMapping(value = "/account/{accountId}")
    @Operation(description = "Service to fetch Account")
    @Parameter(name = "accountId", required = true)
    public AccountDataVO fetchAccountRecord(@PathVariable(value = "accountId",required = true) String accountId) {
        AccountDataVO accountDataVO = new AccountDataVO();
        accountDataVO.setAccountId(0);
        if(validator.isNumeric(accountId)) {
            accountDataVO.setAccountId(Integer.parseInt(accountId));
            accountDataVO.setAction(DmConstants.ACTION_VIEW);
        }
        accountDataVO = accountDataService.fetchRecord(accountDataVO);
        return accountDataVO;
    }

    @GetMapping(value = "/accounts")
    @Operation(description = "Service to fetch all Accounts")
    public List<AccountDataVO> listAccountRecords() {
        AccountDataVO accountDataVO = new AccountDataVO();
        accountDataVO = accountDataService.fetchAllRecords(accountDataVO);
        return (List<AccountDataVO>)accountDataVO.getAttributesMap().get(DmConstants.ACCOUNT_KEY);
    }

    @GetMapping(value = "/accounts/{type}")
    @Operation(description = "Service to fetch all Accounts based on Account Type")
    public List<AccountDataVO> listAccountRecordByType(@PathVariable(value = "type", required = true) String type) {
        AccountDataVO accountDataVO = new AccountDataVO();
        accountDataVO.setAccountType(AccountTypeEnum.valueOf(type));
        accountDataVO.setAction(DmConstants.ACTION_VIEW);
        accountDataVO = accountDataService.fetchAllRecords(accountDataVO);
        return (List<AccountDataVO>)accountDataVO.getAttributesMap().get(DmConstants.ACCOUNT_KEY);
    }

    @PostMapping(value = "/account")
    @Operation(description = "Service to save new Account")
    public AccountDataVO saveAccountRecord(AccountDataVO accountDataVO) {
        try {
            accountDataVO.setAction(DmConstants.ACTION_ADD);
            accountDataVO = accountDataService.saveRecord(accountDataVO);
        } catch (AppBaseException ex) {
            AppBaseExceptionLogger.printErrorMessage(ex);
        }
        return accountDataVO;
    }

    @PutMapping(value = "/account")
    @Operation(description = "Service to modify existing User")
    public AccountDataVO modifyAccountRecord(AccountDataVO accountDataVO) {
        try {
            accountDataVO.setAction(DmConstants.ACTION_MODIFY);
            accountDataVO = accountDataService.modifyRecord(accountDataVO);
        } catch (AppBaseException ex) {
            AppBaseExceptionLogger.printErrorMessage(ex);
        }
        return accountDataVO;
    }

    @DeleteMapping(value = "/account/{accountId}")
    @Operation(description = "Service to remove existing User")
    public ResponseEntity<String> removeAccountRecord(@PathVariable(value = "userId",required = true) String accountId) {
        try {
            AccountDataVO accountDataVO = new AccountDataVO();
            accountDataVO.setAccountId(0);
            if(validator.isNumeric(accountId)) {
                accountDataVO.setAccountId(Integer.parseInt(accountId));
                accountDataVO.setAction(DmConstants.ACTION_DELETE);
            }
            accountDataVO = accountDataService.removeRecord(accountDataVO);
            return ResponseEntity.ok(DELETE_MSG);
        } catch (AppBaseException ex) {
            AppBaseExceptionLogger.printErrorMessage(ex);
            return ResponseEntity.internalServerError().body(ex.getMessage());
        }
    }
}
