package net.aeten.core.spi;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import net.aeten.core.Factory;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.parsing.Document;

@Provider(Processor.class)
@SupportedAnnotationTypes({"net.aeten.core.spi.SpiInitializer", "net.aeten.core.spi.FieldInit"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FieldInitializationProcessor extends AbstractProcessor {

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.logLevel = LogLevel.DEBUG;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {		
		for (Element initializer : roundEnv.getElementsAnnotatedWith(SpiInitializer.class)) {
			String pkg = processingEnv.getElementUtils().getPackageOf(getEnclosingClass(initializer)).getQualifiedName().toString();
			String clazz = initializer.asType().toString();
			debug(FieldInitializationProcessor.class.getSimpleName() + " process " + clazz);
			try (PrintWriter writer = getWriter(processingEnv.getFiler().createSourceFile(pkg + "." + clazz, initializer), AbstractProcessor.WriteMode.CREATE, false)) {
				writer.println("package " + pkg + ";");
				writer.println();
				writeImport(writer, List.class, ArrayList.class, HashMap.class, Map.class, Generated.class, Factory.class, Document.class, FieldInitFactory.class, SpiConfiguration.class);
				writer.println();
				writer.println("@Generated(\"" + FieldInitializationProcessor.class.getName() + "\")");
				writer.println("public class " + clazz + " {");
				writer.println("	private final Map<String, Factory<Object, Void>> fieldsFactories;");
				writer.println();
				writer.println("	public " + clazz + "(" + SpiConfiguration.class.getSimpleName() + " configuration) {");
				writer.println("		fieldsFactories = new HashMap<>();");
				writer.println("		for (Document.Element element : configuration.root.asSequence()) {");
				writer.println("			final String field;");
				writer.println("			final Class<?> type;");
				writer.println("			final List<Class<?>> parameterizedTypes = new ArrayList<>();");
				writer.println("			final Document.MappingEntry entry = element.asMappingEntry();");
				writer.println("			switch (entry.getKey().asString()) {");
				Element objectElement = processingEnv.getElementUtils().getTypeElement("java.lang.Object");
				for (Element element = getEnclosingClass(initializer); !element.equals(objectElement); element = superTypeElement(element)) {
					for (Element fieldInit : getElementsAnnotatedWith(element, FieldInit.class)) {
						List<String> words = splitCamelCase(fieldInit.toString());
						writer.println("			case \"" + fieldInit.toString() + "\":");
						if (words.size() > 1) {
							writer.println("			case \"" + join(words, ' ') + "\":");
							writer.println("			case \"" + join(words, '-') + "\":");
							writer.println("			case \"" + join(words, '_') + "\":");
						}
						AnnotationValue aliasValue = getAnnotationValue(fieldInit, FieldInit.class, "alias");
						if (aliasValue != null) {
							for (Object aliasObject : (Iterable<?>) aliasValue.getValue()) {
								String alias = aliasObject.toString().replace('"', ' ').trim();
								writer.println("			case \"" + alias + "\":");
								if (alias.contains(" ")) {
									writer.println("			case \"" + alias.replace(' ', '-') + "\":");
									writer.println("			case \"" + alias.replace(' ', '_') + "\":");
								}
							}
						}
						writer.println("				field = \"" + fieldInit.toString() + "\";");
						writer.println("				type = " + getType(fieldInit) + ".class;");
						TypeMirror type = fieldInit.asType();
						if (type instanceof DeclaredType) {
							for (TypeMirror parameterized : ((DeclaredType) type).getTypeArguments()) {
								if (parameterized.getKind() == TypeKind.DECLARED) {
									String className = (parameterized instanceof DeclaredType) ? ((DeclaredType) parameterized).asElement().toString() : parameterized.toString();
									writer.println(String.format("				parameterizedTypes.add(%s.class);", className));
								}
							}
						}
						writer.println("				break;");
					}
				}
				writer.println("			default:");
				writer.println("				throw new IllegalArgumentException(String.format(\"No field named %s\", entry.getKey()));");
				writer.println("			}");

				writer.println("			fieldsFactories.put(field, FieldInitFactory.create(entry.getValue(), type, parameterizedTypes, " + clazz + ".class.getClassLoader()));");
				writer.println("		}");
				writer.println("	}");

				for (Element element = getEnclosingClass(initializer); !element.equals(objectElement); element = superTypeElement(element)) {
					for (Element fieldInit : getElementsAnnotatedWith(getEnclosingClass(element), FieldInit.class)) {
						String fildName = fieldInit.toString();
						clazz = getType(fieldInit);
						writer.println("	public " + clazz + " get" + upperFirstChar(fildName) + "() {");
						writer.println("		return (" + clazz + ") fieldsFactories.get(\"" + fildName + "\").create(null);");
						writer.println("	}");
						AnnotationValue isRequiredValue = getAnnotationValue(fieldInit, FieldInit.class, "required");
						if ((isRequiredValue != null) && ((Boolean) isRequiredValue.getValue() == false)) {
							writer.println("	public boolean has" + upperFirstChar(fildName) + "() {");
							writer.println("		return fieldsFactories.containsKey(\"" + fildName + "\");");
							writer.println("	}");
						}
					}
				}
				writer.println("}");

			} catch (Throwable exception) {
				warn("Unexpected exception " + exception.toString(), initializer);
			}
		}
		return false;
	}

	private List<String> splitCamelCase(String string) {
		List<String> result = new ArrayList<>();
		String word = "";
		for (char c : string.toCharArray()) {
			if (Character.isLowerCase(c)) {
				word += c;
			} else {
				if (!word.isEmpty()) {
					result.add(word);
				}
				word = "" + Character.toLowerCase(c);
			}
		}
		result.add(word);
		return result;
	}

	private String join(List<String> words, char separator) {
		String string = "";
		Iterator<String> iterator = words.iterator();
		while (iterator.hasNext()) {
			string += iterator.next();
			if (iterator.hasNext()) {
				string += separator;
			}
		}
		return string;
	}

	static Element getEnclosingClass(Element element) {
		while (element.getKind() != ElementKind.CLASS) {
			element = element.getEnclosingElement();
		}
		return element;
	}

	Element superTypeElement(Element element) {
		TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(element.toString());
		TypeMirror superType = processingEnv.getTypeUtils().directSupertypes(typeElement.asType()).get(0);
		return processingEnv.getTypeUtils().asElement(superType);
	}

	static List<Element> getElementsAnnotatedWith(Element classElement, Class<? extends Annotation> annotation) {
		List<Element> elements = new ArrayList<>();
		for (;classElement.getKind() != ElementKind.PACKAGE;classElement = classElement.getEnclosingElement()) {
			for (Element element : classElement.getEnclosedElements()) {
				if (element.getAnnotation(annotation) != null) {
					elements.add(element);
				}
			}
		}
		return elements;
	}
}
