package net.aeten.core.parsing;

/**
 *
 * @author Thomas Pérennou
 */
public class ParsingException extends Exception {

	private static final long serialVersionUID = -8170274313643319764L;

	public ParsingException(Throwable cause) {
		super(cause);
	}

	public ParsingException(String message, String line, int errorPosition) {
		super(String.format("%s in \"%s\" at position %d", message, line, errorPosition));
	}

	public ParsingException(String message, String line, int lineIndex, int errorPosition) {
		super(String.format("%s in line \"%s\" at position %d", message, lineIndex, line, errorPosition)); // TODO format with "........^" below
	}

	public ParsingException(String message, String line, int lineIndex, int errorPosition, Throwable cause) {
		super(String.format("%s in line \"%s\" at position %d", message, lineIndex, line, errorPosition), cause); // TODO format with "........^" below
	}

	public ParsingException(String message, String line, int errorPosition, Throwable cause) {
		super(String.format("%s in \"%s\" at position %d", message, line, errorPosition), cause);
	}

}
