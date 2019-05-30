package top.devgo.origin;

import java.lang.annotation.*;

/**
 * 调整层
 *
 * <p>标识被注册的方法需要调整，由target指定的目标方法进行调整</p>
 *
 * 注意：
 * <p>应用注解的类不能是final (由javassist的继承实现)</p>
 * <p>target指向的类需要是public的 (因为经过@Adjustment后使用的是javassist生成的子类，为了子类.getMethod能拿到目标方法)</p>
 *
 * 示例：
 * <pre>
 * public Object XXX(Method method, Object obj, Object... args) {
 *     //before
 *     Object result = method.invoke(obj, args);
 *     //after
 *     return result;
 * }
 * </pre>
 */
@Documented
@Target({ElementType.METHOD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Adjustment {

    /**
     * 指向目标方法，语法<code>compositionName#finctionName</code>
     * @return
     */
    String target();
}
