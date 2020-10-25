package de.disk0.dbutil.impl.uuids;

import de.disk0.dbutil.api.IdGenerator;

public class BrokenUuidGeneratorB implements IdGenerator {
	
	public BrokenUuidGeneratorB() {
	}

	@Override
	public String generateId() {
		return "foo";
	}

}
