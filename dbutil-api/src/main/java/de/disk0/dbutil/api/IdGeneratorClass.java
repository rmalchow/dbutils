package de.disk0.dbutil.api;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.disk0.dbutil.api.utils.IdGeneratorGuid;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface IdGeneratorClass {
	
	Class<? extends IdGenerator> value() default IdGeneratorGuid.class;

}
