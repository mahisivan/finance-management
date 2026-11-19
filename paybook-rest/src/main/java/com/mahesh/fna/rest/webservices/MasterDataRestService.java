package com.mahesh.fna.rest.webservices;

import java.util.List;

import org.json.JSONObject;
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
import com.mahesh.fna.app.service.MasterDataService;
import com.mahesh.fna.app.vo.MasterDataVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController(value="masterDataRestService")
@RequestMapping(value="/api/MasterDataRestApi")
@Tag(name="MasterDataRest-API", description = "Company/Designation related operations")
public class MasterDataRestService {

    @Autowired
    private MasterDataService masterDataService;

    @PostMapping(value = "/master/view")
    @Operation(description = "Service to fetch Master")
    public MasterDataVO fetchMasterRecord(MasterDataVO masterDataVO) {
        masterDataVO = masterDataService.fetchRecord(masterDataVO);
        return masterDataVO;
    }

    @PostMapping(value = "/masters/view")
    @Operation(description = "Service to fetch all Masters")
    public List<MasterDataVO> listMasterRecords(MasterDataVO masterDataVO) {
        masterDataVO = masterDataService.fetchAllRecords(masterDataVO);
        String masterKey = masterDataVO.getMasterKey().concat(DmConstants.MASTER_KEY);
        List<MasterDataVO> masters = (List<MasterDataVO>)masterDataVO.getAttributesMap().get(masterKey);
        return masters;
    }

    @PostMapping(value = "/master")
    @Operation(description = "Service to save new Master")
    public MasterDataVO saveMasterRecord(MasterDataVO masterDataVO) {
        try {
            masterDataVO.setAction(DmConstants.ACTION_ADD);
            masterDataVO = masterDataService.saveRecord(masterDataVO);
        } catch (AppBaseException ex) {
            AppBaseExceptionLogger.printErrorMessage(ex);
        }
        return masterDataVO;
    }

    @PutMapping(value = "/master")
    @Operation(description = "Service to modify new Master")
    public MasterDataVO modifyMasterRecord(MasterDataVO masterDataVO) {
        try {
            masterDataVO.setAction(DmConstants.ACTION_MODIFY);
            masterDataVO = masterDataService.modifyRecord(masterDataVO);
        } catch (AppBaseException ex) {
            AppBaseExceptionLogger.printErrorMessage(ex);
        }
        return masterDataVO;
    }

    @DeleteMapping(value = "/master")
    @Operation(description = "Service to remove existing Master")
    public ResponseEntity<String> removeMasterRecord(MasterDataVO masterDataVO) {
        try {
            masterDataVO.setAction(DmConstants.ACTION_DELETE);
            masterDataVO = masterDataService.removeRecord(masterDataVO);
            return ResponseEntity.noContent().build();
        } catch (AppBaseException ex) {
            AppBaseExceptionLogger.printErrorMessage(ex);
            return ResponseEntity.internalServerError().body(ex.getMessage());
        }
    }
    
	@GetMapping(value = "/currency/convert/{data}")
	@Operation(description = "Service to display amount in wordings")
	public String handleCurrency(@PathVariable(name = "data",required = true)String data) {
		StringValidator validator = StringValidator.getInstance();
		JSONObject json = validator.convertNumericWordings(data);
		return json.toString();
	}
}