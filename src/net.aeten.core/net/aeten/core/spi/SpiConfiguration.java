package net.aeten.core.spi;

import java.io.InputStreamReader;

import net.aeten.core.Format;
import java.util.function.Predicate;
import net.aeten.core.parsing.Document;
import net.aeten.core.parsing.MarkupNode;
import net.aeten.core.parsing.Parser;
import net.aeten.core.parsing.ParsingException;

/**
 *
 * @author Thomas Pérennou
 */
public class SpiConfiguration {
	public final Document.Element root;

	public SpiConfiguration(Document.Element configuration) {
		this.root = configuration;
	}

	public SpiConfiguration(Package package_, String resourceName, String parserIdentifier, Class<?> instanceClass) {
		this(package_.getName(), resourceName, parserIdentifier, instanceClass);
	}

	@SuppressWarnings("unchecked")
	public SpiConfiguration(String packageName, String resourceName, String parserIdentifier, Class<?> instanceClass) {
		String resource = packageName.replace('.', '/') + "/" + resourceName;
		Parser<MarkupNode> parser;
		if (parserIdentifier == null || parserIdentifier.isEmpty()) {
			int extensionIndex = resourceName.lastIndexOf('.');
			if ((extensionIndex == -1) || (extensionIndex == (resourceName.length() + 1))) { throw new IllegalArgumentException("Unable to find parser without extension in resource file " + resourceName); }
			final String extension = resourceName.substring(extensionIndex + 1);
			@SuppressWarnings("rawtypes")
			Predicate<Parser> parserPredicate = new Predicate<Parser>() {
				@Override
				public boolean test(Parser element) {
					Format format = element.getClass().getAnnotation(Format.class);
					return (format != null) && format.value().equals(extension);
				}
			};
			parser = (Parser<MarkupNode>) Service.getProvider(Parser.class, parserPredicate);
		} else {
			parser = (Parser<MarkupNode>) Service.getProvider(Parser.class, parserIdentifier);
		}
		try {
			this.root = Document.loadElements(new InputStreamReader(instanceClass.getClassLoader().getResourceAsStream(resource)), parser).root;
		} catch (ParsingException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

}
