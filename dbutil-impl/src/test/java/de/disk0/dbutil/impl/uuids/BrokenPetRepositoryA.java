package de.disk0.dbutil.impl.uuids;

import de.disk0.dbutil.api.GeneratorClass;
import de.disk0.dbutil.impl.AbstractGuidRepository;
import de.disk0.dbutil.impl.pets.Pet;

@GeneratorClass(value = BrokenUuidGeneratorA.class)
public class BrokenPetRepositoryA extends AbstractGuidRepository<Pet> {


}
