package com.bdc.compiler;

import com.bdc.annotation.BRouter;
import com.google.auto.service.AutoService;

import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.bdc.annotation.BRouter"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions("content")
public class BRouterProcessor extends AbstractProcessor {
    private Elements elementsUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementsUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        String content = processingEnvironment.getOptions().get("content");
        messager.printMessage(Diagnostic.Kind.NOTE, content);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return false;
        }
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BRouter.class);
        for (Element element : elements) {
            String packageName = elementsUtils.getPackageOf(element).getQualifiedName().toString();
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "annotated class is " + className);
            String finalClasseNmae = className + "$$BRouter";
            try {
                JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + finalClasseNmae);
                Writer writer = sourceFile.openWriter();
                writer.write("package " + packageName + ";\n");
                writer.write("public class " + finalClasseNmae + "{\n");
                writer.write("public static Class<?> findTargetClass(String path) {\n");
                BRouter router = element.getAnnotation(BRouter.class);
                writer.write("if (path.equals(\"" + router.path() + "\")){\n");
                writer.write("return  " + className + ".class;\n}\n");
                writer.write("return  null;\n");
                writer.write("}\n}");
                writer.close();


            } catch (Exception e) {

            }

        }

        return true;
    }
}
