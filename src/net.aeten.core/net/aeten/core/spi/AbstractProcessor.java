package net.aeten.core.spi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;

public abstract class AbstractProcessor extends javax.annotation.processing.AbstractProcessor {

	public static enum WriteMode {

		CREATE,
		APPEND,
		OVERRIDE
	}

	protected volatile boolean debug = false;

	public static PrintWriter getWriter(FileObject fileObject, WriteMode mode, boolean autoFlush) throws IllegalArgumentException, IOException {
		try {
			File file = getFile(fileObject);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
			}
			if (file.exists() && !file.isDirectory()) {
				switch (mode) {
				case CREATE:
					throw new IOException("File \"" + file + "\" already exist");
				case OVERRIDE:
					file.delete();
					break;
				case APPEND:
				default:
					break;
				}
			}

			if (mode == WriteMode.APPEND) { return new PrintWriter(new FileWriter(file, true)); }
			return new PrintWriter(fileObject.openOutputStream(), autoFlush);
		} catch (UnsupportedOperationException exception) {
			return new PrintWriter(fileObject.openOutputStream(), autoFlush);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(fileObject.toString(), exception);
		}
	}

	public static BufferedReader getReader(FileObject fileObject) throws FileNotFoundException, IllegalArgumentException, IOException {
		return new BufferedReader(new FileReader(getFile(fileObject)));
	}

	protected static File getFile(FileObject fileObject) {
		URI uri = fileObject.toUri();
		if (uri.isAbsolute()) { return new File(uri); }
		return new File(uri.toString());
	}

	protected AnnotationMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass) {
		for (AnnotationMirror annotation: processingEnv.getElementUtils().getAllAnnotationMirrors(element)) {
			if (annotationMirrorMatches(annotation, annotationClass)) { return annotation; }
		}
		return null;
	}

	protected static boolean annotationMirrorMatches(TypeMirror annotation, Class<? extends Annotation> annotationClass) {
		if (annotation instanceof AnnotationMirror) {
			return annotationMirrorMatches((AnnotationMirror) annotation, annotationClass);
		} else {
			return annotationMirrorMatches((DeclaredType) annotation, annotationClass);
		}
	}

	protected static boolean annotationMirrorMatches(AnnotationMirror annotation, Class<? extends Annotation> annotationClass) {
		return annotationMirrorMatches(annotation.getAnnotationType(), annotationClass);
	}

	protected static boolean annotationMirrorMatches(DeclaredType annotation, Class<? extends Annotation> annotationClass) {
		Name qualifiedName = ((TypeElement) (annotation).asElement()).getQualifiedName();
		return qualifiedName.contentEquals(annotationClass.getName());
	}

	protected static TypeElement toElement(AnnotationValue value) {
		return (TypeElement) ((DeclaredType) ((TypeMirror) value.getValue())).asElement();
	}

	protected List<AnnotationMirror> getAnnotationMirrors(Element element, Class<? extends Annotation> annotationClass) {
		List<AnnotationMirror> annotationMirrors = new ArrayList<>();
		for (AnnotationMirror annotation: processingEnv.getElementUtils().getAllAnnotationMirrors(element)) {
			if (annotationMirrorMatches(annotation, annotationClass)) {
				annotationMirrors.add(annotation);
			}
		}
		return annotationMirrors;
	}

	protected static Collection<AnnotationValue> findValue(AnnotationMirror mirror, String key) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: elementValues.entrySet()) {
			if (entry.getKey().getSimpleName().contentEquals(key)) {
				@SuppressWarnings("unchecked")
				Collection<AnnotationValue> result = (Collection<AnnotationValue>) entry.getValue().getValue();
				return result;
			}
		}
		throw new IllegalStateException("No value found in element");
	}

	protected static Collection<AnnotationValue> findValue(AnnotationMirror annotation) {
		return findValue(annotation, "value");
	}

	protected AnnotationValue getAnnotationValue(AnnotationMirror annotation, String key) {
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: processingEnv.getElementUtils().getElementValuesWithDefaults(annotation).entrySet()) {
			if (entry.getKey().getSimpleName().contentEquals(key)) { return entry.getValue(); }
		}
		return null;
	}

	protected AnnotationValue getAnnotationValue(AnnotationMirror annotation) {
		return getAnnotationValue(annotation, "value");
	}

	protected AnnotationValue getAnnotationValue(Element element, Class<? extends Annotation> annotation, String key) {
		for (AnnotationMirror confAnnotation: getAnnotationMirrors(element, annotation)) {
			for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> value: confAnnotation.getElementValues().entrySet()) {
				if (value.getKey().getSimpleName().contentEquals(key)) { return value.getValue(); }
			}
		}
		return null;
	}

	protected AnnotationValue getAnnotationValue(Element element, Class<? extends Annotation> annotation) {
		return getAnnotationValue(element, annotation, "value");
	}

	protected String getProperQualifiedName(TypeElement provider) {
		return processingEnv.getElementUtils().getBinaryName(provider).toString();
	}

	protected void debug(String message) {
		if (debug) processingEnv.getMessager().printMessage(Kind.NOTE, message);
	}

	protected void note(String message) {
		processingEnv.getMessager().printMessage(Kind.NOTE, message);
	}

	protected void warn(String message, Element element, AnnotationMirror annotation, AnnotationValue value) {
		processingEnv.getMessager().printMessage(Kind.WARNING, message, element, annotation, value);
	}

	protected void warn(String message, Element element, AnnotationMirror annotation) {
		processingEnv.getMessager().printMessage(Kind.WARNING, message, element, annotation);
	}

	protected void warn(String message, Element element) {
		processingEnv.getMessager().printMessage(Kind.WARNING, message, element);
	}

	protected void error(String message, Element element, AnnotationMirror annotation, AnnotationValue value) {
		processingEnv.getMessager().printMessage(Kind.ERROR, message, element, annotation, value);
	}

	protected void error(String message, Element element, AnnotationMirror annotation) {
		processingEnv.getMessager().printMessage(Kind.ERROR, message, element, annotation);
	}

	protected void error(String message, Element element) {
		processingEnv.getMessager().printMessage(Kind.ERROR, message, element);
	}

	protected void error(String message, Throwable cause, Element element, AnnotationMirror annotation, AnnotationValue value) {
		processingEnv.getMessager().printMessage(Kind.ERROR, message + "\nCaused by " + cause + ":\n" + getStackTrace(cause), element, annotation, value);
	}

	protected void error(String message, Throwable cause, Element element, AnnotationMirror annotation) {
		processingEnv.getMessager().printMessage(Kind.ERROR, message + "\nCaused by " + cause + ":\n" + getStackTrace(cause), element, annotation);
	}

	protected void error(String message, Throwable cause, Element element) {
		processingEnv.getMessager().printMessage(Kind.ERROR, message + "\nCaused by " + cause + ":\n" + getStackTrace(cause), element);
	}

	private static String getStackTrace(Throwable cause) {
		StringWriter stackTrace = new StringWriter();
		cause.printStackTrace(new PrintWriter(stackTrace));
		return stackTrace.toString();
	}

	protected static void writeImport(PrintWriter writer, Class<?>... classes) {
		writeImport(writer, new ArrayList<String>(classes.length), classes);
	}

	protected static void writeImport(PrintWriter writer, List<String> names, Class<?>... classes) {
		for (Class<?> clazz: classes) {
			names.add(clazz.getName());
		}
		Collections.sort(names);
		for (String name: names) {
			writer.println("import " + name + ";");
		}
	}

	protected static String upperFirstChar(String field) {
		return field.substring(0, 1).toUpperCase() + field.substring(1);
	}

	protected String getType(Element element) {
		TypeMirror type = element.asType();
		switch (type.getKind()) {
		case ARRAY:
		case BOOLEAN:
			return "boolean";
		case BYTE:
			return "byte";
		case CHAR:
			return "char";
		case DOUBLE:
			return "double";
		case FLOAT:
			return "float";
		case INT:
			return "int";
		case LONG:
			return "long";
		default:
			return processingEnv.getTypeUtils().asElement(type).toString();
		}
	}

	protected TypeMirror mirror(Class<?> type, Class<?>... parameterized) {
		TypeMirror[] typeArgs = new TypeMirror[parameterized.length];
		for (int i = 0; i < parameterized.length; i++) {
			typeArgs[i] = processingEnv.getElementUtils().getTypeElement(parameterized[i].getName()).asType();
		}
		return processingEnv.getTypeUtils().getDeclaredType(processingEnv.getElementUtils().getTypeElement(type.getName()), typeArgs);
	}

	protected String getPackageOf(TypeElement element) {
		return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
	}

	protected String getClassOf(TypeElement element) {
		String elementPackage = getPackageOf(element);
		return element.getQualifiedName().toString().substring(elementPackage.isEmpty()? 0: elementPackage.length() + 1);
	}
}
