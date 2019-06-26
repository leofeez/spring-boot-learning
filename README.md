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
