# spring-boot-learning
学习如何使用Spring boot，spring boot 的设计理念-约定优于配置，正由于这样的设计理念才让Spring boot 能够开箱即用。
## 1. spring boot - hello world
如何新建一个spring boot 项目<br>
IDEA工具<br>
<li>File -> new project 
<li>选中Spring Initializr，默认即可
<li>next ->
  
## 2. spring boot - web
使用spring boot 进行web应用的开发<br>
### 2.1 首先需要引入web模块<br>
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
### 2.2 controller
```
@RestController
public class HelloWorld {

    @RequestMapping("/hello")
    public String hello() {
        return "hello world!";
    }
}
```
只需要在controller上加上 @RestController 注解即可

### 2.3 访问controller
在启动类上点击 run main<br>
浏览器输入 http://localhost:8080/hello 就会出现 hello world!
## 3. spring-boot 集成 jpa
### 3.1 引入jpa模块
加入jpa的依赖
```
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
<ul>
<li>1. create： 每次加载 hibernate 时都会删除上一次的生成的表，然后根据你的 model 类再重新来生成新表，<br>
  哪怕两次没有任何改变也要这样执行，这就是导致数据库表数据丢失的一个重要原因。
  
<li>2.create-drop ：每次加载 hibernate 时根据 model 类生成表，但是 sessionFactory 一关闭,表就自动删除
  
<li>3.update：最常用的属性，第一次加载 hibernate 时根据 model 类会自动建立起表的结构（前提是先建立好数据库），<br>
以后加载 hibernate 时根据 model 类自动更新表结构，即使表结构改变了但表中的行仍然存在不会删除以前的行。<br>
要注意的是当部署到服务器后，表结构是不会被马上建立起来的，是要等 应用第一次运行起来后才会。
  
<li>4.validate：每次加载 hibernate 时，验证创建数据库表结构，只会和数据库中的表进行比较，不会创建新表，但是会插入新值。
</ul>
### 3.3 配置完成后新建一个实体和实体对应的Dao
Dao 继承 JpaRepository 就可以使用已经定义好的基本的增删改查
```
public interface PersonDao extends JpaRepository<Person, Long> {

    List<Person> findByName(String name);
}
```
在controller里调用
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
test
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
