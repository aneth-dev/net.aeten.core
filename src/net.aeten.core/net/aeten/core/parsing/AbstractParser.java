package net.aeten.core.parsing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import net.aeten.core.Predicate;
import net.aeten.core.event.Handler;

public abstract class AbstractParser<NodeType extends Enum<?>> implements
		Parser<NodeType> {
	protected static final char CR = '\r'; // Carriage Return
	protected static final char LF = '\n'; // Line Feed
	protected static final char NEL = 0x0085; // Next Line
	protected static final char VT = 0x000B; // Vertical Tab
	protected static final char FF = '\f'; // Form Feed
	protected static final char LS = 0x2028; // Line Separator
	protected static final char PS = 0x2029; // Paragraph Separator

	public static class Tag<NodeType extends Enum<?>> {
		public final Tag<NodeType> parent;
		public final String name;
		public NodeType childrenNodeType;
		public String childrenType;

		public Tag(Tag<NodeType> parent,
				String name) {
			this.parent = parent;
			this.name = name;
		}
	}

	@Override
	public String getIdentifier() {
		return this.getClass ().getName ();
	}

	public static abstract class ParserImplementationHelper {
		private String line;
		private final Parser<MarkupNode> parser;
		private final BufferedReader reader;
		private final boolean replaceEolByLf;
		protected final Handler<ParsingData<MarkupNode>> handler;
		protected int lineIndex;
		protected String consumed = null;

		protected ParserImplementationHelper(Parser<MarkupNode> parser,
				Reader reader,
				Handler<ParsingData<MarkupNode>> handler,
				boolean replaceEolByLf) {
			this.parser = parser;
			this.reader = new BufferedReader (reader);
			this.handler = handler;
			this.replaceEolByLf = replaceEolByLf;
		}

		protected void parseText(Predicate<EntryUnderConstruction> predicate)
				throws ParsingException {
			String entry;
			try {
				while ((entry = nextEntry (predicate)) != null) {
					parse (entry);
				}
			} catch (Throwable error) {
				throw new ParsingException (error);
			}
		}

		private int next = -1;

		int read()
				throws ParsingException {
			int c;
			if (next != -1) {
				c = next;
				next = -1;
				return c;
			}
			try {
				c = reader.read ();
				if (replaceEolByLf) {
					switch (c) {
					case CR:
					case LF:
						char neighbor = (c == CR) ? LF : CR;
						next = reader.read ();
						if (c == neighbor) {
							next = -1;
						}
					case NEL:
					case VT:
					case FF:
					case LS:
					case PS:
						c = '\n';
						break;
					default:
						break;
					}
				}
				switch (c) {
				case CR:
				case LF:
				case NEL:
				case VT:
				case FF:
				case LS:
				case PS:
					lineIndex++;
					break;
				default:
					break;
				}
			} catch (IOException exception) {
				throw new ParsingException (exception);
			}
			return c;
		}

		public static class EntryUnderConstruction {
			private final StringBuilder input;
			private final BufferedReader reader;
			private String restored;

			public EntryUnderConstruction(StringBuilder input,
					BufferedReader reader) {
				super ();
				this.input = input;
				this.reader = reader;
			}

			public char peek() {
				return input.charAt (input.length () - 1);
			}

			public String peek(int backward) {
				int start = input.length () - backward;
				if (start < 0) {
					return null;
				}
				return input.substring (start);
			}

			public int peekPrevious() {
				int index = input.length () - 2;
				if (index < 0) {
					return -1;
				}
				return input.charAt (index);
			}

			public char pop() {
				char last = peek ();
				input.deleteCharAt (input.length () - 1);
				return last;
			}

			public void restore(CharSequence toRestore) {
				if (restored == null) {
					restored = toRestore.toString ();
				} else {
					restored += toRestore;
				}
			}

			public void restore(char toRestore) {
				restore (Character.toString (toRestore));
			}

			public String getRestored() {
				return restored == null ? null : restored.toString ();
			}

			public boolean checkNext(char expected) {
				int next = -1;
				try {
					next = reader.read ();
					if (next == expected) {
						return true;
					}
				} catch (IOException exception) {
					throw new Error (exception);
				} finally {
					if (next != -1) {
						restore ((char) next);
					}
				}
				return false;
			}
		}

		protected static final Predicate<EntryUnderConstruction> END_OF_LINE = new Predicate<EntryUnderConstruction> () {
			@Override
			public boolean evaluate(EntryUnderConstruction element) {
				switch (element.peek ()) {
				case CR:
				case LF:
				case NEL:
				case VT:
				case FF:
				case LS:
				case PS:
					element.pop ();
					return true;
				default:
					return false;
				}
			}
		};

		protected String nextLine()
				throws ParsingException {
			return nextEntry (END_OF_LINE, false);
		}

		protected String nextLine(boolean errorIfendOfFile)
				throws ParsingException {
			return nextEntry (END_OF_LINE, errorIfendOfFile);
		}

		protected String nextEntry(Predicate<EntryUnderConstruction> endOfEntryPredicate)
				throws ParsingException {
			return nextEntry (endOfEntryPredicate, false);
		}

		protected String nextEntry(Predicate<EntryUnderConstruction> endOfEntryPredicate,
				boolean errorIfendOfFile)
				throws ParsingException {
			StringBuilder entry = new StringBuilder ();
			while (true) {
				if (consumed != null) {
					entry.append (consumed);
					consumed = null;
				}
				int c;
				try {
					c = read ();
				} catch (Throwable exception) {
					throw new ParsingException (exception);
				}
				if (c == -1) {
					if (errorIfendOfFile) {
						error ("End of file reached");
					}
					return entry.length () > 0 ? entry.toString () : null;
				}
				entry.append ((char) c);
				EntryUnderConstruction entryParameter = new EntryUnderConstruction (entry, reader);
				boolean endOfEntry = endOfEntryPredicate.evaluate (entryParameter);
				consumed = entryParameter.getRestored ();
				if (endOfEntry) {
					return entry.toString ();
				}
			}
		}

		protected abstract void parse(String entry)
				throws ParsingException;

		protected void error(String message,
				int errorPositionInLine)
				throws ParsingException {
			throw new ParsingException (message, line, lineIndex, errorPositionInLine);
		}

		protected void error(String message,
				int errorPositionInLine,
				Throwable cause)
				throws ParsingException {
			throw new ParsingException (message, line, lineIndex, errorPositionInLine, cause);
		}

		protected void error(String message)
				throws ParsingException {
			error (message, 0);
		}

		protected void error(String message,
				Throwable cause)
				throws ParsingException {
			error (message, 0, cause);
		}

		protected void fireEvent(ParsingEvent event,
				MarkupNode nodeType,
				String value) {
			ParsingData<MarkupNode> data = new ParsingData<> (parser, event, nodeType, value);
			if (event == ParsingEvent.START_NODE || nodeType == MarkupNode.LIST || nodeType == MarkupNode.MAP || nodeType == MarkupNode.TAG) {
				System.out.println (data);
			}
			handler.handleEvent (data);
		}

		protected void text(String value) {
			fireEvent (ParsingEvent.START_NODE, MarkupNode.TEXT, value);
			fireEvent (ParsingEvent.END_NODE, MarkupNode.TEXT, value);
		}

		protected void comment(String value) {
			fireEvent (ParsingEvent.START_NODE, MarkupNode.COMMENT, value);
			fireEvent (ParsingEvent.END_NODE, MarkupNode.COMMENT, value);
		}

		protected void type(String value) {
			fireEvent (ParsingEvent.START_NODE, MarkupNode.TYPE, value);
			fireEvent (ParsingEvent.END_NODE, MarkupNode.TYPE, value);
		}

	}

}
