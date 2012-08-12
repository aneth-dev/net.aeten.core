package net.aeten.core.spi;

import java.io.InputStreamReader;
import net.aeten.core.Format;
import net.aeten.core.Predicate;
import net.aeten.core.parsing.Document;
import net.aeten.core.parsing.MarkupNode;
import net.aeten.core.parsing.Parser;

/**
 *
 * @author Thomas Pérennou
 */
public class SpiConfiguration {
	public final Document.Entry root;

	public SpiConfiguration(Document.Entry configuration) {
		this.root = configuration;
	}

	public SpiConfiguration(String packageName, String resourceName, String parserIdentifier, Class<?> instanceClass) {
		String resource = packageName.replace('.', '/') + "/" + resourceName;
		Parser<MarkupNode> parser;
		if (parserIdentifier.isEmpty()) {
			int extensionIndex = resourceName.lastIndexOf('.');
			if ((extensionIndex == -1) || (extensionIndex == (resourceName.length() + 1))) {
				throw new IllegalArgumentException("Unable to find parser without extension in resource file " + resourceName);
			}
			final String extension = resourceName.substring(extensionIndex+1);
			Predicate<Parser> parserPredicate = new Predicate<Parser>() {
				@Override
				public boolean evaluate(Parser element) {
					Format format = element.getClass().getAnnotation(Format.class);
					return (format != null) && format.value().equals(extension);
				}
			};
			parser = (Parser<MarkupNode>) Service.getProvider(Parser.class, parserPredicate);
		} else {
			parser = (Parser<MarkupNode>) Service.getProvider(Parser.class, parserIdentifier);
		}
		this.root = Document.load(new InputStreamReader(instanceClass.getClassLoader().getResourceAsStream(resource)), parser).root;

	}

}
