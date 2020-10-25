package de.disk0.dbutil.impl.uuids;

import de.disk0.dbutil.api.GeneratorClass;
import de.disk0.dbutil.api.utils.GuidGeneratorFastBase64;
import de.disk0.dbutil.impl.AbstractGuidRepository;
import de.disk0.dbutil.impl.pets.Pet;

@GeneratorClass(value = GuidGeneratorFastBase64.class)
public class PetRepository2 extends AbstractGuidRepository<Pet> {


}
