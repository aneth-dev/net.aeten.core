package net.aeten.core.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class StringUtil {

	public static String[] splitWithQuote (String input) throws IllegalArgumentException {
		return splitWithQuote (input, '"', '"');
	}

	public static String[] splitWithQuote (String input,
														char quote) throws IllegalArgumentException {
		return splitWithQuote (input, quote, quote);
	}

	public static String[] splitWithQuote (String input,
														char openQuote,
														char closeQuote) throws IllegalArgumentException {
		if (input == null) {
			return new String[] {};
		}

		final LinkedList <String> output = new LinkedList <> ();

		int begin;
		int end;
		for (int i = 0; i < input.length (); i++) {
			begin = findNextQuote (input, i, openQuote);
			if (begin > i) addAll (output, new String (input.getBytes (), i, begin - i).trim ().split ("\\s+"));
			if (begin == -1) {
				addAll (output, new String (input.getBytes (), i, input.length () - i).trim ().split ("\\s+"));
				break;
			}
			if (i == begin) continue;
			i = begin;

			end = findNextQuote (input, ++i, closeQuote);
			if (end > i) {
				String quoted = new String (input.getBytes (), i, end - i);
				Pattern pattern = Pattern.compile ("\\" + openQuote, Pattern.LITERAL);
				quoted = pattern.matcher (quoted).replaceAll ("" + openQuote);
				if (openQuote != closeQuote) {
					pattern = Pattern.compile ("\\" + closeQuote, Pattern.LITERAL);
					quoted = pattern.matcher (quoted).replaceAll ("" + closeQuote);
				}

				output.add (quoted);
			}
			if (end == -1) throw new IllegalArgumentException ("Unclosed quote at char " + begin + ". Input=" + input);
			i = end;
		}
		return output.toArray (new String[output.size ()]);
	}

	private static int findNextQuote (	String arguments,
													int begin,
													char quote) {
		boolean isEscaped = false;
		for (int i = begin; i < arguments.length (); i++) {
			if (!isEscaped && (arguments.charAt (i) == quote)) {
				return i;
			}
			isEscaped = (arguments.charAt (i) == '\\')? !isEscaped: false;
		}
		return -1;
	}

	private static void addAll (	LinkedList <String> list,
											String[] elements) {
		for (String element: elements)
			list.add (element);
	}

	public static void main (String[] arguments) {
		doAssertion (splitWithQuote ("-a toto -b titi"), new String[] {
				"-a",
				"toto",
				"-b",
				"titi"
		});
		doAssertion (splitWithQuote ("-a toto -b titi -c \"tutu titi\\\"\" -d tata"), new String[] {
				"-a",
				"toto",
				"-b",
				"titi",
				"-c",
				"tutu titi\"",
				"-d",
				"tata",
		});
		doAssertion (splitWithQuote ("-a toto -b \"-c \\\"titi\\\"\""), new String[] {
				"-a",
				"toto",
				"-b",
				"-c \"titi\""
		});

		doAssertion (splitWithQuote ("-a toto -b titi -c 'tutu titi\\'' -d tata", '\''), new String[] {
				"-a",
				"toto",
				"-b",
				"titi",
				"-c",
				"tutu titi'",
				"-d",
				"tata",
		});
		doAssertion (splitWithQuote ("-a toto -b '-c \\'titi\\''", '\''), new String[] {
				"-a",
				"toto",
				"-b",
				"-c 'titi'"
		});

		doAssertion (splitWithQuote ("-a toto -b titi -c (tutu titi\\)) -d tata", '(', ')'), new String[] {
				"-a",
				"toto",
				"-b",
				"titi",
				"-c",
				"tutu titi)",
				"-d",
				"tata",
		});
		doAssertion (splitWithQuote ("-a toto -b (-c \\(titi\\))", '(', ')'), new String[] {
				"-a",
				"toto",
				"-b",
				"-c (titi)"
		});

		System.out.println ("Success");
	}

	private static void doAssertion (String[] input,
												String[] expectedOutput) {
		assert (input.length == expectedOutput.length): Arrays.toString (input) + " != " + Arrays.toString (expectedOutput) + ")";
		for (int i = 0; i < expectedOutput.length; i++)
			assert input[i].equals (expectedOutput[i]): input[i] + " != " + expectedOutput[i];
	}

}
