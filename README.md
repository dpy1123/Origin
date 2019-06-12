# Origin

简单实现运行时AOP，依托了javassist的动态代理。

定义了@Composition @Solid @Adjustment 三种注解，名称取自AE，各自的用法和限制见注释。  


#### why not spring ?
* 轻量
* 没有著名的"this调用事务不生效"问题，使用更符合直觉


#### usage
测试入口是test包下的OriginTest#main  


欢迎PR！