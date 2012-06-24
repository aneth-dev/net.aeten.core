package net.aeten.core.spi;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Thomas Pérennou
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Provider {
	Class<?>[] value();
}
