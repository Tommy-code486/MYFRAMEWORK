package mg.itu.tommy.annotation;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Url {
    String value() default "";
    String type() default "GET";
}
