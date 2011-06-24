package com.github.simplejpatemplate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "child")
public class EntityChild {
	@Id
	private String idField;

	@Basic
	@Column(name = "somecol")
	private String some;
	
	@ManyToOne
	private EntityParent parent;

	public String getIdField() {
		return idField;
	}

	public void setIdField(String idField) {
		this.idField = idField;
	}

	public String getSome() {
		return some;
	}

	public void setSome(String some) {
		this.some = some;
	}

	public EntityParent getParent() {
		return parent;
	}

	public void setParent(EntityParent parent) {
		this.parent = parent;
	}
}
