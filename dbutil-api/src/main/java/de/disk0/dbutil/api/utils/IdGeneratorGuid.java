package de.disk0.dbutil.api.utils;

import java.util.UUID;

import de.disk0.dbutil.api.IdGenerator;

public class IdGeneratorGuid implements IdGenerator {

	@Override
	public String generateId() {
		return UUID.randomUUID().toString();
	}

}
