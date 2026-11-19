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
import com.mahesh.fna.app.enums.AccountStatusEnum;
import com.mahesh.fna.app.service.UserDataService;
import com.mahesh.fna.app.vo.UserDataVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController(value="userDataRestService")
@RequestMapping(value="/api/UserDataRestApi")
@Tag(name="UserDataRestService-API", description = "User related operations")
public class UserDataRestService {

    @Autowired
    private UserDataService userDataService;

    private static final StringValidator validator = StringValidator.getInstance();

    @GetMapping(value = "/user/{userId}")
    @Operation(description = "Service to fetch User")
    @Parameter(name = "userId", required = true)
    public UserDataVO fetchUser(@PathVariable(value = "userId",required = true) String userId) {
        UserDataVO userDataVO = new UserDataVO();
        userDataVO.setUserId(0);
        if(validator.isNumeric(userId)) {
            userDataVO.setUserId(Integer.parseInt(userId));
            userDataVO.setAction(DmConstants.ACTION_VIEW);
            userDataVO.setAccountStatus(AccountStatusEnum.A);
        }
        userDataVO = userDataService.fetchRecord(userDataVO);
        return userDataVO;
    }

    @GetMapping(value = "/users")
    @Operation(description = "Service to fetch all Users")
    public List<UserDataVO> listUsers() {
        UserDataVO userDataVO = userDataService.fetchAllRecords();
        return (List<UserDataVO>)userDataVO.getAttributesMap().get(DmConstants.USER_KEY);
    }

    @GetMapping(value = "/debtors")
    @Operation(description = "Service to fetch all debtors")
    public List<UserDataVO> listDebtors() {
        UserDataVO userDataVO = userDataService.fetchAllDebtors();
        return (List<UserDataVO>)userDataVO.getAttributesMap().get(DmConstants.USER_KEY);
    }

    @PostMapping(value = "/user")
    @Operation(description = "Service to save new User")
    public UserDataVO saveUser(UserDataVO userDataVO) {
        try {
            userDataVO.setAction(DmConstants.ACTION_ADD);
            userDataVO = userDataService.saveRecord(userDataVO);
        } catch (AppBaseException ex) {
            AppBaseExceptionLogger.printErrorMessage(ex);
        }
        return userDataVO;
    }

    @PutMapping(value = "/user")
    @Operation(description = "Service to modify existing User")
    public UserDataVO modifyUser(UserDataVO userDataVO) {
        try {
            userDataVO.setAction(DmConstants.ACTION_MODIFY);
            userDataVO = userDataService.modifyRecord(userDataVO);
        } catch (AppBaseException ex) {
            AppBaseExceptionLogger.printErrorMessage(ex);
        }
        return userDataVO;
    }

    @DeleteMapping(value = "/user/{userId}")
    @Operation(description = "Service to remove existing User")
    public ResponseEntity<String> removeUser(@PathVariable(value = "userId",required = true) String userId) {
        try {
            UserDataVO userDataVO = new UserDataVO();
            userDataVO.setUserId(0);
            userDataVO.setAccountStatus(AccountStatusEnum.A);
            if(validator.isNumeric(userId)) {
                userDataVO.setUserId(Integer.parseInt(userId));
                userDataVO.setAction(DmConstants.ACTION_DELETE);
            }
            userDataVO = userDataService.removeRecord(userDataVO);
            return ResponseEntity.ok(AccountDataRestService.DELETE_MSG);
        } catch (AppBaseException ex) {
            AppBaseExceptionLogger.printErrorMessage(ex);
            return ResponseEntity.internalServerError().body(ex.getMessage());
        }
    }
}
