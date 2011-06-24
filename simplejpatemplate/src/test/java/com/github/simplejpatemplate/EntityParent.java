package com.github.simplejpatemplate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(catalog = "catalog", name = "name", schema = "schema")
public class EntityParent {
	@Id
	@Column(name="idCol")
	private Integer id;

	@Basic
	@Column(name = "colone")
	private String fieldOne;
	
	@OneToMany
	private EntityChild child;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFieldOne() {
		return fieldOne;
	}

	public void setFieldOne(String fieldOne) {
		this.fieldOne = fieldOne;
	}

	public EntityChild getChild() {
		return child;
	}

	public void setChild(EntityChild child) {
		this.child = child;
	}
}
