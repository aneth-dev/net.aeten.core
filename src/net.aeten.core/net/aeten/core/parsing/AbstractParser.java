package net.aeten.core.parsing;

import net.aeten.core.event.Handler;

public abstract class AbstractParser<NodeType extends Enum<?>> implements Parser<NodeType> {
	protected class Tag {
		public final Tag parent;
		public final String name;
		public NodeType childrenNodeType;
		public String childrenType;

		public Tag(Tag parent, String name) {
			this.parent = parent;
			this.name = name;
		}
	}

	@Override
	public String getIdentifier() {
		return this.getClass().getName();
	}

	protected void fireEvent(Handler<ParsingData<NodeType>> handler, ParsingEvent event, NodeType nodeType, String value, Tag parent) {
		handler.handleEvent(new ParsingData<NodeType>(this, event, nodeType, value, (parent == null) ? null : parent.name));
	}


}
