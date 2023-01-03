package de.disk0.dbutil.impl.pets;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

import de.disk0.dbutil.api.entities.BaseGuidEntity;

@Table(name="pet")
public class Pet extends BaseGuidEntity {
	
	@Column(name="age")
	private Integer age;

	@Column(name="size")
	private int size;

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
	
	

}
