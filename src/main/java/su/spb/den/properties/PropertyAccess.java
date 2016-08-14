package su.spb.den.properties;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author denis
 *
 */
public final class PropertyAccess {

    private static final PropertyAccess INSTANCE = new PropertyAccess();

    private static final String DOT = ".";

    public static PropertyAccess getInstance() {
        return INSTANCE;
    }

    public String getProperty(Class<?> clazz, String... propertyIds) {
        if (propertyIds.length == 0) {
            throw new IllegalStateException(
                    "At least one property ID is required");
        }
        List<String> properties = new ArrayList<>(propertyIds.length);
        Class<?> currentClass = clazz;
        for (String propertyId : propertyIds) {
            Method method = getMethod(currentClass, propertyId);
            String property = getProperty(method);
            if (property == null) {
                String error = String
                        .format("Method '%s' is annotated by @%s(\"%s\") but it's not a bean getter",
                                method.getName(),
                                BeanProperty.class.getSimpleName(), propertyId);
                Logger.getLogger(PropertyAccess.class.getName()).severe(error);
                throw new IllegalStateException(error);
            }
            properties.add(property);
            currentClass = method.getReturnType();
        }
        return properties.stream().collect(Collectors.joining(DOT));
    }

    private String getProperty(Method method) {
        String name = method.getName();
        if (name.startsWith("get")) {
            return Introspector.decapitalize(name.substring(3));
        }
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(boolean.class)) {
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
        return null;
    }

    private Method getMethod(Class<?> clazz, String propertyId) {
        Method[] allMethods = clazz.getMethods();
        List<Method> methods = Stream.of(allMethods)
                .filter(method -> hasPropertyAnnotation(method, propertyId))
                .collect(Collectors.toList());
        if (methods.size() > 1) {
            String error = String
                    .format("Several methods are annotated by @%s annotation with value '%s': %s",
                            BeanProperty.class.getSimpleName(),
                            propertyId,
                            methods.stream().map(Method::getName)
                                    .collect(Collectors.joining(", ")));
            Logger.getLogger(PropertyAccess.class.getName()).severe(error);
            throw new IllegalStateException(error);
        } else if (methods.isEmpty()) {
            String error = String
                    .format("No methods annotated by @%s annotation with value '%s' are found",
                            BeanProperty.class.getSimpleName(), propertyId);
            Logger.getLogger(PropertyAccess.class.getName()).severe(error);
            throw new IllegalStateException(error);
        }
        return methods.get(0);
    }

    private boolean hasPropertyAnnotation(Method method, String property) {
        BeanProperty annotation = method.getAnnotation(BeanProperty.class);
        if (annotation == null) {
            return false;
        }
        return Objects.equals(property, annotation.value());
    }
}
