package cn.lacknb.blog.springdynamicconfig.controller;

import cn.lacknb.blog.springdynamicconfig.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h2>  </h2>
 *
 * @description:
 * @menu
 * @author: nbh
 * @description:
 * @date: 2024/10/15 8:41
 **/
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ConfigurableEnvironment environment;

    @Value("${user.account}")
    private String userAccount;

    @Autowired
    private User user;

    @RequestMapping("/hello")
    public String hello(String name) {
        return userAccount + " -> hello -> " + name;
    }

    @RequestMapping("/user")
    public User user() {
        return user;
    }

    @RequestMapping("/config")
    public String config() {
        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            System.out.println(propertySource.getName());
            System.out.println(propertySource.getSource());
        }
        return "environment";
    }

}
