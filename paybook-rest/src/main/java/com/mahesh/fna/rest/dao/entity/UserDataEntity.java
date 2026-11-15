package com.mahesh.fna.rest.dao.entity;

import java.io.Serializable;
import java.util.Calendar;

import org.hibernate.annotations.Check;

import com.mahesh.fna.app.enums.AccountStatusEnum;
import com.mahesh.fna.app.enums.GenderEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;


/**
 * 
 * @author Kannan
 * pkColumnName    : Name of the primary key column in the table
 * pkColumnValue   : The primary key value in the generator table that distinguishes this set of generated values from others that may be stored in the table.
 * valueColumnName : Name of the column that stores the last value generated.
 */
@Entity
@Table(name = "USER_DATA")
@TableGenerator(name="USER_DATA_GEN",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="USER_ID", allocationSize=1)
public class UserDataEntity implements Serializable {

	/**
	 * Attribute to hold userId
	 */
	private Integer userId;

	/**
	 * Attribute to hold accountStatus
	 */
	private AccountStatusEnum accountStatus;
	
	/**
	 * Attribute to hold communicationAddress
	 */
	private String communicationAddress;
	
	/**
	 * Attribute to hold dateOfBirth
	 */
	private Calendar dateOfBirth;
	
	/**
	 * Attribute to hold gender
	 */
	private GenderEnum gender;	
	
	/**
	 * Attribute to hold emailId
	 */
	private String emailId;
	
	/**
	 * Attribute to hold firstName
	 */
	private String firstName;

	/**
	 * Attribute to hold middleName
	 */
	private String middleName;

	/**
	 * Attribute to hold lastName
	 */
	private String lastName;
	
	/**
	 * Attribute to hold loginCount
	 */
	private Integer loginCount;
	
	/**
	 * Attribute to hold loginPassword
	 */
	private String loginPassword;
	
	/**
	 * Attribute to hold mobileNo
	 */
	private String mobileNo;

	/**
	 * Attribute to hold createdDate
	 */
	private Calendar createdDate;
	
	/**
	 * Attribute to hold modifiedDate
	 */
	private Calendar modifiedDate;

	/**
	 * @return the userId
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator="USER_DATA_GEN")
    @Column(name = "USER_ID")
	public Integer getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * @return the accountStatus
	 */
	@Enumerated(EnumType.STRING)
	@Check(constraints ="ACC_STATUS IN ('A','L','E')")
	@Column(name = "ACC_STATUS", insertable = true, updatable = true, nullable = false, length=1)
	public AccountStatusEnum getAccountStatus() {
		return accountStatus;
	}

	/**
	 * @param accountStatus the accountStatus to set
	 */
	public void setAccountStatus(AccountStatusEnum accountStatus) {
		this.accountStatus = accountStatus;
	}

	/**
	 * @return the communicationAddress
	 */
	@Column(name = "COMM_ADDRESS", nullable = false, length=255)
	public String getCommunicationAddress() {
		return communicationAddress;
	}

	/**
	 * @param communicationAddress the communicationAddress to set
	 */
	public void setCommunicationAddress(String communicationAddress) {
		this.communicationAddress = communicationAddress;
	}

	/**
	 * @return the dateOfBirth
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "DATE_OF_BIRTH", insertable = true, updatable = true, nullable = false)
	public Calendar getDateOfBirth() {
		return dateOfBirth;
	}

	/**
	 * @param dateOfBirth the dateOfBirth to set
	 */
	public void setDateOfBirth(Calendar dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	/**
	 * @return the gender
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "GENDER", nullable = false, length=1)
	public GenderEnum getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(GenderEnum gender) {
		this.gender = gender;
	}

	/**
	 * @return the emailId
	 */
	@Column(name = "EMAIL_ID", nullable = false, unique = true, length=255)
	public String getEmailId() {
		return emailId;
	}

	/**
	 * @param emailId the emailId to set
	 */
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	/**
	 * @return the firstName
	 */
	@Column(name = "FIRST_NAME", nullable = false, length=100)
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the middleName
	 */
	@Column(name = "MIDDLE_NAME", nullable = true, length=100)
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * @param middleName the middleName to set
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * @return the lastName
	 */
	@Column(name = "LAST_NAME", nullable = true, length=100)
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the loginCount
	 */
	@Column(name = "LOGIN_COUNT", nullable = false)
	public Integer getLoginCount() {
		return loginCount;
	}

	/**
	 * @param loginCount the loginCount to set
	 */
	public void setLoginCount(Integer loginCount) {
		this.loginCount = loginCount;
	}

	/**
	 * @return the loginPassword
	 */
	@Column(name = "LOGIN_PASSWORD", nullable = false, length=500)
	public String getLoginPassword() {
		return loginPassword;
	}

	/**
	 * @param loginPassword the loginPassword to set
	 */
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	/**
	 * @return the mobileNo
	 */
	@Column(name = "MOBILE_NO", nullable = false, length=15)
	public String getMobileNo() {
		return mobileNo;
	}

	/**
	 * @param mobileNo the mobileNo to set
	 */
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	/**
	 * @return the createdDate
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "CREATED_DATE", insertable = true, updatable = false, nullable = false)
	public Calendar getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(Calendar createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the modifiedDate
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "MODIFIED_DATE", insertable = true, updatable = true, nullable = false)
	public Calendar getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * @param modifiedDate the modifiedDate to set
	 */
	public void setModifiedDate(Calendar modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
}