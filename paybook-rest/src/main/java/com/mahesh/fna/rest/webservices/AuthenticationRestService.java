package com.mahesh.fna.rest.webservices;

import java.sql.Connection;
import java.sql.DriverManager;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mahesh.cf.customexeception.AppBaseException;
import com.mahesh.cf.customexeception.AppBaseExceptionLogger;
import com.mahesh.fna.app.vo.ReportSearchVO;
import com.mahesh.fna.rest.service.JwtService;
import com.mahesh.fna.rest.util.FnaRestHelper;
import com.mahesh.fna.rest.util.PasswordEncoderUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController(value="authenticationRestService")
@RequestMapping(value="/api/AuthenticationRestApi")
@Tag(name="Authentication-API", description = "Authentication related operations")
public class AuthenticationRestService {

	@Value("${spring.datasource.url}")
	private String databaseUrl;

	@Value("${spring.datasource.username}")
	private String dbUserName;

	@Value("${spring.datasource.password}")
	private String dbPassword;

	@Value("${spring.datasource.driver-class-name}")
	private String driverClassName;

	@Value("${app.password.aes-key}")
	private String passwordAesKey;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtService jwtService;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
		if (loginRequest.emailId() == null || loginRequest.emailId().isBlank()
				|| loginRequest.loginPassword() == null || loginRequest.loginPassword().isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("message", "EmailId and LoginPassword are required"));
		}
		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.emailId(), loginRequest.loginPassword()));
			if (!auth.isAuthenticated()) {
				return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
			}
		} catch (AuthenticationException ex) {
			return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
		}
		String token = jwtService.generateToken(loginRequest.emailId());
		ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token).httpOnly(true).secure(true).path("/").maxAge(3600).sameSite("Strict").build();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(Map.of("message", "Login successful", "emailId", loginRequest.emailId()));
	}

	@GetMapping("/logout")
	public ResponseEntity<?> logout() {
		ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", "").httpOnly(true).secure(true).path("/").maxAge(0).sameSite("Strict").build();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(Map.of("message", "Logout successful"));
	}

	@GetMapping("/encrypt/{password}")
	public ResponseEntity<?> encryptPassword(@PathVariable("password") String password) {
		try {
			String encryptedPassword = PasswordEncoderUtil.encrypt(password, passwordAesKey);
			Map<String, String> hmap = new HashMap<>();
			hmap.put("Encrypted Password", encryptedPassword);
			return ResponseEntity.ok().body(hmap);
		} catch (GeneralSecurityException | IllegalArgumentException ex) {
			return ResponseEntity.internalServerError().body(Map.of("message", "Unable to encrypt the password"));
		}

	}

	@PostMapping(value = "/transaction/report")
	@Operation(description = "Service to generate transaction report")
	public ResponseEntity<ReportSearchVO> generateTransactionReport(ReportSearchVO searchVO) {
		try {
			Class.forName(driverClassName);
			try (Connection con = DriverManager.getConnection(databaseUrl, dbUserName, dbPassword)) {
				AppBaseExceptionLogger.printInfoMessage("Database Status", con == null ? "FAILED" : "SUCCESS");
				if (con != null) {
					Map<String, Object> hmap = new HashMap<String, Object>();
					hmap.put("IS_IGNORE_PAGINATION", false);
					hmap.put("debtorId", searchVO.getDebtorId());
					hmap.put("transactionType", searchVO.getTransactionType());
					hmap.put("fromDate", searchVO.getFromDate());
					hmap.put("toDate", searchVO.getToDate());
					hmap.put("accountId", searchVO.getAccountId());
					hmap.put("imagesFolder", "images/Banners");
					searchVO = FnaRestHelper.generateReport(con, hmap, "DebtReport");
				}
			}
		} catch (Exception ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
		}
		return ResponseEntity.ok(searchVO);
	}

	@PostMapping(value = "/cardtransaction/report")
	@Operation(description = "Service to generate card transaction report")
	public ResponseEntity<ReportSearchVO> generateCardTransactionReport(ReportSearchVO searchVO) {
		try {
			searchVO.getAttributesMap().put("driverClassName", driverClassName);
			searchVO.getAttributesMap().put("databaseUrl", databaseUrl);
			searchVO.getAttributesMap().put("dbUserName", dbUserName);
			searchVO.getAttributesMap().put("dbPassword", dbPassword);
			searchVO = FnaRestHelper.generateCardTransactionReport(searchVO);
			AppBaseExceptionLogger.printInfoMessage("Messages", searchVO.getMessages());
			AppBaseExceptionLogger.printInfoMessage("ReportSearchVO", searchVO.toString());
			return ResponseEntity.ok(searchVO);
		} catch (AppBaseException ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
			return ResponseEntity.internalServerError().body(searchVO);
		}
	}

	@PostMapping(value = "/loan/report")
	@Operation(description = "Service to generate loan report")
	public ResponseEntity<ReportSearchVO> generateLoanReport(ReportSearchVO searchVO) {
		try {
			Class.forName(driverClassName);
			try (Connection con = DriverManager.getConnection(databaseUrl, dbUserName, dbPassword)) {
				AppBaseExceptionLogger.printInfoMessage("Database Status", con == null ? "FAILED" : "SUCCESS");
				if (con != null) {
					Map<String, Object> hmap = new HashMap<String, Object>();
					hmap.put("IS_IGNORE_PAGINATION", false);
					hmap.put("accountId", searchVO.getAccountId());
					hmap.put("fromDate", searchVO.getFromDate());
					hmap.put("toDate", searchVO.getToDate());
					hmap.put("imagesFolder", "images/Banners");
					searchVO = FnaRestHelper.generateReport(con, hmap, "LoanReport");
				}
			}
		} catch (Exception ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
		}
		return ResponseEntity.ok(searchVO);
	}

	@PostMapping(value = "/salary/report")
	@Operation(description = "Service to generate salary report")
	public ResponseEntity<ReportSearchVO> generateSalaryReport(ReportSearchVO searchVO) {
		try {
			Class.forName(driverClassName);
			try (Connection con = DriverManager.getConnection(databaseUrl, dbUserName, dbPassword)) {
				AppBaseExceptionLogger.printInfoMessage("Database Status", con == null ? "FAILED" : "SUCCESS");
				if (con != null) {
					Map<String, Object> hmap = new HashMap<String, Object>();
					hmap.put("IS_IGNORE_PAGINATION", false);
					hmap.put("debtorId", searchVO.getDebtorId());
					hmap.put("transactionType", searchVO.getTransactionType());
					hmap.put("fromDate", searchVO.getFromDate());
					hmap.put("toDate", searchVO.getToDate());
					hmap.put("accountId", searchVO.getAccountId());
					hmap.put("imagesFolder", "images/Banners");
					searchVO = FnaRestHelper.generateReport(con, hmap, "SalaryReport");
				}
			}
		} catch (Exception ex) {
			AppBaseExceptionLogger.printErrorMessage(ex);
		}
		return ResponseEntity.ok(searchVO);
	}

	public record LoginRequest(@JsonAlias("EmailId") String emailId,
			@JsonAlias("LoginPassword") String loginPassword) {
	}
}