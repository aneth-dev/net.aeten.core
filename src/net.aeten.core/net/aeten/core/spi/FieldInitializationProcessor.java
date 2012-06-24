package net.aeten.core.spi;

import java.io.PrintWriter;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import net.aeten.core.logging.LogLevel;

@Provider(Processor.class)
@SupportedAnnotationTypes("net.aeten.core.spi.FieldInit")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FieldInitializationProcessor extends AbstractProcessor {

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.logLevel = LogLevel.DEBUG;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getElementsAnnotatedWith(FieldInit.class)) {
			String pkg = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
			String className = element.getSimpleName() + "$$super";
			PrintWriter writer = null;

			try {
//				writer = getWriter(processingEnv.getFiler().createSourceFile(pkg + "." + className, element), WriteMode.CREATE, false);
//
//				writer.println("package " + pkg + ";");
//				writer.println();
//				writer.println("import javax.annotation.Generated;");
//				writer.println("import " + FieldInit.class.getName() + ";");
//				writer.println();
//				writer.println("@Generated(\"" + FieldInitializationProcessor.class.getName() + "\")");
//				writer.println("public class " + className + " {");
//				for (AnnotationMirror fieldInit : getAnnotationMirrors(element, FieldInit.class)) {
//					writer.println("   protected abstract " + fieldInit + element + "init" + "();");
//				}
//				writer.println("}");

			} catch (Throwable exception) {
				error("Unexpected exception", exception, element);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}
		return false;
	}
}
