package top.devgo.origin;

import java.lang.annotation.*;

/**
 * 固态层
 *
 * <p>被标识的成员变量，由Origin进行赋值</p>
 */
@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Solid {

    /**
     * 要注入的实例的名字
     * @return
     */
    String name() default "";
}
