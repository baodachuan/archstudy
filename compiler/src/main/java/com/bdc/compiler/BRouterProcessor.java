package com.bdc.compiler;

import com.bdc.annotation.BRouter;
import com.bdc.annotation.RouterBean;
import com.bdc.compiler.utils.Constants;
import com.bdc.compiler.utils.Utils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


@AutoService(Processor.class)
@SupportedAnnotationTypes({Constants.BROUTER_ANNOTATION_TYPES})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({Constants.MOUDLER_NAME, Constants.APT_PAKCAGE})
public class BRouterProcessor extends AbstractProcessor {
    private Elements elementsUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;

    private String moduleName;
    private String packageNameforAPT;

    private Map<String, List<RouterBean>> tempPathMap = new HashMap<>();
    private Map<String, String> tempGroupMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementsUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        Map<String, String> options = processingEnvironment.getOptions();
        if (options != null) {
            moduleName = options.get(Constants.MOUDLER_NAME);
            packageNameforAPT = options.get(Constants.APT_PAKCAGE);
            messager.printMessage(Diagnostic.Kind.NOTE, moduleName);
            messager.printMessage(Diagnostic.Kind.NOTE, packageNameforAPT);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return false;
        }
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BRouter.class);
        if (!Utils.isEmpty(elements)) {
            parseElements(elements);
        }

        return true;
    }

    private void parseElements(Set<? extends Element> elements) {
        TypeElement activityType = elementsUtils.getTypeElement(Constants.ACTIVITY);
        TypeMirror activityMirror = activityType.asType();

        // 解析所有被注解的类，封装成bean对象。
        for (Element element : elements) {
            TypeMirror elementMirror = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE, "element info is " + elementMirror.toString());
            BRouter bRouter = element.getAnnotation(BRouter.class);

            RouterBean bean = new RouterBean.Builder()
                    .setGroup(bRouter.group())
                    .setPath(bRouter.path())
                    .setElement(element)
                    .build();

            if (typeUtils.isSubtype(elementMirror, activityMirror)) {
                bean.setType(RouterBean.Type.ACITIVTY);
            } else {
                throw new RuntimeException("only annotation with activity");
            }
            valueOfPathMap(bean);
        }
        //获取两个接口类型
        TypeElement pathElement = elementsUtils.getTypeElement(Constants.ROUTE_PATH);
        TypeElement groupElement = elementsUtils.getTypeElement(Constants.ROUTE_GROUP);

        //生成两个接口的实现类，
        // Path实现类对应于ARouter里的Group,Group实现类对应于ARouter里的Root
        generatePathFile(pathElement);
        generateGroupFile(groupElement, pathElement);
    }

    private void generateGroupFile(TypeElement groupElement, TypeElement pathElement) {
        if (Utils.isEmpty(tempGroupMap) || Utils.isEmpty(tempPathMap)) {
            return;
        }
        //构建方法返回类型
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathElement))));

        // 构建方法体标题
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturns);

        //构建方法内容
           //Map<String, Class<? extends IRouteLoadPath>> groupMap = new HashMap<>();
        methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathElement))),
                Constants.GROUP_PARAMETER_NAME,
                ClassName.get(HashMap.class));

          //groupMap.put("app",BRouter$$Path$$app.class);
        for (Map.Entry<String, String> entry : tempGroupMap.entrySet()) {
            methodBuilder.addStatement("$N.put($S, $T.class)",
                    Constants.GROUP_PARAMETER_NAME,
                    entry.getKey(),
                    ClassName.get(packageNameforAPT, entry.getValue()));
        }
          //return groupMap;
        methodBuilder.addStatement("return $N", Constants.GROUP_PARAMETER_NAME);

        // 生成类名
        String finalGroupName = Constants.GROUP_FILE_NAME + moduleName;
        messager.printMessage(Diagnostic.Kind.NOTE, " finalGroupName: " + finalGroupName);
        // 生成类体
        TypeSpec typeSpec = TypeSpec.classBuilder(finalGroupName)
                .addSuperinterface(ClassName.get(groupElement))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodBuilder.build())
                .build();

        // 写入文件
        try {
            JavaFile.builder(packageNameforAPT, typeSpec)
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void generatePathFile(TypeElement pathElement) {
        if (Utils.isEmpty(tempPathMap)) {
            return;
        }

        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class));
        for (Map.Entry<String, List<RouterBean>> entry : tempPathMap.entrySet()) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturns);


//            Map<String, RouterBean> pathMap=new HashMap<>();
            methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    Constants.PATH_PARAMETER_NAME,
                    ClassName.get(HashMap.class)
            );

            List<RouterBean> value = entry.getValue();
//            pathMap.put("/app/MainActivity",
//                    RouterBean.create(RouterBean.Type.ACITIVTY,
//                            MainActivity.class,
//                            "/app/MainActivity",
//                            "app"
//                    ));
            for (RouterBean bean : value) {
                methodBuilder.addStatement("$N.put($S,$T.create($T.$L, $T.class, $S, $S))",
                        Constants.PATH_PARAMETER_NAME,
                        bean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.Type.class),
                        bean.getType(),
                        ClassName.get((TypeElement) bean.getElement()),
                        bean.getPath(),
                        bean.getGroup());
            }

            methodBuilder.addStatement("return $N", Constants.PATH_PARAMETER_NAME);

            String finalClassName = Constants.PATH_FILE_NAME + entry.getKey();
            messager.printMessage(Diagnostic.Kind.NOTE, "file class name: " + finalClassName);

            try {
                JavaFile.builder(packageNameforAPT,
                        TypeSpec.classBuilder(finalClassName)
                                .addSuperinterface(ClassName.get(pathElement))
                                .addModifiers(Modifier.PUBLIC)
                                .addMethod(methodBuilder.build())
                                .build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            tempGroupMap.put(entry.getKey(), finalClassName);
        }

    }

    //将bean添加到map中
    private void valueOfPathMap(RouterBean bean) {
        if (checkRouterPath(bean)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "bean is " + bean.toString());
            List<RouterBean> routerBeans = tempPathMap.get(bean.getGroup());
            if (Utils.isEmpty(routerBeans)) {
                routerBeans = new ArrayList<>();
                routerBeans.add(bean);
                tempPathMap.put(bean.getGroup(), routerBeans);
            } else {
                routerBeans.add(bean);
            }
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "router annotation is not normal!");
        }

    }

    //检查注解是否规范
    private boolean checkRouterPath(RouterBean bean) {
        String group = bean.getGroup();
        String path = bean.getPath();
        if (Utils.isEmpty(path) || !path.startsWith("/") || path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.NOTE, "annotation style is not normal");
            return false;
        }
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (finalGroup.contains("/")) {
            messager.printMessage(Diagnostic.Kind.NOTE, "annotation style is not normal");
            return false;
        }

        if (!Utils.isEmpty(group) && !group.equals(moduleName)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "group name must be right");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }
        return true;
    }
}
