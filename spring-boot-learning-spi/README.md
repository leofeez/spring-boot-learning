# spring-boot-SPI

## 理念

关于面向接口编程我们应该不会陌生，给我们带来最大的好处就是:
- 灵活性和可复用性
- 良好的扩展性
- 低耦合

而 SPI 机制则很好的体现出了面向接口编程的理念。

## 简介

`SPI，Service Provider Interface`，是 Java 提供的一套用来被第三方实现或者扩展的API，它可以用来启用框架扩展和替换组件。
SPI 的机制能够很好的实现组件的可插拔性，让规范与实现相分离。很多开源项目其实都用到了这个机制，如 `Spring`，`spring-boot` 等。

## 使用

### 使用 ``SPI`` 机制我们需要遵循一定的约定:
- 当服务提供者提供了接口的一种具体实现后，在jar包的 `META-INF/services`目录下创建一个以“接口全限定名”为命名的文件，内容为实现类的全限定名。
- 接口实现类所在的jar包放在主程序的 classpath 中。
- 主程序通过`java.util.ServiceLoder`动态装载实现模块，它通过扫描`META-INF/services`目录下的配置文件找到实现类的全限定名，把类加载到JVM。
- SPI的实现类必须携带一个不带参数的构造方法。

### 常见 ``SPI`` 机制的示例：
- `mysql-connector` ：mysql 数据库连接驱动。  
当我们添加 `mysql-connector-java.jar` 包依赖之后，我们打开 jar 包可以看到在包路径下有一个 `META-INF\services` 文件夹，里面有一个文件
就是以 `com.java.sql.Driver` 为名称的文件，文件内容就是对应的 mysql 数据库驱动的实现类 `com.mysql.cj.jdbc.Driver`。

- `spring-web` 中的 `SpringServletContainerInitializer`。  
在以往的 web 项目中，我们第一步就需要配置 web.xml ，配置 `servlet` ， `filter`， `listener`，但是在 servlet 3.0 中提供了新的更便捷的方式：
   
   - 基于注解 `@WebServlet` , `@WebFilter`, `@WebListener`：此处不作详细说明，见自定义 servlet [LeeServlet ]。
   - 基于 `java spi` 规范。
     
   在 spring-boot 项目中的 `DispatcherServlet` 就是通过 SPI 机制向 web 容器中注册 servlet 组件。  
   打开 spring-web 的 jar 包我们可以看到 `META-INF\services`
   目录下有一个文件为 `javax.servlet.ServletContainerInitializer` 内容为 `org.springframework.web.SpringServletContainerInitializer`，
   这个类就是 servlet 容器初始化的一个实现类，  
   
   流程如下:
   
   1 `@HandlerTypes(WebApplicationInitializer.class)` 注解:  
   该注解指定接口之后，tomcat 在启动的时候就会去查找`WebApplicationInitializer`接口的所有实现类 class 并放到 `onStartup` 方法参数的 set 集合中。
   
   2 执行 `... onStartup(Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)`：  
   ```
    /**
     *@param webAppInitializerClasses 所有实现 WebApplicationInitializer 接口的实现类 class
     */
     @Override
	 public void onStartup(@Nullable Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
			throws ServletException {

		List<WebApplicationInitializer> initializers = new LinkedList<>();

		if (webAppInitializerClasses != null) {
		
		    // 遍历并实例化实现类
			for (Class<?> waiClass : webAppInitializerClasses) {
			
				// 过滤掉 接口，抽象类，然后必须实现 WebApplicationInitializer
				if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
						WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
					try {
					    // 反射生成实现类
						initializers.add((WebApplicationInitializer)
								ReflectionUtils.accessibleConstructor(waiClass).newInstance());
					}
					catch (Throwable ex) {
						throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
					}
				}
			}
		}
        ...
		
		// 遍历执行 WebApplicationInitializer 的 onStartup 方法，
		// 如 SpringBootServletInitializer#onStartup...
		for (WebApplicationInitializer initializer : initializers) {
			initializer.onStartup(servletContext);
		}
	}

   ```
   3 执行 `SpringBootServletInitializer#onStartup(ServletContext servletContext)`:
   ```
   
    @Override
   	public void onStartup(ServletContext servletContext) throws ServletException {
   		
   		...
   		
   		// 这是重点方法
   		WebApplicationContext rootAppContext = createRootApplicationContext(servletContext);
   		
   		...
   	}
    
   ```
   
   接下来看一下 `createRootApplicationContext(servletContext)` 到底做了些什么，源码如下：
   
   ```
    protected WebApplicationContext createRootApplicationContext(ServletContext servletContext) {
    
        // 1. 创建 SpringApplication 的 builder
		SpringApplicationBuilder builder = createSpringApplicationBuilder();
	
	    ...
		
		// 2. build SpringApplication
		SpringApplication application = builder.build();
		
		...
		
		// 3. 执行 SpringApplication.run()
		return run(application);
	}

    ```
  我们可以看到熟悉的 run 方法，在这方法中会看到：
  - 创建 Spring 容器 `ConfigurableApplicationContext`
  - Spring 容器的刷新，最终会调用大名鼎鼎的 `refresh()` 方法
  - 监听器的启动



