package de.disk0.dbutil.api.utils;

import java.util.UUID;

import de.disk0.dbutil.api.GuidGenerator;

public class GuidGeneratorDefault implements GuidGenerator {

	@Override
	public String generateUuid() {
		return UUID.randomUUID().toString();
	}

}
