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

1 . 通过``SpringApplication`` 构造方法实例化 SpringApplication
```
    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
   		......
   		// 1. 确定上下文类型(如 web 项目 则类型为 SERVLET)
   		this.webApplicationType = WebApplicationType.deduceFromClasspath();
   		
   		// 2. 实例化 ApplicationContextInitializer
   		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
   		
   		// 3. 实例化 ApplicationListener
   		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
        ......
   	}
```
   	
构造方法中会通过`SpringFactoriesLoader`去项目路径下，从 `META-INF/spring.factories` 文件中加载并实例化实现了 `ApplicationContextInitializer`，`ApplicationListener` 的配置类。
```
public final class SpringFactoriesLoader {
    private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
        ...
        Enumeration<URL> urls = classLoader != null ? classLoader.getResources("META-INF/spring.factories") : ClassLoader.getSystemResources("META-INF/spring.factories");
        ...
    }
}
```
实例化 `SpringApplication` 后开始执行 `run` 方法。

2 . 执行 `run` 方法
 
```
public ConfigurableApplicationContext run(String... args) {
    ...
    
    // 1. 实例化 SpringApplicationRunListener
    SpringApplicationRunListeners listeners = getRunListeners(args);
    
    // 2. 启动 SpringApplicationRunListener
    listeners.starting();
    try {
        ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        // 3. 初始化环境参数
        ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
        configureIgnoreBeanInfo(environment);
        Banner printedBanner = printBanner(environment);
        
        // 4. 创建容器 ApplicationContext, 默认创建 AnnotationConfigApplicationContext
        context = createApplicationContext();
        exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
                new Class[] { ConfigurableApplicationContext.class }, context);
                
        // 5. 初始化容器
        prepareContext(context, environment, listeners, applicationArguments, printedBanner);
        
        // 6. 刷新容器 ，调用 AbstractApplicationContext.refresh() 方法
        //    实例化各种 bean
        refreshContext(context);
        
        // 7. 刷新结束(模板方法)
        afterRefresh(context, applicationArguments);
        stopWatch.stop();
        if (this.logStartupInfo) {
            new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
        }
        // 8. SpringApplicationRunListener 启动
        listeners.started(context);
        callRunners(context, applicationArguments);
    }
    catch (Throwable ex) {
        handleRunFailure(context, ex, exceptionReporters, listeners);
        throw new IllegalStateException(ex);
    }

    try {
    
        // 9. SpringApplicationRunListener 运行
        listeners.running(context);
    }
    catch (Throwable ex) {
        handleRunFailure(context, ex, exceptionReporters, null);
        throw new IllegalStateException(ex);
    }
    return context;
}

```

## 自定义一个Starter

1.新建一个maven项目，引入Spring-boot的starter
```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <artifactId>spring-boot-learning-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>spring-boot-learning-starter</name>
    <description>Demo project for Spring Boot</description>
    <packaging>jar</packaging>

    <parent>
        <groupId>com.leofee</groupId>
        <artifactId>spring-boot-learning</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
    </dependencies>

</project>

```

2.在resources下新建META-INF目录并添加spring.factories文件，文件内容如下：
```java
# 代表自己需要自动装配的配置类的全路径
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.leofee.starter.configuration.MyStarterAutoConfiguration
```

3. 用maven打包到本地仓库
