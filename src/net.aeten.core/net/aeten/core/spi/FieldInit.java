package net.aeten.core.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.CLASS)
@Inherited
public @interface FieldInit {
	String[] alias() default "";

	boolean required() default true;
}
