package com.mahesh.fna.rest.dao.entity;

import java.io.Serializable;
import java.util.Calendar;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@MappedSuperclass
public class AuditEntity implements Serializable {
	
	/**
	 * Attribute to hold createdBy
	 */
	private Integer createdBy;
	
	/**
	 * Attribute to hold createdDate
	 */
	private Calendar createdDate;
	
	/**
	 * Attribute to hold modifiedBy
	 */
	private Integer modifiedBy;
	
	/**
	 * Attribute to hold modifiedDate
	 */
	private Calendar modifiedDate;

	/**
	 * @return the createdBy
	 */
	@Column(name = "CREATED_BY", nullable=false)
	public Integer getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the createdDate
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "CREATED_DATE", nullable=false)
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
	 * @return the modifiedBy
	 */
	@Column(name = "MODIFIED_BY", nullable=false)
	public Integer getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * @param modifiedBy the modifiedBy to set
	 */
	public void setModifiedBy(Integer modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	/**
	 * @return the modifiedDate
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "MODIFIED_DATE", nullable=false)
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
