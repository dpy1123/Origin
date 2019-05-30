package top.devgo.origin;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Composition
@Getter
@Setter
@ToString
@Slf4j
public class TestComposition {
    @Solid
    private TestBeanA testBeanA;

    public Object adjust2(Method method, Object obj, Object... args){
        log.info(String.format("[%s] before 222", this.getClass().getName()));

        Object result = null;
        try {
            result = method.invoke(obj, args);
            result = "[changed 222]"+result;
        } catch (Exception e) {
            log.error("", e);
        }
        log.info(String.format("[%s] adjust 222: %s", this.getClass().getName(), result));

        log.info(String.format("[%s] after 222", this.getClass().getName()));
        return result;
    }

    @Adjustment(target = "testComposition#adjust2")
    public Object adjust(Method method, Object obj, Object... args){
        log.info(String.format("[%s] before", this.getClass().getName()));

        Object result = null;
        try {
            result = method.invoke(obj, args);
            result = "[changed]"+result;
        } catch (Exception e) {
            log.error("", e);
        }
        log.info(String.format("[%s] adjust: %s", this.getClass().getName(), result));

        log.info(String.format("[%s] after", this.getClass().getName()));
        return result;
    }


    @Composition
    @Getter
    @Setter
    @ToString
    public class TestBeanA {
        private String name;

        @Adjustment(target = "testComposition#adjust")
        public String doSth(){
           return "original method doSth()";
        }
    }
}