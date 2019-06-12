package top.devgo.origin;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OriginTest {


    public static void main(String[] args) {
        Origin.start("top.devgo.origin");

        TestComposition comp = (TestComposition) Origin.get("testComposition");
        TestComposition.TestBeanA beanA = (TestComposition.TestBeanA) Origin.get("testBeanA");
        beanA.setName("testA");

        log.info(comp.toString());
        log.info(comp.getTestBeanA().getName());
        log.info(beanA.doSth());

        log.info("-------------");
        log.info(beanA.doSth2());

        log.info("-------------");
        log.info(beanA.test());
    }


}
