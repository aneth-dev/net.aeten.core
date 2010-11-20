package net.aeten.core.service;

/**
 *
 * @author Thomas Pérennou
 */
public @interface Configuration {
	String name();
	Class<?> provider();
	String parser() default "";
	String converter() default "";
}
