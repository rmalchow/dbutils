package de.disk0.dbutil.impl.uuids;

import de.disk0.dbutil.api.IdGeneratorClass;
import de.disk0.dbutil.api.utils.IdGeneratorFastBase64;
import de.disk0.dbutil.impl.AbstractGuidRepository;
import de.disk0.dbutil.impl.pets.Pet;

@IdGeneratorClass(value = IdGeneratorFastBase64.class)
public class PetRepository2 extends AbstractGuidRepository<Pet> {


}
