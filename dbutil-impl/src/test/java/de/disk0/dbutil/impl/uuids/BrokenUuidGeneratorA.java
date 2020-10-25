package de.disk0.dbutil.impl.uuids;

import de.disk0.dbutil.api.IdGenerator;

public class BrokenUuidGeneratorA implements IdGenerator {
	
	public BrokenUuidGeneratorA() {
		// no empty constructor
	}

	@Override
	public String generateId() {
		throw new RuntimeException();
	}

}
