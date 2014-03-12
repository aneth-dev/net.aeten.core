package net.aeten.core.parsing;

import java.io.Reader;

import net.aeten.core.Identifiable;

/**
 *
 * @author Thomas Pérennou
 */
public interface MarkupConverter<T> extends
		Identifiable {
	public T convert (Reader reader,
							Parser <MarkupNode> parser);
}
