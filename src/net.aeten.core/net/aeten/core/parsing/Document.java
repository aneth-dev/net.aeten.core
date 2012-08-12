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
		public final List<Tag> tags = new ArrayList<>();

		public Entry(Document document) {
			this.parent = null;
			this.document = document;
		}
		public Entry(Entry parent) {
			this.parent = parent;
			this.document = parent.document;
		}

		@Override
		public String toString() {
			return tags.toString();
		}
		
	}

	public static Document load(Reader reader, Parser<MarkupNode> parser) {
		final Document document = new Document();
 		parser.parse(reader, new Handler<ParsingData<MarkupNode>>() {
			Document.Entry entry = null;
			final Queue<Document.Entry> currentEntry = Collections.asLifoQueue(new ArrayDeque<Document.Entry>());
			Document.Tag currentTag = null;

			@Override
			public void handleEvent(ParsingData<MarkupNode> data) {
				switch (data.getEvent()) {
				case START_NODE:
					switch (data.getNodeType()) {
					case TEXT:
						currentTag.value = data.getValue();
						break;
					case MAP:
					case LIST:
						entry = entry == null ? document.root : new Document.Entry(entry);
						if (entry != document.root) {
							currentTag.value = entry;
						}
						currentEntry.add(entry);
						break;
					case TYPE:
						currentTag.type = data.getValue();
						break;
					case TAG:
						currentTag = new Document.Tag(data.getValue());
						entry.tags.add(currentTag);
						break;
					case ANCHOR:
						entry.document.anchors.put(data.getValue(), currentTag);
						break;
					case REFERENCE:
						Document.Tag ref = entry.document.anchors.get(data.getValue());
						Document.Tag tag = currentTag;
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
						currentEntry.poll();
						entry = currentEntry.peek();
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
