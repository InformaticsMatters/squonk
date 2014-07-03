package toolkit.services;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author simetrias
 */

@Retention(RUNTIME)
@Target({METHOD, TYPE})
@InterceptorBinding
public @interface Transactional {

    String puName();

}