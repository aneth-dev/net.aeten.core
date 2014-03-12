package net.aeten.core.spi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
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
import static javax.lang.model.SourceVersion.RELEASE_7;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author Thomas Pérennou
 */
@Provider(Processor.class)
@SupportedAnnotationTypes({ "net.aeten.core.spi.Provider", "net.aeten.core.spi.Configurations", "net.aeten.core.spi.Configuration"})
@SupportedSourceVersion(RELEASE_7)
public class ProviderProcessor extends AbstractProcessor {

	private static final Map<String, FileObject> servicesFileObjects = Collections.synchronizedMap(new HashMap<String, FileObject>());

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.debug = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		List<Element> initializers = new ArrayList<>();
		for (Element element : roundEnv.getElementsAnnotatedWith(SpiInitializer.class)) {
			initializers.add(element);
		}

		for (Element element : roundEnv.getElementsAnnotatedWith(Configurations.class)) {
			for (AnnotationMirror configurations : getAnnotationMirrors(element, Configurations.class)) {
				for (AnnotationValue v : (Iterable<AnnotationValue>) getAnnotationValue(configurations).getValue()) {
					this.configure(element, (AnnotationMirror) v.getValue(), initializers);
				}
			}
		}

		for (Element element : roundEnv.getElementsAnnotatedWith(Configuration.class)) {
			for (AnnotationMirror configuration : getAnnotationMirrors(element, Configuration.class)) {
				this.configure(element, configuration, initializers);
			}
		}

		for (Element provider : roundEnv.getElementsAnnotatedWith(Provider.class)) {
			Element initializer = null;
			for (Element element : initializers) {
				if (element.getEnclosingElement().getEnclosingElement().equals(provider)) {
					initializer = element;
				}
			}
			if (initializer == null) {
				registerProvider(provider);
			}
		}

		return true;
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
					try (PrintWriter writer = getWriter(fileObject, WriteMode.APPEND, true)) {
						writer.write(copy.toString());
						if (!alreadyRegistered) {
							writer.println(providerClassName);
							note(ProviderProcessor.class.getSimpleName() + " add provider " + providerClassName + " for service " + service);
						}
					}
				} catch (IOException | IllegalArgumentException exception) {
					error("Fail to add provider " + providerClassName + " for service " + service, exception, provider);
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void configure(Element element, AnnotationMirror configuration, List<Element> initializers) {
		AnnotationValue nameAnnotationValue = getAnnotationValue(configuration, "name");
		String name = getClassName(nameAnnotationValue);
		String pkg = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
		
		note(ProviderProcessor.class.getName() + " creates " + pkg + "." + name);
		
		TypeElement providerElement = toElement(getAnnotationValue(configuration, "provider"));
		List<TypeElement> services = new ArrayList<>();
		for (AnnotationMirror serviceAnnotation : getAnnotationMirrors(providerElement, Provider.class)) {
			AnnotationValue annotationValue = getAnnotationValue(serviceAnnotation);
			for (AnnotationValue value : (Iterable<AnnotationValue>) annotationValue.getValue()) {
				services.add(toElement(value));
			}
		}
		String parser = (String) getAnnotationValue(configuration, "parser").getValue();

		
		try {
			FileObject fileObject = processingEnv.getFiler().createSourceFile(pkg + "." + name, element);
			try (PrintWriter writer = getWriter(fileObject, WriteMode.CREATE, false)) {
				TypeMirror initializerType = null;
				Iterator<? extends TypeMirror> thrownTypes = null;
				for (Element enclosedElement : providerElement.getEnclosedElements()) {
					if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
						ExecutableElement constructor = (ExecutableElement) enclosedElement;
						if (constructor.getParameters().size() == 1 && constructor.getParameters().get(0).getAnnotation(SpiInitializer.class) != null) {
							initializerType = constructor.getParameters().get(0).asType();
							thrownTypes = constructor.getThrownTypes().iterator();
							break;
						}
					}
				}
				if (initializerType == null) {
					error("SpiInitializer not found in " + providerElement, element);
				}
				String initializerClassName = initializerType.toString();
				int classNamePosition = initializerClassName.lastIndexOf('.');
				if (classNamePosition != -1) {
					initializerClassName = initializerClassName.substring(classNamePosition + 1);
				}

				writer.println("package " + pkg + ";");
				writer.println();
				
				List<String> toImport = new ArrayList<>();
				String providerPackage = getPackageOf(providerElement);
				String providerClassName = getClassOf(providerElement);
				if (!pkg.equals(providerPackage)) {
					toImport.add(providerPackage + "." + initializerClassName);
					// Import only root enclosing class if is inner
					String providerRootImport = getProperQualifiedName(providerElement);
					int inner = providerRootImport.indexOf('$');
					if (inner != -1) {
						providerRootImport = providerRootImport.substring(0, inner);
					}
					toImport.add(providerRootImport);
				}
 				for (TypeElement service : services) {
					if (!getPackageOf(service).equals(pkg)) {
						String serviceRootImport = getProperQualifiedName(service);
						int inner = serviceRootImport.indexOf('$');
						if (inner != -1) {
							serviceRootImport = serviceRootImport.substring(0, inner);
						}
						toImport.add(serviceRootImport);
					}
				}
 				writeImport(writer, toImport, Generated.class, Provider.class, SpiConfiguration.class);
				writer.println();
				writer.println("@Generated(\"" + ProviderProcessor.class.getName() + "\")");
				writer.print("@" + Provider.class.getSimpleName() + ((services.size() > 1) ? "({" : "("));

				for (Iterator<TypeElement> iterator = services.iterator(); iterator.hasNext();) {
					writer.print(getClassOf(iterator.next()) + ".class");
					if (iterator.hasNext()) {
						writer.write(", ");
					}
				}
				writer.println(((services.size() > 1) ? "})" : ")"));
				writer.println("public class " + name + " extends " + providerClassName + " {");
				writer.print("	public " + name + " ()");
				if (thrownTypes.hasNext()) {
					writer.print(" throws ");
				}
				while (thrownTypes.hasNext()) {
					writer.print(thrownTypes.next().toString());
					if (thrownTypes.hasNext()) {
						writer.print(", ");
					}
				}
				writer.println(" {");
				writer.print("		super(new " + initializerClassName + "(new SpiConfiguration(");
				writer.print("\"" + pkg + "\", " + "\"" + nameAnnotationValue.getValue() + "\", " + "\"" + parser + "\", " + providerClassName + ".class");
				writer.println(")));");
				writer.println("	}");
				writer.println("}");
				writer.flush();
			}
		} catch (IOException | IllegalArgumentException | Error exception) {
			error("Unexpected exception", exception, element);
		}
	}

	private static String getClassName(AnnotationValue configurationFileNameAnnotationValue) {
		String[] words = ((String) configurationFileNameAnnotationValue.getValue()).split("[-_\\.]");
		String className = "";
		for (int i = 0; i< words.length - 1; i++) {
			className += upperFirstChar(words[i].toLowerCase());
		}
		return className;
	}
}
