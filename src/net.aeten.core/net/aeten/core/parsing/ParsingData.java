package net.aeten.core.parsing;

import net.aeten.core.event.EventData;

/**
 *
 * @author Thomas Pérennou
 */
public class ParsingData<NodeType extends Enum<?>> extends
		EventData<net.aeten.core.parsing.Parser<NodeType>, ParsingEvent> {
	private final NodeType nodeType;
	private final String value;

	public ParsingData(Parser<NodeType> source,
			ParsingEvent event,
			NodeType nodeType,
			String value) {
		super (source, event);
		this.nodeType = nodeType;
		this.value = value;
	}

	public NodeType getNodeType() {
		return this.nodeType;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "" + event + " " + nodeType + " " + value;
	}
}
