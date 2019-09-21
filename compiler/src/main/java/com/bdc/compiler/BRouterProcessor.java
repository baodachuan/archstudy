package com.bdc.compiler;

import com.bdc.annotation.BRouter;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
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
import javax.lang.model.element.Modifier;
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
            BRouter bRouter = element.getAnnotation(BRouter.class);

            MethodSpec methodSpec = MethodSpec.methodBuilder("findTargetClass")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(Class.class)
                    .addParameter(String[].class, "path")
                    .addStatement("return path.equals($S)?$T.class:null",
                            bRouter.path(),
                            ClassName.get((TypeElement) element))
                    .build();

            TypeSpec typeSpec = TypeSpec.classBuilder(finalClasseNmae)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(methodSpec)
                    .build();

            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .build();

            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        return true;
    }
}
