package net.aeten.core.spi;

public @interface FieldInit {
	String alias() default "";
	boolean required() default false;
}
