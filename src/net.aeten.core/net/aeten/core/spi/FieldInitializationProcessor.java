package net.aeten.core.spi;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor8;

import net.aeten.core.Factory;
import net.aeten.core.parsing.Document;

@Provider(Processor.class)
@SupportedAnnotationTypes({ "net.aeten.core.spi.SpiConstructor", "net.aeten.core.spi.FieldInit" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class FieldInitializationProcessor extends AbstractProcessor {

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		debug = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element spiConstructor: roundEnv.getElementsAnnotatedWith(SpiConstructor.class)) {
			Element enclosingClass = getEnclosingClass(spiConstructor);
			AnnotationValue generate = getAnnotationValue(getAnnotationMirror(spiConstructor, SpiConstructor.class), "generate");
			String initializerClass = ((ExecutableElement)spiConstructor).getParameters().get(0).asType().toString();
			if (!((Boolean) generate.getValue())) {
				debug(FieldInitializationProcessor.class.getSimpleName() + " pass " + initializerClass);
				continue;
			}
			String pkg = processingEnv.getElementUtils().getPackageOf(enclosingClass).getQualifiedName().toString();
			/*
			 * Tweak: when Class is already generated, clazz is
			 * {package.name}.{ClassName}
			 */
			if (!pkg.isEmpty() && initializerClass.startsWith(pkg)) {
				continue;
			}

			debug(FieldInitializationProcessor.class.getSimpleName() + " process " + initializerClass);
			try (PrintWriter writer = getWriter(processingEnv.getFiler().createSourceFile(initializerClass.startsWith(pkg)? initializerClass: pkg + "." + initializerClass, spiConstructor), AbstractProcessor.WriteMode.OVERRIDE, false)) {
				writer.println("package " + pkg + ";");
				writer.println();
				writeImport(writer, List.class, ArrayList.class, HashMap.class, Map.class, Generated.class, Factory.class, Document.class, FieldInitFactory.class, SpiConfiguration.class);
				writer.println();
				writer.println("@Generated(\"" + FieldInitializationProcessor.class.getName() + "\")");
				writer.println("public class " + initializerClass + " {");
				writer.println("	private final Map<String, Factory<Object, Void>> fieldsFactories;");
				writer.println();
				writer.println("	public " + initializerClass + "(" + SpiConfiguration.class.getSimpleName() + " configuration) {");
				writer.println("		fieldsFactories = new HashMap<>();");
				writer.println("		for (Document.Element element : configuration.root.asSequence()) {");
				writer.println("			final String field;");
				writer.println("			final Class<?> type;");
				writer.println("			final List<Class<?>> parameterizedTypes = new ArrayList<>();");
				writer.println("			final Document.MappingEntry entry = element.asMappingEntry();");
				writer.println("			switch (entry.getKey().asString()) {");
				Element objectElement = processingEnv.getElementUtils().getTypeElement("java.lang.Object");
				for (Element element = enclosingClass; !element.equals(objectElement); element = superTypeElement(element)) {
					for (Element fieldInit: getElementsAnnotatedWith(element, FieldInit.class)) {
						List<String> words = splitCamelCase(fieldInit.toString());
						writer.println("			case \"" + fieldInit.toString() + "\":");
						if (words.size() > 1) {
							writer.println("			case \"" + join(words, ' ') + "\":");
							writer.println("			case \"" + join(words, '-') + "\":");
							writer.println("			case \"" + join(words, '_') + "\":");
						}
						AnnotationValue aliasValue = getAnnotationValue(fieldInit, FieldInit.class, "alias");
						if (aliasValue != null) {
							for (Object aliasObject: (Iterable<?>) aliasValue.getValue()) {
								String alias = aliasObject.toString().replace('"', ' ').trim();
								writer.println("			case \"" + alias + "\":");
								if (alias.contains(" ")) {
									writer.println("			case \"" + alias.replace(' ', '-') + "\":");
									writer.println("			case \"" + alias.replace(' ', '_') + "\":");
								}
							}
						}

						String typeName = getType(fieldInit);
						TypeMirror type = fieldInit.asType();
						List<TypeMirror> parameterizedTypes;
						if (type instanceof DeclaredType) {
							parameterizedTypes = (List<TypeMirror>) ((DeclaredType) type).getTypeArguments();
							if (typeName.equals(AtomicReference.class.getName())) {
								type = parameterizedTypes.get(0);
								typeName = type.toString();
								parameterizedTypes = new ArrayList<>();
								if (type instanceof DeclaredType) {
									for (TypeMirror parameterizedType: ((DeclaredType) type).getTypeArguments()) {
										parameterizedTypes.add(parameterizedType);
									}
								}
							}
						} else {
							parameterizedTypes = Collections.emptyList();
						}

						writer.println("				field = \"" + fieldInit.toString() + "\";");
						writer.println("				type = " + typeName + ".class;");
						if (type instanceof DeclaredType) {
							for (TypeMirror parameterized: parameterizedTypes) {
								if (parameterized.getKind() == TypeKind.DECLARED) {
									String className = (parameterized instanceof DeclaredType)? ((DeclaredType) parameterized).asElement().toString(): parameterized.toString();
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

				writer.println("			fieldsFactories.put(field, FieldInitFactory.create(entry.getValue(), type, parameterizedTypes, " + initializerClass + ".class.getClassLoader()));");
				writer.println("		}");
				writer.println("	}");

				for (Element element = enclosingClass; !element.equals(objectElement); element = superTypeElement(element)) {
					for (Element fieldInit: getElementsAnnotatedWith(getEnclosingClass(element), FieldInit.class)) {
						String fildName = fieldInit.toString();
						String typeName = getType(fieldInit);
						TypeMirror type = fieldInit.asType();
						List<TypeMirror> parameterizedTypes;
						if (type instanceof DeclaredType) {
							parameterizedTypes = (List<TypeMirror>) ((DeclaredType) type).getTypeArguments();
							if (typeName.equals(AtomicReference.class.getName())) {
								type = parameterizedTypes.get(0);
								typeName = type.toString();
								parameterizedTypes = new ArrayList<>();
								if (type instanceof DeclaredType) {
									for (TypeMirror parameterizedType: ((DeclaredType) type).getTypeArguments()) {
										parameterizedTypes.add(parameterizedType);
									}
								}
							}
						} else {
							parameterizedTypes = Collections.emptyList();
						}

						// TODO Add parameterized types and manage wildcards for non Type
//						if (!parameterizedTypes.isEmpty()) {
//							typeName += "<";
//							for (Iterator<TypeMirror> it = parameterizedTypes.iterator(); it.hasNext();) {
//								TypeMirror parameterizedType;
//								typeName += it.next();
//								if (it.hasNext()) {
//									typeName += ", ";
//								}
//							}
//							typeName += ">";
//						}

						writer.println("	public " + typeName + " get" + upperFirstChar(fildName) + "() {");
						writer.println("		return (" + typeName + ") fieldsFactories.get(\"" + fildName + "\").create(null);");
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
				warn("Unexpected exception " + exception.toString(), spiConstructor);
			}
		}
		return true;
	}

	private List<String> splitCamelCase(String string) {
		List<String> result = new ArrayList<>();
		String word = "";
		for (char c: string.toCharArray()) {
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

	List<Element> getElementsAnnotatedWith(Element classElement, final Class<? extends Annotation> annotation) {
		final List<Element> elements = new ArrayList<>();
		for (; classElement.getKind() != ElementKind.PACKAGE; classElement = classElement.getEnclosingElement()) {
			for (Element element: classElement.getEnclosedElements()) {
//				if (element.getKind() == ElementKind.CONSTRUCTOR) {
//					final ExecutableElement constructor = element;
//					constructor.asType().accept(new AbstractTypeVisitor<Boolean, Void>() {
//						@Override
//						public Boolean visitExecutable(ExecutableType t, Void p) {
//							for (TypeMirror param: t.getParameterTypes()) {								
//								if (param.getKind() == TypeKind.ERROR) {
//									elements.add(((ErrorType)param).asElement());
//								}
//							}
//							return null;
//						}
//					}, null);
//				} else
					if (element.getAnnotation(annotation) != null) {
					elements.add(element);
				}
			}
		}
		return elements;
	}
}
class AbstractTypeVisitor<R, P> extends AbstractTypeVisitor8<R, P> {

	@Override
	public R visitPrimitive(PrimitiveType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitNull(NullType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitArray(ArrayType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitDeclared(DeclaredType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitError(ErrorType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitTypeVariable(TypeVariable t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitWildcard(WildcardType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitExecutable(ExecutableType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitNoType(NoType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitIntersection(IntersectionType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitUnion(UnionType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
