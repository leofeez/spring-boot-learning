# spring boot starter

### starter 简介
关于 spring-boot 的约定由于配置是如何实现的，主要功劳还是在于 starter 机制，starter机制也将它的设计理念体现的淋漓尽致。
在 spring-boot 项目中，starter 机制其实就是为快速开发提供了一系列的功能依赖，使开发人员不需要 copy 样版式的各种 xml 配置和样板式的  
代码配置，大大简化了项目搭建的复杂度。  

### starter 使用
在 spring-boot 项目中，通常来说，我们初始化一个 spring-boot 项目之后，需要进行 web 开发，这时我们只需要在 pom 依赖中添加，  
  
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
当然 spring-boot starter 还有很多，我们打开 ``spring-boot-starter-web`` 父级 pom 就可以看到，如 `aop`，`Thymeleaf`，`spring-data-jpa`等等。

### starter 原理
spring-boot 项目的启动入口都是项目路径下的带有 ``@SpringBootApplication`` 注解的xxxApplication.java 中的 main 方法。
在 main 方法中由 ``SpringApplication.run(xxxApplication.class, args)`` 开始，  

执行流程如下：  

1 . 通过``SpringApplication`` 构造方法实例化 SpringApplication，
```
    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
   		this.resourceLoader = resourceLoader;
   		Assert.notNull(primarySources, "PrimarySources must not be null");
   		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
   		this.webApplicationType = WebApplicationType.deduceFromClasspath();
   		// 实例化 ApplicationContextInitializer
   		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
   		// 实例化 ApplicationListener
   		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
   		this.mainApplicationClass = deduceMainApplicationClass();
   	}
```
   	
在构造方法中会去项目路径下，从 `META-INF/spring.factories` 文件中加载并实例化实现了 `ApplicationContextInitializer`，`ApplicationListener` 的配置类。
```
public final class SpringFactoriesLoader {
    private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
        ...
        Enumeration<URL> urls = classLoader != null ? classLoader.getResources("META-INF/spring.factories") : ClassLoader.getSystemResources("META-INF/spring.factories");
        ...
    }
}
```


