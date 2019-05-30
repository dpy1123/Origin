package top.devgo.origin;

import java.lang.annotation.*;

/**
 * 合成层
 *
 * <p>被标识的类，会由Origin框架进行实例化</p>
 *
 *
 */
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Composition {
    /**
     * 实例化的名字，默认是class的simpleName首字母小写
     * @return
     */
    String name() default "";

}
