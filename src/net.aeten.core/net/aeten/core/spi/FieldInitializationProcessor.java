package net.aeten.core.spi;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
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
import net.aeten.core.Factory;

import net.aeten.core.logging.LogLevel;
import net.aeten.core.parsing.Document;

@Provider(Processor.class)
@SupportedAnnotationTypes({ "net.aeten.core.spi.SpiInitializer", "net.aeten.core.spi.FieldInit" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FieldInitializationProcessor extends AbstractProcessor {

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.logLevel = LogLevel.DEBUG;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		debug("Process FieldInitializationProcessor");
		for (Element initializer : roundEnv.getElementsAnnotatedWith(SpiInitializer.class)) {
			processingEnv.getElementUtils().getPackageOf(initializer).getQualifiedName().toString();
			String pkg = processingEnv.getElementUtils().getPackageOf(initializer).getQualifiedName().toString();
			String clazz = initializer.asType().toString();
			debug("Process " + initializer.toString());
			try (PrintWriter writer = getWriter(processingEnv.getFiler().createSourceFile(pkg + "." + clazz, initializer), AbstractProcessor.WriteMode.CREATE, false)) {
				writer.println("package " + pkg + ";");
				writer.println();
				writeImport(writer, HashMap.class, Map.class, Generated.class, Factory.class, Document.class, FieldInitFactory.class, SpiConfiguration.class);
				writer.println();
				writer.println("@Generated(\"" + FieldInitializationProcessor.class.getName() + "\")");
				writer.println("public class " + clazz + " {");
				writer.println("	private final Map<String, Factory<Object, Void>> fieldsFactories;");
				writer.println();
				writer.println("	public " + clazz + "(" + SpiConfiguration.class.getSimpleName() + " configuration) {");
				writer.println("		fieldsFactories = new HashMap<>();");
				writer.println("		for (Document.Tag tag : configuration.root.tags) {");
				writer.println("			final String field;");
				writer.println("			final Class<?> type;");
				writer.println("			switch (tag.name) {");
				for (Element fieldInit : roundEnv.getElementsAnnotatedWith(FieldInit.class)) {
					AnnotationValue aliasValue = getAnnotationValue(fieldInit, FieldInit.class, "alias");
					if (aliasValue != null) {
						for (String alias : (String[]) aliasValue.getValue()) {
							writer.println("			case \"" + alias + "\":");
						}
					} else {
						writer.println("			case \"" + fieldInit.toString() + "\":");
					}
					writer.println("				field = tag.name;");
					writer.println("				type = " + getFullName(fieldInit) + ".class;");
					writer.println("				break;");
				}
				writer.println("			default:");
				writer.println("				throw new IllegalArgumentException(\"No field \" + tag);");
				writer.println("			}");
				writer.println("			fieldsFactories.put(field, FieldInitFactory.create(tag, type, SpiTestInitializer.class.getClassLoader()));");
				writer.println("		}");


				writer.println("	}");

				for (Element fieldInit : roundEnv.getElementsAnnotatedWith(FieldInit.class)) {
					String fildName = fieldInit.toString();
					if ("configuration".equals(fildName)) {
						error(String.format("Reserved field name for %s annotated fields", FieldInit.class), fieldInit);
					}
					// Find class element
					Element parent;
					for (parent = initializer.getEnclosingElement(); parent.getKind() != ElementKind.CLASS; parent = parent.getEnclosingElement()) {}
					if (fieldInit.getEnclosingElement().equals(parent)) {
						clazz = getFullName(fieldInit);
						writer.println("	public " + clazz + " get" + upperFirstChar(fildName) + "() {");
						writer.println("		return (" + clazz + ") fieldsFactories.get(\"" + fildName + "\").create(null);");
						writer.println("	}");
						AnnotationValue isRequiredValue = getAnnotationValue(fieldInit, FieldInit.class, "required");
						if (!((isRequiredValue != null) && ((Boolean) isRequiredValue.getValue() == true))) {
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

}
