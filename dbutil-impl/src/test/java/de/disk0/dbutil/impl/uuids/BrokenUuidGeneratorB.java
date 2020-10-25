package de.disk0.dbutil.impl.uuids;

import de.disk0.dbutil.api.GuidGenerator;

public class BrokenUuidGeneratorB implements GuidGenerator {
	
	public BrokenUuidGeneratorB() {
	}

	@Override
	public String generateUuid() {
		return "foo";
	}

}
