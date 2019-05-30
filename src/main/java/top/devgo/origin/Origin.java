package top.devgo.origin;

import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import sun.misc.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public final class Origin {
    private Origin() {}
    private static OClassLoader oClassLoader = new OClassLoader();

    private static Map<String, Object> beans = new HashMap<>();
    private static Map<String, Class<?>> loadedClasses = new HashMap<>();

    public static void start(String... scanPackages) {
        /*
        get all classes, [see] https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection
        */
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(scanPackages)
                .setScanners(new SubTypesScanner(false)).useParallelExecutor());

        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);
        allClasses.stream().map(Class::getName).filter(c -> !c.contains("$")).forEach(Origin::init);//排除内部类 init的时候内部类已经处理了


        doProxy();

        doInject();

    }

    private static void doInject() {
        for (Class<?> v  : loadedClasses.values()) {
            for (Field d : v.getDeclaredFields()) {
                if (d.isAnnotationPresent(Solid.class)) {
                    d.setAccessible(true);
                    String solidName = d.getAnnotation(Solid.class).name();
                    String valueName = "".equals(solidName) ? getBeanName(d.getType()) : solidName;
                    try {
                        d.set(beans.get(getBeanName(v)), beans.get(valueName));
                    } catch (IllegalAccessException e) {
                        log.error("", e);
                    }
                }
            }
        }
    }

    private static void doProxy() {
        for (Class<?> cls  : loadedClasses.values()) {

            Set<String> adjustPoints = Stream.of(cls.getMethods()).filter(m -> m.isAnnotationPresent(Adjustment.class))
                    .map(Method::getName).collect(Collectors.toSet());

            if (!adjustPoints.isEmpty()){
                ProxyFactory proxyFactory = new ProxyFactory();
                proxyFactory.setSuperclass(cls);
                proxyFactory.setFilter(m -> adjustPoints.contains(m.getName()));
                Class pClass = proxyFactory.createClass();
                try {
                    Object newInstance;
                    if (cls.getName().contains("$")){
                        Class pCls= loadedClasses.get(cls.getName().split("\\$")[0]);
                        newInstance = pClass.getConstructor(pCls).newInstance(beans.get(getBeanName(pCls)));
                    } else {
                        newInstance = pClass.newInstance();
                    }
                    ((Proxy)newInstance).setHandler((self, thisMethod, proceed, args) -> {
                        String target = thisMethod.getAnnotation(Adjustment.class).target();//testComposition#adjust

                        Object adjustInstance = beans.get(target.split("#")[0]);
                        if (adjustInstance == null)
                            return proceed.invoke(self, args);

                        Method adjustMethod = adjustInstance.getClass().getMethod(target.split("#")[1], Method.class, Object.class, Object[].class);
                        return adjustMethod.invoke(adjustInstance, proceed, self, args);
                    });

                    beans.put(getBeanName(cls), newInstance);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
    }

    private static void init(String name){
        try {
            Class<?> cls = oClassLoader.loadClass(name);
            if (cls.isAnnotationPresent(Composition.class)){
                Object instance = cls.newInstance();
                beans.put(getBeanName(cls), instance);

                for (Class<?> innerCls : cls.getClasses()) {
                    loadedClasses.put(innerCls.getName(), innerCls);
                    Object childInstance = innerCls.getConstructor(cls).newInstance(instance);//非静态内部类会支持外部类的引用，因此没有无参构造方法
                    beans.put(getBeanName(innerCls), childInstance);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private static String getBeanName(Class<?> cls) {
        String name = cls.getAnnotation(Composition.class).name();
        return "".equals(name) ? cls.getSimpleName().substring(0, 1).toLowerCase() + cls.getSimpleName().substring(1) : name;
    }


    public static Object get(String name) {
        return beans.get(name);
    }

    static class OClassLoader extends ClassLoader {
        private ClassLoader parent = this.getParent();

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (loadedClasses.containsKey(name))
                return loadedClasses.get(name);

            Class c = super.findLoadedClass(name);
            if (c == null){
                log.info("parent load: "+name);
                c = parent.loadClass(name);
            }
            if (c == null){
                log.info("loadClass: "+name);
                c = findClass(name);
            }
            loadedClasses.put(name, c);
            return c;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                String path =  System.getProperty("user.dir") +"/"+ name.replace(".","/")+".class";
                byte[] bytes = getClassFileBytes(path);

//                String jarName = "/" + name.replace('.', '/') + ".class";
//                InputStream in = this.getClass().getResourceAsStream(jarName);
//                byte[] bytes = IOUtils.readFully(in, -1, true);

                return defineClass(name, bytes,0, bytes.length);
            } catch (Exception e) {
                log.error("", e);
            }
            return super.findClass(name);
        }

        private byte[] getClassFileBytes(String classFile) throws IOException {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            try (FileInputStream fis = new FileInputStream(classFile)) {
                FileChannel fileC = fis.getChannel();
                try (WritableByteChannel outC = Channels.newChannel(byteArrayOS)) {
                    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                    while (true) {
                        int i = fileC.read(buffer);
                        if (i == 0 || i == -1) {
                            break;
                        }
                        buffer.flip();
                        outC.write(buffer);
                        buffer.clear();
                    }
                }
            }
            return byteArrayOS.toByteArray();
        }
    }
}
