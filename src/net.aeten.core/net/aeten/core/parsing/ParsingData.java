package net.aeten.core.parsing;

import net.aeten.core.event.EventData;

/**
 *
 * @author Thomas Pérennou
 */
public class ParsingData<NodeType extends Enum<?>> extends EventData<net.aeten.core.parsing.Parser<NodeType>, ParsingEvent> {
	private final NodeType nodeType;
	private final String value, parent;

	public ParsingData(Parser<NodeType> source, ParsingEvent event, NodeType nodeType, String value, String parent) {
		super(source, event);
		this.nodeType = nodeType;
		this.value = value;
		this.parent = parent;
	}

	public NodeType getNodeType() {
		return this.nodeType;
	}

	public String getValue() {
		return this.value;
	}

	public String getParent() {
		return this.parent;
	}

}
