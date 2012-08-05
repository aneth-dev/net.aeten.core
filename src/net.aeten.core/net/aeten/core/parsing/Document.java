package net.aeten.core.parsing;

import java.io.Reader;
import java.util.*;
import net.aeten.core.event.Handler;

/**
 *
 * @author thomas
 */
public class Document {
	public final Map<String, Tag> anchors = new HashMap<>();
	public final Entry root = new Entry(this);

	public static class Tag {
		public String name;
		public String type;
		public Object value;

		public Tag(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name + ((type == null) ? ": " : (" (" + type + "): ")) + value;
		}
	}

	public static class Entry {
		public final Entry parent;
		public final Document document;
		public final Queue<Tag> tags = Collections.asLifoQueue(new ArrayDeque<Tag>());

		public Entry(Document document) {
			this.parent = null;
			this.document = document;
		}
		public Entry(Entry parent) {
			this.parent = parent;
			this.document = parent.document;
		}
	}

	public static Document load(Reader reader, Parser<MarkupNode> parser) {
		final Document document = new Document();
 		parser.parse(reader, new Handler<ParsingData<MarkupNode>>() {
			Document.Entry entry = document.root;
			@Override
			public void handleEvent(ParsingData<MarkupNode> data) {
				switch (data.getEvent()) {
				case START_NODE:
					switch (data.getNodeType()) {
					case TEXT:
						entry.tags.peek().value = data.getValue();
						break;
					case MAP:
					case LIST:
						if (entry != document.root) {
							Document.Entry value = new Document.Entry(entry);
							entry.tags.peek().value = value;
							entry = value;
						}
						break;
					case TYPE:
						entry.tags.peek().type = data.getValue();
						break;
					case TAG:
						entry.tags.add(new Document.Tag(data.getValue()));
						break;
					case ANCHOR:
						entry.document.anchors.put(data.getValue(), entry.tags.peek());
						break;
					case REFERENCE:
						Document.Tag ref = entry.document.anchors.get(data.getValue());
						Document.Tag tag = entry.tags.peek();
						tag.value = ref.value;
						tag.type = ref.type;
						break;
					default:
						break;
					}
					break;
				case END_NODE:
					switch (data.getNodeType()) {
					case MAP:
					case LIST:
						entry = entry.parent;
						break;
					default:
						break;
					}
					break;
				}
			}
		});
		return document;
	}
}
