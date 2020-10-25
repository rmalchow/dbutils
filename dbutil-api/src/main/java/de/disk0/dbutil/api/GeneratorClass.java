package de.disk0.dbutil.api;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.disk0.dbutil.api.utils.GuidGeneratorDefault;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratorClass {
	
	Class<? extends GuidGenerator> value() default GuidGeneratorDefault.class;

}
