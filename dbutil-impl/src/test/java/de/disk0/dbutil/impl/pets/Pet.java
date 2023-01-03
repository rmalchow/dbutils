package de.disk0.dbutil.impl.pets;

import de.disk0.dbutil.api.entities.BaseGuidEntityV1;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Table(name="pet")
public class Pet extends BaseGuidEntityV1 {
	
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
