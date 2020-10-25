package de.disk0.dbutil.impl.uuids;

import de.disk0.dbutil.api.GuidGenerator;

public class BrokenUuidGeneratorA implements GuidGenerator {
	
	public BrokenUuidGeneratorA() {
		// no empty constructor
	}

	@Override
	public String generateUuid() {
		throw new RuntimeException();
	}

}
