package net.aeten.core.parsing;

import java.io.Reader;

import net.aeten.core.Identifiable;
import net.aeten.core.event.Handler;

/**
 *
 * @author Thomas Pérennou
 */
public interface Parser<NodeType extends Enum <?>> extends
		Identifiable {
	public void parse (	Reader reader,
								Handler <ParsingData <NodeType>> handler) throws ParsingException;
}
