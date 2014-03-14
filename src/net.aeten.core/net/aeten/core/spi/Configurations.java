package net.aeten.core.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Thomas Pérennou
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.PACKAGE,
			ElementType.TYPE
})
public @interface Configurations {
	Configuration[] value();
}
