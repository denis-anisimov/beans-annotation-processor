package su.spb.den.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author denis
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface BeanProperties {

}
