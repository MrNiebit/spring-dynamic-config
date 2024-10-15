package cn.lacknb.blog.springdynamicconfig.controller;

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

    @RequestMapping("/hello")
    public String hello(String name) {
        return "hello " + name;
    }

}
