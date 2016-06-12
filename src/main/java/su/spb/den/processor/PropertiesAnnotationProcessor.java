package su.spb.den.processor;

import java.beans.Introspector;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 * @author denis
 */
public class PropertiesAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        Set<String> qNames = annotations.stream()
                .map(TypeElement::getQualifiedName).map(Name::toString)
                .collect(Collectors.toSet());
        qNames.removeAll(getSupportedAnnotationTypes());
        if (qNames.isEmpty()) {
            Set<? extends Element> beans = roundEnv
                    .getElementsAnnotatedWith(BeanProperties.class);
            if (!roundEnv.processingOver()) {
                generateProperties(beans);
            }
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(BeanProperties.class.getName());
    }

    private ProcessingEnvironment getProcessingEnvironment() {
        return processingEnv;
    }

    private void generateProperties(Set<? extends Element> beans) {
        beans.stream().filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast).forEach(this::generateProperties);
    }

    private void generateProperties(TypeElement element) {
        List<ExecutableElement> methods = ElementFilter
                .methodsIn(getProcessingEnvironment().getElementUtils()
                        .getAllMembers(element));
        PackageElement pkg = getProcessingEnvironment().getElementUtils()
                .getPackageOf(element);
        StringBuilder propertiesClassName = new StringBuilder(
                pkg.getQualifiedName());
        propertiesClassName.append('.');
        propertiesClassName.append(element.getSimpleName());
        propertiesClassName.append("Properties");

        try {
            generateClass(element, methods, propertiesClassName.toString());
        } catch (IOException e) {
            Logger.getLogger(PropertiesAnnotationProcessor.class.getName())
                    .log(Level.SEVERE,
                            "Exception during processing properties for class "
                                    + element, e);
            getProcessingEnvironment().getMessager().printMessage(Kind.ERROR,
                    "Cannot write properties class", element);
        }
    }

    private void generateClass(TypeElement element,
            List<ExecutableElement> methods, String fqn) throws IOException {
        JavaFileObject fileObject = getProcessingEnvironment().getFiler()
                .createSourceFile(fqn, element);
        List<String> properties = new ArrayList<>();
        for (ExecutableElement method : methods) {
            if (method.getParameters().isEmpty()
                    && method.getReturnType().getKind() != TypeKind.VOID) {
                String property = getProperty(method);
                if (property != null) {
                    properties.add(property);
                }
            }
        }
        String name = fqn.substring(fqn.lastIndexOf('.') + 1);
        BufferedWriter writer = new BufferedWriter(fileObject.openWriter());
        try {
            writer.append("package ");
            writer.append(getProcessingEnvironment().getElementUtils()
                    .getPackageOf(element).getQualifiedName());
            writer.append(";\n\npublic interface ");
            writer.append(name);
            writer.append(" {\n\n");
            for (String property : properties) {
                writer.append("    String ");
                writer.append(getConstantName(property));
                writer.append(" = ");
                writer.append('"');
                writer.append(property);
                writer.append('"');
                writer.append(";\n");
            }
            writer.append("\n}");
        } finally {
            writer.close();
        }
    }

    private String getConstantName(String property) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < property.length(); i++) {
            char letter = property.charAt(i);
            if (Character.isUpperCase(letter)) {
                builder.append('_');
            }
            builder.append(letter);
        }
        return builder.toString().toUpperCase(Locale.ENGLISH);
    }

    private String getProperty(ExecutableElement method) {
        String name = method.getSimpleName().toString();
        if (method.getEnclosingElement().equals(
                getProcessingEnvironment().getElementUtils().getTypeElement(
                        Object.class.getName()))) {
            return null;
        }
        if (name.startsWith("get")) {
            return Introspector.decapitalize(name.substring(3));
        } else {
            TypeMirror returnType = method.getReturnType();
            TypeElement booleanType = getProcessingEnvironment()
                    .getElementUtils().getTypeElement(Boolean.class.getName());
            if (returnType.getKind().equals(TypeKind.BOOLEAN)
                    || booleanType.asType().equals(returnType)) {
                if (name.startsWith("is")) {
                    name = name.substring(2);
                } else if (name.startsWith("has")) {
                    name = name.substring(3);
                } else {
                    name = null;
                }
                if (name != null) {
                    return Introspector.decapitalize(name);
                }
            }
        }
        return null;
    }
}
