package de.disk0.dbutil.impl.core;

import de.disk0.dbutil.api.entities.BaseGuidEntityV2;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Table(name="item")
public class Item extends BaseGuidEntityV2 {

	@Column(name="name")
	private String name;

	@Column(name="size")
	private Integer size;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

}
