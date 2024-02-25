# spring boot Web 开发

### 2.1 首先需要引入web模块

只需要添加springboot中的web模块的starter即可，无需复杂的各种配置：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
### 2.2 编写Controller

```java
@RestController
public class HelloWorld {

    @RequestMapping("/hello")
    public String hello() {
        return "hello world!";
    }
}
```
只需要在controller上加上 @RestController 注解即可，RestController代表当前Controller是一个rest风格，返回的数据类型是字符串，可以将对象转成对应的JSON字符返回，多用于前后端分离的场景。

## 添加自定义 Servlet，Filter，Listener
在SpringBoot项目中，可以通过以下方式进行注册Servlet，Filter，Listener。

- 利用Servlet 3.0 中提供的注解

  ```java
  @WebServlet(urlPatterns = "/my")
  public class MyWebServlet extends HttpServlet {...}
  
  @WebFilter(filterName = "FirstHelloWorldFilter", urlPatterns = "/*")
  public class FirstHelloWorldFilter extends HttpFilter {...}
  
  @WebListener
  public class MyHttpListener implements HttpSessionListener {...}
  ```

- 利用Springboot中提供的Registration机制

  ```java
  // 注册servlet
  @Bean
  public ServletRegistrationBean<SpringCustomServlet> customServlet() {
      ServletRegistrationBean<SpringCustomServlet> servletRegistrationBean = new ServletRegistrationBean<>(new SpringCustomServlet());
      servletRegistrationBean.addUrlMappings("/customServlet");
      servletRegistrationBean.setLoadOnStartup(1);
      return servletRegistrationBean;
  }
  // 注册Filter
  @Bean
  public FilterRegistrationBean<SecondHelloWorldFilter> customFilter() {
      FilterRegistrationBean<SecondHelloWorldFilter> registrationBean = new FilterRegistrationBean<>(new SecondHelloWorldFilter());
      registrationBean.addInitParameter("customFilter", "SecondHelloWorldFilter");
      registrationBean.addUrlPatterns("/listener/*");
      return registrationBean;
  }
  // 注册Listener
  @Bean
   @Bean
  public ServletListenerRegistrationBean<CustomHttpListener> customListener() {
          ServletListenerRegistrationBean<CustomHttpListener> registrationBean = new ServletListenerRegistrationBean<>(new CustomHttpListener());
      return registrationBean;
  }
  ```

Filter的作用主要用于对用户的访问进行拦截，比如权限控制，过滤敏感词汇等。

Listener的作用主要用于监听HttpSession（HttpSessionListener）和ServletRequest（ServletRequestListener）等域对象的创建和销毁事件。监听域对象的属性发生修改的事件，用于在事件发生前、发生后做一些必要的处理。可用于以下方面：1、统计在线人数和在线用户2、系统启动时加载初始化信息3、统计网站访问量

## 配置文件 propeties, yml

在SpringBoot中最常用的配置文件就是application.propeties或者application.yml，这也是SpringBoot默认会自动加载的配置文件。

我们常常会将数据库连接信息配置在该文件中

```pro
# 当前激活的环境 dev test prod
spring.profiles.active=dev

# 自定义的配置项
com.leofee.username=leofee
com.leofee.usersex=male
com.leofee.userage=22

# 数据库连接信息
spring.datasource.url=jdbc:mysql://localhost:3306/learning?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&useSSL=false&serverTimezone=GMT
spring.datasource.username=root
spring.datasource.password=admin123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

在实际项目当中，如果有多个环境，那么配置文件就需要依据当前系统环境分别加载对应的配置文件，如dev，那么我们可以定义出一个application-dev.properties，这样在dev环境的时候SpringBoot就会根据spring.profiles.active中指定的值去加载dev环境对应的配置文件。

除了默认的application.properties之外，我们还可以让Springboot加载自定义配置文件

- 添加@EnableConfigurationProperties
- 利用@PropertySource(value = { "classpath:custom.properties"})引入自定义文件

这里需要注意的一点是，@PropertySource引入的文件默认是支持properties类型的，如果是yml类型的，需要在@PropertySource中指定yml文件解析的factory，即：@PropertySource(value = "classpath:db.yml", factory = YmlPropertySourceFactory.class)

```java
public class YmlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String s, EncodedResource encodedResource) throws IOException {
        Properties propertiesFromYaml = loadYamlIntoProperties(encodedResource);
        String sourceName = s != null ? s : encodedResource.getResource().getFilename();
        return new PropertiesPropertySource(sourceName, propertiesFromYaml);
    }

    private Properties loadYamlIntoProperties(EncodedResource resource) throws FileNotFoundException {
        try {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();
            return factory.getObject();
        } catch (IllegalStateException e) {
            // for ignoreResourceNotFound
            Throwable cause = e.getCause();
            if (cause instanceof FileNotFoundException) {
                throw (FileNotFoundException) e.getCause();
            }
            throw e;
        }
    }
}
```

当配置文件被SpringBoot识别后，我们可以利用一个对象去接收解析出来的配置项，首先需要再目标类上增加@Component注解，其次为了配置文件的value能和对象的属性一一对应有如下两种方法：

- 在接收对象类上增加@Component注解，并在属性上利用@Value(value = "${com.user.name}")进行注入。
- 在接收对象类上增加@ConfigurationProperties(prefix="com.user")注解，属性名和配置文件中的名称一致即可自动进行注入。

## SpringBoot 静态文件

默认情况下，Spring Boot在 classpath 根目录下从 /static （/public、/resources 或 /META-INF/resources）目录中存放静态文件内容，在WebProperties.Resources中可以看到这些目录的定义，通过WebMvcAutoConfiguration.addResourceHandlers中进行路由绑定。

```java
// staticPathPattern 默认为/**，resourceProperties.getStaticLocations()就是 /static （/public、/resources 或 /META-INF/resources目录
addResourceHandler(registry, this.mvcProperties.getStaticPathPattern(), (registration) -> {
				registration.addResourceLocations(this.resourceProperties.getStaticLocations());
				if (this.servletContext != null) {
					ServletContextResource resource = new ServletContextResource(this.servletContext, SERVLET_LOCATION);
					registration.addResourceLocations(resource);
				}
			});
```

如果需要添加自定义静态目录，可以通过实现WebMvcConfigurer重写addResourceHandlers该方法进行自定义。

```java
@Configuration
public class MyWebMvcAutoConfigurationAdapter implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 拦截路径
        ResourceHandlerRegistration registration = registry.addResourceHandler("/custom/**");
        // 静态文件目录
        registration.addResourceLocations("classpath:/custom/");
    }
}
```



## WebMVC

spring-boot-starter-web还集成了SpringMVC的相关功能，从WebMvcAutoConfiguration类中可以看到MVC相关的很多配置，比如ViewResolver视图解析器。



## 集成Thymeleaf

添加starter

```xml
 <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-thymeleaf</artifactId>
 </dependency>
```

创建模板文件在resources/template下

编写Controller，记住用@Controller，如果是@RestController不会返回模板视图，利用Model返回数据给模板进行渲染。

```java
@Controller
public class LoginController {

    @RequestMapping("/login")
    public String login(HttpSession session, Model model) {
        Person person = new Person();
        person.setName("leofee");
        person.setAge(18);
        person.setSex("男");
        model.addAttribute("person", person);
        return "index";
    }
```



## 国际化支持






## 3. spring-boot 集成 jpa

### 3.1 引入jpa模块
加入jpa的依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<!-- mysql数据库驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```
### 3.2 在配置文件中添加数据库的连接信息和jpa的配置
```
spring.datasource.url=jdbc:mysql://localhost:3306/spring-boot-learning
spring.datasource.username=root
spring.datasource.password=admin123
spring.datasource.driver-class-name=com.mysql.jdbc.Driver

spring.jpa.properties.hibernate.hbm2ddl.auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql= true
```
该参数的作用为: 自动创建，更新，验证数据库表结构，一共可选四种参数:

1 `create`： 每次加载 hibernate 时都会删除上一次的生成的表，然后根据你的 model 类再重新来生成新表，
哪怕两次没有任何改变也要这样执行，这就是导致数据库表数据丢失的一个重要原因。

2 `create-drop` ：每次加载 hibernate 时根据 model 类生成表，但是 sessionFactory 一关闭,表就自动删除

3 `update`：最常用的属性，第一次加载 hibernate 时根据 model 类会自动建立起表的结构（前提是先建立好数据库），
以后加载 hibernate 时根据 model 类自动更新表结构，即使表结构改变了但表中的行仍然存在不会删除以前的行。
要注意的是当部署到服务器后，表结构是不会被马上建立起来的，是要等 应用第一次运行起来后才会。

4 `validate`：每次加载 hibernate 时，验证创建数据库表结构，只会和数据库中的表进行比较，不会创建新表，但是会插入新值。

### 3.3 配置完成后新建一个实体和实体对应的Dao

1 Dao 继承 JpaRepository 就可以使用已经定义好的基本的增删改查

```
public interface PersonDao extends JpaRepository<Person, Long> {

    List<Person> findByName(String name);
}
```
2 在controller里调用

```
@RestController
public class PersonController {

    @Autowired
    private PersonDao personDao;

    @RequestMapping("/getPerson")
    public Person getPerson(Long personId) {
        return personDao.findById(personId).orElseGet(Person::new);
    }

    @RequestMapping("/savePerson")
    public void savePerson(@RequestBody Person person) {
        personDao.save(person);
    }
}
```
3 利用mock mvc 的方式测试一下

```
@AutoConfigureMockMvc
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Sql("classpath:person.sql")
    @Transactional
    @Rollback
    @Test
    public void getPerson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/getPerson")
                .contentType(MediaType.APPLICATION_JSON)
                .param("personId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("leofee"));
    }


    @Test
    public void savePerson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/savePerson")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"小明\",\"sex\":\"male\",\"age\":25}"))
                .andExpect(status().isOk());
    }
}
```