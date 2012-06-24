package net.aeten.core.spi;

import static javax.lang.model.SourceVersion.RELEASE_7;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import net.aeten.core.ConfigurationException;
import net.aeten.core.Format;
import net.aeten.core.Predicate;
import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.aeten.core.parsing.MarkupConverter;
import net.aeten.core.parsing.MarkupNode;
import net.aeten.core.parsing.Parser;

/**
 * 
 * @author Thomas Pérennou
 */
@Provider(Processor.class)
@SupportedAnnotationTypes( { "net.aeten.core.spi.Provider", "javax.annotation.Generated", "net.aeten.core.spi.Configurations", "net.aeten.core.spi.Configuration" })
@SupportedSourceVersion(RELEASE_7)
public class AnnotatedProviderProcessor extends AbstractProcessor {

	private static final Map<String, FileObject> servicesFileObjects = Collections.synchronizedMap(new HashMap<String, FileObject>());

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.logLevel = LogLevel.DEBUG;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {


		for (Element element : roundEnv.getElementsAnnotatedWith(Configurations.class)) {
			if (element.getAnnotation(Configured.class) == null) {
				for (AnnotationMirror configurations : getAnnotationMirrors(element, Configurations.class)) {
					for (AnnotationValue v : (Iterable<AnnotationValue>) getAnnotationValue(configurations).getValue()) {
						this.initConfiguration(element, (AnnotationMirror) v.getValue());
					}
				}
			}
		}

		for (Element element : roundEnv.getElementsAnnotatedWith(Configuration.class)) {
			if (element.getAnnotation(Configured.class) == null) {
				for (AnnotationMirror configuration : getAnnotationMirrors(element, Configuration.class)) {
					this.initConfiguration(element, configuration);
				}
			}
		}

		for (Element element : roundEnv.getRootElements()) {
			if ((getAnnotationMirrors(element, Format.class).size() > 0)) {
				AnnotationValue configured = getAnnotationValue(element, Configured.class);
				if ((configured != null) && !(Boolean) configured.getValue()) {
					configure(element);
				}
			}
		}

		for (Element element : roundEnv.getElementsAnnotatedWith(Provider.class)) {
			if (((element.getAnnotation(Configuration.class) == null) || element.getAnnotation(Configured.class).value())) {
				registerProvider(element);
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private void configure(Element element) {

		AnnotationMirror configuration = getAnnotationMirror(element, Configuration.class);
		AnnotationValue nameAnnotationValue = getAnnotationValue(configuration, "name");
		String name = getClassName(nameAnnotationValue);
		String inputFormat = getFormat(nameAnnotationValue);
		String outputFormat = (String) getAnnotationValue(element, Format.class).getValue();
		TypeElement providerElement = toElement(getAnnotationValue(configuration, "provider"));
		String provider = providerElement.getQualifiedName().toString();
		String converter = (String) getAnnotationValue(configuration, "converter").getValue();
		String parser = getAnnotationValue(configuration, "parser").getValue().toString();
		List<String> services = new ArrayList<>();
		for (AnnotationMirror serviceAnnotation : getAnnotationMirrors(providerElement, Provider.class)) {
			AnnotationValue annotationValue = getAnnotationValue(serviceAnnotation);
			for (AnnotationValue value : (Iterable<AnnotationValue>) annotationValue.getValue()) {
				services.add(getProperQualifiedName(toElement(value)));
			}
		}

		debug("Configure " + name);
		String pkg = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
		PrintWriter writer = null;
		try {
			String className = name;
			debug("Configuration");
			URL file = getClass().getClassLoader().getResource("net/aeten/core/messenger/test/server.aeml");
			debug("Configuration = " + file);
			writer = getWriter(processingEnv.getFiler().createSourceFile(pkg + "." + className, element), WriteMode.CREATE, false);

			String parentProvider = toElement(getAnnotationValue(element, Configuration.class, "provider")).getQualifiedName().toString();

			try {
				writer.println("package " + pkg + ";");
				writer.println();
				writer.println("import javax.annotation.Generated;");
				writer.println("import java.io.InputStreamReader;");
				if (outputFormat == null) {
					writer.println("import java.io.IOException;");
					writer.println("import java.io.BufferedReader;");
				}
				writer.println("import " + Configuration.class.getName() + ";");
				writer.println("import " + ConfigurationException.class.getName() + ";");
				writer.println("import " + Configured.class.getName() + ";");
				writer.println("import " + Format.class.getName() + ";");
				writer.println("import " + Logger.class.getName() + ";");
				writer.println("import " + LogLevel.class.getName() + ";");
				writer.println("import " + Predicate.class.getName() + ";");
				writer.println("import " + Provider.class.getName() + ";");
				if (outputFormat != null) {
					writer.println("import " + MarkupNode.class.getName() + ";");
					writer.println("import " + MarkupConverter.class.getName() + ";");
					writer.println("import " + Parser.class.getName() + ";");
					writer.println("import " + Service.class.getName() + ";");
				}
				writer.println();

				writer.println("@Generated(\"" + AnnotatedProviderProcessor.class.getName() + "\")");
				writer.print("@" + Provider.class.getSimpleName() + ((services.size() > 1) ? "({" : "("));
				for (int i = 0; i < services.size(); i++) {
					writer.print(services.get(i) + ".class");
					if (i < services.size() - 1) {
						writer.write(", ");
					}
				}
				writer.println(((services.size() > 1) ? "})" : ")"));

				writer.println("@" + Configured.class.getSimpleName());
				writer.println("@SuppressWarnings(\"deprecation\")");
				writer.println("@" + Configuration.class.getSimpleName() + "(name=\"" + nameAnnotationValue.getValue() + "\"" + (parser.isEmpty() ? "" : (", parser=\"" + parser + "\"")) + ", provider=" + provider + ".class)");

				writer.println("public class " + className + " extends " + parentProvider + " {");

				writer.println("   public " + className + "() throws " + ConfigurationException.class.getSimpleName() + " {");
				writer.println("      super();");
				writer.println("      try {");
				writer.println("         InputStreamReader reader = new InputStreamReader(" + className + ".class.getClassLoader().getResourceAsStream(\"" + pkg.replace('.', '/') + "/" + nameAnnotationValue.getValue() + "\"));");
				if (outputFormat == null) {
					writer.println("         BufferedReader bufferedReader = new BufferedReader(reader);");
					writer.println("         String line, conf = \"\";");
					writer.println("         try {");
					writer.println("            while ((line = bufferedReader.readLine()) != null) {");
					writer.println("               conf += line + \"\\n\";");
					writer.println("            }");
					writer.println("            this.configure(conf);");
					writer.println("         } catch (IOException exception) {");
					writer.println("            throw new " + ConfigurationException.class.getSimpleName() + "(\"" + name + "\", exception);");
					writer.println("         }");
				} else {
					if (converter.isEmpty()) {
						writer.println("         Predicate<MarkupConverter> converterPredicate = new Predicate<MarkupConverter>() {");
						writer.println("            @Override");
						writer.println("            public boolean matches(MarkupConverter element) {");
						writer.println("               Format format = element.getClass().getAnnotation(Format.class);");
						writer.println("               return (format != null) && format.value().equals(\"" + outputFormat + "\");");
						writer.println("            }");
						writer.println("         };");
						writer.println("         MarkupConverter<String> converter = (MarkupConverter<String>) Service.getProvider(MarkupConverter.class, converterPredicate);");
					} else {
						writer.println("         MarkupConverter<String> converter = (MarkupConverter<String>) Service.getProvider(MarkupConverter.class, \"" + converter + "\");");
					}
					if (parser.isEmpty()) {
						writer.println("         Predicate<Parser> parserPredicate = new Predicate<Parser>() {");
						writer.println("            @Override");
						writer.println("            public boolean matches(Parser element) {");
						writer.println("               Format format = element.getClass().getAnnotation(Format.class);");
						writer.println("               return (format != null) && format.value().equals(\"" + inputFormat + "\");");
						writer.println("            }");
						writer.println("         };");
						writer.println("         Parser<MarkupNode> parser = (Parser<MarkupNode>) Service.getProvider(Parser.class, parserPredicate);");
					} else {
						writer.println("         Parser<MarkupNode> parser = (Parser<MarkupNode>) Service.getProvider(Parser.class, \"" + parser + "\");");
					}

					writer.println("         this.configure(converter.convert(reader, parser));");
				}

				writer.println("      } catch (Throwable error) {");
				writer.println("         Logger.log(this, LogLevel.ERROR, error);");
				writer.println("      }");
				writer.println("   }");

				writer.println("}");
				writer.flush();

			} finally {
				writer.close();
			}
		} catch (Throwable exception) {
			error("Unexpected exception", exception, element);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private void registerProvider(Element provider) {
		String providerClassName = getProperQualifiedName((TypeElement) provider);

		for (AnnotationMirror annotation : getAnnotationMirrors(provider, Provider.class)) {
			for (AnnotationValue value : findValue(annotation)) {
				String service = value.getValue().toString();

				try {
					FileObject fileObject;
					synchronized (servicesFileObjects) {
						fileObject = servicesFileObjects.get(service);
						if (fileObject == null) {
							try {
								fileObject = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "META-INF/services/" + service);
							} catch (Exception exception) {
								fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + service);
							}
						}
						servicesFileObjects.put(service, fileObject);
					}

					StringWriter copy = new StringWriter();
					boolean alreadyRegistered = false;
					try {

						BufferedReader reader = getReader(fileObject);
						String line;

						try {
							while ((line = reader.readLine()) != null) {
								copy.write(line + "\n");
								if (line.trim().equals(providerClassName)) {
									debug("Provider " + providerClassName + " for service " + service + " is already registered");
									alreadyRegistered = true;
								}
							}
						} finally {
							reader.close();
						}
					} catch (FileNotFoundException exception) {
						fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + service);
                                                servicesFileObjects.put(service, fileObject);
					}
					PrintWriter writer = getWriter(fileObject, WriteMode.APPEND, true);
					try {
						writer.write(copy.toString());
						if (!alreadyRegistered) {
							writer.println(providerClassName);
							note("Add provider " + providerClassName + " for service " + service);
						}
					} finally {
						writer.close();
					}
				} catch (Exception exception) {
					error("Fail to add provider " + providerClassName + " for service " + service, exception, provider);
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void initConfiguration(Element element, AnnotationMirror configuration) {
		AnnotationValue nameAnnotationValue = getAnnotationValue(configuration, "name");
		String name = getClassName(nameAnnotationValue);
		TypeElement providerElement = toElement(getAnnotationValue(configuration, "provider"));
		String format = (String) getAnnotationValue(providerElement, Format.class).getValue();
		List<String> services = new ArrayList<String>();
		for (AnnotationMirror serviceAnnotation : getAnnotationMirrors(providerElement, Provider.class)) {
			AnnotationValue annotationValue = getAnnotationValue(serviceAnnotation);
			for (AnnotationValue value : (Iterable<AnnotationValue>) annotationValue.getValue()) {
				services.add(getProperQualifiedName(toElement(value)));
			}
		}
		String provider = providerElement.getQualifiedName().toString();
		String parser = null;
		parser = (String) getAnnotationValue(configuration, "parser").getValue();
		String converter = (String) getAnnotationValue(configuration, "converter").getValue();

		debug("Initialize configuration " + name);
		String pkg = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
		PrintWriter writer = null;
		try {
			String className = name + "Tmp";
			FileObject fileObject = processingEnv.getFiler().createSourceFile(pkg + "." + className, element);
			writer = getWriter(fileObject, WriteMode.CREATE, false);

			try {
				writer.println("package " + pkg + ";");
				writer.println();
				writer.println("import javax.annotation.Generated;");
				writer.println("import " + Configuration.class.getName() + ";");
				writer.println("import " + Format.class.getName() + ";");
				writer.println("import " + Configured.class.getName() + ";");
				writer.println("import " + Provider.class.getName() + ";");
				writer.println();

				writer.println("@SuppressWarnings(\"deprecation\")");
				writer.println("@Generated(\"" + AnnotatedProviderProcessor.class.getName() + "\")");
				writer.println("@" + Configuration.class.getSimpleName() + "(name=\"" + nameAnnotationValue.getValue() + "\"" + ", provider=" + provider + ".class" + (parser.isEmpty() ? "" : (", parser=\"" + parser + "\"")) + (converter.isEmpty() ? ")" : (", converter=\"" + converter + "\")")));
				writer.println("@" + Format.class.getSimpleName() + "(\"" + format + "\")");
				writer.println("@" + Configured.class.getSimpleName() + "(false)");
				writer.print("@" + Provider.class.getSimpleName() + ((services.size() > 1) ? "({" : "("));
				for (int i = 0; i < services.size(); i++) {
					writer.print(services.get(i) + ".class");
					if (i < services.size() - 1) {
						writer.write(", ");
					}
				}
				writer.println(((services.size() > 1) ? "})" : ")"));
				writer.println("class " + className + " extends " + provider + " {}");
				writer.flush();
			} finally {
				writer.close();
			}
		} catch (Throwable exception) {
			error("Unexpected exception", exception, element);

		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private static String getClassName(AnnotationValue configurationFileNameAnnotationValue) {
		String configurationFileName = (String) configurationFileNameAnnotationValue.getValue();
		return configurationFileName.substring(0, configurationFileName.lastIndexOf('.')).replace('.', '_');
	}

	private static String getFormat(AnnotationValue configurationFileNameAnnotationValue) {
		String configurationFileName = (String) configurationFileNameAnnotationValue.getValue();
		return configurationFileName.substring(configurationFileName.lastIndexOf('.') + 1);
	}

}
