package net.aeten.core.spi;

/**
 *
 * @author Thomas Pérennou
 */
public @interface Configuration {
	String name();
	Class<?> provider();
	String parser() default "";
}
