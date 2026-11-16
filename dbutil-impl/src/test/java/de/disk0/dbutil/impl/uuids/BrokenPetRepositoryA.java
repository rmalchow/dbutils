package de.disk0.dbutil.impl.uuids;

import de.disk0.dbutil.api.IdGeneratorClass;
import de.disk0.dbutil.impl.AbstractGuidRepositoryV2;
import de.disk0.dbutil.impl.pets.Pet;

@IdGeneratorClass(value = BrokenUuidGeneratorA.class)
public class BrokenPetRepositoryA extends AbstractGuidRepositoryV2<Pet> {


}
